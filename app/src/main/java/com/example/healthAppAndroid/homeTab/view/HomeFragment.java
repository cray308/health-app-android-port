package com.example.healthAppAndroid.homeTab.view;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.helpers.DateHelper;
import com.example.healthAppAndroid.common.helpers.ViewHelper;
import com.example.healthAppAndroid.common.shareddata.AppColors;
import com.example.healthAppAndroid.common.shareddata.AppUserData;
import com.example.healthAppAndroid.common.views.StatusButton;
import com.example.healthAppAndroid.common.workouts.ExerciseManager;
import com.example.healthAppAndroid.homeTab.HomeTabCoordinator;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Locale;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

public final class HomeFragment extends Fragment {
    private String[] timeNames;
    public int numWorkouts = 0;
    public HomeTabCoordinator delegate;
    private TextView greetingLabel;
    private View weeklyWkContainer;
    private LinearLayout weeklyWorkoutStack;
    private KonfettiView confettiView;

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
            ViewHelper.setTag(currBtn.button, i);
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
        if (delegate != null)
            delegate.checkForChildCoordinator();

        LocalDateTime localInfo = DateHelper.localTime(DateHelper.getCurrentTime());
        int hour = localInfo.getHour();
        int timeOfDay = 0;

        if (hour >= 12 && hour < 17) {
            timeOfDay = 1;
        } else if (hour < 5 || hour >= 17) {
            timeOfDay = 2;
        }
        greetingLabel.setText(timeNames[timeOfDay]);
    }

    public void createWorkoutsList() {
        weeklyWorkoutStack.removeAllViews();
        numWorkouts = 0;
        Context context = getContext();

        byte plan = AppUserData.shared.currentPlan;
        if (plan < 0 || AppUserData.shared.planStart > DateHelper.getCurrentTime()
            || context == null) {
            weeklyWkContainer.setVisibility(View.GONE);
            return;
        }

        String[] workoutNames = {null, null, null, null, null, null, null};
        ExerciseManager.setWeeklyWorkoutNames(
          context, plan, AppUserData.shared.getWeekInPlan(), workoutNames);

        DayOfWeek[] days = DayOfWeek.values();

        for (int i = 0; i < 7; ++i) {
            if (workoutNames[i] == null) continue;
            StatusButton btn = new StatusButton(context);
            ViewHelper.setTag(btn.button, i);
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
            boolean enabled = (completed & (1 << ViewHelper.getTag(v.button))) == 0;
            v.button.setEnabled(enabled);
            v.button.setTextColor(enabled ? AppColors.labelNormal : AppColors.labelDisabled);
            v.checkbox.setBackgroundColor(enabled ? AppColors.gray : AppColors.green);
        }
    }

    private final View.OnClickListener customBtnListener = new View.OnClickListener() {
        public void onClick(View view) {
            delegate.addWorkoutFromCustomButton(ViewHelper.getTag(view));
        }
    };

    private final View.OnClickListener dayWorkoutListener = new View.OnClickListener() {
        public void onClick(View view) {
            delegate.addWorkoutFromPlan(ViewHelper.getTag(view));
        }
    };

    public void showConfetti() {
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
