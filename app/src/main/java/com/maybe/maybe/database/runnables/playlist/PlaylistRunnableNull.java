package com.maybe.maybe.database.runnables.playlist;

import com.maybe.maybe.database.AppDatabase;
import com.maybe.maybe.database.dao.PlaylistDao;
import com.maybe.maybe.database.entity.Playlist;
import com.maybe.maybe.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class PlaylistRunnableNull implements Runnable {
    private final IPlaylistRunnableNull callback;
    private final AppDatabase appDatabase;
    private final String name;
    private final List<Playlist> listOfPlaylist;
    private final int type;

    public PlaylistRunnableNull(IPlaylistRunnableNull callback, AppDatabase appDatabase, String name, List<Playlist> listOfPlaylist, int type) {
        this.callback = callback;
        this.appDatabase = appDatabase;
        this.name = name;
        this.listOfPlaylist = listOfPlaylist;
        this.type = type;
    }

    @Override
    public void run() {
        PlaylistDao dao = appDatabase.playlistDao();
        if (type == Constants.PLAYLIST_REPLACE) {
            List<String> nameList = new ArrayList<>();
            nameList.add(name);
            dao.deleteAllFromPlaylists(nameList);
            dao.insertAll(listOfPlaylist);
        } else if (type == Constants.PLAYLIST_ADD) {
            dao.insertAll(listOfPlaylist);
        } else if (type == Constants.PLAYLIST_REMOVE) {
            for (Playlist p : listOfPlaylist)
                dao.deleteMusicFromPlaylist(p.getPlaylist_file_id(), p.getPlaylist_name());
        }
        callback.onFinishNull();
    }
}
