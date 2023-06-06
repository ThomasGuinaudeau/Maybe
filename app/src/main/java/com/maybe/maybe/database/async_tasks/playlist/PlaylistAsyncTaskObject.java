package com.maybe.maybe.database.async_tasks.playlist;

import android.database.Cursor;
import android.os.AsyncTask;

import com.maybe.maybe.fragments.category.ListItem;
import com.maybe.maybe.database.AppDatabase;
import com.maybe.maybe.database.dao.PlaylistDao;

import java.util.ArrayList;

public class PlaylistAsyncTaskObject extends AsyncTask<Object, Object, ArrayList<ListItem>> {

    private PlaylistAsyncTaskObjectResponse callback;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected ArrayList<ListItem> doInBackground(Object... objects) {
        //PARAMS 0=callback 1=database 2=query
        ArrayList<ListItem> listItems = new ArrayList<>();
        callback = (PlaylistAsyncTaskObjectResponse) objects[0];
        PlaylistDao dao = ((AppDatabase) objects[1]).playlistDao();
        String query = (String) objects[2];
        if (query.equals("selectAllPlaylistWithCount")) {
            Cursor cursor = dao.selectAllPlaylistWithCount();
            listItems.add(new ListItem(-1, "All Musics", ((AppDatabase) objects[1]).musicDao().selectMusicCount()));
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    listItems.add(new ListItem(cursor.getLong(cursor.getColumnIndexOrThrow("playlist_id")), cursor.getString(cursor.getColumnIndexOrThrow("playlist_name")), cursor.getInt(cursor.getColumnIndexOrThrow("count"))));
                }
            }
            cursor.close();
        }
        return listItems;
    }

    @Override
    protected void onPostExecute(ArrayList<ListItem> listItems) {
        super.onPostExecute(listItems);
        callback.onPlaylistAsyncTaskObjectFinish(listItems);
    }
}