package com.example.healthAppAndroid.historyTab;

import android.content.Context;
import android.util.AttributeSet;

import com.example.healthAppAndroid.R;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

public final class LiftingChart extends ChartContainer {
    private HistoryViewModel.LiftChartViewModel viewModel;

    public LiftingChart(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.lift_chart, new int[]{
          R.id.secondEntry, R.id.thirdEntry, R.id.fourthEntry
        });
    }

    void setup(HistoryViewModel.LiftChartViewModel model, IndexAxisValueFormatter xAxisFormatter) {
        viewModel = model;

        int[] colors = getChartColors(getContext());
        for (int i = 0; i < 4; ++i) {
            dataSets[i] = createDataSet(colors[i]);
            dataSets[i].setLineWidth(2);
        }
        setupChartData(dataSets, 4);
        setupChartView(xAxisFormatter);
    }

    void updateChart(boolean isSmall, byte index) {
        for (int i = 0; i < 4; ++i) {
            updateData(i, isSmall, viewModel.entryRefs.get(index).get(i), i, viewModel.legendLabels[i]);
        }
        update(isSmall, viewModel.maxes[index]);
    }
}
