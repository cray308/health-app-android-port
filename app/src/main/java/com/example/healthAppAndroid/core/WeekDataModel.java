package com.example.healthAppAndroid.core;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class WeekDataModel {
    public static final class Week {
        public final String axisString;
        public final short totalWorkouts;
        public final short[] durationByType = {0, 0, 0, 0};
        public final short[] cumulativeDuration = {0, 0, 0, 0};
        public final short[] weightArray = {0, 0, 0, 0};

        Week(PersistenceService.WeeklyData d, ZoneId zoneId, DateTimeFormatter formatter) {
            short timeStrength = d.timeStrength;
            LocalDateTime tm = LocalDateTime.ofInstant(Instant.ofEpochSecond(d.start), zoneId);
            axisString = tm.format(formatter);
            totalWorkouts = d.totalWorkouts;
            weightArray[0] = d.bestSquat;
            weightArray[1] = d.bestPullup;
            weightArray[2] = d.bestBench;
            weightArray[3] = d.bestDeadlift;
            durationByType[0] = timeStrength;
            durationByType[1] = d.timeSE;
            durationByType[2] = d.timeEndurance;
            durationByType[3] = d.timeHIC;
            cumulativeDuration[0] = timeStrength;

            for (int i = 1; i < 4; ++i) {
                cumulativeDuration[i] = (short)(cumulativeDuration[i - 1] + durationByType[i]);
            }
        }
    }

    public final Week[] arr;
    public final int size;

    WeekDataModel(int count) {
        size = (count > 1) ? count - 1 : 0;
        if (size > 0) arr = new Week[size];
        else arr = null;
    }
}
