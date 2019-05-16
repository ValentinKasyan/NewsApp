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

import com.example.android.news.Adapter.SharedNewsAdapter;
import com.example.android.news.Common.Common;
import com.example.android.news.Interface.NewsService;
import com.example.android.news.Model.Shared.SharedNews;
import com.example.android.news.Model.Shared.SharedResult;
import com.example.android.news.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SharedTab extends Fragment {

    ImageView imageViewShared;
    SpotsDialog dialog;
    NewsService mService;
    TextView top_author, top_title;
    SwipeRefreshLayout swipeRefreshLayout;
    private static final String TAG = "SharedTab";
    String webHotURL = "";

    SharedNewsAdapter adapter;
    RecyclerView lstNews;
    RecyclerView.LayoutManager layoutManager;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_shared, container, false);
        //Init Service
        mService = Common.getNewsService();

        dialog = new SpotsDialog(getContext());
        //Init View
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshForShared);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadNews(true);
            }
        });

        imageViewShared = (ImageView) rootView.findViewById(R.id.top_image_shared);
        top_author = (TextView) rootView.findViewById(R.id.top_author_shared);
        top_title = (TextView) rootView.findViewById(R.id.top_title_shared);

        lstNews = (RecyclerView) rootView.findViewById(R.id.lstNewsShared);
        lstNews.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        lstNews.setLayoutManager(layoutManager);
        loadNews(false);

        imageViewShared.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent detail = new Intent(getActivity().getBaseContext(), DetailArticle.class);
                detail.putExtra("webURL", webHotURL);
                startActivity(detail);
            }
        });
        return rootView;
    }

    private void loadNews(boolean isRefreshed) {
        if (!isRefreshed) {
            dialog.show();
            mService.getSharedArticles()
                    .enqueue(new Callback<SharedNews>() {
                        @Override
                        public void onResponse(@NonNull Call<SharedNews> call, @NonNull Response<SharedNews> response) {
                            if (response.body() != null && response.body().getResults() != null) {
                                dialog.dismiss();
                                SharedResult results = response.body().getResults().get(0);
                                //get first article
                                if (results != null && results.getMedia().get(0).getMediaMetadata().get(2).getUrl() != null) {
                                    Log.d(TAG, "response for getImage : " + "response = " + response + "; " + "response.body() = " + response.body());
                                    getImage(response);
                                }
                                assert results != null;
                                if (results.getTitle() != null && results.getMedia().get(0).getCopyright() != null && results.getUrl() != null) {
                                    top_title.setText(results.getTitle());
                                    top_author.setText(results.getMedia().get(0).getCopyright());
                                    webHotURL = results.getUrl();
                                }
                                //Load remain articles
                                List<SharedResult> removeFirstItem = response.body().getResults();
                                //Because we already load first item to show on Diagonal Layout
                                // So we need remove it
                                removeFirstItem.remove(0);
                                adapter = new SharedNewsAdapter(removeFirstItem, getActivity().getBaseContext());
                                lstNews.setAdapter(adapter);
                            } else {
                                Log.d(TAG, "fail : " + "response = " + response + "; " + "response.body() = " + response.body());
                            }
                        }


                        @Override
                        public void onFailure(@NonNull Call<SharedNews> call, @NonNull Throwable t) {
                            Log.d(TAG, "fail" + t.getMessage());
                            showAlertDialog(t);
                        }
                    });


        } else// If from Swipe to Refresh
        {
            dialog.show();
            //fetch new data
            mService.getSharedArticles().enqueue(new Callback<SharedNews>() {
                @Override
                public void onResponse(@NonNull Call<SharedNews> call, @NonNull Response<SharedNews> response) {
                    if (response.body() != null && response.body().getResults() != null) {
                        dialog.dismiss();
                        SharedResult results = response.body().getResults().get(0);
                        //get first article
                        if (results != null && results.getMedia().get(0).getMediaMetadata().get(2).getUrl() != null) {
                            getImage(response);
                        }
                        assert results != null;
                        if (results.getTitle() != null && results.getMedia().get(0).getCopyright() != null && results.getUrl() != null) {
                            top_title.setText(results.getTitle());
                            top_author.setText(results.getMedia().get(0).getCopyright());
                            webHotURL = results.getUrl();
                        }
                        //Load remain articles
                        List<SharedResult> removeFirstItem = response.body().getResults();

                        //Because we already load first item to show on Diagonal Layout
                        // So we need remove it
                        removeFirstItem.remove(0);
                        adapter = new SharedNewsAdapter(removeFirstItem, getActivity().getBaseContext());
                        lstNews.setAdapter(adapter);
                    } else {
                        Log.d(TAG, "fail : " + "response = " + response + "; " + "response.body() = " + response.body());
                    }
                }


                @Override
                public void onFailure(Call<SharedNews> call, Throwable t) {
                    Log.d(TAG, "fail" + t.getMessage());
                    showAlertDialog(t);
                }
            });
            //Dismiss refresh progressing
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    public void getImage(@NonNull Response<SharedNews> response) {
        Picasso.get()
                .load(response.body().getResults().get(0).getMedia().get(0).getMediaMetadata().get(2).getUrl())
                .into(imageViewShared);
    }

    public void showAlertDialog(Throwable throwable) {
        new AlertDialog.Builder(getContext())
                .setTitle("Error")
                .setMessage(throwable.getLocalizedMessage())
                .setPositiveButton("Close", null)
                .show();
        Log.d(TAG, "fail" + throwable.getLocalizedMessage());


    }
}
