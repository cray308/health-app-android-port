package com.example.healthappandroid.historytab;

import com.example.healthappandroid.historytab.data.HistoryFetchTask;
import com.example.healthappandroid.historytab.data.HistoryViewModel;
import com.example.healthappandroid.historytab.view.HistoryFragment;

public class HistoryTabCoordinator {
    public final HistoryFragment fragment = new HistoryFragment();
    private final HistoryViewModel viewModel;

    public HistoryTabCoordinator() {
        viewModel = fragment.viewModel;
    }

    public void fetchData() {
        new HistoryFetchTask().execute(viewModel.data);
    }

    public void finishedLoadingHistoryData() {
        fragment.performForegroundUpdate();
    }
}
