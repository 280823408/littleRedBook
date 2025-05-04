package com.example.notes.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.TextSimilarity;
import com.example.littleredbook.dto.NoteDTO;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.LikeNote;
import com.example.littleredbook.entity.User;
import com.example.notes.service.INoteService;
import com.example.notes.utils.CommunityClient;
import com.example.notes.utils.MessagesClient;
import com.example.notes.utils.RecommendUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
@Service
public class CFRecommendNotesImpl implements RecommendUtils {
    // 最大邻居数
    private final int MAX_NEIGHBORS = 10;
    // 时间衰减系数(半衰期30天)
    private final double DECAY_RATE = Math.log(2) / 30.0;
    @Resource
    private MessagesClient messagesClient;

    @Resource
    private CommunityClient communityClient;

    @Resource
    private INoteService noteService;

    @Override
    public Result recommendNotes(Integer userId) {
        try {
            // 1. 获取用户社交关系数据
            Map<Integer, Double> friendWeights = getFriendWeights(userId);

            // 2. 获取用户和好友的点赞记录
            Map<Integer, List<LikeNote>> userLikesMap = getUserAndFriendLikes(userId, friendWeights.keySet());

            // 3. 计算笔记推荐得分
            Map<Integer, Double> noteScores = calculateNoteScores(userId, userLikesMap, friendWeights);

            // 4. 获取推荐笔记详情
            return getRecommendedNotes(noteScores);

        } catch (Exception e) {
            return Result.fail("推荐系统异常: " + e.getMessage());
        }
    }

    @Override
    public Result recommendNotesByContent(Integer userId, String content) {
        try {

            // 1. 获取用户历史点赞的笔记内容
            List<NoteDTO> userLikedNotes = getUserLikedNotes(userId);

            // 2. 计算内容相似度得分
            Map<Integer, Double> contentScores = calculateContentScores(userLikedNotes, content);

            // 3. 获取协同过滤推荐结果
            Map<Integer, Double> cfScores = getCollaborativeFilteringScores(userId);

            // 4. 混合两种推荐结果
            Map<Integer, Double> hybridScores = hybridRecommendation(cfScores, contentScores);

            // 5. 获取最终推荐结果
            return getRecommendedNotes(hybridScores);

        } catch (Exception e) {
            return Result.fail("内容推荐异常: " + e.getMessage());
        }
    }

    /**
     * 获取好友权重
     */
    private Map<Integer, Double> getFriendWeights(Integer userId) {
        ArrayList<Map<String, Object>> friendsResultData = (ArrayList<Map<String, Object>>)
                communityClient.getFriends(userId).getData();
        List<User> friends = new ArrayList<>();
        friendsResultData.forEach(map -> {
            User user = BeanUtil.mapToBean(map, User.class, true);
            friends.add(user);
        });

        // 使用LinkedHashMap保持插入顺序
        Map<Integer, Double> friendWeights = new LinkedHashMap<>();

        // 为每个好友分配基础权重
        friends.forEach(friend -> {
            // 基础权重 + 亲密度因素(这里简化处理，实际可根据互动频率等计算)
            double weight = 0.7 + (friend.getFansNum() != null ? friend.getFansNum() * 0.001 : 0);
            friendWeights.put(friend.getId(), Math.min(weight, 1.0)); // 权重不超过1
        });

        return friendWeights;
    }

    /**
     * 获取用户和好友的点赞记录
     */
    private Map<Integer, List<LikeNote>> getUserAndFriendLikes(Integer userId, Set<Integer> friendIds) {
        Map<Integer, List<LikeNote>> likesMap = new HashMap<>();

        // 获取用户自身点赞记录
        ArrayList<Map<String, Object>> selfLikesResultData = (ArrayList<Map<String, Object>>)
                messagesClient.getLikeNoteRecordsByUserId(userId).getData();
        List<LikeNote> selfLikesResult = new ArrayList<>();
        selfLikesResultData.forEach(map -> {
            LikeNote likeNote = BeanUtil.mapToBean(map, LikeNote.class, true);
            selfLikesResult.add(likeNote);
        });
        likesMap.put(userId, selfLikesResult);

        // 获取好友点赞记录
        for (Integer friendId : friendIds) {
            ArrayList<Map<String, Object>> friendLikesResultData =  (ArrayList<Map<String, Object>>)
                    messagesClient.getLikeNoteRecordsByUserId(friendId).getData();
            List<LikeNote> friendLikesResult = new ArrayList<>();
            friendLikesResultData.forEach(map -> {
                LikeNote likeNote = BeanUtil.mapToBean(map, LikeNote.class, true);
                friendLikesResult.add(likeNote);
            });
            likesMap.put(friendId, friendLikesResult);
        }

        return likesMap;
    }

