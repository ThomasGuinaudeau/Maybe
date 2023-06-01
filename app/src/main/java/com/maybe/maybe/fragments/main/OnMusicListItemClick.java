package com.maybe.maybe.fragments.main;

import com.maybe.maybe.database.entity.Music;

public interface OnMusicListItemClick {
    void onItemClick(Music music);
    void onLongItemClick(Music music);
}
