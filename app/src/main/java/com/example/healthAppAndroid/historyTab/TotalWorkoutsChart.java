package com.example.healthAppAndroid.historyTab;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.core.content.ContextCompat;

import com.example.healthAppAndroid.R;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

public final class TotalWorkoutsChart extends ChartContainer {
    private HistoryViewModel.TotalWorkoutsChartViewModel viewModel;
    private final int lineColor;

    public TotalWorkoutsChart(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.total_workouts_chart, null);
        lineColor = ContextCompat.getColor(context, R.color.chartLimit);
    }

    void setup(HistoryViewModel.TotalWorkoutsChartViewModel model,
               IndexAxisValueFormatter xAxisFormatter) {
        viewModel = model;
        Context context = getContext();
        Drawable fill = ContextCompat.getDrawable(context, R.drawable.chart_gradient);
        dataSets[0] = createDataSet(ContextCompat.getColor(context, R.color.chartRed));
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
        updateData(0, isSmall, viewModel.entryRefs.get(index), 0, viewModel.legendLabel);
        data.setValueFormatter(new DefaultValueFormatter(2));
        update(isSmall, viewModel.maxes[index]);
    }
}
