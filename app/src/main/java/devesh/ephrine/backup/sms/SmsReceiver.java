package devesh.ephrine.backup.sms;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class SmsReceiver extends BroadcastReceiver {
    static String TAG = "SmsReceived ";
    Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
//---get the SMS message passed in---
        Bundle bundle = intent.getExtras();
        mContext = context;
        SmsMessage[] msgs = null;
        String str = "";
        if (bundle != null) {
            String address = null;
            String msg = null;
            //---retrieve the SMS message received---
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];
            for (int i = 0; i < msgs.length; i++) {
                msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                str += "SMS from " + msgs[i].getOriginatingAddress();
                str += " :";
                str += msgs[i].getMessageBody().toString();
                str += "n";

                address = msgs[i].getOriginatingAddress();
                msg = msgs[i].getMessageBody().toString();

            }
            //---display the new SMS message---
            //    Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onReceive: SMS Received \n" + str + "\n--------");
            long smsReceiveTime = System.currentTimeMillis();
            saveSms(address, msg, "0", String.valueOf(smsReceiveTime), "inbox");
//CreateNotification(Function.getContactbyPhoneNumber(mContext,address),msg);

        }

    }


    public boolean saveSms(String phoneNumber, String message, String readState, String time, String folderName) {
        boolean ret = false;
        try {
            ContentValues values = new ContentValues();
            values.put("address", phoneNumber);
            values.put("body", message);
            values.put("read", readState); //"0" for have not read sms and "1" for have read sms
            values.put("date", time);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Uri uri = Telephony.Sms.Sent.CONTENT_URI;
                if (folderName.equals("inbox")) {
                    uri = Telephony.Sms.Inbox.CONTENT_URI;
                }
                mContext.getContentResolver().insert(uri, values);
            } else {
                mContext.getContentResolver().insert(Uri.parse("content://sms/" + folderName), values);
            }

            ret = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            ret = false;
        }
        return ret;
    }

    void CreateNotification(String title, String message) {


        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(mContext, StartActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, "02")
                .setSmallIcon(R.drawable.logo_notification_mono)
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setColor(mContext.getResources().getColor(R.color.colorAccent))
                .setContentText(message)
                .setSound(null, AudioManager.STREAM_NOTIFICATION)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String CHANNEL_ID = "002";
            CharSequence name = mContext.getString(R.string.app_name);
            String Description = "New Message";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel mChannel;
            mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableVibration(true);

            notificationManager.createNotificationChannel(mChannel);
        }
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(02, builder.build());


    }
    /*
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
     //   throw new UnsupportedOperationException("Not yet implemented");
    }
    */

}
