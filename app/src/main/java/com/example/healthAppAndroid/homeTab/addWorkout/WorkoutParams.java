package com.example.healthAppAndroid.homeTab.addWorkout;

import android.os.Parcel;
import android.os.Parcelable;

public final class WorkoutParams implements Parcelable {
    final byte day;
    public byte type;
    public int index;
    public int sets = 1;
    public int reps = 1;
    public int weight = 1;

    public WorkoutParams(byte day) { this.day = day; }

    public int describeContents() { return 0; }

    public void writeToParcel(Parcel out, int flags) {
        out.writeByte(day);
        out.writeByte(type);
        out.writeInt(index);
        out.writeInt(sets);
        out.writeInt(reps);
        out.writeInt(weight);
    }

    public static final Parcelable.Creator<WorkoutParams> CREATOR =
      new Parcelable.Creator<WorkoutParams>() {
        public WorkoutParams createFromParcel(Parcel in) { return new WorkoutParams(in); }

        public WorkoutParams[] newArray(int size) { return new WorkoutParams[size]; }
    };

    private WorkoutParams(Parcel in) {
        day = in.readByte();
        type = in.readByte();
        index = in.readInt();
        sets = in.readInt();
        reps = in.readInt();
        weight = in.readInt();
    }
}
