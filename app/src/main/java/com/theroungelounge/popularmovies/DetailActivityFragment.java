package com.theroungelounge.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    private final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    /* private final String KEY_TITLE = "TITLE_KEY";
    private final String KEY_RELEASE_DATE = "RELEASE_DATE_KEY";
    private final String KEY_VOTE_AVERAGE = "VOTE_AVERAGE_KEY";
    private final String KEY_OVERVIEW = "OVERVIEW_KEY";
    private final String KEY_POSTER_PATH = "POSTER_PATH_KEY"; */

    private LinearLayout movieTrailerLinearLayout;
    private MovieData movieData;
    private MovieTrailerAdapter movieTrailerAdapter;

    private static String movieId;
    private static MovieTrailer[] trailers; /*static list of trailers to add to adapter to reduce
                                              internet usage. */
    private static String title, releaseDate, voteAvg, overview, posterPath;

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        Intent detailIntent = getActivity().getIntent();
        movieTrailerLinearLayout = (LinearLayout) rootView.
                findViewById(R.id.detail_movie_trailers_linearlayout);
        movieTrailerAdapter = new MovieTrailerAdapter(getActivity(), new ArrayList<MovieTrailer>());
        if(detailIntent != null && detailIntent.hasExtra(Intent.EXTRA_TEXT)) {
            movieId = detailIntent.getStringExtra("MOVIE_ID");
            new FetchMovieTask().execute(movieId);
            movieData = (MovieData) detailIntent.getSerializableExtra(Intent.EXTRA_TEXT);
            title = movieData.getTitle();
            voteAvg = movieData.getVote_average();
            releaseDate = movieData.getRelease_date();
            overview = movieData.getOverview();
            posterPath = movieData.getPath();
        } else {
            for(int i = 0; i < trailers.length; i++) {
                movieTrailerAdapter.add(trailers[i]);
                movieTrailerLinearLayout.addView(movieTrailerAdapter.getView(i, null, null));
            }
        }
        ((TextView) rootView.findViewById(R.id.detail_title))
                .setText(title);
        ((TextView) rootView.findViewById(R.id.detail_release_date))
                .setText(("Release date: " + releaseDate));
        ((TextView) rootView.findViewById(R.id.detail_vote_average))
                .setText(("Rating: " + voteAvg));
        ((TextView) rootView.findViewById(R.id.detail_overview))
                .setText(("Overview: " + overview));
        Picasso.with(getActivity()).load("http://image.tmdb.org/t/p/w342/" + posterPath)
                .into((ImageView)rootView.findViewById(R.id.detail_poster));
        rootView.findViewById(R.id.detail_movie_review_textview).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent reviewIntent = new Intent(getActivity(), ReviewsActivity.class);
                        reviewIntent.putExtra(Intent.EXTRA_TEXT, movieId);
                        startActivity(reviewIntent);
                    }
                }
        );
        final ToggleButton favoriteButton =
                (ToggleButton) rootView.findViewById(R.id.detail_movie_favorite_button);
        //Checking the status of the database to determine status of ToggleButton
        Cursor favoriteCursor = getActivity().getContentResolver().query(
                FavoritesContract.FavoritesEntry.CONTENT_URI,
                new String[]{FavoritesContract.FavoritesEntry._ID},
                FavoritesContract.FavoritesEntry.COLUMN_TITLE + " = ?",
                new String[]{title},
                null);
        if(favoriteCursor != null) {
            if (favoriteCursor.moveToFirst()) {
                favoriteButton.setChecked(true);
            }
            favoriteCursor.close();
        }
        favoriteButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Cursor favoriteCursor = getActivity().getContentResolver().query(
                        FavoritesContract.FavoritesEntry.CONTENT_URI,
                        new String[]{FavoritesContract.FavoritesEntry._ID},
                        FavoritesContract.FavoritesEntry.COLUMN_TITLE + " = ?",
                        new String[]{title},
                        null);
                if(favoriteCursor != null) {
                    if(isChecked && !favoriteCursor.moveToFirst()) {
                        //if movieId does not exist in the favorites database, insert it
                        ContentValues favoriteValues = new ContentValues();
                        favoriteValues.put(FavoritesContract.FavoritesEntry.COLUMN_TITLE,
                                title);
                        favoriteValues.put(FavoritesContract.FavoritesEntry.COLUMN_VOTE_AVG,
                                voteAvg);
                        favoriteValues.put(FavoritesContract.FavoritesEntry.COLUMN_RELEASE_DATE,
                                releaseDate);
                        favoriteValues.put(FavoritesContract.FavoritesEntry.COLUMN_OVERVIEW,
                                overview);
                        favoriteValues.put(FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH,
                                posterPath);
                        favoriteValues.put(FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID,
                                movieId);
                        getActivity().getContentResolver().insert(
                                FavoritesContract.FavoritesEntry.CONTENT_URI, favoriteValues);
                    } else if(favoriteCursor.moveToFirst()){
                        //if movieId was unfavorited, and exists in the favorites database, delete
                        getActivity().getContentResolver()
                                .delete(FavoritesContract.FavoritesEntry.CONTENT_URI,
                                        FavoritesContract.FavoritesEntry.COLUMN_TITLE + " = ?",
                                        new String[]{title});
                    }
                    favoriteCursor.close();
                }
            }
        });
        return rootView;
    }

    // TODO: Either get this to work or read Android doc about Providing Proper Back Navigation
    /* @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_TITLE, movieData.getTitle());
        outState.putString(KEY_RELEASE_DATE, movieData.getRelease_date());
        outState.putString(KEY_VOTE_AVERAGE, movieData.getVote_average());
        outState.putString(KEY_OVERVIEW, movieData.getOverview());
        outState.putString(KEY_POSTER_PATH, movieData.getPath());
    } */

    // TODO: Figure out how this goes along with onSaveInstanceState to save the state of all views
    /* @Override
    public void onStart() {
        super.onStart();
        new FetchMovieTask().execute(getMovieId(movieId));
    } */

    /**
     * This class fetches trailers using the TMDB API.
     */
    private class FetchMovieTask extends AsyncTask<String, Void, JSONObject[]> {

        @Override
        protected JSONObject[] doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String trailerJsonStr = null;
            // Predefined query paremeters
            String language = "en-US";

            try {
                // Methods from TheMovieDB can be found at
                // https://www.themoviedb.org/documentation/api
                final String ID_PARAM = params[0]; //popular or top_rated movieId
                final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/movie/" + ID_PARAM + "?";
                final String API_KEY_PARAM = "api_key";
                final String LANGUAGE_PARAM = "language";
                final String APPEND_RESPONSE_PARAM = "append_to_response";

                Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY_PARAM, "5368fbb59a60ac5f8107f3b3d68ef554") //API Key
                        .appendQueryParameter(LANGUAGE_PARAM, language) //Optional?
                        .appendQueryParameter(APPEND_RESPONSE_PARAM, "videos")
                        .build();

                URL url = new URL(builtUri.toString());


                // Create the request to TheMovieDB, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                trailerJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error: Unable to acquire movieId data.", e);
                // If the code didn't successfully get the movieId data, there's no point in attempting
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            JSONObject[] trailerList = null;
            try {
                trailerList = getMovieTrailersFromJson(trailerJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }
            return trailerList;
        }

        @Override
        protected void onPostExecute(JSONObject[] jsonObjects) {
            final String TMDB_TRAILER_KEY = "key";
            final String TMDB_NAME = "name";

            if(jsonObjects != null) {
                try {
                    trailers = new MovieTrailer[jsonObjects.length];
                    for (int i = 0; i < jsonObjects.length; i++) {
                        trailers[i] = new MovieTrailer(jsonObjects[i].getString(TMDB_NAME),
                                jsonObjects[i].getString(TMDB_TRAILER_KEY));
                        movieTrailerAdapter.add(trailers[i]);
                        movieTrailerLinearLayout.addView(movieTrailerAdapter.getView(i, null, null));
                    }
                } catch(JSONException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                }
            }
        }

        private JSONObject[] getMovieTrailersFromJson(String trailerJsonStr) throws JSONException {
            final String TMDB_VIDEOS = "videos";
            final String TMDB_RESULTS = "results";

            JSONObject trailerJson = new JSONObject(trailerJsonStr);
            JSONObject trailerVideos = trailerJson.getJSONObject(TMDB_VIDEOS);
            JSONArray trailerVideoResults = trailerVideos.getJSONArray(TMDB_RESULTS);

            JSONObject[] results = new JSONObject[trailerVideoResults.length()];
            for (int i = 0; i < results.length; i++) {
                results[i] = trailerVideoResults.getJSONObject(i);
            }
            return results;
        }
    }
}