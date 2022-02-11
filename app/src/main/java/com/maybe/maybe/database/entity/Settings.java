package com.maybe.maybe.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "settings")
public class Settings {
    @PrimaryKey(autoGenerate = true)
    private long settings_id;
    private String settings_current_name;
    private String settings_sort_order;
    private long settings_current_music_id;
    private long settings_timestamp;
    private String settings_whichPAA;

    public Settings(String settings_current_name, String settings_sort_order, long settings_current_music_id, long settings_timestamp, String settings_whichPAA) {
        this.settings_current_name = settings_current_name;
        this.settings_sort_order = settings_sort_order;
        this.settings_current_music_id = settings_current_music_id;
        this.settings_timestamp = settings_timestamp;
        this.settings_whichPAA = settings_whichPAA;
    }

    public long getSettings_id() {
        return settings_id;
    }

    public void setSettings_id(long settings_id) {
        this.settings_id = settings_id;
    }

    public String getSettings_current_name() {
        return settings_current_name;
    }

    public void setSettings_current_name(String settings_current_name) {
        this.settings_current_name = settings_current_name;
    }

    public String getSettings_sort_order() {
        return settings_sort_order;
    }

    public void setSettings_sort_order(String settings_sort_order) {
        this.settings_sort_order = settings_sort_order;
    }

    public long getSettings_current_music_id() {
        return settings_current_music_id;
    }

    public void setSettings_current_music_id(long settings_current_music_id) {
        this.settings_current_music_id = settings_current_music_id;
    }

    public long getSettings_timestamp() {
        return settings_timestamp;
    }

    public void setSettings_timestamp(long settings_timestamp) {
        this.settings_timestamp = settings_timestamp;
    }

    public String getSettings_whichPAA() {
        return settings_whichPAA;
    }

    public void setSettings_whichPAA(String settings_whichPAA) {
        this.settings_whichPAA = settings_whichPAA;
    }
}