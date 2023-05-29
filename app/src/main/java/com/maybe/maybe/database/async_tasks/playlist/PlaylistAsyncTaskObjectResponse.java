package com.maybe.maybe.database.async_tasks.playlist;

import com.maybe.maybe.ListItem;

import java.util.ArrayList;

public interface PlaylistAsyncTaskObjectResponse {
    void onPlaylistAsyncTaskObjectFinish(ArrayList<ListItem> listItems);
}
