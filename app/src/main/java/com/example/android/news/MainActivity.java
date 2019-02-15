package com.example.android.news;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.android.news.Adapter.EmailedNewsAdapter;
import com.example.android.news.Common.Common;
import com.example.android.news.Interface.NewsService;
import com.example.android.news.Model.EmailedNews;
import com.example.android.news.Model.Results;
import com.example.android.news.Remote.DetailArticle;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.github.florent37.diagonallayout.DiagonalLayout;
import com.squareup.picasso.Picasso;

import java.util.List;

import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    KenBurnsView kbv;
    DiagonalLayout diagonalLayout;
    SpotsDialog dialog;
    NewsService mService;
    TextView top_author, top_title;
    SwipeRefreshLayout swipeRefreshLayout;
    private static final String TAG = " MainActivity";

    String webHotURL = "";

    EmailedNewsAdapter adapter;
    RecyclerView lstNews;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Init Service
        mService = Common.getNewsService();

        dialog = new SpotsDialog(this);

        //Init View
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadNews(true);
            }
        });

        diagonalLayout = (DiagonalLayout) findViewById(R.id.diagonalLayout);
        diagonalLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent detail = new Intent(getBaseContext(), DetailArticle.class);
                detail.putExtra("webURL", webHotURL);
                startActivity(detail);
            }
        });
        kbv = (KenBurnsView) findViewById(R.id.top_image);
        top_author = (TextView) findViewById(R.id.top_author);
        top_title = (TextView) findViewById(R.id.top_title);

        lstNews = (RecyclerView) findViewById(R.id.lstNews);
        lstNews.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        lstNews.setLayoutManager(layoutManager);
        loadNews(false);

    }

    private void loadNews(boolean isRefreshed) {
        if (!isRefreshed) {
            dialog.show();
            mService.getNewestArticles()
                    .enqueue(new Callback<EmailedNews>() {
                        @Override
                        public void onResponse(@NonNull Call<EmailedNews> call, @NonNull Response<EmailedNews> response) {
                            if (response.body() != null && response.body().getResults() != null) {
                                dialog.dismiss();
                                Results results = response.body().getResults().get(0);
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
                                List<Results> removeFirstItem = response.body().getResults();
                                //Because we already load first item to show on Diagonal Layout
                                //So we need remove it
                                removeFirstItem.remove(0);
                                adapter = new EmailedNewsAdapter(removeFirstItem, getBaseContext());
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
            mService.getNewestArticles().enqueue(new Callback<EmailedNews>() {
                @Override
                public void onResponse(@NonNull Call<EmailedNews> call, @NonNull Response<EmailedNews> response) {
                    if (response.body() != null && response.body().getResults() != null) {
                        dialog.dismiss();
                        Results results = response.body().getResults().get(0);
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
                        List<Results> removeFirstItem = response.body().getResults();
                        //Because we already load first item to show on Diagonal Layout
                        //So we need remove it
                        removeFirstItem.remove(0);
                        adapter = new EmailedNewsAdapter(removeFirstItem, getBaseContext());
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

    public void showAlertDialog(Throwable throwable) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Error")
                .setMessage(throwable.getLocalizedMessage())
                .setPositiveButton("Close", null)
                .show();

    }

    public void getImage(@NonNull Response<EmailedNews> response) {
        Picasso.get()
                .load(response.body().getResults().get(0).getMedia().get(0).getMediaMetadata().get(2).getUrl())
                .into(kbv);
    }
}
