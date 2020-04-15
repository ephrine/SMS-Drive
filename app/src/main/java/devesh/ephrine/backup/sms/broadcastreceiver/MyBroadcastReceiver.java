package devesh.ephrine.backup.sms.broadcastreceiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.crashlytics.android.Crashlytics;

import devesh.ephrine.backup.sms.services.DeviceScanIntentService;
import io.fabric.sdk.android.Fabric;

import static android.content.Context.ALARM_SERVICE;

public class MyBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "MyBroadcastReceiver";
    BroadcastReceiver br = new MyBroadcastReceiver();
    int requestId = 248;

    @Override
    public void onReceive(Context context, Intent intent) {
       /*   StringBuilder sb = new StringBuilder();
      sb.append("Action: " + intent.getAction() + "\n");
        sb.append("URI: " + intent.toUri(Intent.URI_INTENT_SCHEME).toString() + "\n");
        String log = sb.toString();
        Log.d(TAG, log);
        */
        //Toast.makeText(context, log, Toast.LENGTH_LONG).show();
        Fabric.with(context, new Crashlytics());

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Set the alarm here.


            Intent intent1 = new Intent(context, DeviceScanIntentService.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, 34589767, intent1, 0);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HOUR,
                    AlarmManager.INTERVAL_HOUR, pendingIntent);


        }


    }
}
