package com.example.android.news.Interface;

import com.example.android.news.Common.Common;
import com.example.android.news.Model.EmailedNews;

import retrofit2.Call;
import retrofit2.http.GET;


public interface NewsService {
    @GET("https://api.nytimes.com/svc/mostpopular/v2/emailed/30.json?api-key=" + Common.API_KEY)
    Call<EmailedNews> getNewestArticles();

}
