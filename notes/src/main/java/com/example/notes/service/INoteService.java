package com.example.notes.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.Note;

import java.text.ParseException;
import java.util.List;

/**
 * 笔记业务服务接口
 *
 * <p>功能说明：
 * 1. 定义笔记核心业务操作规范<br>
 * 2. 扩展MyBatis-Plus通用服务接口<br>
 * 3. 声明缓存策略和事务边界<br>
 * 4. 规范跨服务调用数据格式<br>
 *
 * <p>接口特性：
 * - 查询方法支持多级缓存<br>
 * - 写操作保证数据最终一致性<br>
 * - 支持复杂结果集的内存排序<br>
 * - 统一异常处理规范<br>
 *
 * @author Mike
 * @since 2025/3/1
 */
public interface INoteService extends IService<Note> {
    /**
     * 根据ID查询完整笔记信息
     * @param id 笔记唯一标识
     * @return Result标准响应（包含NoteDTO或错误信息）
     * @throws ParseException 时间格式解析异常
     */
    Result getNoteById(Integer id) throws ParseException;

    /**
     * 根据ID批量查询笔记信息
     * @param ids 笔记ID列表
     * @return Result标准响应（包含NoteDTO或错误信息）
     */
    Result getNotesByIds(List<Integer> ids);

    /**
     * 根据作者ID查询其所发布的笔记ID列表
     * @param authorId 作者唯一标识
     * @return Result标准响应（包含笔记ID列表或错误信息）
     */
    Result getNoteIdsByAuthorId(Integer authorId);

    /**
     * 查询用户所有笔记
     * @param userId 用户唯一标识
     * @return Result标准响应（笔记列表或空提示）
     */
    Result getNotesByUserId(Integer userId);

    /**
     * 根据标题关键词模糊查询
     * @param title 搜索关键词（支持模糊匹配）
     * @return Result标准响应（匹配的笔记列表）
     */
    Result getNotesByTitle(String title);

    /**
     * 获取全站笔记按点赞量排序
     * @return Result标准响应（点赞降序列表）
     */
    Result getAllNotesSortedByLikeNum();

    /**
     * 获取全站笔记按创建时间排序
     * @return Result标准响应（时间倒序列表）
     */
    Result getAllNotesSortedByCreatTime();

    /**
     * 根据标签查询关联笔记
     * @param tagId 标签唯一标识
     * @return Result标准响应（标签关联笔记列表）
     */
    Result getNotesByTag(Integer tagId);

    /**
     * 创建新笔记（带缓存清理）
     * @param note 笔记实体对象
     * @return Result标准响应（操作结果）
     */
    Result addNote(Note note);

    /**
     * 更新笔记信息（带缓存失效）
     * @param note 包含更新字段的笔记实体
     * @return Result标准响应（操作结果）
     */
    Result updateNote(Note note);

    /**
     * 点赞回复评论
     * @param id 笔记ID
     * @param userId 用户ID
     * @return Result标准响应（操作结果）
     */
    Result likeNote(Integer id, Integer userId);

    /**
     * 收藏笔记
     * @param id 笔记ID
     * @param userId 用户ID
     * @return Result标准响应（操作结果）
     */
    Result collectNote(Integer id, Integer userId);

    /**
     * 更新笔记评论点赞数（带缓存失效）
     * @param id 评论ID
     * @param isLike 是否点赞
     * @return Result标准响应（操作结果）
     */
    Result updateNoteLikeNum(Integer id, boolean isLike);

    /**
     * 更新笔记收藏数（带缓存失效）
     * @param id 笔记ID
     * @param isCollection 是否收藏
     * @return Result标准响应
     */
    Result updateNoteCollectionNum(Integer id, boolean isCollection);
}
