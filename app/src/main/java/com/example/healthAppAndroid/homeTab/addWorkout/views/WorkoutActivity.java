package com.example.healthAppAndroid.homeTab.addWorkout.views;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
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
import com.example.healthAppAndroid.homeTab.addWorkout.utils.WorkoutNotifService;

public class WorkoutActivity extends AppCompatActivity {
    public WorkoutCoordinator delegate;
    public Workout workout;
    private LinearLayout groupsStack;
    private ExerciseContainer firstContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
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

        WorkoutNotifService.setup(this);

        groupsStack = findViewById(R.id.workoutGroupsStack);

        for (int i = 0; i < workout.activities.length; ++i) {
            ExerciseContainer v = new ExerciseContainer(this);
            v.setup(workout.activities[i], i, tapHandler);
            groupsStack.addView(v);
        }

        firstContainer = (ExerciseContainer) groupsStack.getChildAt(0);
        firstContainer.divider.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        WorkoutNotifService.cleanup(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.workout_items, menu);
        MenuItem item = menu.findItem(R.id.action_startStop);
        StartStopView view = (StartStopView) item.getActionView();
        view.btn.setOnClickListener(startStopListener);
        return super.onCreateOptionsMenu(menu);
    }

    private final View.OnClickListener startStopListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Button btn = (Button) view;
            if (btn.getText().equals(getString(R.string.start))) {
                btn.setText(getString(R.string.end));
                btn.setTextColor(AppColors.red);
                workout.startTime = DateHelper.getCurrentTime();
                handleTap(0, 0, Workout.EventOptionStartGroup);
            } else {
                workout.setDuration();
                delegate.stoppedWorkout(WorkoutActivity.this);
            }
        }
    };

    private final View.OnClickListener tapHandler = view -> {
        int tag = view.getId();
        int groupIdx = (tag & 0xff00) >> 8;
        int exerciseIdx = (tag & 0xff) - 1;
        handleTap(groupIdx, exerciseIdx, (byte) 0);
    };

    public void handleTap(int groupIdx, int exerciseIdx, byte option) {
        boolean finishedWorkout = false;
        if (groupIdx != workout.index || exerciseIdx != workout.group.index) {
            if (option != Workout.EventOptionFinishGroup || groupIdx != workout.index) return;
        }

        ExerciseView v = firstContainer.viewsArr[workout.group.index];
        switch (workout.findTransitionForEvent(this, v, option)) {
            case Workout.TransitionCompletedWorkout:
                finishedWorkout = true;
                workout.setDuration();
                break;
            case Workout.TransitionFinishedCircuitDeleteFirst:
                firstContainer = (ExerciseContainer) groupsStack.getChildAt(1);
                groupsStack.removeViewAt(0);
            case Workout.TransitionFinishedCircuit:
                firstContainer.divider.setVisibility(View.GONE);
                firstContainer.headerLabel.setText(workout.group.createHeader(this));
                int i = 0;
                for (ExerciseEntry e : workout.group.exercises)
                    firstContainer.viewsArr[i++].configure(e);
                break;
            case Workout.TransitionFinishedExercise:
                v = firstContainer.viewsArr[workout.group.index];
                v.configure(workout.entry);
            default:
        }

        if (finishedWorkout)
            delegate.completedWorkout(this, null, true);
    }

    public void finishedGroup() {
        handleTap(workout.index, 255, Workout.EventOptionFinishGroup);
    }

    public void finishedExercise() {
        handleTap(workout.index, workout.group.index, (byte) 0);
    }
}
