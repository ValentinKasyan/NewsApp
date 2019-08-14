package com.example.android.news.Interface;

import com.example.android.news.Common.Common;
import com.example.android.news.Model.Emailed.EmailedNews;
import com.example.android.news.Model.Shared.SharedNews;
import com.example.android.news.Model.Viewed.ViewedNews;

import io.reactivex.Observable;
import retrofit2.http.GET;


public interface NewsService {

    @GET("https://api.nytimes.com/svc/mostpopular/v2/emailed/30.json?api-key=" + Common.API_KEY)
    Observable<EmailedNews> getEmailedArticles();

    @GET("https://api.nytimes.com/svc/mostpopular/v2/shared/1/facebook.json?api-key=" + Common.API_KEY)
    Observable<SharedNews> getSharedArticles();

    @GET("https://api.nytimes.com/svc/mostpopular/v2/viewed/1.json?api-key=" + Common.API_KEY)
    Observable<ViewedNews> getViewedArticles();

}