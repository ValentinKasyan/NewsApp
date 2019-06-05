package com.example.android.news.Common;


import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.example.android.news.R;

public class PrefConfig {
    private SharedPreferences sharedPreferences;
    private Context context;


    public PrefConfig(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.pref_file), Context.MODE_PRIVATE);
    }
    public void searchQuery (String search) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(context.getString(R.string.pref_search_query), search);
        editor.commit();
    }
    public void displayToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

}
