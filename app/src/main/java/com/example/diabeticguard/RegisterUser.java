package com.example.diabeticguard;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Patterns;
import android.view.View;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RegisterUser extends AppCompatActivity implements View.OnClickListener{

    private ImageView banner, register;
    private EditText editFullName, editEmail,editPassword, editBirthYear, editGender ;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private TextToSpeech tts;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR){
                    tts.setLanguage(Locale.US);
                    speak("Please Register");
                }
            }
        });

        banner = this.findViewById(R.id.bannerLogo);
        banner.setOnClickListener(this);

        register = this.findViewById(R.id.registerButton);
        register.setOnClickListener(this);

        editFullName = findViewById(R.id.fullname);
        editEmail = findViewById(R.id.email);
        editPassword = findViewById(R.id.password);

        editBirthYear = findViewById(R.id.birthyear);
        editGender = findViewById(R.id.gender);

        //TODO need to store other user profile in database, eg. name, age, gender ...

        progressBar = findViewById(R.id.registerProgressBar);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bannerLogo:
                startActivity(new Intent(RegisterUser.this, MainActivityLogin.class));
                break;
            case R.id.registerButton:
                registerUser();
                break;
        }
    }
    public void registerUser(){
        String fullName = editFullName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (fullName.isEmpty()){
            editFullName.setError("Full Name is Required");
            speak("Full Name is required");
            editFullName.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editEmail.setError("Valid Email is Required");
            speak("Valid Email is required");
            editEmail.requestFocus();
            return;
        }
        if (password.length() < 6){
            editPassword.setError("Password must be at least 6 characters");
            speak("Password needs to be at least 6 characters long");
            editPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Map<String, Object> user = new HashMap<>();
                            user.put("Email", email);
                            user.put("fullName", fullName);
                            String uid = task.getResult().getUser().getUid();
                            db.collection("users").document(uid).set(user);
                            speak("User Registered Successfully");
                            Toast.makeText(RegisterUser.this, "User Registered Successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterUser.this, MainActivityLogin.class));
                        }
                        else{
                            speak("Failed to Register");
                            Toast.makeText(RegisterUser.this, "Failed To Register", Toast.LENGTH_LONG).show();
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