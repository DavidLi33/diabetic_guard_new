package com.example.diabeticguard;

import android.content.Context;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.mlkit.vision.text.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.TreeSet;

public class MedicineDatabase {
    public TreeSet<String> Medicines = new TreeSet<>();
    public String[] instructions = {"lake", "|ake", "take", "ake"};
    public String[] dates = {"hour", "day", "month", "biweekly", "week", "year", "hourly", "daily", "monthly", "everyday"};
    public String[][] numbers = {{}, {"once", "1", "one"}, {"twice", "two", "2"}, {"thrice", "three", "3"}};

    public void init(Context context) throws IOException {
        Context mContext = context;
        InputStream is = null;
        try {
            is = mContext.getAssets().open("Medicines.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] result = CharStreams.toString(new InputStreamReader(is, Charsets.UTF_8)).split("\r\n");
        for (String i : result) {
            Medicines.add(i.toLowerCase(Locale.ROOT));
        }
    }

    public boolean isMedicine(String m) {
        for (String i : AutoCorrect.correct(m)) {
            if (Medicines.contains(i.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    public String getMedicine(Text resultText) {
        String res = null;
        for (Text.TextBlock block : resultText.getTextBlocks()) {
            for (Text.Line line : block.getLines()) {
                for (Text.Element word : line.getElements()) {
                    if (isMedicine(word.getText().toLowerCase(Locale.ROOT))) {
                        for (String i : AutoCorrect.correct(word.getText())) {
                            if (Medicines.contains(i.toLowerCase(Locale.ROOT))) {
                                res = i.toLowerCase(Locale.ROOT);
                            }
                        }
                    }
                }
            }
        }
        return res;
    }

    public String getMedicine(String resultText) {
        String res = null;
        String[] blocks = resultText.split("\n");
        for (String str : blocks) {
            String[] words = str.split(" ");
            for (String wrd : words) {
                if (isMedicine(wrd.toLowerCase(Locale.ROOT))) {
                    for (String i : AutoCorrect.correct(wrd)) {
                        if (Medicines.contains(i.toLowerCase(Locale.ROOT))) {
                            res = i.toLowerCase(Locale.ROOT);
                        }
                    }
                }
            }
        }
        return res;
    }

    public boolean isInstruction(String instruction) {
        for (String str : instructions) {
            if (str.equals(instruction)) {
                return true;
            }
        }
        return false;
    }

    public boolean isDate(String date) {
        for (String str : dates) {
            if (str.equals(date)) {
                return true;
            }
        }
        return false;
    }

    public String getDosage(String resultText) {
        StringBuilder res = new StringBuilder("");
        boolean add = false;
        String[] blocks = resultText.split("\n");
        for (String str : blocks) {
            String[] words = str.split(" ");
            for (String wrd : words) {
                if (isDate(wrd.toLowerCase(Locale.ROOT))) {
                    res.append(wrd);
                    add = false;
                } else if (add) {
                    res.append(wrd).append(" ");
                } else if (isInstruction(wrd.toLowerCase(Locale.ROOT))) {
                    res = new StringBuilder(wrd + " ");
                    add = true;
                }
            }
        }
        if (res.equals(new StringBuilder(""))) {
            return "No Dosage Found";
        }
        return res.toString();
    }

    public long getDosageInMillis(String dosage) {
        double mult_factor = 1;
        boolean flag = false;
        for (int num = 1; num < 4; num++){
            if (flag){
                break;
            }
            for (String s : numbers[num]){
                if (dosage.contains(s)){
                    mult_factor /= num;
                    flag = true;
                    break;
                }
            }
        }
        for (String s : dates){
            if (dosage.indexOf(s) != -1){
                if (s.contains("day")){
                    return (long) ((long)1000*60*60*24*mult_factor);
                }
                else if (s.contains("week")){
                    if (s.contains("bi")){
                        return (long) ((long)1000*60*60*24*7*2*mult_factor);
                    }
                    return (long) ((long)1000*60*60*24*7*mult_factor);
                }
            }
        }
        return -1;
    }
}
