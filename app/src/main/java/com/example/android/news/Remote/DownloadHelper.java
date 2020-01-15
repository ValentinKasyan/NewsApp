package com.example.android.news.Remote;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.example.android.news.Database.DBHandler;
import com.example.android.news.Database.SavedArticles;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.content.Context.DOWNLOAD_SERVICE;
import static android.webkit.URLUtil.guessFileName;

public class DownloadHelper {

    private String imagePath, fileName, pathToFile, savedTitleLocal;
    private long downloadID;
    private static final String TAG = "DebuggingLogs";
    DBHandler dbHandler;


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
                            Log.d(TAG, "SharedTab: fail" + e.getMessage());
                        } finally {
                            try {
                                fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.d(TAG, "SharedTab: fail" + e.getMessage());
                            }
                        }
                        Log.d("DebuggingLogs", "SharedTab: image saved to >>>" + myImageFile.getAbsolutePath());

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

    void downloadFilesToPrivateDirectory(String urlForDownloading, String savedTitle, Context context) {
        if (isDownloadManagerAvailable(context)) {
            fileName = guessFileName(urlForDownloading, null, MimeTypeMap.getFileExtensionFromUrl(urlForDownloading));
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName);
            if (isExternalStorageWritable()) {
                //Create a DownloadManager.Request with all the information necessary to start the download
                DownloadManager.Request request = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    request = new DownloadManager.Request(Uri.parse(urlForDownloading))
                            .setTitle(fileName)//Title of the downloading notification
                            .setDescription("Downloading")//description of the downloading notification
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                            .setDestinationUri(Uri.fromFile(file))// download in cache
                            .setRequiresCharging(false)// Set if charging is required to begin the download
                            .setAllowedOverMetered(true)// Set if download is allowed on Mobile network
                            .setAllowedOverRoaming(true)
                            .setVisibleInDownloadsUi(true);
                }

                pathToFile = file.getAbsolutePath().toString();
                savedTitleLocal = savedTitle;

                DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
                downloadID = downloadManager.enqueue(request);// enqueue puts the download request in the queue.

            }
        }
    }

    public boolean isDownloadManagerAvailable(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return true;
        }
        Toast.makeText(context, "DownloadManager is not available  on your devise ", Toast.LENGTH_LONG).show();
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

    BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Fetching the download id received with the broadcast
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            //Checking if the received broadcast is for our enqueued download by matching download id
            if (downloadID == id) {
                Log.d(TAG, "SharedTab: download of web page completed >>> " + pathToFile);
                if (savedTitleLocal == null || imagePath == null || pathToFile == null) {
                    Toast.makeText(context, "Download is not available.Check your internet connection", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "SharedTab: PROBLEM !!! >>>" + "savedTitle = " + savedTitleLocal + ";" + "imagePath = " + imagePath + ";" );
                    return;
                }
                //add to database
                SavedArticles savedArticle = new SavedArticles(savedTitleLocal, imagePath, pathToFile);
                dbHandler = new DBHandler(context, null, null, 2);
                dbHandler.addArticle(savedArticle);
            }
        }
    };
}
