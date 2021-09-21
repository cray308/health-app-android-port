package com.example.healthAppAndroid.historyTab.view;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.historyTab.data.HistoryViewModel;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

public class HistoryFragment extends Fragment {
    private static class Formatter extends IndexAxisValueFormatter {
        private final HistoryViewModel viewModel;

        private Formatter(HistoryViewModel viewModel) { this.viewModel = viewModel; }

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
        rangePicker = view.findViewById(R.id.rangePicker);
        totalWorkoutsChart = view.findViewById(R.id.totalWorkoutsContainer);
        workoutTypeChart = view.findViewById(R.id.workoutTypeContainer);
        liftingChart = view.findViewById(R.id.liftContainer);
        Context context = getContext();
        if (context != null)
            viewModel.setup(context);

        totalWorkoutsChart.setup(viewModel.totalWorkoutsViewModel, formatter);
        workoutTypeChart.setup(viewModel.workoutTypeViewModel, formatter);
        liftingChart.setup(viewModel.liftViewModel, formatter);
        rangePicker.setOnCheckedChangeListener(segmentListener);
    }

    private final RadioGroup.OnCheckedChangeListener segmentListener = (radioGroup, i) -> {
        int segment = 0;
        if (i == R.id.segmentCenter) {
            segment = 1;
        } else if (i == R.id.segmentRight) {
            segment = 2;
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
        boolean isSmall = viewModel.formatType == HistoryViewModel.Format.Short;
        totalWorkoutsChart.update(count, isSmall);
        workoutTypeChart.update(count, isSmall);
        liftingChart.update(count, isSmall);
    }
}
