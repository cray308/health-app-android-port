package com.example.healthAppAndroid.homeTab.addWorkout.views;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.helpers.DateHelper;
import com.example.healthAppAndroid.common.shareddata.AppColors;
import com.example.healthAppAndroid.common.shareddata.AppCoordinator;
import com.example.healthAppAndroid.common.workouts.ExerciseEntry;
import com.example.healthAppAndroid.common.workouts.Workout;
import com.example.healthAppAndroid.homeTab.addWorkout.WorkoutCoordinator;
import com.example.healthAppAndroid.homeTab.addWorkout.utils.NotificationService;

public class WorkoutActivity extends AppCompatActivity {
    private WorkoutCoordinator delegate;
    private Workout workout;
    private LinearLayout groupsStack;
    private ExerciseContainer firstContainer;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null)
            bar.setDisplayHomeAsUpEnabled(true);

        delegate = AppCoordinator.shared.homeCoordinator.child;
        workout = delegate.workout;
        setTitle(workout.title);
        groupsStack = findViewById(R.id.workoutGroupsStack);
        NotificationService.setup(this);

        for (int i = 0; i < workout.activities.length; ++i) {
            ExerciseContainer v = new ExerciseContainer(this);
            v.setup(workout.activities[i], i, tapHandler);
            groupsStack.addView(v);
        }

        firstContainer = (ExerciseContainer) groupsStack.getChildAt(0);
        firstContainer.divider.setVisibility(View.GONE);
    }

    @Override protected void onDestroy() {
        NotificationService.cleanup(this);
        super.onDestroy();
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.workout_items, menu);
        MenuItem item = menu.findItem(R.id.action_startStop);
        StartStopView startStopView = (StartStopView) item.getActionView();
        startStopView.btn.setOnClickListener(view -> {
            Button btn = (Button) view;
            if (btn.getText().equals(getString(R.string.start))) {
                btn.setText(getString(R.string.end));
                btn.setTextColor(AppColors.red);
                workout.startTime = DateHelper.getCurrentTime();
                handleTap(0, 0, Workout.EventOption.startGroup);
            } else {
                workout.setDuration();
                delegate.stoppedWorkout(WorkoutActivity.this);
            }
        });
        return super.onCreateOptionsMenu(menu);
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
                workout.setDuration();
                break;

            case Workout.Transition.finishedCircuitDeleteFirst:
                firstContainer = (ExerciseContainer) groupsStack.getChildAt(1);
                groupsStack.removeViewAt(0);
            case Workout.Transition.finishedCircuit:
                firstContainer.divider.setVisibility(View.GONE);
                firstContainer.headerLabel.setText(workout.group.createHeader(this));
                int i = 0;
                for (ExerciseEntry e : workout.group.exercises)
                    firstContainer.viewsArr[i++].configure(e);
                break;

            case Workout.Transition.finishedExercise:
                v = firstContainer.viewsArr[workout.group.index];
                v.configure(workout.entry);
            default:
        }

        if (finishedWorkout)
            delegate.completedWorkout(this, null, true);
    }

    public void finishedGroup() {
        handleTap(workout.index, 255, Workout.EventOption.finishGroup);
    }

    public void finishedExercise() {
        handleTap(workout.index, workout.group.index, (byte) 0);
    }
}
