package com.example.healthAppAndroid.core;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.example.healthAppAndroid.R;

public abstract class AppColors {
    public static int gray = 0;
    static int gray2 = 0;
    static int gray5 = 0;
    public static int blue = 0;
    public static int orange = 0;
    public static int green = 0;
    public static int red = 0;
    public static int labelDisabled = 0;
    public static int labelNormal = 0;

    static void setColors(Context context) {
        gray = ContextCompat.getColor(context, R.color.systemGray);
        gray2 = ContextCompat.getColor(context, R.color.systemGray2);
        gray5 = ContextCompat.getColor(context, R.color.systemGray5);
        blue = ContextCompat.getColor(context, R.color.systemBlue);
        orange = ContextCompat.getColor(context, R.color.systemOrange);
        green = ContextCompat.getColor(context, R.color.systemGreen);
        red = ContextCompat.getColor(context, R.color.systemRed);
        labelDisabled = ContextCompat.getColor(context, R.color.secondaryLabel);
        labelNormal = ContextCompat.getColor(context, R.color.label);
    }
}
