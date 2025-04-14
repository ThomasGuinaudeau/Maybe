package com.maybe.maybe.database.runnables.playlist;

import com.maybe.maybe.fragments.category.ListItem;

import java.util.ArrayList;

public interface IPlaylistRunnableObject {
    void onFinish(ArrayList<ListItem> listItems);
}
