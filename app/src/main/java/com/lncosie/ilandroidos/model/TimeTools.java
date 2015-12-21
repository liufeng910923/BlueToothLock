package com.lncosie.ilandroidos.model;

/**
 * 时间的各种表示之间的互相转换
 */
public class TimeTools {
    public static long toTime(byte[] time) {
        if (time == null || time.length < 5)
            return 0;
        long ti = 0;
        for (int i = 0; i < 5; i++) {
            ti = ti * 100 + time[i];
        }
        return ti;
    }
    public static long toTime(byte[] time,int idx) {
        if (time == null || time.length-5-idx < 0)
            return 0;
        long ti = 0;
        for (int i = idx; i < idx+5; i++) {
            ti = ti * 100 + time[i];
        }
        return ti;
    }
    public static String toString(long time) {
        if (time == 0)
            return "";
        return String.format("20%02d-%02d-%02d %02d:%02d",
                time / 1_00_00_00_00L % 100,
                time / 100_00_00 % 100,
                time / 100_00 % 100,
                time / 100 % 100,
                time % 100
        );
    }
    public static String rawString(long time) {
        if (time == 0)
            return "";
        return String.format("20%02d%02d%02d%02d%02d",
                time / 1_00_00_00_00L % 100,
                time / 100_00_00 % 100,
                time / 100_00 % 100,
                time / 100 % 100,
                time % 100
        );
    }
    public static String toDayString2(long time) {
        return String.format("20%02d/%02d/%02d",
                time / 100_00_00_00L % 100,
                time / 100_00_00 % 100,
                time / 100_00 % 100
        );
    }

    public static String toDayString(long time) {
        return String.format("20%02d-%02d-%02d",
                time / 100_00_00_00L % 100,
                time / 100_00_00 % 100,
                time / 100_00 % 100
        );
    }

    public static String toTimeString(long time) {
        return String.format("%02d:%02d",
                time / 100 % 100,
                time % 100
        );
    }

    public static byte[] toTime(int year, int month, int day, int hour, int min) {
        byte[] time = new byte[6];
        time[0] = (byte) (year % 100);
        time[1] = (byte) month;
        time[2] = (byte) day;
        time[3] = (byte) hour;
        time[4] = (byte) min;
        time[5] = (byte) 0x0;
        return time;
    }

    public static long toDayLow(int year, int month, int day) {
        long ti = 0;
        return (year % 100) * 100_00_00_00L + month * 100_00_00L + day * 100_00L;
    }

    public static long toDayUp(int year, int month, int day) {
        long ti = 0;
        return (year % 100) * 100_00_00_00L + month * 100_00_00L + day * 100_00L + 2400;
    }
}
