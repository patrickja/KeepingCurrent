package com.example.android.keepingcurrent.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.android.keepingcurrent.R;
import com.example.android.keepingcurrent.adapters.InfiniteScrollListener;
import com.example.android.keepingcurrent.adapters.ListAdapter;
import com.example.android.keepingcurrent.model.Article;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.snackbar.Snackbar;

public class CategoryFragment extends Fragment
        implements ListAdapter.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener,
        InfiniteScrollListener.LoadNextPageCallback {

    private static final String ARG_ARTICLE_TYPE = "article_type";

    private static final String EXTRA_NEWS_LIST_STATE = "newsListState";
    private static final String EXTRA_CONTAINER_ID = "containerId";
    private static final String EXTRA_REFRESHING_STATE = "refreshingState";

    private ListAdapter adapter;
    private MainViewModel viewModel;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private int type = -1;
    private boolean refreshing = false;
    private Snackbar snackbar;
    private boolean visible = false;

    private int containerId = (int) (Math.random() * Integer.MAX_VALUE);

    public CategoryFragment() {
    }

    public static CategoryFragment newInstance(int articleType) {
        CategoryFragment fragment = new CategoryFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ARTICLE_TYPE, articleType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        setUserVisibleHint(false);
        if (getArguments() != null) {
            type = getArguments().getInt(ARG_ARTICLE_TYPE);
        }
        if (state != null) {
            containerId = state.getInt(EXTRA_CONTAINER_ID);
            refreshing = state.getBoolean(EXTRA_REFRESHING_STATE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle state) {
        View rootView = inflater.inflate(R.layout.content_main, container, false);

        View v = rootView.findViewById(R.id.main_detail_container);
        if (v != null) v.setId(containerId);

        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(refreshing);

        setupRecyclerView(rootView);
        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getArticlesByCategory(type).observe(this, adapter::setArticles);

        AdView adView = rootView.findViewById(R.id.adView);
        AdRequest request = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
        adView.loadAd(request);
        return rootView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        RecyclerView.LayoutManager layoutManager;
        if (recyclerView != null && (layoutManager = recyclerView.getLayoutManager()) != null)
            outState.putParcelable(EXTRA_NEWS_LIST_STATE, layoutManager.onSaveInstanceState());
        outState.putInt(EXTRA_CONTAINER_ID, containerId);
        outState.putBoolean(EXTRA_REFRESHING_STATE, refreshing);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager != null && savedInstanceState != null) {
            layoutManager.onRestoreInstanceState(savedInstanceState.getParcelable(EXTRA_NEWS_LIST_STATE));
        }
    }

    private void setupRecyclerView(View rootView) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        adapter = new ListAdapter(null, this);

        recyclerView = rootView.findViewById(R.id.newsList);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnScrollListener(new InfiniteScrollListener(layoutManager, this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(@NonNull Article article, @NonNull ImageView imageView) {
        if (getContext() != null) {
            boolean isTablet = getContext().getResources().getBoolean(R.bool.tablet_layout);
            if (isTablet) {
                if (getActivity() != null) {
                    getActivity()
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(
                                    containerId,
                                    DetailFragment.getInstance(article),
                                    DetailFragment.FRAGMENT_TAG + getTag())
                            .commit();
                }
            } else {
                DetailActivity.launch(getContext(), article, getActivity(), imageView);
            }
        }
    }

    @Override
    public void onRefresh() {
        viewModel.loadArticlesByCategory(type, true);
    }

    @Override
    public void loadNextPage() {
        viewModel.getNextArticlesByCategory(type);
    }

    public void onEvent(String event, int articleType) {
        if (articleType == type) {
            switch (event) {
                case ArticleBaseActivity.EVENT_LOADING:
                    setRefreshing(true);
                    break;

                case ArticleBaseActivity.EVENT_LOAD_EMPTY:
                    setRefreshing(false);
                    showSnackbar(getString(R.string.text_article_update_empty));
                    break;

                case ArticleBaseActivity.EVENT_LOAD_FINISHED:
                    setRefreshing(false);
                    showSnackbar(getString(R.string.text_article_update_success));
                    break;

                case ArticleBaseActivity.EVENT_LOAD_FAILED:
                    setRefreshing(false);
                    showSnackbar(getString(R.string.text_article_update_fail));
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        visible = isVisibleToUser;
    }

    private void setRefreshing(boolean refreshing) {
        this.refreshing = refreshing;
        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(refreshing);
    }

    private void showSnackbar(String msg) {
        if (visible) {
            if (snackbar == null) snackbar = Snackbar.make(recyclerView, "", Snackbar.LENGTH_SHORT);
            if (snackbar.isShownOrQueued()) snackbar.dismiss();
            snackbar.setText(msg);
            snackbar.show();
        }
    }
}
