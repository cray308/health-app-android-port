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
            private final LineDataSet boundaryDataSet;

            private Formatter(LineDataSet boundaryDataSet) {
                this.boundaryDataSet = boundaryDataSet;
            }

            public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                return 0;
            }
        }

        private Renderer(LineChart view) {
            super(view, view.getAnimator(), view.getViewPortHandler());
        }

        protected void drawLinearFill(Canvas c, ILineDataSet dataSet,
                                      Transformer trans, XBounds bounds) {
            Path filled = mGenerateFilledPathBuffer;
            int startIndex = bounds.min, endIndex = bounds.range + bounds.min, indexInterval = 128;
            int currStart, currEnd, i = 0;

            do {
                currStart = startIndex + (i * indexInterval);
                currEnd = currStart + indexInterval;
                currEnd = Math.min(currEnd, endIndex);

                if (currStart <= currEnd) {
                    createFilledPath(dataSet, currStart, currEnd, filled);
                    trans.pathValueToPixel(filled);
                    Drawable drawable = dataSet.getFillDrawable();
                    if (drawable != null) {
                        drawFilledPath(c, filled, drawable);
                    } else {
                        drawFilledPath(c, filled, dataSet.getFillColor(), dataSet.getFillAlpha());
                    }
                }
                ++i;
            } while (currStart <= currEnd);
        }

        private void createFilledPath(ILineDataSet dataSet, int start, int end, Path filled) {
            Renderer.Formatter formatter = (Renderer.Formatter) dataSet.getFillFormatter();
            List<Entry> boundaryEntries = formatter.boundaryDataSet.getValues();
            float phaseY = mAnimator.getPhaseY();
            filled.reset();

            Entry entry = dataSet.getEntryForIndex(start);
            filled.moveTo(entry.getX(), boundaryEntries.get(0).getY());
            filled.lineTo(entry.getX(), entry.getY() * phaseY);

            for (int x = start + 1; x <= end; ++x) {
                Entry curr = dataSet.getEntryForIndex(x);
                filled.lineTo(curr.getX(), curr.getY() * phaseY);
            }

            for (int x = end; x > start; --x) {
                Entry prev = boundaryEntries.get(x);
                filled.lineTo(prev.getX(), prev.getY() * phaseY);
            }
            filled.close();
        }
    }

    private HistoryViewModel.WorkoutTypeChartViewModel viewModel;

    public WorkoutTypeChart(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.workout_type_chart, new int[]{
          R.id.secondEntry, R.id.thirdEntry, R.id.fourthEntry
        });
    }

    void setup(HistoryViewModel.WorkoutTypeChartViewModel model,
               IndexAxisValueFormatter xAxisFormatter) {
        viewModel = model;

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
        data.setValueFormatter(viewModel);
        setupChartView(xAxisFormatter);
        chartView.getAxisLeft().setValueFormatter(viewModel);
        chartView.setRenderer(new Renderer(chartView));
    }

    void updateChart(boolean isSmall, int index) {
        dataSets[0].setValues(viewModel.entryRefs.get(index).get(0));
        for (int i = 1; i < 5; ++i) {
            updateData(i, isSmall,
                       viewModel.entryRefs.get(index).get(i), i - 1, viewModel.legendLabels[i - 1]);
        }
        update(isSmall, viewModel.maxes[index]);
    }
}
