package com.example.healthAppAndroid.historyTab;

import androidx.fragment.app.Fragment;

import com.example.healthAppAndroid.common.shareddata.PersistenceService;
import com.example.healthAppAndroid.historyTab.data.HistoryViewModel;
import com.example.healthAppAndroid.historyTab.view.HistoryFragment;

public final class HistoryTabCoordinator {
    private static final class FetchHandler implements PersistenceService.Block {
        private final HistoryTabCoordinator coordinator;
        private final HistoryViewModel.WeekDataModel data;

        private FetchHandler(HistoryTabCoordinator coordinator,
                             HistoryViewModel.WeekDataModel data) {
            this.coordinator = coordinator;
            this.data = data;
        }

        public void completion() {
            coordinator.viewModel.populateData(data);
            coordinator.fragment.refresh();
        }
    }

    private final HistoryFragment fragment;
    private final HistoryViewModel viewModel;

    public HistoryTabCoordinator(Fragment fragment) {
        this.fragment = (HistoryFragment) fragment;
        viewModel = this.fragment.viewModel;
    }

    public void fetchData() {
        HistoryViewModel.WeekDataModel model = new HistoryViewModel.WeekDataModel();
        PersistenceService.fetchHistoryData(model, new FetchHandler(this, model));
    }

    public void handleDataDeletion() {
        if (viewModel.nEntries[2] != 0) {
            viewModel.clearData();
            fragment.refresh();
        }
    }
}
