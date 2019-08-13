package com.example.android.news;

import android.content.Context;
import android.content.ContextWrapper;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;

import com.example.android.news.Adapter.SavedNewsAdapter;
import com.example.android.news.Database.DBHandler;
import com.example.android.news.Database.SavedArticles;
import com.example.android.news.Remote.EmailedTab;
import com.example.android.news.Remote.SavedTab;
import com.example.android.news.Remote.SharedTab;
import com.example.android.news.Remote.ViewedTab;

import java.util.List;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private static final String TAG = "DebuggingLogs";

    SpotsDialog dialog;
    SwipeRefreshLayout swipeRefreshLayout;
    private static final String DEBUG = "DebuggingLogs";

    SavedNewsAdapter adapter;
    RecyclerView lstNews;
    RecyclerView.LayoutManager layoutManager;
    DBHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!isOnline()) {
            Log.d(TAG, "MainActivity: networkInfo >>> disconnected");
            setContentView(R.layout.tab_saved);
            dialog = new SpotsDialog(this);
            //Init View
            swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshForSaved);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    loadNews(true);
                }
            });
            lstNews = (RecyclerView) findViewById(R.id.lstNewsSaved);
            layoutManager = new LinearLayoutManager(this);
            lstNews.setLayoutManager(layoutManager);
            dbHandler = new DBHandler(this, null, null, 1);
            loadNews(false);

        }
        if (isOnline()) {
            mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
            mViewPager = (ViewPager) findViewById(R.id.container);
            mViewPager.setAdapter(mSectionsPagerAdapter);
            TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
            mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
            tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                //responsible for the location of the functional tabs
                case 0:
                    EmailedTab emailedTab = new EmailedTab();
                    return emailedTab;
                case 1:
                    SharedTab sharedTab = new SharedTab();
                    return sharedTab;
                case 2:
                    ViewedTab viewedTab = new ViewedTab();
                    return viewedTab;
                case 3:
                    SavedTab savedTab = new SavedTab();
                    return savedTab;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            return 4;
        }

    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    private void loadNews(boolean isRefreshed) {
        if (!isRefreshed) {
            List<SavedArticles> savedArticlesList = dbHandler.databaseToObject();
            if (savedArticlesList.size() > 0) {
                adapter = new SavedNewsAdapter(savedArticlesList, this.getBaseContext());
                lstNews.setAdapter(adapter);
            } else {
                dialog.show();
            }
        } else// If from Swipe to Refresh
        {
            //Dismiss refresh progressing
            List<SavedArticles> savedArticlesList = dbHandler.databaseToObject();
            adapter = new SavedNewsAdapter(savedArticlesList, this.getBaseContext());
            lstNews.setAdapter(adapter);
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    //Floating context menu
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 221:
                Log.d(DEBUG, "SavedTab: pressed delete ");
                String title = adapter.removeItem(item.getGroupId());
                dbHandler.deleteArticles(title);
                ContextWrapper cw = new ContextWrapper(this);
                Log.d(DEBUG, "SavedTab: pressed delete on favorites article with title: " + item.getTitle().toString());
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

}
