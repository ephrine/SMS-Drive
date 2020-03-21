package devesh.ephrine.backup.sms.services;

import android.Manifest;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lifeofcoding.cacheutlislibrary.CacheUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import devesh.ephrine.backup.sms.Function;
import devesh.ephrine.backup.sms.R;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class SyncIntentService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "devesh.ephrine.backup.sms.services.action.FOO";
    private static final String ACTION_BAZ = "devesh.ephrine.backup.sms.services.action.BAZ";
   // private static final Context ACTION_CONTEXT;

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "devesh.ephrine.backup.sms.services.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "devesh.ephrine.backup.sms.services.extra.PARAM2";

    final String TAG = "SyncIntentService";
    final boolean isDefaultApp = true;
    //  final String DBRoot = "SMSDrive/";
    public HashMap<String, ArrayList<HashMap<String, String>>> iThread;
    //   public HashMap<String, Object> iThread;
    Context mContext;
    boolean isFinished;
    String UserUID;
    DatabaseReference SMSBackupDB;
    DatabaseReference SMSDB;
    FirebaseDatabase database;
    FirebaseUser user;
    SharedPreferences sharedPrefAutoBackup;
    boolean SMSAutoBackup;
    boolean isSubscribed;
    ArrayList<HashMap<String, String>> smsList = new ArrayList<>();
    ArrayList<HashMap<String, String>> customList = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> tmpList = new ArrayList<HashMap<String, String>>();


    String name;


    public SyncIntentService() {
        super("SyncIntentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, SyncIntentService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, SyncIntentService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }
    public static boolean isIntentServiceRunning = false;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: SyncIntentService() #9086");
  /*      if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O){
            startMyOwnForeground();
            Log.d(TAG, "onCreate: SyncIntentService() startMyOwnForeground #9086");

        }
        else{
            startForeground(1, new Notification());
            Log.d(TAG, "onCreate: SyncIntentService() startForeground #9086");

        }
*/
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent: SyncIntentService() #9086");
        if(!isIntentServiceRunning) {
            isIntentServiceRunning = true;
        }
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }

        }

        mContext=getApplicationContext();
        iThread = new HashMap<>();
        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();

        isSubscribed = true;
        sharedPrefAutoBackup = PreferenceManager.getDefaultSharedPreferences(mContext /* Activity context */);
        SMSAutoBackup = sharedPrefAutoBackup.getBoolean(mContext.getResources().getString(R.string.settings_sync), false);

        isFinished = false;
        //  getSMS();
        if (isDefaultApp) {

            if (ContextCompat.checkSelfPermission(mContext,
                    Manifest.permission.READ_SMS)
                    == PackageManager.PERMISSION_GRANTED) {
                if (user != null && isSubscribed) {
                    UserUID = user.getPhoneNumber().replace("+", "x");
                    //Download Full Backup First to Prevent DataLoss
                    SMSBackupDB = database.getReference("/users/" + UserUID + "/sms/backup");
                    //SMSScanDevice();

                    try {

                        tmpList.clear();
                        tmpList = (ArrayList<HashMap<String, String>>) Function.readCachedFile(mContext, "orgsms");

                    } catch (Exception e) {
                    }


                    SMSBackupDB.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // This method is called once with the initial value and again
                            // whenever data at this location is updated.
                            //String value = dataSnapshot.getValue(String.class);
                            // Log.d(TAG, "Value is: " + value);

                            if (dataSnapshot.exists() && isSubscribed) {

                                long total = dataSnapshot.getChildrenCount();
                                long i;
                                i = 0;
                                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                    // TODO: handle the post
                                    i = i + 1;


                                    String threadName = postSnapshot.getKey();
                                    Log.d(TAG, "onDataChange: threadName: ");

                               //     GetThread(postSnapshot, threadName);

                                    Log.d(TAG, "GetThread: DS :  DS.getChildren().toString()");

                                    Log.d(TAG, "onDataChange: msg:");
DataSnapshot DS=postSnapshot;
//            String MsgTime = DS.child("date").getValue().toString();
                                    String phone = DS.child(Function.KEY_PHONE).getValue(String.class);

                                    String name = DS.child(Function.KEY_NAME).getValue(String.class);
                                    String _id = DS.child(Function._ID).getValue(String.class);
                                    String thread_id = DS.child(Function.KEY_THREAD_ID).getValue(String.class);
                                    String msg = DS.child(Function.KEY_MSG).getValue(String.class);
                                    String type = DS.child(Function.KEY_TYPE).getValue(String.class);
                                    String timestamp = DS.child(Function.KEY_TIMESTAMP).getValue(String.class);
                                    String time = DS.child(Function.KEY_TIME).getValue(String.class);
                                    //    Date date = new Date(Long.parseLong(MsgTime));
                                    //  String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);

                                    HashMap<String, String> SMS = new HashMap<>();
                                    SMS.put(Function._ID, _id);
                                    SMS.put(Function.KEY_THREAD_ID, thread_id);
                                    SMS.put(Function.KEY_PHONE, phone);
                                    SMS.put(Function.KEY_MSG, msg);
                                    SMS.put(Function.KEY_TYPE, type);
                                    SMS.put(Function.KEY_TIMESTAMP, timestamp);
                                    SMS.put(Function.KEY_TIME, time);
                                    SMS.put(Function.KEY_NAME, name);


                                    smsList.add(SMS);

                                    if (iThread.containsKey(threadName)) {
                                        iThread.get(threadName).add(SMS);
                                    } else {
                                        ArrayList<HashMap<String, String>> temp1 = new ArrayList<>();
                                        temp1.add(SMS);
                                        iThread.put(threadName, temp1);
                                    }

                                   /* for (DataSnapshot DS : postSnapshot.getChildren()) {


                                    }*/



                                    Log.d(TAG, "i:" + i + "\n Total:" + total);
                                    if (i == total) {
                                        sms1();
                                    }

                                }

                            } else {
                                if (isSubscribed) {
                                    sms1();
                                }
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                            Log.w(TAG, "Failed to read value.", error.toException());
                        }
                    });


                }

            }

        }



    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void syncNow() {


        // TODO: Handle action Foo
        isFinished = false;
        //  getSMS();
        if (isDefaultApp) {

            if (ContextCompat.checkSelfPermission(mContext,
                    Manifest.permission.READ_SMS)
                    == PackageManager.PERMISSION_GRANTED) {
                if (user != null && isSubscribed) {
                    UserUID = user.getPhoneNumber().replace("+", "x");
                    //Download Full Backup First to Prevent DataLoss
                    SMSBackupDB = database.getReference("/users/" + UserUID + "/sms/backup");
                    //SMSScanDevice();

                    try {

                        tmpList.clear();
                        tmpList = (ArrayList<HashMap<String, String>>) Function.readCachedFile(mContext, "orgsms");

                    } catch (Exception e) {
                    }


                    SMSBackupDB.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // This method is called once with the initial value and again
                            // whenever data at this location is updated.
                            //String value = dataSnapshot.getValue(String.class);
                            // Log.d(TAG, "Value is: " + value);

                            if (dataSnapshot.exists() && isSubscribed) {

                                long total = dataSnapshot.getChildrenCount();
                                long i;
                                i = 0;
                                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                    // TODO: handle the post
                                    i = i + 1;


                                    String threadName = postSnapshot.getKey();
                                    Log.d(TAG, "onDataChange: threadName: ");

                                    GetThread(postSnapshot, threadName);
                                    Log.d(TAG, "i:" + i + "\n Total:" + total);
                                    if (i == total) {
                                        sms1();
                                    }

                                }

                            } else {
                                if (isSubscribed) {
                                    sms1();
                                }
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                            Log.w(TAG, "Failed to read value.", error.toException());
                        }
                    });


                }

            }

        }

        //    new SmsSyncTask().execute("url1", "url2", "url3");
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }

    void GetThread(final DataSnapshot postSnapshot, final String threadName) {

        new Thread(new Runnable() {
            public void run() {
                // a potentially time consuming task
                for (DataSnapshot DS : postSnapshot.getChildren()) {

                    Log.d(TAG, "GetThread: DS :  DS.getChildren().toString()");

                    //   String msg = DS.getKey();
                    // String MsgBody = DS.child("body").getValue().toString();

                    //---

  /*
            //String formattedDate = new SimpleDateFormat("MM/dd/yyyy").format(date);
            String MsgText = DS.child("body").getValue().toString();
            String folder = DS.child("folder").getValue().toString();
          SMS.put("msg", MsgText);
            SMS.put("time", formattedDate);
            SMS.put("folder", folder);
*/
                    //  Log.d(TAG, "onDataChange: msg:" + msg + "\nMSG: " + MsgBody);
                    Log.d(TAG, "onDataChange: msg:");

//            String MsgTime = DS.child("date").getValue().toString();
                    String phone = DS.child(Function.KEY_PHONE).getValue(String.class);

                    String name = DS.child(Function.KEY_NAME).getValue(String.class);
                    String _id = DS.child(Function._ID).getValue(String.class);
                    String thread_id = DS.child(Function.KEY_THREAD_ID).getValue(String.class);
                    String msg = DS.child(Function.KEY_MSG).getValue(String.class);
                    String type = DS.child(Function.KEY_TYPE).getValue(String.class);
                    String timestamp = DS.child(Function.KEY_TIMESTAMP).getValue(String.class);
                    String time = DS.child(Function.KEY_TIME).getValue(String.class);


                    //    Date date = new Date(Long.parseLong(MsgTime));
                    //  String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);

                    HashMap<String, String> SMS = new HashMap<>();
                    SMS.put(Function._ID, _id);
                    SMS.put(Function.KEY_THREAD_ID, thread_id);
                    SMS.put(Function.KEY_PHONE, phone);
                    SMS.put(Function.KEY_MSG, msg);
                    SMS.put(Function.KEY_TYPE, type);
                    SMS.put(Function.KEY_TIMESTAMP, timestamp);
                    SMS.put(Function.KEY_TIME, time);
                    SMS.put(Function.KEY_NAME, name);


                    smsList.add(SMS);

                    if (iThread.containsKey(threadName)) {
                        iThread.get(threadName).add(SMS);
                    } else {
                        ArrayList<HashMap<String, String>> temp1 = new ArrayList<>();
                        temp1.add(SMS);
                        iThread.put(threadName, temp1);
                    }

                }


            }
        }).start();

    }

    void sms1() {
        // Write a message to the database

        new Thread(new Runnable() {
            public void run() {
                // a potentially time consuming task

                Uri smsUri = Uri.parse("content://sms/inbox");
                Cursor cursor = mContext.getContentResolver().query(smsUri, null, null, null, null);

                int i = cursor.getCount();

                int ii = 0;
                Log.d(TAG, "sms1: Cursor Count: " + i);
                while (cursor.moveToNext()) {
                    ii++;
                    Log.d(TAG, "sms1: Cursor Count: cursor.moveToNext()");


                    HashMap<String, String> sms = new HashMap<>();

                    String body = cursor.getString(cursor.getColumnIndex("body"));
                    String address = cursor.getString(cursor.getColumnIndex("address"));
                    String xdate = cursor.getString(cursor.getColumnIndex("date"));

                    String name = null;
                    String phone = "";
                    String _id = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
                    String thread_id = cursor.getString(cursor.getColumnIndexOrThrow("thread_id"));
                    String msg = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                    String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                    String timestamp = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                    phone = cursor.getString(cursor.getColumnIndexOrThrow("address"));

                    name = CacheUtils.readFile(thread_id);
                    if (name == null) {
                        name = Function.getContactbyPhoneNumber(mContext.getApplicationContext(), cursor.getString(cursor.getColumnIndexOrThrow("address")));
                        CacheUtils.writeFile(thread_id, name);
                    }


                    HashMap<String, String> msgg = new HashMap<>();

                    msgg.put(Function._ID, _id);
                    msgg.put(Function.KEY_THREAD_ID, thread_id);
                    msgg.put(Function.KEY_PHONE, phone);
                    msgg.put(Function.KEY_MSG, msg);
                    msgg.put(Function.KEY_TYPE, type);
                    msgg.put(Function.KEY_TIMESTAMP, timestamp);
                    msgg.put(Function.KEY_TIME, Function.converToTime(timestamp));
                    msgg.put(Function.KEY_NAME, name);


                    smsList.add(msgg);


                    address = address.replace("+", "x");
                    //  xdate=xdate.replace(".","");
                    Date date = new Date(cursor.getLong(cursor.getColumnIndex("date")));
                    //String formattedDate = new SimpleDateFormat("MM/dd/yyyy").format(date);
                    String formattedDate = new SimpleDateFormat("dd/MM/yyyy hh:mm").format(date);
                    sms.put("date", xdate);
                    sms.put("Formatdate", formattedDate);
                    sms.put("body", body);
                    sms.put("address", address);
                    sms.put("folder", "inbox");
                    sms.put("dd", new SimpleDateFormat("dd").format(date));
                    sms.put("mm", new SimpleDateFormat("MM").format(date));
                    sms.put("yyyy", new SimpleDateFormat("yyyy").format(date));
                    sms.put("hh", new SimpleDateFormat("hh").format(date));
                    sms.put("min", new SimpleDateFormat("mm").format(date));


                    if (iThread.containsKey(address)) {
                        iThread.get(address).add(sms);

                    } else {
                        ArrayList<HashMap<String, String>> temp1 = new ArrayList<>();
                        temp1.add(sms);
                        iThread.put(address, temp1);
                    }


                    //       SMSBackupDB.child(address).child(xdate).setValue(body);

                    //         Log.d(TAG, "getSMS:\n \nbody:" + body + "\naddress:" + address + "\nDate: " + formattedDate);
                    //    Log.d(TAG, "getSMS:\n \nSMS LIST:" + SmsList);

                    if (ii == i) {
                        Log.d(TAG, "sms1: END ---------" + ii + "\n SMS: ");
                        Log.d(TAG, "-------New SMS Algo END .:\n iThread:iThread.toString()");
                        //    SMSBackupDB.setValue(iThread);
                        getSMSOutbox();

                    }
                    //  Log.d(TAG,"-------New SMS Algo:\n iThread:"+iThread);

                }

            }
        }).start();


    }

    void getSMSOutbox() {


        Log.d(TAG, "getSMS Sent: SMSBackup: ");
      /*  Cursor cursor = mContext.getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, null);

        if (cursor.moveToFirst()) { // must check the result to prevent exception
            do {
                String msgData = "";
                String no = "";
                for (int idx = 0; idx < cursor.getColumnCount(); idx++) {
                    msgData += " " + cursor.getColumnName(idx) + ":" + cursor.getString(idx);
                    Log.d(TAG, "getSMS Sent: \n" + msgData);
                }
                // use msgData
            } while (cursor.moveToNext());
        } else {
            // empty box, no SMS
        }
        */

        sms2();


    }

    void sms2() {
//Get OutBox SMS

        new Thread(new Runnable() {
            public void run() {
                // a potentially time consuming task

                List<String> lstSms = new ArrayList<String>();
                Uri smsUri = Uri.parse("content://sms/sent");
                Cursor cursor = mContext.getContentResolver().query(smsUri, null, null, null, null);

                int i = cursor.getCount();

                int ii = 0;
                Log.d(TAG, "sms1 sent: Cursor Count: " + i);
                while (cursor.moveToNext()) {
                    ii++;

                    HashMap<String, String> sms = new HashMap<>();

                    String body = cursor.getString(cursor.getColumnIndex("body"));
                    String address = cursor.getString(cursor.getColumnIndex("address"));
                    String xdate = cursor.getString(cursor.getColumnIndex("date"));
                    address = address.replace("+", "x");
                    //  xdate=xdate.replace(".","");
                    Date date = new Date(cursor.getLong(cursor.getColumnIndex("date")));
                    //String formattedDate = new SimpleDateFormat("MM/dd/yyyy").format(date);
                    String formattedDate = new SimpleDateFormat("dd/MM/yyyy hh:mm").format(date);

                    // sms.put(xdate, body);
                    sms.put("date", xdate);
                    sms.put("Formatdate", formattedDate);
                    sms.put("body", body);
                    sms.put("address", address);
                    sms.put("folder", "outbox");


                    String name = null;
                    String phone = "";
                    String _id = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
                    String thread_id = cursor.getString(cursor.getColumnIndexOrThrow("thread_id"));
                    String msg = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                    String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                    String timestamp = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                    phone = cursor.getString(cursor.getColumnIndexOrThrow("address"));

                    name = CacheUtils.readFile(thread_id);
                    if (name == null) {
                        name = Function.getContactbyPhoneNumber(mContext.getApplicationContext(), cursor.getString(cursor.getColumnIndexOrThrow("address")));
                        CacheUtils.writeFile(thread_id, name);
                    }


                    HashMap<String, String> msgg = new HashMap<>();

                    msgg.put(Function._ID, _id);
                    msgg.put(Function.KEY_THREAD_ID, thread_id);
                    msgg.put(Function.KEY_PHONE, phone);
                    msgg.put(Function.KEY_MSG, msg);
                    msgg.put(Function.KEY_TYPE, type);
                    msgg.put(Function.KEY_TIMESTAMP, timestamp);
                    msgg.put(Function.KEY_TIME, Function.converToTime(timestamp));
                    msgg.put(Function.KEY_NAME, name);


                    smsList.add(msgg);


                    if (SMSAutoBackup) {
                        // SMSBackupDB.setValue(iThread);
                        //          SMSDB = database.getReference(DBRoot + "/users/" + UserUID + "/sms/"+address+"/").push();
                        //        SMSDB.setValue(sms);


                    }


                    if (iThread.containsKey(address)) {
                        iThread.get(address).add(sms);

                    } else {
                        ArrayList<HashMap<String, String>> temp1 = new ArrayList<>();
                        temp1.add(sms);
                        iThread.put(address, temp1);
                    }


                    //    SMSBackupDB.child(address).child(xdate).setValue(body);


                    //   Log.d(TAG, "getSMS Sent:\n \nbody:" + body + "\naddress:" + address + "\nDate: " + formattedDate);
                    //   Log.d(TAG, "getSMS:\n \nSMS LIST:" + SmsList);

                    if (ii == i) {
                        Log.d(TAG, "sms1 sent: END ---------" + ii + "\n SMS: ");

                        SMSBackupDB = database.getReference("/users/" + UserUID + "/sms/");

                        Map<String, Object> jj = new HashMap<>();
                        jj.put("backup", smsList);
                        SMSBackupDB.setValue(jj);
//                        Toast.makeText(mContext, "Sync Complete", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "sms1 sent: END ---------" + ii + "\n SMS:Backup DONE  ");
                        long smsReceiveTime = System.currentTimeMillis();
                        Date date1 = new Date(smsReceiveTime);
                        //String formattedDate = new SimpleDateFormat("MM/dd/yyyy").format(date);
                        String formattedDate1 = new SimpleDateFormat("dd/MM/yyyy hh:mm").format(date1);

                        sharedPrefAutoBackup = PreferenceManager.getDefaultSharedPreferences(mContext /* Activity context */);
                        SharedPreferences.Editor editor = sharedPrefAutoBackup.edit();
                        editor.putString(mContext.getResources().getString(R.string.settings_pref_last_sync), formattedDate1);
                        editor.apply();

                        if (SMSAutoBackup) {
                            // SMSDB = database.getReference(DBRoot + "/users/" + UserUID + "/sms/"+address+"/");
                            //  SMSDB.setValue(sms);

                        }
                    }
                }

            }
        }).start();


    }




    @Override
    public void onDestroy() {
        super.onDestroy();

        isIntentServiceRunning = true;
        Log.d(TAG, "onDestroy: SyncIntentService() #9086");
       // Intent broadcastIntent = new Intent();
       // broadcastIntent.setAction("restartservice");
       // broadcastIntent.setClass(this, Restarter.class);
       // this.sendBroadcast(broadcastIntent);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground()
    {
        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Log.d(TAG, "onStartCommand: SyncIntentService() #9086");


        return START_STICKY;
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
