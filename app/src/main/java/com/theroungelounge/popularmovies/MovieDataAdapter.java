package com.theroungelounge.popularmovies;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Rounge on 1/16/2017.
 */

public class MovieDataAdapter extends ArrayAdapter<MovieData> {
    private final String LOG_TAG = MovieDataAdapter.class.getSimpleName();
    private Activity context;

    public MovieDataAdapter(Activity context, List<MovieData> posterList) {
        super(context, 0, posterList);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MovieData movieData = getItem(position);

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.list_item_poster_image, parent, false);
        }

        ImageView posterView = (ImageView) convertView.findViewById(R.id.list_item_poster_image_imageview);
        Picasso.with(context)
                .load("http://image.tmdb.org/t/p/w342/" + movieData.getPath())
                .into(posterView);

        return convertView;
    }
}
