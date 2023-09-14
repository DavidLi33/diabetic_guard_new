package com.example.diabeticguard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class AutoCorrect {
    public static ArrayList<String> correct(String s){
        ArrayList<String> possibilities = new ArrayList<>();
        possibilities.add(s);
        char[] sarr = s.toCharArray();
        for (int i = 0; i < sarr.length; i++){
            if (((Character)sarr[i]).equals('0')){
                sarr[i] = 'O';
            }
            if (((Character)sarr[i]).equals('1')){
                sarr[i] = 'I';
                possibilities.add(new String(sarr));
                sarr[i] = 'L';
                possibilities.add(new String(sarr));
            }
        }
        if (((Character)sarr[0]).toString().toLowerCase(Locale.ROOT).equals("e")){
            possibilities.add(new String(Arrays.copyOfRange(sarr, 1, sarr.length)));
        }
        return possibilities;
    }
}
