package com.maybe.maybe.database.entity;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(tableName = "music_artist_cross_ref", foreignKeys = { @ForeignKey(entity = Music.class, parentColumns = "music_id", childColumns = "music_id", onDelete = CASCADE), @ForeignKey(entity = Artist.class, parentColumns = "artist_id", childColumns = "artist_id", onDelete = CASCADE) }, primaryKeys = { "music_id", "artist_id" }, indices = @Index(value = { "artist_id" }))
public class MusicArtistCrossRef {
    private long music_id;
    private long artist_id;

    public MusicArtistCrossRef(long music_id, long artist_id) {
        this.music_id = music_id;
        this.artist_id = artist_id;
    }

    public long getMusic_id() {
        return music_id;
    }

    public void setMusic_id(long music_id) {
        this.music_id = music_id;
    }

    public long getArtist_id() {
        return artist_id;
    }

    public void setArtist_id(long artist_id) {
        this.artist_id = artist_id;
    }
}
