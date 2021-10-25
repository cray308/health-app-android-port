package com.example.healthAppAndroid.common.helpers;

import com.example.healthAppAndroid.common.shareddata.AppUserData;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

public abstract class DateHelper {
    private final static long daySeconds = 86400;
    public final static long weekSeconds = 604800;

    private static long getStartOfDay(long date, LocalDateTime info) {
        int seconds = (info.getHour() * 3600) + (info.getMinute() * 60) + info.getSecond();
        return date - seconds;
    }

    public static long twoYearsAgo() {
        return AppUserData.shared.weekStart - 63244800;
    }

    public static LocalDateTime localTime(long date) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(date),
                                       TimeZone.getDefault().toZoneId());
    }

    public static long getCurrentTime() {
        return Instant.now().getEpochSecond();
    }

    public static long calcStartOfWeek(long date) {
        LocalDateTime localInfo = localTime(date);
        int weekday = localInfo.getDayOfWeek().getValue();

        if (weekday == 1) return getStartOfDay(date, localInfo);

        date -= weekSeconds;
        while (weekday != 1) {
            date += daySeconds;
            weekday = weekday == 7 ? 1 : weekday + 1;
        }
        localInfo = localTime(date);
        return getStartOfDay(date, localInfo);
    }

    public static int getOffsetFromGMT(long date) {
        return TimeZone.getDefault().getOffset(date * 1000) / 1000;
    }
}
