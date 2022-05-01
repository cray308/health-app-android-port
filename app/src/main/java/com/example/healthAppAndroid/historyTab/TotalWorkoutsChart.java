package com.example.healthAppAndroid.historyTab;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.core.content.ContextCompat;

import com.example.healthAppAndroid.R;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.components.LimitLine;

public final class TotalWorkoutsChart extends ChartContainer {
    private final int lineColor;

    public TotalWorkoutsChart(Context c, AttributeSet attrs) {
        super(c, attrs, R.layout.total_workouts_chart, null);
        lineColor = ContextCompat.getColor(c, R.color.chartLimit);
    }

    void setup() {
        Context c = getContext();
        Drawable fill = ContextCompat.getDrawable(c, R.drawable.chart_gradient);
        dataSets[0] = createDataSet(ContextCompat.getColor(c, R.color.chartRed));
        dataSets[0].setFillDrawable(fill);
        dataSets[0].setDrawFilled(true);
        dataSets[0].setFillAlpha(191);
        setupChartData(dataSets, 1);
        setupChartView();
    }

    void updateChart(boolean isSmall, int index) {
        HistoryViewModel.TotalWorkoutsModel m = HistoryFragment.viewModel.totalWorkouts;
        axis.removeAllLimitLines();
        LimitLine limitLine = new LimitLine(m.avgs[index]);
        limitLine.enableDashedLine(10, 10, 0);
        limitLine.setLineWidth(2);
        limitLine.setLineColor(lineColor);
        axis.addLimitLine(limitLine);
        updateData(0, isSmall, m.entryRefs.get(index), 0, m.legendLabel);
        data.setValueFormatter(new DefaultValueFormatter(2));
        update(isSmall, m.maxes[index]);
    }
}
