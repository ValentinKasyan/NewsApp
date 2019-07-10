package com.example.android.news.Interface;


import com.example.android.news.Model.Shared.SharedResult;

public interface ItemDownloadCallback {
    // TODO: 23.06.2019   использовал только SharedResult
    void onDownloadEnqueued(SharedResult downloadableItem);

    void onDownloadStarted(SharedResult downloadableItem);

    void onDownloadComplete();
}
