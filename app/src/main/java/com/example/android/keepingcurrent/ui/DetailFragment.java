package com.example.android.keepingcurrent.ui;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.android.keepingcurrent.R;
import com.example.android.keepingcurrent.model.Article;
import com.example.android.keepingcurrent.utilities.PreferenceUtility;
import com.example.android.keepingcurrent.utilities.TimestampUtility;
import com.squareup.picasso.Picasso;

public class DetailFragment extends Fragment implements View.OnClickListener {

    public static final String FRAGMENT_TAG = "detailFragment";
    private Article article;

    public DetailFragment() {
    }

    static DetailFragment getInstance(Article article) {
        Bundle arg = new Bundle();
        arg.putParcelable(DetailActivity.EXTRA_ARTICLE, article);
        DetailFragment fragment = new DetailFragment();
        fragment.setArguments(arg);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            article = getArguments().getParcelable(DetailActivity.EXTRA_ARTICLE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.content_detail, container, false);

        if (article != null && getContext() != null) {
            TextView title = rootView.findViewById(R.id.newsTitle);
            TextView author = rootView.findViewById(R.id.newsAuthor);
            TextView published = rootView.findViewById(R.id.publishedAt);
            TextView desc = rootView.findViewById(R.id.description);
            TextView content = rootView.findViewById(R.id.newsContent);
            TextView url = rootView.findViewById(R.id.newsURL);
            ImageView imageView = rootView.findViewById(R.id.imageTablet);
            Button button = rootView.findViewById(R.id.button2);

            if (imageView != null) {
                Picasso.with(getContext())
                        .load(article.getUrlToImage())
                        .placeholder(R.drawable.img_ph)
                        .error(R.drawable.img_ph)
                        .into(imageView);
            }

            url.setText(article.getUrl());
            url.setOnClickListener(this);
            button.setOnClickListener(this);
            title.setText(article.getTitle());
            author.setText(article.getAuthor());

            String dateString;
            if (PreferenceUtility.getPrefRelatedTime(getContext()))
                dateString = TimestampUtility.getRelativeDisplayString(article.getPublishedAt());
            else dateString = TimestampUtility.getDateDisplayString(article.getPublishedAt());
            published.setText(dateString);

            desc.setText(article.getDescription());
            content.setText(article.getContent());
        }
        return rootView;
    }

    public Article getArticle() {
        return article;
    }

    @Override
    public void onClick(View v) {
        CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
        intentBuilder.setToolbarColor(ContextCompat.getColor(v.getContext(), R.color.colorPrimary));
        CustomTabsIntent customTabsIntent = intentBuilder.build();
        customTabsIntent.launchUrl(v.getContext(), Uri.parse(article.getUrl()));
    }
}
