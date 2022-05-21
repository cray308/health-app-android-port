package com.example.healthAppAndroid.homeTab.addWorkout;

import android.content.Context;
import android.icu.text.MeasureFormat;
import android.icu.util.MeasureUnit;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.core.Macros;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.util.Locale;

public final class Workout {
    static abstract class Transition {
        static final int completedWorkout = 1;
        static final int finishedCircuit = 2;
        static final int finishedCircuitDeleteFirst = 3;
        static final int finishedExercise = 4;
    }

    public static abstract class Type {
        public static final byte strength = 0;
        public static final byte SE = 1;
        public static final byte endurance = 2;
        static final byte HIC = 3;
    }

    public static final class Params implements Parcelable {
        public static abstract class Index {
            static final int mainStrength = 0;
            public static final int testMax = 2;
        }

        public static final Parcelable.Creator<Params> CREATOR = new Parcelable.Creator<Params>() {
            public Params createFromParcel(Parcel source) { return new Params(source); }

            public Params[] newArray(int size) { return new Params[size]; }
        };

        public int[] lifts = {0, 0, 0, 0};
        public int index;
        public int bodyWeight;
        public int reps = 1;
        public int weight = 100;
        public int sets = 1;
        final byte day;
        public byte type;

        public Params(byte day) { this.day = day; }

        private Params(Parcel src) {
            for (int i = 0; i < 4; ++i) {
                lifts[i] = src.readInt();
            }
            index = src.readInt();
            bodyWeight = src.readInt();
            sets = src.readInt();
            reps = src.readInt();
            weight = src.readInt();
            day = src.readByte();
            type = src.readByte();
        }

        public int describeContents() { return 0; }

        public void writeToParcel(Parcel dest, int flags) {
            for (int i = 0; i < 4; ++i) {
                dest.writeInt(lifts[i]);
            }
            dest.writeInt(index);
            dest.writeInt(bodyWeight);
            dest.writeInt(sets);
            dest.writeInt(reps);
            dest.writeInt(weight);
            dest.writeByte(day);
            dest.writeByte(type);
        }
    }

    public static final class Output implements Parcelable {
        public static final Creator<Output> CREATOR = new Creator<Output>() {
            public Output createFromParcel(Parcel source) { return new Output(source); }

            public Output[] newArray(int size) { return new Output[size]; }
        };

        public final int[] weights = {-1, -1, -1, -1};
        public final int duration;
        public final byte day;
        public final byte type;

        Output(byte day, byte type, int duration, int[] weights) {
            if (weights != null) System.arraycopy(weights, 0, this.weights, 0, 4);
            this.duration = duration;
            this.day = day;
            this.type = type;
        }

        private Output(Parcel src) {
            for (int i = 0; i < 4; ++i) {
                weights[i] = src.readInt();
            }
            duration = src.readInt();
            day = src.readByte();
            type = src.readByte();
        }

        public int describeContents() { return 0; }

        public void writeToParcel(Parcel dest, int flags) {
            for (int i = 0; i < 4; ++i) {
                dest.writeInt(weights[i]);
            }
            dest.writeInt(duration);
            dest.writeByte(day);
            dest.writeByte(type);
        }
    }

    static void setupData() {
        Locale locale = Locale.getDefault();
        MeasureUnit unit = Macros.isMetric(locale) ? MeasureUnit.KILOGRAM : MeasureUnit.POUND;
        MeasureFormat format = MeasureFormat.getInstance(locale, MeasureFormat.FormatWidth.NARROW);
        weightUnit = format.getUnitDisplayName(unit);
    }

    private static String weightUnit;
    public static final int minDuration = 15;

    final Circuit[] circuits;
    long startTime;
    private int index;
    final int bodyWeight;
    int duration;
    final byte type;
    final byte day;
    final boolean testMax;

