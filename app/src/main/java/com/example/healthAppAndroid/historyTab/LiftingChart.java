package com.example.healthAppAndroid.historyTab;

import android.content.Context;
import android.util.AttributeSet;

import com.example.healthAppAndroid.R;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

public final class LiftingChart extends ChartContainer {
    private HistoryViewModel.LiftModel model;

    public LiftingChart(Context c, AttributeSet attrs) {
        super(c, attrs, R.layout.lift_chart, new int[]{
          R.id.secondEntry, R.id.thirdEntry, R.id.fourthEntry
        });
    }

    void setup(HistoryViewModel.LiftModel m, IndexAxisValueFormatter xAxisFormatter) {
        model = m;

        int[] colors = getChartColors(getContext());
        for (int i = 0; i < 4; ++i) {
            dataSets[i] = createDataSet(colors[i]);
            dataSets[i].setLineWidth(2);
        }
        setupChartData(dataSets, 4);
        setupChartView(xAxisFormatter);
    }

    void updateChart(boolean isSmall, int index) {
        for (int i = 0; i < 4; ++i) {
            updateData(i, isSmall, model.entryRefs.get(index).get(i), i, model.legendLabels[i]);
        }
        update(isSmall, model.maxes[index]);
    }
}
