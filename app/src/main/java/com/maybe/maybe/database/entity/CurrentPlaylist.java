package com.maybe.maybe.database.entity;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(tableName = "current_playlist", foreignKeys = @ForeignKey(entity = Music.class, parentColumns = "music_id", childColumns = "current_playlist_file_id", onDelete = CASCADE), primaryKeys = { "current_id", "current_playlist_file_id" }, indices = { @Index(value = { "current_playlist_file_id" }) })
public class CurrentPlaylist {
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
