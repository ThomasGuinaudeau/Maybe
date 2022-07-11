package com.maybe.maybe.database.async_tasks.playlist;

import java.util.List;

public interface PlaylistAsyncTaskLongResponse {
    void onPlaylistAsyncTaskLongFinish(List<Long> longs);
}
