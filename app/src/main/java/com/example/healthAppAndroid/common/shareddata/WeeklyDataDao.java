package com.example.healthAppAndroid.common.shareddata;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface WeeklyDataDao {
    @Query("SELECT * FROM weeks")
    WeeklyData[] getAll();

    @Query("SELECT * FROM weeks WHERE start == :date LIMIT 1")
    WeeklyData findCurrentWeek(long date);

    @Query("SELECT * FROM weeks WHERE start < :endDate AND start > :startDate ORDER BY start")
    WeeklyData[] getDataInIntervalSorted(long startDate, long endDate);

    @Query("SELECT * FROM weeks WHERE start < :endDate AND start > :startDate")
    WeeklyData[] getDataInInterval(long startDate, long endDate);

    @Insert
    void insertWeeks(WeeklyData[] data);

    @Update
    void updateWeeks(WeeklyData[] weeks);

    @Delete
    void delete(WeeklyData[] weeks);
}
