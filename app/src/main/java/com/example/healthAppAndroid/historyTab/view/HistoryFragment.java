package com.example.healthAppAndroid.historyTab.view;

import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.helpers.ViewHelper;
import com.example.healthAppAndroid.historyTab.data.HistoryViewModel;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

public class HistoryFragment extends Fragment {
    private static class Formatter extends IndexAxisValueFormatter {
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
    private RadioGroup rangePicker;
    private TotalWorkoutsChart totalWorkoutsChart;
    private WorkoutTypeChart workoutTypeChart;
    private LiftingChart liftingChart;

    public HistoryFragment() {}

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
        rangePicker.setOnCheckedChangeListener(segmentListener);
    }

    private final RadioGroup.OnCheckedChangeListener segmentListener = (radioGroup, i) -> {
        byte segment = HistoryViewModel.Segment.sixMonths;
        if (i == R.id.segmentCenter) {
            segment = HistoryViewModel.Segment.oneYear;
        } else if (i == R.id.segmentRight) {
            segment = HistoryViewModel.Segment.twoYears;
        }
        viewModel.formatDataForTimeRange(getContext(), segment);
        updateCharts();
    };

    public void performForegroundUpdate() {
        int id = R.id.segmentLeft;
        rangePicker.check(id);
        segmentListener.onCheckedChanged(rangePicker, id);
    }

    private void updateCharts() {
        if (viewModel.data.size == 0) {
            totalWorkoutsChart.disable();
            workoutTypeChart.disable();
            liftingChart.disable();
            return;
        }

        int count = viewModel.totalWorkoutsViewModel.entries.length;
        totalWorkoutsChart.update(count, viewModel.isSmall);
        workoutTypeChart.update(count, viewModel.isSmall);
        liftingChart.update(count, viewModel.isSmall);
    }
}
