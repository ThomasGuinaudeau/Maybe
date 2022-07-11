package com.maybe.maybe.database.async_tasks.playlist;

import android.os.AsyncTask;

import com.maybe.maybe.database.AppDatabase;
import com.maybe.maybe.database.dao.PlaylistDao;
import com.maybe.maybe.database.entity.Playlist;

import java.util.List;

public class PlaylistAsyncTaskPlaylist extends AsyncTask<Object, Object, List<Playlist>> {

    private PlaylistAsyncTaskPlaylistResponse callback;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected List<Playlist> doInBackground(Object... objects) {
        //PARAMS 0=callback 1=database 2=query
        List<Playlist> playlists = null;
        callback = (PlaylistAsyncTaskPlaylistResponse) objects[0];
        PlaylistDao dao = ((AppDatabase) objects[1]).playlistDao();
        String query = (String) objects[2];
        if (query.equals("selectAllPlaylist"))
            playlists = dao.selectAllPlaylist();
        else if (query.equals("selectAllPlaylistsOfId"))//3=musicId
            playlists = dao.selectAllPlaylistsOfId((long) objects[3]);
        return playlists;
    }

    @Override
    protected void onPostExecute(List<Playlist> playlists) {
        super.onPostExecute(playlists);
        callback.onPlaylistAsyncTaskPlaylistFinish(playlists);
    }

}
