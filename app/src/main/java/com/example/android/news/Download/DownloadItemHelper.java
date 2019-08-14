package com.example.android.news.Download;


import android.content.Context;
import android.content.SharedPreferences;

import com.example.android.news.Model.Shared.SharedResult;

public class DownloadItemHelper {

    //    This method returns the downloadable Item with the latest percent and downloading status
    public static SharedResult getItem(Context context, SharedResult
            downloadableItem) {
        if (context == null || downloadableItem == null) {
            return downloadableItem;
        }
        String downloadingStatus = DownloadItemHelper.getDownloadStatus(context, downloadableItem.getPositionId());
        int downloadPercent = DownloadItemHelper.getDownloadPercent(context, downloadableItem.getPositionId());
        downloadableItem.setDownloadingStatus(DownloadingStatus.getValue(downloadingStatus));
        downloadableItem.setItemDownloadPercent(downloadPercent);
        return downloadableItem;
    }

    public static String getDownloadStatus(Context context, String itemId) {
        SharedPreferences preferences =
                context.getSharedPreferences(Constants.SHARED_PREFERENCES, Context
                        .MODE_PRIVATE);
        return preferences.getString(Constants.DOWNLOAD_PREFIX + itemId,
                DownloadingStatus.NOT_DOWNLOADED.getDownloadStatus());
    }

    public static void persistItemState(Context context, SharedResult downloadableItem) {
        DownloadItemHelper.setDownloadPercent(context, downloadableItem.getPositionId(),
                downloadableItem.getItemDownloadPercent());
        DownloadItemHelper.setDownloadStatus(context, downloadableItem.getPositionId(),
                downloadableItem.getDownloadingStatus());
    }

    public static void setDownloadStatus(Context context, String itemId, DownloadingStatus
            downloadingStatus) {
        SharedPreferences preferences =
                context.getSharedPreferences(Constants.SHARED_PREFERENCES, Context
                        .MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.DOWNLOAD_PREFIX + itemId, downloadingStatus.getDownloadStatus());
        editor.commit();
    }

    public static void setDownloadPercent(Context context, String itemId, int percent) {
        SharedPreferences preferences =
                context.getSharedPreferences(Constants.SHARED_PREFERENCES, Context
                        .MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(Constants.PERCENT_PREFIX + itemId, percent);
        editor.commit();
    }

    public static int getDownloadPercent(Context context, String itemId) {
        SharedPreferences preferences =
                context.getSharedPreferences(Constants.SHARED_PREFERENCES, Context
                        .MODE_PRIVATE);
        return preferences.getInt(Constants.PERCENT_PREFIX + itemId, 0);
    }
}
