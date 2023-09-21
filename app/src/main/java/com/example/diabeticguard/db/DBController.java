package com.example.diabeticguard.db;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class DBController extends SQLiteOpenHelper {
    private static final String LOGCAT = null;

    public static String tableTrackName = "tblTrack";
    public static String colDate = "Date";
    public static String colTime = "Time";
    public static String colLevel = "Level";
    public static String colIsFasting = "IsFasting";

    public static String tableProfileName = "tblProfile";
    public static String colUserId = "UserId";
    public static String colUserName = "UserName";
    public static String colGender = "Gender";
    public static String colAge = "Age";
    public static String colIsPregnent = "IsPregnant";
    public static String colOtherRisks = "OtherRisks";


    public DBController(Context applicationcontext) {

        super(applicationcontext, "mydb.db", null, 1);  // creating DATABASE

        Log.d(LOGCAT, "Created");

    }


    @Override
    public void onCreate(SQLiteDatabase database) {

        String trackQuery, profileQuery, stardardQuery;

        trackQuery = "CREATE TABLE IF NOT EXISTS " + tableTrackName + "( " + colDate + " TEXT, " + colTime +
                " TEXT, " + colLevel + " TEXT, " + colIsFasting + " TEXT)";
        Log.e("create Query", trackQuery);
        database.execSQL(trackQuery);

    }


    @Override
    public void onUpgrade(SQLiteDatabase database, int version_old, int current_version) {
        String query;
        query = "DROP TABLE IF EXISTS " + tableTrackName;
        database.execSQL(query);
        onCreate(database);
    }


    public ArrayList<HashMap<String, String>> getAllTracks() {

        ArrayList<HashMap<String, String>> trackList;
        trackList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT  * FROM " + tableTrackName;
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {

            do {
                //Id, Company,Name,Price
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("a", cursor.getString(0));
                map.put("b", cursor.getString(1));
                map.put("c", cursor.getString(2));
                trackList.add(map);
                Log.e("dataofList", cursor.getString(0) + "," + cursor.getString(1) + "," + cursor.getString(2));
            } while (cursor.moveToNext());
        }
        return trackList;

    }

}
