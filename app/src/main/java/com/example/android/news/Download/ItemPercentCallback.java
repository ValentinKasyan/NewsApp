package com.example.android.news.Download;

import com.example.android.news.Model.Emailed.EmailedResults;

public interface ItemPercentCallback {
    // TODO: 23.06.2019   использовал только EmailedResults
    void updateDownloadableItem(EmailedResults downloadableItem);
}
