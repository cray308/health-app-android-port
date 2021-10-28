package com.example.healthAppAndroid.homeTab.addWorkout.utils;

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
import androidx.core.content.ContextCompat;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.helpers.ViewHelper;
import com.example.healthAppAndroid.homeTab.addWorkout.views.WorkoutActivity;

public abstract class NotificationService {
    public static abstract class Type {
        public static final byte Exercise = 0;
        public static final byte Circuit = 1;
    }

    private static final String ChannelId = "HealthAppAndroid_channel";
    private static String[] messages;
    private static String contentTitle;
    private static int color;
    private static int filterId = 1;
    private static final String[] baseFilters = {
        "com.healthAppAndroid.workoutTimer.exercise", "com.healthAppAndroid.workoutTimer.circuit"
    };
    private static final String[] filters = {null, null};

    private static final int flags = PendingIntent.FLAG_UPDATE_CURRENT |
                                     PendingIntent.FLAG_IMMUTABLE;
    private static final int defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;

    private static int identifier = 1;
    private static NotificationManager notificationMgr;
    private static AlarmManager alarmMgr;
    private static final BroadcastReceiver[] receivers = {null, null};

    public static void setupAppNotifications(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(
            Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(
            ChannelId, "workout_timer_channel", NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(context.getString(R.string.channelDescription));
        channel.setShowBadge(true);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        manager.createNotificationChannel(channel);
    }

    public static void init(Context context) {
        messages = context.getResources().getStringArray(R.array.notifications);
        contentTitle = context.getString(R.string.workoutNotificationTitle);
        color = ContextCompat.getColor(context, R.color.notification);
    }

    public static void setup(Context outerContext) {
        notificationMgr = (NotificationManager) outerContext.getSystemService(
            Context.NOTIFICATION_SERVICE);
        alarmMgr = (AlarmManager) outerContext.getSystemService(Context.ALARM_SERVICE);

        receivers[Type.Exercise] = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                notificationMgr.notify(identifier++, createNotification(context, Type.Exercise));
                ((WorkoutActivity) context).finishedExercise();
            }
        };
        receivers[Type.Circuit] = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                notificationMgr.notify(identifier++, createNotification(context, Type.Circuit));
                ((WorkoutActivity) context).finishedGroup();
            }
        };
        int id = filterId++;
        for (int i = 0; i < 2; ++i) {
            filters[i] = ViewHelper.format("%s.%d", baseFilters[i], id);
            outerContext.registerReceiver(receivers[i], new IntentFilter(filters[i]));
        }
    }

    public static void cleanup(Context context) {
        for (int i = 0; i < 2; ++i) {
            context.unregisterReceiver(receivers[i]);
            receivers[i] = null;
        }
        notificationMgr.cancelAll();
        notificationMgr = null;
        alarmMgr = null;
    }

    public static void scheduleAlarm(Context context, long secondsFromNow, byte type) {
        secondsFromNow = SystemClock.elapsedRealtime() + (secondsFromNow * 1000);
        if (type == Type.Circuit)
            secondsFromNow += 1000;
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, new Intent(filters[type]),
                                                           PendingIntent.FLAG_IMMUTABLE);
        alarmMgr.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, secondsFromNow, pIntent);
    }

    private static Notification createNotification(Context context, byte type) {
        Intent intent = new Intent(context, WorkoutActivity.class);
        intent.setAction("timerAction");
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, flags);
        return new NotificationCompat.Builder(context, ChannelId)
            .setAutoCancel(true)
            .setDefaults(defaults)
            .setSmallIcon(R.drawable.ic_notif)
            .setColor(color)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pIntent)
            .setContentTitle(contentTitle)
            .setContentText(messages[type]).build();
    }
}
