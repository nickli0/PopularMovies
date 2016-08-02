package com.nicholasli.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }


    public static class DetailFragment extends Fragment {

        private String mForecast;

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(ImageFragment.Movies.EXTRA_MOVIE)) {
                ImageFragment.Movies movie = new ImageFragment.Movies(intent.getBundleExtra(ImageFragment.Movies.EXTRA_MOVIE));
                ((TextView)findViewById(R.id.textView_title)).setText(movie.title);
                ((TextView)findViewById(R.id.textView_rating)).setText(movie.getRating());
                ((TextView)findViewById(R.id.textView_overview)).setText(movie.overview);
                ((TextView)findViewById(R.id.textView_release_date)).setText(movie.release_date);

                Picasso.with(this)
                        .load()
                        .into((ImageView)findViewById(R.id.imageView_movie_poster));
            return rootView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_setting){
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);


    }
}

