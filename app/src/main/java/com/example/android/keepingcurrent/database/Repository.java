package com.example.android.keepingcurrent.database;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.example.android.keepingcurrent.R;
import com.example.android.keepingcurrent.api.KeepingCurrent;
import com.example.android.keepingcurrent.api.NewsAPIService;
import com.example.android.keepingcurrent.model.Article;
import com.example.android.keepingcurrent.model.ArticleType;
import com.example.android.keepingcurrent.model.NewsResponse;
import com.example.android.keepingcurrent.ui.ArticleBaseActivity;
import com.example.android.keepingcurrent.widget.Widget;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Repository {
    private static final Object LOCK = new Object();
    private static final int PAGE_SIZE = 20;
    private static final String TOTAL_PAGES_TEMPLATE = "total_pages_";
    private static final String CURRENT_PAGE_TEMPLATE = "current_page_";
    private static final String LAST_REFRESH_TEMPLATE = "last_refresh_";
    private static final String LOADING_PAGE_TEMPLATE = "loading_";
    private static final long AUTO_REFRESH_INTERVAL = 120000;
    private static final String TAG = "Repository";

    @SuppressLint("StaticFieldLeak")
    private static Repository mInstance;

    private final Executor executor;
    private ArticleDao articleDao;
    private NewsAPIService apiService;
    private Bundle extras;
    private Context context;
    private String countryCode;

    private Repository(Context context) {
        this.articleDao = Dependency.getArticleDao(context);
        apiService = Dependency.getAPIService();
        this.context = context;
        executor = Executors.newSingleThreadExecutor();
        extras = new Bundle();
        onCountryCodeChanged();
    }

    static Repository getInstance(Context context) {
        if (mInstance == null) {
            synchronized (LOCK) {
                mInstance = new Repository(context.getApplicationContext());
            }
        }
        return mInstance;
    }

    public void onCountryCodeChanged() {
        this.countryCode =
                PreferenceManager.getDefaultSharedPreferences(context)
                        .getString(
                                context.getString(R.string.pref_key_country),
                                context.getString(R.string.pref_default_country));
    }

    public void getTopHeadlines(boolean onDemand) {
        int type = ArticleType.Type.TOP_HEAD;
        if ((onDemand || isAfterInterval(type)) && isNotLoading(type)) {
            getTopHeadlines(1);
        }
    }

    private boolean isNotLoading(int type) {
        return !extras.getBoolean(LOADING_PAGE_TEMPLATE + type, false);
    }

    private boolean isAfterInterval(int type) {
        long lastRefreshed = extras.getLong(LAST_REFRESH_TEMPLATE + type, 0);
        return (System.currentTimeMillis() - lastRefreshed) > AUTO_REFRESH_INTERVAL;
    }

    public void getNextTopHeadlines() {
        if (isNotLoading(ArticleType.Type.TOP_HEAD)) {
            int nextPage = getNextPageNumber(ArticleType.Type.TOP_HEAD);
            if (nextPage != -1) getTopHeadlines(nextPage);
        }
    }

    private int getNextPageNumber(int type) {
        int totalPages = extras.getInt(TOTAL_PAGES_TEMPLATE + type, 1);
        int currentPage = extras.getInt(CURRENT_PAGE_TEMPLATE + type, 1);

        if (currentPage < totalPages) {
            return ++currentPage;
        } else {
            return -1;
        }
    }

    private void getTopHeadlines(int pageNumber) {
        extras.putBoolean(LOADING_PAGE_TEMPLATE + ArticleType.Type.TOP_HEAD, true);
        sendBroadcast(ArticleBaseActivity.EVENT_LOADING, ArticleType.Type.TOP_HEAD);

        apiService
                .getTopHeadlines(countryCode, pageNumber, context.getString(R.string.NEWS_API_KEY))
                .enqueue(
                        new Callback<NewsResponse>() {
                            @Override
                            public void onResponse(
                                    @NonNull Call<NewsResponse> call, @NonNull Response<NewsResponse> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    NewsResponse newsResponse = response.body();

                                    storeTotalPages(newsResponse.getTotalResults(), ArticleType.Type.TOP_HEAD);
                                    storeCurrentPage(call.request().url().toString(), ArticleType.Type.TOP_HEAD);

                                    if (newsResponse.getArticles() != null
                                            && newsResponse.getStatus().equalsIgnoreCase("ok")) {
                                        insertAllAsync(newsResponse.getArticles(), ArticleType.Type.TOP_HEAD);

                                    } else {
                                        Log.d(TAG, "Invalid or null response from server");
                                        sendBroadcast(ArticleBaseActivity.EVENT_LOAD_FAILED, ArticleType.Type.TOP_HEAD);
                                    }
                                } else {
                                    Log.d(TAG, "Unsuccessful response");
                                    sendBroadcast(ArticleBaseActivity.EVENT_LOAD_FAILED, ArticleType.Type.TOP_HEAD);
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<NewsResponse> call, @NonNull Throwable t) {
                                Log.d(TAG, "Error occurred during article request");
                                sendBroadcast(ArticleBaseActivity.EVENT_LOAD_FAILED, ArticleType.Type.TOP_HEAD);
                            }
                        });
    }

    private void storeCurrentPage(String s, int type) {
        String[] queries = s.substring(s.lastIndexOf("/")).split("&");
        for (String query : queries) {
            if (query.contains("page")) {
                int page = Integer.valueOf(query.split("=")[1].trim());
                extras.putInt(CURRENT_PAGE_TEMPLATE + type, page);
                if (page == 1) {
                    extras.putLong(LAST_REFRESH_TEMPLATE + type, System.currentTimeMillis());
                }
                break;
            }
        }
    }

    public void getArticlesByCategory(int type, boolean onDemand) {
        if ((onDemand || isAfterInterval(type)) && isNotLoading(type)) {
            getArticlesByCategory(type, 1);
        }
    }

    public void getNextArticleByCategory(int type) {
        if (isNotLoading(type)) {
            int nextPage = getNextPageNumber(type);
            if (nextPage != -1) getArticlesByCategory(type, nextPage);
        }
    }

    private void getArticlesByCategory(int type, int pageNumber) {
        extras.putBoolean(LOADING_PAGE_TEMPLATE + type, true);
        sendBroadcast(ArticleBaseActivity.EVENT_LOADING, type);

        apiService
                .getArticlesByCategory(
                        countryCode,
                        pageNumber,
                        context.getString(R.string.NEWS_API_KEY),
                        ArticleType.Type.getName(type))
                .enqueue(
                        new Callback<NewsResponse>() {
                            @Override
                            public void onResponse(
                                    @NonNull Call<NewsResponse> call, @NonNull Response<NewsResponse> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    NewsResponse newsResponse = response.body();

                                    storeTotalPages(newsResponse.getTotalResults(), type);
                                    storeCurrentPage(call.request().url().toString(), type);

                                    if (newsResponse.getArticles() != null
                                            && newsResponse.getStatus().equalsIgnoreCase("ok")) {
                                        insertAllAsync(newsResponse.getArticles(), type);
                                        return;
                                    } else {
                                        Log.d(TAG, "Invalid or null response from server\"");
                                        return;
                                    }
                                }
                                Log.d(TAG, "Unsuccessful response");
                                sendBroadcast(ArticleBaseActivity.EVENT_LOAD_FAILED, type);
                            }

                            @Override
                            public void onFailure(@NonNull Call<NewsResponse> call, @NonNull Throwable t) {
                                Log.d(TAG, "Error during requesting articles");
                                sendBroadcast(ArticleBaseActivity.EVENT_LOAD_FAILED, type);
                            }
                        });
    }

    private void storeTotalPages(int responseCount, int type) {
        int totalPages;
        if (responseCount < 20) totalPages = 1;
        else totalPages = responseCount / PAGE_SIZE;

        extras.putInt(TOTAL_PAGES_TEMPLATE + type, totalPages);
    }

    private void insertAllAsync(Article[] articles, int type) {
        executor.execute(
                () -> {
                    List<Article> insertedArticles = insertAll(articles, type);
                    if (insertedArticles != null && insertedArticles.size() > 0) {
                        sendBroadcast(ArticleBaseActivity.EVENT_LOAD_FINISHED, type);
                    } else {
                        sendBroadcast(ArticleBaseActivity.EVENT_LOAD_EMPTY, type);
                    }
                });
    }

    public List<Article> insertAll(Article[] articles, int type) {
        ArrayList<Article> insertedArticles = new ArrayList<>();
        int insertCount = 0;
        for (Article article : articles) {
            try {
                long id = articleDao.insert(article);
                if (id == -1) {
                    id =
                            articleDao.getArticleId(
                                    article.getTitle(), article.getUrl(), article.getPublishedAt());
                } else {
                    insertCount++;
                    article.setId((int) id);
                    insertedArticles.add(article);
                }
                ArticleType articleType = new ArticleType();
                articleType.setId((int) id);
                articleType.setType(type);
                articleDao.insert(articleType);
            } catch (Exception e) {
                e.printStackTrace();
                Tracker t = ((KeepingCurrent) context).getDefaultTracker();
                t.send(
                        new HitBuilders.ExceptionBuilder()
                                .setDescription(
                                        new StandardExceptionParser(context, null)
                                                .getDescription(Thread.currentThread().getName(), e))
                                .setFatal(false)
                                .build());
            }
        }
        if (insertCount > 0 && type == ArticleType.Type.TOP_HEAD) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(context, Widget.class));
            if (ids.length > 0) {
                Intent intent = new Intent(context, Widget.class);
                intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                context.sendBroadcast(intent);
            }
            extras.putBoolean(LOADING_PAGE_TEMPLATE + type, false);
            return insertedArticles;
        }
        extras.putBoolean(LOADING_PAGE_TEMPLATE + type, false);
        return null;
    }

    private void sendBroadcast(String event, int type) {
        Intent intent = new Intent(event);
        intent.putExtra(ArticleBaseActivity.EXTRA_EVENT_ARTICLE_TYPE, type);
        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intent);
    }
}
