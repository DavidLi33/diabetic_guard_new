package com.example.diabeticguard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class CouponPage extends AppCompatActivity implements View.OnClickListener {

    private WebView dealWebView;
    private ImageView bannerLogo;
    private String url_path = "https://www.google.com/search?q=diabetic+good+deal+coupon";

    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_coupon_page);

        dealWebView =(WebView)findViewById(R.id.dealWebView);

        dealWebView.setWebViewClient(new WebViewClient());
        dealWebView.getSettings().setJavaScriptEnabled(true);
        dealWebView.getSettings().setDomStorageEnabled(true);
        dealWebView.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        dealWebView.loadUrl(url_path);

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
