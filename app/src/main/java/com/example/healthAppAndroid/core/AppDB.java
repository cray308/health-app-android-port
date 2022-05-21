package com.example.healthAppAndroid.core;

import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.RoomDatabase;
import androidx.room.Update;

@Database(entities = {AppDB.WeeklyData.class}, version = 1, exportSchema = false)
public abstract class AppDB extends RoomDatabase {
    @Dao public interface DAO {
        @Query("SELECT * FROM weeks ORDER BY start") WeeklyData[] getAllSorted();

        @Query("SELECT * FROM weeks ORDER BY start DESC LIMIT 1") WeeklyData findCurrentWeek();

        @Insert void insertWeeks(WeeklyData[] data);

        @Update void updateWeeks(WeeklyData[] weeks);

        @Delete void delete(WeeklyData[] weeks);
    }

    @Entity(tableName = "weeks") static class WeeklyData {
        @PrimaryKey(autoGenerate = true) int uid;

        @ColumnInfo(name = "start") long start;

        @ColumnInfo(name = "best_bench") short bestBench;

        @ColumnInfo(name = "best_deadlift") short bestDeadlift;

        @ColumnInfo(name = "best_pullup") short bestPullup;

        @ColumnInfo(name = "best_squat") short bestSquat;

        @ColumnInfo(name = "time_endurance") short timeEndurance;

        @ColumnInfo(name = "time_hic") short timeHIC;

        @ColumnInfo(name = "time_se") short timeSE;

        @ColumnInfo(name = "time_strength") short timeStrength;

        @ColumnInfo(name = "total_workouts") short totalWorkouts;

        void copyLiftMaxes(WeeklyData other) {
            bestBench = other.bestBench;
            bestDeadlift = other.bestDeadlift;
            bestPullup = other.bestPullup;
            bestSquat = other.bestSquat;
        }
    }

    public abstract DAO dao();
}
