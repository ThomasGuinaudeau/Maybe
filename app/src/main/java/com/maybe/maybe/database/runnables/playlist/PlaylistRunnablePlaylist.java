package com.maybe.maybe.database.runnables.playlist;

import com.maybe.maybe.database.AppDatabase;
import com.maybe.maybe.database.dao.PlaylistDao;
import com.maybe.maybe.database.entity.Playlist;

import java.util.List;

public class PlaylistRunnablePlaylist implements Runnable {
    private final IPlaylistRunnablePlaylist callback;
    private final AppDatabase appDatabase;
    private final String query;
    private final long musicId;

    public PlaylistRunnablePlaylist(IPlaylistRunnablePlaylist callback, AppDatabase appDatabase, String query, long musicId) {
        this.callback = callback;
        this.appDatabase = appDatabase;
        this.query = query;
        this.musicId = musicId;
    }

    @Override
    public void run() {
        List<Playlist> playlists = null;
        PlaylistDao dao = appDatabase.playlistDao();
        if (query.equals("selectAllPlaylist"))
            playlists = dao.selectAllPlaylist();
        else if (query.equals("selectAllPlaylistsOfId"))
            playlists = dao.selectAllPlaylistsOfId(musicId);
        callback.onFinish(playlists);
    }
}
