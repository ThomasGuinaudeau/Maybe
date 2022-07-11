package com.maybe.maybe.database.async_tasks;

import com.maybe.maybe.database.entity.CurrentPlaylist;

import java.util.List;

public interface OnCurrentPlaylistAsyncTaskFinish {
    void onSelectCurrentPlaylistAsyncFinish(List<CurrentPlaylist> currentPlaylists);
}
