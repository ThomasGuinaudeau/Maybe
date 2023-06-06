package com.maybe.maybe.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.maybe.maybe.database.entity.Artist;
import com.maybe.maybe.database.entity.ArtistWithMusics;

import java.util.List;

@Dao
public interface ArtistDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    List<Long> insertAll(List<Artist> artists);

    @Delete
    void delete(Artist artist);

    @Query("DELETE FROM artist WHERE artist_id = :artist_id")
    void deleteById(long artist_id);

    @Query("DELETE FROM artist")
    void deleteAll();

    @Query("SELECT * FROM artist")
    List<Artist> selectAll();

    @Query("SELECT artist_id FROM artist")
    List<Long> selectAllArtistsId();

    @Transaction
    @Query("SELECT * FROM artist ORDER BY artist_name")
    List<ArtistWithMusics> selectAllArtistWithMusics();
}