package com.example.healthappandroid.common.shareddata;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.healthappandroid.common.helpers.DateHelper;

@Database(entities = {WeeklyData.class}, version = 1, exportSchema = false)
public abstract class PersistenceService extends RoomDatabase {
    private static final String DBName = "HealthApp-db";
    public abstract WeeklyDataDao dao();
    public static PersistenceService shared;

    public static void setup(long tzDifference) {
        if (tzDifference != 0)
            shared.changeTimestamps(tzDifference);
        shared.performStartupUpdate();
        AppCoordinator.shared.historyCoordinator.fetchData();
    }

    public static void createFromDB(Context context) {
        shared = Room.databaseBuilder(context, PersistenceService.class, DBName)
                .createFromAsset("test.db").build();
    }

    public static void create(Context context) {
        shared = Room.databaseBuilder(context, PersistenceService.class, DBName).build();
    }

    public void deleteEntries(WeeklyData[] data) {
        if (data.length != 0)
            dao().delete(data);
    }

    public void saveChanges(WeeklyData[] data) { dao().updateWeeks(data); }

    private void performStartupUpdate() {
        int count;
        WeeklyData[] data = dao().getDataInInterval(0, DateHelper.twoYearsAgo());
        deleteEntries(data);

        data = dao().getDataInIntervalSorted(0, AppUserData.shared.weekStart);
        count = data.length;
        WeeklyData currWeek = getCurrentWeek();
        boolean newEntryForCurrentWeek = false;
        if (currWeek == null) {
            newEntryForCurrentWeek = true;
            currWeek = new WeeklyData();
            currWeek.start = AppUserData.shared.weekStart;
        }

        if (count == 0) {
            if (newEntryForCurrentWeek)
                dao().insertWeeks(new WeeklyData[]{currWeek});
            return;
        }

        WeeklyData[] newWeeks = new WeeklyData[128];
        WeeklyData last = data[count - 1];
        int newEntryCount = 0;
        for (long currStart = last.start + DateHelper.weekSeconds; currStart < AppUserData.shared.weekStart; currStart += DateHelper.weekSeconds) {
            WeeklyData thisWeek = new WeeklyData();
            thisWeek.start = currStart;
            thisWeek.copyLiftMaxes(last);
            newWeeks[newEntryCount++] = thisWeek;
        }

        WeeklyData[] dataToSave = new WeeklyData[newEntryForCurrentWeek ? newEntryCount + 1 : newEntryCount];
        System.arraycopy(newWeeks, 0, dataToSave, 0, newEntryCount);

        if (newEntryForCurrentWeek) {
            currWeek.copyLiftMaxes(last);
            dataToSave[newEntryCount] = currWeek;
        }

        dao().insertWeeks(dataToSave);
    }

    private void changeTimestamps(long difference) {
        int count;
        WeeklyData[] data = dao().getAll();
        count = data.length;
        if (count == 0) return;
        for (int i = 0; i < count; ++i) {
            data[i].start += difference;
        }
        saveChanges(data);
    }

    public WeeklyData getCurrentWeek() {
        return dao().findCurrentWeek(AppUserData.shared.weekStart);
    }
}
