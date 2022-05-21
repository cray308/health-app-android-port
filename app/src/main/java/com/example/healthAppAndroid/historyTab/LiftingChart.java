package com.example.healthAppAndroid.historyTab;

import android.content.Context;
import android.util.AttributeSet;

import com.example.healthAppAndroid.R;

public final class LiftingChart extends ChartContainer implements HistoryFragment.HistoryChart {
    private HistoryModel.LiftModel model;

    public LiftingChart(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.lift_chart, new int[]{
          R.id.secondEntry, R.id.thirdEntry, R.id.fourthEntry
        });
    }

    public void setup(HistoryModel historyModel, int[] chartColors, int labelColor,
                      String defaultText, boolean ltr) {
        model = historyModel.lifts;
        for (int i = 0; i < 4; ++i) {
            sets[i] = createDataSet(chartColors[i], labelColor, ltr);
            sets[i].setLineWidth(2);
        }
        setupChartData(sets);
        setupChartView(historyModel, labelColor, defaultText, ltr);
    }

    public void updateChart(boolean isSmall, int index) {
        for (int i = 0; i < 4; ++i) {
            updateData(i, isSmall, model.refs.get(index).get(i), i, model.legendLabels[i]);
        }
        update(isSmall, model.maxes[index]);
    }
}
