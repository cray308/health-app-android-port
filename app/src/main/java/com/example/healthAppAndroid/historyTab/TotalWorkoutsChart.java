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
    private HistoryViewModel.TotalWorkoutsModel model;
    private final int lineColor;

    public TotalWorkoutsChart(Context c, AttributeSet attrs) {
        super(c, attrs, R.layout.total_workouts_chart, null);
        lineColor = ContextCompat.getColor(c, R.color.chartLimit);
    }

    void setup(HistoryViewModel.TotalWorkoutsModel m, IndexAxisValueFormatter xAxisFormatter) {
        model = m;
        Context c = getContext();
        Drawable fill = ContextCompat.getDrawable(c, R.drawable.chart_gradient);
        dataSets[0] = createDataSet(ContextCompat.getColor(c, R.color.chartRed));
        dataSets[0].setFillDrawable(fill);
        dataSets[0].setDrawFilled(true);
        dataSets[0].setFillAlpha(191);
        setupChartData(dataSets, 1);
        setupChartView(xAxisFormatter);
    }

    void updateChart(boolean isSmall, int index) {
        chart.getAxisLeft().removeAllLimitLines();
        LimitLine limitLine = new LimitLine(model.avgs[index]);
        limitLine.enableDashedLine(10, 10, 0);
        limitLine.setLineWidth(2);
        limitLine.setLineColor(lineColor);
        chart.getAxisLeft().addLimitLine(limitLine);
        updateData(0, isSmall, model.entryRefs.get(index), 0, model.legendLabel);
        data.setValueFormatter(new DefaultValueFormatter(2));
        update(isSmall, model.maxes[index]);
    }
}
