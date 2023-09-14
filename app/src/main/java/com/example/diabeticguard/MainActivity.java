package com.example.diabeticguard;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView register, login, displayChart;
    private TextToSpeech tts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR){
                    tts.setLanguage(Locale.US);
                    speak("Supradata Diabetic Guard, Register or Login");
                }
            }
        });
        register = (TextView) this.findViewById(R.id.mainRegisterButton);
        register.setOnClickListener(this);

        login = (TextView) this.findViewById(R.id.mainLoginButton);
        login.setOnClickListener(this);

        displayChart = (TextView) this.findViewById(R.id.displayChartButton);
        displayChart.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.mainRegisterButton:
                startActivity(new Intent(this, RegisterUser.class));
                break;
            case R.id.mainLoginButton:
                startActivity(new Intent(this, LoginUser.class));
                break;
            case R.id.displayChartButton:
                startActivity(new Intent(this, DisplayBloodSugarChartPage.class));
                break;
        }
    }
    public void onPause(){
        if (tts != null){
            tts.stop();
            tts.shutdown();
        }
        super.onPause();
    }
    public void speak(String s){
        tts.speak(s, TextToSpeech.QUEUE_FLUSH, null, this.hashCode()+"");
        //test
    }
}