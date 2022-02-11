package com.maybe.maybe.adapters;

import android.graphics.Color;

import java.util.Arrays;
import java.util.List;

public class CardColor {
    private static int x = 0;
    /*private static int multiplier = 1;
    private static final List<String> C = Arrays.asList("CC99FF", "C5A4FD", "BEAFFC", "B7BBFA", "B0C6F9", "A9D1F7", "ABD7E7", "ADDDD7", "B0E4C7", "B2EAB7", "B4F0A7", "C3F3AC", "D2F6B1", "E1F9B5", "F0FCBA", "FFFFBF", "FFF9BF", "FFF2BF", "FFECBE", "FFE5BE", "FFDFBE", "FFD6BB", "FFCDB8", "FFC3B6", "FFBAB3", "FFB1B0", "F5ACC0", "EBA7D0", "E0A3DF", "D69EEF");

    public static int getColor() {
        if(i == C.size()-1)
            multiplier = -1;
        else if(i == 0)
            multiplier = 1;
        i += multiplier;
        return Color.parseColor("#" + C.get(i));
    }*/

    public static int getColor2() {
        if(x == 0) {
            x = 1;
            return Color.argb(100, 40, 40, 40);
        }
        else {
            x = 0;
            return Color.argb(100, 60, 60, 60);
        }

    }
}