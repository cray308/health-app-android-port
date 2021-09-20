package com.example.healthAppAndroid.historyTab;

import androidx.fragment.app.Fragment;

import com.example.healthAppAndroid.historyTab.data.HistoryFetchThread;
import com.example.healthAppAndroid.historyTab.data.HistoryViewModel;
import com.example.healthAppAndroid.historyTab.view.HistoryFragment;

public class HistoryTabCoordinator {
    private final HistoryFragment fragment;
    private final HistoryViewModel viewModel;

    public HistoryTabCoordinator(Fragment fragment) {
        this.fragment = (HistoryFragment) fragment;
        viewModel = this.fragment.viewModel;
    }

    public void fetchData() {
        new HistoryFetchThread(viewModel.data).start();
    }

    public void finishedLoadingHistoryData() {
        fragment.performForegroundUpdate();
    }
}
