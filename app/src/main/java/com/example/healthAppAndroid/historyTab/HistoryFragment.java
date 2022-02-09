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
import com.example.healthAppAndroid.core.PersistenceService;
import com.example.healthAppAndroid.core.SegmentedControl;

public final class HistoryFragment extends Fragment {
    private static final class FetchHandler implements PersistenceService.Block {
        private final HistoryFragment fragment;
        private final WeekDataModel data;

        private FetchHandler(HistoryFragment fragment, WeekDataModel data) {
            this.fragment = fragment;
            this.data = data;
        }

        public void completion() {
            if (data.size != 0)
                fragment.viewModel.populateData(data);
            new android.os.Handler(Looper.getMainLooper()).post(fragment::refresh);
        }
    }

    private static final class StartupHandler implements PersistenceService.Block {
        private final HistoryFragment fragment;

        private StartupHandler(HistoryFragment fragment) { this.fragment = fragment; }

        public void completion() {
            WeekDataModel model = new WeekDataModel();
            PersistenceService.fetchHistoryData(model, new FetchHandler(fragment, model));
        }
    }

    private final HistoryViewModel viewModel = new HistoryViewModel();
    private SegmentedControl rangePicker;
    private TotalWorkoutsChart totalWorkoutsChart;
    private WorkoutTypeChart workoutTypeChart;
    private LiftingChart liftingChart;

    public static HistoryFragment init(Object[] blockArray) {
        HistoryFragment fragment = new HistoryFragment();
        blockArray[0] = new StartupHandler(fragment);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rangePicker = view.findViewById(R.id.rangePicker);
        totalWorkoutsChart = view.findViewById(R.id.totalWorkoutsContainer);
        workoutTypeChart = view.findViewById(R.id.workoutTypeContainer);
        liftingChart = view.findViewById(R.id.liftContainer);
        viewModel.setup(getResources());

        totalWorkoutsChart.setup(viewModel.totalWorkouts, viewModel);
        workoutTypeChart.setup(viewModel.workoutTypes, viewModel);
        liftingChart.setup(viewModel.lifts, viewModel);
        rangePicker.delegate = this;
    }

    public void didSelectSegment(byte index) {
        int count = viewModel.nEntries[index];
        if (count == 0) {
            totalWorkoutsChart.disable();
            workoutTypeChart.disable();
            liftingChart.disable();
            rangePicker.delegate = null;
            return;
        }

        Context context = getContext();
        if (context == null) return;

        viewModel.formatDataForTimeRange(context, index);
        boolean isSmall = count < 7;
        totalWorkoutsChart.updateChart(isSmall, index);
        workoutTypeChart.updateChart(isSmall, index);
        liftingChart.updateChart(isSmall, index);
    }

    private void refresh() {
        rangePicker.setSelectedIndex((byte) 0);
    }

    public void handleDataDeletion() {
        if (viewModel.nEntries[2] != 0) {
            viewModel.clearData();
            refresh();
        }
    }
}
