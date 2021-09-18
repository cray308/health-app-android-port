package com.example.healthappandroid.hometab.addWorkout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.healthappandroid.R;
import com.example.healthappandroid.common.shareddata.AppColors;
import com.example.healthappandroid.common.shareddata.AppCoordinator;
import com.example.healthappandroid.common.workouts.ExerciseEntry;
import com.example.healthappandroid.common.workouts.Workout;
import com.example.healthappandroid.hometab.addWorkout.utils.WorkoutNotifService;
import com.example.healthappandroid.hometab.addWorkout.views.ExerciseContainer;
import com.example.healthappandroid.hometab.addWorkout.views.ExerciseView;

import java.time.Instant;

public class WorkoutActivity extends AppCompatActivity {
    private Workout workout;
    private LinearLayout groupsStack;
    private ExerciseContainer firstContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);

        System.out.println("chris - WorkoutActivity onCreate!!!");
        workout = AppCoordinator.shared.homeCoordinator.child.workout;
        setTitle(workout.title);

        WorkoutNotifService.setup(this);

        Button backBtn = findViewById(R.id.workoutBackBtn);
        backBtn.setOnClickListener(backListener);
        Button startBtn = findViewById(R.id.workoutStartStopBtn);
        startBtn.setOnClickListener(startStopListener);

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
        if (workout.stopType == 0)
            workout.stopType = Workout.StopTypeManual;
        super.onDestroy();
    }

    private final View.OnClickListener backListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            workout.stopTime = Instant.now().getEpochSecond() + 1;
            workout.stopType = Workout.StopTypeManual;
            WorkoutActivity.this.finish();
        }
    };

    private final View.OnClickListener startStopListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Button btn = (Button) view;
            if (btn.getText().equals("Start")) {
                btn.setText("End");
                btn.setTextColor(AppColors.red);
                workout.startTime = Instant.now().getEpochSecond();
                handleTap(0, 0, Workout.EventOptionStartGroup);
            } else {
                workout.stopTime = Instant.now().getEpochSecond() + 1;
                workout.stopType = Workout.StopTypeManual;
                WorkoutActivity.this.finish();
            }
        }
    };

    private final View.OnClickListener tapHandler = view -> {
        int tag = view.getId();
        int groupIdx = (tag & 0xff00) >> 8;
        int exerciseIdx = (tag & 0xff) - 1;
        handleTap(groupIdx, exerciseIdx, Workout.EventOptionNone);
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
                workout.stopTime = Instant.now().getEpochSecond() + 1;
                workout.stopType = Workout.StopTypeCompleted;
                break;
            case Workout.TransitionFinishedCircuitDeleteFirst:
                firstContainer = (ExerciseContainer) groupsStack.getChildAt(1);
                groupsStack.removeViewAt(0);
            case Workout.TransitionFinishedCircuit:
                firstContainer.divider.setVisibility(View.GONE);
                firstContainer.headerLabel.setText(workout.group.createHeader());
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
            finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        byte type = intent.getByteExtra(
                WorkoutNotifService.IntentExtraKey, WorkoutNotifService.NotificationFinishExercise);

        if (type == WorkoutNotifService.NotificationFinishExercise) {
            handleTap(workout.index, workout.group.index, Workout.EventOptionNone);
        } else {
            handleTap(workout.index, 255, Workout.EventOptionFinishGroup);
        }
    }
}
