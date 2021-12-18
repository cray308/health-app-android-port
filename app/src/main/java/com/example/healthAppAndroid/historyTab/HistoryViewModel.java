package com.example.healthAppAndroid.historyTab;

import android.content.Context;
import android.content.res.Resources;

import com.example.healthAppAndroid.R;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.Arrays;
import java.util.Locale;

final class HistoryViewModel extends IndexAxisValueFormatter {
    private String[] workoutNames;
    private String[] liftNames;
    private String[] months;

    final static class TotalWorkoutsChartViewModel {
        private Entry[] entries;
        Entry[] dynamicEntries;
        String legendLabel;
        final float[] avgs = {0, 0, 0};
        final float[] maxes = {0, 0, 0};
    }

    final static class WorkoutTypeChartViewModel extends IndexAxisValueFormatter {
        private final Entry[][] entries = {null, null, null, null, null};
        final Entry[][] dynamicEntries = {null, null, null, null, null};
        final String[] legendLabels = {null, null, null, null};
        private final int[][] avgs = {{0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}};
        final float[] maxes = {0, 0, 0};

        public String getFormattedValue(float value) {
            int minutes = (int) value;
            if (minutes == 0) {
                return "";
            } else if (minutes < 60) {
                return String.format(Locale.US, "%dm", minutes);
            } else {
                return String.format(Locale.US, "%dh %dm", minutes / 60, minutes % 60);
            }
        }
    }

    final static class LiftChartViewModel {
        private final Entry[][] entries = {null, null, null, null};
        final Entry[][] dynamicEntries = {null, null, null, null};
        final String[] legendLabels = {null, null, null, null};
        private final float[][] avgs = {{0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}};
        final float[] maxes = {0, 0, 0};
    }

    final TotalWorkoutsChartViewModel totalWorkouts = new TotalWorkoutsChartViewModel();
    final WorkoutTypeChartViewModel workoutTypes = new WorkoutTypeChartViewModel();
    final LiftChartViewModel lifts = new LiftChartViewModel();
    private final WeekDataModel.TimeData[] timeData = new WeekDataModel.TimeData[128];
    final int[] nEntries = {0, 0, 0};
    private final int[] refIndices = {0, 0, 0};

    public void setup(Resources res) {
        workoutNames = res.getStringArray(R.array.workoutTypes);
        liftNames = res.getStringArray(R.array.liftTypes);
        months = res.getStringArray(R.array.months);
    }

