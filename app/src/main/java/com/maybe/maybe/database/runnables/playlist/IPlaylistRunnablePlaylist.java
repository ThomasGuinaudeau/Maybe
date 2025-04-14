package com.maybe.maybe.database.runnables.playlist;

import com.maybe.maybe.database.entity.Playlist;

import java.util.List;

public interface IPlaylistRunnablePlaylist {
    void onFinish(List<Playlist> playlists);
}
