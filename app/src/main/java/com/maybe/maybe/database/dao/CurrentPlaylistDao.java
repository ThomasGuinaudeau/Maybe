package com.maybe.maybe.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.maybe.maybe.database.entity.CurrentPlaylist;

import java.util.List;

@Dao
public interface CurrentPlaylistDao {

    @Query("SELECT * FROM current_playlist")
    List<CurrentPlaylist> selectAllCurrentPlaylists();

    @Insert
    void insert(CurrentPlaylist currentPlaylist);

    @Query("DELETE FROM current_playlist")
    void deleteAll();

}
