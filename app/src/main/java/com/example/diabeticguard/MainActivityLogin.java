package com.example.diabeticguard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

public class MainActivityLogin extends AppCompatActivity implements View.OnClickListener{

    private ImageView banner;
    private EditText editEmail;
    private EditText editPassword;
    private ProgressBar progressBar;
    private ImageView register, login;
    private TextToSpeech tts;
    private final String EMAIL = "email";
    private final String PASSWORD = "password";
    public final String SESSION = "session";
    private final String SHARED_PREFS = "sharedPrefs";
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_with_login);
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR){
                    tts.setLanguage(Locale.US);
                    speak("Diabetic Guard is a data collaboration tool to help monitor and analysis blood sugar result, please start to use the system via Register or Login");
                }
            }
        });

        progressBar = findViewById(R.id.loginProgressBar);
        progressBar.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();
       //always sign out first when load main login page
        mAuth.signOut();

        banner = this.findViewById(R.id.bannerLogo);
        banner.setOnClickListener(this);

        editEmail = findViewById(R.id.loginEmail);
        editPassword = findViewById(R.id.loginPassword);

        login = findViewById(R.id.mainLoginButton);
        login.setOnClickListener(this);

        register = findViewById(R.id.mainRegisterButton);
        register.setOnClickListener(this);
        
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        //reset session data at main login page
        editor.putBoolean(SESSION, false);
        editor.putString(EMAIL, null);
        editor.putString(PASSWORD, null);
        editor.apply();

        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR){
                    tts.setLanguage(Locale.US);
                    speak("Please Login");
                }
            }
        });

    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.mainLoginButton:
                loginUser();
                break;
            case R.id.mainRegisterButton:
                startActivity(new Intent(this, RegisterUser.class));
                break;
        }
    }
    public void loginUser(){
        progressBar.setVisibility(View.VISIBLE);
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        if(email.length() == 0){
            editEmail.setError("Please enter an email");
            speak("Please enter an email");
            editEmail.requestFocus();
            progressBar.setVisibility(View.GONE);
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editEmail.setError("Please enter a valid email");
            speak("Please enter a valid email");
            editEmail.requestFocus();
            progressBar.setVisibility(View.GONE);
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>(){
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> login){
                        if(login.isSuccessful()){
                            progressBar.setVisibility(View.GONE);
                            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS , MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();

                            if (!sharedPreferences.getBoolean(SESSION, false)){
                                editor.putBoolean(SESSION, true);
                                editor.putString(EMAIL, email);
                                editor.putString(PASSWORD, password);

                                editor.apply();
                            }

                            Toast.makeText(MainActivityLogin.this, "Welcome", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(MainActivityLogin.this, HomePage.class));
                        }
                        else{
                            Toast.makeText(MainActivityLogin.this, "Please check your credentials", Toast.LENGTH_LONG).show();
                            speak("Please check your credentials");
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }
    public void speak(String s){
        tts.speak(s, TextToSpeech.QUEUE_FLUSH, null, this.hashCode()+"");
    }
    public void onPause(){
        if (tts != null){
            tts.stop();
            tts.shutdown();
        }
        super.onPause();
    }
}
