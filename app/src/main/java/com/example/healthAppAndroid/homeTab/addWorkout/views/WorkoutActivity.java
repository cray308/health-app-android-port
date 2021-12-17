package com.example.healthAppAndroid.homeTab.addWorkout.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.shareddata.AppColors;
import com.example.healthAppAndroid.common.shareddata.AppCoordinator;
import com.example.healthAppAndroid.common.shareddata.AppUserData;
import com.example.healthAppAndroid.common.shareddata.PersistenceService;
import com.example.healthAppAndroid.common.workouts.Circuit;
import com.example.healthAppAndroid.common.workouts.ExerciseManager;
import com.example.healthAppAndroid.common.workouts.Workout;
import com.example.healthAppAndroid.homeTab.addWorkout.utils.NotificationService;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.time.Instant;
import java.util.Locale;

public final class WorkoutActivity extends AppCompatActivity {
    private final static class UpdateHandler implements PersistenceService.Block {
        private final short[] lifts;

        private UpdateHandler(short[] lifts) { this.lifts = lifts; }

        private static UpdateHandler init(short[] lifts) {
            if (lifts == null) return null;
            return new UpdateHandler(lifts);
        }

        public void completion() {
            AppCoordinator.shared.updateMaxWeights(lifts);
        }
    }

    public final static String bundleKey = "WorkoutActivityKey";
    private Workout workout;
    private LinearLayout groupsStack;
    private ExerciseContainer firstContainer;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);
        Bundle args = getIntent().getExtras();
        if (args != null) {
            Workout.Params params = args.getParcelable(bundleKey);
            workout = ExerciseManager.getWorkoutFromLibrary(this, params);
        }

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
                workout.setDuration();
                if (workout.checkEnduranceDuration()) {
                    handleFinishedWorkout(null, null, true);
                } else {
                    PersistenceService.updateCurrentWeek(workout, null, null);
                    sendBroadcast(0);
                    finish();
                }
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

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (workout.startTime != 0) {
                workout.setDuration();
                if (workout.checkEnduranceDuration()) {
                    handleFinishedWorkout(null, null, false);
                } else {
                    PersistenceService.updateCurrentWeek(workout, null, null);
                    sendBroadcast(0);
                }
            } else {
                sendBroadcast(0);
            }
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
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

        if (finishedWorkout) {
            workout.setDuration();
            if (workout.title.equalsIgnoreCase(getString(R.string.workoutTitleTestDay))) {
                AddWorkoutUpdateMaxesDialog modal = new AddWorkoutUpdateMaxesDialog();
                modal.show(getSupportFragmentManager(), "AddWorkoutUpdateMaxesDialog");
            } else {
                handleFinishedWorkout(null, null, true);
            }
        }
    }

    public void finishedGroup() {
        handleTap(workout.index, 255, Workout.EventOption.finishGroup);
    }

    public void finishedExercise() {
        handleTap(workout.index, workout.group.index, (byte) 0);
    }

    private void sendBroadcast(int totalCompleted) {
        Intent intent = new Intent(Workout.finishedNotification);
        intent.putExtra(Workout.userInfoKey, totalCompleted);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    void handleFinishedWorkout(BottomSheetDialogFragment dialog, short[] lifts, boolean close) {
        int totalCompleted = 0;
        if (workout.duration >= Workout.MinWorkoutDuration && workout.day >= 0)
            totalCompleted = AppUserData.shared.addCompletedWorkout(workout.day);

        PersistenceService.updateCurrentWeek(workout, lifts, UpdateHandler.init(lifts));
        if (dialog != null)
            dialog.dismiss();
        sendBroadcast(totalCompleted);
        if (close)
            finish();
    }
}
