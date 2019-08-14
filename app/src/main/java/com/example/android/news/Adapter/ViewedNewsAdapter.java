package com.example.android.news.Adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.news.Interface.ItemClickListener;
import com.example.android.news.Model.Viewed.ViewedResult;
import com.example.android.news.R;
import com.example.android.news.Remote.DetailArticle;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

class ViewedNewsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {
    private ItemClickListener itemClickListener;
    TextView article_title;
    RelativeTimeTextView article_time;
    CircleImageView article_image;
    CardView cardViewViewed;

    ViewedNewsViewHolder(View itemView) {
        super(itemView);
        article_image = (CircleImageView) itemView.findViewById(R.id.article_image_viewed);
        article_title = (TextView) itemView.findViewById(R.id.article_title_viewed);
        article_time = (RelativeTimeTextView) itemView.findViewById(R.id.article_time_viewed);
        cardViewViewed = (CardView) itemView.findViewById(R.id.cardViewViewed);
        cardViewViewed.setOnCreateContextMenuListener(this);

        itemView.setOnClickListener((View.OnClickListener) this);
    }

    void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view, getAdapterPosition(), false);
    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        contextMenu.setHeaderTitle("add article to favorites");
        contextMenu.add(this.getAdapterPosition(), 421, 0, "save");

    }
}

public class ViewedNewsAdapter extends RecyclerView.Adapter<ViewedNewsViewHolder> {

    private List<ViewedResult> articleList;
    private Context context;

    public ViewedNewsAdapter(List<ViewedResult> articleList, Context context) {
        this.articleList = articleList;
        this.context = context;
    }


    @Override
    public ViewedNewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.news_layout_viewed, parent, false);
        return new ViewedNewsViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewedNewsViewHolder holder, int position) {
        Picasso.get()
                .load(articleList
                        .get(position).getMedia()
                        .get(0).getMediaMetadata()
                        .get(2).getUrl())
                .into(holder.article_image);


        holder.article_title.setText(articleList.get(position).getTitle());

        String date = articleList.get(position).getPublishedDate();
        holder.article_time.setText(date);

        //set event click
        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                Intent detail = new Intent(context, DetailArticle.class);
                detail.putExtra("webURL", articleList.get(position).getUrl());
                detail.putExtra("source", "viewed");
                detail.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(detail);

            }
        });
    }

    @Override
    public int getItemCount() {
        return articleList.size();
    }

    public String getItemTitleTransaction(int position) {
        return articleList.get(position).getTitle();
    }

    public String getItemImageUrlTransaction(int position) {
        return articleList.get(position).getMedia()
                .get(0).getMediaMetadata()
                .get(2).getUrl();
    }

    public String getItemArticleUrlTransaction(int position) {
        return articleList.get(position).getUrl();
    }
}
