package com.example.healthAppAndroid.historyTab;

import android.content.Context;
import android.content.res.Resources;

import com.example.healthAppAndroid.R;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

final class HistoryViewModel extends IndexAxisValueFormatter {
    private String[] workoutNames;
    private String[] liftNames;

    final static class TotalWorkoutsChartViewModel {
        private List<Entry> entries;
        ArrayList<List<Entry>> entryRefs;
        String legendLabel;
        final float[] avgs = {0, 0, 0};
        final float[] maxes = {0, 0, 0};
    }

    final static class WorkoutTypeChartViewModel extends IndexAxisValueFormatter {
        private ArrayList<List<Entry>> entries;
        ArrayList<ArrayList<List<Entry>>> entryRefs;
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
        private ArrayList<List<Entry>> entries;
        ArrayList<ArrayList<List<Entry>>> entryRefs;
        final String[] legendLabels = {null, null, null, null};
        private final float[][] avgs = {{0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}};
        final float[] maxes = {0, 0, 0};
    }

    final TotalWorkoutsChartViewModel totalWorkouts = new TotalWorkoutsChartViewModel();
    final WorkoutTypeChartViewModel workoutTypes = new WorkoutTypeChartViewModel();
    final LiftChartViewModel lifts = new LiftChartViewModel();
    private String[] axisStrings;
    final short[] nEntries = {0, 0, 0};
    private final short[] refIndices = {0, 0, 0};

    void setup(Resources res) {
        workoutNames = res.getStringArray(R.array.workoutTypes);
        liftNames = res.getStringArray(R.array.liftTypes);
    }

    void populateData(WeekDataModel results) {
        refIndices[0] = (short) (results.size - 26);
        refIndices[1] = (short) (results.size - 52);
        if (refIndices[1] < 0)
            refIndices[1] = 0;
        if (refIndices[0] < 0)
            refIndices[0] = 0;

        nEntries[0] = (short) (results.size - refIndices[0]);
        nEntries[1] = (short) (results.size - refIndices[1]);
        nEntries[2] = results.size;

        axisStrings = new String[results.size];
        totalWorkouts.entries = new ArrayList<>(results.size);
        lifts.entries = new ArrayList<>(4);
        workoutTypes.entries = new ArrayList<>(5);
        workoutTypes.entries.add(new ArrayList<>(results.size));
        for (int i = 0; i < 4; ++i) {
            workoutTypes.entries.add(new ArrayList<>(results.size));
            lifts.entries.add(new ArrayList<>(results.size));
        }

        short[] sectionIndices = {results.size, refIndices[0], refIndices[1], 0};
        int[] totalWorkoutsArr = {0, 0, 0}, maxWorkouts = {0, 0, 0}, innerLimits = {-1, 0, 1};
        int[] maxTime = {0, 0, 0}, maxWeight = {0, 0, 0};
        int[][] totalByType = {{0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}};
        int[][] totalByExercise = {{0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}};

        for (byte section = 3; section > 0; --section) {
            int jEnd = innerLimits[section - 1];
            for (short i = sectionIndices[section]; i < sectionIndices[section - 1]; ++i) {
                WeekDataModel.Week e = results.arr[i];
                axisStrings[i] = e.axisString;

                for (int j = 2; j > jEnd; --j) {
                    totalWorkoutsArr[j] += e.totalWorkouts;
                    if (e.totalWorkouts > maxWorkouts[j])
                        maxWorkouts[j] = e.totalWorkouts;
                }
                totalWorkouts.entries.add(new Entry(i, e.totalWorkouts));

                for (int x = 0; x < 4; ++x) {
                    for (int j = 2; j > jEnd; --j) {
                        totalByType[j][x] += e.durationByType[x];
                        totalByExercise[j][x] += e.weightArray[x];
                        if (e.weightArray[x] > maxWeight[j])
                            maxWeight[j] = e.weightArray[x];
                    }
                    lifts.entries.get(x).add(new Entry(i, e.weightArray[x]));
                }

                for (int j = 2; j > jEnd; --j) {
                    if (e.cumulativeDuration[3] > maxTime[j])
                        maxTime[j] = e.cumulativeDuration[3];
                }
                workoutTypes.entries.get(0).add(new Entry(i, 0));
                for (int x = 1; x < 5; ++x) {
                    workoutTypes.entries.get(x).add(new Entry(i, e.cumulativeDuration[x - 1]));
                }
            }
        }

        totalWorkouts.entryRefs = new ArrayList<>(3);
        lifts.entryRefs = new ArrayList<>(3);
        workoutTypes.entryRefs = new ArrayList<>(3);

        for (int i = 0; i < 3; ++i) {
            int refIdx = refIndices[i];
            int endIdx = refIdx + nEntries[i];
            totalWorkouts.avgs[i] = (float) totalWorkoutsArr[i] / nEntries[i];
            totalWorkouts.maxes[i] = maxWorkouts[i] < 7 ? 7f : 1.1f * maxWorkouts[i];
            workoutTypes.maxes[i] = 1.1f * maxTime[i];
            lifts.maxes[i] = 1.1f * maxWeight[i];
            totalWorkouts.entryRefs.add(totalWorkouts.entries.subList(refIdx, endIdx));
            lifts.entryRefs.add(new ArrayList<>(4));
            workoutTypes.entryRefs.add(new ArrayList<>(5));

            for (int j = 0; j < 4; ++j) {
                workoutTypes.avgs[i][j] = totalByType[i][j] / nEntries[i];
                lifts.avgs[i][j] = (float) totalByExercise[i][j] / nEntries[i];
                lifts.entryRefs.get(i).add(lifts.entries.get(j).subList(refIdx, endIdx));
                workoutTypes.entryRefs.get(i).add(
                  workoutTypes.entries.get(j).subList(refIdx, endIdx));
            }
            workoutTypes.entryRefs.get(i).add(workoutTypes.entries.get(4).subList(refIdx, endIdx));
        }
    }

    void formatDataForTimeRange(Context context, byte index) {
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
        Arrays.fill(nEntries, (short) 0);
        axisStrings = null;
        totalWorkouts.entryRefs = null;
        totalWorkouts.entries = null;
        workoutTypes.entryRefs = null;
        workoutTypes.entries = null;
        lifts.entryRefs = null;
        lifts.entries = null;
    }
}
