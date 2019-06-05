package com.example.android.news.Remote;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.news.Adapter.SearchNewsAdapter;
import com.example.android.news.Common.Common;
import com.example.android.news.Common.PrefConfig;
import com.example.android.news.Interface.NewsService;
import com.example.android.news.Model.Search.Doc;
import com.example.android.news.Model.Search.SearchNews;
import com.example.android.news.R;

import java.util.List;

import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

;

public class SearchTab extends Fragment {
    SpotsDialog dialog;
    NewsService mService;
    TextView top_author, top_title;
    SearchView searchView;
    SwipeRefreshLayout swipeRefreshLayout;
    private static final String TAG = " SearchTab";
    String searchQuery;
    public static PrefConfig prefConfig;


    SearchNewsAdapter adapter;
    RecyclerView lstNews;
    RecyclerView.LayoutManager layoutManager;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_search, container, false);
        //Init Service
        mService = Common.getNewsService();

        dialog = new SpotsDialog(getContext());
        //Init View
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshForSearch);
        searchView = (SearchView) rootView.findViewById(R.id.search_view);
        searchView.setQueryHint("Search news ...");
        searchQuery = searchView.getQuery().toString();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadNewsSearch(true);
            }
        });
        top_title = (TextView) rootView.findViewById(R.id.top_title);
        lstNews = (RecyclerView) rootView.findViewById(R.id.lstNewsSearch);
        lstNews.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        lstNews.setLayoutManager(layoutManager);

        prefConfig = new PrefConfig(getContext());

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        loadNewsSearch(false);
        return rootView;
    }

    public void loadNewsSearch(boolean isRefreshed) {
        if (searchQuery.length() == 0) {
            if (!isRefreshed) {
                dialog.show();
                mService.getSearchedArticles()
                        .enqueue(new Callback<SearchNews>() {
                            @Override
                            public void onResponse(@NonNull Call<SearchNews> call, @NonNull Response<SearchNews> response) {
                                if (response.body() != null && response.body().getResponse() != null) {
                                    dialog.dismiss();
                                    //Load remain articles
                                    List<Doc> removeFirstItem = response.body().getResponse().getDocs();
                                    adapter = new SearchNewsAdapter(removeFirstItem, getActivity().getBaseContext());
                                    lstNews.setAdapter(adapter);
                                } else {
                                    Log.d(TAG, "fail : " + "response = " + response + "; " + "response.body() = " + response.body());
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<SearchNews> call, @NonNull Throwable t) {
                                Log.d(TAG, "fail" + t.getMessage());
                                showAlertDialog(t);
                            }
                        });

            } else// If from Swipe to Refresh
            {

                dialog.show();
                //fetch new data
                mService.getSearchedArticles().enqueue(new Callback<SearchNews>() {
                    @Override
                    public void onResponse(@NonNull Call<SearchNews> call, @NonNull Response<SearchNews> response) {
                        if (response.body() != null && response.body().getResponse() != null) {
                            dialog.dismiss();
                            //Load remain articles
                            List<Doc> listItems = response.body().getResponse().getDocs();
                            adapter = new SearchNewsAdapter(listItems, getActivity().getBaseContext());
                            lstNews.setAdapter(adapter);
                        } else {
                            Log.d(TAG, "fail : " + "response = " + response + "; " + "response.body() = " + response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<SearchNews> call, Throwable t) {
                        showAlertDialog(t);
                        Log.d(TAG, "fail" + t.getMessage());
                    }
                });
                //Dismiss refresh progressing
                swipeRefreshLayout.setRefreshing(false);
            }
        } else {

        }
    }

    public void showAlertDialog(Throwable throwable) {
        new AlertDialog.Builder(getContext())
                .setTitle("Error")
                .setMessage(throwable.getLocalizedMessage())
                .setPositiveButton("Close", null)
                .show();

    }
}


