package com.example.healthAppAndroid.historyTab.view;

import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.shareddata.PersistenceService;
import com.example.healthAppAndroid.common.views.SegmentedControl;
import com.example.healthAppAndroid.historyTab.data.HistoryViewModel;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.Locale;

public final class HistoryFragment extends Fragment {
    private static final class FetchHandler implements PersistenceService.Block {
        private final HistoryFragment fragment;
        private final HistoryViewModel.WeekDataModel data;

        private FetchHandler(HistoryFragment fragment,
                             HistoryViewModel.WeekDataModel data) {
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
            HistoryViewModel.WeekDataModel model = new HistoryViewModel.WeekDataModel();
            PersistenceService.fetchHistoryData(model, new FetchHandler(fragment, model));
        }
    }

    static final class Formatter extends IndexAxisValueFormatter {
        private final String[] months;
        HistoryViewModel.WeekDataModel.TimeData[] timeData;

        private Formatter(HistoryViewModel.WeekDataModel.TimeData[] timeData, Resources res) {
            this.timeData = timeData;
            months = res.getStringArray(R.array.months);
        }

        public String getFormattedValue(float value) {
            HistoryViewModel.WeekDataModel.TimeData data = timeData[(int) value];
            return String.format(Locale.US, "%s/%d/%d", months[data.month], data.day, data.year);
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
        Resources res = getResources();
        Formatter formatter = new Formatter(viewModel.timeData, res);
        viewModel.setup(res);

        totalWorkoutsChart.setup(viewModel.totalWorkouts, formatter);
        workoutTypeChart.setup(viewModel.workoutTypes, formatter);
        liftingChart.setup(viewModel.lifts, formatter);
        rangePicker.delegate = this;
    }

    public void didSelectSegment(byte index) {
        if (viewModel.nEntries[2] == 0) {
            totalWorkoutsChart.disable();
            workoutTypeChart.disable();
            liftingChart.disable();
            return;
        }

        viewModel.formatDataForTimeRange(getContext(), index);
        boolean isSmall = viewModel.nEntries[index] < 7;
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
