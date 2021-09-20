package com.example.healthAppAndroid.historyTab.data;

import android.os.Handler;
import android.os.Looper;

import com.example.healthAppAndroid.common.helpers.DateHelper;
import com.example.healthAppAndroid.common.shareddata.AppCoordinator;
import com.example.healthAppAndroid.common.shareddata.AppUserData;
import com.example.healthAppAndroid.common.shareddata.PersistenceService;
import com.example.healthAppAndroid.common.shareddata.WeeklyData;

public class HistoryFetchThread extends Thread {
    private final HistoryViewModel.WeekDataModel model;

    public HistoryFetchThread(HistoryViewModel.WeekDataModel model) {
        this.model = model;
    }

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
