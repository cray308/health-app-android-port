package com.example.healthAppAndroid.homeTab.addWorkout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.time.Instant;
import java.util.Locale;

public final class WorkoutActivity extends AppCompatActivity {
    private static abstract class Event {
        private static final byte finishGroup = 1;
        private static final byte finishExercise = 2;
    }

    private final static String bundleKey = "WorkoutActivityKey";
    public static final String notification = "FinishedWorkoutNotification";
    public static final String userInfo = "totalWorkouts";
    private Workout workout;
    private LinearLayout groupsStack;
    private ExerciseContainer firstContainer;
    private final short[] weights = {0, 0, 0, 0};

    public static void start(FragmentActivity parent, Parcelable params) {
        Intent intent = new Intent(parent, WorkoutActivity.class);
        intent.putExtra(bundleKey, params);
        parent.startActivity(intent);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);
        Bundle args = getIntent().getExtras();
        if (args != null)
            workout = ExerciseManager.getWorkoutFromLibrary(this, args.getParcelable(bundleKey));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null)
            bar.setDisplayHomeAsUpEnabled(true);
        ((TextView)toolbar.findViewById(R.id.titleLabel)).setText(workout.title);
        toolbar.findViewById(R.id.startStopButton).setOnClickListener(view -> {
            Button btn = (Button)view;
            if (btn.getText().equals(getString(R.string.start))) {
                btn.setText(getString(R.string.end));
                btn.setTextColor(AppColors.red);
                workout.startTime = Instant.now().getEpochSecond();
                workout.startGroup(this, true);
                int nExercises = workout.group.exercises.length;
                for (int i = 0; i < nExercises; ++i) {
                    firstContainer.viewsArr[i].configure();
                }
                View nextView;
                if (!workout.group.headerStr.str.toString().isEmpty()) {
                    nextView = firstContainer.headerView.headerLabel;
                } else {
                    nextView = firstContainer.viewsArr[0].button;
                }
                nextView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
            } else {
                NotificationService.cleanup(this);
                boolean longEnough = workout.setDuration();
                if (workout.isCompleted()) {
                    handleFinishedWorkout(true, longEnough);
                } else {
                    if (longEnough)
                        AppCoordinator.shared.addWorkoutData(
                          (byte)-1, workout.type, workout.duration, null);
                    sendBroadcast((byte)0);
                    finish();
                }
            }
        });
        groupsStack = findViewById(R.id.workoutGroupsStack);
        NotificationService.setup(this);

        int count = workout.activities.length;
        for (int i = 0; i < count; ++i) {
            groupsStack.addView(new ExerciseContainer(this, workout.activities[i], i, tapHandler));
        }
        groupsStack.getChildAt(count - 1).setLayoutParams(new LinearLayout.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        firstContainer = (ExerciseContainer)groupsStack.getChildAt(0);
        firstContainer.headerView.divider.setVisibility(View.GONE);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != android.R.id.home) return super.onOptionsItemSelected(item);

        NotificationService.cleanup(this);
        if (workout.startTime != 0) {
            boolean longEnough = workout.setDuration();
            if (workout.isCompleted()) {
                handleFinishedWorkout(false, longEnough);
            } else {
                if (longEnough)
                    AppCoordinator.shared.addWorkoutData(
                      (byte)-1, workout.type, workout.duration, null);
                sendBroadcast((byte)0);
            }
        } else {
            sendBroadcast((byte)0);
        }
        onBackPressed();
        return true;
    }

    private final View.OnClickListener tapHandler = view -> {
        int tag = view.getId();
        int groupIdx = ((tag & 0xff00) >> 8), exerciseIdx = ((tag & 0xff) - 1);
        handleTap(groupIdx, exerciseIdx, 0);
    };

    private void handleTap(int groupIdx, int exerciseIdx, int option) {
        if (groupIdx != workout.index ||
            (exerciseIdx != workout.group.index && option != Event.finishGroup)) {
            return;
        }

        ExerciseView v = firstContainer.viewsArr[exerciseIdx];
        View nextView = null;
        int transition = Workout.Transition.noChange;
        switch (option) {
            case Event.finishGroup:
                transition = Workout.Transition.finishedCircuitDeleteFirst;
                if (++workout.index == workout.activities.length) {
                    transition = Workout.Transition.completedWorkout;
                    break;
                }
                workout.group = workout.activities[workout.index];
                workout.startGroup(this, true);
                break;

            case Event.finishExercise:
                v.userInteractionEnabled = true;
                v.button.setEnabled(true);
                if (workout.type == WorkoutType.endurance) {
                    v.headerLabel.setTextColor(AppColors.green);
                    v.headerLabel.setText(getString(R.string.exerciseDurationMet));
                    v.headerLabel.setVisibility(View.VISIBLE);
                    v.updateAccessibility();
                    nextView = v.button;
                    break;
                }

            default:
                boolean exerciseDone = v.entry.cycle(this, groupIdx, exerciseIdx);
                v.configure();
                transition = workout.findTransition(this, exerciseDone);
                break;
        }

        switch (transition) {
            case Workout.Transition.completedWorkout:
                NotificationService.cleanup(this);
                handleFinishedWorkout(true, workout.setDuration());
                return;

            case Workout.Transition.finishedCircuitDeleteFirst:
                firstContainer = (ExerciseContainer)groupsStack.getChildAt(1);
                groupsStack.removeViewAt(0);
                firstContainer.headerView.divider.setVisibility(View.GONE);
                nextView = firstContainer.headerView.headerLabel;
            case Workout.Transition.finishedCircuit:
                if (workout.group.reps > 1 && workout.group.type == Circuit.Type.rounds) {
                    String newNum = String.format(Locale.getDefault(),
                                                  "%d", workout.group.completedReps + 1);
                    workout.group.headerStr.replace(newNum);
                    workout.group.headerStr.length = newNum.length();
                    firstContainer.headerView.headerLabel.setText(workout.group.headerStr.str);
                    nextView = firstContainer.headerView.headerLabel;
                }
                int nExercises = workout.group.exercises.length;
                for (int i = 0; i < nExercises; ++i) {
                    firstContainer.viewsArr[i].configure();
                }
                if (nextView == null)
                    nextView = firstContainer.viewsArr[0].button;
                break;

            case Workout.Transition.finishedExercise:
                firstContainer.viewsArr[workout.group.index].configure();
                break;

            default:
                if (workout.testMax) {
                    UpdateMaxesDialog.init(exerciseIdx).show(
                      getSupportFragmentManager(), "UpdateMaxes");
                    return;
                }
                break;
        }
        if (nextView != null)
            nextView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
    }

    void finishedGroup(int group) { handleTap(group, 0, Event.finishGroup); }

    void finishedExercise(int group, int index) { handleTap(group, index, Event.finishExercise); }

    private void sendBroadcast(byte completed) {
        Intent intent = new Intent(notification);
        intent.putExtra(userInfo, completed);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void handleFinishedWorkout(boolean close, boolean longEnough) {
        byte completed = 0;
        if (workout.testMax)
            longEnough = true;

        if (longEnough) {
            completed = AppCoordinator.shared.addWorkoutData(
              workout.day, workout.type, workout.duration, weights[0] > 0 ? weights : null);
        }

        sendBroadcast(completed);
        if (close)
            finish();
    }

    void finishedBottomSheet(BottomSheetDialogFragment dialog, int index, short weight) {
        dialog.dismiss();
        weights[index] = weight;
        handleTap(0, index, 0);
    }
}
