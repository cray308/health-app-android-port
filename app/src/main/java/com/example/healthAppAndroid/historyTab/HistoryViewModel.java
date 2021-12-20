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
    private final String[] axisStrings = new String[128];
    final int[] nEntries = {0, 0, 0};
    private final int[] refIndices = {0, 0, 0};

    public void setup(Resources res) {
        workoutNames = res.getStringArray(R.array.workoutTypes);
        liftNames = res.getStringArray(R.array.liftTypes);
    }

    void populateData(WeekDataModel results) {
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

        int[] sectionIndices = {results.size, refIndices[0], refIndices[1], 0};
        int[] totalWorkoutsArr = {0, 0, 0}, maxWorkouts = {0, 0, 0}, innerLimits = {-1, 0, 1};
        int[] maxTime = {0, 0, 0}, maxWeight = {0, 0, 0};
        int[][] totalByType = {{0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}};
        int[][] totalByExercise = {{0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}};

        for (int section = 3; section > 0; --section) {
            int limit = sectionIndices[section - 1];
            int jEnd = innerLimits[section - 1];
            for (int i = sectionIndices[section]; i < limit; ++i) {
                WeekDataModel.Week e = results.arr[i];
                axisStrings[i] = e.axisString;

                for (int j = 2; j > jEnd; --j) {
                    totalWorkoutsArr[j] += e.totalWorkouts;
                    if (e.totalWorkouts > maxWorkouts[j])
                        maxWorkouts[j] = e.totalWorkouts;
                }
                totalWorkouts.entries[i] = new Entry(i, e.totalWorkouts);

                for (int x = 0; x < 4; ++x) {
                    for (int j = 2; j > jEnd; --j) {
                        totalByType[j][x] += e.durationByType[x];
                        totalByExercise[j][x] += e.weightArray[x];
                        if (e.weightArray[x] > maxWeight[j])
                            maxWeight[j] = e.weightArray[x];
                    }
                    lifts.entries[x][i] = new Entry(i, e.weightArray[x]);
                }

                for (int j = 2; j > jEnd; --j) {
                    if (e.cumulativeDuration[3] > maxTime[j])
                        maxTime[j] = e.cumulativeDuration[3];
                }
                workoutTypes.entries[0][i] = new Entry(i, 0);
                for (int x = 1; x < 5; ++x) {
                    workoutTypes.entries[x][i] = new Entry(i, e.cumulativeDuration[x - 1]);
                }
            }
        }

        for (int i = results.size; i < 128; ++i) {
            axisStrings[i] = "";
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
        int val = (int) value;
        return (val >= 0) ? axisStrings[val] : "";
    }

    void clearData() {
        Arrays.fill(nEntries, 0);
        Arrays.fill(refIndices, 0);
        Arrays.fill(totalWorkouts.avgs, 0);
        Arrays.fill(totalWorkouts.maxes, 0);
        Arrays.fill(lifts.maxes, 0);
        Arrays.fill(workoutTypes.maxes, 0);
        Arrays.fill(axisStrings, null);

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
