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
import com.example.healthAppAndroid.historyTab.HistoryFragment;
import com.example.healthAppAndroid.historyTab.WeekDataModel;
import com.example.healthAppAndroid.homeTab.addWorkout.WorkoutType;

import java.time.ZoneId;

@SuppressWarnings("AbstractClassWithOnlyOneDirectInheritor")
@Database(entities = {PersistenceService.WeeklyData.class}, version = 1, exportSchema = false)
public abstract class PersistenceService extends RoomDatabase {
    @SuppressWarnings("InterfaceWithOnlyOneDirectInheritor")
    @Dao public interface DAO {
        @Query("SELECT * FROM weeks ORDER BY start") WeeklyData[] getAllSorted();

        @Query("SELECT * FROM weeks ORDER BY start DESC LIMIT 1")
        WeeklyData findCurrentWeek();

        @Insert void insertWeeks(WeeklyData[] data);

        @Update void updateWeeks(WeeklyData[] weeks);

        @Delete void delete(WeeklyData[] weeks);
    }

    @Entity(tableName = "weeks") public static class WeeklyData {
        @PrimaryKey(autoGenerate = true) int uid;

        @ColumnInfo(name = "start") public long start;

        @ColumnInfo(name = "best_bench") public short bestBench;

        @ColumnInfo(name = "best_deadlift") public short bestDeadlift;

        @ColumnInfo(name = "best_pullup") public short bestPullup;

        @ColumnInfo(name = "best_squat") public short bestSquat;

        @ColumnInfo(name = "time_endurance") public short timeEndurance;

        @ColumnInfo(name = "time_hic") public short timeHIC;

        @ColumnInfo(name = "time_se") public short timeSE;

        @ColumnInfo(name = "time_strength") public short timeStrength;

        @ColumnInfo(name = "total_workouts") public short totalWorkouts;

        private void copyLiftMaxes(WeeklyData other) {
            bestBench = other.bestBench;
            bestDeadlift = other.bestDeadlift;
            bestPullup = other.bestPullup;
            bestSquat = other.bestSquat;
        }
    }

    private static final String DBName = "HealthApp-db";
    public abstract DAO dao();
    private static PersistenceService shared;

    static void create(Context context) {
        if (BuildConfig.DEBUG) {
            shared = Room.databaseBuilder(
                context, PersistenceService.class, DBName).createFromAsset("test.db").build();
        } else {
            init(context);
        }
    }

    static void init(Context context) {
        shared = Room.databaseBuilder(context, PersistenceService.class, DBName).build();
    }

    private static void fetchHistory(Object[] args, DAO dao) {
        WeekDataModel model = (WeekDataModel) args[0];
        HistoryFragment.FetchHandler block = (HistoryFragment.FetchHandler) args[1];
        ZoneId zoneId = ZoneId.systemDefault();
        WeeklyData[] data = dao.getAllSorted();
        int count = data.length;
        if (count > 1) {
            model.size = count - 1;
            for (int i = 0; i < model.size; ++i) {
                model.arr[i] = new WeekDataModel.Week(data[i], zoneId);
            }
        }
        new Handler(Looper.getMainLooper()).post(block::completion);
    }

    static void start(long weekStart, int tzOffset, Object[] args) {
        final long weekSeconds = 604800;
        long endPt = weekStart - 63244800;
        DAO dao = shared.dao();
        WeeklyData[] data = dao.getAllSorted();
        int count = data.length;

        if (count == 0) {
            WeeklyData first = new WeeklyData();
            first.start = weekStart;
            dao.insertWeeks(new WeeklyData[]{first});
            fetchHistory(args, dao);
            return;
        }

        if (tzOffset != 0) {
            for (WeeklyData datum : data) {
                datum.start += tzOffset;
            }
            dao.updateWeeks(data);
        }

        WeeklyData[] newEntries = new WeeklyData[128];
        WeeklyData[] oldEntries = new WeeklyData[128];
        int oldCount = 0, newCount = 0;
        WeeklyData last = data[count - 1];
        long start = last.start;
        if (start != weekStart) {
            WeeklyData currWeek = new WeeklyData();
            currWeek.start = weekStart;
            currWeek.copyLiftMaxes(last);
            newEntries[newCount++] = currWeek;
        }

        for (WeeklyData d : data) {
            if (d.start < endPt)
                oldEntries[oldCount++] = d;
        }

        for (start = last.start + weekSeconds; start < weekStart; start += weekSeconds) {
            WeeklyData curr = new WeeklyData();
            curr.start = start;
            curr.copyLiftMaxes(last);
            newEntries[newCount++] = curr;
        }

        if (oldCount != 0) {
            WeeklyData[] deleted = new WeeklyData[oldCount];
            System.arraycopy(oldEntries, 0, deleted, 0, oldCount);
            dao.delete(deleted);
        }
        if (newCount != 0) {
            WeeklyData[] inserted = new WeeklyData[newCount];
            System.arraycopy(newEntries, 0, inserted, 0, newCount);
            dao.insertWeeks(inserted);
        }
        fetchHistory(args, dao);
    }

    private static final class DeleteDataTask implements Runnable {
        public void run() {
            DAO dao = shared.dao();
            WeeklyData[] data = dao.getAllSorted();
            int count = data.length;
            if (count == 0) return;

            int end = count - 1;
            if (end != 0) {
                WeeklyData[] toDelete = new WeeklyData[end];
                System.arraycopy(data, 0, toDelete, 0, end);
                dao.delete(toDelete);
            }
            WeeklyData currWeek = data[end];
            currWeek.totalWorkouts = 0;
            currWeek.timeEndurance = 0;
            currWeek.timeHIC = 0;
            currWeek.timeSE = 0;
            currWeek.timeStrength = 0;
            dao.updateWeeks(new WeeklyData[]{currWeek});
        }
    }

    static void deleteAppData() { new Thread(new DeleteDataTask()).start(); }

    private static final class UpdateCurrentWeekTask implements Runnable {
        private final short duration;
        private final short[] lifts;
        private final byte type;

        private UpdateCurrentWeekTask(byte type, short duration, short[] lifts) {
            this.type = type;
            this.duration = duration;
            this.lifts = lifts;
        }

        public void run() {
            PersistenceService service = shared;
            DAO dao = service.dao();
            WeeklyData curr = dao.findCurrentWeek();
            curr.totalWorkouts += 1;
            if (type == WorkoutType.strength) {
                curr.timeStrength += duration;
            } else if (type == WorkoutType.SE) {
                curr.timeSE += duration;
            } else if (type == WorkoutType.endurance) {
                curr.timeEndurance += duration;
            } else {
                curr.timeHIC += duration;
            }

            if (lifts != null) {
                curr.bestSquat = lifts[0];
                curr.bestPullup = lifts[1];
                curr.bestBench = lifts[2];
                curr.bestDeadlift = lifts[3];
            }

            dao.updateWeeks(new WeeklyData[]{curr});
        }
    }

    public static void updateCurrentWeek(byte type, short duration, short[] lifts) {
        new Thread(new UpdateCurrentWeekTask(type, duration, lifts)).start();
    }
}
