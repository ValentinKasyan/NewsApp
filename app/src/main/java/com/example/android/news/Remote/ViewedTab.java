package com.example.android.news.Remote;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.news.Adapter.ViewedNewsAdapter;
import com.example.android.news.Common.Common;
import com.example.android.news.Interface.NewsService;
import com.example.android.news.Model.Viewed.ViewedNews;
import com.example.android.news.Model.Viewed.ViewedResult;
import com.example.android.news.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ViewedTab extends Fragment {
    ImageView imageViewViewed;
    SpotsDialog dialog;
    NewsService mService;
    SwipeRefreshLayout swipeRefreshLayout;
    private static final String TAG = "DebuggingLogs";
    String webHotURL = "";
    String savedTitle, savedImgUrl, savedWebPageUrlForDownloading;
    ViewedNewsAdapter adapter;
    RecyclerView lstNews;
    RecyclerView.LayoutManager layoutManager;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    private static int REQUEST_CODE = 1;
    DownloadHelper downloadHelper;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_viewed, container, false);
        //Init Service
        mService = Common.getNewsService();

        dialog = new SpotsDialog(getContext());
        //Init View
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshForViewed);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isOnline()) {
                    loadNewsViewed(true);
                } else {
                    Toast.makeText(getActivity(), "No internet connection. Restart the application", Toast.LENGTH_LONG).show();
                }
            }
        });
        imageViewViewed = (ImageView) rootView.findViewById(R.id.top_image_viewed);
        lstNews = (RecyclerView) rootView.findViewById(R.id.lstNewsViewed);
        lstNews.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        lstNews.setLayoutManager(layoutManager);
        downloadHelper = new DownloadHelper();

        if (!isOnline()) {
            Toast.makeText(getActivity(), "No internet connection. Restart the application", Toast.LENGTH_LONG).show();
        }
        //add permission
        ActivityCompat.requestPermissions(this.getActivity(), new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, REQUEST_CODE);

        getActivity().registerReceiver(downloadHelper.onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        loadNewsViewed(false);

        imageViewViewed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent viewedIntent = new Intent(getActivity().getBaseContext(), DetailArticle.class);
                viewedIntent.putExtra("source", "viewed");
                viewedIntent.putExtra("webURL", webHotURL);
                startActivity(viewedIntent);
            }
        });
        return rootView;
    }

    private void loadNewsViewed(boolean isRefreshed) {
        if (!isRefreshed) {
            dialog.show();
            if (isOnline()) {
                compositeDisposable.add(mService.getViewedArticles()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<ViewedNews>() {
                            @Override
                            public void accept(ViewedNews viewedNews) throws Exception {
                                displayData(viewedNews);
                                dialog.dismiss();
                            }
                        }));
            }
        } else// If from Swipe to Refresh
        {
            dialog.show();
            //fetch new data
            compositeDisposable.add(mService.getViewedArticles()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<ViewedNews>() {
                        @Override
                        public void accept(ViewedNews viewedNews) throws Exception {
                            displayData(viewedNews);
                            dialog.dismiss();
                        }
                    }));
            //Dismiss refresh progressing
            swipeRefreshLayout.setRefreshing(false);
        }
    }


    private void displayData(ViewedNews viewedNews) {

        if (viewedNews != null && viewedNews.getResults() != null) {
            ViewedResult results = viewedNews.getResults().get(0);
            //get first article
            if (results != null && results.getMedia().get(0).getMediaMetadata().get(2).getUrl() != null) {
                getImage(viewedNews);
            }
            assert results != null;
            if (results.getMedia().get(0).getCopyright() != null && results.getUrl() != null) {
                webHotURL = results.getUrl();
            }

        }
        List<ViewedResult> removeFirstItem = null;
        if (viewedNews != null) {
            removeFirstItem = viewedNews.getResults();
        }
        adapter = new ViewedNewsAdapter(removeFirstItem, getActivity().getBaseContext());
        lstNews.setAdapter(adapter);
    }

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(downloadHelper.onDownloadComplete);
    }

    //Floating context menu
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 421:
                Log.d(TAG, "ViewedTab: pressed add to favorites in Viewed tab");
                savedTitle = adapter.getItemTitleTransaction(item.getGroupId());
                savedImgUrl = adapter.getItemImageUrlTransaction(item.getGroupId());
                savedWebPageUrlForDownloading = adapter.getItemArticleUrlTransaction(item.getGroupId());

                Picasso.get().load(savedImgUrl).into(downloadHelper.picassoImageTarget(getContext(), "imageDir"));
                downloadHelper.downloadFilesToPrivateDirectory(savedWebPageUrlForDownloading, savedTitle, this.getContext());

                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void getImage(@NonNull ViewedNews viewedNews) {
        Picasso.get()
                .load(viewedNews.getResults().get(0).getMedia().get(0).getMediaMetadata().get(2).getUrl())
                .into(imageViewViewed);
    }


    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

}
