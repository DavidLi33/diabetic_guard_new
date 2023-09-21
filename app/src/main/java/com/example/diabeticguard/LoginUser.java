package com.example.diabeticguard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

public class LoginUser extends AppCompatActivity implements View.OnClickListener{
    private TextView register;
    private ImageView banner;
    private EditText editEmail;
    private EditText editPassword;
    private ProgressBar progressBar;
    private Button login;
    private TextToSpeech tts;
    private final String EMAIL = "email";
    private final String PASSWORD = "password";
    public final String SESSION = "session";
    private final String SHARED_PREFS = "sharedPrefs";
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_main);
        //^obv not for login idk j do the gui it's 11.37 at night oke


        progressBar = findViewById(R.id.loginProgressBar);
        progressBar.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();

        banner = this.findViewById(R.id.bannerLogo);
        banner.setOnClickListener(this);

        editEmail = findViewById(R.id.loginEmail);
        editPassword = findViewById(R.id.loginPassword);

        login = findViewById(R.id.loginButton);
        login.setOnClickListener(this);
        
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (!sharedPreferences.contains(SESSION)){
            editor.putBoolean(SESSION, false);
            editor.putString(EMAIL, null);
            editor.putString(PASSWORD, null);

            editor.apply();
        }
        else{
            if (sharedPreferences.getBoolean(SESSION, false)){
                String email = sharedPreferences.getString(EMAIL, "");
                String password = sharedPreferences.getString(PASSWORD, "");
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

                                    Toast.makeText(LoginUser.this, "Welcome", Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(LoginUser.this, HomePage.class));
                                }
                                else{
                                    Toast.makeText(LoginUser.this, "Please check your credentials", Toast.LENGTH_LONG).show();
                                    speak("Please check your credentials");
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        });
            }
        }
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
            case R.id.bannerLogo:
                startActivity(new Intent(LoginUser.this, MainActivity.class));
                break;
            case R.id.loginButton:
                loginUser();
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

                            Toast.makeText(LoginUser.this, "Welcome", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(LoginUser.this, HomePage.class));
                        }
                        else{
                            Toast.makeText(LoginUser.this, "Please check your credentials", Toast.LENGTH_LONG).show();
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
