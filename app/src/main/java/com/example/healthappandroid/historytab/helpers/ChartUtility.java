package com.example.healthappandroid.historytab.helpers;

import com.example.healthappandroid.common.shareddata.AppColors;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.Arrays;
import java.util.Collections;

public abstract class ChartUtility {
    public static void setupChartView(
            LineChart view, IndexAxisValueFormatter xAxisFormatter, LegendEntry[] legendEntries) {
        view.setNoDataText("No data is available");
        YAxis leftAxis = view.getAxisLeft();
        leftAxis.setAxisMinimum(0);
        leftAxis.setTextColor(AppColors.labelNormal);
        view.getAxisRight().setEnabled(false);
        Legend legend = view.getLegend();
        legend.setMaxSizePercent(0.2f);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        legend.setTextSize(16);
        legend.setTextColor(AppColors.labelNormal);
        legend.setCustom(legendEntries);
        legend.setEnabled(false);
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

    public static void setupLegendEntries(LegendEntry[] entries, int[] colors, int count) {
        for (int i = 0; i < count; ++i) {
            entries[i] = new LegendEntry("", Legend.LegendForm.DEFAULT, 20,
                    Float.NaN, null, colors[i]);
        }
    }

    public static void disableLineChartView(LineChart view) {
        view.getLegend().setEnabled(false);
        view.setData(null);
        view.notifyDataSetChanged();
    }

    public static void updateDataSet(boolean isSmall, LineDataSet dataSet, Entry[] entries) {
        dataSet.setDrawCircles(isSmall);
        dataSet.setValues(Arrays.asList(entries));
    }

    public static void updateChart(boolean isSmall, int count,
                                   LineChart v, LineData data, float axisMax) {
        v.getAxisLeft().setAxisMaximum(axisMax);
        v.getXAxis().setLabelCount(isSmall ? count : 6);
        v.getLegend().setEnabled(true);
        data.setDrawValues(isSmall);
        v.setData(data);
        data.notifyDataChanged();
        v.notifyDataSetChanged();
        v.animateX(isSmall ? 1500 : 2500);
    }
}
