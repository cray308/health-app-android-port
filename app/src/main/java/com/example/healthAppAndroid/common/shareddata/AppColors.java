package com.example.healthAppAndroid.common.shareddata;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.example.healthAppAndroid.R;

public abstract class AppColors {
    public static int gray = 0;
    public static int blue = 0;
    public static int orange = 0;
    public static int green = 0;
    public static int red = 0;
    public static int labelDisabled = 0;
    public static int labelNormal = 0;
    public static final int[] chartColors = {0, 0, 0, 0};

    public static void setColors(Context context) {
        gray = ContextCompat.getColor(context, R.color.systemGray);
        blue = ContextCompat.getColor(context, R.color.systemBlue);
        orange = ContextCompat.getColor(context, R.color.systemOrange);
        green = ContextCompat.getColor(context, R.color.systemGreen);
        red = ContextCompat.getColor(context, R.color.systemRed);
        labelDisabled = ContextCompat.getColor(context, R.color.secondaryLabel);
        labelNormal = ContextCompat.getColor(context, R.color.label);
        chartColors[0] = ContextCompat.getColor(context, R.color.chartBlue);
        chartColors[1] = ContextCompat.getColor(context, R.color.chartGreen);
        chartColors[2] = ContextCompat.getColor(context, R.color.chartOrange);
        chartColors[3] = ContextCompat.getColor(context, R.color.chartPink);
    }
}
