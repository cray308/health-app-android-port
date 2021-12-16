package com.example.healthAppAndroid.historyTab;

import android.os.Looper;

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
            if (data.size != 0)
                coordinator.viewModel.populateData(data);
            new android.os.Handler(Looper.getMainLooper()).post(coordinator.fragment::refresh);
        }
    }

    private static final class StartupHandler implements PersistenceService.Block {
        private final HistoryTabCoordinator coordinator;

        private StartupHandler(HistoryTabCoordinator coordinator) {
            this.coordinator = coordinator;
        }

        public void completion() {
            HistoryViewModel.WeekDataModel model = new HistoryViewModel.WeekDataModel();
            PersistenceService.fetchHistoryData(model, new FetchHandler(coordinator, model));
        }
    }

    private final HistoryFragment fragment;
    private final HistoryViewModel viewModel;

    public static HistoryTabCoordinator create(Object[] blockArray, Fragment fragment) {
        HistoryTabCoordinator coordinator = new HistoryTabCoordinator(fragment);
        blockArray[0] = new StartupHandler(coordinator);
        return coordinator;
    }

    private HistoryTabCoordinator(Fragment fragment) {
        this.fragment = (HistoryFragment) fragment;
        viewModel = this.fragment.viewModel;
    }

    public void handleDataDeletion() {
        if (viewModel.nEntries[2] != 0) {
            viewModel.clearData();
            fragment.refresh();
        }
    }
}
