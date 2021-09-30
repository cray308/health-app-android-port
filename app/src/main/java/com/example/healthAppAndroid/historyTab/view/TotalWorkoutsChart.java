package com.example.healthAppAndroid.historyTab.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.core.content.ContextCompat;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.shareddata.AppColors;
import com.example.healthAppAndroid.historyTab.data.HistoryViewModel;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

public class TotalWorkoutsChart extends ChartContainer {
    HistoryViewModel.TotalWorkoutsChartViewModel viewModel;

    public TotalWorkoutsChart(Context context) { super(context); }

    public TotalWorkoutsChart(Context context, AttributeSet attrs) { super(context, attrs); }

    void setup() {
        inflate(getContext(), R.layout.total_workouts_chart, this);
        init();
    }

    void setup(HistoryViewModel.TotalWorkoutsChartViewModel viewModel,
               IndexAxisValueFormatter xAxisFormatter) {
        this.viewModel = viewModel;

        Drawable fill = ContextCompat.getDrawable(getContext(), R.drawable.chart_gradient);
        dataSets[0] = createDataSet(AppColors.red);
        dataSets[0].setFillDrawable(fill);
        dataSets[0].setDrawFilled(true);
        dataSets[0].setFillAlpha(191);
        setupChartData(dataSets, 1);
        setupChartView(xAxisFormatter);
    }

    void update(boolean isSmall) {
        chartView.getAxisLeft().removeAllLimitLines();
        LimitLine limitLine = new LimitLine(viewModel.avgWorkouts);
        limitLine.enableDashedLine(10, 10, 0);
        limitLine.setLineWidth(2);
        limitLine.setLineColor(AppColors.chartColors[0]);
        chartView.getAxisLeft().addLimitLine(limitLine);
        updateData(0, isSmall, viewModel.entries, 0, viewModel.legendLabel);
        data.setValueFormatter(new DefaultValueFormatter(2));
        update(isSmall, viewModel.yMax);
    }
}
