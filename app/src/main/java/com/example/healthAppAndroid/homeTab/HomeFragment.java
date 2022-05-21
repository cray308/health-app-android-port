package com.example.healthAppAndroid.homeTab;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Handler;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.core.MainActivity;
import com.example.healthAppAndroid.core.UserData;
import com.example.healthAppAndroid.homeTab.addWorkout.ExerciseManager;
import com.example.healthAppAndroid.homeTab.addWorkout.HeaderView;
import com.example.healthAppAndroid.homeTab.addWorkout.Workout;
import com.example.healthAppAndroid.homeTab.addWorkout.WorkoutActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.format.TextStyle;
import java.util.Locale;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

public final class HomeFragment extends Fragment {
    private static int tag(View view) {
        int tag = view.getId();
        return --tag;
    }

    private static void setTag(View view, int tag) { view.setId(tag + 1); }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Workout.Output data = intent.getParcelableExtra(WorkoutActivity.outputKey);
            LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
            byte completedWorkouts = 0;
            MainActivity activity = (MainActivity)getActivity();
            if (activity != null) completedWorkouts = activity.addWorkoutData(data);
            if (completedWorkouts == 0) return;

            int totalCompleted = 0;
            for (int i = 0; i < 7; ++i) {
                if (((1 << i) & completedWorkouts) != 0) ++totalCompleted;
            }

            updateWorkoutsList(completedWorkouts);
            if (workoutsStack().getChildCount() == totalCompleted)
                new Handler().postDelayed(this::showConfetti, 2500);
        }

        private void showConfetti() {
            FrameLayout view = (FrameLayout)getView();
            Context context = getContext();
            if (view == null || context == null) return;

            KonfettiView confettiView = new KonfettiView(context);
            confettiView.setBackgroundColor(ContextCompat.getColor(context, R.color.lightGray));
            view.addView(confettiView);
            confettiView.build()
                        .addColors(
                          ContextCompat.getColor(context, R.color.systemRed),
                          ContextCompat.getColor(context, R.color.systemBlue),
                          ContextCompat.getColor(context, R.color.systemGreen),
                          ContextCompat.getColor(context, R.color.systemOrange))
                        .setDirection(0.0, 359.0)
                        .setSpeed(1f, 5f)
                        .setFadeOutEnabled(true)
                        .setTimeToLive(5000)
                        .addShapes(Shape.Square.INSTANCE, Shape.Circle.INSTANCE)
                        .addSizes(new Size(12, 5f))
                        .setPosition(150f, null, -50f, null)
                        .streamFor(128, 4500);
            new Handler().postDelayed(() -> {
                view.removeView(confettiView);
                new AlertDialog.Builder(getActivity())
                  .setTitle(getString(R.string.homeAlert))
                  .setMessage(getString(R.string.homeAlertMessage))
                  .setPositiveButton(getString(R.string.ok), null).create().show();
            }, 5500);
        }
    };
    private final View.OnClickListener planListener = view -> {
        UserData data = MainActivity.userData;
        Workout.Params params = ExerciseManager.weeklyWorkout(
          getContext(), tag(view), data.plan, data.weightToUse(), data.lifts);
        navigateToWorkout(null, params);
    };
    private LinearLayout planContainer;
    private View divider;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, null);

        View.OnClickListener customListener = button -> {
            byte index = (byte)tag(button);
            if (index == 0) {
                UserData data = MainActivity.userData;
                Workout.Params params = new Workout.Params((byte)-1);
                params.index = Workout.Params.Index.testMax;
                params.bodyWeight = data.weightToUse();
                params.lifts = data.lifts;
                navigateToWorkout(null, params);
                return;
            }

            SetupWorkoutDialog.init(--index).show(getParentFragmentManager(), "SetupWorkout");
        };

        planContainer = view.findViewById(R.id.planContainer);
        LinearLayout customContainer = view.findViewById(R.id.customContainer);
        divider = ((HeaderView)customContainer.getChildAt(0)).divider;

        for (int i = 0; i < 5; ++i) {
            StatusView sv = (StatusView)customContainer.getChildAt(i + 1);
            setTag(sv.button, i);
            sv.button.setOnClickListener(customListener);
        }
        createWorkoutsList(MainActivity.userData);
    }

    private LinearLayout workoutsStack() { return planContainer.findViewById(R.id.planStack); }

    public void createWorkoutsList(UserData data) {
        LinearLayout stack = workoutsStack();
        stack.removeAllViews();
        Context context = getContext();

        int visibility = data.plan < 0 || data.planStart > Instant.now().getEpochSecond()
                         ? View.GONE : View.VISIBLE;
        planContainer.setVisibility(visibility);
        divider.setVisibility(visibility);
        if (visibility == View.GONE) return;

        String[] workoutNames = ExerciseManager.weeklyWorkoutNames(context, data.plan);
        DayOfWeek[] days = DayOfWeek.values();
        Locale locale = Locale.getDefault();

        for (int i = 0; i < 7; ++i) {
            if (workoutNames[i] == null) continue;
            StatusView sv = new StatusView(context);
            setTag(sv.button, i);
            sv.button.setOnClickListener(planListener);
            String headerText = days[i].getDisplayName(TextStyle.FULL, locale);
            sv.header.setText(headerText);
            sv.button.setText(workoutNames[i]);
            stack.addView(sv);
            sv.updateAccessibility(headerText, workoutNames[i]);
        }
        updateWorkoutsList(data.completedWorkouts);
    }

    public void updateWorkoutsList(byte completedWorkouts) {
        Context context = getContext();
        if (context == null) return;

        LinearLayout stack = workoutsStack();
        int count = stack.getChildCount();
        for (int i = 0; i < count; ++i) {
            StatusView sv = (StatusView)stack.getChildAt(i);
            boolean enable = (completedWorkouts & (1 << tag(sv.button))) == 0;
            sv.button.setEnabled(enable);
            sv.box.setBackgroundColor(ContextCompat.getColor(
              context, enable ? R.color.systemGray : R.color.systemGreen));
        }
    }

    void navigateToWorkout(BottomSheetDialogFragment dialog, Parcelable params) {
        FragmentActivity activity = getActivity();
        Context context = getContext();
        if (activity == null || context == null) return;

        if (dialog != null) dialog.dismiss();
        IntentFilter filter = new IntentFilter(WorkoutActivity.notification);
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter);
        WorkoutActivity.start(activity, params);
    }
}
