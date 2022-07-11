package com.maybe.maybe.database.async_tasks.playlist;

import android.database.Cursor;
import android.os.AsyncTask;

import com.maybe.maybe.database.AppDatabase;
import com.maybe.maybe.database.dao.PlaylistDao;

import java.util.ArrayList;
import java.util.HashMap;

public class PlaylistAsyncTaskObject extends AsyncTask<Object, Object, ArrayList<HashMap<String, Object>>> {

    private PlaylistAsyncTaskObjectResponse callback;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected ArrayList<HashMap<String, Object>> doInBackground(Object... objects) {
        //PARAMS 0=callback 1=database 2=query
        ArrayList<HashMap<String, Object>> hashMapList = new ArrayList<HashMap<String, Object>>();
        callback = (PlaylistAsyncTaskObjectResponse) objects[0];
        PlaylistDao dao = ((AppDatabase) objects[1]).playlistDao();
        String query = (String) objects[2];
        if (query.equals("selectAllPlaylistWithCount")) {
            Cursor cursor = dao.selectAllPlaylistWithCount();
            HashMap<String, Object> firstHashMap = new HashMap<String, Object>();
            firstHashMap.put("name", "All Musics");
            firstHashMap.put("count", ((AppDatabase) objects[1]).musicDao().selectMusicCount());
            hashMapList.add(firstHashMap);
            while (cursor.moveToNext()) {
                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                hashMap.put("name", cursor.getString(cursor.getColumnIndex("playlist_name")));
                hashMap.put("count", cursor.getInt(cursor.getColumnIndex("count")));
                hashMapList.add(hashMap);
            }
            cursor.close();
        }
        return hashMapList;
    }

    @Override
    protected void onPostExecute(ArrayList<HashMap<String, Object>> hashMapList) {
        super.onPostExecute(hashMapList);
        callback.onPlaylistAsyncTaskObjectFinish(hashMapList);
    }

}