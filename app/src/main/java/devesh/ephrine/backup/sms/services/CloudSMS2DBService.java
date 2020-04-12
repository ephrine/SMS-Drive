package devesh.ephrine.backup.sms.services;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.room.Room;

import com.crashlytics.android.Crashlytics;
import com.lifeofcoding.cacheutlislibrary.CacheUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import devesh.ephrine.backup.sms.Function;
import devesh.ephrine.backup.sms.R;
import devesh.ephrine.backup.sms.room.AppDatabase;
import devesh.ephrine.backup.sms.room.Sms;

public class CloudSMS2DBService extends Service {
    ArrayList<HashMap<String, String>> CloudSms = new ArrayList<>();
    String TAG = "CloudSMS2DBService :";
    SharedPreferences sharedPrefAppGeneral;

    /**
     * indicates how to behave if the service is killed
     */
    int mStartMode;
    /**
     * interface for clients that bind
     */
    IBinder mBinder;
    /**
     * indicates whether onRebind should be used
     */
    boolean mAllowRebind;
    AppDatabase db;
    PowerManager.WakeLock wakeLock;

    /**
     * interface for clients that bind
     */

    public CloudSMS2DBService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;

    }

    /**
     * The service is starting, due to a call to startService()
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    /**
     * Called when The service is no longer used and is being destroyed
     */
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        db.close();
        wakeLock.release();

    }

    @Override
    public void onCreate() {
        super.onCreate();

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                TAG + "::MyWakelockTag");
        wakeLock.acquire();

        if (getCurProcessName(getApplicationContext()).equals(getPackageName())) {
            // initialize the database
            db = Room.databaseBuilder(getApplicationContext(),
                    AppDatabase.class, getString(R.string.DATABASE_CLOUD_SMS_DB))
                    //.setJournalMode(RoomDatabase.JournalMode.AUTOMATIC)

                    .build();
        }
        sharedPrefAppGeneral = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPrefAppGeneral.edit();
        editor.putString(getString(R.string.BG_Task_Status), "1").apply();


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

    class AddSmsDB extends AsyncTask<String, Void, String> {
        final String TAG = "CloudSMS2DBService ";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "onPreExecute");
            CacheUtils.writeFile(getString(R.string.BG_Task_Status), "1");

        }

        protected String doInBackground(String... args) {
            SharedPreferences.Editor editor = sharedPrefAppGeneral.edit();
            editor.putString(getString(R.string.BG_Task_Status), "1").apply();


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

            for (int j = 1; j <= CloudSms.size(); j++) {

                progress = j / t * 100;
                Log.d(TAG, "AddSmsDB | doInBackground: PROGRESS: " + progress + "% \n j=" + j + "/" + t);
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
                    Log.e(TAG, "doInBackground: #ERROR " + e);
                    Crashlytics.logException(e);


                }


            }
            db.userDao().insertAllr2(slist);

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
            wakeLock.release();
            db.close();

            SharedPreferences.Editor editor = sharedPrefAppGeneral.edit();
            editor.putString(getString(R.string.BG_Task_Status), "0").apply();


        }
    }


}
