package com.example.android.news.Remote;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.example.android.news.R;

import dmax.dialog.SpotsDialog;

public class DetailArticle extends AppCompatActivity {
    WebView webView;
    private String pathToFile;
    SpotsDialog dialog;
    private static final String TAG = "DebuggingLogs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_article);

        dialog = new SpotsDialog(this);
        // TODO: 12.08.2019 commit
//        dialog.show();
        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                dialog.dismiss();
            }
        });
        // TODO: 11.08.2019 uncomit 
//        //page loading from a network
//        if (getIntent() != null) {
//            if (!getIntent().getStringExtra("webURL").isEmpty())
//                webView.loadUrl(getIntent().getStringExtra("webURL"));
//        }
        //page loading from external storage
        if (getIntent() != null) {
            if (getIntent().getStringExtra("pathToFile").isEmpty()) {
                Log.d("DebuggingLogs", "DetailArticle: Download failed !!! pathToFile is Empty >>>" + getIntent().getStringExtra("pathToFile").toString());
            } else if (!isExternalStorageReadable()) {
                Toast.makeText(this, "Download failed " + " External Storage is unreadable ! ", Toast.LENGTH_LONG).show();
                Log.d("DebuggingLogs", "DetailArticle: Download failed !!! External Storage is unreadable !" );
            } else {
                webView.getSettings().setSaveFormData(true);
                webView.getSettings().setBuiltInZoomControls(true);
                webView.setWebViewClient(new MyWebViewClient());
                pathToFile = getIntent().getStringExtra("pathToFile");
                String path = "file://" + pathToFile;
                webView.loadUrl(path);
            }
        }
    }

    private class MyWebViewClient extends WebViewClient {

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        //show the web page in webview but not in web browser
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(request.getUrl().toString());
            return true;
        }

        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    // Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}
