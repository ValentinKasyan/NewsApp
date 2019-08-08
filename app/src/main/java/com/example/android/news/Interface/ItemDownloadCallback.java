package com.example.android.news.Interface;


import com.example.android.news.Model.Shared.SharedResult;

//       used only in SharedResult
public interface ItemDownloadCallback {

    void onDownloadEnqueued(SharedResult downloadableItem);

    void onDownloadStarted(SharedResult downloadableItem);

    void onDownloadComplete();
}
