package com.maybe.maybe.database.async_tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.maybe.maybe.R;
import com.maybe.maybe.database.AppDatabase;
import com.maybe.maybe.database.dao.CurrentPlaylistDao;
import com.maybe.maybe.database.entity.CurrentPlaylist;
import com.maybe.maybe.database.entity.MusicWithArtists;

import java.util.List;

public class SaveCurrentListAsyncTask extends AsyncTask<Object, Integer, Object> {

    private final OnSaveCurrentListAsyncTaskFinish callback;
    private final List<MusicWithArtists> musicWithArtists;

    public SaveCurrentListAsyncTask(List<MusicWithArtists> musicWithArtists, OnSaveCurrentListAsyncTaskFinish callback) {
        this.musicWithArtists = musicWithArtists;
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        callback.onSaveCurrentListAsyncTaskStart(musicWithArtists.size());
    }

    @Override
    protected Object doInBackground(Object... objects) {
        //PARAMS: 0=db 1=context 2=currentCol 3=currentCat 4=sort
        Context context = (Context) objects[1];
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString(context.getString(R.string.current_col), (String) objects[2]);
        editor.putString(context.getString(R.string.current_cat), (String) objects[3]);
        editor.putString(context.getString(R.string.sort), (String) objects[4]);
        editor.apply();

        CurrentPlaylistDao dao = ((AppDatabase) objects[0]).currentPlaylistDao();
        dao.deleteAll();
        for (int i = 0; i < musicWithArtists.size(); i++) {
            CurrentPlaylist currentPlaylist = new CurrentPlaylist(i, musicWithArtists.get(i).music.getMusic_id());
            dao.insert(currentPlaylist);
            publishProgress(i);
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        callback.onSaveCurrentListAsyncTaskProgress(values[0]);
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        callback.onSaveCurrentListAsyncTaskFinish();
    }
}
