package com.maybe.maybe.database.dao;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RewriteQueriesToDropUnusedColumns;
import androidx.room.Transaction;

import com.maybe.maybe.database.entity.Music;
import com.maybe.maybe.database.entity.MusicWithArtists;

import java.util.List;

import static com.maybe.maybe.utils.Constants.SORT_ALPHA;
import static com.maybe.maybe.utils.Constants.SORT_NUM;
import static com.maybe.maybe.utils.Constants.SORT_RANDOM;

@Dao
public interface MusicDao {

    @Insert
    void insert(Music music);

    @Query("DELETE FROM music")
    void deleteAll();

    @Transaction
    @Query("SELECT * FROM music ORDER BY CASE WHEN :sort LIKE '" + SORT_ALPHA + "' THEN music.music_title END ASC, CASE WHEN :sort LIKE '" + SORT_RANDOM + "' THEN music.music_title END ASC, CASE WHEN :sort LIKE '" + SORT_NUM + "' THEN music.music_track END ASC")
    List<MusicWithArtists> selectAll(String sort);

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM music INNER JOIN playlist ON music.music_id = playlist.playlist_file_id WHERE playlist.playlist_name LIKE :playlistName ORDER BY " +
            "CASE WHEN :sort LIKE '" + SORT_ALPHA + "' THEN music.music_title END ASC, CASE WHEN :sort LIKE '" + SORT_RANDOM + "' THEN music.music_title END ASC, CASE WHEN :sort LIKE '" + SORT_NUM + "' THEN music.music_track END ASC")
    List<MusicWithArtists> selectAllMusicsOfPlaylist(String sort, String playlistName);

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM music INNER JOIN music_artist_cross_ref macr ON music.music_id = macr.music_id INNER JOIN artist ON macr.artist_id = artist.artist_id WHERE artist_name LIKE :artistName ORDER BY " +
            "CASE WHEN :sort LIKE '" + SORT_ALPHA + "' THEN music.music_title END ASC, CASE WHEN :sort LIKE '" + SORT_RANDOM + "' THEN music.music_title END ASC, CASE WHEN :sort LIKE '" + SORT_NUM + "' THEN music.music_track END ASC")
    List<MusicWithArtists> selectAllMusicsOfArtist(String sort, String artistName);

    @Transaction
    @Query("SELECT *, music_track track, music_title title FROM music WHERE music_album LIKE :albumName ORDER BY " +
            "CASE WHEN :sort LIKE '" + SORT_ALPHA + "' THEN music.music_title END ASC, CASE WHEN :sort LIKE '" + SORT_RANDOM + "' THEN music.music_title END ASC, CASE WHEN :sort LIKE '" + SORT_NUM + "' THEN music.music_track END ASC")
    List<MusicWithArtists> selectAllMusicsOfAlbum(String sort, String albumName);

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM music INNER JOIN current_playlist ON music.music_id = current_playlist.current_playlist_file_id ORDER BY current_playlist.current_id ASC")
    List<MusicWithArtists> selectAllMusicsOfCurrentPlaylist();

    @Query("SELECT music_id FROM music WHERE music_title LIKE :title")
    List<Long> selectAllIdsByTitle(String title);

    /*@Transaction
    @Query("SELECT * FROM music WHERE music_id IN (:musicFileIds)")
    List<MusicWithArtists> selectAllByIds(List<Integer> musicFileIds);

    @Query("SELECT music_id FROM music")
    List<Long> selectAllIds();*/

    @Query("SELECT COUNT(music_id) count FROM music ORDER BY count")
    int selectMusicCount();

    //ALBUM
    @Query("SELECT music_id, music_album name, COUNT(music_id) count FROM music GROUP BY name ORDER BY name ASC")
    Cursor selectAllAlbumWithCount();

}