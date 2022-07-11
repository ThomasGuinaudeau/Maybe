package com.maybe.maybe.database.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "artist")
public class Artist implements Parcelable {
    @Ignore
    public static Parcelable.Creator<Artist> CREATOR = new Parcelable.Creator<Artist>() {

        @Override
        public Artist createFromParcel(Parcel source) {
            return new Artist(source);
        }

        @Override
        public Artist[] newArray(int size) {
            return new Artist[size];
        }
    };
    @PrimaryKey(autoGenerate = true)
    private long artist_id;
    private String artist_name;

    public Artist(String artist_name) {
        this.artist_name = artist_name;
    }

    @Ignore
    public Artist(Parcel parcel) {
        this.artist_id = parcel.readLong();
        this.artist_name = parcel.readString();
    }

    public long getArtist_id() {
        return artist_id;
    }

    public void setArtist_id(long artist_id) {
        this.artist_id = artist_id;
    }

    public String getArtist_name() {
        return artist_name;
    }

    public void setArtist_name(String artist_name) {
        this.artist_name = artist_name;
    }

    @Ignore
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(artist_id);
        parcel.writeString(artist_name);
    }

    @Ignore
    @Override
    public int describeContents() {
        return 0;
    }
}
