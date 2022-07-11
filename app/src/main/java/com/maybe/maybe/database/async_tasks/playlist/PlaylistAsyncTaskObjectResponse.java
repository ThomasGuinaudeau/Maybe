package com.maybe.maybe.database.async_tasks.playlist;

import java.util.ArrayList;
import java.util.HashMap;

public interface PlaylistAsyncTaskObjectResponse {
    void onPlaylistAsyncTaskObjectFinish(ArrayList<HashMap<String, Object>> hashMapList);
}
