package com.example.healthAppAndroid.homeTab.addWorkout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.healthAppAndroid.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.time.Instant;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.Queue;

public final class WorkoutActivity extends AppCompatActivity {
    private static abstract class Event {
        private static final int finishCircuit = 1;
        private static final int finishExercise = 2;
    }

    private static final class Notification {
        private final int section;
        private final int row;
        private final int event;

        private Notification(int section, int row, int event) {
            this.section = section;
            this.row = row;
            this.event = event;
        }
    }

    private static final String BundleKey = "WorkoutActivityKey";
    public static final String notification = "FinishedWorkoutNotification";
    public static final String outputKey = "WorkoutOutput";

    public static void start(FragmentActivity parent, Parcelable params) {
        parent.startActivity(new Intent(parent, WorkoutActivity.class).putExtra(BundleKey, params));
    }

    private Workout workout;
    private LinearLayout stack;
    private final Queue<Notification> queue = new PriorityQueue<>(8, (n1, n2) -> {
        if (n1.event > n2.event) return 1;
        return n1.event == n2.event ? 0 : -1;
    });
    private final int[] weights = {0, 0, 0, 0};
    private boolean isActive = true;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Bundle args = getIntent().getExtras();
        if (args != null) {
            Workout.Params params = args.getParcelable(BundleKey);
            workout = ExerciseManager.workout(this, params);
            String[] wNames = getResources().getStringArray(ExerciseManager.TitleKeys[workout.type]);
            ((TextView)toolbar.findViewById(R.id.titleLabel)).setText(wNames[params.index]);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
        toolbar.findViewById(R.id.startStopButton).setOnClickListener(view -> {
            Button button = (Button)view;
            if (button.getText().equals(getString(R.string.start))) {
                button.setText(getString(R.string.end));
                button.setTextColor(ContextCompat.getColor(this, R.color.systemRed));
                workout.startTime = Instant.now().getEpochSecond();
                workout.circuits[0].start(this, 0, true);
                ExerciseContainer container = (ExerciseContainer)stack.getChildAt(0);
                ExerciseView ev = container.views[0];
                ev.configure();
                View nextView = workout.circuits[0].header.str.toString().isEmpty()
                                ? ev.button : container.headerView.header;
                nextView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
            } else {
                NotificationService.cleanup(this);
                if (workout.isCompleted()) {
                    handleFinishedWorkout(true);
                } else {
                    sendBroadcast(new Workout.Output((byte)-1, workout.type, workout.duration, null));
                    finish();
                }
            }
        });

        View.OnClickListener tapListener = view -> {
            int tag = view.getId();
            handleEvent(((tag & 0xff00) >> 8), ((tag & 0xff) - 1), 0);
        };

        stack = findViewById(R.id.stack);
        int count = workout.circuits.length;
        for (int i = 0; i < count; ++i) {
            stack.addView(new ExerciseContainer(this, workout.circuits[i], i, tapListener));
        }
        stack.getChildAt(count - 1).setLayoutParams(new LinearLayout.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        ((ExerciseContainer)stack.getChildAt(0)).headerView.divider.setVisibility(View.GONE);
        NotificationService.setup(this);
    }

    protected void onResume() {
        super.onResume();
        isActive = true;

        boolean empty = queue.isEmpty();
        while (!empty) {
            Notification note = queue.remove();
            handleEvent(note.section, note.row, note.event);
            empty = queue.isEmpty();
        }
    }

    protected void onStop() {
        super.onStop();
        isActive = false;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != android.R.id.home) return super.onOptionsItemSelected(item);

        NotificationService.cleanup(this);
        if (workout.startTime != 0) {
            if (workout.isCompleted()) {
                handleFinishedWorkout(false);
            } else {
                sendBroadcast(new Workout.Output((byte)-1, workout.type, workout.duration, null));
            }
        } else {
            sendBroadcast(new Workout.Output((byte)-1, (byte)0, 0, null));
        }
        onBackPressed();
        return true;
    }

    private void handleEvent(int section, int row, int event) {
        Circuit circuit = workout.circuits[section];
        if (row != circuit.index && event != Event.finishCircuit) return;

        ExerciseContainer container = (ExerciseContainer)stack.getChildAt(0);
        ExerciseView ev = container.views[row];
        View next = null;
        int transition = 0;
        switch (event) {
            case Event.finishCircuit:
                transition = workout.increment(this);
                break;

            case Event.finishExercise:
                ev.userInteractionEnabled = true;
                ev.button.setEnabled(true);
                if (workout.type == Workout.Type.endurance) {
                    ev.header.setTextColor(ContextCompat.getColor(this, R.color.systemGreen));
                    String durationMsg = getString(R.string.exerciseDuration);
                    ev.header.setText(durationMsg);
                    ev.header.setVisibility(View.VISIBLE);
                    ev.updateAccessibility(durationMsg, ev.exercise.title.str);
                    next = ev.button;
                    break;
                }
            default:
                boolean exerciseDone = ev.exercise.cycle(this, section, row);
                ev.configure();
                if (exerciseDone) {
                    transition = circuit.increment(this, section);
                    if (transition == Workout.Transition.finishedCircuitDeleteFirst)
                        transition = workout.increment(this);
                }
                break;
        }

        switch (transition) {
            case Workout.Transition.completedWorkout:
                NotificationService.cleanup(this);
                workout.setDuration();
                handleFinishedWorkout(true);
                return;

            case Workout.Transition.finishedCircuitDeleteFirst:
                circuit = workout.circuits[section + 1];
                container = (ExerciseContainer)stack.getChildAt(1);
                stack.removeViewAt(0);
                container.headerView.divider.setVisibility(View.GONE);
                next = container.headerView.header;
            case Workout.Transition.finishedCircuit:
                if (circuit.reps > 1 && circuit.type == Circuit.Type.rounds) {
                    Locale locale = Locale.getDefault();
                    circuit.header.replace(String.format(locale, "%d", circuit.completedReps + 1));
                    container.headerView.header.setText(circuit.header.str);
                    next = container.headerView.header;
                }

                for (ExerciseView v : container.views) {
                    if (circuit.type == Circuit.Type.decrement)
                        v.button.setText(v.exercise.title.str);
                    v.configure();
                }
                if (next == null) next = container.views[0].button;
                break;

            case Workout.Transition.finishedExercise:
                ev = container.views[circuit.index];
                ev.configure();
                next = ev.button;
                break;

            default:
                if (workout.testMax) {
                    UpdateMaxesDialog dialog = UpdateMaxesDialog.init(row, workout.bodyWeight);
                    dialog.show(getSupportFragmentManager(), "UpdateMaxes");
                    return;
                }
                break;
        }
        if (next != null) next.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
    }

    void receivedNote(int section, int row, int type) {
        int event = type + 1;
        if (!isActive) {
            queue.add(new Notification(section, row, event));
            return;
        }

        handleEvent(section, row, event);
    }

    private void sendBroadcast(Parcelable data) {
        Intent intent = new Intent(notification).putExtra(outputKey, data);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void handleFinishedWorkout(boolean close) {
        int[] lifts = null;
        if (weights[3] != 0) {
            lifts = weights;
            workout.duration = Math.max(workout.duration, Workout.minDuration);
        }
        sendBroadcast(new Workout.Output(workout.day, workout.type, workout.duration, lifts));
        if (close) finish();
    }

    void finishedBottomSheet(BottomSheetDialogFragment dialog, int index, int weight) {
        dialog.dismiss();
        weights[index] = weight;
        handleEvent(0, index, 0);
    }
}
