package com.maybe.maybe.database.async_tasks;

import static com.maybe.maybe.utils.Constants.SORT_RANDOM;

import android.database.Cursor;
import android.os.AsyncTask;

import com.maybe.maybe.database.AppDatabase;
import com.maybe.maybe.database.dao.MusicDao;
import com.maybe.maybe.database.entity.MusicWithArtists;
import com.maybe.maybe.fragments.category.ListItem;

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
            case "selectMusicFromId": //3=musicId
                List<MusicWithArtists> tempList = new ArrayList<>();
                tempList.add(dao.selectMusicFromId((long) objects[3]));
                musicWithArtists = tempList;
                break;
            case "selectAll": //3=sort
                musicWithArtists = dao.selectAll((String) objects[3]);
                break;
            case "selectAllMusicsOfPlaylist": //3=sort 4=playlistName
                musicWithArtists = dao.selectAllMusicsOfPlaylist((String) objects[3], (String) objects[4]);
                break;
            case "selectAllMusicsOfArtist": //3=sort 4=artistName
                musicWithArtists = dao.selectAllMusicsOfArtist((String) objects[3], (String) objects[4]);
                break;
            case "selectAllMusicsOfAlbum": //3=sort 4=albumName selectAllMusicsOfFolder
                musicWithArtists = dao.selectAllMusicsOfAlbum((String) objects[3], (String) objects[4]);
                break;
            case "selectAllMusicsOfFolder": //3=sort 4=folderName
                musicWithArtists = dao.selectAllMusicsOfFolder((String) objects[3], (String) objects[4]);
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
            default:
                onSelectAlbumAsyncTaskFinish = (OnSelectAlbumAsyncTaskFinish) objects[0];
                Cursor cursor = null;
                if (query.equals("selectAllAlbumWithCount"))
                    cursor = dao.selectAllAlbumWithCount();
                else if (query.equals("selectAllFolderWithCount"))
                    cursor = dao.selectAllFolderWithCount();
                List<ListItem> listItems = new ArrayList<>();
                if (cursor != null && cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        listItems.add(new ListItem(cursor.getLong(cursor.getColumnIndexOrThrow("music_id")), cursor.getString(cursor.getColumnIndexOrThrow("name")), cursor.getInt(cursor.getColumnIndexOrThrow("count"))));
                    }
                }
                cursor.close();
                list = (List<Object>) (Object) listItems;
        }

        if (query.equals("selectMusicFromId") || query.equals("selectAll") || query.equals("selectAllMusicsOfPlaylist") || query.equals("selectAllMusicsOfArtist") || query.equals("selectAllMusicsOfAlbum") || query.equals("selectAllMusicsOfFolder")) {
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
            case "selectMusicFromId":
            case "selectAll":
            case "selectAllMusicsOfPlaylist":
            case "selectAllMusicsOfArtist":
            case "selectAllMusicsOfAlbum":
            case "selectAllMusicsOfFolder":
            case "selectAllMusicsOfCurrentPlaylist":
                onSelectMusicAsyncTaskFinish.onSelectMusicAsyncFinish(list);
                break;
            case "selectAllIdsByTitle":
            case "selectAllIdsByPath":
                onSearchMusicAsyncFinish.onSearchMusicAsyncFinish(list);
                break;
            case "selectAllAlbumWithCount":
            case "selectAllFolderWithCount":
                onSelectAlbumAsyncTaskFinish.onSelectAlbumAsyncFinish(list);
                break;
        }
    }
}