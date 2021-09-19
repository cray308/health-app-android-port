package com.example.healthappandroid.hometab;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import androidx.fragment.app.FragmentActivity;

import com.example.healthappandroid.common.shareddata.AppCoordinator;
import com.example.healthappandroid.common.shareddata.AppUserData;
import com.example.healthappandroid.common.workouts.ExerciseManager;
import com.example.healthappandroid.common.workouts.Workout;
import com.example.healthappandroid.hometab.addWorkout.views.WorkoutActivity;
import com.example.healthappandroid.hometab.addWorkout.WorkoutCoordinator;
import com.example.healthappandroid.hometab.data.HomeViewModel;
import com.example.healthappandroid.hometab.view.HomeFragment;
import com.example.healthappandroid.hometab.view.HomeSetupWorkoutDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class HomeTabCoordinator {
    private static final int CustomWorkoutIndexTestMax = 0;
    private static final int CustomWorkoutIndexEndurance = 1;
    //private static final int CustomWorkoutIndexStrength = 2;
    private static final int CustomWorkoutIndexSE = 3;
    private static final int CustomWorkoutIndexHIC = 4;

    public final HomeFragment fragment = new HomeFragment();
    public final HomeViewModel viewModel;
    public WorkoutCoordinator child;

    public HomeTabCoordinator() {
        viewModel = fragment.viewModel;
        fragment.delegate = this;
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
            .setTitle("Nicely done!")
            .setMessage("Great job meeting your workout goal this week.")
            .setPositiveButton("OK", null);
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
                Workout w = ExerciseManager.getWorkoutFromLibrary(
                        context, Workout.TypeStrength, 2, 1, 1, 100);
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
        modal.show(AppCoordinator.shared.fm, "HomeSetupWorkoutDialog");
    }

    public void finishedSettingUpCustomWorkout(BottomSheetDialogFragment modal,
                                               byte type, int index, short[] params) {
        Workout w = ExerciseManager.getWorkoutFromLibrary(
                fragment.getContext(), type, index, params[0], params[1], params[2]);
        if (w != null)
            navigateToAddWorkout(modal, w);
    }

    public void updateUI() { fragment.updateWorkoutsList(); }

    public void resetUI() {
        viewModel.fetchData(fragment.getContext());
        fragment.createWorkoutsList();
    }
}