    void populateData(com.example.healthAppAndroid.historyTab.WeekDataModel results) {
        refIndices[0] = results.size - 26;
        refIndices[1] = results.size - 52;
        if (refIndices[1] < 0)
            refIndices[1] = 0;
        if (refIndices[0] < 0)
            refIndices[0] = 0;

        nEntries[0] = results.size - refIndices[0];
        nEntries[1] = results.size - refIndices[1];
        nEntries[2] = results.size;

        totalWorkouts.entries = new Entry[results.size];
        for (int i = 0; i < 4; ++i) {
            workoutTypes.entries[i] = new Entry[results.size];
            lifts.entries[i] = new Entry[results.size];
        }
        workoutTypes.entries[4] = new Entry[results.size];

        int[] totalWorkoutsArr = {0, 0, 0}, maxWorkouts = {0, 0, 0};
        int[] maxTime = {0, 0, 0}, maxWeight = {0, 0, 0};
        int[][] totalByType = {{0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}};
        int[][] totalByExercise = {{0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}};

        for (int i = 0; i < refIndices[1]; ++i) {
            WeekDataModel.Week e = results.arr[i];
            timeData[i] = e.timeData;

            totalWorkoutsArr[2] += e.totalWorkouts;
            if (e.totalWorkouts > maxWorkouts[2])
                maxWorkouts[2] = e.totalWorkouts;
            totalWorkouts.entries[i] = new Entry(i, e.totalWorkouts);

            for (int j = 0; j < 4; ++j) {
                totalByType[2][j] += e.durationByType[j];

                totalByExercise[2][j] += e.weightArray[j];
                if (e.weightArray[j] > maxWeight[2])
                    maxWeight[2] = e.weightArray[j];
                lifts.entries[j][i] = new Entry(i, e.weightArray[j]);
            }

            if (e.cumulativeDuration[3] > maxTime[2])
                maxTime[2] = e.cumulativeDuration[3];
            workoutTypes.entries[0][i] = new Entry(i, 0);
            for (int j = 1; j < 5; ++j) {
                workoutTypes.entries[j][i] = new Entry(i, e.cumulativeDuration[j - 1]);
            }
        }

        for (int i = refIndices[1]; i < refIndices[0]; ++i) {
            WeekDataModel.Week e = results.arr[i];
            timeData[i] = e.timeData;

            totalWorkoutsArr[2] += e.totalWorkouts;
            totalWorkoutsArr[1] += e.totalWorkouts;
            if (e.totalWorkouts > maxWorkouts[2])
                maxWorkouts[2] = e.totalWorkouts;
            if (e.totalWorkouts > maxWorkouts[1])
                maxWorkouts[1] = e.totalWorkouts;
            totalWorkouts.entries[i] = new Entry(i, e.totalWorkouts);

            for (int j = 0; j < 4; ++j) {
                totalByType[2][j] += e.durationByType[j];
                totalByType[1][j] += e.durationByType[j];

                totalByExercise[2][j] += e.weightArray[j];
                totalByExercise[1][j] += e.weightArray[j];
                if (e.weightArray[j] > maxWeight[2])
                    maxWeight[2] = e.weightArray[j];
                if (e.weightArray[j] > maxWeight[1])
                    maxWeight[1] = e.weightArray[j];
                lifts.entries[j][i] = new Entry(i, e.weightArray[j]);
            }

            if (e.cumulativeDuration[3] > maxTime[2])
                maxTime[2] = e.cumulativeDuration[3];
            if (e.cumulativeDuration[3] > maxTime[1])
                maxTime[1] = e.cumulativeDuration[3];
            workoutTypes.entries[0][i] = new Entry(i, 0);
            for (int j = 1; j < 5; ++j) {
                workoutTypes.entries[j][i] = new Entry(i, e.cumulativeDuration[j - 1]);
            }
        }

        for (int i = refIndices[0]; i < results.size; ++i) {
            WeekDataModel.Week e = results.arr[i];
            timeData[i] = e.timeData;

            totalWorkoutsArr[2] += e.totalWorkouts;
            totalWorkoutsArr[1] += e.totalWorkouts;
            totalWorkoutsArr[0] += e.totalWorkouts;
            if (e.totalWorkouts > maxWorkouts[2])
                maxWorkouts[2] = e.totalWorkouts;
            if (e.totalWorkouts > maxWorkouts[1])
                maxWorkouts[1] = e.totalWorkouts;
            if (e.totalWorkouts > maxWorkouts[0])
                maxWorkouts[0] = e.totalWorkouts;
            totalWorkouts.entries[i] = new Entry(i, e.totalWorkouts);

            for (int j = 0; j < 4; ++j) {
                totalByType[2][j] += e.durationByType[j];
                totalByType[1][j] += e.durationByType[j];
                totalByType[0][j] += e.durationByType[j];

                totalByExercise[2][j] += e.weightArray[j];
                totalByExercise[1][j] += e.weightArray[j];
                totalByExercise[0][j] += e.weightArray[j];
                if (e.weightArray[j] > maxWeight[2])
                    maxWeight[2] = e.weightArray[j];
                if (e.weightArray[j] > maxWeight[1])
                    maxWeight[1] = e.weightArray[j];
                if (e.weightArray[j] > maxWeight[0])
                    maxWeight[0] = e.weightArray[j];
                lifts.entries[j][i] = new Entry(i, e.weightArray[j]);
            }

            if (e.cumulativeDuration[3] > maxTime[2])
                maxTime[2] = e.cumulativeDuration[3];
            if (e.cumulativeDuration[3] > maxTime[1])
                maxTime[1] = e.cumulativeDuration[3];
            if (e.cumulativeDuration[3] > maxTime[0])
                maxTime[0] = e.cumulativeDuration[3];
            workoutTypes.entries[0][i] = new Entry(i, 0);
            for (int j = 1; j < 5; ++j) {
                workoutTypes.entries[j][i] = new Entry(i, e.cumulativeDuration[j - 1]);
            }
        }

        for (int i = 0; i < 3; ++i) {
            totalWorkouts.avgs[i] = (float) totalWorkoutsArr[i] / nEntries[i];
            totalWorkouts.maxes[i] = maxWorkouts[i] < 7 ? 7f : 1.1f * maxWorkouts[i];
            workoutTypes.maxes[i] = 1.1f * maxTime[i];
            lifts.maxes[i] = 1.1f * maxWeight[i];

            for (int j = 0; j < 4; ++j) {
                workoutTypes.avgs[i][j] = totalByType[i][j] / nEntries[i];
                lifts.avgs[i][j] = (float) totalByExercise[i][j] / nEntries[i];
            }
        }
    }

