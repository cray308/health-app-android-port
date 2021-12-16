package com.example.healthAppAndroid.historyTab.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.shareddata.AppColors;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.Arrays;
import java.util.Collections;

public abstract class ChartContainer extends LinearLayout {
    LineChart chartView;
    private LinearLayout legendContainer;
    final HistoryChartLegendEntry[] legendEntries = {null, null, null, null};
    final LineDataSet[] dataSets = {null, null, null, null, null};
    final LineData data = new LineData();

    public ChartContainer(Context context) { super(context); }

    ChartContainer(Context context, int id) {
        super(context);
        init(id);
    }

    public ChartContainer(Context context, AttributeSet attrs) { super(context, attrs); }

    ChartContainer(Context context, AttributeSet attrs, int id) {
        super(context, attrs);
        init(id);
    }

    private void init(int id) {
        inflate(getContext(), id, this);
        chartView = findViewById(R.id.chartView);
        legendContainer = findViewById(R.id.legendContainer);
        legendEntries[0] = findViewById(R.id.firstEntry);
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

    void setupChartView(IndexAxisValueFormatter xAxisFormatter) {
        chartView.setNoDataText(chartView.getContext().getString(R.string.chartEmptyText));
        chartView.getDescription().setEnabled(false);
        YAxis leftAxis = chartView.getAxisLeft();
        leftAxis.setAxisMinimum(0);
        leftAxis.setTextSize(10);
        leftAxis.setTextColor(AppColors.labelNormal);
        chartView.getAxisRight().setEnabled(false);
        chartView.getLegend().setEnabled(false);
        XAxis xAxis = chartView.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGridLineWidth(0.5f);
        xAxis.setTextSize(10);
        xAxis.setTextColor(AppColors.labelNormal);
        xAxis.setGranularityEnabled(true);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setLabelRotationAngle(45);
        xAxis.setValueFormatter(xAxisFormatter);
    }

    void disable() {
        legendContainer.setVisibility(View.GONE);
        for (int i = 0; i < 5; ++i) {
            if (dataSets[i] != null)
                dataSets[i].setValues(null);
        }
        chartView.setData(null);
        chartView.notifyDataSetChanged();
        HistoryFragment.Formatter formatter =
          (HistoryFragment.Formatter) chartView.getXAxis().getValueFormatter();
        formatter.timeData = null;
    }

    void updateData(int index, boolean isSmall, Entry[] entries, int iLegend, String text) {
        dataSets[index].setDrawCircles(isSmall);
        dataSets[index].setValues(Arrays.asList(entries));
        legendEntries[iLegend].label.setText(text);
    }

    void update(boolean isSmall, float axisMax) {
        chartView.zoom(0.01f, 0.01f, 0, 0);
        chartView.getAxisLeft().setAxisMaximum(axisMax);
        legendContainer.setVisibility(View.VISIBLE);
        data.setDrawValues(isSmall);
        chartView.setData(data);
        data.notifyDataChanged();
        chartView.notifyDataSetChanged();
        chartView.animateX(isSmall ? 1500 : 2500);
    }
}
