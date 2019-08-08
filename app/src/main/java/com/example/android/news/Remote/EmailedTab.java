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
import android.net.Uri;
import android.os.AsyncTask;
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

import com.example.android.news.Adapter.EmailedNewsAdapter;
import com.example.android.news.Common.Common;
import com.example.android.news.Database.DBHandler;
import com.example.android.news.Database.SavedArticles;
import com.example.android.news.Interface.NewsService;
import com.example.android.news.MainActivity;
import com.example.android.news.Model.Emailed.EmailedNews;
import com.example.android.news.Model.Emailed.EmailedResults;
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


import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

import static android.webkit.URLUtil.guessFileName;

import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.DOWNLOAD_SERVICE;
import static android.webkit.URLUtil.guessFileName;

public class EmailedTab extends Fragment {

    ImageView imageViewEmailed;
    SpotsDialog dialog;
    NewsService mService;
    SwipeRefreshLayout swipeRefreshLayout;
    private static final String TAG = "DebuggingLogs";
    String webHotURL = "";
    String webPageContent;
    EmailedNewsAdapter adapter;
    RecyclerView lstNews;
    RecyclerView.LayoutManager layoutManager;
    String imagePath;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    private long downloadID;
    private String pathToFile,fileName;
    private DownloadManager.Request request;
    private static int REQUEST_CODE = 1;
    // TODO: 08.08.2019 change
    private String url = "https://www.nytimes.com/2019/07/16/opinion/trump-2020.html";

    DBHandler dbHandler;

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
        lstNews = (RecyclerView) rootView.findViewById(R.id.lstNewsEmailed);
        lstNews.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        lstNews.setLayoutManager(layoutManager);
        dbHandler = new DBHandler(getContext(), null, null, 2);
        //add permission
        ActivityCompat.requestPermissions(this.getActivity(), new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, REQUEST_CODE);
        getActivity().registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
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

    private void loadNewsEmailed(boolean isRefreshed) {
        if(!isRefreshed){
            dialog.show();
        compositeDisposable.add(mService.getEmailedArticles()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<EmailedNews>() {
                    @Override
                    public void accept(EmailedNews emailedNews) throws Exception {
                        displayData(emailedNews);
                        dialog.dismiss();
                    }
                }));
        }else// If from Swipe to Refresh
        {
            dialog.show();
            //fetch new data
            compositeDisposable.add(mService.getEmailedArticles()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<EmailedNews>() {
                        @Override
                        public void accept(EmailedNews emailedNews) throws Exception {
                            displayData(emailedNews);
                            dialog.dismiss();
                        }
                    }));
            //Dismiss refresh progressing
            swipeRefreshLayout.setRefreshing(false);
        }
    }


    private void displayData(EmailedNews emailedNews) {

        if (emailedNews != null && emailedNews.getResults() != null) {
            EmailedResults results = emailedNews.getResults().get(0);
            //get first article
            if (results != null && results.getMedia().get(0).getMediaMetadata().get(2).getUrl() != null) {
                getImage(emailedNews);
            }
            assert results != null;
            if (results.getMedia().get(0).getCopyright() != null && results.getUrl() != null) {
                webHotURL = results.getUrl();
            }

        }
        List<EmailedResults> removeFirstItem = null;
        if (emailedNews != null) {
            removeFirstItem = emailedNews.getResults();
        }
        adapter = new EmailedNewsAdapter(removeFirstItem, getActivity().getBaseContext());
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
            case 121:
                Log.d(TAG, "EmailedTab: pressed add to favorites in Emailed tab");

                String savedTitle = adapter.getItemTitleTransaction(item.getGroupId());
                String urlImg = adapter.getItemImageUrlTransaction(item.getGroupId());
                Picasso.get().load(urlImg).into(picassoImageTarget(getContext(), "imageDir"));
                // TODO: 08.08.2019 download web page
                String urlArticle = adapter.getItemArticleUrlTransaction(item.getGroupId());
                try {

                   //
                    // webPageDownloader.execute(urlArticle);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (imagePath == null || webPageContent == null) {

                    // TODO: 12.06.2019 заменить,переделать
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.d(TAG, "EmailedTab: fail" + e.getMessage());
                    }
                }
                SavedArticles savedArticle = new SavedArticles(savedTitle, imagePath);
                dbHandler.addArticle(savedArticle);

                Log.d(TAG, "EmailedTab: pressed add to favorites article with title: " + item.getTitle().toString());
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void getImage(@NonNull EmailedNews emailedNews) {
        Picasso.get()
                .load(emailedNews.getResults().get(0).getMedia().get(0).getMediaMetadata().get(2).getUrl())
                .into(imageViewEmailed);
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

    private void downloadFilesToPrivateDirectory() {
        if (isDownloadManagerAvailable(getActivity().getBaseContext())) {
            fileName = guessFileName(url, null, MimeTypeMap.getFileExtensionFromUrl(url));
            File file = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName);

//            if (!file.mkdirs()) {
//                Toast.makeText(MainActivity.this, "Directory not created ", Toast.LENGTH_LONG).show();
//            }
            if (isExternalStorageWritable()) {
                //Create a DownloadManager.Request with all the information necessary to start the download
                DownloadManager.Request request = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    request = new DownloadManager.Request(Uri.parse(url))
                            .setTitle(fileName)//Title of the downloading notification
                            .setDescription("Downloading")//description of the downloading notification
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)//visibility downloading notification(VISIBILITY_VISIBLE)
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
                Toast.makeText(getActivity().getBaseContext(), "Download Completed " + fileName.toString(), Toast.LENGTH_LONG).show();
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

}
