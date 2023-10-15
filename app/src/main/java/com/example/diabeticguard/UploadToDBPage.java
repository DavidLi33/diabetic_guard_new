package com.example.diabeticguard;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.diabeticguard.db.DBController;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class UploadToDBPage extends ListActivity implements View.OnClickListener {

    ImageView bannerLogo;
    TextView lbl;
    DBController controller;
    FirebaseUser currentUser;
    Button btnimport;
    ListView lv;
    final Context context = this;
    ListAdapter adapter;

    ArrayList<HashMap<String, String>> myList;
    public static final int requestcode = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_upload_to_db);

        controller = new DBController(context);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        bannerLogo = findViewById(R.id.bannerLogo);
        bannerLogo.setOnClickListener(this);

        lbl = (TextView) findViewById(R.id.txtresulttext);
        btnimport = (Button) findViewById(R.id.btnupload);
        lv = getListView();
        btnimport.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent fileintent = new Intent(Intent.ACTION_GET_CONTENT);
                fileintent.setType("*/*");

                try {
                    startActivityForResult(fileintent, requestcode);
                } catch (ActivityNotFoundException e) {
                    lbl.setText("No activity can handle picking a file. Showing alternatives.");
                }
            }
        });

        myList = controller.getAllTracks(currentUser.getUid());
        if (myList.size() != 0) {
            ListView lv = getListView();
            ListAdapter adapter = new SimpleAdapter(UploadToDBPage.this, myList,
                    R.layout.lst_template, new String[]{"date", "time", "level"}, new int[]{
                    R.id.txtDate, R.id.txtTime, R.id.txtLevel});
            setListAdapter(adapter);
            lbl.setText("Display Results");
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (data == null)

            return;
        switch (requestCode) {

            case requestcode:
                FileReader fileReader = null;
                Uri uri = data.getData();
                Log.i("UPLOAD", uri.toString());

                controller = new DBController(this);
                SQLiteDatabase db = controller.getWritableDatabase();

                // delete for reset, start from beginning
                //db.execSQL("delete from " + DBController.tableTrackName + " where " + DBController.colUserId + " = \'" + currentUser.getUid() + "\'");

                try {

                    if (resultCode == RESULT_OK) {
                        Log.i("RESULT CODE", "OK");
                        try {
                            String readImagePermission = "";
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                                readImagePermission = Manifest.permission.READ_MEDIA_IMAGES;
                            else
                                readImagePermission = Manifest.permission.READ_EXTERNAL_STORAGE;
                            int permissionCheck = ContextCompat.checkSelfPermission(this, readImagePermission);
                            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                                Log.i("File Permission", "UploadToDBPage Permission not granted.");
                                ActivityCompat.requestPermissions(this, new String[]{readImagePermission}, 225);
                            } else {
                                Log.i(TAG, "You already have permission!");
                            }

                            ArrayList<ContentValues> contentValuesList = readDataFromCSV(uri);

                            db.beginTransaction();

                            for( ContentValues contentValues : contentValuesList ) {
                                //execute updating instead of insertijng if the same date/time exists in db
                                String theDate = (String)contentValues.get(DBController.colDate);
                                String theTime = (String)contentValues.get(DBController.colTime);
                                String theLevel = (String)contentValues.get(DBController.colLevel);

                                //if existing data found, update level value, otherwise insert as new data
                                String foundId = controller.foundMatchedData(theDate, theTime, currentUser.getUid());
                                if( foundId == null || foundId.isEmpty() ) {
                                    db.insert(DBController.tableTrackName, null, contentValues);
                                }else {
                                    db.update(DBController.tableTrackName, contentValues,"_id = ?", new String[]{foundId});
                                }
                                 lbl.setText("Successfully Updated Database.");
                                Log.i("UPLOAD", "Successfully Updated Database.");
                            }
                            db.setTransactionSuccessful();

                            db.endTransaction();

                        } catch (SQLException e) {
                            Log.e("SQLError", e.getMessage().toString());
                        }
                    } else {
                        Log.e("RESULT CODE", "InValid");
                        if (db.inTransaction())
                            db.endTransaction();
                        Toast.makeText(UploadToDBPage.this, "Only CSV files allowed.", Toast.LENGTH_LONG).show();

                    }
                } catch (Exception ex) {
                    Log.e("Error", ex.getMessage().toString());
                    if (db.inTransaction())
                        db.endTransaction();
                    //Toast.makeText(UploadToDBPage.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                }

        }

        myList = controller.getAllTracks(currentUser.getUid());

        if (myList.size() != 0) {

            ListView lv = getListView();

            ListAdapter adapter = new SimpleAdapter(UploadToDBPage.this, myList,

                    R.layout.lst_template, new String[]{"date", "time", "level"}, new int[]{
                    R.id.txtDate, R.id.txtTime, R.id.txtLevel});

            setListAdapter(adapter);

            lbl.setText("Data Imported");

        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bannerLogo:
                startActivity(new Intent(UploadToDBPage.this, HomePage.class));
                break;
        }
    }

    private ArrayList<ContentValues> readDataFromCSV(Uri uri) {
        ArrayList<ContentValues> contentValuesList = new ArrayList<ContentValues>();
        //Resources res = getResources();
        // Make Sure You Specify Your CSV File Name in My Case the CSV File Name is gfg
        //InputStream inputStream = res.openRawResource(R.raw.blood_sugar_track_0);

        String userId = currentUser.getUid();
        try {
            InputStream inputStream = this.getContentResolver().openInputStream(uri);
            CSVReader reader = new CSVReader(new InputStreamReader(inputStream));
            String[] nextLine;
            // Flag to skip the first line of the CSV file that is header
            String mostRecent;
            boolean isFirstLine = true;
            while ((nextLine = reader.readNext()) != null) {
                if (isFirstLine) {
                    // Skiping the first line header of the CSV file
                    isFirstLine = false;
                    continue;
                }
                if (nextLine.length >= 3) {
                    // Extracting the year from the CSV data
                    String date = nextLine[0].trim();
                    String time = nextLine[1].trim();
                    String level = nextLine[2].trim() ;
                    ContentValues  contentValues = new ContentValues();
                    contentValues.put(DBController.colDate, date);
                    contentValues.put(DBController.colTime, time);
                    contentValues.put(DBController.colLevel, level);
                    contentValues.put(DBController.colUserId, userId);
                    contentValuesList.add( contentValues );

                    Log.i( "READ FILE in LINE", date + ", " + time + ", " + level);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contentValuesList;
    }

}