package com.example.android.keepingcurrent.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RoomWarnings;

import com.example.android.keepingcurrent.model.Article;
import com.example.android.keepingcurrent.model.ArticleType;

import java.util.List;

@Dao
public interface ArticleDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Article article);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(ArticleType articleType);

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query(
            "select * from articles a, article_type at  where a.id = at.id and at.type =:type order by published_at desc")
    LiveData<List<Article>> getArticles(int type);

    @Query("select * from articles order by published_at desc")
    LiveData<List<Article>> getAllArticles();

    @Query(
            "select id from articles where title = :title and url = :url and published_at = :publishedAt")
    long getArticleId(String title, String url, String publishedAt);

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query(
            "select a.id, title from articles a, article_type at  where a.id = at.id and at.type ="
                    + ArticleType.Type.TOP_HEAD
                    + " order by published_at desc")
    List<Article> getHeadlines();
}
