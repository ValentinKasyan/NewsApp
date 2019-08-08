package com.example.android.news.Download;


import android.app.DownloadManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.example.android.news.Model.Shared.SharedResult;

import java.io.File;

import io.reactivex.ObservableEmitter;

public class RxDownloadManagerHelper {

    private final static int DOWNLOAD_QUERY_DELAY_PARAM = 200;
    private final static int DOWNLOAD_COMPLETE_PERCENT = 100;
    private final static int PERCENT_MULTIPLIER = 100;
    private final static int MIN_DOWNLOAD_PERCENT_DIFF = 3;
    private final static int INVALID_DOWNLOAD_ID = -1;
    private static final String DEBUG = "DebuggingLogs";
    private DownloadManager.Request request;


    public static long enqueueDownload(DownloadManager downloadManager,
                                       String downloadUrl) {
        if (downloadManager == null || downloadUrl == null || downloadUrl.equals("")) {
            return INVALID_DOWNLOAD_ID;
        }
        Log.d(DEBUG, "class RxDownloadManagerHelper - enqueueDownload() ");
        Uri uri = Uri.parse(downloadUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        return downloadManager.enqueue(request);
    }

    //This method will be called upon every 'x' milliseconds to know the percentage of a download.
    // This method will only emit the percent download, if the current percent download is 5%
    // greater than the previous percent download.
    public static void queryDownloadPercents(final DownloadManager downloadManager,
                                             final SharedResult downloadableItem,
                                             final ObservableEmitter percentFlowableEmiitter) {

        //If the emitter has been disposed, then return.
        if (downloadManager == null || downloadableItem == null || percentFlowableEmiitter == null
                || percentFlowableEmiitter.isDisposed()) {
            return;
        }

        long lastEmittedDownloadPercent = downloadableItem.getLastEmittedDownloadPercent();

        DownloadableResult downloadableResult = getDownloadResult(downloadManager, downloadableItem
                .getDownloadId());

        if (downloadableResult == null) {
            return;
        }

        //Get the current DownloadPercent and download status
        int currentDownloadPercent = downloadableResult.getPercent();
        int downloadStatus = downloadableResult.getDownloadStatus();
        downloadableItem.setItemDownloadPercent(currentDownloadPercent);
        if ((currentDownloadPercent - lastEmittedDownloadPercent >= MIN_DOWNLOAD_PERCENT_DIFF) ||
                currentDownloadPercent == DOWNLOAD_COMPLETE_PERCENT) {
            percentFlowableEmiitter.onNext(downloadableItem);
            downloadableItem.setLastEmittedDownloadPercent(currentDownloadPercent);
        }
        Log.d(DEBUG, "class RxDownloadManagerHelper - queryDownloadPercents()" +
                " Querying the DB: DownloadStatus is " + downloadStatus + " and downloadPercent is " +
                "" + currentDownloadPercent);
        switch (downloadStatus) {
            case DownloadManager.STATUS_FAILED:
                break;

            case DownloadManager.STATUS_SUCCESSFUL:
                break;

            case DownloadManager.STATUS_PENDING:
            case DownloadManager.STATUS_RUNNING:
            case DownloadManager.STATUS_PAUSED:
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        queryDownloadPercents(downloadManager, downloadableItem, percentFlowableEmiitter);
                    }
                }, DOWNLOAD_QUERY_DELAY_PARAM);
                break;

            case DownloadManager.ERROR_FILE_ERROR:
                break;
        }
    }

    private static DownloadableResult getDownloadResult(DownloadManager downloadManager,
                                                        long downloadId) {

        Log.d(DEBUG, "class RxDownloadManagerHelper - getDownloadResult() ");
        //Create a query with downloadId as the filter.
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);

        //Create an instance of downloadable result
        DownloadableResult downloadableResult = new DownloadableResult();

        Cursor cursor = null;
        try {
            cursor = downloadManager.query(query);
            if (cursor == null || !cursor.moveToFirst()) {
                return downloadableResult;
            }
            //Get the download percent
            float bytesDownloaded =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            float bytesTotal =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
            int downloadPercent = (int) ((bytesDownloaded / bytesTotal) * PERCENT_MULTIPLIER);
            if (downloadPercent <= Constants.DOWNLOAD_COMPLETE_PERCENT) {
                downloadableResult.setPercent(downloadPercent);
            }
            //Get the download status
            int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);

            int downloadStatus = cursor.getInt(columnIndex);
            downloadableResult.setDownloadStatus(downloadStatus);
            // TODO: 08.08.2019  try to get pass of downloaded html file and then save this pass to db,  content://downloads/all_downloads/1407
            String downloadFilepath = null;
            String downloadFileLocalUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
            if (downloadFileLocalUri != null) {
                File mfile = new File(Uri.parse(downloadFileLocalUri).getPath());
                downloadFilepath = mfile.getAbsolutePath();
            }
            downloadableResult.setPathToFile(downloadFileLocalUri);
            Log.d(DEBUG, "class RxDownloadManagerHelper : downloadFileLocalUri - " + downloadFileLocalUri + " ; " + "downloadFilepath - " + downloadFilepath);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return downloadableResult;
    }


}
