package com.example.healthAppAndroid.settingsTab;

import android.os.Handler;
import android.os.Looper;

import com.example.healthAppAndroid.common.shareddata.AppCoordinator;
import com.example.healthAppAndroid.common.shareddata.AppUserData;
import com.example.healthAppAndroid.common.shareddata.PersistenceService;
import com.example.healthAppAndroid.common.shareddata.WeeklyData;
import com.example.healthAppAndroid.common.shareddata.WeeklyDataDao;

public class ClearDataThread extends Thread {
    public ClearDataThread() {}

    @Override
    public void run() {
        WeeklyDataDao dao = PersistenceService.shared.dao();
        WeeklyData[] data = dao.getDataInInterval(0, AppUserData.shared.weekStart);
        PersistenceService.shared.deleteEntries(dao, data);
        new Handler(Looper.getMainLooper()).post(() -> {
            AppUserData.shared.deleteSavedData();
            AppCoordinator.shared.deletedAppData();
        });
    }
}
