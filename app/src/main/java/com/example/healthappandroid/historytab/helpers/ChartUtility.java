package com.example.healthappandroid.historytab.helpers;

import android.view.View;

import com.example.healthappandroid.common.shareddata.AppColors;
import com.example.healthappandroid.historytab.view.ChartContainer;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.Arrays;
import java.util.Collections;

public abstract class ChartUtility {
    public static void setupChartView(LineChart view, IndexAxisValueFormatter xAxisFormatter) {
        view.setNoDataText("No data is available");
        YAxis leftAxis = view.getAxisLeft();
        leftAxis.setAxisMinimum(0);
        leftAxis.setTextColor(AppColors.labelNormal);
        view.getAxisRight().setEnabled(false);
        view.getLegend().setEnabled(false);
        XAxis xAxis = view.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGridLineWidth((float) 1.5);
        xAxis.setTextSize(12);
        xAxis.setTextColor(AppColors.labelNormal);
        xAxis.setGranularityEnabled(true);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setLabelRotationAngle(45);
        xAxis.setValueFormatter(xAxisFormatter);
    }

    public static LineDataSet createEmptyDataSet() {
        return new LineDataSet(Collections.emptyList(), null);
    }

    public static LineDataSet createDataSet(int color) {
        LineDataSet dataSet = createEmptyDataSet();
        dataSet.setColor(color);
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSet.setValueTextSize(11);
        dataSet.setValueTextColor(AppColors.labelNormal);
        dataSet.setCircleColor(color);
        dataSet.setDrawCircleHole(false);
        dataSet.setCircleRadius(2);
        return dataSet;
    }

    public static LineData createChartData(LineDataSet[] dataSets, int count) {
        LineData data = new LineData();
        for (int i = 0; i < count; ++i)
            data.addDataSet(dataSets[i]);
        return data;
    }

    public static void disableLineChartView(ChartContainer container) {
        container.getLegend().setVisibility(View.GONE);
        LineChart view = container.getChartView();
        view.setData(null);
        view.notifyDataSetChanged();
    }

    public static void updateDataSet(boolean isSmall, LineDataSet dataSet, Entry[] entries) {
        dataSet.setDrawCircles(isSmall);
        dataSet.setValues(Arrays.asList(entries));
    }

    public static void updateChart(boolean isSmall, int count,
                                   ChartContainer container, float axisMax) {
        LineChart view = container.getChartView();
        view.getAxisLeft().setAxisMaximum(axisMax);
        view.getXAxis().setLabelCount(isSmall ? count : 6);
        container.getLegend().setVisibility(View.VISIBLE);
        LineData data = container.getData();
        data.setDrawValues(isSmall);
        view.setData(data);
        data.notifyDataChanged();
        view.notifyDataSetChanged();
        view.animateX(isSmall ? 1500 : 2500);
    }
}
