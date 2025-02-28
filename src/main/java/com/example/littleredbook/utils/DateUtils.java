package com.example.littleredbook.utils;


import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
public class DateUtils {
    public static String TimeStampToString(Timestamp timestamp) {
        if (timestamp == null) return "null";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(timestamp);
    }
    public static Timestamp StringToTimeStamp(String time) throws ParseException {
        if (time == null || time.equals("null")) return null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return new Timestamp(simpleDateFormat.parse(time).getTime());
    }
}
