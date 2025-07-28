package com.maybe.maybe.database.entity;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "playlist", foreignKeys = @ForeignKey(entity = Music.class, parentColumns = "music_id", childColumns = "playlist_file_id", onDelete = CASCADE), indices = { @Index(value = { "playlist_file_id" }) })
public class Playlist {
    @PrimaryKey(autoGenerate = true)
    private long playlist_id;
    private long playlist_file_id;
    private String playlist_name;

    public Playlist(long playlist_file_id, String playlist_name) {
        this.playlist_file_id = playlist_file_id;
        this.playlist_name = playlist_name;
    }

    public long getPlaylist_id() {
        return playlist_id;
    }

    public void setPlaylist_id(long id) {
        this.playlist_id = playlist_id;
    }

    public long getPlaylist_file_id() {
        return playlist_file_id;
    }

    public void setPlaylist_file_id(long playlist_file_id) {
        this.playlist_file_id = playlist_file_id;
    }

    public String getPlaylist_name() {
        return playlist_name;
    }

    public void setPlaylist_name(String playlist_name) {
        this.playlist_name = playlist_name;
    }
}
