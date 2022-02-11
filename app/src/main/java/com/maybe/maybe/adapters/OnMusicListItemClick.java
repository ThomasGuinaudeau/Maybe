package com.maybe.maybe.adapters;

import com.maybe.maybe.database.entity.Music;

public interface OnMusicListItemClick {
    void onItemClick(Music music);
    void onLongItemClick(Music music);
}
