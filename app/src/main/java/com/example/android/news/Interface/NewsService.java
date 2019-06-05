package com.example.android.news.Interface;

import com.example.android.news.Common.Common;
import com.example.android.news.Model.Emailed.EmailedNews;
import com.example.android.news.Model.Search.SearchNews;
import com.example.android.news.Model.Shared.SharedNews;
import com.example.android.news.Model.Viewed.ViewedNews;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface NewsService {

    @GET("https://api.nytimes.com/svc/mostpopular/v2/emailed/30.json?api-key=" + Common.API_KEY)
    Call<EmailedNews> getEmailedArticles();

    @GET("https://api.nytimes.com/svc/mostpopular/v2/shared/1/facebook.json?api-key=" + Common.API_KEY)
    Call<SharedNews> getSharedArticles();

    @GET("https://api.nytimes.com/svc/mostpopular/v2/viewed/1.json?api-key=" + Common.API_KEY)
    Call<ViewedNews> getViewedArticles();

    @GET("https://api.nytimes.com/svc/search/v2/articlesearch.json?q="+Common.SEARCH_QUERY+"&api-key=" + Common.API_KEY)
    Call<SearchNews> getSearchedArticles();
}