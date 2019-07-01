package com.example.android.keepingcurrent.service;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.android.keepingcurrent.R;
import com.example.android.keepingcurrent.database.ArticleDao;
import com.example.android.keepingcurrent.database.Dependency;
import com.example.android.keepingcurrent.model.Article;

import java.util.List;

public class WidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new NewsWidgetRemoteViewsFactory(getApplicationContext());
    }

    static class NewsWidgetRemoteViewsFactory implements RemoteViewsFactory {

        List<Article> articles;
        Context context;
        ArticleDao articleDao;

        NewsWidgetRemoteViewsFactory(Context context) {
            articleDao = Dependency.getArticleDao(context);
            this.context = context;
        }

        @Override
        public void onDataSetChanged() {
            articles = articleDao.getHeadlines();
        }

        @Override
        public int getCount() {
            if (articles != null) return articles.size();
            return 0;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_headline);
            remoteViews.setTextViewText(R.id.news_widget_headline, articles.get(position).getTitle());
            remoteViews.setOnClickFillInIntent(
                    R.id.news_widget_headline, new Intent());
            return remoteViews;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return articles.get(position).getId();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDestroy() {
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }
    }
}
