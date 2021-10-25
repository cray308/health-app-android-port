package com.example.healthAppAndroid.historyTab;

import androidx.fragment.app.Fragment;

import com.example.healthAppAndroid.common.shareddata.PersistenceService;
import com.example.healthAppAndroid.historyTab.data.HistoryViewModel;
import com.example.healthAppAndroid.historyTab.view.HistoryFragment;

public final class HistoryTabCoordinator {
    private static final class FetchHandler implements PersistenceService.Block {
        private final HistoryTabCoordinator coordinator;

        private FetchHandler(HistoryTabCoordinator coordinator) { this.coordinator = coordinator; }

        public void completion() {
            coordinator.finishedLoadingHistoryData();
        }
    }

    private final HistoryFragment fragment;
    private final HistoryViewModel viewModel;

    public HistoryTabCoordinator(Fragment fragment) {
        this.fragment = (HistoryFragment) fragment;
        viewModel = this.fragment.viewModel;
    }

    public void fetchData() {
        PersistenceService.fetchHistoryData(viewModel.data, new FetchHandler(this));
    }

    public void handleDataDeletion() {
        viewModel.data.size = 0;
        finishedLoadingHistoryData();
    }

    private void finishedLoadingHistoryData() { fragment.performForegroundUpdate(); }
}
