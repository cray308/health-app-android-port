package com.example.healthappandroid.historytab.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.healthappandroid.R;
import com.example.healthappandroid.common.shareddata.AppColors;
import com.example.healthappandroid.historytab.data.HistoryViewModel;
import com.example.healthappandroid.historytab.helpers.ChartUtility;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

public class LiftingChart extends LinearLayout {
    public LineChart chartView;
    public HistoryViewModel.LiftChartViewModel viewModel;
    private final LegendEntry[] legendEntries = {null, null, null, null};
    private final LineDataSet[] dataSets = {null, null, null, null};
    private LineData data;

    public LiftingChart(Context context) {
        super(context);
        setup();
    }

    public LiftingChart(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    private void setup() {
        inflate(getContext(), R.layout.lift_chart, this);
        chartView = findViewById(R.id.liftChart);
    }

    public void setup(HistoryViewModel.LiftChartViewModel viewModel,
                      IndexAxisValueFormatter xAxisFormatter) {
        this.viewModel = viewModel;

        ChartUtility.setupLegendEntries(legendEntries, AppColors.chartColors, 4);
        for (int i = 0; i < 4; ++i) {
            dataSets[i] = ChartUtility.createDataSet(AppColors.chartColors[i]);
            dataSets[i].setLineWidth(2);
        }

        data = ChartUtility.createChartData(dataSets, 4);
        ChartUtility.setupChartView(chartView, xAxisFormatter, legendEntries);
    }

    public void update(int count, boolean isSmall) {
        for (int i = 0; i < 4; ++i) {
            legendEntries[i].label = viewModel.legendLabels[i];
            ChartUtility.updateDataSet(isSmall, dataSets[i], viewModel.entries[i]);
        }
        ChartUtility.updateChart(isSmall, count, chartView, data, viewModel.yMax);
    }
}
