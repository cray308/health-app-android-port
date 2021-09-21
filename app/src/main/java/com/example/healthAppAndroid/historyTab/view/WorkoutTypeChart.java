package com.example.healthAppAndroid.historyTab.view;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.shareddata.AppColors;
import com.example.healthAppAndroid.historyTab.data.HistoryViewModel;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.Arrays;

public class WorkoutTypeChart extends ChartContainer {
    private static class Formatter extends IndexAxisValueFormatter {
        private final HistoryViewModel.WorkoutTypeChartViewModel viewModel;

        private Formatter(HistoryViewModel.WorkoutTypeChartViewModel viewModel) {
            this.viewModel = viewModel;
        }

        public String getFormattedValue(float value) {
            return viewModel.getDuration((int) value);
        }
    }

    HistoryViewModel.WorkoutTypeChartViewModel viewModel;

    public WorkoutTypeChart(Context context) { super(context); }

    public WorkoutTypeChart(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    void setup() {
        inflate(getContext(), R.layout.workout_type_chart, this);
        init();
        legendEntries[1] = findViewById(R.id.secondEntry);
        legendEntries[2] = findViewById(R.id.thirdEntry);
        legendEntries[3] = findViewById(R.id.fourthEntry);
    }

    void setup(HistoryViewModel.WorkoutTypeChartViewModel viewModel,
               IndexAxisValueFormatter xAxisFormatter) {
        this.viewModel = viewModel;
        Formatter formatter = new Formatter(viewModel);

        dataSets[0] = createEmptyDataSet();
        for (int i = 1; i < 5; ++i) {
            dataSets[i] = createDataSet(AppColors.chartColors[i - 1]);
            dataSets[i].setFillColor(AppColors.chartColors[i - 1]);
            dataSets[i].setDrawFilled(true);
            dataSets[i].setFillAlpha(191);
            AreaChartRenderer.Formatter fillFormatter = new AreaChartRenderer.Formatter(
                dataSets[i - 1]);
            dataSets[i].setFillFormatter(fillFormatter);
        }

        setupChartData(new LineDataSet[]{dataSets[4], dataSets[3], dataSets[2], dataSets[1]}, 4);
        data.setValueFormatter(formatter);

        setupChartView(xAxisFormatter);
        chartView.getAxisLeft().setValueFormatter(formatter);

        AreaChartRenderer renderer = new AreaChartRenderer(chartView);
        chartView.setRenderer(renderer);
    }

    void update(int count, boolean isSmall) {
        dataSets[0].setValues(Arrays.asList(viewModel.entries[0]));
        for (int i = 1; i < 5; ++i)
            updateData(i, isSmall, viewModel.entries[i], i - 1, viewModel.legendLabels[i - 1]);
        update(isSmall, count, viewModel.yMax);
    }
}
