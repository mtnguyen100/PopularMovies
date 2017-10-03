package com.theroungelounge.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

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
public class ReviewsActivityFragment extends Fragment {

    private final String LOG_TAG = ReviewsActivityFragment.class.getSimpleName();
    private MovieReviewAdapter movieReviewAdapter;
    private ListView reviewsListView;
    private static String movieId;
    private String[][] reviews;

    public ReviewsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reviews, container, false);
        Intent reviewsIntent = getActivity().getIntent();

        movieReviewAdapter = new MovieReviewAdapter(getActivity(), new ArrayList<MovieReview>());
        reviewsListView = (ListView) rootView.findViewById(R.id.content_reviews);
        if(reviewsIntent.hasExtra(Intent.EXTRA_TEXT)) {
            movieId = getActivity().getIntent().getStringExtra(Intent.EXTRA_TEXT);
        }

        new FetchReviewTask().execute(movieId);

        reviewsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent reviewFocusIntent = new Intent(getActivity(), ReviewFocusActivity.class);
                reviewFocusIntent.putExtra(Intent.EXTRA_TEXT, reviews[position]);
                startActivity(reviewFocusIntent);
            }
        });
        return rootView;
    }

    private class FetchReviewTask extends AsyncTask<String, Void, String[][]> {
        @Override
        protected String[][] doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String reviewJsonStr = null;
            // Predefined query paremeters
            String language = "en-US";

            try {
                // Methods from TheMovieDB can be found at
                // https://www.themoviedb.org/documentation/api
                final String ID_PARAM = params[0]; //popular or top_rated movie
                final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/movie/" + ID_PARAM + "?";
                final String API_KEY_PARAM = "api_key";
                final String APPEND_RESPONSE_PARAM = "append_to_response";

                Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY_PARAM, "5368fbb59a60ac5f8107f3b3d68ef554") //API Key
                        .appendQueryParameter(APPEND_RESPONSE_PARAM, "reviews")
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
                reviewJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error: Unable to acquire movie data.", e);
                // If the code didn't successfully get the movie data, there's no point in attempting
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
            String[][] trailerList = null;
            try {
                trailerList = getMovieReviewsFromJson(reviewJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }
            return trailerList;
        }

        @Override
        protected void onPostExecute(String[][] strings) {
            if (strings != null && strings.length != 0) {
                for (int i = 0; i < strings.length; i++) {
                    MovieReview movieReview;
                    if(strings[i][1].length() > 500) {
                        movieReview = new MovieReview(
                                strings[i][0], strings[i][1].substring(0, 500) + "...");
                    } else {
                        movieReview = new MovieReview(strings[i][0], strings[i][1]);
                    }
                    movieReviewAdapter.add(movieReview);
                }
            } else {
                movieReviewAdapter.add(new MovieReview("No reviews available.", ""));
            }
            reviewsListView.setAdapter(movieReviewAdapter);
            reviews = strings;
        }

        private String[][] getMovieReviewsFromJson(String reviewJsonStr) throws JSONException {
            final String TMDB_RESULTS = "results";
            final String TMDB_REVIEWS = "reviews";
            final String TMDB_AUTHOR = "author";
            final String TMDB_CONTENT = "content";

            JSONObject reviewJson = new JSONObject(reviewJsonStr);
            JSONObject reviewReviews = reviewJson.getJSONObject(TMDB_REVIEWS);
            JSONArray reviews = reviewReviews.getJSONArray(TMDB_RESULTS);

            String[][] reviewContent = new String[reviews.length()][2];
            for (int i = 0; i < reviews.length(); i++) {
                reviewContent[i][0] = reviews.getJSONObject(i).getString(TMDB_AUTHOR);
                reviewContent[i][1] = reviews.getJSONObject(i).getString(TMDB_CONTENT);
            }
            return reviewContent;
        }
    }
}
