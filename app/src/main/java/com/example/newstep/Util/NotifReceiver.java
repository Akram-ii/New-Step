package com.example.newstep.Util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import com.example.newstep.MainActivity;
import com.example.newstep.R;

public class NotifReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SQLiteDatabase database = context.openOrCreateDatabase("reminderDB", Context.MODE_PRIVATE, null);
        Cursor cursor = database.rawQuery("SELECT * FROM Reminder", null);

        String title = "Stay on track   ";
        String description = "Don't forget to Log your day!";

        if (cursor.moveToFirst()) {
            title = cursor.getString(1);
            description = cursor.getString(2);
        }
        cursor.close();

        showNotification(context, title, description);
    }

    private void showNotification(Context context, String title, String description) {
        String channelId = "reminder_channel";
        String channelName = "Daily Reminder";

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 500, 500});
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(description)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[]{0, 500, 500, 500});

        notificationManager.notify(1, builder.build());
    }
}