package com.example.healthAppAndroid.homeTab.addWorkout;

final class MutableString {
    static String one;

    final StringBuilder str = new StringBuilder(16);
    private int index;
    private int length;

    void setup(String num, String substring) {
        length = num.length();
        if (substring == null) {
            index = str.indexOf(num);
            return;
        }

        int subIdx = str.indexOf(substring);
        int numIdx = str.substring(subIdx, subIdx + substring.length()).indexOf(num);
        index = subIdx + numIdx;
    }

    void replace(String replacement) {
        str.replace(index, index + length, replacement);
        length = replacement.length();
    }
}
