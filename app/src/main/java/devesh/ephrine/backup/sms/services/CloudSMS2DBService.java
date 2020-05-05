package devesh.ephrine.backup.sms.services;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.room.Room;

import com.crashlytics.android.Crashlytics;
import com.lifeofcoding.cacheutlislibrary.CacheUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import devesh.ephrine.backup.sms.Function;
import devesh.ephrine.backup.sms.R;
import devesh.ephrine.backup.sms.room.AppDatabase;
import devesh.ephrine.backup.sms.room.Sms;
import io.fabric.sdk.android.Fabric;

public class CloudSMS2DBService extends JobIntentService {
    /*
        @Override
        public IBinder onBind(Intent intent) {
            // TODO: Return the communication channel to the service.
            return mBinder;

        }
        */
    public static final int JOB_ID = 3;
    ArrayList<HashMap<String, String>> CloudSms = new ArrayList<>();
    String TAG = "CloudSMS2DBService :";
    // SharedPreferences sharedPrefAppGeneral;
    /**
     * interface for clients that bind
     */
    //  IBinder mBinder;
    /**
     * indicates how to behave if the service is killed
     */
    int mStartMode;
    /**
     * indicates whether onRebind should be used
     */
    boolean mAllowRebind;
    AppDatabase db;
    PowerManager.WakeLock wakeLock;
    NotificationManagerCompat notificationManager;
    NotificationCompat.Builder nmbuilder;
    int PROGRESS_MAX = 100;

    /**
     * The service is starting, due to a call to startService()
     */
    /*@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
    */
    int PROGRESS_CURRENT = 0;
//    Trace myTrace;


    /**
     * interface for clients that bind
     */

    public CloudSMS2DBService() {
    }

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, CloudSMS2DBService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        // your code

        Fabric.with(getApplicationContext(), new Crashlytics());

        Log.d(TAG, "onCreate: SyncIntentService() #9086");


        //    startSync();

    }

    /**
     * Called when The service is no longer used and is being destroyed
     */
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        db.close();
        try {
            wakeLock.release();
        } catch (Exception e) {
            Log.e(TAG, "onDestroy: ERROR #8786 ", e);
        }

        try {
            // myTrace.stop();
        } catch (Exception e) {
            Log.e(TAG, "onDestroy: ERROR #564 ", e);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //   myTrace = FirebasePerformance.getInstance().newTrace("SyncIntentService");
        //   myTrace.start();

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                TAG + "::MyWakelockTag");
        wakeLock.acquire();

        //  sharedPrefAppGeneral = PreferenceManager.getDefaultSharedPreferences(this);

        if (getCurProcessName(getApplicationContext()).equals(getPackageName())) {
            // initialize the database
            db = Room.databaseBuilder(getApplicationContext(),
                    AppDatabase.class, getString(R.string.DATABASE_CLOUD_SMS_DB))
                    //.setJournalMode(RoomDatabase.JournalMode.AUTOMATIC)

                    .build();
        }
        //    SharedPreferences.Editor editor = sharedPrefAppGeneral.edit();
        //    editor.putString(getString(R.string.BG_Task_Status), "1").apply();
        setNotificationCloudRefresh();

        AddSmsDB asd = new AddSmsDB();
        asd.execute();


    }

    private String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return "";
    }

    void setNotificationCloudRefresh() {

        nmbuilder = new NotificationCompat.Builder(this, "002")
                .setSmallIcon(R.drawable.app_logo)
                .setContentTitle("Processing Messages from Cloud")
                .setContentText("")
                .setOngoing(true)
                .setProgress(PROGRESS_MAX, PROGRESS_CURRENT, true)

                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.FLAG_ONGOING_EVENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "General Tasks";
            String description = "Refresh Messages in background";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("002", name, importance);
            channel.setDescription(description);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

        }

        notificationManager = NotificationManagerCompat.from(this);
        nmbuilder.setContentText("");

