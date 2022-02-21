package com.example.healthAppAndroid.homeTab.addWorkout;

final class MutableString {
    @SuppressWarnings("StringBufferField") final StringBuilder str = new StringBuilder(16);
    short index = 0;
    short end = 1;

    void replace(String replacement) { str.replace(index, end, replacement); }
}
