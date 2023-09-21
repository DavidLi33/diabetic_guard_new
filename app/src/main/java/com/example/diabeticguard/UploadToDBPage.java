package com.example.diabeticguard;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class UploadToDBPage extends AppCompatActivity implements View.OnClickListener{

    private TextView homePageLogo, displayChart, uploadToDB, searchButton, scanButton;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR){
                    tts.setLanguage(Locale.US);
                    speak("Welcome! Please scan or search");
                }
            }
        });
        homePageLogo = findViewById(R.id.homePageLogo);

        displayChart = (TextView) this.findViewById(R.id.displayChartButton);
        displayChart.setOnClickListener(this);

        uploadToDB = (TextView) this.findViewById(R.id.uploadToDBButton);
        uploadToDB.setOnClickListener(this);

        searchButton = findViewById(R.id.homePageSearch);
        searchButton.setOnClickListener(this);

        scanButton = findViewById(R.id.homePageScan);
        scanButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.homePageScan:
                startActivity(new Intent(UploadToDBPage.this, CameraHomePage.class));
                break;
            case R.id.homePageSearch:
                startActivity(new Intent(UploadToDBPage.this, SearchHomePage.class));
                break;
            case R.id.displayChartButton:
                startActivity(new Intent(this, DisplayBloodSugarChartPage.class));
                break;
            case R.id.uploadToDBButton:
                startActivity(new Intent(this, UploadToDBPage.class));
                break;
        }
    }
    public void speak(String s){
        tts.speak(s, TextToSpeech.QUEUE_FLUSH, null, UploadToDBPage.this.hashCode()+"");
    }
    public void onPause(){
        if (tts != null){
            tts.stop();
            tts.shutdown();
        }
        super.onPause();
    }
}