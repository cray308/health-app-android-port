package com.example.healthAppAndroid.core;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.Update;

import com.example.healthAppAndroid.BuildConfig;
import com.example.healthAppAndroid.historyTab.HistoryFragment;
import com.example.healthAppAndroid.homeTab.addWorkout.Workout;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

@Database(entities = {PersistenceManager.WeeklyData.class}, version = 1, exportSchema = false)
public abstract class PersistenceManager extends RoomDatabase {
    @Dao interface DAO {
        @Query("SELECT * FROM weeks ORDER BY start DESC LIMIT 1") WeeklyData lastWeek();

        @Query("SELECT * FROM weeks ORDER BY start") WeeklyData[] all();

        @Insert void insert(WeeklyData data);

        @Query("UPDATE weeks SET start = start + :tzDiff") void updateTimeZone(int tzDiff);

        @Query("DELETE FROM weeks WHERE start < :endDate") void deleteOldEntries(long endDate);

        @Query("DELETE FROM weeks WHERE uid < :lastRow") void clearHistory(int lastRow);

        @Update void update(WeeklyData[] data);
    }

    @Entity(tableName = "weeks") public static final class WeeklyData {
        @Ignore public int[] weights;
        @Ignore public int[] durationByType;
        @Ignore public int[] cumulativeDuration;

        @PrimaryKey(autoGenerate = true) int uid;

        @ColumnInfo(name = "start") long weekStart;

        @ColumnInfo(name = "best_bench") int bestBench;

        @ColumnInfo(name = "best_deadlift") int bestDeadLift;

        @ColumnInfo(name = "best_pullup") int bestPullUp;

        @ColumnInfo(name = "best_squat") int bestSquat;

        @ColumnInfo(name = "time_endurance") int timeEndurance;

        @ColumnInfo(name = "time_hic") int timeHIC;

        @ColumnInfo(name = "time_se") int timeSE;

        @ColumnInfo(name = "time_strength") int timeStrength;

        @ColumnInfo(name = "total_workouts") public int totalWorkouts;

        WeeklyData() {}

        private WeeklyData(long weekStart, WeeklyData other) {
            this.weekStart = weekStart;
            if (other != null) {
                bestBench = other.bestBench;
                bestDeadLift = other.bestDeadLift;
                bestPullUp = other.bestPullUp;
                bestSquat = other.bestSquat;
            }
        }
    }

    abstract DAO dao();
    private static final String DBName = "HealthApp-db";
    private static PersistenceManager shared;

    static void create(Context context) {
        if (BuildConfig.DEBUG) {
            shared = Room.databaseBuilder(context, PersistenceManager.class, DBName)
                         .createFromAsset("test.db").build();
            new Thread(() -> {
                DAO dao = shared.dao();
                WeeklyData[] data = dao.all();
                long date = Instant.now().getEpochSecond() - 126489600;
                date = new UserData.TimeData(date).weekStart;
                for (WeeklyData d : data) {
                    d.weekStart = date;
                    date += Macros.weekSeconds;
                }
                dao.update(data);
            }).start();
        } else {
            init(context);
        }
    }

    static void init(Context context) {
        if (shared == null)
            shared = Room.databaseBuilder(context, PersistenceManager.class, DBName).build();
    }

    static final class StartupTask implements Runnable {
        private final HistoryFragment.Block block;
        private final long weekStart;
        private final int tzDiff;

        StartupTask(HistoryFragment.Block block, long weekStart, int tzDiff) {
            this.block = block;
            this.weekStart = weekStart;
            this.tzDiff = tzDiff;
        }

        public void run() {
            DAO dao = shared.dao();
            WeeklyData lastWeek = dao.lastWeek();

            if (lastWeek == null) {
                dao.insert(new WeeklyData(weekStart, null));
                new Handler(Looper.getMainLooper()).post(() -> block.completion(null, null));
                return;
            }

            long start = lastWeek.weekStart;

            if (tzDiff != 0) {
                dao.updateTimeZone(tzDiff);
                start += tzDiff;
            }

            dao.deleteOldEntries(weekStart - (Macros.weekSeconds * 104) - (Macros.daySeconds << 2));

            boolean addForThisWeek = start != weekStart;
            long lastDate = weekStart - Macros.hourSeconds;

            for (start = start + Macros.weekSeconds; start < lastDate; start += Macros.weekSeconds) {
                dao.insert(new WeeklyData(start, lastWeek));
            }

            if (addForThisWeek) dao.insert(new WeeklyData(weekStart, lastWeek));

            fetchHistory(dao);
        }

        private void fetchHistory(DAO dao) {
            WeeklyData[] data = dao.all();
            int count = data.length;
            if (--count == 0) {
                new Handler(Looper.getMainLooper()).post(() -> block.completion(null, null));
                return;
            }

            WeeklyData[] weeks = new WeeklyData[count];
            System.arraycopy(data, 0, weeks, 0, count);
            ZoneId zoneId = ZoneId.systemDefault();
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
            String[] axisStrings = new String[count];
            int index = 0;

            for (WeeklyData w : weeks) {
                Instant instant = Instant.ofEpochSecond(w.weekStart);
                axisStrings[index++] = LocalDateTime.ofInstant(instant, zoneId).format(formatter);

                w.weights = new int[]{w.bestSquat, w.bestPullUp, w.bestBench, w.bestDeadLift};
                w.durationByType = new int[]{w.timeStrength, w.timeSE, w.timeEndurance, w.timeHIC};
                w.cumulativeDuration = new int[]{w.durationByType[0], 0, 0, 0};
                for (int j = 1; j < 4; ++j) {
                    w.cumulativeDuration[j] = w.cumulativeDuration[j - 1] + w.durationByType[j];
                }
            }

            new Handler(Looper.getMainLooper()).post(() -> block.completion(weeks, axisStrings));
        }
    }

    static final class DeleteDataTask implements Runnable {
        public void run() {
            DAO dao = shared.dao();
            WeeklyData currWeek = dao.lastWeek();
            dao.clearHistory(currWeek.uid);
            currWeek.totalWorkouts = 0;
            currWeek.timeStrength = 0;
            currWeek.timeSE = 0;
            currWeek.timeEndurance = 0;
            currWeek.timeHIC = 0;
            dao.update(new WeeklyData[]{currWeek});
        }
    }

    static final class WorkoutDataTask implements Runnable {
        private final Workout.Output data;

        WorkoutDataTask(Workout.Output data) { this.data = data; }

        public void run() {
            DAO dao = shared.dao();
            WeeklyData currWeek = dao.lastWeek();
            currWeek.totalWorkouts += 1;
            if (data.type == 0) {
                currWeek.timeStrength += data.duration;
            } else if (data.type == 1) {
                currWeek.timeSE += data.duration;
            } else if (data.type == 2) {
                currWeek.timeEndurance += data.duration;
            } else {
                currWeek.timeHIC += data.duration;
            }

            if (data.weights[0] != -1) {
                currWeek.bestSquat = data.weights[0];
                currWeek.bestPullUp = data.weights[1];
                currWeek.bestBench = data.weights[2];
                currWeek.bestDeadLift = data.weights[3];
            }

            dao.update(new WeeklyData[]{currWeek});
        }
    }
}
