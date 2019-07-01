package com.example.android.keepingcurrent.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.keepingcurrent.R;
import com.example.android.keepingcurrent.model.Article;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.NewsListViewHolder> {

    private final OnItemClickListener itemClickListener;
    private List<Article> articles;

    public ListAdapter(List<Article> articles, OnItemClickListener clickListener) {
        this.articles = articles;
        this.itemClickListener = clickListener;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NewsListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view =
                LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item, viewGroup, false);
        return new NewsListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsListViewHolder newsListViewHolder, int position) {
        newsListViewHolder.bind(articles.get(position));
    }

    @Override
    public int getItemCount() {
        return articles == null ? 0 : articles.size();
    }

    @Override
    public void onViewRecycled(@NonNull NewsListViewHolder holder) {
        holder.image.setImageDrawable(null);
        super.onViewRecycled(holder);
    }

    public interface OnItemClickListener {
        void onItemClick(Article article, ImageView imageView);
    }

    class NewsListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView title;
        final TextView subTitle;
        final ImageView image;

        NewsListViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.newsTitle);
            image = itemView.findViewById(R.id.newsThumbnail);
            subTitle = itemView.findViewById(R.id.newsSubtitle);
            itemView.setOnClickListener(this);
        }

        void bind(Article article) {

            title.setText(article.getTitle());

            if (article.getDescription() != null && !article.getDescription().equals(""))
                subTitle.setText(article.getDescription());
            else subTitle.setText(article.getUrl());

            if (article.getUrlToImage() != null && !article.getUrlToImage().equals("")) {
                Picasso.with(itemView.getContext())
                        .load(article.getUrlToImage())
                        .placeholder(R.drawable.img_ph)
                        .error(R.drawable.img_ph)
                        .fit()
                        .centerCrop()
                        .into(image);
            }
        }

        @Override
        public void onClick(View v) {
            if (itemClickListener != null)
                itemClickListener.onItemClick(articles.get(getAdapterPosition()), image);
        }
    }
}
