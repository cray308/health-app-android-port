package com.example.healthAppAndroid.historyTab.view;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.drawable.Drawable;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.renderer.LineChartRenderer;
import com.github.mikephil.charting.utils.Transformer;

import java.util.List;

public class AreaChartRenderer extends LineChartRenderer {
    public static class Formatter implements IFillFormatter {
        private final LineDataSet boundaryDataSet;

        public Formatter(LineDataSet boundaryDataSet) { this.boundaryDataSet = boundaryDataSet; }

        public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
            return 0;
        }
    }

    public AreaChartRenderer(LineChart view) {
        super(view, view.getAnimator(), view.getViewPortHandler());
    }

    @Override
    protected void drawLinearFill(Canvas c,
                                  ILineDataSet dataSet, Transformer trans, XBounds bounds) {
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
        List<Entry> boundaryEntries = ((Formatter) dataSet.getFillFormatter()).boundaryDataSet.getValues();
        Entry curr, prev;
        float phaseY = mAnimator.getPhaseY();
        filled.reset();

        Entry entry = dataSet.getEntryForIndex(start);
        filled.moveTo(entry.getX(), boundaryEntries.get(0).getY());
        filled.lineTo(entry.getX(), entry.getY() * phaseY);

        for (int x = start + 1; x <= end; ++x) {
            curr = dataSet.getEntryForIndex(x);
            filled.lineTo(curr.getX(), curr.getY() * phaseY);
        }

        for (int x = end; x > start; --x) {
            prev = boundaryEntries.get(x);
            filled.lineTo(prev.getX(), prev.getY() * phaseY);
        }
        filled.close();
    }
}
