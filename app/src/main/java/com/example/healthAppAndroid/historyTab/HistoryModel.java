package com.example.healthAppAndroid.historyTab;

import android.content.Context;

import androidx.core.text.TextUtilsCompat;
import androidx.core.view.ViewCompat;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.core.Macros;
import com.example.healthAppAndroid.core.PersistenceManager;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

final class HistoryModel extends IndexAxisValueFormatter {
    static final class TotalWorkoutsModel {
        private List<Entry> entries;
        ArrayList<List<Entry>> refs;
        String legendLabel;
        final float[] avgs = {0, 0, 0};
        final float[] maxes = {0, 0, 0};
    }

    static final class WorkoutTypeModel {
        private ArrayList<List<Entry>> entries;
        ArrayList<ArrayList<List<Entry>>> refs;
        final String[] legendLabels = {null, null, null, null};
        private final int[][] avgs = {{0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}};
        final float[] maxes = {0, 0, 0};
    }

    static final class LiftModel {
        private ArrayList<List<Entry>> entries;
        ArrayList<ArrayList<List<Entry>>> refs;
        final String[] legendLabels = {null, null, null, null};
        private final float[][] avgs = {{0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}};
        final float[] maxes = {0, 0, 0};
    }

    static boolean isLtr(Locale locale) {
        return TextUtilsCompat.getLayoutDirectionFromLocale(locale)
               == ViewCompat.LAYOUT_DIRECTION_LTR;
    }

    final TotalWorkoutsModel totals = new TotalWorkoutsModel();
    final WorkoutTypeModel types = new WorkoutTypeModel();
    final LiftModel lifts = new LiftModel();
    private String[] axisStrings;
    final int[] nEntries = {0, 0, 0};
    private final int[] refIndices = {0, 0, 0};

