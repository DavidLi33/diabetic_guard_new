package com.example.diabeticguard;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;


public class DisplayStandardPage extends AppCompatActivity implements View.OnClickListener {

    private ImageView bannerLogo, displayStandard;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_standard);

        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR){
                    tts.setLanguage(Locale.US);
                    speak("Welcome! Please view blood sugar level standard");
                }
            }
        });

        bannerLogo = findViewById(R.id.bannerLogo);
        bannerLogo.setOnClickListener(this);

        displayStandard = (ImageView) this.findViewById(R.id.displayStandard);
        displayStandard.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bannerLogo:
                startActivity(new Intent(this, HomePage.class));
                break;
            case R.id.displayStandard:
                startActivity(new Intent(this, HomePage.class));
                break;
        }
    }

    public void speak(String s){
        tts.speak(s, TextToSpeech.QUEUE_FLUSH, null, DisplayStandardPage.this.hashCode()+"");
    }
    public void onPause(){
        if (tts != null){
            tts.stop();
            tts.shutdown();
        }
        super.onPause();
    }
}
