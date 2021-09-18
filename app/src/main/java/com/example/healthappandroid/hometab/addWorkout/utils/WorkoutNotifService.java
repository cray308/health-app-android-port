package com.example.healthappandroid.hometab.addWorkout.utils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;

import androidx.core.app.NotificationCompat;

import com.example.healthappandroid.R;
import com.example.healthappandroid.hometab.addWorkout.views.WorkoutActivity;

public abstract class WorkoutNotifService {
    public static final String ChannelId = "HealthAppAndroid_channel";
    private static final String[] BroadcastFilters = {"com.healthAppAndroid.workoutTimer.exercise", "com.healthAppAndroid.workoutTimer.circuit"};
    public static final String IntentExtraKey = "TimerExtraByte";

    private static final int Flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;

    public static final byte NotificationFinishExercise = 0;
    public static final byte NotificationFinishCircuit = 1;

    private static int identifier = 1;

    private static NotificationManager notificationMgr;
    private static AlarmManager alarmMgr;
    private static final BroadcastReceiver[] receivers = {null, null};

    public static void setupAppNotifications(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(
                ChannelId, "workout_notif_channel", NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Intra-workout updates");
        channel.setShowBadge(true);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        manager.createNotificationChannel(channel);
    }

    public static void setup(Context outerContext) {
        notificationMgr = (NotificationManager) outerContext.getSystemService(
                Context.NOTIFICATION_SERVICE);
        alarmMgr = (AlarmManager) outerContext.getSystemService(Context.ALARM_SERVICE);

        receivers[NotificationFinishExercise] = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("exerciseReceiver");
                NotificationCompat.Builder b = createNotification(
                        context, NotificationFinishExercise);
                notificationMgr.notify(identifier++, b.build());
            }
        };
        receivers[NotificationFinishCircuit] = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("circuitReceiver");
                NotificationCompat.Builder b = createNotification(
                        context, NotificationFinishCircuit);
                notificationMgr.notify(identifier++, b.build());
            }
        };
        for (int i = 0; i < 2; ++i)
            outerContext.registerReceiver(receivers[i], new IntentFilter(BroadcastFilters[i]));
    }

    public static void cleanup(Context context) {
        for (int i = 0; i < 2; ++i) {
            context.unregisterReceiver(receivers[i]);
            receivers[i] = null;
        }
        notificationMgr = null;
        alarmMgr = null;
    }

    public static void scheduleAlarm(Context context, long secondsFromNow, byte type) {
        System.out.println("Scheduling alarm for " + type);
        Intent intent = new Intent(BroadcastFilters[type]);
        secondsFromNow = SystemClock.elapsedRealtime() + (secondsFromNow * 1000);
        if (type == NotificationFinishCircuit)
            secondsFromNow += 500;
        PendingIntent pIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, secondsFromNow, pIntent);
    }

    public static NotificationCompat.Builder createNotification(Context context, byte type) {
        String message;
        if (type == NotificationFinishExercise) {
            message = "Finished exercise!";
        } else {
            message = "Finished AMRAP circuit!";
        }
        Intent intent = new Intent(context, WorkoutActivity.class);
        intent.setAction("notifAction");
        intent.putExtra(IntentExtraKey, type);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, Flags);
        return new NotificationCompat.Builder(context, ChannelId)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pIntent)
                .setContentTitle("Workout Update")
                .setContentText(message);
    }
}
