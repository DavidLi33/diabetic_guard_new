package com.example.diabeticguard;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diabeticguard.db.DBController;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;


public class DisplayMeterInfo extends AppCompatActivity implements View.OnClickListener {

    private ImageView bannerLogo;
    private Button yesButton, noButton;
    private EditText inputLevel, inputDate, inputTime;
    private FirebaseUser currentUser;
    private String inputText;
    private MeterDataParser meterParser;
    private String myLevel;
    private String myDate;
    private String myTime;
    private int alermLevelLimit = 100;
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private DBController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_meter_info);

        bannerLogo = findViewById(R.id.bannerLogo);
        bannerLogo.setOnClickListener(this);

        controller = new DBController(this);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        yesButton = findViewById(R.id.UserInfoYes);
        yesButton.setOnClickListener(this);

        noButton = findViewById(R.id.UserInfoNo);
        noButton.setOnClickListener(this);

        inputLevel = findViewById(R.id.inputLevel);
        inputDate = findViewById(R.id.inputDate);
        inputTime = findViewById(R.id.inputTime);

        inputText = getIntent().getExtras().getString("Text");
        Log.i( "Camera Dictate Info", "input text is " + inputText );
        meterParser = new MeterDataParser(inputText);
        myLevel = meterParser.getLevel();
        myDate = meterParser.getDate();
        myTime = meterParser.getTime();
        inputLevel.setText(myLevel);
        inputDate.setText(myDate);
        inputTime.setText(myTime);

        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR) {
                        speak("Your blood sugar level is " + myLevel);
//                        while (tts.isSpeaking()) {SystemClock.sleep(500);}
                        speak("Collected date is " + myDate );
                        speak("Collected time is " + myTime );
                }
            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bannerLogo:
                startActivity(new Intent(DisplayMeterInfo.this, HomePage.class));
                break;
            case R.id.UserInfoYes:
                String displayMessage = "";
                String finalLevel = inputLevel.getText().toString().trim();
                String finalDate = inputDate.getText().toString().trim();
                String finalTime = inputTime.getText().toString().trim();
                if( finalTime.isEmpty() || finalLevel.isEmpty()) {
                    displayMessage += "Missing level or time input ";
                    speak("Missing level or time input ");
                    Toast.makeText(DisplayMeterInfo.this, "Missing level or time input ", Toast.LENGTH_SHORT).show();

                }else {
                    boolean isSucceeded = storeInputData(finalLevel, finalDate, finalTime);
                    if( isSucceeded ) {
                        displayMessage = "Stored latest record " + finalLevel;
                        speak("Stored latest record " + finalLevel);
                        Toast.makeText(DisplayMeterInfo.this, "Stored latest record " + myLevel, Toast.LENGTH_SHORT).show();
                    }
                }
//                if( Integer.parseInt(myLevel) > alermLevelLimit) {
//                    startNotification(getApplicationContext());
//                }
                startActivity(new Intent(DisplayMeterInfo.this, CameraHomePage.class).putExtra("displayResult", displayMessage));
                break;
            case R.id.UserInfoNo:
                speak("Did not store record " + myLevel);
                while (tts.isSpeaking()){}
                Toast.makeText(DisplayMeterInfo.this, "Did not store record " + myLevel, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(DisplayMeterInfo.this, CameraHomePage.class));
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getCurrentTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    public boolean storeInputData(String theLevel, String theDate, String theTime) {
        boolean isSucceeded = false;
        controller = new DBController(this);
        SQLiteDatabase db = controller.getWritableDatabase();

        try {
            db.beginTransaction();
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBController.colLevel, theLevel);
            contentValues.put(DBController.colDate, theDate);
            contentValues.put(DBController.colTime, theTime);
            contentValues.put(DBController.colUserId, currentUser.getUid());

            //if existing data found, update level value, otherwise insert as new data
            String foundId = controller.foundMatchedData(theDate, theTime, currentUser.getUid());
            if( foundId == null || foundId.isEmpty() ) {
                db.insert(DBController.tableTrackName, null, contentValues);
            }else {
                db.update(DBController.tableTrackName, contentValues,"_id = ?", new String[]{foundId});
            }

            Log.i("Camera Dictate", "Successfully stored scanned data.");
            isSucceeded = true;
            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (SQLException e) {
            Log.e("SQLError", e.getMessage().toString());
        } catch (Exception ex) {
            Log.e("Error", ex.getMessage().toString());
            if (db.inTransaction())
                db.endTransaction();
            Toast.makeText(DisplayMeterInfo.this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
        return isSucceeded;
    }

    public void speak(String s){
        tts.speak(s, TextToSpeech.QUEUE_ADD, null, this.hashCode()+"");
    }
    @Override
    public void onStop() {
        if (tts != null) {
            tts.stop();
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.shutdown();
        }
        super.onDestroy();
    }
//    public void startNotification(Context context) {
//        Intent _intent = new Intent(context, AlarmBroadcastReceiver.class);
//        _intent.putExtra("level", myLevel);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, medicine.hashCode(), _intent, FLAG_IMMUTABLE);
//        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
//        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
//    }
}