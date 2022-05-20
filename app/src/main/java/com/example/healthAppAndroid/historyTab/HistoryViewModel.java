package com.example.healthAppAndroid.historyTab;

import android.content.Context;
import android.content.res.Resources;

import androidx.core.text.TextUtilsCompat;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.core.AppCoordinator;
import com.example.healthAppAndroid.core.WeekDataModel;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

final class HistoryViewModel extends IndexAxisValueFormatter {
    private String[] workoutNames;
    private String[] liftNames;

    final static class TotalWorkoutsModel {
        private List<Entry> entries;
        ArrayList<List<Entry>> entryRefs;
        String legendLabel;
        final float[] avgs = {0, 0, 0};
        final float[] maxes = {0, 0, 0};
    }

    final static class WorkoutTypeModel {
        private ArrayList<List<Entry>> entries;
        ArrayList<ArrayList<List<Entry>>> entryRefs;
        final String[] legendLabels = {null, null, null, null};
        private final int[][] avgs = {{0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}};
        final float[] maxes = {0, 0, 0};
    }

    final static class LiftModel {
        private ArrayList<List<Entry>> entries;
        ArrayList<ArrayList<List<Entry>>> entryRefs;
        final String[] legendLabels = {null, null, null, null};
        private final float[][] avgs = {{0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}};
        final float[] maxes = {0, 0, 0};
    }

    final TotalWorkoutsModel totalWorkouts = new TotalWorkoutsModel();
    final WorkoutTypeModel workoutTypes = new WorkoutTypeModel();
    final LiftModel lifts = new LiftModel();
    private String[] axisStrings;
    final int[] nEntries = {0, 0, 0};
    private final int[] refIndices = {0, 0, 0};
    static boolean ltr = true;

    void setup(Resources res) {
        ltr = TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == 0;
        workoutNames = new String[]{res.getString(R.string.workout0), res.getString(R.string.workout1),
                                    res.getString(R.string.workout2), res.getString(R.string.workout3)};
        String[] exNames = res.getStringArray(R.array.exNames);
        liftNames = new String[]{exNames[0], exNames[1], exNames[2], exNames[3]};
    }

