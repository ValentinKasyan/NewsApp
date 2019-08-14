package com.example.android.news.Remote;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.news.Adapter.ViewedNewsAdapter;
import com.example.android.news.Common.Common;
import com.example.android.news.Database.DBHandler;
import com.example.android.news.Database.SavedArticles;
import com.example.android.news.Interface.NewsService;
import com.example.android.news.Model.Viewed.ViewedNews;
import com.example.android.news.Model.Viewed.ViewedResult;
import com.example.android.news.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.DOWNLOAD_SERVICE;
import static android.webkit.URLUtil.guessFileName;

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
    String imagePath;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    private long downloadID;
    private String pathToFile, fileName;
    private DownloadManager.Request request;
    private static int REQUEST_CODE = 1;

    DBHandler dbHandler;

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
                    // TODO: 14.08.2019 1 проверка  
                    Toast.makeText(getActivity(), "No internet connection. Restart the application", Toast.LENGTH_LONG).show();
                }
            }
        });
        imageViewViewed = (ImageView) rootView.findViewById(R.id.top_image_viewed);
        lstNews = (RecyclerView) rootView.findViewById(R.id.lstNewsViewed);
        lstNews.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        lstNews.setLayoutManager(layoutManager);
        dbHandler = new DBHandler(getContext(), null, null, 2);
        if (!isOnline()) {
            Toast.makeText(getActivity(), "No internet connection. Restart the application", Toast.LENGTH_LONG).show();
        }
        //add permission
        ActivityCompat.requestPermissions(this.getActivity(), new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, REQUEST_CODE);

        getActivity().registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

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
        getActivity().unregisterReceiver(onDownloadComplete);
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
                Picasso.get().load(savedImgUrl).into(picassoImageTarget(getContext(), "imageDir"));

                downloadFilesToPrivateDirectory(savedWebPageUrlForDownloading);

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

    private void showAlertDialog(Throwable throwable) {
        new AlertDialog.Builder(getContext())
                .setTitle("Error")
                .setMessage(throwable.getLocalizedMessage())
                .setPositiveButton("Close", null)
                .show();

    }

    Target picassoImageTarget(Context context, final String imageDir) {
        Log.d(TAG, " picassoImageTarget");
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
                            Log.d(TAG, "ViewedTab: fail" + e.getMessage());
                        } finally {
                            try {
                                fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.d(TAG, "ViewedTab: fail" + e.getMessage());
                            }
                        }
                        Log.d(TAG, "ViewedTab: image saved to >>>" + myImageFile.getAbsolutePath());

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

    private void downloadFilesToPrivateDirectory(String urlForDownloading) {
        if (isDownloadManagerAvailable(getActivity().getBaseContext())) {
            fileName = guessFileName(urlForDownloading, null, MimeTypeMap.getFileExtensionFromUrl(urlForDownloading));
            File file = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName);
            if (isExternalStorageWritable()) {
                //Create a DownloadManager.Request with all the information necessary to start the download
                DownloadManager.Request request = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    request = new DownloadManager.Request(Uri.parse(urlForDownloading))
                            .setTitle(fileName)//Title of the downloading notification
                            .setDescription("Downloading")//description of the downloading notification
//                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)//visibility downloading notification(VISIBILITY_VISIBLE)
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                            .setDestinationUri(Uri.fromFile(file))//By default, downloads are saved to a generated filename in the shared download cache and may be deleted by the system at any time to reclaim space.
                            .setRequiresCharging(false)// Set if charging is required to begin the download(Установите, если для начала загрузки требуется зарядка)
                            .setAllowedOverMetered(true)// Set if download is allowed on Mobile network
                            .setAllowedOverRoaming(true)
                            .setVisibleInDownloadsUi(true);
                }

                pathToFile = file.getAbsolutePath().toString();

                DownloadManager downloadManager = (DownloadManager) getActivity().getSystemService(DOWNLOAD_SERVICE);
                downloadID = downloadManager.enqueue(request);// enqueue puts the download request in the queue.

            }
        }
    }

    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Fetching the download id received with the broadcast
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            //Checking if the received broadcast is for our enqueued download by matching download id
            if (downloadID == id) {
                Log.d(TAG, "ViewedTab: download of web page completed >>> " + pathToFile);
                if (savedTitle == null && imagePath == null && pathToFile == null) {
                    Toast.makeText(getActivity().getBaseContext(), "Download is not available.Check your internet connection", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "ViewedTab: PROBLEM !!! >>>" + "savedTitle = " + savedTitle + ";" + "imagePath = " + imagePath + ";" + "savedWebPageUrlForDownloading = " + savedWebPageUrlForDownloading);
                    return;
                }
                //add to database
                SavedArticles savedArticle = new SavedArticles(savedTitle, imagePath, pathToFile);
                dbHandler.addArticle(savedArticle);
            }
        }
    };

    public boolean isDownloadManagerAvailable(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return true;
        }
        Toast.makeText(getActivity().getBaseContext(), "DownloadManager is not available  on your devise ", Toast.LENGTH_LONG).show();
        return false;
    }

    // Checks if external storage is available for read and write

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

}
