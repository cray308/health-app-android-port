package com.example.healthAppAndroid.historyTab;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.core.WeekDataModel;
import com.google.android.material.button.MaterialButtonToggleGroup;

public final class HistoryFragment extends Fragment {
    public static final class FetchHandler {
        private final HistoryFragment fragment;
        private final WeekDataModel data;

        private FetchHandler(HistoryFragment fragment, WeekDataModel data) {
            this.fragment = fragment;
            this.data = data;
        }

        public void completion() {
            if (data.size != 0)
                viewModel.populateData(data);
            new android.os.Handler(Looper.getMainLooper()).post(
              () -> fragment.rangePicker.check(R.id.buttonLeft));
        }
    }

    static final HistoryViewModel viewModel = new HistoryViewModel();
    private MaterialButtonToggleGroup rangePicker;
    private TotalWorkoutsChart totalWorkoutsChart;
    private WorkoutTypeChart workoutTypeChart;
    private LiftingChart liftingChart;
    private final MaterialButtonToggleGroup.OnButtonCheckedListener listener = (group, checkedId, isChecked) -> {
        if (!isChecked) return;
        int selected = 0;
        if (checkedId == R.id.buttonMid) {
            selected = 1;
        } else if (checkedId == R.id.buttonRight) {
            selected = 2;
        }
        didSelectSegment(selected);
    };
    private final boolean setIndex;

    public HistoryFragment() { setIndex = true; }

    public HistoryFragment(Object[] results) {
        WeekDataModel model = new WeekDataModel();
        results[0] = model;
        results[1] = new FetchHandler(this, model);
        setIndex = false;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, null);

        rangePicker = view.findViewById(R.id.rangePicker);
        rangePicker.addOnButtonCheckedListener(listener);
        totalWorkoutsChart = view.findViewById(R.id.totalWorkoutsContainer);
        workoutTypeChart = view.findViewById(R.id.workoutTypeContainer);
        liftingChart = view.findViewById(R.id.liftContainer);
        viewModel.setup(getResources());

        totalWorkoutsChart.setup();
        workoutTypeChart.setup();
        liftingChart.setup();
        if (setIndex) rangePicker.check(R.id.buttonLeft);
    }

    private void didSelectSegment(int index) {
        int count = viewModel.nEntries[index];
        if (count == 0) {
            totalWorkoutsChart.disable();
            workoutTypeChart.disable();
            liftingChart.disable();
            rangePicker.removeOnButtonCheckedListener(listener);
            return;
        }

        Context c = getContext();
        if (c == null) return;

        viewModel.formatDataForTimeRange(c, index);
        boolean isSmall = count < 7;
        totalWorkoutsChart.updateChart(isSmall, index);
        workoutTypeChart.updateChart(isSmall, index);
        liftingChart.updateChart(isSmall, index);
    }

    public void handleDataDeletion() {
        if (viewModel.nEntries[2] != 0) {
            viewModel.clearData();
            rangePicker.check(R.id.buttonLeft);
        }
    }
}
