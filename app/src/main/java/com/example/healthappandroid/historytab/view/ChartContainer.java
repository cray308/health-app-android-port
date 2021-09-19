package com.example.healthappandroid.historytab.view;

import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;

public interface ChartContainer {
    LineChart getChartView();
    LinearLayout getLegend();
    LineData getData();
}
