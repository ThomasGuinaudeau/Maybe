package com.maybe.maybe;

import com.maybe.maybe.database.entity.MusicWithArtists;

public class EditingListItem {
    private MusicWithArtists musicWithArtists;
    private boolean isSelected;

    public EditingListItem(MusicWithArtists musicWithArtists, boolean isSelected) {
        this.musicWithArtists = musicWithArtists;
        this.isSelected = isSelected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }
}
