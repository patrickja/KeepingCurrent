package com.example.android.keepingcurrent.ui;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ShareCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.android.keepingcurrent.R;
import com.example.android.keepingcurrent.adapters.InfiniteScrollListener;
import com.example.android.keepingcurrent.adapters.ListAdapter;
import com.example.android.keepingcurrent.api.KeepingCurrent;
import com.example.android.keepingcurrent.database.Dependency;
import com.example.android.keepingcurrent.model.Article;
import com.example.android.keepingcurrent.model.ArticleType;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends ArticleBaseActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ListAdapter.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener,
        InfiniteScrollListener.LoadNextPageCallback,
        View.OnClickListener {

    private static final String EXTRA_LIST_STATE = "listState";
    public static boolean isAppAlive = false;
    private MainViewModel viewModel;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListAdapter adapter;
    private Snackbar snackbar;
    private RecyclerView recyclerView;
    private boolean isTablet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        isAppAlive = true;
        isTablet = getResources().getBoolean(R.bool.tablet_layout);

        FloatingActionButton fab = findViewById(R.id.fab);
        if (isTablet) {
            fab.setOnClickListener(this);
        } else {
            fab.hide();
        }

        TextView navSubtitle = setupNavigationDrawer(toolbar);
        setupRecyclerView();

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);

        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel
                .getArticleList()
                .observe(
                        this,
                        articles -> {
                            adapter.setArticles(articles);
                            if (articles != null) {
                                navSubtitle.setText(
                                        getString(R.string.navigation_header_subtitle, articles.size()));
                            } else {
                                navSubtitle.setText("");
                            }
                        });

        Dependency.scheduleUpdateJob(getApplicationContext(), false);

        AdView adView = findViewById(R.id.adView);
        AdRequest request = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
        adView.loadAd(request);

        Tracker t = ((KeepingCurrent) getApplication()).getDefaultTracker();
        t.setScreenName("MainActivity");
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager != null) {
            outState.putParcelable(EXTRA_LIST_STATE, layoutManager.onSaveInstanceState());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        LinearLayoutManager layoutManager;
        if (recyclerView != null
                && (layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager()) != null) {
            layoutManager.onRestoreInstanceState(savedInstanceState.getParcelable(EXTRA_LIST_STATE));
        }
    }

    @Override
    void onEvent(String event, int articleType) {
        if (articleType == ArticleType.Type.TOP_HEAD) {
            switch (event) {
                case EVENT_LOADING:
                    swipeRefreshLayout.setRefreshing(true);
                    break;
                case EVENT_LOAD_EMPTY:
                    swipeRefreshLayout.setRefreshing(false);
                    showSnackbar(getString(R.string.text_article_update_empty));
                    break;
                case EVENT_LOAD_FAILED:
                    swipeRefreshLayout.setRefreshing(false);
                    showSnackbar(getString(R.string.text_article_update_fail));
                    break;
                case EVENT_LOAD_FINISHED:
                    swipeRefreshLayout.setRefreshing(false);
                    showSnackbar(getString(R.string.text_article_update_success));
                    break;
            }
        }
    }

    private void showSnackbar(String s) {
        if (snackbar == null) snackbar = Snackbar.make(recyclerView, "", Snackbar.LENGTH_SHORT);
        if (snackbar.isShownOrQueued()) snackbar.dismiss();
        snackbar.setText(s);
        snackbar.show();
    }

    private void setupRecyclerView() {
        adapter = new ListAdapter(null, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        recyclerView = findViewById(R.id.newsList);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new InfiniteScrollListener(layoutManager, this));
        recyclerView.setHasFixedSize(true);
    }

    private TextView setupNavigationDrawer(Toolbar toolbar) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle(
                        this,
                        drawer,
                        toolbar,
                        R.string.navigation_drawer_open,
                        R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().findItem(R.id.nav_top_headlines).setChecked(true);
        return navigationView.getHeaderView(0).findViewById(R.id.nav_title_bottom);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        if (searchManager != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(true);
            searchView.setSubmitButtonEnabled(true);
        }
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_all_articles) {
            Intent intent = new Intent(this, AllArticlesActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_categories) {
            Intent intent = new Intent(this, CategoryActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onItemClick(Article article, ImageView imageView) {
        if (isTablet) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.main_detail_container,
                            DetailFragment.getInstance(article),
                            DetailFragment.FRAGMENT_TAG)
                    .commit();
        } else {
            DetailActivity.launch(this, article, this, imageView);
        }
    }

    @Override
    public void onRefresh() {
        viewModel.loadTopHeadlines(true);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onDestroy() {
        isAppAlive = false;
        super.onDestroy();
    }

    @Override
    public void loadNextPage() {
        viewModel.getNextTopHeadlines();
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
