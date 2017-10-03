package com.theroungelounge.popularmovies;

/**
 * Created by Rounge on 1/21/2017.
 */

public class MovieTrailer {
    private String name;
    private String key; //Query parameter to add to youtube link

    public MovieTrailer(String name, String key) {
        this.name = name;
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return key;
    }
}
