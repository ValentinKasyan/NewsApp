package com.example.android.news.Remote;


import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

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
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        getWebPage(s);
    }

    public String getWebPage(String webPageContent) {
        return webPageContent;
    }
}
