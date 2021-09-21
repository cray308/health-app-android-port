package com.example.healthAppAndroid.historyTab;

import android.os.Handler;
import android.os.Looper;

import androidx.fragment.app.Fragment;

import com.example.healthAppAndroid.common.helpers.DateHelper;
import com.example.healthAppAndroid.common.shareddata.AppCoordinator;
import com.example.healthAppAndroid.common.shareddata.AppUserData;
import com.example.healthAppAndroid.common.shareddata.PersistenceService;
import com.example.healthAppAndroid.common.shareddata.WeeklyData;
import com.example.healthAppAndroid.historyTab.data.HistoryViewModel;
import com.example.healthAppAndroid.historyTab.view.HistoryFragment;

public class HistoryTabCoordinator {
    private static class HistoryFetchThread extends Thread {
        private final HistoryViewModel.WeekDataModel model;
        private HistoryFetchThread(HistoryViewModel.WeekDataModel model) { this.model = model; }

        @Override
        public void run() {
            model.size = 0;
            WeeklyData[] data = PersistenceService.shared.dao().getDataInInterval(
                DateHelper.twoYearsAgo(), AppUserData.shared.weekStart);
            model.size = data.length;
            for (int i = 0; i < model.size; ++i)
                model.arr[i] = new HistoryViewModel.WeekDataModel.WeekModel(data[i]);
            new Handler(Looper.getMainLooper()).post(
                () -> AppCoordinator.shared.historyCoordinator.finishedLoadingHistoryData());
        }
    }

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
