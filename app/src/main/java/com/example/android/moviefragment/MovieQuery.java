package com.example.android.moviefragment;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import com.example.android.moviefragment.Data.FavoritesDbHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jaren Lynch on 9/25/2016.
 */

public class MovieQuery {

    public static final String LOG_TAG = MovieQuery.class.getSimpleName();
    public static Boolean favoritesCheck = null;
    public FavoritesDbHelper dbHelper;


    private MovieQuery() {}


    public static List<Movie> fetchMovieData(String requestURL, Boolean favorites) {
        URL url = createUrl(requestURL);
        String jsonResponse = null;
        favoritesCheck = favorites;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error closing input stream", e);
        }

        List<Movie> movies = extractMovies(jsonResponse, favoritesCheck);

        return movies;
    }

    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error return code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the earthquake JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.defaultCharset().forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    private static List<Movie> extractMovies(String movieJSON, Boolean favorites) {
        String imageUrl = "http://image.tmdb.org/t/p/w780";

        if (TextUtils.isEmpty(movieJSON)) {
            return null;
        }
        List<Movie> movies = new ArrayList<>();

        try {
            JSONObject jsonRootObject = new JSONObject(movieJSON);
            JSONArray jsonArray = jsonRootObject.optJSONArray("results");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject currentMovie = jsonArray.getJSONObject(i);

                String movieTitle = currentMovie.optString("title");
                String releaseDate = currentMovie.optString("release_date");
                String movieRating = currentMovie.optString("vote_average");
                String moviePlot = currentMovie.optString("overview");
                String movieImage = currentMovie.optString("poster_path");
                movieImage = movieImage.replace("\\", "");
                String movieID = currentMovie.optString("id");

                if (favorites == true) {
                    SQLiteDatabase db;
                }
                // Check if favorites. If true only add movies with corresponding id to array. Dbhelper not work
                // working need to figure out.
                movies.add(new Movie(movieTitle, getBitmapFromURL(imageUrl + movieImage), moviePlot, movieRating,
                        releaseDate, movieID));
            }
        } catch (JSONException e) {
            Log.e("MovieQuery", "Problem parsing the movie json results", e);
        }
        return movies;
    }

    private static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
