package com.example.healthAppAndroid.historyTab.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.helpers.ViewHelper;
import com.example.healthAppAndroid.common.shareddata.AppColors;
import com.example.healthAppAndroid.historyTab.data.HistoryViewModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.renderer.LineChartRenderer;
import com.github.mikephil.charting.utils.Transformer;

import java.util.Arrays;
import java.util.List;

public class WorkoutTypeChart extends ChartContainer {
    private static class Renderer extends LineChartRenderer {
        public static class Formatter implements IFillFormatter {
            private final LineDataSet boundaryDataSet;

            private Formatter(LineDataSet boundaryDataSet) {
                this.boundaryDataSet = boundaryDataSet;
            }

            public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                return 0;
            }
        }

        public Renderer(LineChart view) {
            super(view, view.getAnimator(), view.getViewPortHandler());
        }

        @Override protected void drawLinearFill(Canvas c, ILineDataSet dataSet,
                                                Transformer trans, XBounds bounds) {
            Path filled = mGenerateFilledPathBuffer;
            int startIndex = bounds.min, endIndex = bounds.range + bounds.min, indexInterval = 128;
            int currStart, currEnd, i = 0;

            do {
                currStart = startIndex + (i * indexInterval);
                currEnd = currStart + indexInterval;
                currEnd = Math.min(currEnd, endIndex);

                if (currStart <= currEnd) {
                    generateFilledPath(dataSet, currStart, currEnd, filled);
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

        private void generateFilledPath(ILineDataSet dataSet, int start, int end, Path filled) {
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

    private static class Formatter extends IndexAxisValueFormatter {
        @Override public String getFormattedValue(float value) {
            int minutes = (int) value;
            if (minutes == 0) {
                return "";
            } else if (minutes < 60) {
                return ViewHelper.format("%dm", minutes);
            } else {
                return ViewHelper.format("%dh %dm", minutes / 60, minutes % 60);
            }
        }
    }

    HistoryViewModel.WorkoutTypeChartViewModel viewModel;

    public WorkoutTypeChart(Context context) { super(context); }

    public WorkoutTypeChart(Context context, AttributeSet attrs) { super(context, attrs); }

    void setup() {
        inflate(getContext(), R.layout.workout_type_chart, this);
        init();
        legendEntries[1] = findViewById(R.id.secondEntry);
        legendEntries[2] = findViewById(R.id.thirdEntry);
        legendEntries[3] = findViewById(R.id.fourthEntry);
    }

    void setup(HistoryViewModel.WorkoutTypeChartViewModel viewModel,
               IndexAxisValueFormatter xAxisFormatter) {
        this.viewModel = viewModel;
        Formatter formatter = new Formatter();

        dataSets[0] = createEmptyDataSet();
        for (int i = 1; i < 5; ++i) {
            dataSets[i] = createDataSet(AppColors.chartColors[i - 1]);
            dataSets[i].setFillColor(AppColors.chartColors[i - 1]);
            dataSets[i].setDrawFilled(true);
            dataSets[i].setFillAlpha(191);
            dataSets[i].setFillFormatter(new Renderer.Formatter(dataSets[i - 1]));
        }
        setupChartData(new LineDataSet[]{dataSets[4], dataSets[3], dataSets[2], dataSets[1]}, 4);
        data.setValueFormatter(formatter);
        setupChartView(xAxisFormatter);
        chartView.getAxisLeft().setValueFormatter(formatter);
        chartView.setRenderer(new Renderer(chartView));
    }

    void update(int count, boolean isSmall) {
        dataSets[0].setValues(Arrays.asList(viewModel.entries[0]));
        for (int i = 1; i < 5; ++i)
            updateData(i, isSmall, viewModel.entries[i], i - 1, viewModel.legendLabels[i - 1]);
        update(isSmall, count, viewModel.yMax);
    }
}
