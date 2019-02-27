package com.example.android.news.Adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.news.Interface.ItemClickListener;
import com.example.android.news.Model.Results;
import com.example.android.news.R;
import com.example.android.news.Remote.DetailArticle;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

class SharedNewsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    private ItemClickListener itemClickListener;
    TextView article_title;
    RelativeTimeTextView article_time;
    CircleImageView article_image;

    SharedNewsViewHolder(View itemView) {
        super(itemView);
        article_image = (CircleImageView) itemView.findViewById(R.id.article_image);
        article_title = (TextView) itemView.findViewById(R.id.article_title);
        article_time = (RelativeTimeTextView) itemView.findViewById(R.id.article_time);

        itemView.setOnClickListener(this);
    }

    void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view, getAdapterPosition(), false);
    }
}

public class SharedNewsAdapter extends RecyclerView.Adapter<EmailedNewsViewHolder> {

    private List<Results> articleList;
    private Context context;

    public SharedNewsAdapter(List<Results> articleList, Context context) {
        this.articleList = articleList;
        this.context = context;
    }


    @Override
    public EmailedNewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.news_layout, parent, false);
        return new EmailedNewsViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(EmailedNewsViewHolder holder, int position) {
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
                detail.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(detail);
            }
        });
    }

    @Override
    public int getItemCount() {
        return articleList.size();
    }
}
