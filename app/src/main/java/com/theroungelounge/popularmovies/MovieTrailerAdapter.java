package com.theroungelounge.popularmovies;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Rounge on 1/21/2017.
 */

public class MovieTrailerAdapter extends ArrayAdapter<MovieTrailer> {

    private final String LOG_TAG = MovieTrailerAdapter.class.getSimpleName();
    private final String YOUTUBE_BASE_URL = "http://www.youtube.com/watch?v=";
    private Activity context;

    public MovieTrailerAdapter(Activity context, List<MovieTrailer> trailerList) {
        super(context, 0, trailerList);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final MovieTrailer trailer = getItem(position);

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.list_item_movie_trailer, parent, false);
        }

        ImageButton playButton = (ImageButton)convertView.findViewById(R.id.list_item_movie_trailer_button);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent playIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(YOUTUBE_BASE_URL + trailer.getKey()));
                context.startActivity(playIntent);
            }
        });
        TextView trailerNameView = (TextView) convertView.findViewById(R.id.list_item_movie_trailer_textview);
        trailerNameView.setText(trailer.getName());

        return convertView;
    }
}
