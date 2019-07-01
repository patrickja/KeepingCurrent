package com.example.android.keepingcurrent.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.app.ShareCompat;

import com.example.android.keepingcurrent.R;
import com.example.android.keepingcurrent.model.Article;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String EXTRA_ARTICLE = "article";
    private Article article;
    private Snackbar snackbar;

    public static void launch(
            @NonNull Context context,
            @NonNull Article article,
            @Nullable Activity activity,
            @Nullable ImageView imageView) {

        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_ARTICLE, article);

        if (activity != null && imageView != null) {
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                            activity, imageView, context.getString(R.string.image_transition_name));

            context.startActivity(intent, options.toBundle());
            return;
        }

        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().hasExtra(EXTRA_ARTICLE)) {
            article = getIntent().getParcelableExtra(EXTRA_ARTICLE);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(
                            R.id.news_detail_activity_container,
                            DetailFragment.getInstance(article),
                            DetailFragment.FRAGMENT_TAG)
                    .commit();
        }

        if (article != null) {
            ImageView imageView = findViewById(R.id.image);
            Picasso.with(this).load(article.getUrlToImage()).fit().centerCrop().into(imageView);

            findViewById(R.id.fab).setOnClickListener(this);

            setTitle(article.getTitle());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            supportFinishAfterTransition();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSnackbar(String s) {
        if (snackbar == null)
            snackbar = Snackbar.make(findViewById(R.id.fab), "", Snackbar.LENGTH_SHORT);
        if (snackbar.isShownOrQueued()) snackbar.dismiss();
        snackbar.setText(s);
        snackbar.show();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab) {
            DetailFragment fragment =
                    (DetailFragment)
                            getSupportFragmentManager().findFragmentByTag(DetailFragment.FRAGMENT_TAG);
            if (fragment != null && fragment.getArticle() != null) {
                Intent shareIntent =
                        ShareCompat.IntentBuilder.from(this)
                                .setType(getString(R.string.share_text_mime_type))
                                .setText(getString(R.string.article_share_template, fragment.getArticle().getUrl()))
                                .setChooserTitle(R.string.share_intent_chooser_title)
                                .getIntent();

                if (getPackageManager().resolveActivity(shareIntent, 0) != null) {
                    startActivity(shareIntent);
                } else {
                    showSnackbar(getString(R.string.share_article_no_app_to_share));
                }
            } else {
                showSnackbar(getString(R.string.share_article_no_article_selected));
            }
        }
    }
}
