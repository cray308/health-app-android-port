package com.example.healthAppAndroid.homeTab;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.core.MainActivity;
import com.example.healthAppAndroid.core.TextValidator;
import com.example.healthAppAndroid.core.UserData;
import com.example.healthAppAndroid.homeTab.addWorkout.ExerciseManager;
import com.example.healthAppAndroid.homeTab.addWorkout.Workout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public final class SetupWorkoutDialog
  extends BottomSheetDialogFragment implements AdapterView.OnItemSelectedListener {
    private static abstract class Index {
        private static final int sets = 0;
        private static final int reps = 1;
        private static final int weight = 2;
    }

    private static final String BundleKey = "SetupWorkoutKey";

    static SetupWorkoutDialog init(byte type) {
        SetupWorkoutDialog fragment = new SetupWorkoutDialog();
        Bundle args = new Bundle();
        args.putByte(BundleKey, type);
        fragment.setArguments(args);
        return fragment;
    }

    private final Workout.Params params = new Workout.Params((byte)-1);

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) params.type = args.getByte(BundleKey);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        return inflater.inflate(R.layout.setup_workout_modal, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Context context = getContext();
        if (context == null) return;

        view.findViewById(R.id.cancelButton).setOnClickListener(view1 -> dismiss());

        Button submitButton = view.findViewById(R.id.submitButton);
        TextValidator validator = new TextValidator(submitButton);
        submitButton.setOnClickListener(view2 -> {
            switch (params.type) {
                case Workout.Type.strength:
                    params.weight = (int)validator.children[Index.weight].result;
                case Workout.Type.SE:
                    params.sets = (int)validator.children[Index.sets].result;
                    params.reps = (int)validator.children[Index.reps].result;
                    break;

                case Workout.Type.endurance:
                    params.reps = (int)validator.children[0].result;
                default:
            }
            UserData data = MainActivity.userData;
            params.bodyWeight = data.weightToUse();
            params.lifts = data.lifts;
            HomeFragment fragment = (HomeFragment)getParentFragmentManager().findFragmentByTag("1");
            if (fragment != null) fragment.navigateToWorkout(this, params);
        });

        Spinner spinner = view.findViewById(R.id.workoutSpinner);
        spinner.setOnItemSelectedListener(this);
        String[] names = ExerciseManager.workoutNamesForType(context, params.type);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
          context, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);

        int[] maxes = {5, 5, 100}, min = {1, 1, 1}, titleKeys = {R.string.sets, R.string.reps, -1};

        if (params.type == Workout.Type.strength) {
            titleKeys[Index.weight] = R.string.setupWorkoutMaxWeight;
        } else if (params.type == Workout.Type.SE) {
            maxes[Index.sets] = 3;
            maxes[Index.reps] = 50;
        } else if (params.type == Workout.Type.endurance) {
            titleKeys[Index.sets] = -1;
            titleKeys[Index.reps] = R.string.setupWorkoutDuration;
            maxes[Index.reps] = 180;
            min[Index.reps] = Workout.minDuration;
        } else {
            titleKeys[0] = titleKeys[1] = -1;
            submitButton.setEnabled(true);
        }

        LinearLayout stack = view.findViewById(R.id.fieldStack);
        for (int i = 0; i < 3; ++i) {
            if (titleKeys[i] == -1) continue;
            TextValidator.InputView iv = new TextValidator.InputView(context);
            validator.addChild(iv, getString(titleKeys[i]), min[i], maxes[i]);
            stack.addView(iv);
        }
        BottomSheetDialog dialog = (BottomSheetDialog)getDialog();
        if (dialog != null) dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        params.index = position;
    }

    public void onNothingSelected(AdapterView<?> parent) {}
}
