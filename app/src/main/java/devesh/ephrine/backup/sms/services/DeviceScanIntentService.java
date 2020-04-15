package devesh.ephrine.backup.sms.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;
import com.lifeofcoding.cacheutlislibrary.CacheUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import devesh.ephrine.backup.sms.Function;
import devesh.ephrine.backup.sms.MapComparator;
import devesh.ephrine.backup.sms.R;
import devesh.ephrine.backup.sms.payment.CheckSubscriptionService;
import io.fabric.sdk.android.Fabric;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DeviceScanIntentService extends JobIntentService {
    public static final int JOB_ID = 1;
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "devesh.ephrine.backup.sms.services.action.FOO";
    // private static final Context ACTION_CONTEXT;
    private static final String ACTION_BAZ = "devesh.ephrine.backup.sms.services.action.BAZ";
    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "devesh.ephrine.backup.sms.services.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "devesh.ephrine.backup.sms.services.extra.PARAM2";
    public static boolean isIntentServiceRunning = false;
    final String TAG = "DeviceScanService";


    ArrayList<HashMap<String, String>> DeviceSMS = new ArrayList<>();
    PowerManager.WakeLock wakeLock;
    SharedPreferences sharedPrefAppGeneral;
    Trace myTrace;

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, DeviceScanIntentService.class, JOB_ID, work);
    }

    @Override
    public void onCreate() {

        myTrace = FirebasePerformance.getInstance().newTrace("SyncIntentService");
        myTrace.start();

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                TAG + "::MyWakelockTag");
        wakeLock.acquire();


        sharedPrefAppGeneral = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPrefAppGeneral.edit();
        editor.putString(getString(R.string.BG_Task_Status), "1").apply();

        Fabric.with(getApplicationContext(), new Crashlytics());
        try {
            //Subscription Check
            Intent subscriptionCheck = new Intent(this, CheckSubscriptionService.class);
            startService(subscriptionCheck);

        } catch (Exception e) {
            Log.e(TAG, "onCreate: #654867 ", e);
            Crashlytics.logException(e);
            CacheUtils.writeFile(getString(R.string.BG_Task_Status), "0");

        }

        Log.d(TAG, "onCreate: SyncIntentService() #9086");
       /* if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O){
            startMyOwnForeground();
            Log.d(TAG, "onCreate: SyncIntentService() startMyOwnForeground #908600");

        }
        else{
            startForeground(1, new Notification());
            Log.d(TAG, "onCreate: SyncIntentService() startForeground #9086");

        }*/


        super.onCreate();
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        // your code

        Fabric.with(getApplicationContext(), new Crashlytics());

        Log.d(TAG, "onCreate: SyncIntentService() #9086");


        Fabric.with(getApplicationContext(), new Crashlytics());
        //    startSync();
        LoadSms ss = new LoadSms();
        ss.execute();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        wakeLock.release();
        SharedPreferences.Editor editor = sharedPrefAppGeneral.edit();
        editor.putString(getString(R.string.BG_Task_Status), "0").apply();

        try{
            myTrace.stop();
        }catch (Exception e){
            Log.e(TAG, "onDestroy: ERROR #564 ",e );
        }


    }

    class LoadSms extends AsyncTask<String, Void, String> {
        final String TAG = "LoadSms | ";
        ArrayList<HashMap<String, String>> smsList = new ArrayList<HashMap<String, String>>();
        ArrayList<HashMap<String, String>> tmpList = new ArrayList<HashMap<String, String>>();
       /* Context mContext;

        public LoadSms(Context context) {
            CacheUtils.configureCache(context);

            mContext = context;
        }
        */

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            smsList.clear();
            //   loadingCircle.setVisibility(View.VISIBLE);
            SharedPreferences.Editor editor = sharedPrefAppGeneral.edit();
            editor.putString(getString(R.string.BG_Task_Status), "1").apply();


        }

        protected String doInBackground(String... args) {
            SharedPreferences.Editor editor = sharedPrefAppGeneral.edit();
            editor.putString(getString(R.string.BG_Task_Status), "1").apply();

            String xml = "";
            try {
                Uri uriInbox = Uri.parse("content://sms/inbox");

                Cursor inbox = getContentResolver().query(uriInbox, null, "address IS NOT NULL) GROUP BY (thread_id", null, null); // 2nd null = "address IS NOT NULL) GROUP BY (address"
                Uri uriSent = Uri.parse("content://sms/sent");
                Cursor sent = getContentResolver().query(uriSent, null, "address IS NOT NULL) GROUP BY (thread_id", null, null); // 2nd null = "address IS NOT NULL) GROUP BY (address"
                Cursor c = new MergeCursor(new Cursor[]{inbox, sent}); // Attaching inbox and sent sms


                if (c.moveToFirst()) {
                    for (int i = 0; i < c.getCount(); i++) {

                        String name = null;
                        String phone = "";
                        String _id = c.getString(c.getColumnIndexOrThrow("_id"));
                        String thread_id = c.getString(c.getColumnIndexOrThrow("thread_id"));
                        String msg = c.getString(c.getColumnIndexOrThrow("body"));
                        String type = c.getString(c.getColumnIndexOrThrow("type"));
                        String timestamp = c.getString(c.getColumnIndexOrThrow("date"));
                        phone = c.getString(c.getColumnIndexOrThrow("address"));
                        String read = c.getString(c.getColumnIndexOrThrow("read"));

                        name = CacheUtils.readFile(thread_id);
                        if (name == null) {
                            name = Function.getContactbyPhoneNumber(getApplicationContext(), c.getString(c.getColumnIndexOrThrow("address")));
                            CacheUtils.writeFile(thread_id, name);
                        }

  /*                          Sms u=new Sms();
                            //u.uid= 1;

                            u.ID=_id;
                            u.KEY_THREAD_ID=thread_id;
                            u.KEY_NAME=name;
                            u.KEY_PHONE=phone;
                            u.KEY_MSG=msg;
                            u.KEY_TYPE=type;
                            u.KEY_TIMESTAMP=timestamp;
                            u.KEY_TIME=Function.converToTime(timestamp);
                            u.KEY_READ=read;

                            db.userDao().insertAllr(u);
*/

                        smsList.add(Function.mappingInbox(_id, thread_id, name, phone, msg, type, timestamp, Function.converToTime(timestamp), read));
                        DeviceSMS.add(Function.mappingInbox(_id, thread_id, name, phone, msg, type, timestamp, Function.converToTime(timestamp), read));
                        c.moveToNext();

  /*                      Log.d(TAG, "-------\ndoInBackground: \n" + name +
                                "\n" + phone + "\n"
                                + _id + "\n"
                                + thread_id + "\n"
                                + msg + "\n"
                                + type + "\n"
                                + timestamp + "\n"
                                + phone);
*/
                    }

                }
                c.close();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Crashlytics.logException(e);

            }

            try {
                Function.createCachedFile(DeviceScanIntentService.this, "orgsms", smsList);
                Function.createCachedFile(DeviceScanIntentService.this, getString(R.string.file_device_sms), DeviceSMS);

                Log.d(TAG, "doInBackground: createCachedFile ORG SMS CREATED");
            } catch (Exception e) {
                Crashlytics.logException(e);

            }

            Collections.sort(smsList, new MapComparator(Function.KEY_TIMESTAMP, "dsc")); // Arranging sms by timestamp decending
            ArrayList<HashMap<String, String>> purified = Function.removeDuplicates(smsList); // Removing duplicates from inbox & sent
            smsList.clear();
            smsList.addAll(purified);

            // Updating cache data
            try {
                Function.createCachedFile(DeviceScanIntentService.this, "smsapp", smsList);
                Log.d(TAG, "doInBackground: createCachedFile CREATED");
            } catch (Exception e) {
                Crashlytics.logException(e);

            }
            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {

            wakeLock.release();

            SharedPreferences.Editor editor = sharedPrefAppGeneral.edit();
            editor.putString(getString(R.string.BG_Task_Status), "0").apply();

            Intent intent = new Intent(getApplicationContext(), CloudSMS2DBService.class);

            //  String message = editText.getText().toString();
            //intent.putExtra(EXTRA_MESSAGE, message);
            startService(intent);
            try{
                myTrace.stop();
            }catch (Exception e){
                Log.e(TAG, "onDestroy: ERROR #564 ",e );
            }



        }
    }

    /**
     File directory = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Images");
     localFile = new File(directory, filename);
     * */


}

