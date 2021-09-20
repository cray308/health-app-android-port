package com.example.healthAppAndroid.common.shareddata;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "weeks")
public class WeeklyData {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "start")
    public long start;

    @ColumnInfo(name = "best_bench")
    public int bestBench;

    @ColumnInfo(name = "best_deadlift")
    public int bestDeadlift;

    @ColumnInfo(name = "best_pullup")
    public int bestPullup;

    @ColumnInfo(name = "best_squat")
    public int bestSquat;

    @ColumnInfo(name = "time_endurance")
    public int timeEndurance;

    @ColumnInfo(name = "time_hic")
    public int timeHIC;

    @ColumnInfo(name = "time_se")
    public int timeSE;

    @ColumnInfo(name = "time_strength")
    public int timeStrength;

    @ColumnInfo(name = "total_workouts")
    public int totalWorkouts;

    public void copyLiftMaxes(WeeklyData other) {
        bestBench = other.bestBench;
        bestDeadlift = other.bestDeadlift;
        bestPullup = other.bestPullup;
        bestSquat = other.bestSquat;
    }
}
