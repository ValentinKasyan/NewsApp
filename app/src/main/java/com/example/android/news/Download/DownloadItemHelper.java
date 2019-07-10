package com.example.android.news.Download;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;

import com.example.android.news.Model.Shared.SharedResult;
import com.example.android.news.R;

import java.util.ArrayList;

public class DownloadItemHelper {

//    public static ArrayList getItems(Context context) {
//        ArrayList<DownloadableItem> downloadableItems = new ArrayList<>();
//
//        if (context == null) {
//            return downloadableItems;
//        }
//
//        Resources res = context.getResources();
//        String[] imagesId = res.getStringArray(R.array.image_ids);
//        String[] imagesDisplayNamesList = res.getStringArray(R.array.image_display_names_list);
//        String[] imageDownloadUrlList = res.getStringArray(R.array.image_download_url_list);
//        TypedArray imageDownloadCoverList = res.obtainTypedArray(R.array.image_download_cover_list);
//
//        for (int i = 0; i < imagesId.length; i++) {
//            DownloadableItem downloadableItem = new DownloadableItem();
//            String itemId = imagesId[i];
//            downloadableItem.setId(itemId);
//            String downloadingStatus = getDownloadStatus(context, itemId);
//            downloadableItem.setDownloadingStatus(DownloadingStatus.getValue(downloadingStatus));
//            downloadableItem.setItemTitle(imagesDisplayNamesList[i]);
//            downloadableItem.setItemCoverId(imageDownloadCoverList.getResourceId(i, 0));
//            downloadableItem.setItemDownloadUrl(imageDownloadUrlList[i]);
//            downloadableItems.add(downloadableItem);
//        }
//        return downloadableItems;
//    }

    /**
     * This method returns the downloadable Item with the latest percent and downloading status
     * @param context
     * @param downloadableItem
     * @return
     */
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

    // TODO: 10.07.2019 запись в SharedPreferences статусов
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