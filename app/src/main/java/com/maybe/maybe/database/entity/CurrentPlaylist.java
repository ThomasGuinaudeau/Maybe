package com.maybe.maybe.database.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "current_playlist", foreignKeys = @ForeignKey(entity = Music.class, parentColumns = "music_id", childColumns = "current_playlist_file_id", onDelete = CASCADE), indices = {@Index(value = {"current_playlist_file_id"})})
public class CurrentPlaylist {
    @PrimaryKey
    private long current_id;
    private long current_playlist_file_id;

    public CurrentPlaylist(long current_id, long current_playlist_file_id) {
        this.current_id = current_id;
        this.current_playlist_file_id = current_playlist_file_id;
    }

    public long getCurrent_id() {
        return current_id;
    }

    public void setCurrent_id(long current_id) {
        this.current_id = current_id;
    }

    public long getCurrent_playlist_file_id() {
        return current_playlist_file_id;
    }

    public void setCurrent_playlist_file_id(long current_playlist_file_id) {
        this.current_playlist_file_id = current_playlist_file_id;
    }
}
