package com.example.healthAppAndroid.historyTab.view;

import android.content.Context;
import android.util.AttributeSet;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.shareddata.AppColors;
import com.example.healthAppAndroid.historyTab.data.HistoryViewModel;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

public class LiftingChart extends ChartContainer {
    HistoryViewModel.LiftChartViewModel viewModel;

    public LiftingChart(Context context) { super(context); }

    public LiftingChart(Context context, AttributeSet attrs) { super(context, attrs); }

    void setup() {
        inflate(getContext(), R.layout.lift_chart, this);
        init();
        legendEntries[1] = findViewById(R.id.secondEntry);
        legendEntries[2] = findViewById(R.id.thirdEntry);
        legendEntries[3] = findViewById(R.id.fourthEntry);
    }

    void setup(HistoryViewModel.LiftChartViewModel viewModel,
               IndexAxisValueFormatter xAxisFormatter) {
        this.viewModel = viewModel;

        for (int i = 0; i < 4; ++i) {
            dataSets[i] = createDataSet(AppColors.chartColors[i]);
            dataSets[i].setLineWidth(2);
        }
        setupChartData(dataSets, 4);
        setupChartView(xAxisFormatter);
    }

    void update(boolean isSmall) {
        for (int i = 0; i < 4; ++i)
            updateData(i, isSmall, viewModel.entries[i], i, viewModel.legendLabels[i]);
        update(isSmall, viewModel.yMax);
    }
}
