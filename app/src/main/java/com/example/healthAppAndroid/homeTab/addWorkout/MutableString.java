package com.example.healthAppAndroid.homeTab.addWorkout;

final class MutableString {
    @SuppressWarnings("StringBufferField") final StringBuilder str = new StringBuilder(16);
    int index = 0;
    int end = 1;

    void replace(String replacement) { str.replace(index, end, replacement); }
}
