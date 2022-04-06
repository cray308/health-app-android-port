package com.example.healthAppAndroid.historyTab;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.core.AppColors;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.Collections;
import java.util.List;

public abstract class ChartContainer extends LinearLayout {
    LineChart chart;
    private final HistoryChartLegendEntry[] legendEntries = {null, null, null, null};
    final LineDataSet[] dataSets = {null, null, null, null, null};
    final LineData data = new LineData();

    public ChartContainer(Context c, AttributeSet attrs) { super(c, attrs); }

    ChartContainer(Context c, AttributeSet attrs, int id, int[] legendIds) {
        super(c, attrs);
        inflate(c, id, this);
        chart = findViewById(R.id.chartView);
        legendEntries[0] = findViewById(R.id.firstEntry);
        if (legendIds != null) {
            int count = legendIds.length;
            for (int i = 0; i < count; ++i) {
                legendEntries[i + 1] = findViewById(legendIds[i]);
            }
        }
    }

    static int[] getChartColors(Context c) {
        return new int[]{ContextCompat.getColor(c, R.color.chartBlue),
                         ContextCompat.getColor(c, R.color.chartGreen),
                         ContextCompat.getColor(c, R.color.chartOrange),
                         ContextCompat.getColor(c, R.color.chartPink)};
    }

    static LineDataSet createEmptyDataSet() {
        return new LineDataSet(Collections.emptyList(), null);
    }

    static LineDataSet createDataSet(int color) {
        LineDataSet dataSet = createEmptyDataSet();
        dataSet.setColor(color);
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSet.setValueTextSize(10);
        dataSet.setValueTextColor(AppColors.labelNormal);
        dataSet.setCircleColor(color);
        dataSet.setDrawCircleHole(false);
        dataSet.setCircleRadius(2);
        return dataSet;
    }

    void setupChartData(LineDataSet[] sets, int count) {
        for (int i = 0; i < count; ++i)
            data.addDataSet(sets[i]);
    }

    void setupChartView() {
        chart.setNoDataText(chart.getContext().getString(R.string.chartEmptyText));
        chart.getDescription().setEnabled(false);
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0);
        leftAxis.setTextSize(10);
        leftAxis.setTextColor(AppColors.labelNormal);
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(false);
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGridLineWidth(0.5f);
        xAxis.setTextSize(10);
        xAxis.setTextColor(AppColors.labelNormal);
        xAxis.setGranularityEnabled(true);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setLabelRotationAngle(45);
        xAxis.setValueFormatter(HistoryFragment.viewModel);
    }

    void disable() {
        findViewById(R.id.legendContainer).setVisibility(View.GONE);
        for (int i = 0; i < 5; ++i) {
            if (dataSets[i] != null)
                dataSets[i].setValues(null);
        }
        chart.setData(null);
        chart.notifyDataSetChanged();
    }

    void updateData(int index, boolean isSmall, List<Entry> entries, int iLegend, String text) {
        dataSets[index].setDrawCircles(isSmall);
        dataSets[index].setValues(entries);
        legendEntries[iLegend].label.setText(text);
    }

    void update(boolean isSmall, float axisMax) {
        chart.zoom(0.01f, 0.01f, 0, 0);
        chart.getAxisLeft().setAxisMaximum(axisMax);
        data.setDrawValues(isSmall);
        chart.setData(data);
        data.notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.animateX(isSmall ? 1500 : 2500);
    }
}
