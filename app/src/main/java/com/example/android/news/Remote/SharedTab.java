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

import com.example.android.news.Adapter.SharedNewsAdapter;
import com.example.android.news.Common.Common;
import com.example.android.news.Interface.NewsService;
import com.example.android.news.Model.Shared.SharedNews;
import com.example.android.news.Model.Shared.SharedResult;
import com.example.android.news.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class SharedTab extends Fragment {

    ImageView imageViewShared;
    SpotsDialog dialog;
    NewsService mService;
    SwipeRefreshLayout swipeRefreshLayout;
    private static final String TAG = "DebuggingLogs";
    String webHotURL = "";
    String savedTitle, savedImgUrl, savedWebPageUrlForDownloading;
    SharedNewsAdapter adapter;
    RecyclerView lstNews;
    RecyclerView.LayoutManager layoutManager;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    private static int REQUEST_CODE = 1;
    DownloadHelper downloadHelper;

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
                if (isOnline()) {
                    loadNewsShared(true);
                } else {
                    Toast.makeText(getActivity(), "No internet connection. Restart the application", Toast.LENGTH_LONG).show();
                }
            }
        });

        imageViewShared = (ImageView) rootView.findViewById(R.id.top_image_shared);
        lstNews = (RecyclerView) rootView.findViewById(R.id.lstNewsShared);
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

        loadNewsShared(false);

        imageViewShared.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sharedIntent = new Intent(getActivity().getBaseContext(), DetailArticle.class);
                sharedIntent.putExtra("source", "shared");
                sharedIntent.putExtra("webURL", webHotURL);
                startActivity(sharedIntent);
            }
        });
        return rootView;
    }

    private void loadNewsShared(boolean isRefreshed) {
        if (!isRefreshed) {
            dialog.show();
            if (isOnline()) {
                compositeDisposable.add(mService.getSharedArticles()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<SharedNews>() {
                            @Override
                            public void accept(SharedNews sharedNews) throws Exception {
                                displayData(sharedNews);
                                dialog.dismiss();
                            }
                        }));
            }

        } else// If from Swipe to Refresh
        {
            dialog.show();
            //fetch new data
            compositeDisposable.add(mService.getSharedArticles()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<SharedNews>() {
                        @Override
                        public void accept(SharedNews sharedNews) throws Exception {
                            displayData(sharedNews);
                            dialog.dismiss();
                        }
                    }));
            //Dismiss refresh progressing
            swipeRefreshLayout.setRefreshing(false);
        }
    }


    private void displayData(SharedNews sharedNews) {

        if (sharedNews != null && sharedNews.getResults() != null) {
            SharedResult results = sharedNews.getResults().get(0);
            //get first article
            if (results != null && results.getMedia().get(0).getMediaMetadata().get(2).getUrl() != null) {
                getImage(sharedNews);
            }
            assert results != null;
            if (results.getMedia().get(0).getCopyright() != null && results.getUrl() != null) {
                webHotURL = results.getUrl();
            }

        }
        List<SharedResult> removeFirstItem = null;
        if (sharedNews != null) {
            removeFirstItem = sharedNews.getResults();
        }
        adapter = new SharedNewsAdapter(removeFirstItem, getActivity().getBaseContext());
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
            case 321:
                Log.d(TAG, "SharedTab: pressed add to favorites in Shared tab");
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

    private void getImage(@NonNull SharedNews sharedNews) {
        Picasso.get()
                .load(sharedNews.getResults().get(0).getMedia().get(0).getMediaMetadata().get(2).getUrl())
                .into(imageViewShared);
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}
