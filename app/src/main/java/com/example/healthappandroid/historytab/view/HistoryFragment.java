package com.example.healthappandroid.historytab.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.example.healthappandroid.R;
import com.example.healthappandroid.historytab.data.HistoryViewModel;
import com.example.healthappandroid.historytab.helpers.ChartUtility;
import com.example.healthappandroid.historytab.view.LiftingChart;
import com.example.healthappandroid.historytab.view.TotalWorkoutsChart;
import com.example.healthappandroid.historytab.view.WorkoutTypeChart;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

public class HistoryFragment extends Fragment {
    public static class Formatter extends IndexAxisValueFormatter {
        private final HistoryViewModel viewModel;

        public Formatter(HistoryViewModel viewModel) { this.viewModel = viewModel; }

        @Override public String getFormattedValue(float value) {
            return viewModel.getXAxisLabel((int) value);
        }
    }

    public final HistoryViewModel viewModel = new HistoryViewModel();
    private RadioGroup rangePicker;
    private TotalWorkoutsChart totalWorkoutsChart;
    private WorkoutTypeChart workoutTypeChart;
    private LiftingChart liftingChart;

    public HistoryFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Formatter formatter = new Formatter(viewModel);
        rangePicker = view.findViewById(R.id.historyTimePicker);
        totalWorkoutsChart = view.findViewById(R.id.totalWorkoutsChartContainer);
        workoutTypeChart = view.findViewById(R.id.workoutTypeChartContainer);
        liftingChart = view.findViewById(R.id.liftChartContainer);

        totalWorkoutsChart.setup(viewModel.totalWorkoutsViewModel, formatter);
        workoutTypeChart.setup(viewModel.workoutTypeViewModel, formatter);
        liftingChart.setup(viewModel.liftViewModel, formatter);
        rangePicker.setOnCheckedChangeListener(segmentListener);
    }

    private final RadioGroup.OnCheckedChangeListener segmentListener = (radioGroup, i) -> {
        int segment = 0;
        if (i == R.id.histSeg1Year) {
            segment = 1;
        } else if (i == R.id.histSeg2Year) {
            segment = 2;
        }
        viewModel.formatDataForTimeRange(segment);
        updateCharts();
    };

    public void performForegroundUpdate() {
        int id = R.id.histSeg6Months;
        rangePicker.check(id);
        segmentListener.onCheckedChanged(rangePicker, id);
    }

    private void updateCharts() {
        if (viewModel.data.size == 0) {
            ChartUtility.disableLineChartView(totalWorkoutsChart.chartView);
            ChartUtility.disableLineChartView(workoutTypeChart.chartView);
            ChartUtility.disableLineChartView(liftingChart.chartView);
            return;
        }

        int count = viewModel.totalWorkoutsViewModel.entries.length;
        boolean isSmall = viewModel.formatType == HistoryViewModel.FormatShort;
        totalWorkoutsChart.update(count, isSmall);
        workoutTypeChart.update(count, isSmall);
        liftingChart.update(count, isSmall);
    }
}