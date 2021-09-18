package com.example.healthappandroid.common.helpers;

import android.view.View;

import java.util.Locale;

public abstract class ViewHelper {
    public static int getTag(View v) {
        int tag = v.getId();
        return --tag;
    }

    public static void setTag(View v, int tag) {
        v.setId(tag + 1);
    }

    public static String format(String format, Object... args) {
        return String.format(Locale.US, format, args);
    }
}
