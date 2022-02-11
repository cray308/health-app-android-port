package com.example.healthAppAndroid.historyTab;

import com.example.healthAppAndroid.core.PersistenceService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;

public final class WeekDataModel {
    public static class Week {
        final String axisString;
        final short totalWorkouts;
        final short[] durationByType = {0, 0, 0, 0};
        final short[] cumulativeDuration = {0, 0, 0, 0};
        final short[] weightArray = {0, 0, 0, 0};

        public Week(PersistenceService.WeeklyData d, ZoneId zoneId) {
            short timeStrength = (short) d.timeStrength;
            LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochSecond(d.start), zoneId);
            axisString = String.format(Locale.US, "%d/%d/%d", time.getMonthValue(),
                                       time.getDayOfMonth(), time.getYear() % 100);
            totalWorkouts = (short) d.totalWorkouts;
            weightArray[0] = (short) d.bestSquat;
            weightArray[1] = (short) d.bestPullup;
            weightArray[2] = (short) d.bestBench;
            weightArray[3] = (short) d.bestDeadlift;
            durationByType[0] = timeStrength;
            durationByType[1] = (short) d.timeHIC;
            durationByType[2] = (short) d.timeSE;
            durationByType[3] = (short) d.timeEndurance;
            cumulativeDuration[0] = timeStrength;

            for (int i = 1; i < 4; ++i) {
                cumulativeDuration[i] = (short) (cumulativeDuration[i - 1] + durationByType[i]);
            }
        }
    }

    public final Week[] arr = new Week[128];
    public short size;
}
