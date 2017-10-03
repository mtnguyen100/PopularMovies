package com.theroungelounge.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    private MovieDataAdapter movieDataAdapter;
    private ImageView tmdbLogoImageView;
    private SharedPreferences sharedPref;
    private GridView gridView;
    private MovieData[] movieData;
    private String[] movieIds;
    private static String sortOrder = "popular";
    private static int pageNum = 1;
    private static int maxNumPages;

    public MainActivityFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case R.id.action_next_page:
                new FetchMovieTask().execute(sortOrder, String.valueOf((pageNum == maxNumPages) ? pageNum : ++pageNum));
                return true;
            case R.id.action_prev_page:
                new FetchMovieTask().execute(sortOrder, String.valueOf((pageNum < 2) ? pageNum : --pageNum));
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        movieDataAdapter = new MovieDataAdapter(getActivity(), new ArrayList<MovieData>());

        Log.v("onCreateView", "MainActivityFragment onCreateView");
        updateOrder();
        if(sortOrder.equalsIgnoreCase("favorites")) {
            addFavorites();
        } else {
            new FetchMovieTask().execute(sortOrder, String.valueOf(pageNum));
        }
        gridView = (GridView) rootView.findViewById(R.id.gridview_movies);
        gridView.setAdapter(movieDataAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
                Serializable item = movieDataAdapter.getItem(position);
                detailIntent.putExtra(Intent.EXTRA_TEXT, item);
                detailIntent.putExtra("MOVIE_ID", movieIds[position]);
                startActivity(detailIntent);
            }
        });
        Picasso.with(getActivity()).setLoggingEnabled(true);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.v("onStart", "MainActivityFragment onStart");
        updateOrder();
        if(sortOrder.equalsIgnoreCase("favorites")) {
            addFavorites();
        } else {
            new FetchMovieTask().execute(sortOrder, String.valueOf(pageNum));
        }
    }

    /* returns true if order was changed, false otherwise. */
    private boolean updateOrder() {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String currOrder = sharedPref.getString(getString(R.string.pref_sort_order_key),
                getString(R.string.pref_order_popular));
        currOrder = (currOrder.equalsIgnoreCase("Top Rated")) ? "top_rated" :
                (currOrder.equalsIgnoreCase("Popular")) ? "popular" : "favorites";
        if(!sortOrder.equalsIgnoreCase(currOrder)) {
            sortOrder = currOrder;
            pageNum = 1;
            return true;
        }
        return false;
    }

    private void addFavorites() {
        Cursor favoritesCursor = getActivity().getContentResolver().query(
                FavoritesContract.FavoritesEntry.CONTENT_URI,
                new String[]{FavoritesContract.FavoritesEntry.COLUMN_TITLE,
                        FavoritesContract.FavoritesEntry.COLUMN_VOTE_AVG,
                        FavoritesContract.FavoritesEntry.COLUMN_RELEASE_DATE,
                        FavoritesContract.FavoritesEntry.COLUMN_OVERVIEW,
                        FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH,
                        FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID},
                null,
                null,
                null);
        if(favoritesCursor != null) {
            movieIds = new String[favoritesCursor.getCount()];
            movieData = new MovieData[movieIds.length];
            for(int i = 0; favoritesCursor.moveToNext(); i++) {
                movieData[i] = new MovieData(
                        favoritesCursor.getString(
                                favoritesCursor.getColumnIndex(
                                        FavoritesContract.FavoritesEntry.COLUMN_TITLE)),
                        favoritesCursor.getString(
                                favoritesCursor.getColumnIndex(
                                        FavoritesContract.FavoritesEntry.COLUMN_RELEASE_DATE)),
                        favoritesCursor.getString(
                                favoritesCursor.getColumnIndex(
                                        FavoritesContract.FavoritesEntry.COLUMN_VOTE_AVG)),
                        favoritesCursor.getString(
                                favoritesCursor.getColumnIndex(
                                        FavoritesContract.FavoritesEntry.COLUMN_OVERVIEW)),
                        favoritesCursor.getString(
                                favoritesCursor.getColumnIndex(
                                        FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH)));
                movieIds[i] = favoritesCursor.getString(
                        favoritesCursor.getColumnIndex(
                                FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID));
            }
        }
        movieDataAdapter.clear();
        movieDataAdapter.addAll(movieData);
        try {
            favoritesCursor.close();
        } catch(NullPointerException e) {
            Log.e(LOG_TAG, "favoritesCursor is null.", e);
        }
    }

    /**
     *  This class fetches movieIds from the TMDB database IF the sort order is either
     *  top_rated or popular.
     */
    public class FetchMovieTask extends AsyncTask<String, Void, JSONObject[]> {

        @Override
        protected JSONObject[] doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String movieJsonStr = null;
            // Predefined query paremeters
            // TODO: Edit or delete later
            String language = "en-US";

            try {
                // Methods from TheMovieDB can be found at
                // https://www.themoviedb.org/documentation/api
                final String CATEGORY_PARAM = params[0]; //popular or top_rated movie
                final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/movie/" + CATEGORY_PARAM + "?";
                final String API_KEY_PARAM = "api_key";
                final String LANGUAGE_PARAM = "language";
                final String PAGE_PARAM = "page";

                Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY_PARAM, "5368fbb59a60ac5f8107f3b3d68ef554") //API Key
                        .appendQueryParameter(LANGUAGE_PARAM, language) //Optional?
                        .appendQueryParameter(PAGE_PARAM, params[1]) //Displays the first page of movieIds
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
                movieJsonStr = buffer.toString();
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
            JSONObject[] movieList = null;
            try {
                movieList = getMovieDataFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }
            return movieList;
        }

        @Override
        protected void onPostExecute(JSONObject[] jsonObjects) {
            if(jsonObjects != null) {
                movieDataAdapter.clear();
                movieDataAdapter.addAll(movieData);
            }
        }

        /**
         * Takes in a JSON-formatted String from TheMovieDB and returns an array of
         * JSONObjects, each with movie data (title, plot synopsis, etc.)
         * @param movieJsonStr JSON-formatted String from TheMovieDB
         * @return JSONObject array of movieIds
         * @throws JSONException
         */
        private JSONObject[] getMovieDataFromJson(String movieJsonStr) throws JSONException {
            final String TMDB_RESULTS = "results";
            final String TMDB_TOTAL_PAGES = "total_pages";
            final String TMDB_TITLE = "title";
            final String TMDB_RELEASE_DATE = "release_date";
            final String TMDB_POSTER_PATH = "poster_path"; //parse this to get the path to poster image
            final String TMDB_VOTE_AVERAGE = "vote_average";
            final String TMDB_OVERVIEW = "overview";
            final String TMDB_MOVIE_ID = "id";

            JSONObject movieJson = new JSONObject(movieJsonStr);

            maxNumPages = Integer.parseInt(movieJson.getString(TMDB_TOTAL_PAGES));

            JSONArray movieResults = movieJson.getJSONArray(TMDB_RESULTS);
            movieData = new MovieData[movieResults.length()];
            movieIds = new String[movieData.length];

            JSONObject[] resultMovies = new JSONObject[movieResults.length()];
            for(int i = 0; i < resultMovies.length; i++) {
                JSONObject movie = movieResults.getJSONObject(i);
                resultMovies[i] = movie;
                movieData[i] = new MovieData(resultMovies[i].getString(TMDB_TITLE),
                        resultMovies[i].getString(TMDB_RELEASE_DATE),
                        resultMovies[i].getString(TMDB_VOTE_AVERAGE),
                        resultMovies[i].getString(TMDB_OVERVIEW),
                        resultMovies[i].getString(TMDB_POSTER_PATH));
                movieIds[i] = resultMovies[i].getString(TMDB_MOVIE_ID);
            }
            return resultMovies;
        }
    }
}