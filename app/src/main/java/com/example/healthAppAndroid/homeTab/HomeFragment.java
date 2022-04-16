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
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.core.AppColors;
import com.example.healthAppAndroid.core.AppUserData;
import com.example.healthAppAndroid.core.StatusButton;
import com.example.healthAppAndroid.homeTab.addWorkout.ExerciseManager;
import com.example.healthAppAndroid.homeTab.addWorkout.HeaderView;
import com.example.healthAppAndroid.homeTab.addWorkout.WorkoutActivity;
import com.example.healthAppAndroid.homeTab.addWorkout.WorkoutParams;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.format.TextStyle;
import java.util.Locale;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

public final class HomeFragment extends Fragment {
    private View weeklyWkContainer;
    private LinearLayout weeklyWorkoutStack;
    private HeaderView customWorkoutsHeader;
    private KonfettiView confettiView;
    private int numWorkouts = 0;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context c, Intent intent) {
            handleFinishedWorkout(intent.getByteExtra(WorkoutActivity.userInfo, (byte)0));
        }
    };

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, null);
        int[] customBtnIds = {R.id.customButton1, R.id.customButton2, R.id.customButton3,
                              R.id.customButton4, R.id.customButton5};
        for (int i = 0; i < 5; ++i) {
            StatusButton currBtn = view.findViewById(customBtnIds[i]);
            setTag(currBtn.button, i);
            currBtn.button.setOnClickListener(customBtnListener);
            currBtn.updateAccessibility();
        }
        weeklyWkContainer = view.findViewById(R.id.weeklyWorkoutsContainer);
        weeklyWkContainer.setVisibility(View.GONE);
        weeklyWorkoutStack = view.findViewById(R.id.weeklyWorkoutsStack);
        customWorkoutsHeader = view.findViewById(R.id.customWorkoutsHeader);
        confettiView = view.findViewById(R.id.confettiView);
        createWorkoutsList(AppUserData.shared.currentPlan);
    }

    private static int getTag(View v) {
        int tag = v.getId();
        return --tag;
    }

    private static void setTag(View v, int tag) {
        v.setId(tag + 1);
    }

    public void createWorkoutsList(byte plan) {
        weeklyWorkoutStack.removeAllViews();
        numWorkouts = 0;
        Context c = getContext();

        if (plan < 0 || AppUserData.shared.planStart > Instant.now().getEpochSecond()) {
            weeklyWkContainer.setVisibility(View.GONE);
            customWorkoutsHeader.divider.setVisibility(View.GONE);
            return;
        }

        String[] workoutNames = ExerciseManager.getWeeklyWorkoutNames(c, plan);
        DayOfWeek[] days = DayOfWeek.values();
        Locale l = Locale.getDefault();

        for (int i = 0; i < 7; ++i) {
            if (workoutNames[i] == null) continue;
            StatusButton btn = new StatusButton(c);
            setTag(btn.button, i);
            btn.button.setOnClickListener(dayWorkoutListener);
            btn.headerLabel.setText(days[i].getDisplayName(TextStyle.FULL, l));
            btn.button.setText(workoutNames[i]);
            weeklyWorkoutStack.addView(btn);
            btn.updateAccessibility();
            numWorkouts += 1;
        }
        weeklyWkContainer.setVisibility(View.VISIBLE);
        customWorkoutsHeader.divider.setVisibility(View.VISIBLE);
        updateWorkoutsList(AppUserData.shared.completedWorkouts);
    }

    public void updateWorkoutsList(byte completed) {
        if (numWorkouts == 0) return;

        for (int i = 0; i < numWorkouts; ++i) {
            StatusButton v = (StatusButton)weeklyWorkoutStack.getChildAt(i);
            boolean enabled = (completed & (1 << getTag(v.button))) == 0;
            v.button.setEnabled(enabled);
            v.button.setTextColor(enabled ? AppColors.labelNormal : AppColors.labelDisabled);
            v.checkbox.setBackgroundColor(enabled ? AppColors.gray : AppColors.green);
        }
    }

    private final View.OnClickListener customBtnListener = view -> {
        byte index = (byte)getTag(view);

        if (index == 0) {
            WorkoutParams params = new WorkoutParams((byte)-1);
            params.type = 0;
            params.index = 2;
            params.reps = params.sets = 1;
            params.weight = 100;
            navigateToAddWorkout(null, params);
            return;
        }

        SetupWorkoutDialog.init(getContext(), --index).show(
          getParentFragmentManager(), "SetupWorkout");
    };

    private final View.OnClickListener dayWorkoutListener = view ->
      navigateToAddWorkout(null, ExerciseManager.getWeeklyWorkout(
        getContext(), getTag(view), AppUserData.shared.currentPlan));

    void navigateToAddWorkout(BottomSheetDialogFragment dialog, Parcelable params) {
        if (dialog != null)
            dialog.dismiss();

        FragmentActivity activity = getActivity();
        Context c = getContext();
        if (activity == null || c == null) return;

        IntentFilter filter = new IntentFilter(WorkoutActivity.notification);
        LocalBroadcastManager.getInstance(c).registerReceiver(receiver, filter);
        WorkoutActivity.start(activity, params);
    }

    private void handleFinishedWorkout(byte completed) {
        Context c = getContext();
        if (c == null) return;
        LocalBroadcastManager.getInstance(c).unregisterReceiver(receiver);

        if (completed == 0) return;

        int totalCompleted = 0;
        for (int i = 0; i < 7; ++i) {
            if (((1 << i) & completed) != 0)
                ++totalCompleted;
        }

        updateWorkoutsList(completed);
        if (numWorkouts == totalCompleted)
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
              .setPositiveButton(
                getString(com.google.android.material.R.string.mtrl_picker_confirm), null);
            builder.create().show();
        }, 5500);
    }
}
