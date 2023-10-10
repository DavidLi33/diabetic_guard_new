package com.example.diabeticguard;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

public class MeterDataParser {

    private String inputText;
    private String level = "";
    private String date = "";
    private String time = "";
    private String currDate;

    private static String DELIMITER = " ";
    private static String DELIMITER2 = "-";

    public MeterDataParser(String text) {
        inputText = text;
        init();
        parseText();
    }
    public void init() {
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        Date today = new Date();
        currDate = df.format(today);
    }

    private void parseText() {

        //sample format:
//        ONETOUCH Ultra2
//        BEFORE MEAL
//        104
//        mgldL
//        01-17-10 930 AM

        if( inputText != null ) {
            String[] blocks = inputText.split("\n");
            for (String str : blocks) {
                String[] words = str.split(" ");
                //in most cases, the level score is a single value at individual line
                if( words.length == 1 ) {
                    //sometime, there is ending '.' at the number, remove it
                    String oneWrd = words[0];
                    if( oneWrd.endsWith(".") ) {
                        oneWrd = oneWrd.substring(0, oneWrd.length()-1);
                    }
                    int value = getStringNumberValue(oneWrd);
                    if( level == null || level.isEmpty() ) {
                        if( value > -1 && value < 1000 ) {
                            level = String.valueOf(value);
                        }
                    }
                }
                if( str.endsWith("AM") ||  str.endsWith("am") || str.endsWith("PM") || str.endsWith("pm")) {
                    //all date and time AM/PM combined together, eg. 11-01-23830PM, need to remove date
                    String timeStr = "";
                    if( words.length == 1 ) {
                        String wrd =  words[0].substring(0, words[0].length()-2);
                        if (wrd.length() >= 11) {
                            timeStr = wrd.substring(8);
                        } else {
                            timeStr = wrd;
                        }
                    }else {
                        //If the last word just AM or PM, last 2nd word in this line is the time, eg. 9:50 PM
                        if( words[words.length-1].length() == 2 ) {
                            timeStr = words[words.length - 2];
                        }else {
                            //the last word contain time, eg. 9:50PM
                            timeStr = words[words.length-1].substring(0, words.length-2);
                        }
                    }
                    //get hour and minute part, if PM, hour + 12
                    String part1 = "";
                    String part2 = "";
                    if (timeStr.contains(":")) {
                        String[] timeItems = timeStr.split(":");
                        part1 = timeItems[0];
                        part2 = timeItems[1];
                    } else {
                        if (timeStr.length() >= 3) {
                            part1 = timeStr.substring(0, timeStr.length() - 2);
                            part2 = timeStr.substring(timeStr.length() - 2);
                        }
                    }
                    int hourTemp = getStringNumberValue(part1);
                    if (hourTemp > -1) {
                        if (str.endsWith("PM")) {
                            hourTemp += 12;
                        }
                    }
                    int minuteTemp = getStringNumberValue(part2);
                    if (minuteTemp > -1 && hourTemp > -1) {
                        time = "" + hourTemp + ":" + minuteTemp + ":00";
                    }

                }
                //assign current Date to date as default for easy testing
                if( date == null || date.isEmpty() ) {
                    date = currDate;
                }
            }

        }
        Log.i( "ParseText", "Input Text: " + inputText);
        Log.i( "ParseText", "Level: " + level + ", date: " + date + ", time: " + time );
    }

    public String getLevel() {
        return level;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public int getStringNumberValue(String input) {
        try {
            int levelNumber = Integer.parseInt(input);
            return levelNumber;
        }catch(NumberFormatException nfe) {
            return -1;
        }
    }

}
