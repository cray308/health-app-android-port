package com.example.healthappandroid.common.helpers;

import com.example.healthappandroid.common.shareddata.AppUserData;

import java.util.TimeZone;

public abstract class DateHelper {
    public static long daySeconds = 86400;
    public static long weekSeconds = 604800;

    public static long twoYearsAgo() {
        return AppUserData.shared.weekStart - 63244800;
    }

    public static int getOffsetFromGMT(long date) {
        return TimeZone.getDefault().getOffset(date * 1000) / 1000;
    }
}
