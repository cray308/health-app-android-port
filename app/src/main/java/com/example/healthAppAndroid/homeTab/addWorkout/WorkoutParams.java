package com.example.healthAppAndroid.homeTab.addWorkout;

import android.os.Parcel;
import android.os.Parcelable;

public final class WorkoutParams implements Parcelable {
    public int index;
    public short reps = 1;
    public short weight = 1;
    public short sets = 1;
    final byte day;
    public byte type;

    public WorkoutParams(byte day) { this.day = day; }

    public int describeContents() { return 0; }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(index);
        out.writeInt(sets);
        out.writeInt(reps);
        out.writeInt(weight);
        out.writeByte(day);
        out.writeByte(type);
    }

    public static final Parcelable.Creator<WorkoutParams> CREATOR = new Parcelable.Creator<WorkoutParams>() {
        public WorkoutParams createFromParcel(Parcel in) { return new WorkoutParams(in); }

        public WorkoutParams[] newArray(int size) { return new WorkoutParams[size]; }
    };

    private WorkoutParams(Parcel in) {
        index = in.readInt();
        sets = (short)in.readInt();
        reps = (short)in.readInt();
        weight = (short)in.readInt();
        day = in.readByte();
        type = in.readByte();
    }
}