// notificationId is a unique int for each notification that you must define
        notificationManager.notify(002, nmbuilder.build());


    }

    class AddSmsDB extends AsyncTask<String, Void, String> {
        final String TAG = "CloudSMS2DBService ";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "onPreExecute");
            CacheUtils.writeFile(getString(R.string.BG_Task_Status), "1");


        }

        protected String doInBackground(String... args) {
            //  SharedPreferences.Editor editor = sharedPrefAppGeneral.edit();
            //  editor.putString(getString(R.string.BG_Task_Status), "1").apply();


            String xml = "";
            ArrayList<HashMap<String, String>> al = null;
            try {
                CloudSms = (ArrayList<HashMap<String, String>>) Function.readCachedFile(CloudSMS2DBService.this, getString(R.string.file_cloud_sms));

            } catch (Exception e) {
                Log.e(TAG, "doInBackground: #6567 ", e);
                Crashlytics.logException(e);
                //stopSelf();
            }


            double t = CloudSms.size();

            double progress;
            List<Sms> slist = new ArrayList<>();
            nmbuilder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
            notificationManager.notify(002, nmbuilder.build());

            try {
                for (int j = 0; j <= CloudSms.size(); j++) {

                    progress = j / t * 100;
                    String prg = new DecimalFormat("##.##").format(progress);
                    Log.d(TAG, "AddSmsDB | doInBackground: PROGRESS: " + progress + "% \n j=" + j + "/" + t);
                    PROGRESS_CURRENT = (int) progress;
                    nmbuilder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false).setContentText("" + prg + "%");
                    notificationManager.notify(002, nmbuilder.build());
                    try {

                        Sms u = new Sms();
                        //u.uid= 1;
                        u.ID = CloudSms.get(j).get(Function._ID);
                        u.KEY_THREAD_ID = CloudSms.get(j).get(Function.KEY_THREAD_ID);
                        u.KEY_NAME = CloudSms.get(j).get(Function.KEY_NAME);
                        u.KEY_PHONE = CloudSms.get(j).get(Function.KEY_PHONE);
                        u.KEY_MSG = CloudSms.get(j).get(Function.KEY_MSG);
                        u.KEY_TYPE = CloudSms.get(j).get(Function.KEY_TYPE);
                        u.KEY_TIMESTAMP = CloudSms.get(j).get(Function.KEY_TIMESTAMP);
                        u.KEY_TIME = CloudSms.get(j).get(Function.KEY_TIME);
                        u.KEY_READ = CloudSms.get(j).get(Function.KEY_READ);

                        slist.add(u);
                        //db.close();

                    } catch (Exception e) {
                        Log.e(TAG, "doInBackground: ERROR #04732 " + e);
                        Crashlytics.logException(e);
                        notificationManager.cancel(002);

                    }

                }

            } catch (Exception e) {
                Log.e(TAG, "doInBackground: #45642 ", e);
                Crashlytics.logException(e);
                stopSelf();
            }

            try {
                db.userDao().insertAllr2(slist);
                Log.d(TAG, "doInBackground: Added into DB SUCCESS");
            } catch (Exception e) {
                Log.e(TAG, "doInBackground: ERROR #76542 ", e);
            }

  /*          db.userDao().insertAll(u);
            db.userDao().getAll();
            List<Sms> ll=db.userDao().getAll();
            Log.d(TAG, "onCreate: "+ll.get(0).KEY_MSG);
*/
            return "Done";
        }

        @Override
        protected void onPostExecute(String xml) {
            Log.d(TAG, "onPostExecute");
            try {
                wakeLock.release();
            } catch (Exception e) {
                Log.e(TAG, "onPostExecute: ERROR #35271 ", e);
            }
            if (db != null) {
                db.close();
            }


            //      SharedPreferences.Editor editor = sharedPrefAppGeneral.edit();
            //    editor.putString(getString(R.string.BG_Task_Status), "0").apply();
            CloudSms.clear();
            notificationManager.cancel(002);
            try {
                //     myTrace.stop();
            } catch (Exception e) {
                Log.e(TAG, "onDestroy: ERROR #564 ", e);
            }
            stopSelf();
        }
    }


}
