package com.example.healthAppAndroid.homeTab;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.core.AppColors;
import com.example.healthAppAndroid.core.AppUserData;
import com.example.healthAppAndroid.core.StatusButton;
import com.example.healthAppAndroid.homeTab.addWorkout.ExerciseManager;
import com.example.healthAppAndroid.homeTab.addWorkout.WorkoutActivity;
import com.example.healthAppAndroid.homeTab.addWorkout.WorkoutParams;
import com.example.healthAppAndroid.homeTab.addWorkout.WorkoutType;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Locale;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

public final class HomeFragment extends Fragment {
    private static abstract class CustomWorkoutIndex {
        private static final int TestMax = 0, Endurance = 1, SE = 3, HIC = 4;
    }

    private String[] timeNames;
    private int numWorkouts = 0;
    private TextView greetingLabel;
    private View weeklyWkContainer;
    private LinearLayout weeklyWorkoutStack;
    private KonfettiView confettiView;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            handleFinishedWorkout(intent.getIntExtra(WorkoutActivity.userInfo, 0));
        }
    };

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        greetingLabel = view.findViewById(R.id.greetingLabel);
        timeNames = getResources().getStringArray(R.array.timesOfDay);
        int[] customBtnIds = {R.id.customButton1, R.id.customButton2, R.id.customButton3,
            R.id.customButton4, R.id.customButton5};
        for (int i = 0; i < 5; ++i) {
            StatusButton currBtn = view.findViewById(customBtnIds[i]);
            setTag(currBtn.button, i);
            currBtn.button.setOnClickListener(customBtnListener);
        }
        weeklyWkContainer = view.findViewById(R.id.weeklyWorkoutsContainer);
        weeklyWkContainer.setVisibility(View.GONE);
        weeklyWorkoutStack = view.findViewById(R.id.weeklyWorkoutsStack);
        confettiView = view.findViewById(R.id.confettiView);
        createWorkoutsList();
    }

    public void onResume() {
        super.onResume();

        long now = Instant.now().getEpochSecond();
        LocalDateTime localInfo = LocalDateTime.ofInstant(Instant.ofEpochSecond(now),
                                                          ZoneId.systemDefault());
        int hour = localInfo.getHour();
        int timeOfDay = 0;

        if (hour >= 12 && hour < 17) {
            timeOfDay = 1;
        } else if (hour < 5 || hour >= 17) {
            timeOfDay = 2;
        }
        greetingLabel.setText(timeNames[timeOfDay]);
    }

    private static int getTag(View v) {
        int tag = v.getId();
        return --tag;
    }

    private static void setTag(View v, int tag) {
        v.setId(tag + 1);
    }

    public void createWorkoutsList() {
        weeklyWorkoutStack.removeAllViews();
        numWorkouts = 0;
        Context context = getContext();

        byte plan = AppUserData.shared.currentPlan;
        if (plan < 0 || AppUserData.shared.planStart > Instant.now().getEpochSecond()
            || context == null) {
            weeklyWkContainer.setVisibility(View.GONE);
            return;
        }

        String[] workoutNames = ExerciseManager.getWeeklyWorkoutNames(
          context, plan, AppUserData.shared.getWeekInPlan());
        DayOfWeek[] days = DayOfWeek.values();

        for (int i = 0; i < 7; ++i) {
            if (workoutNames[i] == null) continue;
            StatusButton btn = new StatusButton(context);
            setTag(btn.button, i);
            btn.button.setOnClickListener(dayWorkoutListener);
            btn.headerLabel.setText(days[i].getDisplayName(TextStyle.FULL, Locale.US));
            btn.button.setText(workoutNames[i]);
            weeklyWorkoutStack.addView(btn);
            numWorkouts += 1;
        }
        weeklyWkContainer.setVisibility(View.VISIBLE);
        updateWorkoutsList();
    }

    public void updateWorkoutsList() {
        if (numWorkouts == 0) return;

        byte completed = AppUserData.shared.completedWorkouts;
        for (int i = 0; i < numWorkouts; ++i) {
            StatusButton v = (StatusButton) weeklyWorkoutStack.getChildAt(i);
            boolean enabled = (completed & (1 << getTag(v.button))) == 0;
            v.button.setEnabled(enabled);
            v.button.setTextColor(enabled ? AppColors.labelNormal : AppColors.labelDisabled);
            v.checkbox.setBackgroundColor(enabled ? AppColors.gray : AppColors.green);
        }
    }

    private final View.OnClickListener customBtnListener = view -> {
        Context context = getContext();
        int index = getTag(view);
        byte type = WorkoutType.strength;
        if (index == CustomWorkoutIndex.SE) {
            type = WorkoutType.SE;
        } else if (index == CustomWorkoutIndex.HIC) {
            type = WorkoutType.HIC;
        } else if (index == CustomWorkoutIndex.Endurance) {
            type = WorkoutType.endurance;
        } else if (index == CustomWorkoutIndex.TestMax) {
            WorkoutParams params = new WorkoutParams((byte) -1);
            params.type = WorkoutType.strength;
            params.index = 2;
            params.sets = params.reps = 1;
            params.weight = 100;
            navigateToAddWorkout(null, params);
            return;
        }

        String[] names = ExerciseManager.getWorkoutNamesForType(context, type);
        if (names == null || names.length == 0) return;

        HomeSetupWorkoutDialog.Params params = new HomeSetupWorkoutDialog.Params(type, names);
        HomeSetupWorkoutDialog modal = HomeSetupWorkoutDialog.newInstance(params);
        modal.show(getParentFragmentManager(), "HomeSetupWorkoutDialog");
    };

    private final View.OnClickListener dayWorkoutListener = view -> {
        byte plan = AppUserData.shared.currentPlan;
        WorkoutParams params = ExerciseManager.getWeeklyWorkout(
          getContext(), plan, AppUserData.shared.getWeekInPlan(), getTag(view));
        if (params != null)
            navigateToAddWorkout(null, params);
    };

    void navigateToAddWorkout(BottomSheetDialogFragment dialog, WorkoutParams params) {
        if (dialog != null)
            dialog.dismiss();

        FragmentActivity activity = getActivity();
        Context context = getContext();
        if (activity == null || context == null) return;

        IntentFilter filter = new IntentFilter(WorkoutActivity.notification);
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter);
        WorkoutActivity.start(activity, params);
    }

    private void handleFinishedWorkout(int totalCompleted) {
        Context context = getContext();
        if (context != null)
            LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);

        boolean update = totalCompleted != 0;
        if (update)
            updateWorkoutsList();

        if (update && numWorkouts == totalCompleted)
            new Handler().postDelayed(this::showConfetti, 2500);
    }

    private void showConfetti() {
        confettiView.setVisibility(View.VISIBLE);
        confettiView.build()
            .addColors(AppColors.red, AppColors.blue, AppColors.green, AppColors.orange)
            .setDirection(0.0, 359.0)
            .setSpeed(1f, 5f)
            .setFadeOutEnabled(true)
            .setTimeToLive(5000)
            .addShapes(Shape.Square.INSTANCE, Shape.Circle.INSTANCE)
            .addSizes(new Size(12, 5f))
            .setPosition(150f, null, -50f, null)
            .streamFor(128, 4500);
        new Handler().postDelayed(() -> {
            confettiView.setVisibility(View.GONE);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
              .setTitle(getString(R.string.homeAlertTitle))
              .setMessage(getString(R.string.homeAlertMessage))
              .setPositiveButton(getString(R.string.ok), null);
            builder.create().show();
        }, 5500);
    }
}
