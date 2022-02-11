package com.example.healthAppAndroid.core;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.Update;

import com.example.healthAppAndroid.BuildConfig;
import com.example.healthAppAndroid.historyTab.WeekDataModel;
import com.example.healthAppAndroid.homeTab.addWorkout.LiftType;
import com.example.healthAppAndroid.homeTab.addWorkout.WorkoutType;

import java.time.ZoneId;

@SuppressWarnings("AbstractClassWithOnlyOneDirectInheritor")
@Database(entities = {PersistenceService.WeeklyData.class}, version = 1, exportSchema = false)
public abstract class PersistenceService extends RoomDatabase {
    @SuppressWarnings("InterfaceWithOnlyOneDirectInheritor")
    @Dao public interface DAO {
        @Query("SELECT * FROM weeks") WeeklyData[] getAll();

        @Query("SELECT * FROM weeks WHERE start == :date LIMIT 1")
        WeeklyData findCurrentWeek(long date);

        @Query("SELECT * FROM weeks WHERE start < :endDate AND start > :startDate ORDER BY start")
        WeeklyData[] getDataInIntervalSorted(long startDate, long endDate);

        @Query("SELECT * FROM weeks WHERE start < :endDate AND start > :startDate")
        WeeklyData[] getDataInInterval(long startDate, long endDate);

        @Insert void insertWeeks(WeeklyData[] data);

        @Update void updateWeeks(WeeklyData[] weeks);

        @Delete void delete(WeeklyData[] weeks);
    }

    @Entity(tableName = "weeks") public static class WeeklyData {
        @PrimaryKey(autoGenerate = true) int uid;

        @ColumnInfo(name = "start") public long start;

        @ColumnInfo(name = "best_bench") public int bestBench;

        @ColumnInfo(name = "best_deadlift") public int bestDeadlift;

        @ColumnInfo(name = "best_pullup") public int bestPullup;

        @ColumnInfo(name = "best_squat") public int bestSquat;

        @ColumnInfo(name = "time_endurance") public int timeEndurance;

        @ColumnInfo(name = "time_hic") public int timeHIC;

        @ColumnInfo(name = "time_se") public int timeSE;

        @ColumnInfo(name = "time_strength") public int timeStrength;

        @ColumnInfo(name = "total_workouts") public int totalWorkouts;

        private void copyLiftMaxes(WeeklyData other) {
            bestBench = other.bestBench;
            bestDeadlift = other.bestDeadlift;
            bestPullup = other.bestPullup;
            bestSquat = other.bestSquat;
        }
    }

    public interface Block {
        void completion();
    }

    private static final String DBName = "HealthApp-db";
    public abstract DAO dao();
    private static PersistenceService shared;

    public static void start(long tzDifference, Block block) {
        shared.performStartupUpdate(tzDifference);
        block.completion();
    }

    static void create(Context context) {
        if (BuildConfig.DEBUG) {
            shared = Room.databaseBuilder(
                context, PersistenceService.class, DBName).createFromAsset("test.db").build();
        } else {
            init(context);
        }
    }

    public static void init(Context context) {
        shared = Room.databaseBuilder(context, PersistenceService.class, DBName).build();
    }

    private static long twoYearsAgo() { return AppUserData.shared.weekStart - 63244800; }

    private static void deleteEntries(DAO dao, WeeklyData[] data) {
        if (data.length != 0)
            dao.delete(data);
    }

    private static void saveChanges(DAO dao, WeeklyData[] data) {
        dao.updateWeeks(data);
    }

