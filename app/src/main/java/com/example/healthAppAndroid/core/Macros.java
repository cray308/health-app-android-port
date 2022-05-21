package com.example.healthAppAndroid.core;

import android.icu.util.LocaleData;
import android.icu.util.ULocale;
import android.os.Build;

import java.util.Locale;

public abstract class Macros {
    static final long weekSeconds = 604800;
    static final long daySeconds = 86400;
    static final int hourSeconds = 3600;
    public static final float toKg = 0.453592f;

    public static boolean isMetric(Locale locale) {
        ULocale uLocale = ULocale.forLocale(locale);
        return !LocaleData.getMeasurementSystem(uLocale).equals(LocaleData.MeasurementSystem.US);
    }

    public static boolean onEmulator() {
        String hardware = Build.HARDWARE;
        return hardware.contains("goldfish") || hardware.contains("ranchu");
    }

    public static float savedMassFactor(boolean metric) { return metric ? 2.204623f : 1; }
}
