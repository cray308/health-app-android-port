package com.example.healthAppAndroid.historyTab.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.shareddata.AppColors;
import com.example.healthAppAndroid.historyTab.data.HistoryViewModel;
import com.example.healthAppAndroid.historyTab.helpers.ChartUtility;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.Arrays;

public class WorkoutTypeChart extends LinearLayout implements ChartContainer {
    public static class Formatter extends IndexAxisValueFormatter {
        private final HistoryViewModel.WorkoutTypeChartViewModel viewModel;

        public Formatter(HistoryViewModel.WorkoutTypeChartViewModel viewModel) {
            this.viewModel = viewModel;
        }

        public String getFormattedValue(float value) {
            return viewModel.getDuration((int) value);
        }
    }

    public LineChart chartView;
    public LinearLayout legendContainer;
    private final HistoryChartLegendEntry[] legendEntries = {null, null, null, null};
    public HistoryViewModel.WorkoutTypeChartViewModel viewModel;
    private final LineDataSet[] dataSets = {null, null, null, null, null};
    private LineData data;

    public WorkoutTypeChart(Context context) {
        super(context);
        setup();
    }

    public WorkoutTypeChart(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    private void setup() {
        inflate(getContext(), R.layout.workout_type_chart, this);
        chartView = findViewById(R.id.workoutTypeChart);
        legendContainer = findViewById(R.id.legendContainer);
        legendEntries[0] = findViewById(R.id.firstEntry);
        legendEntries[1] = findViewById(R.id.secondEntry);
        legendEntries[2] = findViewById(R.id.thirdEntry);
        legendEntries[3] = findViewById(R.id.fourthEntry);
    }

    public void setup(HistoryViewModel.WorkoutTypeChartViewModel viewModel,
                        IndexAxisValueFormatter xAxisFormatter) {
        this.viewModel = viewModel;
        Formatter formatter = new Formatter(viewModel);

        dataSets[0] = ChartUtility.createEmptyDataSet();
        for (int i = 1; i < 5; ++i) {
            dataSets[i] = ChartUtility.createDataSet(AppColors.chartColors[i - 1]);
            dataSets[i].setFillColor(AppColors.chartColors[i - 1]);
            dataSets[i].setDrawFilled(true);
            dataSets[i].setFillAlpha(191);
            AreaChartRenderer.Formatter fillFormatter = new AreaChartRenderer.Formatter(
                    dataSets[i - 1]);
            dataSets[i].setFillFormatter(fillFormatter);
        }

        data = ChartUtility.createChartData(new LineDataSet[]{
                dataSets[4], dataSets[3], dataSets[2], dataSets[1]
        }, 4);
        data.setValueFormatter(formatter);

        ChartUtility.setupChartView(chartView, xAxisFormatter);
        chartView.getAxisLeft().setValueFormatter(formatter);

        AreaChartRenderer renderer = new AreaChartRenderer(chartView);
        chartView.setRenderer(renderer);
    }

    public void update(int count, boolean isSmall) {
        dataSets[0].setValues(Arrays.asList(viewModel.entries[0]));
        for (int i = 1; i < 5; ++i) {
            legendEntries[i - 1].label.setText(viewModel.legendLabels[i - 1]);
            ChartUtility.updateDataSet(isSmall, dataSets[i], viewModel.entries[i]);
        }
        ChartUtility.updateChart(isSmall, count, this, viewModel.yMax);
    }

    public LineChart getChartView() { return chartView; }
    public LinearLayout getLegend() { return legendContainer; }
    public LineData getData() { return data; }
}
