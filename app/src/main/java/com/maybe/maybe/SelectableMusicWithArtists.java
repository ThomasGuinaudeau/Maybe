package com.maybe.maybe;

import com.maybe.maybe.database.entity.MusicWithArtists;

public class SelectableMusicWithArtists {
    private MusicWithArtists musicWithArtists;
    private boolean isSelected;
    private long key;

    public SelectableMusicWithArtists(MusicWithArtists musicWithArtists) {
        this.musicWithArtists = musicWithArtists;
        isSelected = false;
        key = musicWithArtists.music.getMusic_id();
        //key = (long) (Math.random() * 1000000);
    }

    public MusicWithArtists getMusicWithArtists() {
        return musicWithArtists;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getDuration() {
        long duration = musicWithArtists.music.getMusic_duration();
        long totalSeconds = duration / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(minutes + ": %02d", seconds);
    }

    /*public long getKey() {
        return key;
}*/
}
