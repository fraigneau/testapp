package com.example.testapp.entity;

import java.util.List;

public class Track {

    private String name; // Nom de la chanson
    private List<Artist> artists; // Liste des artistes

    // Constructor
    public Track(String name, List<Artist> artists) {
        this.name = name;
        this.artists = artists;
    }

    // Getters et Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Artist> getArtists() {
        return artists;
    }

    public void setArtists(List<Artist> artists) {
        this.artists = artists;
    }

    @Override
    public String toString() {
        StringBuilder artistNames = new StringBuilder();
        for (Artist artist : artists) {
            artistNames.append(artist.getName()).append(", ");
        }
        return "Track{" +
                "name='" + name + '\'' +
                ", artists=" + artistNames.toString() +
                '}';
    }
}
