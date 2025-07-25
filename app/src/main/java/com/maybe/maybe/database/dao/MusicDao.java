package com.maybe.maybe.database.dao;

import static com.maybe.maybe.utils.Constants.SORT_ALPHA;
import static com.maybe.maybe.utils.Constants.SORT_NUM;
import static com.maybe.maybe.utils.Constants.SORT_RANDOM;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RewriteQueriesToDropUnusedColumns;
import androidx.room.Transaction;
import androidx.room.Update;

import com.maybe.maybe.database.entity.Music;
import com.maybe.maybe.database.entity.MusicWithArtists;

import java.util.List;

@Dao
public interface MusicDao {

    @Insert
    void insert(Music music);

    @Update
    void update(Music music);

    @Query("DELETE FROM music WHERE music_id = :music_id")
    void deleteById(long music_id);

    @Query("DELETE FROM music")
    void deleteAll();

    @Query("SELECT * FROM music WHERE music_id = :id")
    MusicWithArtists selectMusicFromId(long id);

    @Transaction
    @Query("SELECT * FROM music ORDER BY CASE WHEN :sort = '" + SORT_ALPHA + "' THEN music.music_title END ASC, CASE WHEN :sort = '" + SORT_RANDOM + "' THEN music.music_title END ASC, CASE WHEN :sort = '" + SORT_NUM + "' THEN music.music_track END ASC")
    List<MusicWithArtists> selectAll(String sort);

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM music INNER JOIN playlist ON music.music_id = playlist.playlist_file_id WHERE playlist.playlist_name LIKE :playlistName ORDER BY " +
            "CASE WHEN :sort = '" + SORT_ALPHA + "' THEN music.music_title END ASC, CASE WHEN :sort = '" + SORT_RANDOM + "' THEN music.music_title END ASC, CASE WHEN :sort = '" + SORT_NUM + "' THEN music.music_track END ASC")
    List<MusicWithArtists> selectAllMusicsOfPlaylist(String sort, String playlistName);

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM music INNER JOIN music_artist_cross_ref macr ON music.music_id = macr.music_id INNER JOIN artist ON macr.artist_id = artist.artist_id WHERE artist_name LIKE :artistName ORDER BY " +
            "CASE WHEN :sort = '" + SORT_ALPHA + "' THEN music.music_title END ASC, CASE WHEN :sort = '" + SORT_RANDOM + "' THEN music.music_title END ASC, CASE WHEN :sort = '" + SORT_NUM + "' THEN music.music_track END ASC")
    List<MusicWithArtists> selectAllMusicsOfArtist(String sort, String artistName);

    @Transaction
    @Query("SELECT *, music_track track, music_title title FROM music WHERE music_album LIKE :albumName ORDER BY " +
            "CASE WHEN :sort = '" + SORT_ALPHA + "' THEN music.music_title END ASC, CASE WHEN :sort = '" + SORT_RANDOM + "' THEN music.music_title END ASC, CASE WHEN :sort = '" + SORT_NUM + "' THEN music.music_track END ASC")
    List<MusicWithArtists> selectAllMusicsOfAlbum(String sort, String albumName);

    @Transaction
    @Query("SELECT *, music_track track, music_title title FROM music WHERE music_folder LIKE :albumName ORDER BY " +
            "CASE WHEN :sort = '" + SORT_ALPHA + "' THEN music.music_title END ASC, CASE WHEN :sort = '" + SORT_RANDOM + "' THEN music.music_title END ASC, CASE WHEN :sort = '" + SORT_NUM + "' THEN music.music_track END ASC")
    List<MusicWithArtists> selectAllMusicsOfFolder(String sort, String albumName);

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM music INNER JOIN current_playlist ON music.music_id = current_playlist.current_playlist_file_id ORDER BY current_playlist.current_id ASC")
    List<MusicWithArtists> selectAllMusicsOfCurrentPlaylist();

    @Query("SELECT music_id FROM music WHERE music_title LIKE :title")
    List<Long> selectAllIdsByTitle(String title);

    @Query("SELECT music_id FROM music WHERE music_path IN (:paths)")
    List<Long> selectAllIdsByPath(List<String> paths);

    @Query("SELECT music_id FROM music")
    List<Long> selectAllIds();

    @Query("SELECT music_path FROM music")
    List<String> selectAllPaths();

    @Query("SELECT music_folder FROM music")
    List<String> selectAllFolders();

    @Query("SELECT music_rms FROM music")
    List<Double> selectAllRMS();

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

    //FOLDER
    @Query("SELECT music_id, music_folder name, COUNT(music_id) count FROM music GROUP BY name ORDER BY name ASC")
    Cursor selectAllFolderWithCount();
}