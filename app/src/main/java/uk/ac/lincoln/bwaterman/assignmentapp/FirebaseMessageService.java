package uk.ac.lincoln.bwaterman.assignmentapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessageService extends FirebaseMessagingService {

    boolean openFavourite;

    public FirebaseMessageService() {
    }

    //Handle message received
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Handled whether in foreground or background
        if (remoteMessage.getData().size() > 0) {

        }

        // Only received when app is open, when in background notification goes to notification bar
        if (remoteMessage.getNotification() != null) {

        }

        //Display the notification to the user
        sendNotification(remoteMessage);
    }

    //Display the notification
    private void sendNotification(RemoteMessage message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.twitch_logo_icon)
                .setContentTitle(message.getNotification().getTitle())
                .setContentText(message.getNotification().getBody())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }

}
