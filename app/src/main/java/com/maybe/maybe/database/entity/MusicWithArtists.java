package com.maybe.maybe.database.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Embedded;
import androidx.room.Ignore;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.ArrayList;
import java.util.List;

public class MusicWithArtists implements Parcelable {
    @Ignore
    public static Parcelable.Creator<MusicWithArtists> CREATOR = new Parcelable.Creator<MusicWithArtists>() {

        @Override
        public MusicWithArtists createFromParcel(Parcel source) {
            return new MusicWithArtists(source);
        }

        @Override
        public MusicWithArtists[] newArray(int size) {
            return new MusicWithArtists[size];
        }

    };
    @Embedded
    public Music music;
    @Relation(
            parentColumn = "music_id",
            entityColumn = "artist_id",
            associateBy = @Junction(MusicArtistCrossRef.class)
    )
    public List<Artist> artists;

    public MusicWithArtists(Music music, List<Artist> artists) {
        this.music = music;
        this.artists = artists;
    }

    @Ignore
    public MusicWithArtists(Parcel parcel) {
        this.music = parcel.readTypedObject(Music.CREATOR);
        if (artists == null)
            artists = new ArrayList<Artist>();
        parcel.readTypedList(artists, Artist.CREATOR);
    }

    @Ignore
    public String artistsToString() {
        String txt = "";
        for (int i = 0; i < artists.size(); i++) {
            if (i > 0)
                txt += "/";
            txt += artists.get(i).getArtist_name();
        }
        return txt;
    }

    @Ignore
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedObject(music, 0);
        parcel.writeTypedList(artists);
    }

    @Ignore
    @Override
    public int describeContents() {
        return 0;
    }
}