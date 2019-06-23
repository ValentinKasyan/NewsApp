package com.example.android.news.Download;


import com.example.android.news.Model.Emailed.EmailedResults;

public interface ItemDownloadCallback {
    // TODO: 23.06.2019   использовал только EmailedResults
    void onDownloadEnqueued(EmailedResults downloadableItem);

    void onDownloadStarted(EmailedResults  downloadableItem);

    void onDownloadComplete();
}
