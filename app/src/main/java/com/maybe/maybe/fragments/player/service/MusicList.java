package com.maybe.maybe.fragments.player.service;

import static com.maybe.maybe.utils.Constants.REPEAT_ALL;
import static com.maybe.maybe.utils.Constants.REPEAT_ONE;

import java.util.ArrayList;

public class MusicList {

    private ArrayList<Long> idList;
    private int pointer;

    public MusicList() {
        idList = new ArrayList<>();
        pointer = -1;
    }

    public ArrayList<Long> getMusics() {
        return idList;
    }

    public void setMusics(ArrayList<Long> idList) {
        this.idList = idList;
    }

    public int getPointer() {
        return pointer;
    }

    public int size() {
        return idList.size();
    }

    public void resetPointer() {
        pointer = -1;
    }

    public long getCurrent() {
        if (pointer == -1)
            pointer = 0;
        return idList.get(pointer);
    }

    public boolean changeForMusicWithId(long fileId) {
        for (long id : idList)
            if (id == fileId) {
                pointer = idList.indexOf(id);
                return true;
            }
        pointer = 0;
        return false;
    }

    public void changeForIndex(int index) {
        pointer = index;
    }

    public long getNext() {
        if (pointer == -1)
            return 0;
        else if (pointer == idList.size() - 1)
            return idList.get(0);
        else
            return idList.get(pointer + 1);
    }

    public void goNext(String loop) {
        if (loop.equals(REPEAT_ONE)) ;
        else if (pointer == idList.size() - 1 && loop.equals(REPEAT_ALL))
            pointer = 0;
        else if (pointer != idList.size() - 1)
            pointer++;
        else
            pointer = -1;
    }

    public void goPrevious(String loop) {
        if (loop.equals(REPEAT_ONE)) ;
        else if ((pointer == 0 || pointer == -1) && loop.equals(REPEAT_ALL))
            pointer = idList.size() - 1;
        else if (pointer != 0)
            pointer--;
        else
            pointer = -1;
    }
}