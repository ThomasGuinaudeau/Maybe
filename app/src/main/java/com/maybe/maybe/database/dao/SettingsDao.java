package com.maybe.maybe.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.maybe.maybe.database.entity.Settings;

import java.util.List;

@Dao
public interface SettingsDao {

    @Query("SELECT * FROM settings")
    List<Settings> selectAll();

    @Insert
    void insert(Settings settings);

    @Query("DELETE FROM settings")
    void deleteAll();

}
