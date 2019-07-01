package com.example.android.keepingcurrent.service;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import com.example.android.keepingcurrent.R;
import com.example.android.keepingcurrent.api.KeepingCurrent;
import com.example.android.keepingcurrent.api.NewsAPIService;
import com.example.android.keepingcurrent.database.Dependency;
import com.example.android.keepingcurrent.database.Repository;
import com.example.android.keepingcurrent.model.Article;
import com.example.android.keepingcurrent.model.ArticleType;
import com.example.android.keepingcurrent.model.NewsResponse;
import com.example.android.keepingcurrent.ui.MainActivity;
import com.example.android.keepingcurrent.ui.NewArticleNotification;
import com.example.android.keepingcurrent.utilities.PreferenceUtility;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;

import java.io.IOException;
import java.util.List;

public class UpdateService extends JobService {

    private static AsyncTask<Void, Void, Boolean> updateCheckTask;

    @SuppressLint("StaticFieldLeak")
    @Override
    public boolean onStartJob(JobParameters job) {
        updateCheckTask =
                new AsyncTask<Void, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(Void... voids) {
                        if (!MainActivity.isAppAlive) {
                            NewsAPIService apiService = Dependency.getAPIService();
                            Repository repository = Dependency.getRepository(getApplicationContext());

                            try {
                                NewsResponse response =
                                        apiService
                                                .getTopHeadlines("in", 1, getString(R.string.NEWS_API_KEY))
                                                .execute()
                                                .body();
                                if (response != null && response.getArticles() != null) {
                                    List<Article> insertedArticles =
                                            repository.insertAll(response.getArticles(), ArticleType.Type.TOP_HEAD);
                                    if (insertedArticles != null && insertedArticles.size() > 0) {
                                        if (PreferenceUtility.getPrefEnableNotification(getApplicationContext())) {
                                            NewArticleNotification.notify(
                                                    getApplicationContext(), insertedArticles, insertedArticles.size());
                                        }
                                    }
                                    return true;
                                } else {
                                    return false;
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                Tracker t = ((KeepingCurrent) getApplication()).getDefaultTracker();
                                t.send(
                                        new HitBuilders.ExceptionBuilder()
                                                .setDescription(
                                                        new StandardExceptionParser(getApplicationContext(), null)
                                                                .getDescription(Thread.currentThread().getName(), e))
                                                .setFatal(false)
                                                .build());
                                return false;
                            }
                        } else {
                            return true;
                        }
                    }

                    @Override
                    protected void onPostExecute(Boolean aBoolean) {
                        jobFinished(job, !aBoolean);
                    }
                };

        updateCheckTask.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        if (updateCheckTask != null) {
            updateCheckTask.cancel(true);
            updateCheckTask = null;
            return true;
        }
        return false;
    }
}
