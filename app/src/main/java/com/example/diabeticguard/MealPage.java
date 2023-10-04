package com.example.diabeticguard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class MealPage extends AppCompatActivity implements View.OnClickListener {

    private WebView webview;
    private ImageView bannerLogo;

    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_meal_page);

            webview =(WebView)findViewById(R.id.webView);

            webview.setWebViewClient(new WebViewClient());
            webview.getSettings().setJavaScriptEnabled(true);
            webview.getSettings().setDomStorageEnabled(true);
            webview.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
            webview.loadUrl("https://www.diabetesfoodhub.org/all-recipes.html");

            bannerLogo = findViewById(R.id.bannerLogo);
            bannerLogo.setOnClickListener(this);
        }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bannerLogo:
                startActivity(new Intent(this, HomePage.class));
                break;
        }
    }

}
