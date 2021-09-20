package com.example.healthAppAndroid.homeTab;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.shareddata.AppUserData;
import com.example.healthAppAndroid.common.workouts.ExerciseManager;
import com.example.healthAppAndroid.common.workouts.Workout;
import com.example.healthAppAndroid.homeTab.addWorkout.views.WorkoutActivity;
import com.example.healthAppAndroid.homeTab.addWorkout.WorkoutCoordinator;
import com.example.healthAppAndroid.homeTab.data.HomeViewModel;
import com.example.healthAppAndroid.homeTab.view.HomeFragment;
import com.example.healthAppAndroid.homeTab.view.HomeSetupWorkoutDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class HomeTabCoordinator {
    private static final int CustomWorkoutIndexTestMax = 0;
    private static final int CustomWorkoutIndexEndurance = 1;
    private static final int CustomWorkoutIndexSE = 3;
    private static final int CustomWorkoutIndexHIC = 4;

    private final HomeFragment fragment;
    private final HomeViewModel viewModel;
    public WorkoutCoordinator child;

    public HomeTabCoordinator(Fragment fragment) {
        this.fragment = (HomeFragment) fragment;
        viewModel = this.fragment.viewModel;
        this.fragment.delegate = this;
    }

    private void navigateToAddWorkout(BottomSheetDialogFragment dialog, Workout w) {
        if (dialog != null)
            dialog.dismiss();

        FragmentActivity main = fragment.getActivity();
        if (main == null) return;

        child = new WorkoutCoordinator(w, this);
        main.startActivity(new Intent(main, WorkoutActivity.class));
    }

    public void showWeeklyGoalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity())
            .setTitle(fragment.getString(R.string.homeAlertTitle))
            .setMessage(fragment.getString(R.string.homeAlertMessage))
            .setPositiveButton(fragment.getString(R.string.ok), null);
        builder.create().show();
    }

    public void finishedAddingWorkout(FragmentActivity activity, int totalCompletedWorkouts) {
        child = null;
        activity.finish();
        fragment.updateWorkoutsList();
        if (viewModel.shouldShowConfetti(totalCompletedWorkouts))
            new Handler().postDelayed(fragment::showConfetti, 750);
    }

    public void checkForChildCoordinator() {
        if (child != null) {
            child.stopWorkoutFromBackButtonPress();
            child = null;
        }
    }

    public void addWorkoutFromPlan(int index) {
        byte plan = AppUserData.shared.currentPlan;
        Workout w = ExerciseManager.getWeeklyWorkoutAtIndex(
            fragment.getContext(), plan, AppUserData.shared.getWeekInPlan(), index);
        if (w != null)
            navigateToAddWorkout(null, w);
    }

    public void addWorkoutFromCustomButton(int index) {
        Context context = fragment.getContext();
        byte type = Workout.TypeStrength;
        switch (index) {
            case CustomWorkoutIndexSE:
                type = Workout.TypeSE;
                break;
            case CustomWorkoutIndexHIC:
                type = Workout.TypeHIC;
                break;
            case CustomWorkoutIndexTestMax:
                Workout.Params params = new Workout.Params((byte) -1);
                params.type = Workout.TypeStrength;
                params.index = 2;
                params.sets = params.reps = 1;
                params.weight = 100;
                Workout w = ExerciseManager.getWorkoutFromLibrary(context, params);
                if (w != null)
                    navigateToAddWorkout(null, w);
                return;
            case CustomWorkoutIndexEndurance:
                type = Workout.TypeEndurance;
            default:
                break;
        }

        String[] names = ExerciseManager.getWorkoutNamesForType(context, type);
        if (names == null || names.length == 0) return;

        HomeSetupWorkoutDialog.Params params = new HomeSetupWorkoutDialog.Params(type, names);
        HomeSetupWorkoutDialog modal = HomeSetupWorkoutDialog.newInstance(params);
        modal.delegate = this;
        modal.show(fragment.getParentFragmentManager(), "HomeSetupWorkoutDialog");
    }

    public void finishedBottomSheet(BottomSheetDialogFragment modal, Workout.Params params) {
        Workout w = ExerciseManager.getWorkoutFromLibrary(fragment.getContext(), params);
        if (w != null)
            navigateToAddWorkout(modal, w);
    }

    public void updateUI() {
        fragment.updateWorkoutsList();
    }

    public void resetUI() {
        viewModel.fetchData(fragment.getContext());
        fragment.createWorkoutsList();
    }
}
