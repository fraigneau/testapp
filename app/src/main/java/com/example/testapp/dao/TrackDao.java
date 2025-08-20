package com.example.testapp.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.testapp.entity.TrackEntity;

import java.util.List;

@Dao
public interface TrackDao {

    @Query("SELECT * FROM tracks")
    List<TrackEntity> getAllTracks();

    @Query("SELECT * FROM tracks WHERE uid = :trackId")
    TrackEntity getTrackById(int trackId);

    @Insert
    void insertTrack(TrackEntity track);

    @Insert
    void insertAllTracks(List<TrackEntity> tracks);

    @Delete
    void deleteTrack(TrackEntity track);
}
