package com.example.healthAppAndroid.historyTab;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.core.content.ContextCompat;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.core.AppColors;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

public final class TotalWorkoutsChart extends ChartContainer {
    private HistoryViewModel.TotalWorkoutsChartViewModel viewModel;
    private int lineColor;

    public TotalWorkoutsChart(Context context) { super(context, R.layout.total_workouts_chart); }

    public TotalWorkoutsChart(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.total_workouts_chart);
    }

    void setup(HistoryViewModel.TotalWorkoutsChartViewModel model,
               IndexAxisValueFormatter xAxisFormatter) {
        viewModel = model;
        int[] colors = AppColors.getChartColors(getContext());
        lineColor = colors[0];

        Drawable fill = ContextCompat.getDrawable(getContext(), R.drawable.chart_gradient);
        dataSets[0] = createDataSet(ContextCompat.getColor(getContext(), R.color.chartRed));
        dataSets[0].setFillDrawable(fill);
        dataSets[0].setDrawFilled(true);
        dataSets[0].setFillAlpha(191);
        setupChartData(dataSets, 1);
        setupChartView(xAxisFormatter);
    }

    void updateChart(boolean isSmall, byte index) {
        chartView.getAxisLeft().removeAllLimitLines();
        LimitLine limitLine = new LimitLine(viewModel.avgs[index]);
        limitLine.enableDashedLine(10, 10, 0);
        limitLine.setLineWidth(2);
        limitLine.setLineColor(lineColor);
        chartView.getAxisLeft().addLimitLine(limitLine);
        updateData(0, isSmall, viewModel.dynamicEntries, 0, viewModel.legendLabel);
        data.setValueFormatter(new DefaultValueFormatter(2));
        update(isSmall, viewModel.maxes[index]);
    }
}
