package com.example.android.news.Adapter;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.news.Interface.ItemClickListener;
import com.example.android.news.Model.Search.Doc;
import com.example.android.news.Model.Search.Multimedia;
import com.example.android.news.R;
import com.example.android.news.Remote.DetailArticle;
import com.github.curioustechizen.ago.RelativeTimeTextView;

import java.util.List;

class SearchNewsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private ItemClickListener itemClickListener;
    TextView article_title;
    RelativeTimeTextView article_time;


    SearchNewsViewHolder(View itemView) {
        super(itemView);
        article_title = (TextView) itemView.findViewById(R.id.article_title_search);
        article_time = (RelativeTimeTextView) itemView.findViewById(R.id.article_time_search);

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

public class SearchNewsAdapter extends RecyclerView.Adapter<SearchNewsViewHolder> {

    private List<Doc> articleList;
    private List<Multimedia> multimediaList;
    private Context context;


    public SearchNewsAdapter(List<Doc> articleList, Context context) {
        this.articleList = articleList;
        this.context = context;

    }


    @Override
    public SearchNewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.news_layout_search, parent, false);

        return new SearchNewsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SearchNewsViewHolder holder, int position) {
        holder.article_title.setText(articleList.get(position).getHeadline().getMain());
        String date = articleList.get(position).getPubDate();
        holder.article_time.setText(date);

        //set event click
        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                Intent detail = new Intent(context, DetailArticle.class);
                detail.putExtra("webURL", articleList.get(position).getWebUrl());
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
