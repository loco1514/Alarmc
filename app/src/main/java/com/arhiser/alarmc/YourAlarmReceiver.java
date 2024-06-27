package com.arhiser.alarmc;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class YourAlarmReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "AlarmChannel";
    private static final int NOTIFICATION_ID = 1;
    private static final String CANCEL_ACTION = "com.arhiser.alarmc.ACTION_CANCEL_ALARM";

    private static MediaPlayer mediaPlayer; // Ссылка на MediaPlayer

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action != null && action.equals(CANCEL_ACTION)) {
            // Если это действие отмены будильника
            cancelAlarm(context);
        } else {
            // Если это срабатывание будильника
            String extraData = intent.getStringExtra("YOUR_EXTRA_DATA");
            playRingtone(context);
            showNotification(context, "Alarm Triggered", extraData);
        }
    }

    private void playRingtone(Context context) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }
    }

    private void stopRingtone() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void showNotification(Context context, String title, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            createNotificationChannel(notificationManager);

            Intent cancelIntent = new Intent(context, YourAlarmReceiver.class);
            cancelIntent.setAction(CANCEL_ACTION);
            PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(context, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder;
            builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setSmallIcon(R.drawable.your_notification_icon)
                    .setAutoCancel(true)
                    .addAction(R.drawable.your_notification_icon, "Отменить", cancelPendingIntent);

            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    private void createNotificationChannel(NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Alarm Channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void cancelAlarm(Context context) {
        stopRingtone(); // Остановить звуковой сигнал
        cancelNotification(context);
    }

    private void cancelNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_ID);
        }
    }
}