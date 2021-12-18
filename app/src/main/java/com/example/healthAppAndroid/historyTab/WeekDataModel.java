package com.example.healthAppAndroid.historyTab;

import com.example.healthAppAndroid.core.PersistenceService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public final class WeekDataModel {
    final static class TimeData {
        final int year;
        final int month;
        final int day;

        private TimeData(LocalDateTime info) {
            year = info.getYear() % 100;
            month = info.getMonthValue() - 1;
            day = info.getDayOfMonth();
        }
    }

    public static class Week {
        final TimeData timeData;
        final int totalWorkouts;
        final int[] durationByType = {0, 0, 0, 0};
        final int[] cumulativeDuration = {0, 0, 0, 0};
        final int[] weightArray = {0, 0, 0, 0};

        public Week(PersistenceService.WeeklyData d, ZoneId zoneId) {
            int timeStrength = d.timeStrength;
            LocalDateTime localInfo = LocalDateTime.ofInstant(Instant.ofEpochSecond(d.start),
                                                              zoneId);

            timeData = new TimeData(localInfo);
            totalWorkouts = d.totalWorkouts;
            weightArray[0] = d.bestSquat;
            weightArray[1] = d.bestPullup;
            weightArray[2] = d.bestBench;
            weightArray[3] = d.bestDeadlift;
            durationByType[0] = timeStrength;
            durationByType[1] = d.timeHIC;
            durationByType[2] = d.timeSE;
            durationByType[3] = d.timeEndurance;
            cumulativeDuration[0] = timeStrength;

            for (int i = 1; i < 4; ++i) {
                cumulativeDuration[i] = cumulativeDuration[i - 1] + durationByType[i];
            }
        }
    }

    public final Week[] arr = new Week[128];
    public int size;
}
