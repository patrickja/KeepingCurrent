package com.example.android.keepingcurrent.api;

import android.app.Application;

import com.example.android.keepingcurrent.R;
import com.example.android.keepingcurrent.database.Dependency;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

public class KeepingCurrent extends Application {

    private static GoogleAnalytics sAnalytics;
    private static Tracker sTracker;

    @Override
    public void onCreate() {
        super.onCreate();
        MobileAds.initialize(this, getString(R.string.GOOGLE_ADS_ID));
        Dependency.getAPIService();
        Dependency.getArticleDao(this);
        sAnalytics = GoogleAnalytics.getInstance(this);
    }

    public synchronized Tracker getDefaultTracker() {
        if (sTracker == null) {
            sTracker = sAnalytics.newTracker(R.xml.global_tracker);
            sTracker.enableAutoActivityTracking(true);
        }
        return sTracker;
    }
}
