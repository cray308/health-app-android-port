package com.example.healthappandroid.historytab.data;

import android.os.AsyncTask;

import com.example.healthappandroid.common.helpers.DateHelper;
import com.example.healthappandroid.common.shareddata.AppCoordinator;
import com.example.healthappandroid.common.shareddata.AppUserData;
import com.example.healthappandroid.common.shareddata.PersistenceService;
import com.example.healthappandroid.common.shareddata.WeeklyData;

public class HistoryFetchTask extends AsyncTask<HistoryViewModel.WeekDataModel, Void, Void> {
    @Override
    protected Void doInBackground(HistoryViewModel.WeekDataModel... args) {
        HistoryViewModel.WeekDataModel model = args[0];
        model.size = 0;
        WeeklyData[] data = PersistenceService.shared.dao().getDataInInterval(
                DateHelper.twoYearsAgo(), AppUserData.shared.weekStart);
        model.size = data.length;
        for (int i = 0; i < model.size; ++i)
            model.arr[i] = new HistoryViewModel.WeekDataModel.WeekModel(data[i]);
        return null;
    }

    @Override
    protected void onPostExecute(Void unused) {
        super.onPostExecute(unused);
        AppCoordinator.shared.historyCoordinator.finishedLoadingHistoryData();
    }
}