    void formatDataForTimeRange(Context context, byte index) {
        int currEntries = nEntries[index], refIdx = refIndices[index];
        totalWorkouts.dynamicEntries = null;
        for (int i = 0; i < 4; ++i) {
            workoutTypes.dynamicEntries[i] = null;
            lifts.dynamicEntries[i] = null;
        }
        workoutTypes.dynamicEntries[4] = null;
        totalWorkouts.dynamicEntries = new Entry[currEntries];
        System.arraycopy(totalWorkouts.entries, refIdx,
                         totalWorkouts.dynamicEntries, 0, currEntries);
        for (int i = 0; i < 4; ++i) {
            workoutTypes.dynamicEntries[i] = new Entry[currEntries];
            System.arraycopy(workoutTypes.entries[i], refIdx,
                             workoutTypes.dynamicEntries[i], 0, currEntries);
            lifts.dynamicEntries[i] = new Entry[currEntries];
            System.arraycopy(lifts.entries[i], refIdx, lifts.dynamicEntries[i], 0, currEntries);
        }
        workoutTypes.dynamicEntries[4] = new Entry[currEntries];
        System.arraycopy(workoutTypes.entries[4], refIdx,
                         workoutTypes.dynamicEntries[4], 0, currEntries);

        totalWorkouts.legendLabel = context.getString(R.string.totalWorkoutsLegend,
                                                      totalWorkouts.avgs[index]);

        for (int i = 0; i < 4; ++i) {
            int typeAverage = workoutTypes.avgs[index][i];
            String buf;
            if (typeAverage > 59) {
                buf = String.format(Locale.US, "%d h %d m", typeAverage / 60, typeAverage % 60);
            } else {
                buf = String.format(Locale.US, "%d m", typeAverage);
            }
            workoutTypes.legendLabels[i] = context.getString(R.string.workoutTypeLegend,
                                                             workoutNames[i], buf);

            lifts.legendLabels[i] = context.getString(R.string.liftLegend,
                                                      liftNames[i], lifts.avgs[index][i]);
        }
    }

    public String getFormattedValue(float value) {
        WeekDataModel.TimeData data = timeData[(int) value];
        return String.format(Locale.US, "%s/%d/%d", months[data.month], data.day, data.year);
    }

    void clearData() {
        Arrays.fill(nEntries, 0);
        Arrays.fill(refIndices, 0);
        Arrays.fill(totalWorkouts.avgs, 0);
        Arrays.fill(totalWorkouts.maxes, 0);
        Arrays.fill(lifts.maxes, 0);
        Arrays.fill(workoutTypes.maxes, 0);
        Arrays.fill(timeData, null);

        for (int i = 0; i < 3; ++i) {
            Arrays.fill(workoutTypes.avgs[i], 0);
            Arrays.fill(lifts.avgs[i], 0);
        }

        totalWorkouts.entries = null;
        totalWorkouts.dynamicEntries = null;
        for (int i = 0; i < 4; ++i) {
            workoutTypes.entries[i] = null;
            workoutTypes.dynamicEntries[i] = null;
            lifts.entries[i] = null;
            lifts.dynamicEntries[i] = null;
        }
        workoutTypes.entries[4] = null;
        workoutTypes.dynamicEntries[4] = null;
    }
}
