package com.maybe.maybe.database.runnables.playlist;

import com.maybe.maybe.database.AppDatabase;
import com.maybe.maybe.database.dao.PlaylistDao;
import com.maybe.maybe.database.entity.Playlist;

import java.util.ArrayList;
import java.util.List;

public class PlaylistRunnableNull implements Runnable {
    private final IPlaylistRunnableNull callback;
    private final AppDatabase appDatabase;
    private final String name;
    private final List<Playlist> listOfPlaylist;

    public PlaylistRunnableNull(IPlaylistRunnableNull callback, AppDatabase appDatabase, String name, List<Playlist> listOfPlaylist) {
        this.callback = callback;
        this.appDatabase = appDatabase;
        this.name = name;
        this.listOfPlaylist = listOfPlaylist;
    }

    @Override
    public void run() {
        PlaylistDao dao = appDatabase.playlistDao();
        List<String> nameList = new ArrayList<>();
        nameList.add(name);
        dao.deleteAllFromPlaylists(nameList);
        dao.insertAll(listOfPlaylist);
        callback.onFinishNull();
    }
}
