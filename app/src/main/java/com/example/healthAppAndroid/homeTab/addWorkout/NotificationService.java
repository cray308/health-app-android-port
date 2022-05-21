package com.example.healthAppAndroid.homeTab.addWorkout;

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

import java.util.Locale;

public abstract class NotificationService {
    static abstract class Type {
        static final int circuit = 0;
        static final int exercise = 1;
    }

    private static final String ChannelId = "HealthAppAndroid_channel";
    private static final String[] filters = {null, null};
    private static final BroadcastReceiver[] receivers = {null, null};
    private static final PendingIntent[] intents = {null, null};
    private static final int[][] activeIds = {{0, 0}, {0, 0}};
    private static int noteId = 1;
    private static int filterId = 1;

    public static void setupAppNotifications(Context context) {
        NotificationChannel channel = new NotificationChannel(
          ChannelId, "workout_timer_channel", NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(context.getString(R.string.channelDescription));
        channel.setShowBadge(true);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        Object nm = context.getSystemService(Context.NOTIFICATION_SERVICE);
        ((NotificationManager)nm).createNotificationChannel(channel);
    }

    private static BroadcastReceiver createReceiver(int type) {
        return new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (type == Type.circuit && intents[Type.exercise] != null) {
                    AlarmManager am = ((AlarmManager)context.getSystemService(Context.ALARM_SERVICE));
                    am.cancel(intents[Type.exercise]);
                    intents[Type.exercise] = null;
                }
                intents[type] = null;
                Object nm = context.getSystemService(Context.NOTIFICATION_SERVICE);
                ((NotificationManager)nm).notify(noteId++, createNotification(context));
                ((WorkoutActivity)context).receivedNote(activeIds[type][0], activeIds[type][1], type);
            }

            private Notification createNotification(Context context) {
                int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
                Intent intent = new Intent(context, WorkoutActivity.class).setAction("timerAction");
                return new NotificationCompat.Builder(context, ChannelId)
                  .setAutoCancel(true)
                  .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                  .setSmallIcon(R.drawable.ic_notif)
                  .setColor(ContextCompat.getColor(context, R.color.notification))
                  .setPriority(NotificationManager.IMPORTANCE_HIGH)
                  .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                  .setContentIntent(PendingIntent.getActivity(context, 0, intent, flags))
                  .setContentTitle(context.getString(R.string.workoutNotificationTitle))
                  .setContentText(context.getResources().getStringArray(R.array.notifications)[type])
                  .build();
            }
        };
    }

    static void setup(Context context) {
        String[] baseFilters = {"HAA.workoutTimer.circuit", "HAA.workoutTimer.exercise"};
        int id = filterId++;
        for (int i = 0; i < 2; ++i) {
            receivers[i] = createReceiver(i);
            filters[i] = String.format(Locale.US, "%s.%d", baseFilters[i], id);
            context.registerReceiver(receivers[i], new IntentFilter(filters[i]));
        }
    }

    static void cleanup(Context context) {
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        for (int i = 0; i < 2; ++i) {
            if (intents[i] != null) {
                am.cancel(intents[i]);
                intents[i] = null;
            }
            context.unregisterReceiver(receivers[i]);
            receivers[i] = null;
        }
        ((NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
    }

    static void scheduleAlarm(Context context, long secondsFromNow, int type, int section, int row) {
        secondsFromNow = SystemClock.elapsedRealtime() + (secondsFromNow * 1000);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, new Intent(filters[type]),
                                                           PendingIntent.FLAG_IMMUTABLE);
        intents[type] = pIntent;
        activeIds[type][0] = section;
        activeIds[type][1] = row;
        AlarmManager am = ((AlarmManager)context.getSystemService(Context.ALARM_SERVICE));
        am.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, secondsFromNow, pIntent);
    }
}
