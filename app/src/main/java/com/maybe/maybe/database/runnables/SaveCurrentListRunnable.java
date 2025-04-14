package com.maybe.maybe.database.runnables;

import android.content.Context;
import android.content.SharedPreferences;

import com.maybe.maybe.R;
import com.maybe.maybe.database.AppDatabase;
import com.maybe.maybe.database.dao.CurrentPlaylistDao;
import com.maybe.maybe.database.entity.CurrentPlaylist;
import com.maybe.maybe.database.entity.MusicWithArtists;

import java.util.List;

public class SaveCurrentListRunnable implements Runnable {
    private final ISaveCurrentListRunnable callback;
    private final List<MusicWithArtists> musicWithArtists;
    private final AppDatabase appDatabase;
    private final Context context;
    private final int currentCol;
    private final String currentCat, sort;
    private final boolean isStart;

    public SaveCurrentListRunnable(ISaveCurrentListRunnable callback, List<MusicWithArtists> musicWithArtists, AppDatabase appDatabase, Context context, int currentCol, String currentCat, String sort, boolean isStart) {
        this.callback = callback;
        this.musicWithArtists = musicWithArtists;
        this.appDatabase = appDatabase;
        this.context = context;
        this.currentCol = currentCol;
        this.currentCat = currentCat;
        this.sort = sort;
        this.isStart = isStart;
    }

    @Override
    public void run() {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putInt(context.getString(R.string.current_category_id), currentCol);
        editor.putString(context.getString(R.string.current_name), currentCat);
        editor.putString(context.getString(R.string.sort), sort);
        editor.apply();

        if (!isStart) {
            CurrentPlaylistDao dao = appDatabase.currentPlaylistDao();
            dao.deleteAll();
            for (int i = 0; i < musicWithArtists.size(); i++) {
                CurrentPlaylist currentPlaylist = new CurrentPlaylist(i, musicWithArtists.get(i).music.getMusic_id());
                dao.insert(currentPlaylist);
                callback.onSaveCurrentListRunnableProgress(i);
            }
        }
    }
}
