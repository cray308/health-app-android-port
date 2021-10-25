package com.example.healthAppAndroid.historyTab.view;

import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.helpers.ViewHelper;
import com.example.healthAppAndroid.common.views.SegmentedControl;
import com.example.healthAppAndroid.historyTab.data.HistoryViewModel;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

public final class HistoryFragment extends Fragment {
    private static final class Formatter extends IndexAxisValueFormatter {
        private final String[] months;
        private final HistoryViewModel viewModel;

        private Formatter(HistoryViewModel viewModel, Resources res) {
            this.viewModel = viewModel;
            months = res.getStringArray(R.array.months);
        }

        @Override public String getFormattedValue(float value) {
            HistoryViewModel.WeekDataModel.Week model = viewModel.data.arr[(int) value];
            return ViewHelper.format("%s/%d/%d", months[model.month], model.day, model.year);
        }
    }

    public final HistoryViewModel viewModel = new HistoryViewModel();
    private SegmentedControl rangePicker;
    private TotalWorkoutsChart totalWorkoutsChart;
    private WorkoutTypeChart workoutTypeChart;
    private LiftingChart liftingChart;

    @Override public View onCreateView(LayoutInflater inflater,
                                       ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rangePicker = view.findViewById(R.id.rangePicker);
        totalWorkoutsChart = view.findViewById(R.id.totalWorkoutsContainer);
        workoutTypeChart = view.findViewById(R.id.workoutTypeContainer);
        liftingChart = view.findViewById(R.id.liftContainer);
        Resources res = getResources();
        Formatter formatter = new Formatter(viewModel, res);
        viewModel.setup(res);

        totalWorkoutsChart.setup(viewModel.totalWorkoutsViewModel, formatter);
        workoutTypeChart.setup(viewModel.workoutTypeViewModel, formatter);
        liftingChart.setup(viewModel.liftViewModel, formatter);
        rangePicker.delegate = this;
    }

    public void didSelectSegment(byte index) {
        viewModel.formatDataForTimeRange(getContext(), index);
        updateCharts();
    }

    public void performForegroundUpdate() {
        rangePicker.setSelectedIndex((byte) 0);
    }

    private void updateCharts() {
        if (viewModel.data.size == 0) {
            totalWorkoutsChart.disable();
            workoutTypeChart.disable();
            liftingChart.disable();
            return;
        }

        totalWorkoutsChart.update(viewModel.isSmall);
        workoutTypeChart.update(viewModel.isSmall);
        liftingChart.update(viewModel.isSmall);
    }
}
