package com.example.healthAppAndroid.historyTab.data;

import android.content.Context;
import android.content.res.Resources;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.helpers.DateHelper;
import com.example.healthAppAndroid.common.helpers.ViewHelper;
import com.example.healthAppAndroid.common.shareddata.WeeklyData;
import com.github.mikephil.charting.data.Entry;

import java.time.LocalDateTime;
import java.util.Arrays;

public class HistoryViewModel {
    private String[] wordMonths;
    private String[] numMonths;
    private String[] workoutNames;
    private String[] liftNames;
    public static final byte FormatShort = 0;
    public static final byte FormatLong = 1;

    public static class TotalWorkoutsChartViewModel {
        public Entry[] entries;
        public String legendLabel = "";
        public float avgWorkouts;
        public float yMax;
    }

    public static class WorkoutTypeChartViewModel {
        public final Entry[][] entries = {null, null, null, null, null};
        public final String[] legendLabels = {"", "", "", ""};
        public final int[] totalByType = {0, 0, 0, 0};
        public float yMax;

        public String getDuration(int minutes) {
            if (minutes == 0) {
                return "";
            } else if (minutes < 60) {
                return ViewHelper.format("%dm", minutes);
            } else {
                return ViewHelper.format("%dh %dm", minutes / 60, minutes % 60);
            }
        }
    }

    public static class LiftChartViewModel {
        public final Entry[][] entries = {null, null, null, null};
        public final String[] legendLabels = {"", "", "", ""};
        public final int[] totalByExercise = {0, 0, 0, 0};
        public float yMax;
    }

    public static class WeekDataModel {
        public static class WeekModel {
            public final int year;
            public final int month;
            public final int day;
            public final int totalWorkouts;
            public final int[] durationByType = {0, 0, 0, 0};
            public final int[] cumulativeDuration = {0, 0, 0, 0};
            public final int[] weightArray = {0, 0, 0, 0};

            public WeekModel(WeeklyData d) {
                int timeStrength = d.timeStrength;
                LocalDateTime localInfo = DateHelper.localTime(d.start);

                year = localInfo.getYear() % 100;
                month = localInfo.getMonthValue() - 1;
                day = localInfo.getDayOfMonth();
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

                for (int i = 1; i < 4; ++i)
                    cumulativeDuration[i] = cumulativeDuration[i - 1] + durationByType[i];
            }
        }

        public int size;
        public final WeekModel[] arr = new WeekModel[128];
    }

    public byte formatType;
    public final TotalWorkoutsChartViewModel totalWorkoutsViewModel = new TotalWorkoutsChartViewModel();
    public final WorkoutTypeChartViewModel workoutTypeViewModel = new WorkoutTypeChartViewModel();
    public final LiftChartViewModel liftViewModel = new LiftChartViewModel();
    public final WeekDataModel data = new WeekDataModel();

    public void setup(Context context) {
        Resources res = context.getResources();
        wordMonths = res.getStringArray(R.array.wordMonths);
        numMonths = res.getStringArray(R.array.numMonths);
        workoutNames = res.getStringArray(R.array.workoutTypes);
        liftNames = res.getStringArray(R.array.liftTypes);
    }

    public void formatDataForTimeRange(Context context, int index) {
        formatType = FormatShort;
        totalWorkoutsViewModel.avgWorkouts = 0;
        totalWorkoutsViewModel.yMax = 0;
        workoutTypeViewModel.yMax = 0;
        liftViewModel.yMax = 0;
        Arrays.fill(liftViewModel.totalByExercise, 0);
        Arrays.fill(workoutTypeViewModel.totalByType, 0);
        totalWorkoutsViewModel.entries = null;
        for (int i = 0; i < 4; ++i) {
            workoutTypeViewModel.entries[i] = null;
            liftViewModel.entries[i] = null;
        }
        workoutTypeViewModel.entries[4] = null;

        if (data.size == 0) return;

        int startIndex = 0;
        if (index == 0) {
            startIndex = data.size - 26;
        } else if (index == 1) {
            startIndex = data.size - 52;
        }

        if (startIndex < 0)
            startIndex = 0;
        if (data.size - startIndex >= 7)
            formatType = FormatLong;

        int nEntries = data.size - startIndex;

        totalWorkoutsViewModel.entries = new Entry[nEntries];
        for (int i = 0; i < 4; ++i) {
            workoutTypeViewModel.entries[i] = new Entry[nEntries];
            liftViewModel.entries[i] = new Entry[nEntries];
        }
        workoutTypeViewModel.entries[4] = new Entry[nEntries];

        int totalWorkouts = 0, maxWorkouts = 0, maxActivityTime = 0, maxWeight = 0;
        for (int i = startIndex, entryIdx = 0; i < data.size; ++i, ++entryIdx) {
            WeekDataModel.WeekModel e = data.arr[i];
            int workouts = e.totalWorkouts;
            totalWorkouts += workouts;
            if (workouts > maxWorkouts)
                maxWorkouts = workouts;
            totalWorkoutsViewModel.entries[entryIdx] = new Entry(i, workouts);

            for (int j = 0; j < 4; ++j) {
                workoutTypeViewModel.totalByType[j] += e.durationByType[j];

                int weight = e.weightArray[j];
                liftViewModel.totalByExercise[j] += weight;
                if (weight > maxWeight)
                    maxWeight = weight;
                liftViewModel.entries[j][entryIdx] = new Entry(i, weight);
            }

            if (e.cumulativeDuration[3] > maxActivityTime)
                maxActivityTime = e.cumulativeDuration[3];
            workoutTypeViewModel.entries[0][entryIdx] = new Entry(i, 0);
            for (int j = 1; j < 5; ++j)
                workoutTypeViewModel.entries[j][entryIdx] = new Entry(i, e.cumulativeDuration[j - 1]);
        }

        totalWorkoutsViewModel.avgWorkouts = (float) totalWorkouts / nEntries;
        totalWorkoutsViewModel.yMax = maxWorkouts < 7 ? 7 : 1.1f * maxWorkouts;
        workoutTypeViewModel.yMax = 1.1f * maxActivityTime;
        liftViewModel.yMax = 1.1f * maxWeight;

        totalWorkoutsViewModel.legendLabel = context.getString(R.string.totalWorkoutsLegend,
                                                               totalWorkoutsViewModel.avgWorkouts);

        for (int i = 0; i < 4; ++i) {
            float liftAverage = (float) liftViewModel.totalByExercise[i] / nEntries;
            int typeAverage = workoutTypeViewModel.totalByType[i] / nEntries;
            String buf;
            if (typeAverage > 59) {
                buf = ViewHelper.format("%d h %d m", typeAverage / 60, typeAverage % 60);
            } else {
                buf = ViewHelper.format("%d m", typeAverage);
            }
            workoutTypeViewModel.legendLabels[i] = context.getString(R.string.workoutTypeLegend,
                                                                     workoutNames[i], buf);

            liftViewModel.legendLabels[i] = context.getString(R.string.liftLegend,
                                                              liftNames[i], liftAverage);
        }
    }

    public String getXAxisLabel(int index) {
        WeekDataModel.WeekModel model = data.arr[index];
        if (formatType == FormatShort) {
            return ViewHelper.format("%s %d", wordMonths[model.month], model.day);
        } else {
            return ViewHelper.format("%s/%d/%d", numMonths[model.month], model.day, model.year);
        }
    }
}
