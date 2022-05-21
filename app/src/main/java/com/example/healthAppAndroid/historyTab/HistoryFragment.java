package com.example.healthAppAndroid.historyTab;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.core.PersistenceManager;
import com.example.healthAppAndroid.core.SegmentedControl;

import java.util.Locale;

public final class HistoryFragment extends Fragment implements SegmentedControl.Delegate {
    interface HistoryChart {
        void disable();
        void updateChart(boolean isSmall, int index);
        void setup(HistoryModel historyModel, int[] chartColors, int labelColor,
                   String defaultText, boolean ltr);
    }

    public static final class Block {
        private final HistoryFragment fragment;

        public Block(HistoryFragment fragment) { this.fragment = fragment; }

        public void completion(PersistenceManager.WeeklyData[] weeks, String[] axisStrings) {
            if (weeks != null) fragment.model.populate(weeks, axisStrings);
            fragment.rangeControl.setSelectedIndex(0);
        }
    }

    private final HistoryModel model = new HistoryModel();
    private SegmentedControl rangeControl;
    private final HistoryChart[] charts = {null, null, null};

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, null);
        Context context = getContext();
        if (context == null) return;

        rangeControl = view.findViewById(R.id.rangeControl);
        rangeControl.delegate = this;
        charts[0] = view.findViewById(R.id.totalWorkoutsChart);
        charts[1] = view.findViewById(R.id.workoutTypeChart);
        charts[2] = view.findViewById(R.id.liftChart);

        int labelColor = ContextCompat.getColor(context, R.color.label);
        int[] chartColors = {
          ContextCompat.getColor(context, R.color.chartBlue),
          ContextCompat.getColor(context, R.color.chartGreen),
          ContextCompat.getColor(context, R.color.chartOrange),
          ContextCompat.getColor(context, R.color.chartPink)
        };
        String defaultText = getString(R.string.chartEmptyText);
        boolean ltr = HistoryModel.isLtr(Locale.getDefault());
        for (HistoryChart v : charts) { v.setup(model, chartColors, labelColor, defaultText, ltr); }
    }

    public void selectedIndexChanged(int index) {
        int count = model.nEntries[index];
        if (count == 0) {
            for (HistoryChart v : charts) { v.disable(); }
            rangeControl.delegate = null;
            return;
        }

        Context context = getContext();
        if (context != null) model.formatDataForTimeRange(context, index);
        boolean isSmall = count < 7;
        for (HistoryChart v : charts) { v.updateChart(isSmall, index); }
    }

    public void clearData() {
        if (model.nEntries[2] != 0) {
            model.clear();
            rangeControl.setSelectedIndex(0);
        }
    }
}
