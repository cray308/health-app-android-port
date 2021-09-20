package com.example.healthAppAndroid.historyTab.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.shareddata.AppColors;
import com.example.healthAppAndroid.historyTab.data.HistoryViewModel;
import com.example.healthAppAndroid.historyTab.helpers.ChartUtility;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

public class TotalWorkoutsChart extends LinearLayout implements ChartContainer {
    public LineChart chartView;
    public LinearLayout legendContainer;
    private HistoryChartLegendEntry legendEntry;
    public HistoryViewModel.TotalWorkoutsChartViewModel viewModel;
    private LineData data;
    private LineDataSet dataSet;

    public TotalWorkoutsChart(Context context) {
        super(context);
        setup();
    }

    public TotalWorkoutsChart(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    private void setup() {
        inflate(getContext(), R.layout.total_workouts_chart, this);
        chartView = findViewById(R.id.totalWorkoutsChart);
        legendContainer = findViewById(R.id.legendContainer);
        legendEntry = findViewById(R.id.legendEntry);
    }

    public void setup(HistoryViewModel.TotalWorkoutsChartViewModel viewModel,
                      IndexAxisValueFormatter xAxisFormatter) {
        this.viewModel = viewModel;

        Drawable fill = ContextCompat.getDrawable(getContext(), R.drawable.chart_gradient);
        dataSet = ChartUtility.createDataSet(AppColors.red);
        dataSet.setFillDrawable(fill);
        dataSet.setDrawFilled(true);
        dataSet.setFillAlpha(191);
        data = ChartUtility.createChartData(new LineDataSet[]{dataSet}, 1);
        ChartUtility.setupChartView(chartView, xAxisFormatter);
    }

    public void update(int count, boolean isSmall) {
        chartView.getAxisLeft().removeAllLimitLines();
        legendEntry.label.setText(viewModel.legendLabel);
        LimitLine limitLine = new LimitLine(viewModel.avgWorkouts);
        limitLine.enableDashedLine(5, 5, 0);
        limitLine.setLineWidth(2);
        limitLine.setLineColor(AppColors.blue);
        chartView.getAxisLeft().addLimitLine(limitLine);
        ChartUtility.updateDataSet(isSmall, dataSet, viewModel.entries);
        data.setValueFormatter(new DefaultValueFormatter(2));
        ChartUtility.updateChart(isSmall, count, this, viewModel.yMax);
    }

    public LineChart getChartView() { return chartView; }
    public LinearLayout getLegend() { return legendContainer; }
    public LineData getData() { return data; }
}
