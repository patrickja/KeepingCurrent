package com.example.android.keepingcurrent.database;

import android.content.Context;

import androidx.room.Room;

import com.example.android.keepingcurrent.api.NewsAPIService;
import com.example.android.keepingcurrent.service.MaintenanceService;
import com.example.android.keepingcurrent.service.UpdateService;
import com.example.android.keepingcurrent.utilities.PreferenceUtility;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Dependency {
    private static final int NETWORK_CONNECT_TIMEOUT_MILLIS = 10000; // 10 seconds
    private static final int NETWORK_READ_TIMEOUT_MILLIS = 15000; // 15 seconds
    private static final String SERVER_BASE_URL = "http://newsapi.org/v2/";
    private static final String DATABASE_FILE_NAME = "article.db";
    private static final String TAG_ARTICLE_UPDATE_JOB = "articleUpdateJob";
    private static final String TAG_MAINTENANCE_JOB = "maintenanceJob";

    private static final Object LOCK = new Object();
    private static NewsAPIService apiService;
    private static ArticleDatabase database;
    private static ArticleDao articleDao;
    private static boolean scheduled = false;

    public static Repository getRepository(Context context) {
        return Repository.getInstance(context.getApplicationContext());
    }

    public static NewsAPIService getAPIService() {
        if (apiService == null) {
            synchronized (LOCK) {
                OkHttpClient okHttpClient =
                        new OkHttpClient.Builder()
                                .connectTimeout(NETWORK_CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                                .readTimeout(NETWORK_READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                                .retryOnConnectionFailure(true)
                                .build();
                Retrofit retrofit =
                        new Retrofit.Builder()
                                .baseUrl(SERVER_BASE_URL)
                                .client(okHttpClient)
                                .addConverterFactory(GsonConverterFactory.create())
                                .build();
                apiService = retrofit.create(NewsAPIService.class);
            }
        }
        return apiService;
    }

    public static ArticleDao getArticleDao(Context context) {
        if (articleDao == null) {
            if (database == null) database = getDatabase(context);
            articleDao = database.getArticleDao();
        }
        return articleDao;
    }

    public static ArticleMaintenanceDao getMaintenanceDao(Context context) {
        if (database == null) database = getDatabase(context);
        return database.getMaintenanceDao();
    }

    private static ArticleDatabase getDatabase(Context context) {
        if (database == null) {
            synchronized (LOCK) {
                database =
                        Room.databaseBuilder(
                                context.getApplicationContext(), ArticleDatabase.class, DATABASE_FILE_NAME)
                                .build();
            }
        }
        return database;
    }

    public static void scheduleUpdateJob(Context context, boolean onDemand) {
        if (!scheduled || onDemand) {
            FirebaseJobDispatcher jobDispatcher =
                    new FirebaseJobDispatcher(new GooglePlayDriver(context));

            int updateFrequency = PreferenceUtility.getPrefUpdateFrequency(context);
            if (updateFrequency == -1) {
                jobDispatcher.cancel(TAG_ARTICLE_UPDATE_JOB);
            } else {
                Job updateJob =
                        jobDispatcher
                                .newJobBuilder()
                                .setService(UpdateService.class)
                                .setTag(TAG_ARTICLE_UPDATE_JOB)
                                .setRecurring(true)
                                .setLifetime(Lifetime.FOREVER)
                                .setTrigger(Trigger.executionWindow(0, updateFrequency * 60 * 60))
                                .setReplaceCurrent(true)
                                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                                .setConstraints(Constraint.ON_ANY_NETWORK)
                                .build();
                jobDispatcher.mustSchedule(updateJob);
            }

            Job maintenanceJob =
                    jobDispatcher
                            .newJobBuilder()
                            .setService(MaintenanceService.class)
                            .setTag(TAG_MAINTENANCE_JOB)
                            .setRecurring(true)
                            .setLifetime(Lifetime.FOREVER)
                            .setTrigger(Trigger.executionWindow(43200000, 86400000))
                            .setReplaceCurrent(false)
                            .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
                            .build();
            jobDispatcher.mustSchedule(maintenanceJob);

            scheduled = true;
        }
    }
}
