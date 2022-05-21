package com.example.healthAppAndroid.homeTab.addWorkout;

import android.os.Parcel;
import android.os.Parcelable;

public class WorkoutData implements Parcelable {
    public final short[] weights = {-1, -1, -1, -1};
    public final short duration;
    public final byte day;
    public final byte type;

    WorkoutData(byte day, byte type, short duration, short[] weights) {
        if (weights != null) System.arraycopy(weights, 0, this.weights, 0, 4);
        this.duration = duration;
        this.day = day;
        this.type = type;
    }

    private WorkoutData(Parcel in) {
        for (int i = 0; i < 4; ++i) {
            weights[i] = (short)in.readInt();
        }
        duration = (short)in.readInt();
        day = in.readByte();
        type = in.readByte();
    }

    public static final Creator<WorkoutData> CREATOR = new Creator<WorkoutData>() {
        public WorkoutData createFromParcel(Parcel in) { return new WorkoutData(in); }

        public WorkoutData[] newArray(int size) { return new WorkoutData[size]; }
    };

    public int describeContents() { return 0; }

    public void writeToParcel(Parcel out, int flags) {
        for (int i = 0; i < 4; ++i) {
            out.writeInt(weights[i]);
        }
        out.writeInt(duration);
        out.writeByte(day);
        out.writeByte(type);
    }
}
