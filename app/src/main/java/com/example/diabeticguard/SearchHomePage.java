package com.example.diabeticguard;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class SearchHomePage extends AppCompatActivity implements View.OnClickListener{

    public TextView displayText, displayButton, displayLogo;
    public EditText medicineText;
    String medicineName;
    MedicineDatabase Medicine;
    FirebaseFirestore db;
    FirebaseUser usr;
    CollectionReference medicineLog;
    private TextToSpeech tts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR){
                    tts.setLanguage(Locale.US);
                    speak("Please enter a medicine name and press search");
                }
            }
        });
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_home_page);

        Medicine = new MedicineDatabase();
        try{
            Medicine.init(SearchHomePage.this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        db = FirebaseFirestore.getInstance();
        usr = FirebaseAuth.getInstance().getCurrentUser();
        medicineLog = db
                .collection("users")
                .document(usr.getUid())
                .collection("Medicines");
        displayText = findViewById(R.id.searchHomePageDrugInfo);

        displayButton = findViewById(R.id.searchHomePageButton);
        displayButton.setOnClickListener(this);

        displayLogo = findViewById(R.id.searchHomePageLogo);
        displayLogo.setOnClickListener(this);

        medicineText = findViewById(R.id.searchHomePageText);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.searchHomePageLogo:
                startActivity(new Intent(SearchHomePage.this, HomePage.class));
                break;
            case R.id.searchHomePageButton:
                medicineName = medicineText.getText().toString();
                if (Medicine.isMedicine(medicineName)){
                    //they entered a valid medicine!
                    DocumentReference specificMedicine = medicineLog.document(medicineName.toLowerCase(Locale.ROOT));
                    specificMedicine.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()){
                                DocumentSnapshot doc = task.getResult();
                                if (doc.exists()){
                                    ArrayList<String> lastTimesTaken = (ArrayList<String>) doc.get("lastTimeTaken");
                                    displayText.setText("You last took " + medicineName + " at " + lastTimesTaken.get(lastTimesTaken.size()-1));
                                    speak("You last took " + medicineName + " at " + lastTimesTaken.get(lastTimesTaken.size()-1));
                                }
                                else{
                                    displayText.setText("You have never taken this medicine before");
                                    speak("You have never taken this medicine before");
                                }
                            }
                            else{
                                Toast.makeText(SearchHomePage.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    break;
                }
                else{
                    medicineText.setError("Please Enter a valid medicine");
                    medicineText.requestFocus();
                    break;
                }
        }
    }
    public void speak(String s){
        tts.speak(s, TextToSpeech.QUEUE_FLUSH, null, SearchHomePage.this.hashCode()+"");
    }
    public void onPause(){
        if (tts != null){
            tts.stop();
            tts.shutdown();
        }
        super.onPause();
    }
}