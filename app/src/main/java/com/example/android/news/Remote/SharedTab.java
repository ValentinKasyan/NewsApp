package com.example.android.news.Remote;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
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

import com.example.android.news.Adapter.SharedNewsAdapter;
import com.example.android.news.Common.Common;
import com.example.android.news.Database.DBHandler;
import com.example.android.news.Download.SongDownloaderIconView;
import com.example.android.news.Interface.NewsService;
import com.example.android.news.Model.Shared.SharedNews;
import com.example.android.news.Model.Shared.SharedResult;
import com.example.android.news.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
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
    String webPageContent;
    SharedNewsAdapter adapter;
    RecyclerView lstNews;
    RecyclerView.LayoutManager layoutManager;
    String imagePath;
    private SongDownloaderIconView imageDownloadIcon;
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    DBHandler dbHandler;

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
                loadNewsShared(true);
            }
        });

        imageViewShared = (ImageView) rootView.findViewById(R.id.top_image_shared);
        lstNews = (RecyclerView) rootView.findViewById(R.id.lstNewsShared);
        lstNews.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        lstNews.setLayoutManager(layoutManager);
        dbHandler = new DBHandler(getContext(), null, null, 2);
        loadNewsShared(false);

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

    private void loadNewsShared(boolean isRefreshed) {
        if (!isRefreshed) {
            dialog.show();
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

        if (sharedNews == null && sharedNews.getResults() == null) {
            Log.d(TAG, "fail : " + "sharedNews = " + sharedNews + "; " +
                    "sharedNews.getResults() = " + sharedNews.getResults());
            return;
        } else {
            SharedResult results = sharedNews.getResults().get(0);
            //get first article
            if (results == null && results.getMedia() == null &&
                    results.getMedia().get(0).getMediaMetadata().get(2).getUrl() == null) {
                Log.d(TAG, "fail : " + "sharedNews.getResults().get(0) = " +
                        results + "; " + "results.getMedia() = " + results.getMedia() +
                        "sharedNews.getResults().get(0).getMedia().get(0).getMediaMetadata().get(2).getUrl() = " +
                        results.getMedia().get(0).getMediaMetadata().get(2).getUrl());
                return;
            } else {
                getImage(sharedNews);
                if (results.getUrl() == null) {
                    Log.d(TAG, "fail : " + "results.getUrl()=" +
                            results.getUrl() + "; ");
                    return;
                }
            }
        }
        List<SharedResult> downloadableItemList = null;
        if (sharedNews == null) {
            Log.d(TAG, "fail : " + "sharedNews = " + sharedNews + "; ");
            return;
        }
        downloadableItemList = sharedNews.getResults();
        //for giving positionId count
        Resources res = this.getResources();
        int counter = 0;
        String[] arrPositionId = res.getStringArray(R.array.position_ids);
        for (SharedResult sharedResult : downloadableItemList) {
            sharedResult.setPositionId(arrPositionId[counter]);
            counter++;
        }
        //create adapter
        adapter = new SharedNewsAdapter(getActivity().getBaseContext(), downloadableItemList, lstNews);
        lstNews.setAdapter(adapter);
    }

    // TODO: 13.07.2019 rename removeFirstItem to downloadableItemList
    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

//    //Floating context menu
//    @Override
//    public boolean onContextItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case 121:
//                Log.d(TAG, "SharedTab: pressed add to favorites in Emailed tab");
//                DownloadingStatus downloadingStatus = imageDownloadIcon.getDownloadingStatus();
//                //Only when the icon is in not downloaded state, then do the following.
//                if (downloadingStatus == DownloadingStatus.NOT_DOWNLOADED){
//                    setI
//                }
//
//                String savedTitle = adapter.getItemTitleTransaction(item.getGroupId());
//                String urlImg = adapter.getItemImageUrlTransaction(item.getGroupId());
//                Picasso.get().load(urlImg).into(picassoImageTarget(getContext(), "imageDir"));
//                SharedTab.WebPageDownloader webPageDownloader = new SharedTab.WebPageDownloader();
//                String urlArticle = adapter.getItemArticleUrlTransaction(item.getGroupId());
//                try {
//
//                    webPageDownloader.execute(urlArticle);
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                if (imagePath == null || webPageContent == null) {
//
//                    // TODO: 12.06.2019 заменить,переделать
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                        Log.d(TAG, "SharedTab: fail" + e.getMessage());
//                    }
//                }
//                SavedArticles savedArticle = new SavedArticles(savedTitle, imagePath);
//                dbHandler.addArticle(savedArticle);
//
//                Log.d(TAG, "SharedTab: pressed add to favorites article with title: " + item.getTitle().toString());
//                return true;
//            default:
//                return super.onContextItemSelected(item);
//        }
//    }

    private void getImage(@NonNull SharedNews sharedNews) {
        Picasso.get()
                .load(sharedNews.getResults().get(0).getMedia().get(0).getMediaMetadata().get(2).getUrl())
                .into(imageViewShared);
    }

    private void showAlertDialog(Throwable throwable) {
        new AlertDialog.Builder(getContext())
                .setTitle("Error")
                .setMessage(throwable.getLocalizedMessage())
                .setPositiveButton("Close", null)
                .show();

    }

    Target picassoImageTarget(Context context, final String imageDir) {
        Log.d("picassoImageTarget", " picassoImageTarget");
        ContextWrapper cw = new ContextWrapper(context);
        final File directory = cw.getDir(imageDir, Context.MODE_PRIVATE); // path to /data/data/yourapp/app_imageDir
        return new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String imageName = String.valueOf(System.currentTimeMillis()) + ".jpeg";
                        imagePath = imageName;
                        final File myImageFile = new File(directory, imageName); // Create image file
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(myImageFile);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d(TAG, "EmailedTab: fail" + e.getMessage());
                        } finally {
                            try {
                                fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.d(TAG, "EmailedTab: fail" + e.getMessage());
                            }
                        }
                        Log.i("DebuggingLogs", "EmailedTab: image saved to >>>" + myImageFile.getAbsolutePath());

                    }
                }).start();

            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }


            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                if (placeHolderDrawable != null) {

                }
            }
        };
    }

    public class WebPageDownloader extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... urls) {

            StringBuffer webPageContentStringBuffer = new StringBuffer();
            if (urls.length <= 0) {
                return "Invalid URL";
            }

            try {
                String url = urls[0];
                URL webUrl = new URL(url);
                InputStream webPageDataStream = webUrl.openStream();
                InputStreamReader webPageDataReader = new InputStreamReader(webPageDataStream);
                int maxBytesToRead = 1024;
                char[] buffer = new char[maxBytesToRead];
                int bytesRead = webPageDataReader.read(buffer);

                while (bytesRead != -1) {
                    webPageContentStringBuffer.append(buffer, 0, bytesRead);
                    bytesRead = webPageDataReader.read(buffer);
                }

                return webPageContentStringBuffer.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return "Failed to get webpage content";
        }

        @Override
        protected void onPostExecute(String webContent) {
            super.onPostExecute(webContent);
            setWebPageContent(webContent);
        }
    }

    void setWebPageContent(String webPageContent) {
        this.webPageContent = webPageContent;
    }
}
