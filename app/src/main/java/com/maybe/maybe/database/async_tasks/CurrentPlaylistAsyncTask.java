package com.maybe.maybe.database.async_tasks;

import android.os.AsyncTask;

import com.maybe.maybe.database.AppDatabase;
import com.maybe.maybe.database.dao.CurrentPlaylistDao;
import com.maybe.maybe.database.entity.CurrentPlaylist;

import java.util.ArrayList;
import java.util.List;

public class CurrentPlaylistAsyncTask extends AsyncTask<Object, Object, List<CurrentPlaylist>> {

    private OnCurrentPlaylistAsyncTaskFinish callback;
    private String query;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected List<CurrentPlaylist> doInBackground(Object... objects) {
        //PARAMS 0=callback 1=database 2=query
        List<CurrentPlaylist> list = new ArrayList<CurrentPlaylist>();
        callback = (OnCurrentPlaylistAsyncTaskFinish) objects[0];
        CurrentPlaylistDao dao = ((AppDatabase) objects[1]).currentPlaylistDao();
        query = (String) objects[2];

        if (query.equals("selectAllCurrentPlaylists"))
            list = dao.selectAllCurrentPlaylists();

        return list;
    }

    @Override
    protected void onPostExecute(List<CurrentPlaylist> list) {
        super.onPostExecute(list);
        callback.onSelectCurrentPlaylistAsyncFinish(list);
    }

}