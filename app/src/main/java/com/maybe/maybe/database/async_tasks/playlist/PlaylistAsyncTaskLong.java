package com.maybe.maybe.database.async_tasks.playlist;

import android.os.AsyncTask;

import com.maybe.maybe.database.AppDatabase;
import com.maybe.maybe.database.dao.PlaylistDao;

import java.util.List;

public class PlaylistAsyncTaskLong extends AsyncTask<Object, Object, List<Long>> {

    private PlaylistAsyncTaskLongResponse callback;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected List<Long> doInBackground(Object... objects) {
        List<Long> longList = null;
        //PARAMS 0=callback 1=database 2=query
        callback = (PlaylistAsyncTaskLongResponse) objects[0];
        PlaylistDao dao = ((AppDatabase) objects[1]).playlistDao();
        String query = (String) objects[2];
        if (query.equals("selectAllIdsOfPlaylist"))//3=playlistName
            longList = dao.selectAllIdsOfPlaylist((String) objects[3]);
        return longList;
    }

    @Override
    protected void onPostExecute(List<Long> longs) {
        super.onPostExecute(longs);
        callback.onPlaylistAsyncTaskLongFinish(longs);
    }

}
