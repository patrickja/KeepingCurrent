package com.example.android.keepingcurrent.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class InfiniteScrollListener extends RecyclerView.OnScrollListener {
    private LoadNextPageCallback loadNextPageCallback;
    private LinearLayoutManager layoutManager;

    public InfiniteScrollListener(LinearLayoutManager layoutManager, LoadNextPageCallback callback) {
        this.layoutManager = layoutManager;
        loadNextPageCallback = callback;
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        if (layoutManager.getChildCount() + layoutManager.findLastVisibleItemPosition()
                >= layoutManager.getItemCount()) {
            if (loadNextPageCallback != null) loadNextPageCallback.loadNextPage();
        }
    }

    public interface LoadNextPageCallback {
        void loadNextPage();
    }
}