    void populate(PersistenceManager.WeeklyData[] weeks, String[] strings) {
        int size = weeks.length;
        int[] refs = {size, size - 26, size - 52, 0};
        refs[2] = Math.max(refs[2], 0);
        refs[1] = Math.max(refs[1], 0);
        System.arraycopy(new int[]{size - refs[1], size - refs[2], size}, 0, nEntries, 0, 3);

        Locale locale = Locale.getDefault();
        boolean ltr = isLtr(locale);
        if (ltr) {
            System.arraycopy(refs, 1, refIndices, 0, 3);
        } else {
            for (int i = 0, j = size - 1; i < j; ++i, --j) {
                String temp = strings[i];
                strings[i] = strings[j];
                strings[j] = temp;
            }
        }

        axisStrings = strings;
        totals.entries = new ArrayList<>(size);
        lifts.entries = new ArrayList<>(4);
        types.entries = new ArrayList<>(5);
        types.entries.add(new ArrayList<>(size));
        for (int i = 0; i < 4; ++i) {
            types.entries.add(new ArrayList<>(size));
            lifts.entries.add(new ArrayList<>(size));
        }

        int increment = ltr ? 1 : -1;
        float weightFactor = Macros.isMetric(locale) ? Macros.toKg : 1;
        int[] totalWorkouts = {0, 0, 0}, maxWorkouts = {0, 0, 0}, innerLimits = {-1, 0, 1};
        int[] maxTime = {0, 0, 0}, maxWeight = {0, 0, 0};
        int[][] totalByType = {{0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}};
        int[][] totalByExercise = {{0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}};

        for (int section = 3, entryIdx = ltr ? 0 : size - 1; section > 0; --section) {
            int jEnd = innerLimits[section - 1], iEnd = refs[section - 1];
            for (int i = refs[section]; i < iEnd; ++i, entryIdx += increment) {
                PersistenceManager.WeeklyData week = weeks[i];
                for (int j = 2; j > jEnd; --j) {
                    totalWorkouts[j] += week.totalWorkouts;
                    maxWorkouts[j] = Math.max(maxWorkouts[j], week.totalWorkouts);
                    maxTime[j] = Math.max(maxTime[j], week.cumulativeDuration[3]);
                }
                totals.entries.add(new Entry(entryIdx, week.totalWorkouts));

                for (int x = 0; x < 4; ++x) {
                    for (int j = 2; j > jEnd; --j) {
                        totalByType[j][x] += week.durationByType[x];
                        totalByExercise[j][x] += week.weights[x];
                        maxWeight[j] = Math.max(maxWeight[j], week.weights[x]);
                    }
                    lifts.entries.get(x).add(new Entry(entryIdx, week.weights[x] * weightFactor));
                }

                types.entries.get(0).add(new Entry(entryIdx, 0));
                for (int x = 1; x < 5; ++x) {
                    types.entries.get(x).add(new Entry(entryIdx, week.cumulativeDuration[x - 1]));
                }
            }
        }

        if (!ltr) {
            Comparator<Entry> comparator = Comparator.comparingInt(e -> (int)e.getX());
            totals.entries.sort(comparator);
            types.entries.get(0).sort(comparator);
            for (int i = 0; i < 4; ++i) {
                types.entries.get(i + 1).sort(comparator);
                lifts.entries.get(i).sort(comparator);
            }
        }

        totals.refs = new ArrayList<>(3);
        lifts.refs = new ArrayList<>(3);
        types.refs = new ArrayList<>(3);
        float[] inverseEntries = {1f / nEntries[0], 1f / nEntries[1], 1f / nEntries[2]};

        for (int i = 0; i < 3; ++i) {
            int startIdx = refIndices[i];
            int endIdx = startIdx + nEntries[i];
            totals.avgs[i] = totalWorkouts[i] * inverseEntries[i];
            totals.maxes[i] = maxWorkouts[i] < 7 ? 7 : 1.1f * maxWorkouts[i];
            types.maxes[i] = maxTime[i] * 1.1f;
            lifts.maxes[i] = maxWeight[i] * weightFactor * 1.1f;
            totals.refs.add(totals.entries.subList(startIdx, endIdx));
            types.refs.add(new ArrayList<>(5));
            lifts.refs.add(new ArrayList<>(4));

            for (int j = 0; j < 4; ++j) {
                types.avgs[i][j] = totalByType[i][j] / nEntries[i];
                lifts.avgs[i][j] = totalByExercise[i][j] * weightFactor * inverseEntries[i];
                lifts.refs.get(i).add(lifts.entries.get(j).subList(startIdx, endIdx));
                types.refs.get(i).add(types.entries.get(j).subList(startIdx, endIdx));
            }
            types.refs.get(i).add(types.entries.get(4).subList(startIdx, endIdx));
        }
    }

    void formatDataForTimeRange(Context context, int index) {
        totals.legendLabel = context.getString(R.string.legend0, totals.avgs[index]);

        int[] workoutIds = {
          R.string.workout0, R.string.workout1, R.string.workout2, R.string.workout3
        };
        String[] exNames = context.getResources().getStringArray(R.array.exNames);
        String[] liftNames = {exNames[0], exNames[1], exNames[2], exNames[3]};

        for (int i = 0; i < 4; ++i) {
            String duration;
            int typeAverage = types.avgs[index][i];
            if (typeAverage < 60) {
                duration = context.getString(R.string.minFmt, typeAverage);
            } else {
                duration = context.getString(R.string.hourMinFmt, typeAverage / 60, typeAverage % 60);
            }
            types.legendLabels[i] = context.getString(R.string.legend1,
                                                      context.getString(workoutIds[i]), duration);
            lifts.legendLabels[i] = context.getString(R.string.legend2,
                                                      liftNames[i], lifts.avgs[index][i]);
        }
    }

    public String getFormattedValue(float value) {
        int val = (int)value;
        return val >= 0 ? axisStrings[val] : "";
    }

    void clear() {
        Arrays.fill(nEntries, 0);
        axisStrings = null;
        totals.refs = null;
        totals.entries = null;
        types.refs = null;
        types.entries = null;
        lifts.refs = null;
        lifts.entries = null;
    }
}
