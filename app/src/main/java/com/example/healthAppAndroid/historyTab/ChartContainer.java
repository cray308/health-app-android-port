package com.example.healthAppAndroid.historyTab;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.example.healthAppAndroid.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.Collections;
import java.util.List;

public abstract class ChartContainer extends LinearLayout {
    static final int FillAlpha = 191;

    static LineDataSet createEmptyDataSet() {
        return new LineDataSet(Collections.emptyList(), null);
    }

    static LineDataSet createDataSet(int chartColor, int labelColor, boolean ltr) {
        LineDataSet dataSet = createEmptyDataSet();
        dataSet.setColor(chartColor);
        dataSet.setAxisDependency(ltr ? YAxis.AxisDependency.LEFT : YAxis.AxisDependency.RIGHT);
        dataSet.setValueTextSize(10);
        dataSet.setValueTextColor(labelColor);
        dataSet.setCircleColor(chartColor);
        dataSet.setDrawCircleHole(false);
        dataSet.setCircleRadius(2);
        return dataSet;
    }

    final LineChart chart;
    private final HistoryLegendEntry[] legendEntries = {null, null, null, null};
    final LineDataSet[] sets = {null, null, null, null, null};
    final LineData data = new LineData();
    YAxis yAxis;

    ChartContainer(Context context, AttributeSet attrs, int id, int[] legendIds) {
        super(context, attrs);
        inflate(context, id, this);
        chart = findViewById(R.id.chart);
        legendEntries[0] = findViewById(R.id.firstEntry);
        if (legendIds != null) {
            int count = legendIds.length;
            for (int i = 0; i < count; ++i) {
                legendEntries[i + 1] = findViewById(legendIds[i]);
            }
        }
    }

    void setupChartData(LineDataSet[] sets) { for (LineDataSet s : sets) { data.addDataSet(s); } }

    void setupChartView(HistoryModel model, int labelColor, String defaultText, boolean ltr) {
        chart.setNoDataText(defaultText);
        chart.getDescription().setEnabled(false);
        if (ltr) {
            yAxis = chart.getAxisLeft();
            chart.getAxisRight().setEnabled(false);
        } else {
            yAxis = chart.getAxisRight();
            chart.getAxisLeft().setEnabled(false);
        }
        yAxis.setEnabled(true);
        yAxis.setAxisMinimum(0);
        yAxis.setTextSize(10);
        yAxis.setTextColor(labelColor);
        chart.getLegend().setEnabled(false);
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGridLineWidth(0.5f);
        xAxis.setTextSize(10);
        xAxis.setTextColor(labelColor);
        xAxis.setGranularityEnabled(true);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setLabelRotationAngle(ltr ? 45 : -45);
        xAxis.setValueFormatter(model);
    }

    public void disable() {
        findViewById(R.id.legendStack).setVisibility(View.GONE);
        for (LineDataSet s : sets) { if (s != null) s.setValues(null); }
        chart.setData(null);
        chart.notifyDataSetChanged();
    }

    void updateData(int index, boolean isSmall,
                    List<Entry> entries, int legendIndex, CharSequence legendText) {
        sets[index].setDrawCircles(isSmall);
        sets[index].setValues(entries);
        legendEntries[legendIndex].label.setText(legendText);
    }

    void update(boolean isSmall, float axisMax) {
        chart.zoom(0.01f, 0.01f, 0, 0);
        yAxis.setAxisMaximum(axisMax);
        data.setDrawValues(isSmall);
        chart.setData(data);
        data.notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.animateX(isSmall ? 1500 : 2500);
    }
}
