package com.example.android.news.Remote;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.news.Adapter.EmailedNewsAdapter;
import com.example.android.news.Common.Common;
import com.example.android.news.Database.DBHandler;
import com.example.android.news.Database.SavedArticles;
import com.example.android.news.Interface.NewsService;
import com.example.android.news.Model.Emailed.EmailedNews;
import com.example.android.news.Model.Emailed.EmailedResults;
import com.example.android.news.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmailedTab extends Fragment {

    ImageView imageViewEmailed;
    SpotsDialog dialog;
    NewsService mService;
    TextView top_title;
    SwipeRefreshLayout swipeRefreshLayout;
    private static final String TAG = "DebuggingLogs";
    String webHotURL = "";
    String webPageContent ;
    EmailedNewsAdapter adapter;
    RecyclerView lstNews;
    RecyclerView.LayoutManager layoutManager;
    String imagePath;

    DBHandler dbHandler;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_emailed, container, false);
        //Init Service
        mService = Common.getNewsService();

        dialog = new SpotsDialog(getContext());
        //Init View
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshForEmailed);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadNewsEmailed(true);
            }
        });

        imageViewEmailed = (ImageView) rootView.findViewById(R.id.top_image_emailed);
        top_title = (TextView) rootView.findViewById(R.id.top_title);

        lstNews = (RecyclerView) rootView.findViewById(R.id.lstNewsEmailed);
        lstNews.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        lstNews.setLayoutManager(layoutManager);
        dbHandler = new DBHandler(getContext(), null, null, 2);
        loadNewsEmailed(false);
        // Create the event notifier and pass ourself to it.

        imageViewEmailed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent detail = new Intent(getActivity().getBaseContext(), DetailArticle.class);
                detail.putExtra("webURL", webHotURL);
                startActivity(detail);
            }
        });
        return rootView;
    }

    public void loadNewsEmailed(boolean isRefreshed) {
        if (!isRefreshed) {
            dialog.show();
            mService.getEmailedArticles()
                    .enqueue(new Callback<EmailedNews>() {
                        @Override
                        public void onResponse(@NonNull Call<EmailedNews> call, @NonNull Response<EmailedNews> response) {
                            if (response.body() != null && response.body().getResults() != null) {
                                dialog.dismiss();
                                EmailedResults results = response.body().getResults().get(0);
                                //get first article
                                if (results != null && results.getMedia().get(0).getMediaMetadata().get(2).getUrl() != null) {
                                    Log.d(TAG, "EmailedTab: response for getImage : " + "response = " + response + "; " + "response.body() = " + response.body());
                                    getImage(response);
                                }
                                assert results != null;
                                if (results.getTitle() != null && results.getMedia().get(0).getCopyright() != null && results.getUrl() != null) {
                                    top_title.setText(results.getTitle());
                                    webHotURL = results.getUrl();
                                }
                                //Load remain articles
                                List<EmailedResults> removeFirstItem = response.body().getResults();
                                //Because we already load first item to show on Diagonal Layout
                                // So we need remove it
                                removeFirstItem.remove(0);
                                adapter = new EmailedNewsAdapter(removeFirstItem, getActivity().getBaseContext());
                                lstNews.setAdapter(adapter);
                            } else {
                                Log.d(TAG, "EmailedTab: fail : " + "response = " + response + "; " + "response.body() = " + response.body());
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<EmailedNews> call, @NonNull Throwable t) {
                            Log.d(TAG, "EmailedTab: fail" + t.getMessage());
                            showAlertDialog(t);
                        }
                    });

        } else// If from Swipe to Refresh
        {
            dialog.show();
            //fetch new data
            mService.getEmailedArticles().enqueue(new Callback<EmailedNews>() {
                @Override
                public void onResponse(@NonNull Call<EmailedNews> call, @NonNull Response<EmailedNews> response) {
                    if (response.body() != null && response.body().getResults() != null) {
                        dialog.dismiss();
                        EmailedResults results = response.body().getResults().get(0);
                        //get first article
                        if (results != null && results.getMedia().get(0).getMediaMetadata().get(2).getUrl() != null) {
                            getImage(response);
                        }
                        assert results != null;
                        if (results.getTitle() != null && results.getMedia().get(0).getCopyright() != null && results.getUrl() != null) {
                            top_title.setText(results.getTitle());
                            webHotURL = results.getUrl();
                        }
                        //Load remain articles
                        List<EmailedResults> removeFirstItem = response.body().getResults();

                        //Because we already load first item to show on Diagonal Layout
                        // So we need remove it
                        removeFirstItem.remove(0);
                        adapter = new EmailedNewsAdapter(removeFirstItem, getActivity().getBaseContext());
                        lstNews.setAdapter(adapter);
                    } else {
                        Log.d(TAG, "EmailedTab: fail : " + "response = " + response + "; " + "response.body() = " + response.body());
                    }
                }

                @Override
                public void onFailure(Call<EmailedNews> call, Throwable t) {
                    showAlertDialog(t);
                    Log.d(TAG, "EmailedTab: fail" + t.getMessage());
                }
            });
            //Dismiss refresh progressing
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    //Floating context menu
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 121:
                Log.d(TAG, "EmailedTab: pressed add to favorites in Emailed tab");

                String savedTitle = adapter.getItemTitleTransaction(item.getGroupId());
                String urlImg = adapter.getItemImageUrlTransaction(item.getGroupId());
                Picasso.get().load(urlImg).into(picassoImageTarget(getContext(), "imageDir"));
                WebPageDownloader webPageDownloader = new WebPageDownloader();
                String urlArticle = adapter.getItemArticleUrlTransaction(item.getGroupId());
                try {

                    webPageDownloader.execute(urlArticle);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (imagePath == null || webPageContent==null) {

                    // TODO: 12.06.2019 заменить,переделать
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.d(TAG, "EmailedTab: fail" + e.getMessage());
                    }
                }
                SavedArticles savedArticle = new SavedArticles(savedTitle, imagePath);
                dbHandler.addArticle(savedArticle);

                Log.d(TAG, "EmailedTab: pressed add to favorites article with title: " + item.getTitle().toString());
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }


    private void getImage(@NonNull Response<EmailedNews> response) {
        Picasso.get()
                .load(response.body().getResults().get(0).getMedia().get(0).getMediaMetadata().get(2).getUrl())
                .into(imageViewEmailed);
    }

    private void showAlertDialog(Throwable throwable) {
        new AlertDialog.Builder(getContext())
                .setTitle("Error")
                .setMessage(throwable.getLocalizedMessage())
                .setPositiveButton("Close", null)
                .show();

    }

    Target picassoImageTarget(Context context, final String imageDir) {
        Log.d("picassoImageTarget", " picassoImageTarget");
        ContextWrapper cw = new ContextWrapper(context);
        final File directory = cw.getDir(imageDir, Context.MODE_PRIVATE); // path to /data/data/yourapp/app_imageDir
        return new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String imageName = String.valueOf(System.currentTimeMillis()) + ".jpeg";
                        imagePath = imageName;
                        final File myImageFile = new File(directory, imageName); // Create image file
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(myImageFile);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d(TAG, "EmailedTab: fail" + e.getMessage());
                        } finally {
                            try {
                                fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.d(TAG, "EmailedTab: fail" + e.getMessage());
                            }
                        }
                        Log.i("DebuggingLogs", "EmailedTab: image saved to >>>" + myImageFile.getAbsolutePath());

                    }
                }).start();

            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }


            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                if (placeHolderDrawable != null) {

                }
            }
        };
    }

    public class WebPageDownloader extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... urls) {

            StringBuffer webPageContentStringBuffer = new StringBuffer();
            if (urls.length <= 0) {
                return "Invalid URL";
            }

            try {
                String url = urls[0];
                URL webUrl = new URL(url);
                InputStream webPageDataStream = webUrl.openStream();
                InputStreamReader webPageDataReader = new InputStreamReader(webPageDataStream);
                int maxBytesToRead = 1024;
                char[] buffer = new char[maxBytesToRead];
                int bytesRead = webPageDataReader.read(buffer);

                while (bytesRead != -1) {
                    webPageContentStringBuffer.append(buffer, 0, bytesRead);
                    bytesRead = webPageDataReader.read(buffer);
                }

                return webPageContentStringBuffer.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return "Failed to get webpage content";
        }

        @Override
        protected void onPostExecute(String webContent) {
            super.onPostExecute(webContent);
            setWebPageContent(webContent);
        }
    }

    void setWebPageContent(String webPageContent) {
        this.webPageContent = webPageContent;
    }

}
