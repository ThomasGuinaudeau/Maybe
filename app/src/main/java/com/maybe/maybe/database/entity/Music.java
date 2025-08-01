package com.maybe.maybe.database.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "music")
public class Music implements Parcelable {

    @Ignore
    public static Creator<Music> CREATOR = new Creator<Music>() {

        @Override
        public Music createFromParcel(Parcel source) {
            return new Music(source);
        }

        @Override
        public Music[] newArray(int size) {
            return new Music[size];
        }
    };
    @PrimaryKey
    private long music_id;
    private int music_track;
    private String music_title;
    private String music_album;
    private long music_duration;
    private String music_filename;
    private String music_path;
    private String music_folder;
    private double music_rms;

    public Music(long music_id, int music_track, String music_title, String music_album, long music_duration, String music_filename, String music_path, String music_folder, double music_rms) {
        this.music_id = music_id;
        this.music_track = music_track;
        this.music_title = music_title;
        this.music_album = music_album;
        this.music_duration = music_duration;
        this.music_filename = music_filename;
        this.music_path = music_path;
        this.music_folder = music_folder;
        this.music_rms = music_rms;
    }

    @Ignore
    public Music(Parcel parcel) {
        this.music_id = parcel.readLong();
        this.music_track = parcel.readInt();
        this.music_title = parcel.readString();
        this.music_album = parcel.readString();
        this.music_duration = parcel.readLong();
        this.music_filename = parcel.readString();
        this.music_path = parcel.readString();
        this.music_folder = parcel.readString();
        this.music_rms = parcel.readDouble();
    }

    public long getMusic_id() {
        return music_id;
    }

    public void setMusic_id(long music_id) {
        this.music_id = music_id;
    }

    public int getMusic_track() {
        return music_track;
    }

    public void setMusic_track(int music_track) {
        this.music_track = music_track;
    }

    public String getMusic_title() {
        return music_title;
    }

    public void setMusic_title(String music_title) {
        this.music_title = music_title;
    }

    public String getMusic_album() {
        return music_album;
    }

    public void setMusic_album(String music_album) {
        this.music_album = music_album;
    }

    public long getMusic_duration() {
        return music_duration;
    }

    public void setMusic_duration(long music_duration) {
        this.music_duration = music_duration;
    }

    public String getMusic_filename() {
        return music_filename;
    }

    public void setMusic_filename(String music_filename) {
        this.music_filename = music_filename;
    }

    public String getMusic_path() {
        return music_path;
    }

    public void setMusic_path(String music_path) {
        this.music_path = music_path;
    }

    public String getMusic_folder() {
        return music_folder;
    }

    public void setMusic_folder(String music_folder) {
        this.music_folder = music_folder;
    }

    public double getMusic_rms() {
        return music_rms;
    }

    public void setMusic_rms(double music_rms) {
        this.music_rms = music_rms;
    }

    @Ignore
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(music_id);
        parcel.writeInt(music_track);
        parcel.writeString(music_title);
        parcel.writeString(music_album);
        parcel.writeLong(music_duration);
        parcel.writeString(music_filename);
        parcel.writeString(music_path);
        parcel.writeString(music_folder);
        parcel.writeDouble(music_rms);
    }

    @Ignore
    @Override
    public int describeContents() {
        return 0;
    }
}