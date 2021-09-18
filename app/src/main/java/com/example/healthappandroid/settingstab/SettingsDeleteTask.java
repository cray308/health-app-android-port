package com.example.healthappandroid.settingstab;

import android.os.AsyncTask;

import com.example.healthappandroid.common.shareddata.AppCoordinator;
import com.example.healthappandroid.common.shareddata.AppUserData;
import com.example.healthappandroid.common.shareddata.PersistenceService;
import com.example.healthappandroid.common.shareddata.WeeklyData;

public class SettingsDeleteTask extends AsyncTask<Void, Void, Void> {
    @Override
    protected Void doInBackground(Void... voids) {
        WeeklyData[] data = PersistenceService.shared.dao().getDataInInterval(0, AppUserData.shared.weekStart);
        PersistenceService.shared.deleteEntries(data);
        return null;
    }

    @Override
    protected void onPostExecute(Void unused) {
        super.onPostExecute(unused);
        AppUserData.shared.deleteSavedData();
        AppCoordinator.shared.deletedAppData();
    }
}
