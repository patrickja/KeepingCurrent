package com.example.android.keepingcurrent.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.android.keepingcurrent.database.Dependency;
import com.example.android.keepingcurrent.model.Article;

import java.util.List;

public class AllArticlesViewModel extends AndroidViewModel {

    private final LiveData<List<Article>> allArticles;

    public AllArticlesViewModel(@NonNull Application application) {
        super(application);
        allArticles =
                Dependency.getArticleDao(getApplication().getApplicationContext()).getAllArticles();
    }

    LiveData<List<Article>> getAllArticles() {
        return allArticles;
    }
}
