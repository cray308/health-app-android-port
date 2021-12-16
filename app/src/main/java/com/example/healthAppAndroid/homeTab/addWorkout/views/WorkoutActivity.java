package com.example.healthAppAndroid.homeTab.addWorkout.views;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.shareddata.AppColors;
import com.example.healthAppAndroid.common.shareddata.AppCoordinator;
import com.example.healthAppAndroid.common.workouts.Circuit;
import com.example.healthAppAndroid.common.workouts.Workout;
import com.example.healthAppAndroid.homeTab.addWorkout.WorkoutCoordinator;
import com.example.healthAppAndroid.homeTab.addWorkout.utils.NotificationService;

import java.time.Instant;
import java.util.Locale;

public final class WorkoutActivity extends AppCompatActivity {
    private WorkoutCoordinator delegate;
    private Workout workout;
    private LinearLayout groupsStack;
    private ExerciseContainer firstContainer;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);
        delegate = AppCoordinator.shared.homeCoordinator.child;
        workout = delegate.workout;
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null)
            bar.setDisplayHomeAsUpEnabled(true);
        ((TextView) toolbar.findViewById(R.id.titleLabel)).setText(workout.title);
        toolbar.findViewById(R.id.startStopButton).setOnClickListener(view -> {
            Button btn = (Button) view;
            if (btn.getText().equals(getString(R.string.start))) {
                btn.setText(getString(R.string.end));
                btn.setTextColor(AppColors.red);
                workout.startTime = Instant.now().getEpochSecond();
                handleTap(0, 0, Workout.EventOption.startGroup);
            } else {
                delegate.stoppedWorkout(this);
            }
        });
        groupsStack = findViewById(R.id.workoutGroupsStack);
        NotificationService.setup(this);

        int count = workout.activities.length;
        for (int i = 0; i < count; ++i) {
            ExerciseContainer v = new ExerciseContainer(this);
            v.setup(workout.activities[i], i, tapHandler);
            groupsStack.addView(v);
        }

        firstContainer = (ExerciseContainer) groupsStack.getChildAt(0);
        firstContainer.headerView.divider.setVisibility(View.GONE);
    }

    protected void onDestroy() {
        NotificationService.cleanup(this);
        super.onDestroy();
    }

    private final View.OnClickListener tapHandler = view -> {
        int tag = view.getId(), groupIdx = (tag & 0xff00) >> 8, exerciseIdx = (tag & 0xff) - 1;
        handleTap(groupIdx, exerciseIdx, (byte) 0);
    };

    private void handleTap(int groupIdx, int exerciseIdx, byte option) {
        boolean finishedWorkout = false;
        if (groupIdx != workout.index || exerciseIdx != workout.group.index) {
            if (option != Workout.EventOption.finishGroup || groupIdx != workout.index) return;
        }

        ExerciseView v = firstContainer.viewsArr[workout.group.index];
        switch (workout.findTransitionForEvent(this, v, option)) {
            case Workout.Transition.completedWorkout:
                finishedWorkout = true;
                break;

            case Workout.Transition.finishedCircuitDeleteFirst:
                firstContainer = (ExerciseContainer) groupsStack.getChildAt(1);
                groupsStack.removeViewAt(0);
                firstContainer.headerView.divider.setVisibility(View.GONE);
            case Workout.Transition.finishedCircuit:
                if (workout.group.reps > 1 && workout.group.type == Circuit.Type.rounds) {
                    String newNumber = String.format(Locale.US, "%d",
                                                     workout.group.completedReps + 1);
                    workout.group.headerStr.replace(
                      workout.group.numberRange.index, workout.group.numberRange.end, newNumber);
                    firstContainer.headerView.headerLabel.setText(workout.group.headerStr);
                }
                int nExercises = workout.group.exercises.length;
                for (int i = 0; i < nExercises; ++i) {
                    firstContainer.viewsArr[i].configure();
                }
                break;

            case Workout.Transition.finishedExercise:
                firstContainer.viewsArr[workout.group.index].configure();
            default:
        }

        if (finishedWorkout)
            delegate.completedWorkout(this, null, true, null);
    }

    public void finishedGroup() {
        handleTap(workout.index, 255, Workout.EventOption.finishGroup);
    }

    public void finishedExercise() {
        handleTap(workout.index, workout.group.index, (byte) 0);
    }
}
