package com.example.android.keepingcurrent.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.android.keepingcurrent.model.Article;
import com.example.android.keepingcurrent.model.ArticleType;

@Database(
        entities = {Article.class, ArticleType.class},
        version = 1,
        exportSchema = false)
public abstract class ArticleDatabase extends RoomDatabase {
    public abstract ArticleDao getArticleDao();

    public abstract ArticleMaintenanceDao getMaintenanceDao();
}
