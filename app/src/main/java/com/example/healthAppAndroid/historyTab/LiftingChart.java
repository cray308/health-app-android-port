package com.example.healthAppAndroid.historyTab;

import android.content.Context;
import android.util.AttributeSet;

import com.example.healthAppAndroid.R;

public final class LiftingChart extends ChartContainer {
    private HistoryViewModel.LiftModel m;

    public LiftingChart(Context c, AttributeSet attrs) {
        super(c, attrs, R.layout.lift_chart, new int[]{
          R.id.secondEntry, R.id.thirdEntry, R.id.fourthEntry
        });
    }

    void setup(HistoryViewModel model) {
        m = model.lifts;
        int[] colors = getChartColors(getContext());
        for (int i = 0; i < 4; ++i) {
            dataSets[i] = createDataSet(colors[i]);
            dataSets[i].setLineWidth(2);
        }
        setupChartData(dataSets, 4);
        setupChartView(model);
    }

    void updateChart(boolean isSmall, int index) {
        for (int i = 0; i < 4; ++i) {
            updateData(i, isSmall, m.entryRefs.get(index).get(i), i, m.legendLabels[i]);
        }
        update(isSmall, m.maxes[index]);
    }
}
