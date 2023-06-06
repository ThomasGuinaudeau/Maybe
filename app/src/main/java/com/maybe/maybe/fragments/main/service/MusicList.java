package com.maybe.maybe.fragments.main.service;

import com.maybe.maybe.database.entity.MusicWithArtists;

import java.util.ArrayList;

import static com.maybe.maybe.utils.Constants.REPEAT_ALL;
import static com.maybe.maybe.utils.Constants.REPEAT_ONE;

public class MusicList {

    private ArrayList<MusicWithArtists> musics;
    private int pointer;

    public MusicList() {
        musics = new ArrayList<>();
        pointer = -1;
    }

    public ArrayList<MusicWithArtists> getMusics() {
        return musics;
    }

    public void setMusics(ArrayList<MusicWithArtists> musics) {
        this.musics = musics;
    }

    public int getPointer() {
        return pointer;
    }

    public int size() {
        return musics.size();
    }

    public void resetPointer() {
        pointer = -1;
    }

    public MusicWithArtists getCurrent() {
        if(pointer == -1)
            pointer = 0;
        return musics.get(pointer);
    }

    public boolean changeForMusicWithId(long fileId) {
        for (MusicWithArtists musicWithArtists : musics)
            if (musicWithArtists.music.getMusic_id() == fileId) {
                pointer = musics.indexOf(musicWithArtists);
                return true;
            }
        pointer = 0;
        return false;
    }

    public void changeForIndex(int index) {
        pointer = index;
    }

    public void goNext(String loop) {
        if (loop.equals(REPEAT_ONE)) ;
        else if (pointer == musics.size() - 1 && loop == REPEAT_ALL)
            pointer = 0;
        else if (pointer != musics.size() - 1)
            pointer++;
        else
            pointer = -1;
    }

    public void goPrevious(String loop) {
        if (loop.equals(REPEAT_ONE)) ;
        else if ((pointer == 0 || pointer == -1) && loop == REPEAT_ALL)
            pointer = musics.size() - 1;
        else if (pointer != 0)
            pointer--;
        else
            pointer = -1;
    }

}