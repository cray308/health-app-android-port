package com.example.healthappandroid.settingstab;

import android.os.AsyncTask;

import com.example.healthappandroid.common.shareddata.AppCoordinator;
import com.example.healthappandroid.common.shareddata.AppUserData;
import com.example.healthappandroid.common.shareddata.PersistenceService;
import com.example.healthappandroid.common.shareddata.WeeklyData;
import com.example.healthappandroid.common.shareddata.WeeklyDataDao;

public class SettingsDeleteTask extends AsyncTask<Void, Void, Void> {
    @Override
    protected Void doInBackground(Void... voids) {
        WeeklyDataDao dao = PersistenceService.shared.dao();
        WeeklyData[] data = dao.getDataInInterval(0, AppUserData.shared.weekStart);
        PersistenceService.shared.deleteEntries(dao, data);
        return null;
    }

    @Override
    protected void onPostExecute(Void unused) {
        AppUserData.shared.deleteSavedData();
        AppCoordinator.shared.deletedAppData();
    }
}