    private void performStartupUpdate(long tzOffset) {
        DAO dao = dao();
        WeeklyData[] data;
        int count;
        if (tzOffset != 0) {
            data = dao.getAll();
            count = data.length;
            if (count > 0) {
                for (int i = 0; i < count; ++i)
                    data[i].start += tzOffset;
                saveChanges(dao, data);
            }
        }

        data = dao.getDataInInterval(0, twoYearsAgo());
        deleteEntries(dao, data);

        data = dao.getDataInIntervalSorted(0, AppUserData.shared.weekStart);
        count = data.length;
        WeeklyData currWeek = getCurrentWeek(dao);
        boolean newEntryForCurrentWeek = false;
        if (currWeek == null) {
            newEntryForCurrentWeek = true;
            currWeek = new WeeklyData();
            currWeek.start = AppUserData.shared.weekStart;
        }

        if (count == 0) {
            if (newEntryForCurrentWeek)
                dao.insertWeeks(new WeeklyData[]{currWeek});
            return;
        }

        WeeklyData[] newWeeks = new WeeklyData[128];
        WeeklyData last = data[count - 1];
        int newEntryCount = 0;
        for (long currStart = last.start + AppUserData.weekSeconds;
             currStart < AppUserData.shared.weekStart;
             currStart += AppUserData.weekSeconds) {
            WeeklyData thisWeek = new WeeklyData();
            thisWeek.start = currStart;
            thisWeek.copyLiftMaxes(last);
            newWeeks[newEntryCount++] = thisWeek;
        }

        int savedSize = newEntryForCurrentWeek ? newEntryCount + 1 : newEntryCount;
        WeeklyData[] dataToSave = new WeeklyData[savedSize];
        System.arraycopy(newWeeks, 0, dataToSave, 0, newEntryCount);

        if (newEntryForCurrentWeek) {
            currWeek.copyLiftMaxes(last);
            dataToSave[newEntryCount] = currWeek;
        }

        dao.insertWeeks(dataToSave);
    }

    private static WeeklyData getCurrentWeek(DAO dao) {
        return dao.findCurrentWeek(AppUserData.shared.weekStart);
    }

    private static final class DeleteDataTask implements Runnable {
        public void run() {
            PersistenceService service = shared;
            DAO dao = service.dao();
            WeeklyData[] data = dao.getDataInInterval(0, AppUserData.shared.weekStart);
            deleteEntries(dao, data);
        }
    }

    static void deleteAppData() { new Thread(new DeleteDataTask()).start(); }

    private static final class UpdateCurrentWeekTask implements Runnable {
        private final byte type;
        private final short duration;
        private final short[] lifts;

        private UpdateCurrentWeekTask(byte type, short duration, short[] lifts) {
            this.type = type;
            this.duration = duration;
            this.lifts = lifts;
        }

        public void run() {
            PersistenceService service = shared;
            DAO dao = service.dao();
            WeeklyData curr = getCurrentWeek(dao);
            curr.totalWorkouts += 1;
            switch (type) {
                case WorkoutType.SE:
                    curr.timeSE += duration;
                    break;
                case WorkoutType.HIC:
                    curr.timeHIC += duration;
                    break;
                case WorkoutType.strength:
                    curr.timeStrength += duration;
                    break;
                default:
                    curr.timeEndurance += duration;
            }

            if (lifts != null) {
                curr.bestSquat = lifts[LiftType.squat];
                curr.bestPullup = lifts[LiftType.pullUp];
                curr.bestBench = lifts[LiftType.bench];
                curr.bestDeadlift = lifts[LiftType.deadlift];
            }

            saveChanges(dao, new WeeklyData[]{curr});
        }
    }

    public static void updateCurrentWeek(byte type, short duration, short[] lifts) {
        new Thread(new UpdateCurrentWeekTask(type, duration, lifts)).start();
    }

    private static final class HistoryFetchTask implements Runnable {
        private final WeekDataModel model;
        private final Block block;

        private HistoryFetchTask(WeekDataModel model, Block block) {
            this.model = model;
            this.block = block;
        }

        public void run() {
            ZoneId zoneId = ZoneId.systemDefault();
            PersistenceService service = shared;
            WeeklyData[] data = service.dao().getDataInInterval(twoYearsAgo(),
                                                                AppUserData.shared.weekStart);
            model.size = (short) data.length;
            for (short i = 0; i < model.size; ++i) {
                model.arr[i] = new WeekDataModel.Week(data[i], zoneId);
            }
            new Handler(Looper.getMainLooper()).post(block::completion);
        }
    }

    public static void fetchHistoryData(WeekDataModel model, Block block) {
        new Thread(new HistoryFetchTask(model, block)).start();
    }
}
