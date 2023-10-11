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

    public static final String DATABASE_NAME = "diabeticguard.db";
    public static final int DATABASE_VERSION = 1;
    private static DBController instance;



    public static String tableTrackName = "tblTrack";
    public static String colDate = "Date";
    public static String colTime = "Time";
    public static String colLevel = "Level";
    public static String colUserId = "UserId";

    public static String colIsFasting = "IsFasting";

//    public static String tableProfileName = "tblProfile";
//    public static String colUserId = "UserId";
//    public static String colUserName = "UserName";
//    public static String colGender = "Gender";
//    public static String colAge = "Age";
//    public static String colIsPregnent = "IsPregnant";
//    public static String colOtherRisks = "OtherRisks";

    public DBController(Context context) {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
        SQLiteDatabase db = this.getWritableDatabase(); //Force database open

//        //TODO one time
//        String query1 = "DROP TABLE IF EXISTS " + tableTrackName;
//        db.execSQL(query1);
//        onCreate(db);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {

        String createTableSql;

        createTableSql = "CREATE TABLE IF NOT EXISTS " + tableTrackName + " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, " + colDate + " TEXT, " + colTime +
                " TEXT, " + colLevel + " TEXT, " + colUserId + " TEXT ); ";
        Log.i("create Query", createTableSql);

        database.execSQL(createTableSql);

    }


    @Override
    public void onUpgrade(SQLiteDatabase database, int version_old, int current_version) {
        String query;
        query = "DROP TABLE IF EXISTS " + tableTrackName;
        database.execSQL(query);
        onCreate(database);
    }


    public ArrayList<HashMap<String, String>> getAllTracks(String userId) {

        ArrayList<HashMap<String, String>> trackList;
        trackList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT  * FROM " + tableTrackName + " where UserId = \"" + userId + "\" order by Date, Time";

        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {

            do {
                //Id, Company,Name,Price
                HashMap<String, String> map = new HashMap<String, String>();
                //cursor no.0 is id, don't need to display
                map.put("date", cursor.getString(1));
                map.put("time", cursor.getString(2));
                map.put("level", cursor.getString(3));
                map.put("userId", cursor.getString(4));
                trackList.add(map);
                Log.i("dataofList", cursor.getString(1) + "," + cursor.getString(2) + "," + cursor.getString(3) +  cursor.getString(4));
            } while (cursor.moveToNext());
        }
        // at last closing our cursor and returning array list.
        cursor.close();
        return trackList;

    }

    public String foundMatchedData(String date, String time, String userId) {
        String returnId = "";
        ArrayList<HashMap<String, String>> trackList;
        trackList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT _id FROM " + tableTrackName +
                " where " + colUserId + " = \"" + userId + "\"" +
                " and "+ colDate +" = \"" + date + "\"" +
                " and "+ colTime + " = \"" + time + "\"";

        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            returnId = cursor.getString(0);
        }
        return returnId;
    }

}
