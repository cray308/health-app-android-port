package com.example.healthAppAndroid.historyTab;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.example.healthAppAndroid.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.renderer.LineChartRenderer;
import com.github.mikephil.charting.utils.Transformer;

import java.util.List;

public final class WorkoutTypeChart extends ChartContainer {
    private static final class Renderer extends LineChartRenderer {
        private static final class Formatter implements IFillFormatter {
            private final LineDataSet set;

            private Formatter(LineDataSet boundaryDataSet) { set = boundaryDataSet; }

            public float getFillLinePosition(ILineDataSet s, LineDataProvider p) { return 0; }
        }

        private Renderer(LineChart v) { super(v, v.getAnimator(), v.getViewPortHandler()); }

        protected void drawLinearFill(Canvas c, ILineDataSet set, Transformer trans, XBounds bounds) {
            Path filled = mGenerateFilledPathBuffer;
            int startIndex = bounds.min, endIndex = bounds.range + bounds.min, indexInterval = 128;
            int currStart, currEnd, i = 0;

            do {
                currStart = startIndex + (i * indexInterval);
                currEnd = currStart + indexInterval;
                currEnd = Math.min(currEnd, endIndex);

                if (currStart <= currEnd) {
                    createFilledPath(set, currStart, currEnd, filled);
                    trans.pathValueToPixel(filled);
                    Drawable drawable = set.getFillDrawable();
                    if (drawable != null) {
                        drawFilledPath(c, filled, drawable);
                    } else {
                        drawFilledPath(c, filled, set.getFillColor(), set.getFillAlpha());
                    }
                }
                ++i;
            } while (currStart <= currEnd);
        }

        private void createFilledPath(ILineDataSet set, int start, int end, Path filled) {
            List<Entry> entries = ((Renderer.Formatter)set.getFillFormatter()).set.getValues();
            float phaseY = mAnimator.getPhaseY();
            filled.reset();

            Entry entry = set.getEntryForIndex(start);
            filled.moveTo(entry.getX(), entries.get(0).getY());
            filled.lineTo(entry.getX(), entry.getY() * phaseY);

            for (int x = start + 1; x <= end; ++x) {
                Entry curr = set.getEntryForIndex(x);
                filled.lineTo(curr.getX(), curr.getY() * phaseY);
            }

            for (int x = end; x > start; --x) {
                Entry prev = entries.get(x);
                filled.lineTo(prev.getX(), prev.getY() * phaseY);
            }
            filled.close();
        }
    }

    private HistoryViewModel.WorkoutTypeModel model;

    public WorkoutTypeChart(Context c, AttributeSet attrs) {
        super(c, attrs, R.layout.workout_type_chart, new int[]{
          R.id.secondEntry, R.id.thirdEntry, R.id.fourthEntry
        });
    }

    void setup(HistoryViewModel.WorkoutTypeModel m, IndexAxisValueFormatter xAxisFormatter) {
        model = m;

        int[] colors = getChartColors(getContext());
        dataSets[0] = createEmptyDataSet();
        for (int i = 1; i < 5; ++i) {
            dataSets[i] = createDataSet(colors[i - 1]);
            dataSets[i].setFillColor(colors[i - 1]);
            dataSets[i].setDrawFilled(true);
            dataSets[i].setFillAlpha(191);
            dataSets[i].setFillFormatter(new Renderer.Formatter(dataSets[i - 1]));
        }
        LineDataSet[] orderedSets = {dataSets[4], dataSets[3], dataSets[2], dataSets[1]};
        setupChartData(orderedSets, 4);
        data.setValueFormatter(model);
        setupChartView(xAxisFormatter);
        chart.getAxisLeft().setValueFormatter(model);
        chart.setRenderer(new Renderer(chart));
    }

    void updateChart(boolean isSmall, int index) {
        dataSets[0].setValues(model.entryRefs.get(index).get(0));
        for (int i = 1; i < 5; ++i) {
            updateData(i, isSmall,
                       model.entryRefs.get(index).get(i), i - 1, model.legendLabels[i - 1]);
        }
        update(isSmall, model.maxes[index]);
    }
}