    void populateData(WeekDataModel results) {
        int inc = ltr ? 1 : -1;
        int[] refs = {results.size, results.size - 26, results.size - 52, 0};
        refs[2] = Math.max(refs[2], 0);
        refs[1] = Math.max(refs[1], 0);

        int[] _num = {results.size - refs[1], results.size - refs[2], results.size};
        System.arraycopy(_num, 0, nEntries, 0, 3);
        if (ltr) System.arraycopy(refs, 1, refIndices, 0, 3);

        axisStrings = new String[results.size];
        totalWorkouts.entries = new ArrayList<>(results.size);
        lifts.entries = new ArrayList<>(4);
        workoutTypes.entries = new ArrayList<>(5);
        workoutTypes.entries.add(new ArrayList<>(results.size));
        for (int i = 0; i < 4; ++i) {
            workoutTypes.entries.add(new ArrayList<>(results.size));
            lifts.entries.add(new ArrayList<>(results.size));
        }

        float mf = AppCoordinator.shared.metric ? 0.453592f : 1;
        int[] totalWorkoutsArr = {0, 0, 0}, maxWorkouts = {0, 0, 0}, innerLimits = {-1, 0, 1};
        int[] maxTime = {0, 0, 0}, maxWeight = {0, 0, 0};
        int[][] totalByType = {{0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}};
        int[][] totalByExercise = {{0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}};

        for (int section = 3, index = ltr ? 0 : results.size - 1; section > 0; --section) {
            int jEnd = innerLimits[section - 1];
            for (int i = refs[section]; i < refs[section - 1]; ++i, index += inc) {
                WeekDataModel.Week e = results.arr[i];
                axisStrings[index] = e.axisString;

                for (int j = 2; j > jEnd; --j) {
                    totalWorkoutsArr[j] += e.totalWorkouts;
                    if (e.totalWorkouts > maxWorkouts[j]) maxWorkouts[j] = e.totalWorkouts;
                }
                totalWorkouts.entries.add(new Entry(index, e.totalWorkouts));

                for (int x = 0; x < 4; ++x) {
                    for (int j = 2; j > jEnd; --j) {
                        totalByType[j][x] += e.durationByType[x];
                        totalByExercise[j][x] += e.weightArray[x];
                        if (e.weightArray[x] > maxWeight[j]) maxWeight[j] = e.weightArray[x];
                    }
                    lifts.entries.get(x).add(new Entry(index, e.weightArray[x] * mf));
                }

                for (int j = 2; j > jEnd; --j) {
                    if (e.cumulativeDuration[3] > maxTime[j]) maxTime[j] = e.cumulativeDuration[3];
                }
                workoutTypes.entries.get(0).add(new Entry(index, 0));
                for (int x = 1; x < 5; ++x) {
                    workoutTypes.entries.get(x).add(new Entry(index, e.cumulativeDuration[x - 1]));
                }
            }
        }

        if (!ltr) {
            Comparator<Entry> c = Comparator.comparingInt(e -> (int)e.getX());
            totalWorkouts.entries.sort(c);
            workoutTypes.entries.get(0).sort(c);
            for (int i = 0; i < 4; ++i) {
                workoutTypes.entries.get(i + 1).sort(c);
                lifts.entries.get(i).sort(c);
            }
        }

        totalWorkouts.entryRefs = new ArrayList<>(3);
        lifts.entryRefs = new ArrayList<>(3);
        workoutTypes.entryRefs = new ArrayList<>(3);
        float[] invEntries = {1f / nEntries[0], 1f / nEntries[1], 1f / nEntries[2]};

        for (int i = 0; i < 3; ++i) {
            int refIdx = refIndices[i];
            int endIdx = refIdx + nEntries[i];
            totalWorkouts.avgs[i] = totalWorkoutsArr[i] * invEntries[i];
            totalWorkouts.maxes[i] = maxWorkouts[i] < 7 ? 7f : 1.1f * maxWorkouts[i];
            workoutTypes.maxes[i] = 1.1f * maxTime[i];
            lifts.maxes[i] = maxWeight[i] * mf * 1.1f;
            totalWorkouts.entryRefs.add(totalWorkouts.entries.subList(refIdx, endIdx));
            lifts.entryRefs.add(new ArrayList<>(4));
            workoutTypes.entryRefs.add(new ArrayList<>(5));

            for (int j = 0; j < 4; ++j) {
                workoutTypes.avgs[i][j] = totalByType[i][j] / nEntries[i];
                lifts.avgs[i][j] = totalByExercise[i][j] * mf * invEntries[i];
                lifts.entryRefs.get(i).add(lifts.entries.get(j).subList(refIdx, endIdx));
                List<Entry> sublist = workoutTypes.entries.get(j).subList(refIdx, endIdx);
                workoutTypes.entryRefs.get(i).add(sublist);
            }
            workoutTypes.entryRefs.get(i).add(workoutTypes.entries.get(4).subList(refIdx, endIdx));
        }
    }

    void formatDataForTimeRange(Context c, int index) {
        totalWorkouts.legendLabel = c.getString(R.string.legend0, totalWorkouts.avgs[index]);

        for (int i = 0; i < 4; ++i) {
            int typeAverage = workoutTypes.avgs[index][i];
            String buf;
            if (typeAverage < 60) {
                buf = c.getString(R.string.minFmt, typeAverage);
            } else {
                buf = c.getString(R.string.hourMinFmt, typeAverage / 60, typeAverage % 60);
            }
            workoutTypes.legendLabels[i] = c.getString(R.string.legend1, workoutNames[i], buf);
            lifts.legendLabels[i] = c.getString(R.string.legend2, liftNames[i], lifts.avgs[index][i]);
        }
    }

    public String getFormattedValue(float value) {
        int val = (int)value;
        return (val >= 0) ? axisStrings[val] : "";
    }

    void clearData() {
        Arrays.fill(nEntries, 0);
        axisStrings = null;
        totalWorkouts.entryRefs = null;
        totalWorkouts.entries = null;
        workoutTypes.entryRefs = null;
        workoutTypes.entries = null;
        lifts.entryRefs = null;
        lifts.entries = null;
    }
}
