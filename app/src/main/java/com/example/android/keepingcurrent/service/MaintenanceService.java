package com.example.android.keepingcurrent.service;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import com.example.android.keepingcurrent.database.ArticleMaintenanceDao;
import com.example.android.keepingcurrent.database.Dependency;
import com.example.android.keepingcurrent.model.ArticleType;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

public class MaintenanceService extends JobService {

    AsyncTask<Void, Void, Void> maintenanceTask;

    @SuppressLint("StaticFieldLeak")
    @Override
    public boolean onStartJob(JobParameters job) {
        maintenanceTask =
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        ArticleMaintenanceDao maintenanceDao =
                                Dependency.getMaintenanceDao(getApplicationContext());
                        for (int type : ArticleType.Type.types) {
                            maintenanceDao.deleteOldArticles(type);
                        }
                        maintenanceDao.deleteOldArticles(ArticleType.Type.TOP_HEAD);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        jobFinished(job, false);
                    }
                };
        maintenanceTask.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        if (maintenanceTask != null) {
            maintenanceTask.cancel(true);
            maintenanceTask = null;
            return true;
        }
        return false;
    }
}
