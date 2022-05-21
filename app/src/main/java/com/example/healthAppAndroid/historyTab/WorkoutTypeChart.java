package com.example.healthAppAndroid.historyTab;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Path;
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

public final class WorkoutTypeChart extends ChartContainer implements HistoryFragment.HistoryChart {
    private static final class ValueFormatter extends IndexAxisValueFormatter {
        private final Resources res;

        private ValueFormatter(Resources res) { this.res = res; }

        public String getFormattedValue(float value) {
            int minutes = (int)value;
            if (minutes == 0) {
                return "";
            } else if (minutes < 60) {
                return res.getString(R.string.minFmt, minutes);
            } else {
                return res.getString(R.string.hourMinFmt, minutes / 60, minutes % 60);
            }
        }
    }

    private static final class Renderer extends LineChartRenderer {
        private static final class Formatter implements IFillFormatter {
            private final LineDataSet set;

            private Formatter(LineDataSet boundaryDataSet) { set = boundaryDataSet; }

            public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                return 0;
            }
        }

        private Renderer(LineChart view) {
            super(view, view.getAnimator(), view.getViewPortHandler());
        }

        protected void drawLinearFill(Canvas c,
                                      ILineDataSet dataSet, Transformer trans, XBounds bounds) {
            Path filled = mGenerateFilledPathBuffer;
            int startIndex = bounds.min, endIndex = bounds.range + bounds.min;
            int color = dataSet.getFillColor(), currStart, currEnd, idx = 0;

            do {
                currStart = startIndex + (idx << 7);
                currEnd = currStart + 128;
                currEnd = Math.min(currEnd, endIndex);

                if (currStart <= currEnd) {
                    createFilledPath(dataSet, currStart, currEnd, filled);
                    trans.pathValueToPixel(filled);
                    drawFilledPath(c, filled, color, FillAlpha);
                }
                ++idx;
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

    private HistoryModel.WorkoutTypeModel model;

    public WorkoutTypeChart(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.workout_type_chart, new int[]{
          R.id.secondEntry, R.id.thirdEntry, R.id.fourthEntry
        });
    }

    public void setup(HistoryModel historyModel, int[] chartColors, int labelColor,
                      String defaultText, boolean ltr) {
        model = historyModel.types;
        sets[0] = createEmptyDataSet();
        for (int i = 1; i < 5; ++i) {
            sets[i] = createDataSet(chartColors[i - 1], labelColor, ltr);
            sets[i].setFillColor(chartColors[i - 1]);
            sets[i].setDrawFilled(true);
            sets[i].setFillAlpha(FillAlpha);
            sets[i].setFillFormatter(new Renderer.Formatter(sets[i - 1]));
        }
        setupChartData(new LineDataSet[]{sets[4], sets[3], sets[2], sets[1]});
        ValueFormatter formatter = new ValueFormatter(getResources());
        data.setValueFormatter(formatter);
        setupChartView(historyModel, labelColor, defaultText, ltr);
        yAxis.setValueFormatter(formatter);
        chart.setRenderer(new Renderer(chart));
    }

    public void updateChart(boolean isSmall, int index) {
        sets[0].setValues(model.refs.get(index).get(0));
        for (int i = 1; i < 5; ++i) {
            updateData(i, isSmall, model.refs.get(index).get(i), i - 1, model.legendLabels[i - 1]);
        }
        update(isSmall, model.maxes[index]);
    }
}