    Workout(Context context, ExerciseManager.WorkoutJSON data, Params params) {
        float[] weights = {0, 0, 0, 0};
        int customSets = 1, customReps = 0, customCircuitReps = 0;
        if (params.type == Type.strength) {
            float multiplier = params.weight / 100f;
            weights[0] = params.lifts[0] * multiplier;

            if (params.index < Params.Index.testMax) {
                weights[1] = params.lifts[LiftType.bench] * multiplier;
                if (params.index == Params.Index.mainStrength) {
                    float weight = ((params.lifts[LiftType.pullUp] + params.bodyWeight) * multiplier)
                                   - params.bodyWeight;
                    weights[2] = Math.max(weight, 0);
                } else {
                    weights[2] = params.lifts[LiftType.deadLift] * multiplier;
                }
            } else {
                for (int i = 1; i < 4; ++i) {
                    weights[i] = params.lifts[i];
                }
            }

            if (Macros.isMetric(Locale.getDefault())) {
                for (int i = 0; i < 4; ++i) {
                    weights[i] *= Macros.toKg;
                }
            }

            customReps = params.reps;
            customSets = params.sets;
        } else if (params.type == Type.SE) {
            customCircuitReps = params.sets;
            customReps = params.reps;
        } else if (params.type == Type.endurance) {
            customReps = params.reps * 60;
        }

        type = params.type;
        day = params.day;
        bodyWeight = params.bodyWeight;
        testMax = type == Type.strength && params.index == Params.Index.testMax;

        String[] exNames = context.getResources().getStringArray(R.array.exNames);
        Circuit[] _circuits = null;

        try {
            JSONArray activities = data.lib.getJSONArray(params.type).getJSONArray(params.index);
            int wSize = activities.length();
            boolean multiple = wSize > 1;
            _circuits = new Circuit[wSize];

            for (int i = 0; i < wSize; ++i) {
                JSONObject cData = activities.getJSONObject(i);
                byte cType = (byte)cData.getInt(ExerciseManager.Keys.type);
                JSONArray exercisesArr = cData.getJSONArray("E");
                int exSets = customSets, cReps = customCircuitReps, cSize = exercisesArr.length();
                Exercise[] exercises = new Exercise[cSize];
                if (cReps == 0) cReps = cData.getInt(ExerciseManager.Keys.reps);
                if (Macros.onEmulator() && cType == Circuit.Type.AMRAP) cReps = wSize > 1 ? 1 : 2;

                if (type == Type.HIC && cType == Circuit.Type.rounds && cSize == 1) {
                    exSets = cReps;
                    cReps = 1;
                }

                for (int j = 0; j < cSize; ++j) {
                    JSONObject eData = exercisesArr.getJSONObject(j);
                    byte exType = (byte)eData.getInt(ExerciseManager.Keys.type);
                    int exReps = customReps;
                    if (exReps == 0) exReps = eData.getInt(ExerciseManager.Keys.reps);
                    if (Macros.onEmulator() && exType == Exercise.Type.duration)
                        exReps = type == Type.HIC ? 15 : 60;

                    exercises[j] = new Exercise(context, exType, exReps, exSets, eData.getInt("B"));
                    String name = exNames[eData.getInt(ExerciseManager.Keys.index)];
                    String title;

                    if (exType == Exercise.Type.reps) {
                        if (params.type == Workout.Type.strength) {
                            title = context.getString(R.string.exWeight,
                                                      name, exReps, weights[j], weightUnit);
                        } else {
                            title = context.getString(R.string.exReps, name, exReps);
                        }
                    } else if (exType == Exercise.Type.duration) {
                        if (exReps > 120) {
                            title = context.getString(R.string.exMinutes, name, exReps / 60f);
                        } else {
                            title = context.getString(R.string.exSeconds, name, exReps);
                        }
                    } else {
                        title = context.getString(R.string.exDistance,
                                                  name, exReps, ((5 * exReps) >> 2));
                    }

                    exercises[j].title.str.append(title);
                }

                _circuits[i] = new Circuit(exercises, cType, cReps);
                String header;

                if (cType == Circuit.Type.AMRAP) {
                    if (multiple) {
                        header = context.getString(R.string.circuitAMRAPM, i + 1, wSize, cReps);
                    } else {
                        header = context.getString(R.string.circuitAMRAP, cReps);
                    }
                    _circuits[i].header.str.append(header);
                } else if (cReps > 1) {
                    if (multiple) {
                        header = context.getString(R.string.circuitRoundsM, i + 1, wSize, 1, cReps);
                    } else {
                        header = context.getString(R.string.circuitRounds, 1, cReps);
                    }
                    _circuits[i].header.str.append(header);
                    _circuits[i].header.setup(MutableString.one, context.getString(R.string.rounds1));
                } else if (multiple) {
                    header = context.getString(R.string.circuitProgress, i + 1, wSize);
                    _circuits[i].header.str.append(header);
                }
            }
        } catch (JSONException ignored) {}

        circuits = _circuits;
    }

    int increment(Context context) {
        if (++index == circuits.length) return Transition.completedWorkout;

        circuits[index].start(context, index, true);
        return Transition.finishedCircuitDeleteFirst;
    }

    void setDuration() {
        duration = (int)((Instant.now().getEpochSecond() - startTime) / 60f) + 1;
        if (Macros.onEmulator()) {
            duration += 1;
            duration *= 10;
        }
    }

    boolean isCompleted() {
        setDuration();
        Circuit circuit = circuits[index];
        if (index != circuits.length - 1 || circuit.index != circuit.exercises.length - 1)
            return false;
        if (type == Type.endurance) return duration >= circuit.exercises[0].reps / 60;

        if (circuit.type == Circuit.Type.rounds && circuit.completedReps == circuit.reps - 1) {
            Exercise ex = circuit.exercises[circuit.index];
            return ex.state == Exercise.State.resting && ex.completedSets == ex.sets - 1;
        }
        return false;
    }
}
