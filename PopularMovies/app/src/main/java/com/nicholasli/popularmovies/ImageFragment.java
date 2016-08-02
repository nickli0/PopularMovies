package com.nicholasli.popularmovies;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Movie;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ImageFragment extends Fragment {

    ImageAdapter mImageAdapter;

    public ImageFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_setting) {
            updateMovie();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mImageAdapter = new ImageAdapter(getActivity());

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        GridView gridView = (GridView) rootView.findViewById(R.id.list_item);
        gridView.setAdapter(mImageAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Movies image = mImageAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Movies.EXTRA_MOVIE, image.toBundle());
                startActivity(intent);
            }
        });
        return rootView;
    }

    private void updateMovie() {
        FetchMovieTask movieTask = new FetchMovieTask();
        movieTask.execute();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovie();
    }

    public class FetchMovieTask extends AsyncTask<Void, Void, List<String>> {

        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

        @Override
        protected List<String> doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String movieJsonStr = null;

            final String BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
            final String SORTING_PARAM = "sort_by";
            final String popularity = "popularity.desc";
            final String rating = "vote_average.desc";
            final String APPID_PARAM = "api_key";

            String builtUriPopularity = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(SORTING_PARAM, popularity)
                    .appendQueryParameter(APPID_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                    .build().toString();
            String builtUriRating = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(SORTING_PARAM, rating)
                    .appendQueryParameter(APPID_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                    .build().toString();

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String sortingType = sharedPrefs.getString(
                    getString(R.string.pref_sorting_popularity), getString(R.string.pref_sorting_rating));


            if(sortingType.equals(getString(R.string.pref_sorting_popularity))){
                builtUriRating = null;
            } else if (!sortingType.equals(getString(R.string.pref_sorting_rating))){
                builtUriPopularity = null;
            }

            try {
                URL url = new URL(builtUriPopularity + builtUriRating);

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

                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                movieJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
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
            try {
                return getMovieDataFromJson(movieJsonStr);
            } catch (JSONException j) {
                Log.e(LOG_TAG, "JSON Error", j);
            }
            return null;
        }



        private List<String> getMovieDataFromJson(String movieJsonStr)
                throws JSONException {
            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray("results");
            List<String> urls = new ArrayList<>();
            for (int i = 0; i < movieArray.length(); i++) {
                JSONObject movie = movieArray.getJSONObject(i);
                urls.add("http://image.tmdb.org/t/p/w185" + movie.getString("poster_path"));
            }
            return urls;
        }

        @Override
        protected void onPostExecute(List<String> strings) {
            mImageAdapter.replace(strings);
        }
    }


    public class Movies{

        public final long id;
        public final String title;
        public final String overview;
        public final String poster_path;
        public final double vote_average;
        public final long vote_count;
        public final String release_date;

        public Movies(long id, String title, String overview, String poster_path,
                     double vote_average, long vote_count, String release_date){
            this.id = id;
            this.title = title;
            this.overview = overview;
            this.poster_path = poster_path;
            this.vote_average = vote_average;
            this.vote_count = vote_count;
            this.release_date = release_date;
        }

        public static final String EXTRA_MOVIE = "com.nicholasli.popularmovies.EXTRA_MOVIE";
        public static final String KEY_ID = "id";
        public static final String KEY_TITLE = "title";
        public static final String KEY_OVERVIEW = "overview";
        public static final String KEY_POSTER_PATH = "poster_path";
        public static final String KEY_VOTE_AVERAGE = "vote_average";
        public static final String KEY_VOTE_COUNT = "vote_count";
        public static final String KEY_RELEASE_DATE = "release_date";

        public Movies(Bundle bundle){
            this(
                    bundle.getLong(KEY_ID),
                    bundle.getString(KEY_TITLE),
                    bundle.getString(KEY_OVERVIEW),
                    bundle.getString(KEY_POSTER_PATH),
                    bundle.getDouble(KEY_VOTE_AVERAGE),
                    bundle.getLong(KEY_VOTE_COUNT),
                    bundle.getString(KEY_RELEASE_DATE)
            );
        }

        public String getRating(){
            return vote_average + " / 10";
        }

        public Bundle toBundle(){
            Bundle bundle = new Bundle();
            bundle.putLong(KEY_ID, id);
            bundle.putString(KEY_TITLE, title);
            bundle.putString(KEY_OVERVIEW, overview);
            bundle.putString(KEY_POSTER_PATH, poster_path);
            bundle.putDouble(KEY_VOTE_AVERAGE, vote_average);
            bundle.putLong(KEY_VOTE_COUNT, vote_count);
            bundle.putString(KEY_RELEASE_DATE, release_date);
            return bundle;
        }

        public Movies fromJson(JSONObject jsonObject) throws JSONException {
            return new Movies(
                    jsonObject.getLong(KEY_ID),
                    jsonObject.getString(KEY_TITLE),
                    jsonObject.getString(KEY_OVERVIEW),
                    jsonObject.getString(KEY_POSTER_PATH),
                    jsonObject.getDouble(KEY_VOTE_AVERAGE),
                    jsonObject.getLong(KEY_VOTE_COUNT),
                    jsonObject.getString(KEY_RELEASE_DATE));
        }

        public Uri posterUri() {

            Uri builtUri = Uri.parse("http://image.tmdb.org/t/p/w185").buildUpon()
                    .appendEncodedPath(poster_path)
                    .build();
            return builtUri;
        }

    }


    public class ImageAdapter extends BaseAdapter {
        private final String LOG_TAG = ImageAdapter.class.getSimpleName();
        private final Context context;
        private final ArrayList<Movies> urls = new ArrayList<>();

        public ImageAdapter(Context context) {
            this.context = context;
            Collections.addAll(urls, poster_path);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new ImageView(context);
            }
            ImageView imageView = (ImageView) convertView;

            Picasso.with(context).load(getItem(position).posterUri()).into(imageView);

            return convertView;
        }

        @Override
        public int getCount() {
            return urls.size();
        }

        @Override
        public Movies getItem(int position) {
            return urls.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
        public void replace(List<String> urls) {
            this.urls.clear();
            this.urls.addAll(urls);
            notifyDataSetChanged();
        }
    }


}

