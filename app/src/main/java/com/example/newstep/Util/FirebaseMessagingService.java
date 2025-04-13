package com.example.newstep.Util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Vibrator;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.newstep.MainActivity;
import com.example.newstep.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    private NotificationManager notificationManager;

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
updateNewToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        Vibrator vibrator=(Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern={0,10,100,200};
        vibrator.vibrate(pattern,-1);
        NotificationCompat.Builder builder =new NotificationCompat.Builder(this,"Notification");
        Intent resultIntent=new Intent(this, MainActivity.class);
        PendingIntent pendingIntent;
        pendingIntent=PendingIntent.getActivity(this,1,resultIntent,PendingIntent.FLAG_MUTABLE);
        builder.setContentTitle(message.getNotification().getTitle());
        builder.setContentText(message.getNotification().getBody());
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(message.getNotification().getBody()));
        builder.setAutoCancel(true);
        builder.setSmallIcon(R.drawable.icon_group);
        builder.setVibrate(pattern);
        builder.setPriority(Notification.PRIORITY_MAX);

        notificationManager =(NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            String channelId="Notification";
            NotificationChannel channel=new NotificationChannel(
                    channelId,"Coding",NotificationManager.IMPORTANCE_HIGH
            );
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setVibrationPattern(pattern);
            channel.canBypassDnd();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                channel.canBubble();
            }

            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channelId);
        }
        notificationManager.notify(100,builder.build());
    }
public void updateNewToken(String token){
FirebaseUtil.allUserCollectionRef().document(FirebaseUtil.getCurrentUserId()).update("token",token)
        .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
}
}
