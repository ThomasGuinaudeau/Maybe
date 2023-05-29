package com.maybe.maybe.database.async_tasks.playlist;

import android.os.AsyncTask;

import com.maybe.maybe.database.AppDatabase;
import com.maybe.maybe.database.dao.PlaylistDao;
import com.maybe.maybe.database.entity.Playlist;

import java.util.ArrayList;
import java.util.List;

public class PlaylistAsyncTaskNull extends AsyncTask<Object, Object, Void> {

    private PlaylistAsyncTaskNullResponse callback;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Object... objects) {
        //PARAMS 0=callback 1=database 2=query
        callback = (PlaylistAsyncTaskNullResponse) objects[0];
        PlaylistDao dao = ((AppDatabase) objects[1]).playlistDao();
        String query = (String) objects[2];
        if (query.equals("updatePlaylist")) {//3=listOfPlaylistObject 4=playlistName
            List<String> nameList = new ArrayList<>();
            nameList.add((String) objects[4]);
            dao.deleteAllFromPlaylists(nameList);
            dao.insertAll((List<Playlist>) objects[3]);
        } else if (query.equals("insertAll"))
            dao.insertAll((List<Playlist>) objects[3]);
        else if (query.equals("deleteAllPlaylistsByIds"))//3=playlistName 4=musicIds
            dao.deleteAllPlaylistsByIds((String) objects[3], (List<Long>) objects[4]);
        else if (query.equals("deleteAllFromPlaylists")) {//3=playlistNames
            dao.deleteAllFromPlaylists((List<String>) objects[3]);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        super.onPostExecute(v);
        callback.onPlaylistAsyncTaskNullFinish();
    }
}
