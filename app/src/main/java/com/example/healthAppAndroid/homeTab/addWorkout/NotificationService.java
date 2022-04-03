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
        static final byte Exercise = 0;
        static final byte Circuit = 1;
    }

    private static final String ChannelId = "HealthAppAndroid_channel";
    private static String[] messages;
    private static String contentTitle;
    private static final String[] baseFilters = {
      "com.healthAppAndroid.workoutTimer.exercise", "com.healthAppAndroid.workoutTimer.circuit"
    };
    private static final String[] filters = {null, null};
    private static NotificationManager notificationMgr;
    private static AlarmManager alarmMgr;
    private static final BroadcastReceiver[] receivers = {null, null};
    private static PendingIntent circuitIntent;
    private static PendingIntent exerciseIntent;
    private static final int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
    private static final int defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
    private static int color;
    private static int groupId = 0;
    private static int exerciseGroup = 0;
    private static int exerciseIndex = 0;
    private static short identifier = 1;
    private static short filterId = 1;

    public static void setupAppNotifications(Context c) {
        NotificationManager manager =
          (NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(
          ChannelId, "workout_timer_channel", NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(c.getString(R.string.channelDescription));
        channel.setShowBadge(true);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        manager.createNotificationChannel(channel);
    }

    static void init(Context c) {
        messages = c.getResources().getStringArray(R.array.notifications);
        contentTitle = c.getString(R.string.workoutNotificationTitle);
        color = ContextCompat.getColor(c, R.color.notification);
        notificationMgr = (NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
        alarmMgr = (AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
    }

    static void setup(Context outerContext) {
        receivers[Type.Exercise] = new BroadcastReceiver() {
            public void onReceive(Context c, Intent intent) {
                exerciseIntent = null;
                notificationMgr.notify(identifier++, createNotification(c, Type.Exercise));
                ((WorkoutActivity)c).finishedExercise(exerciseGroup, exerciseIndex);
            }
        };
        receivers[Type.Circuit] = new BroadcastReceiver() {
            public void onReceive(Context c, Intent intent) {
                if (exerciseIntent != null) {
                    alarmMgr.cancel(exerciseIntent);
                    exerciseIntent = null;
                }
                circuitIntent = null;
                notificationMgr.notify(identifier++, createNotification(c, Type.Circuit));
                ((WorkoutActivity)c).finishedGroup(groupId);
            }
        };
        short id = filterId++;
        for (int i = 0; i < 2; ++i) {
            filters[i] = String.format(Locale.US, "%s.%d", baseFilters[i], id);
            outerContext.registerReceiver(receivers[i], new IntentFilter(filters[i]));
        }
    }

    static void cleanup(Context c) {
        if (circuitIntent != null) {
            alarmMgr.cancel(circuitIntent);
            circuitIntent = null;
        }
        if (exerciseIntent != null) {
            alarmMgr.cancel(exerciseIntent);
            exerciseIntent = null;
        }
        for (int i = 0; i < 2; ++i) {
            c.unregisterReceiver(receivers[i]);
            receivers[i] = null;
        }
        notificationMgr.cancelAll();
    }

    static void scheduleAlarm(Context c, long secondsFromNow, byte type, int group, int index) {
        secondsFromNow = SystemClock.elapsedRealtime() + (secondsFromNow * 1000);
        PendingIntent pIntent = PendingIntent.getBroadcast(
          c, 0, new Intent(filters[type]), PendingIntent.FLAG_IMMUTABLE);
        if (type == Type.Circuit) {
            secondsFromNow += 1000;
            groupId = group;
            circuitIntent = pIntent;
        } else {
            exerciseGroup = group;
            exerciseIndex = index;
            exerciseIntent = pIntent;
        }
        alarmMgr.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, secondsFromNow, pIntent);
    }

    private static Notification createNotification(Context c, byte type) {
        Intent intent = new Intent(c, WorkoutActivity.class);
        intent.setAction("timerAction");
        return new NotificationCompat.Builder(c, ChannelId)
          .setAutoCancel(true)
          .setDefaults(defaults)
          .setSmallIcon(R.drawable.ic_notif)
          .setColor(color)
          .setPriority(NotificationManager.IMPORTANCE_HIGH)
          .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
          .setContentIntent(PendingIntent.getActivity(c, 0, intent, flags))
          .setContentTitle(contentTitle)
          .setContentText(messages[type]).build();
    }
}
