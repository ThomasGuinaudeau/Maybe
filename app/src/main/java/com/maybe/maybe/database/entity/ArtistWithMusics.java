package com.maybe.maybe.database.entity;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.List;

public class ArtistWithMusics {
    @Embedded
    public Artist artist;
    @Relation(
            parentColumn = "artist_id",
            entityColumn = "music_id",
            associateBy = @Junction(MusicArtistCrossRef.class)
    )
    public List<Music> musics;

    public ArtistWithMusics(Artist artist, List<Music> musics) {
        this.artist = artist;
        this.musics = musics;
    }
}
