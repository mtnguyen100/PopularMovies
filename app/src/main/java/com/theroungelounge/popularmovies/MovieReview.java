package com.theroungelounge.popularmovies;

/**
 * Created by Rounge on 1/24/2017.
 */

public class MovieReview {
    private String author;
    private String content;

    public MovieReview(String author, String content) {
        this.author = author;
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }
}
