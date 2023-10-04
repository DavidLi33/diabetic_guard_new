package com.example.diabeticguard;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class HomePage extends AppCompatActivity implements View.OnClickListener{

    private ImageView bannerLogo,displayStandard, displayChart,
            uploadToDB, searchButton, scanButton, mealLogo;

    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR){
                    tts.setLanguage(Locale.US);
                    speak("Welcome! At home dashboard, can view standard, upload your records and track blood sugar level, as well as scan to load medicines and search them");
                }
            }
        });
        bannerLogo = findViewById(R.id.bannerLogo);
        bannerLogo.setOnClickListener(this);

        displayStandard = (ImageView) this.findViewById(R.id.homePageViewStandard);
        displayStandard.setOnClickListener(this);

        displayChart = (ImageView) this.findViewById(R.id.homePageViewChart);
        displayChart.setOnClickListener(this);

        uploadToDB = (ImageView) this.findViewById(R.id.homePageUpload);
        uploadToDB.setOnClickListener(this);

        searchButton = findViewById(R.id.homePageSearch);
        searchButton.setOnClickListener(this);

        scanButton = findViewById(R.id.homePageScan);
        scanButton.setOnClickListener(this);

        mealLogo = findViewById(R.id.homePageMeal);
        mealLogo.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bannerLogo:
                startActivity(new Intent(this, MainActivityLogin.class));
                break;
            case R.id.homePageScan:
                startActivity(new Intent(HomePage.this, CameraHomePage.class));
                break;
            case R.id.homePageSearch:
                //startActivity(new Intent(HomePage.this, SearchHomePage.class));
                startActivity(new Intent(HomePage.this, CouponPage.class));
                break;
            case R.id.homePageViewChart:
                startActivity(new Intent(this, DisplayBloodSugarChartPage.class));
                break;
            case R.id.homePageUpload:
                startActivity(new Intent(this, UploadToDBPage.class));
                break;
            case R.id.homePageViewStandard:
                startActivity(new Intent(this, DisplayStandardPage.class));
                break;
            case R.id.homePageMeal:
                startActivity(new Intent(this, MealPage.class));
                break;
        }
    }
    public void speak(String s){
        tts.speak(s, TextToSpeech.QUEUE_FLUSH, null, HomePage.this.hashCode()+"");
    }
    public void onPause(){
        if (tts != null){
            tts.stop();
            tts.shutdown();
        }
        super.onPause();
    }
}