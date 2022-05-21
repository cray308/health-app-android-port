package com.example.healthAppAndroid.historyTab;

import android.content.Context;
import android.util.AttributeSet;

import androidx.core.content.ContextCompat;

import com.example.healthAppAndroid.R;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.components.LimitLine;

public final class TotalWorkoutsChart extends ChartContainer implements HistoryFragment.HistoryChart {
    private HistoryModel.TotalWorkoutsModel model;

    public TotalWorkoutsChart(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.total_workouts_chart, null);
    }

    public void setup(HistoryModel historyModel, int[] chartColors, int labelColor,
                      String defaultText, boolean ltr) {
        model = historyModel.totals;
        Context context = getContext();
        sets[0] = createDataSet(ContextCompat.getColor(context, R.color.chartRed), labelColor, ltr);
        sets[0].setFillDrawable(ContextCompat.getDrawable(context, R.drawable.chart_gradient));
        sets[0].setDrawFilled(true);
        sets[0].setFillAlpha(FillAlpha);
        setupChartData(sets);
        setupChartView(historyModel, labelColor, defaultText, ltr);
    }

    public void updateChart(boolean isSmall, int index) {
        yAxis.removeAllLimitLines();
        LimitLine limitLine = new LimitLine(model.avgs[index]);
        limitLine.enableDashedLine(10, 10, 0);
        limitLine.setLineWidth(2);
        limitLine.setLineColor(ContextCompat.getColor(getContext(), R.color.chartLimit));
        yAxis.addLimitLine(limitLine);
        updateData(0, isSmall, model.refs.get(index), 0, model.legendLabel);
        data.setValueFormatter(new DefaultValueFormatter(2));
        update(isSmall, model.maxes[index]);
    }
}