    /**
     * 计算笔记推荐得分
     */
    private Map<Integer, Double> calculateNoteScores(Integer userId,
                                                     Map<Integer, List<LikeNote>> userLikesMap,
                                                     Map<Integer, Double> friendWeights) {
        // 使用HashMap存储未排序的得分
        Map<Integer, Double> noteScores = new HashMap<>();

        // 获取用户已点赞的笔记ID(用于去重)
        Set<Integer> userLikedNoteIds = userLikesMap.getOrDefault(userId, Collections.emptyList())
                .stream()
                .map(LikeNote::getNoteId)
                .collect(Collectors.toSet());

        // 处理每个用户的点赞记录
        for (Map.Entry<Integer, List<LikeNote>> entry : userLikesMap.entrySet()) {
            Integer currentUserId = entry.getKey();
            List<LikeNote> likes = entry.getValue();

            // 确定当前用户的权重(自身权重为1，好友权重按亲密度)
            double weight = currentUserId.equals(userId) ? 1.0 :
                    friendWeights.getOrDefault(currentUserId, 0.5);

            // 处理每条点赞记录
            for (LikeNote like : likes) {
                int noteId = like.getNoteId();

                // 跳过用户已经点赞过的笔记
                if (userLikedNoteIds.contains(noteId)) {
                    continue;
                }

                // 计算时间衰减因子
                double timeDecay = calculateTimeDecay(like.getLikeTime());

                // 计算当前得分 = 用户权重 × 时间衰减因子
                double currentScore = weight * timeDecay;

                // 累加到总得分
                noteScores.merge(noteId, currentScore, Double::sum);
            }
        }

        // 按得分降序排序
        return noteScores.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }


    /**
     * 获取推荐笔记详情
     */
    private Result getRecommendedNotes(Map<Integer, Double> noteScores) {
        List<Integer> recommendedNoteIds = new ArrayList<>(noteScores.keySet());

        // 获取笔记详情
        return noteService.getNotesByIds(recommendedNoteIds);
    }

    /**
     * 计算时间衰减因子(指数衰减)
     */
    private double calculateTimeDecay(Timestamp likeTime) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime likeDateTime = likeTime.toLocalDateTime();
        long daysPassed = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() -
                likeDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        daysPassed = daysPassed / (1000 * 60 * 60 * 24);

        return Math.exp(-DECAY_RATE * daysPassed);
    }

    /**
     * 获取用户点赞过的笔记
    */
    private List<NoteDTO> getUserLikedNotes(Integer userId) {
        ArrayList<Map<String, Object>> likesResultData = (ArrayList<Map<String, Object>>)
                messagesClient.getLikeNoteRecordsByUserId(userId).getData();
        List<LikeNote> likes = new ArrayList<>();
        likesResultData.forEach(map -> {
            LikeNote likeNote = BeanUtil.mapToBean(map, LikeNote.class, true);
            likes.add(likeNote);
        });
        List<Integer> noteIds = likes.stream().map(LikeNote::getNoteId).collect(Collectors.toList());
        return (List<NoteDTO>) noteService.getNotesByIds(noteIds).getData();
    }

    /**
    * 计算内容相似度得分
    */
    private Map<Integer, Double> calculateContentScores(List<NoteDTO> userLikedNotes, String queryContent) {
        // 获取所有候选笔记(简化处理，实际应该有限制条件)
        List<NoteDTO> allNotes = (List<NoteDTO>) noteService.getAllNotesSortedByLikeNum().getData();

        Map<Integer, Double> scores = new HashMap<>();

        // 计算每个笔记与用户历史喜欢内容和当前查询的相似度
        for (NoteDTO note : allNotes) {
            double maxSimilarity = 0;

            // 与用户历史喜欢笔记的相似度
            for (NoteDTO likedNote : userLikedNotes) {
                double similarity = calculateTextSimilarity(note.getContent(), likedNote.getContent());
                maxSimilarity = Math.max(maxSimilarity, similarity);
            }

            // 与当前查询内容的相似度
            double querySimilarity = calculateTextSimilarity(note.getContent(), queryContent);

            // 综合得分
            scores.put(note.getId(), 0.7 * maxSimilarity + 0.3 * querySimilarity);
        }

        return scores;
    }

    /**
     * 文本相似度计算(使用Hutool工具)
    */
    private double calculateTextSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null || text1.isEmpty() || text2.isEmpty()) {
            return 0;
        }
        return TextSimilarity.similar(text1, text2);
    }

    /**
     * 获取协同过滤推荐得分
    */
    private Map<Integer, Double> getCollaborativeFilteringScores(Integer userId) {
        Map<Integer, Double> friendWeights = getFriendWeights(userId);
        Map<Integer, List<LikeNote>> userLikesMap = getUserAndFriendLikes(userId, friendWeights.keySet());
        return calculateNoteScores(userId, userLikesMap, friendWeights);
    }

    /**
     * 混合推荐策略
    */
    private Map<Integer, Double> hybridRecommendation(Map<Integer, Double> cfScores,
                                                      Map<Integer, Double> contentScores) {
        // 归一化处理
        normalizeScores(cfScores);
        normalizeScores(contentScores);

        // 混合得分(可调整权重)
        Map<Integer, Double> hybridScores = new HashMap<>();

        // 合并协同过滤得分
        cfScores.forEach((noteId, score) -> {
            hybridScores.merge(noteId, score * 0.6, Double::sum);
        });

        // 合并内容得分
        contentScores.forEach((noteId, score) -> {
            hybridScores.merge(noteId, score * 0.4, Double::sum);
        });

        return hybridScores;
    }

    /**
     * 得分归一化(0-1范围)
    */
    private void normalizeScores(Map<Integer, Double> scores) {
        if (scores.isEmpty()) return;

        double max = Collections.max(scores.values());
        if (max <= 0) return;

        scores.replaceAll((k, v) -> v / max);
    }

}
