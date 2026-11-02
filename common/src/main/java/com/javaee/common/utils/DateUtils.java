package com.javaee.common.utils;

import com.javaee.common.constant.CommonConstant;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 日期时间工具，提供格式化、解析与日期时间转换能力。
 *
 * <p>统一使用 java.time 与系统默认时区；入参为空时返回空串、null 或 0，
 * 不抛出空指针异常。</p>
 *
 * @author qxk
 */
public class DateUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(CommonConstant.DATE_FORMAT);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(CommonConstant.TIME_FORMAT);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(CommonConstant.DATE_TIME_FORMAT);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern(CommonConstant.TIMESTAMP_FORMAT);

    /**
     * 获取当前日期。
     *
     * @return 当前系统日期
     */
    public static LocalDate nowDate() {
        return LocalDate.now();
    }

    /**
     * 获取当前时间。
     *
     * @return 当前系统时间
     */
    public static LocalTime nowTime() {
        return LocalTime.now();
    }

    /**
     * 获取当前日期时间。
     *
     * @return 当前系统日期时间
     */
    public static LocalDateTime nowDateTime() {
        return LocalDateTime.now();
    }

    /**
     * 获取当前时间戳。
     *
     * @return 当前毫秒级时间戳
     */
    public static long nowTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 按 yyyy-MM-dd 格式化日期。
     *
     * @param date 待格式化的日期
     * @return 格式化后的字符串；入参为空时返回空串
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DATE_FORMATTER);
    }

    /**
     * 按 HH:mm:ss 格式化时间。
     *
     * @param time 待格式化的时间
     * @return 格式化后的字符串；入参为空时返回空串
     */
    public static String formatTime(LocalTime time) {
        if (time == null) {
            return "";
        }
        return time.format(TIME_FORMATTER);
    }

    /**
     * 按 yyyy-MM-dd HH:mm:ss 格式化日期时间。
     *
     * @param dateTime 待格式化的日期时间
     * @return 格式化后的字符串；入参为空时返回空串
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    /**
     * 按 yyyy-MM-dd HH:mm:ss.SSS 格式化日期时间。
     *
     * @param dateTime 待格式化的日期时间
     * @return 带毫秒的格式化字符串；入参为空时返回空串
     */
    public static String formatTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(TIMESTAMP_FORMATTER);
    }

    /**
     * 按 yyyy-MM-dd 格式化日期。
     *
     * @param date 待格式化的日期
     * @return 格式化后的字符串；入参为空时返回空串
     */
    public static String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(CommonConstant.DATE_FORMAT);
        return sdf.format(date);
    }

    /**
     * 按 yyyy-MM-dd HH:mm:ss 格式化 Date。
     *
     * @param date 待格式化的日期
     * @return 格式化后的字符串；入参为空时返回空串
     */
    public static String formatDateTime(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(CommonConstant.DATE_TIME_FORMAT);
        return sdf.format(date);
    }

    /**
     * 按 yyyy-MM-dd 解析日期字符串。
     *
     * @param dateStr 日期字符串
     * @return 解析结果；入参为空时返回 null
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }

    /**
     * 按 yyyy-MM-dd HH:mm:ss 解析日期时间字符串。
     *
     * @param dateTimeStr 日期时间字符串
     * @return 解析结果；入参为空时返回 null
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
    }

    /**
     * 按 yyyy-MM-dd 解析日期字符串。
     *
     * @param dateStr 日期字符串
     * @return 解析结果；入参为空时返回 null
     */
    public static Date parseDateToDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(CommonConstant.DATE_FORMAT);
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 按 yyyy-MM-dd HH:mm:ss 把字符串解析为 Date。
     *
     * @param dateTimeStr 日期时间字符串
     * @return 解析结果；入参为空或格式非法时返回 null
     */
    public static Date parseDateTimeToDate(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(CommonConstant.DATE_TIME_FORMAT);
            return sdf.parse(dateTimeStr);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 将 LocalDate 转换为 Date，时间取当天零点。
     *
     * @param localDate 待转换的日期
     * @return 转换后的 Date；入参为空时返回 null
     */
    public static Date toDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 将 LocalDateTime 转换为 Date。
     *
     * @param localDateTime 待转换的日期时间
     * @return 转换后的 Date；入参为空时返回 null
     */
    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 将 Date 转换为 LocalDate。
     *
     * @param date 待转换的日期
     * @return 转换后的 LocalDate；入参为空时返回 null
     */
    public static LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * 将 Date 转换为 LocalDateTime。
     *
     * @param date 待转换的日期
     * @return 转换后的 LocalDateTime；入参为空时返回 null
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * 将毫秒时间戳转换为 LocalDateTime。
     *
     * @param timestamp 毫秒时间戳
     * @return 转换后的日期时间
     */
    public static LocalDateTime timestampToDateTime(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

    /**
     * 将 LocalDateTime 转换为毫秒时间戳。
     *
     * @param dateTime 日期时间
     * @return 毫秒时间戳；入参为空时返回 0
     */
    public static long dateTimeToTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            return 0;
        }
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * 计算两个日期之间的天数差。
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 天数差；任一入参为空时返回 0
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return Duration.between(startDate.atStartOfDay(), endDate.atStartOfDay()).toDays();
    }

    /**
     * 计算两个日期时间之间的小时差。
     *
     * @param startDateTime 开始日期时间
     * @param endDateTime 结束日期时间
     * @return 小时差；任一入参为空时返回 0
     */
    public static long hoursBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            return 0;
        }
        return Duration.between(startDateTime, endDateTime).toHours();
    }

    /**
     * 计算两个日期时间之间的分钟差。
     *
     * @param startDateTime 开始日期时间
     * @param endDateTime 结束日期时间
     * @return 分钟差；任一入参为空时返回 0
     */
    public static long minutesBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            return 0;
        }
        return Duration.between(startDateTime, endDateTime).toMinutes();
    }

    /**
     * 计算两个日期时间之间的秒差。
     *
     * @param startDateTime 开始日期时间
     * @param endDateTime 结束日期时间
     * @return 秒差；任一入参为空时返回 0
     */
    public static long secondsBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            return 0;
        }
        return Duration.between(startDateTime, endDateTime).getSeconds();
    }

    /**
     * 在日期上增加指定天数。
     *
     * @param date 基准日期
     * @param days 增加的天数，可为负数
     * @return 新日期；入参为空时返回 null
     */
    public static LocalDate plusDays(LocalDate date, long days) {
        if (date == null) {
            return null;
        }
        return date.plusDays(days);
    }

    /**
     * 在日期时间上增加指定天数。
     *
     * @param dateTime 基准日期时间
     * @param days 增加的天数，可为负数
     * @return 新日期时间；入参为空时返回 null
     */
    public static LocalDateTime plusDays(LocalDateTime dateTime, long days) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusDays(days);
    }

    /**
     * 在日期时间上增加指定小时数。
     *
     * @param dateTime 基准日期时间
     * @param hours 增加的小时数，可为负数
     * @return 新日期时间；入参为空时返回 null
     */
    public static LocalDateTime plusHours(LocalDateTime dateTime, long hours) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusHours(hours);
    }

    /**
     * 在日期时间上增加指定分钟数。
     *
     * @param dateTime 基准日期时间
     * @param minutes 增加的分钟数，可为负数
     * @return 新日期时间；入参为空时返回 null
     */
    public static LocalDateTime plusMinutes(LocalDateTime dateTime, long minutes) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusMinutes(minutes);
    }

    /**
     * 判断两个日期时间是否为同一天。
     *
     * @param dateTime1 第一个日期时间
     * @param dateTime2 第二个日期时间
     * @return true-同一天；任一入参为空时返回 false
     */
    public static boolean isSameDay(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        if (dateTime1 == null || dateTime2 == null) {
            return false;
        }
        return dateTime1.toLocalDate().equals(dateTime2.toLocalDate());
    }

    /**
     * 判断日期时间是否为今天。
     *
     * @param dateTime 待判断的日期时间
     * @return true-今天；入参为空时返回 false
     */
    public static boolean isToday(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        return dateTime.toLocalDate().equals(LocalDate.now());
    }

    /**
     * 获取指定日期的开始时间。
     *
     * @param date 日期
     * @return 当天零点；入参为空时返回 null
     */
    public static LocalDateTime startOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay();
    }

    /**
     * 获取指定日期的结束时间。
     *
     * @param date 日期
     * @return 当天最后一刻；入参为空时返回 null
     */
    public static LocalDateTime endOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return LocalDateTime.of(date, LocalTime.MAX);
    }
}
