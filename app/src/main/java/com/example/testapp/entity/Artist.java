package com.example.testapp.entity;

public class Artist {

    private String name;

    // Constructor
    public Artist(String name) {
        this.name = name;
    }

    // Getter et Setter
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Artist{" +
                "name='" + name + '\'' +
                '}';
    }
}
