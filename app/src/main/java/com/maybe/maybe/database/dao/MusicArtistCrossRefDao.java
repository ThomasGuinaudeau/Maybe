package com.maybe.maybe.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.maybe.maybe.database.entity.MusicArtistCrossRef;

import java.util.List;

@Dao
public interface MusicArtistCrossRefDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<MusicArtistCrossRef> musicArtistCrossRefs);

    @Query("DELETE FROM music_artist_cross_ref WHERE music_id = :music_id")
    void deleteById(long music_id);

    @Query("DELETE FROM music_artist_cross_ref")
    void deleteAll();

    @Query("SELECT artist_id FROM music_artist_cross_ref GROUP BY artist_id")
    List<Long> selectAllArtistsId();
}