package com.maybe.maybe.database.async_tasks;

import static com.maybe.maybe.utils.Constants.SORT_RANDOM;

import android.database.Cursor;
import android.os.AsyncTask;

import com.maybe.maybe.fragments.category.ListItem;
import com.maybe.maybe.database.AppDatabase;
import com.maybe.maybe.database.dao.MusicDao;
import com.maybe.maybe.database.entity.MusicWithArtists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MusicAsyncTask extends AsyncTask<Object, Object, List<Object>> {

    private OnSelectMusicAsyncTaskFinish onSelectMusicAsyncTaskFinish;
    private OnSearchMusicAsyncTaskFinish onSearchMusicAsyncFinish;
    private OnSelectAlbumAsyncTaskFinish onSelectAlbumAsyncTaskFinish;
    private String query;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected List<Object> doInBackground(Object... objects) {
        //PARAMS 0=callback 1=database 2=query
        List<Object> list = new ArrayList<>();
        MusicDao dao = ((AppDatabase) objects[1]).musicDao();
        query = (String) objects[2];
        List<MusicWithArtists> musicWithArtists = null;

        switch (query) {
            case "selectAll": //3=sort
                musicWithArtists = dao.selectAll((String) objects[3]);
                break;
            case "selectAllMusicsOfPlaylist": //3=sort 4=playlistName
                musicWithArtists = dao.selectAllMusicsOfPlaylist((String) objects[3], (String) objects[4]);
                break;
            case "selectAllMusicsOfArtist": //3=sort 4=artistName
                musicWithArtists = dao.selectAllMusicsOfArtist((String) objects[3], (String) objects[4]);
                break;
            case "selectAllMusicsOfAlbum": //3=sort 4=albumName
                musicWithArtists = dao.selectAllMusicsOfAlbum((String) objects[3], (String) objects[4]);
                break;
            case "selectAllMusicsOfCurrentPlaylist":
                onSelectMusicAsyncTaskFinish = (OnSelectMusicAsyncTaskFinish) objects[0];
                list = (List<Object>) (Object) dao.selectAllMusicsOfCurrentPlaylist();
                break;
            case "selectAllIdsByTitle": //3=title
                onSearchMusicAsyncFinish = (OnSearchMusicAsyncTaskFinish) objects[0];
                list = (List<Object>) (Object) dao.selectAllIdsByTitle("%" + objects[3] + "%");
                break;
            case "selectAllIdsByPath": //3=pathList
                onSearchMusicAsyncFinish = (OnSearchMusicAsyncTaskFinish) objects[0];
                list = (List<Object>) (Object) dao.selectAllIdsByPath((List<String>) objects[3]);
                break;
            case "selectAllAlbumWithCount":
                onSelectAlbumAsyncTaskFinish = (OnSelectAlbumAsyncTaskFinish) objects[0];
                Cursor cursor = dao.selectAllAlbumWithCount();
                //List<HashMap<String, Object>> hashMapList = new ArrayList<>();
                List<ListItem> listItems = new ArrayList<>();
                if (cursor != null && cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        listItems.add(new ListItem(cursor.getLong(cursor.getColumnIndexOrThrow("music_id")), cursor.getString(cursor.getColumnIndexOrThrow("name")), cursor.getInt(cursor.getColumnIndexOrThrow("count"))));
                    }
                }
                cursor.close();
                list = (List<Object>) (Object) listItems;
                break;
        }

        if (query.equals("selectAll") || query.equals("selectAllMusicsOfPlaylist") || query.equals("selectAllMusicsOfArtist") || query.equals("selectAllMusicsOfAlbum")) {
            onSelectMusicAsyncTaskFinish = (OnSelectMusicAsyncTaskFinish) objects[0];
            if (objects[3].equals(SORT_RANDOM))
                Collections.shuffle(musicWithArtists);
            list = (List<Object>) (Object) musicWithArtists;
        }

        return list;
    }

    @Override
    protected void onPostExecute(List<Object> list) {
        super.onPostExecute(list);
        switch (query) {
            case "selectAll":
            case "selectAllMusicsOfPlaylist":
            case "selectAllMusicsOfArtist":
            case "selectAllMusicsOfAlbum":
            case "selectAllMusicsOfCurrentPlaylist":
                onSelectMusicAsyncTaskFinish.onSelectMusicAsyncFinish(list);
                break;
            case "selectAllIdsByTitle":
            case "selectAllIdsByPath":
                onSearchMusicAsyncFinish.onSearchMusicAsyncFinish(list);
                break;
            case "selectAllAlbumWithCount":
                onSelectAlbumAsyncTaskFinish.onSelectAlbumAsyncFinish(list);
                break;
        }
    }
}