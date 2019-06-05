package com.example.android.news.Remote;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.news.Adapter.EmailedNewsAdapter;
import com.example.android.news.Common.Common;
import com.example.android.news.Interface.NewsService;
import com.example.android.news.Model.Emailed.EmailedNews;
import com.example.android.news.Model.Emailed.EmailedResults;
import com.example.android.news.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmailedTab extends Fragment {

    ImageView imageViewEmailed;
    SpotsDialog dialog;
    NewsService mService;
    TextView top_title;
    SwipeRefreshLayout swipeRefreshLayout;
    private static final String TAG = " EmailedTab";
    String webHotURL = "";

    EmailedNewsAdapter adapter;
    RecyclerView lstNews;
    RecyclerView.LayoutManager layoutManager;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_emailed, container, false);
        //Init Service
        mService = Common.getNewsService();

        dialog = new SpotsDialog(getContext());
        //Init View
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshForEmailed);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadNewsEmailed(true);
            }
        });

        imageViewEmailed = (ImageView) rootView.findViewById(R.id.top_image_emailed);
        top_title = (TextView) rootView.findViewById(R.id.top_title);

        lstNews = (RecyclerView) rootView.findViewById(R.id.lstNewsEmailed);
        lstNews.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        lstNews.setLayoutManager(layoutManager);
        loadNewsEmailed(false);

        imageViewEmailed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent detail = new Intent(getActivity().getBaseContext(), DetailArticle.class);
                detail.putExtra("webURL", webHotURL);
                startActivity(detail);
            }
        });
        return rootView;
    }

    public void loadNewsEmailed(boolean isRefreshed) {
        if (!isRefreshed) {
            dialog.show();
            mService.getEmailedArticles()
                    .enqueue(new Callback<EmailedNews>() {
                        @Override
                        public void onResponse(@NonNull Call<EmailedNews> call, @NonNull Response<EmailedNews> response) {
                            if (response.body() != null && response.body().getResults() != null) {
                                dialog.dismiss();
                                EmailedResults results = response.body().getResults().get(0);
                                //get first article
                                if (results != null && results.getMedia().get(0).getMediaMetadata().get(2).getUrl() != null) {
                                    Log.d(TAG, "response for getImage : " + "response = " + response + "; " + "response.body() = " + response.body());
                                    getImage(response);
                                }
                                assert results != null;
                                if (results.getTitle() != null && results.getMedia().get(0).getCopyright() != null && results.getUrl() != null) {
                                    top_title.setText(results.getTitle());
                                    webHotURL = results.getUrl();
                                }
                                //Load remain articles
                                List<EmailedResults> removeFirstItem = response.body().getResults();
                                //Because we already load first item to show on Diagonal Layout
                                // So we need remove it
                                removeFirstItem.remove(0);
                                adapter = new EmailedNewsAdapter(removeFirstItem, getActivity().getBaseContext());
                                lstNews.setAdapter(adapter);
                            } else {
                                Log.d(TAG, "fail : " + "response = " + response + "; " + "response.body() = " + response.body());
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<EmailedNews> call, @NonNull Throwable t) {
                            Log.d(TAG, "fail" + t.getMessage());
                            showAlertDialog(t);
                        }
                    });

        } else// If from Swipe to Refresh
        {

            dialog.show();
            //fetch new data
            mService.getEmailedArticles().enqueue(new Callback<EmailedNews>() {
                @Override
                public void onResponse(@NonNull Call<EmailedNews> call, @NonNull Response<EmailedNews> response) {
                    if (response.body() != null && response.body().getResults() != null) {
                        dialog.dismiss();
                        EmailedResults results = response.body().getResults().get(0);
                        //get first article
                        if (results != null && results.getMedia().get(0).getMediaMetadata().get(2).getUrl() != null) {
                            getImage(response);
                        }
                        assert results != null;
                        if (results.getTitle() != null && results.getMedia().get(0).getCopyright() != null && results.getUrl() != null) {
                            top_title.setText(results.getTitle());
                            webHotURL = results.getUrl();
                        }
                        //Load remain articles
                        List<EmailedResults> removeFirstItem = response.body().getResults();

                        //Because we already load first item to show on Diagonal Layout
                        // So we need remove it
                        removeFirstItem.remove(0);
                        adapter = new EmailedNewsAdapter(removeFirstItem, getActivity().getBaseContext());
                        lstNews.setAdapter(adapter);
                    } else {
                        Log.d(TAG, "fail : " + "response = " + response + "; " + "response.body() = " + response.body());
                    }
                }

                @Override
                public void onFailure(Call<EmailedNews> call, Throwable t) {
                    showAlertDialog(t);
                    Log.d(TAG, "fail" + t.getMessage());
                }
            });
            //Dismiss refresh progressing
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    public void getImage(@NonNull Response<EmailedNews> response) {
        Picasso.get()
                .load(response.body().getResults().get(0).getMedia().get(0).getMediaMetadata().get(2).getUrl())
                .into(imageViewEmailed);
    }

    public void showAlertDialog(Throwable throwable) {
        new AlertDialog.Builder(getContext())
                .setTitle("Error")
                .setMessage(throwable.getLocalizedMessage())
                .setPositiveButton("Close", null)
                .show();

    }
}
