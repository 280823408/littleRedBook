package com.example.littleredbook.utils;


import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * 日期格式转换工具类
 *
 * <p>功能说明：
 * 1. 提供时间戳与字符串互转能力<br>
 * 2. 支持数据库时间字段格式化输出<br>
 * 3. 实现空值安全处理机制<br>
 * 4. 统一系统时间格式标准<br>
 *
 * <p>典型场景：
 * - 数据库Timestamp类型字段展示<br>
 * - 前后端时间数据格式转换<br>
 * - 空值时间字段容错处理<br>
 * - 系统日志时间标准化输出<br>
 *
 * @author Mike
 * @since 2025/2/28
 */
public class DateUtils {

    /**
     * 时间戳转标准格式字符串
     * @param timestamp 数据库时间戳对象
     * @return "yyyy-MM-dd HH:mm:ss"格式字符串，空值返回"null"
     */
    public static String TimeStampToString(Timestamp timestamp) {
        if (timestamp == null) return "null";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(timestamp);
    }

    /**
     * 标准格式字符串转时间戳
     * @param time 符合格式的字符串
     * @return java.sql.Timestamp对象，无效输入返回null
     * @throws ParseException 时间格式解析异常
     */
    public static Timestamp StringToTimeStamp(String time) throws ParseException {
        if (time == null || time.equals("null")) return null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return new Timestamp(simpleDateFormat.parse(time).getTime());
    }
}
