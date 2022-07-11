package com.maybe.maybe.database.dao;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.maybe.maybe.database.entity.Playlist;

import java.util.List;

@Dao
public interface PlaylistDao {

    @Insert
    void insertAll(List<Playlist> playlists);

    @Query("DELETE FROM playlist WHERE playlist_file_id IN (:fileIds) AND playlist_name LIKE :playlistName")
    void deleteAllPlaylistsByIds(String playlistName, List<Long> fileIds);

    @Query("DELETE FROM playlist WHERE playlist_name IN (:playlistName)")
    void deleteAllFromPlaylists(List<String> playlistName);

    @Query("SELECT playlist_id, playlist_name, COUNT(playlist_id) count FROM playlist GROUP BY playlist_name ORDER BY playlist_name ASC")
    Cursor selectAllPlaylistWithCount();

    @Query("SELECT * FROM playlist GROUP BY playlist_name ORDER BY playlist_name ASC")
    List<Playlist> selectAllPlaylist();

    @Query("SELECT * FROM playlist WHERE playlist_file_id = :musicFileId")
    List<Playlist> selectAllPlaylistsOfId(long musicFileId);

    @Query("SELECT playlist_id FROM playlist WHERE playlist_name LIKE :playlistName")
    List<Long> selectAllIdsOfPlaylist(String playlistName);

}