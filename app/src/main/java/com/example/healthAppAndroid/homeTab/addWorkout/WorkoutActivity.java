package com.example.healthAppAndroid.homeTab.addWorkout;

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
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.core.AppColors;
import com.example.healthAppAndroid.core.AppCoordinator;
import com.example.healthAppAndroid.core.AppUserData;
import com.example.healthAppAndroid.core.PersistenceService;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.time.Instant;
import java.util.Locale;

public final class WorkoutActivity extends AppCompatActivity {
    private final static String bundleKey = "WorkoutActivityKey";
    public static final String notification = "FinishedWorkoutNotification";
    public static final String userInfo = "totalWorkouts";
    private Workout workout;
    private LinearLayout groupsStack;
    private ExerciseContainer firstContainer;
    private final short[] weights = {0, 0, 0, 0};

    public static void start(FragmentActivity parent, WorkoutParams params) {
        Intent intent = new Intent(parent, WorkoutActivity.class);
        intent.putExtra(bundleKey, params);
        parent.startActivity(intent);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);
        Bundle args = getIntent().getExtras();
        if (args != null) {
            WorkoutParams params = args.getParcelable(bundleKey);
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
                    handleFinishedWorkout(true);
                } else {
                    if (workout.longEnough())
                        PersistenceService.updateCurrentWeek(workout.type, workout.duration, null);
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
                    handleFinishedWorkout(false);
                } else {
                    if (workout.longEnough())
                        PersistenceService.updateCurrentWeek(workout.type, workout.duration, null);
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
        boolean finishedWorkout = false, showModal = false;
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
                break;

            default:
                if (workout.testMax)
                    showModal = true;
                break;
        }

        if (finishedWorkout) {
            workout.setDuration();
            handleFinishedWorkout(true);
        } else if (showModal) {
            AddWorkoutUpdateMaxesDialog modal = AddWorkoutUpdateMaxesDialog.newInstance(exerciseIdx);
            modal.show(getSupportFragmentManager(), "AddWorkoutUpdateMaxesDialog");
        }
    }

    public void finishedGroup() {
        handleTap(workout.index, 255, Workout.EventOption.finishGroup);
    }

    public void finishedExercise() {
        handleTap(workout.index, workout.group.index, (byte) 0);
    }

    private void sendBroadcast(int totalCompleted) {
        Intent intent = new Intent(notification);
        intent.putExtra(userInfo, totalCompleted);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void handleFinishedWorkout(boolean close) {
        int totalCompleted = 0;

        if (workout.testMax) {
            AppCoordinator.shared.updateMaxWeights(weights);
            if (workout.duration < 15)
                workout.duration = 15;
        }

        if (workout.longEnough()) {
            if (workout.day >= 0)
                totalCompleted = AppUserData.shared.addCompletedWorkout(workout.day);
            PersistenceService.updateCurrentWeek(
              workout.type, workout.duration, workout.testMax ? weights : null);
        }

        sendBroadcast(totalCompleted);
        if (close)
            finish();
    }

    void finishedBottomSheet(BottomSheetDialogFragment dialog, int index, short weight) {
        dialog.dismiss();
        weights[index] = weight;
        handleTap(0, index, (byte) 0);
    }
}
