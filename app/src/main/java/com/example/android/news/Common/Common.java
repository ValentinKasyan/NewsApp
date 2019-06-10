package com.example.android.news.Common;


import com.example.android.news.Interface.NewsService;
import com.example.android.news.Remote.RetrofitClient;

public class Common {
    private static final String BASE_URL = "https://api.nytimes.com/";
    public static final String API_KEY = "DzqhverGeH8XEegtdHYayLn9Jh49U0Uw";

    public static NewsService getNewsService() {
        return RetrofitClient.getClient(BASE_URL).create(NewsService.class);
    }


}
