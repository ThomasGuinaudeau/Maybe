package com.maybe.maybe.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.maybe.maybe.database.dao.ArtistDao;
import com.maybe.maybe.database.dao.CurrentPlaylistDao;
import com.maybe.maybe.database.dao.MusicArtistCrossRefDao;
import com.maybe.maybe.database.dao.MusicDao;
import com.maybe.maybe.database.dao.PlaylistDao;
import com.maybe.maybe.database.dao.SettingsDao;
import com.maybe.maybe.database.entity.Artist;
import com.maybe.maybe.database.entity.CurrentPlaylist;
import com.maybe.maybe.database.entity.Music;
import com.maybe.maybe.database.entity.MusicArtistCrossRef;
import com.maybe.maybe.database.entity.Playlist;
import com.maybe.maybe.database.entity.Settings;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.maybe.maybe.activities.MainActivity.DATABASE_VERSION;

@Database(entities = {Music.class, MusicArtistCrossRef.class, Artist.class, Playlist.class, CurrentPlaylist.class, Settings.class}, version = DATABASE_VERSION, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "maybe_db";
    private static AppDatabase sInstance;

    public static AppDatabase getInstance(final Context context) {
        if (sInstance == null) {
            synchronized (AppDatabase.class) {
                if (sInstance == null) {
                    sInstance = buildDatabase(context.getApplicationContext(), Executors.newSingleThreadExecutor());
                }
            }
        }
        return sInstance;
    }

    private static AppDatabase buildDatabase(final Context appContext, final Executor thread) {
        return Room.databaseBuilder(appContext, AppDatabase.class, DATABASE_NAME).fallbackToDestructiveMigration().build();
    }

    public abstract MusicDao musicDao();

    public abstract MusicArtistCrossRefDao musicArtistCrossRefDao();

    public abstract ArtistDao artistDao();

    public abstract PlaylistDao playlistDao();

    public abstract CurrentPlaylistDao currentPlaylistDao();

    public abstract SettingsDao settingsDao();

}