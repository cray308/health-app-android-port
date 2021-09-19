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
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

public class LiftingChart extends LinearLayout implements ChartContainer {
    public LineChart chartView;
    public LinearLayout legendContainer;
    private final HistoryChartLegendEntry[] legendEntries = {null, null, null, null};
    public HistoryViewModel.LiftChartViewModel viewModel;
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
        legendContainer = findViewById(R.id.legendContainer);
        legendEntries[0] = findViewById(R.id.firstEntry);
        legendEntries[1] = findViewById(R.id.secondEntry);
        legendEntries[2] = findViewById(R.id.thirdEntry);
        legendEntries[3] = findViewById(R.id.fourthEntry);
    }

    public void setup(HistoryViewModel.LiftChartViewModel viewModel,
                      IndexAxisValueFormatter xAxisFormatter) {
        this.viewModel = viewModel;

        for (int i = 0; i < 4; ++i) {
            dataSets[i] = ChartUtility.createDataSet(AppColors.chartColors[i]);
            dataSets[i].setLineWidth(2);
        }

        data = ChartUtility.createChartData(dataSets, 4);
        ChartUtility.setupChartView(chartView, xAxisFormatter);
    }

    public void update(int count, boolean isSmall) {
        for (int i = 0; i < 4; ++i) {
            legendEntries[i].label.setText(viewModel.legendLabels[i]);
            ChartUtility.updateDataSet(isSmall, dataSets[i], viewModel.entries[i]);
        }
        ChartUtility.updateChart(isSmall, count, this, viewModel.yMax);
    }

    public LineChart getChartView() { return chartView; }
    public LinearLayout getLegend() { return legendContainer; }
    public LineData getData() { return data; }
}
