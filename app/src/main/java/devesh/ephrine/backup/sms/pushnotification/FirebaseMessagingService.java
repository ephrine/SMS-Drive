package devesh.ephrine.backup.sms.pushnotification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.RemoteMessage;
import com.lifeofcoding.cacheutlislibrary.CacheUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;

import devesh.ephrine.backup.sms.Function;
import devesh.ephrine.backup.sms.MainActivity;
import devesh.ephrine.backup.sms.R;
import devesh.ephrine.backup.sms.StartActivity;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    String TAG = "FB";
    LinkedHashSet<HashMap<String, String>> notificationsDataHash = new LinkedHashSet<>();

    public FirebaseMessagingService() {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        String url=null;
        String title=null;
        String desc=null;
        String time=null;

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            url = remoteMessage.getData().get(EpNotificationsConstants.EP_FCM_URL);
            title=remoteMessage.getData().get(EpNotificationsConstants.EP_FCM_TITLE);
            desc =remoteMessage.getData().get(EpNotificationsConstants.EP_FCM_DESC);

            time=String.valueOf(System.currentTimeMillis());
            Log.d(TAG, "onMessageReceived: GOT URL " + url);
            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
//                scheduleJob();
            } else {
                // Handle message within 10 seconds
                //              handleNow();
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            CreateNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.


        HashMap<String, String> data=new HashMap<>();
        data.put(EpNotificationsConstants.EP_FCM_URL,url);
        data.put(EpNotificationsConstants.EP_FCM_TITLE,title);
        data.put(EpNotificationsConstants.EP_FCM_DESC,desc);
        data.put("time",time);

        try {
            notificationsDataHash=(LinkedHashSet<HashMap<String, String>>) Function.readCachedFile(this, getString(R.string.FCM_Notifications_Data));
             } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "onMessageReceived: ERROR #5234 ", e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "onMessageReceived: ERROR #65 ",e );
        }
        notificationsDataHash.add(data);
        try {
            Function.createCachedNotificationFile(this,getString(R.string.FCM_Notifications_Data),notificationsDataHash);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "onMessageReceived: ERROR #2343 ",e );
        }


    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        //  sendRegistrationToServer(token);
    }

    void CreateNotification(String title, String message) {

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, StartActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "010")
                .setSmallIcon(R.drawable.app_logo)
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setColor(getResources().getColor(R.color.colorAccent))
                .setContentText(message)
                .setSound(null, AudioManager.STREAM_NOTIFICATION)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String CHANNEL_ID = "010";
            CharSequence name = "News & Updates";
            String Description = "Push Notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel mChannel;
            mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableVibration(true);

            notificationManager.createNotificationChannel(mChannel);
        }
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(010, builder.build());


    }

}
