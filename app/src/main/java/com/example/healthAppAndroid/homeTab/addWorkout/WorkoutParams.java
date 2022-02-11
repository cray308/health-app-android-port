package com.example.healthAppAndroid.homeTab.addWorkout;

import android.os.Parcel;
import android.os.Parcelable;

public final class WorkoutParams implements Parcelable {
    public short reps = 1;
    public short weight = 1;
    final byte day;
    public byte type;
    public byte index;
    public byte sets = 1;

    public WorkoutParams(byte day) { this.day = day; }

    public int describeContents() { return 0; }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(reps);
        out.writeInt(weight);
        out.writeByte(day);
        out.writeByte(type);
        out.writeByte(index);
        out.writeByte(sets);
    }

    public static final Parcelable.Creator<WorkoutParams> CREATOR =
      new Parcelable.Creator<WorkoutParams>() {
        public WorkoutParams createFromParcel(Parcel in) { return new WorkoutParams(in); }

        public WorkoutParams[] newArray(int size) { return new WorkoutParams[size]; }
    };

    private WorkoutParams(Parcel in) {
        reps = (short) in.readInt();
        weight = (short) in.readInt();
        day = in.readByte();
        type = in.readByte();
        index = in.readByte();
        sets = in.readByte();
    }
}
