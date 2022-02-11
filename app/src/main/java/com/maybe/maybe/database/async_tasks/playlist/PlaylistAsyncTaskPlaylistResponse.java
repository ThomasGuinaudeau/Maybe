package com.maybe.maybe.database.async_tasks.playlist;

import com.maybe.maybe.database.entity.Playlist;

import java.util.List;

public interface PlaylistAsyncTaskPlaylistResponse {
    void onPlaylistAsyncTaskPlaylistFinish(List<Playlist> playlists);
}
