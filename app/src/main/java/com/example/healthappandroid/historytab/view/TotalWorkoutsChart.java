package com.example.healthappandroid.historytab.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.healthappandroid.R;
import com.example.healthappandroid.common.shareddata.AppColors;
import com.example.healthappandroid.historytab.data.HistoryViewModel;
import com.example.healthappandroid.historytab.helpers.ChartUtility;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

public class TotalWorkoutsChart extends LinearLayout {
    public LineChart chartView;
    public HistoryViewModel.TotalWorkoutsChartViewModel viewModel;
    private LineData data;
    private LineDataSet dataSet;
    private final LegendEntry[] legendEntries = {null};

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
    }

    public void setup(HistoryViewModel.TotalWorkoutsChartViewModel viewModel,
                      IndexAxisValueFormatter xAxisFormatter) {
        this.viewModel = viewModel;

        ChartUtility.setupLegendEntries(legendEntries, new int[]{AppColors.teal}, 1);
        Drawable fill = ContextCompat.getDrawable(getContext(), R.drawable.chart_gradient);
        dataSet = ChartUtility.createDataSet(AppColors.red);
        dataSet.setFillDrawable(fill);
        dataSet.setDrawFilled(true);
        dataSet.setFillAlpha(191);
        data = ChartUtility.createChartData(new LineDataSet[]{dataSet}, 1);
        ChartUtility.setupChartView(chartView, xAxisFormatter, legendEntries);
    }

    public void update(int count, boolean isSmall) {
        chartView.getAxisLeft().removeAllLimitLines();
        legendEntries[0].label = viewModel.legendLabel;
        LimitLine limitLine = new LimitLine(viewModel.avgWorkouts);
        limitLine.enableDashedLine(5, 5, 0);
        limitLine.setLineColor(legendEntries[0].formColor);
        chartView.getAxisLeft().addLimitLine(limitLine);
        ChartUtility.updateDataSet(isSmall, dataSet, viewModel.entries);
        data.setValueFormatter(new DefaultValueFormatter(2));
        ChartUtility.updateChart(isSmall, count, chartView, data, viewModel.yMax);
    }
}
