package com.maybe.maybe.database.runnables;

import static com.maybe.maybe.utils.Constants.SORT_RANDOM;

import android.content.Context;
import android.database.Cursor;

import androidx.core.content.ContextCompat;

import com.maybe.maybe.database.AppDatabase;
import com.maybe.maybe.database.dao.ArtistDao;
import com.maybe.maybe.database.dao.MusicDao;
import com.maybe.maybe.database.entity.MusicWithArtists;
import com.maybe.maybe.fragments.category.ListItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

public class MusicRunnable implements Runnable {
    private final Context context;
    private final IMusicRunnable callback;
    private final AppDatabase appDatabase;
    private final String query;
    private final long musicId;
    private final String sort, name;
    private final List<String> pathList;

    public MusicRunnable(Context context, IMusicRunnable callback, AppDatabase appDatabase, String query, long musicId, String sort, String name, List<String> pathList) {
        this.context = context;
        this.callback = callback;
        this.appDatabase = appDatabase;
        this.query = query;
        this.musicId = musicId;
        this.sort = sort;
        this.name = name;
        this.pathList = pathList;
    }

    @Override
    public void run() {
        List<Object> list = new ArrayList<>();
        MusicDao dao = appDatabase.musicDao();
        List<MusicWithArtists> musicWithArtists = null;

        switch (query) {
            case "selectAllArtistWithMusics":
                ArtistDao artistDao = appDatabase.artistDao();
                list = (List<Object>) (Object) artistDao.selectAllArtistWithMusics();
            case "selectMusicFromId":
                List<MusicWithArtists> tempList = new ArrayList<>();
                tempList.add(dao.selectMusicFromId(musicId));
                musicWithArtists = tempList;
                break;
            case "selectAll":
                musicWithArtists = dao.selectAll(sort);
                break;
            case "selectAllMusicsOfPlaylist":
                musicWithArtists = dao.selectAllMusicsOfPlaylist(sort, name);
                break;
            case "selectAllMusicsOfArtist":
                musicWithArtists = dao.selectAllMusicsOfArtist(sort, name);
                break;
            case "selectAllMusicsOfAlbum":
                musicWithArtists = dao.selectAllMusicsOfAlbum(sort, name);
                break;
            case "selectAllMusicsOfFolder":
                musicWithArtists = dao.selectAllMusicsOfFolder(sort, name);
                break;
            case "selectAllMusicsOfCurrentPlaylist":
                list = (List<Object>) (Object) dao.selectAllMusicsOfCurrentPlaylist();
                break;
            case "selectAllIdsByTitle":
                list = (List<Object>) (Object) dao.selectAllIdsByTitle("%" + name + "%");
                break;
            case "selectAllIdsByPath":
                list = (List<Object>) (Object) dao.selectAllIdsByPath(pathList);
                break;
            default:
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
            if (!query.equals("selectMusicFromId") && sort.equals(SORT_RANDOM))
                Collections.shuffle(musicWithArtists);
            list = (List<Object>) (Object) musicWithArtists;
        }

        final List<Object> finalList = list;
        Executor mainThreadExecutor = ContextCompat.getMainExecutor(context);
        mainThreadExecutor.execute(() -> callback.onFinish(finalList));
    }
}
