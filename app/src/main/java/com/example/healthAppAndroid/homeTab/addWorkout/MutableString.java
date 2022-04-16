package com.example.healthAppAndroid.homeTab.addWorkout;

final class MutableString {
    final StringBuilder str = new StringBuilder(16);
    int index = 0;
    int length = 1;

    void replace(String replacement) { str.replace(index, index + length, replacement); }
}
