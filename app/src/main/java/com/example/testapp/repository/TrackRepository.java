package com.example.testapp.repository;


import com.example.testapp.dao.TrackDao;
import com.example.testapp.db.AppDatabase;
import com.example.testapp.entity.TrackEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrackRepository {

    private final TrackDao trackDao;

    public TrackRepository(TrackDao trackDao) {
        this.trackDao = trackDao;
    }

    public List<TrackEntity> getAllTracks() {
        return trackDao.getAllTracks();

    }

    public void insertTracks(List<TrackEntity> tracks) {
        trackDao.insertAllTracks(tracks);
    }

    public TrackEntity createTrack(String title, String artist, String album, int duration) {
        TrackEntity track = new TrackEntity();
        track.title = title;
        track.artist = artist;
        track.album = album;
        track.duration = duration;
        return track;
    }

    List<TrackEntity> tracks = Arrays.asList(
            createTrack("Bohemian Rhapsody", "Queen", "A Night at the Opera", 354),
            createTrack("Billie Jean", "Michael Jackson", "Thriller", 294),
            createTrack("Imagine", "John Lennon", "Imagine", 183),
            createTrack("Smells Like Teen Spirit", "Nirvana", "Nevermind", 301),
            createTrack("Hey Jude", "The Beatles", "Hey Jude", 431),
            createTrack("Like a Rolling Stone", "Bob Dylan", "Highway 61 Revisited", 369),
            createTrack("Hotel California", "Eagles", "Hotel California", 391),
            createTrack("Lose Yourself", "Eminem", "8 Mile", 326),
            createTrack("Wonderwall", "Oasis", "(What's the Story) Morning Glory?", 258),
            createTrack("Stairway to Heaven", "Led Zeppelin", "Led Zeppelin IV", 482)
    );

    public void pushTracksToDatabase(AppDatabase database) {
        TrackDao trackDao = database.trackDao();
        List<TrackEntity> trackList = new ArrayList<>();
        for (TrackEntity track : tracks) {
            if (track != null) {
                trackList.add(track);
            }
        }
        trackDao.insertAllTracks(trackList);
    }

}
