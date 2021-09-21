package com.example.healthAppAndroid.homeTab.view;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.helpers.ViewHelper;
import com.example.healthAppAndroid.common.shareddata.AppColors;
import com.example.healthAppAndroid.common.shareddata.AppCoordinator;
import com.example.healthAppAndroid.common.shareddata.AppUserData;
import com.example.healthAppAndroid.common.views.StatusButton;
import com.example.healthAppAndroid.homeTab.HomeTabCoordinator;
import com.example.healthAppAndroid.homeTab.data.HomeViewModel;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Locale;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

public class HomeFragment extends Fragment {
    private String[] timeNames;

    public final HomeViewModel viewModel = new HomeViewModel();
    public HomeTabCoordinator delegate;
    private TextView greetingLabel;
    private View weeklyWkContainer;
    private LinearLayout weeklyWorkoutStack;
    private KonfettiView confettiView;

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
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
        updateGreeting();
        viewModel.fetchData(getContext());
        createWorkoutsList();
    }

    @Override
    public void onResume() {
        super.onResume();
        FragmentActivity a = getActivity();
        if (a != null)
            a.setTitle(AppCoordinator.shared.titles[0]);
        if (delegate != null)
            delegate.checkForChildCoordinator();
        if (viewModel.updateTimeOfDay())
            updateGreeting();
    }

    public void createWorkoutsList() {
        weeklyWorkoutStack.removeAllViews();

        Context context = getContext();
        if (!viewModel.hasWorkoutsForThisWeek() || context == null) {
            weeklyWkContainer.setVisibility(View.GONE);
            return;
        }
        weeklyWkContainer.setVisibility(View.VISIBLE);

        DayOfWeek[] days = DayOfWeek.values();

        for (int i = 0; i < 7; ++i) {
            if (viewModel.workoutNames[i] == null) continue;
            StatusButton dayBtn = new StatusButton(context);
            String dayName = days[i].getDisplayName(TextStyle.FULL, Locale.US);
            dayBtn.setProperties(dayName, viewModel.workoutNames[i], StatusButton.State.Active, true);
            ViewHelper.setTag(dayBtn.button, i);
            dayBtn.button.setOnClickListener(dayWorkoutListener);
            weeklyWorkoutStack.addView(dayBtn);
        }
        updateWorkoutsList();
    }

    public void updateWorkoutsList() {
        int count = weeklyWorkoutStack.getChildCount();
        if (!(viewModel.hasWorkoutsForThisWeek() && count > 0)) return;

        final byte completed = AppUserData.shared.completedWorkouts;
        for (int i = 0; i < count; ++i) {
            StatusButton v = (StatusButton) weeklyWorkoutStack.getChildAt(i);
            boolean enabled = (completed & (1 << ViewHelper.getTag(v.button))) == 0;
            byte state = enabled ? StatusButton.State.Disabled : StatusButton.State.Finished;
            v.updateStateAndButton(state, enabled);
        }
    }

    private void updateGreeting() {
        greetingLabel.setText(timeNames[viewModel.timeOfDay]);
    }

    private final View.OnClickListener customBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            delegate.addWorkoutFromCustomButton(ViewHelper.getTag(view));
        }
    };

    private final View.OnClickListener dayWorkoutListener = new View.OnClickListener() {
        @Override
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
            if (delegate != null)
                delegate.showWeeklyGoalDialog();
        }, 5500);
    }
}
