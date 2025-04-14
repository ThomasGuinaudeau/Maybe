package com.maybe.maybe.database.runnables.playlist;

import android.database.Cursor;

import com.maybe.maybe.database.AppDatabase;
import com.maybe.maybe.database.dao.PlaylistDao;
import com.maybe.maybe.fragments.category.ListItem;

import java.util.ArrayList;

public class PlaylistRunnableObject implements Runnable {
    private final IPlaylistRunnableObject callback;
    private final AppDatabase appDatabase;

    public PlaylistRunnableObject(IPlaylistRunnableObject callback, AppDatabase appDatabase) {
        this.callback = callback;
        this.appDatabase = appDatabase;
    }

    @Override
    public void run() {
        ArrayList<ListItem> listItems = new ArrayList<>();
        PlaylistDao dao = appDatabase.playlistDao();
        Cursor cursor = dao.selectAllPlaylistWithCount();
        listItems.add(new ListItem(-1, "All Musics", appDatabase.musicDao().selectMusicCount()));
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                listItems.add(new ListItem(cursor.getLong(cursor.getColumnIndexOrThrow("playlist_id")), cursor.getString(cursor.getColumnIndexOrThrow("playlist_name")), cursor.getInt(cursor.getColumnIndexOrThrow("count"))));
            }
        }
        cursor.close();
        callback.onFinish(listItems);
    }
}
