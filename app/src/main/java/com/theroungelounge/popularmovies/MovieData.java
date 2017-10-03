package com.theroungelounge.popularmovies;

import java.io.Serializable;

/**
 * Created by Rounge on 1/16/2017.
 */

public class MovieData implements Serializable {
    private String title;
    private String release_date;
    private String vote_average;
    private String overview;
    private String path;

    public MovieData(String title, String release_date, String vote_average,
                     String overview, String path) {
        this.title = title;
        this.release_date = release_date;
        this.vote_average = vote_average;
        this.overview = overview;
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public String getRelease_date() {
        return release_date;
    }

    public String getVote_average() {
        return vote_average;
    }

    public String getOverview() {
        return overview;
    }

    public String getPath() {
        return path;
    }
}
