package com.theroungelounge.popularmovies;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Rounge on 1/24/2017.
 */

public class MovieReviewAdapter extends ArrayAdapter<MovieReview> {

    private final String LOG_TAG = MovieReviewAdapter.class.getSimpleName();
    private Activity context;

    public MovieReviewAdapter(Activity context, List<MovieReview> reviewList) {
        super(context, 0 , reviewList);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final MovieReview review = getItem(position);

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.list_item_movie_review, parent, false);
        }

        TextView authorView = (TextView) convertView.findViewById(R.id.list_item_movie_review_author_textview);
        authorView.setText(review.getAuthor());
        TextView contentView = (TextView) convertView.findViewById(R.id.list_item_movie_review_content_textview);
        contentView.setText(review.getContent());

        return convertView;
    }
}