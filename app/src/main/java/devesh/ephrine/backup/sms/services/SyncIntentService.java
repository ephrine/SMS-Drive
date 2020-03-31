package devesh.ephrine.backup.sms.services;

import android.Manifest;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.core.content.ContextCompat;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lifeofcoding.cacheutlislibrary.CacheUtils;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import devesh.ephrine.backup.sms.Function;
import devesh.ephrine.backup.sms.R;
import io.fabric.sdk.android.Fabric;

import static devesh.ephrine.backup.sms.Function.KEY_THREAD_ID;
import static devesh.ephrine.backup.sms.Function.mappingInbox;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class SyncIntentService extends JobIntentService {
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
    String BackupStorageDB;
    String name;
    /*
        public SyncIntentService() {
            super("SyncIntentService");
        }*/
    File localFile;
    Gson gson;

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

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, SyncIntentService.class, JOB_ID, work);
    }

    public static File newFile(ZipEntry zipEntry) throws IOException {
        File destFile = new File(zipEntry.getName());

        // String destDirPath = destinationDir.getCanonicalPath();
        // String destFilePath = destFile.getCanonicalPath();

        //  if (!destFilePath.startsWith(destDirPath + File.separator)) {
        //      throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        //  }

        return destFile;
    }



    @Override
    public void onCreate() {
        Fabric.with(getApplicationContext(), new Crashlytics());

        Log.d(TAG, "onCreate: SyncIntentService() #9086");
       /* if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O){
            startMyOwnForeground();
            Log.d(TAG, "onCreate: SyncIntentService() startMyOwnForeground #908600");

        }
        else{
            startForeground(1, new Notification());
            Log.d(TAG, "onCreate: SyncIntentService() startForeground #9086");

        }*/
        startSync();

        super.onCreate();
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        // your code

        Fabric.with(getApplicationContext(), new Crashlytics());

        Log.d(TAG, "onCreate: SyncIntentService() #9086");


        Fabric.with(getApplicationContext(), new Crashlytics());
        //    startSync();

    }

    /*  @Override
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
        startSync();


    }
*/
    public void startSync() {
        mContext = getApplicationContext();
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
                    //     SMSBackupDB = database.getReference("/users/" + UserUID + "/sms/backup");
                    //SMSScanDevice();
                    BackupStorageDB = "SMSDrive/Users/" + UserUID + "/backup/file_cloud_sms.zip";

                    try {

                        tmpList.clear();
                        tmpList = (ArrayList<HashMap<String, String>>) Function.readCachedFile(mContext, "orgsms");

                    } catch (Exception e) {
                        Log.d(TAG, "startSync: ERROR " + e);
                        Crashlytics.logException(e);

                    }

                    sms1();

  /*                  SMSBackupDB.addListenerForSingleValueEvent(new ValueEventListener() {
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

*/
                }

            }

        }


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
                    String thread_id = DS.child(KEY_THREAD_ID).getValue(String.class);
                    String msg = DS.child(Function.KEY_MSG).getValue(String.class);
                    String type = DS.child(Function.KEY_TYPE).getValue(String.class);
                    String timestamp = DS.child(Function.KEY_TIMESTAMP).getValue(String.class);
                    String time = DS.child(Function.KEY_TIME).getValue(String.class);


                    //    Date date = new Date(Long.parseLong(MsgTime));
                    //  String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);

                    HashMap<String, String> SMS = new HashMap<>();
                    SMS.put(Function._ID, _id);
                    SMS.put(KEY_THREAD_ID, thread_id);
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
                    msgg.put(KEY_THREAD_ID, thread_id);
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
                    msgg.put(KEY_THREAD_ID, thread_id);
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
                        DownloadFromCloud();
                        //      SMSBackupDB = database.getReference("/users/" + UserUID + "/sms/");

                        //   Map<String, Object> jj = new HashMap<>();
                        //  jj.put("backup", smsList);
                        //   SMSBackupDB.setValue(jj);
//                        Toast.makeText(mContext, "Sync Complete", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "sms1 sent: END ---------" + ii + "\n SMS:Backup DONE  ");

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

    /*
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
                    .setContentTitle("SMS Sync in Progress..")
                    .setContentText("SMS Sync in Progress")

                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            startForeground(2, notification);
        }


        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            super.onStartCommand(intent, flags, startId);

            Log.d(TAG, "onStartCommand: SyncIntentService() #9086");
          //  startSync();

            return START_STICKY;
        }
        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        */
    public void DownloadFromCloud() {
        Log.d(TAG, "DownloadFromCloud");
        StorageReference mStorageRef;
        mStorageRef = FirebaseStorage.getInstance().getReference();
        StorageReference riversRef = mStorageRef.child(BackupStorageDB);
        localFile = null;
        gson = new Gson();

        try {
            tmpList.clear();
            tmpList = (ArrayList<HashMap<String, String>>) Function.readCachedFile(mContext, getString(R.string.file_device_sms));

        } catch (Exception e) {
            Log.d(TAG, "DownloadFromCloud: ERROR " + e);
            Crashlytics.logException(e);

        }

        try {
            localFile = File.createTempFile("smscloud", "backup");
        } catch (Exception e) {
            Log.d(TAG, "DownloadFromCloud: #ERROR " + e);
            Crashlytics.logException(e);

        }
        riversRef.getFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        // Successfully downloaded data to local file
                        Log.d(TAG, "onSuccess: DownloadFromCloud");


                      File unziped=unzipFile(localFile);
                        Log.d(TAG, "onSuccess: Unziped: "+unziped.getPath());
                        String JsonStr = ConvertFileToStrng(unziped);
                        Type type = new TypeToken<ArrayList<HashMap<String, String>>>() {
                        }.getType();

                        ArrayList<HashMap<String, String>> jj = gson.fromJson(JsonStr, type);
                        smsList.addAll(jj);

                        ArrayList<HashMap<String, String>> CleanHash = new ArrayList<>();
                        CleanHash = RemoveDuplicateHashMaps(smsList);
                        //CleanHash =  smsList;
                        UploadToCloud(CleanHash);

                        //Unzip File






                    /*
                     try {
                        Function.createCachedFile(getApplicationContext(),getString(R.string.file_cloud_sms),jj);
                    }catch (Exception e){
                        Log.d(TAG, "onSuccess: ERROR "+e);
                    }
                    */

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle failed download
                Log.d(TAG, "onFailure: ERROR " + exception);

                UploadToCloud(smsList);

            }
        });
    }

    File unzipFile(File zipfile){
        InputStream is;
        ZipInputStream zis;
       File unzip_file=null;

        try
        {
            unzip_file=File.createTempFile("backuprestore","json");
            String filename;
            is = new FileInputStream(zipfile);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null)
            {
                filename = ze.getName();

                // Need to create directories if not exists, or
                // it will generate an Exception...
                if (ze.isDirectory()) {
                    File fmd = new File(getFilesDir(), filename);
                    fmd.mkdirs();
                    continue;
                }

                OutputStream fout = new FileOutputStream(unzip_file);

                while ((count = zis.read(buffer)) != -1)
                {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zis.closeEntry();
            }

            zis.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            Crashlytics.logException(e);


        }
return  unzip_file;
    }

    public void UploadToCloud(ArrayList<HashMap<String, String>> sms) {
        Log.d(TAG, "UploadToCloud");
        Gson gson = new Gson();
        String jsonSTR = gson.toJson(sms);

        Type type = new TypeToken<ArrayList<HashMap<String, String>>>() {
        }.getType();

        ArrayList<HashMap<String, String>> tempCloudSMS=gson.fromJson(jsonSTR,type);

        try {
            Function.createCachedFile(getApplicationContext(), getString(R.string.file_cloud_sms), tempCloudSMS);
            //  ArrayList<HashMap<String, String>> tmpList = (ArrayList<HashMap<String, String>>) Function.readCachedFile(getApplicationContext(), getString(R.string.file_device_sms));
        } catch (Exception e) {
            Log.d(TAG, "UploadToCloud: ERROR #4653 " + e);
            Crashlytics.logException(e);

        }

        File mfile = null;
        try {
            mfile = File.createTempFile("mbackup", "json");
        } catch (Exception e) {
            Log.d(TAG, "DownloadFromCloud: #ERROR " + e);
            Crashlytics.logException(e);

        }

        try {
            mfile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(mfile);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(jsonSTR);
            myOutWriter.close();
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
            Crashlytics.logException(e);

        }

        // Create ZIP
        // String sourceFile = jsonSTRING;
     File zip_file=null;
        FileOutputStream fos = null;
        OutputStream out;

        try {
            zip_file=File.createTempFile("backup","zip");
            //out = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath()+"/backup.zip");
            out = new FileOutputStream(zip_file);
         //   out = new FileOutputStream("backup.zip");
            ZipOutputStream zipOut = new ZipOutputStream(out);
            File fileToZip = mfile;
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            zipOut.close();
            fis.close();
            out.close();

        } catch (Exception e) {
            Log.d(TAG, "createZIP: ERROR #2345 " + e);
            Crashlytics.logException(e);

        }

        BackupStorageDB = "SMSDrive/Users/" + UserUID + "/backup/file_cloud_sms.zip";

        StorageReference mStorageRef;
        mStorageRef = FirebaseStorage.getInstance().getReference();
        StorageReference riversRef = mStorageRef.child(BackupStorageDB);


      /*  InputStream stream = null;
        try {
            stream = new FileInputStream(File.createTempFile("temp1", "backup"));


            // ByteArrayOutputStream buffer = (ByteArrayOutputStream) fos;
            //  byte[] bytes = buffer.toByteArray();
            // stream = new ByteArrayInputStream(bytes);
            //FileUtils.copyFile(stream,fos);

            //IOUtils.copyLarge(stream, fos);
        } catch (Exception e) {
            Log.d(TAG, "UploadToCloud: ERROR #564 " + e);
        }
        */


//        UploadTask uploadTask = riversRef.putStream(stream);
    //    File cfile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/backup.zip");
        File cfile = zip_file;
        Uri file = Uri.fromFile(cfile);

        UploadTask uploadTask = riversRef.putFile(file);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
                FinalWork();
                Log.d(TAG, "onSuccess: SUCCESS UPLOAD ZIP");
            }
        });


    }

    String ConvertFileToStrng(File file) {

        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                //text.append('\n');
            }
            br.close();
        } catch (IOException e) {
            //You'll need to add proper error handling here
            Crashlytics.logException(e);

        }

        return text.toString();
    }

    ArrayList<HashMap<String, String>> RemoveDuplicateHashMaps(ArrayList<HashMap<String, String>> A) {
        Log.d(TAG, "RemoveDuplicateHashMaps: Removing Duplicate...");
        ArrayList<HashMap<String, String>> CleanHash = new ArrayList<>();
       // ArrayList<HashMap<String, String>> gpList = A;
        for (int i = 0; i < A.size(); i++) {
            boolean available = false;
            for (int j = 0; j < CleanHash.size(); j++) {
                if (CleanHash.get(j).get(Function.KEY_MSG) == A.get(i).get(Function.KEY_MSG)
                && CleanHash.get(j).get(Function.KEY_TIMESTAMP) == A.get(i).get(Function.KEY_TIMESTAMP)
                        && CleanHash.get(j).get(Function.KEY_PHONE) == A.get(i).get(Function.KEY_PHONE)
                        && CleanHash.get(j).get(Function.KEY_TYPE) == A.get(i).get(Function.KEY_TYPE)
                ){
                    available = true;
                    Log.d(TAG, "RemoveDuplicateHashMaps: Duplicate Found");
                    break;
                }
            }

            if (!available) {
                Log.d(TAG, "RemoveDuplicateHashMaps: Added Non-Duplicate");
                CleanHash.add(A.get(i));
            }
        }
        return CleanHash;
    }

    void FinalWork() {
        long smsReceiveTime = System.currentTimeMillis();
        Date date1 = new Date(smsReceiveTime);
        //String formattedDate = new SimpleDateFormat("MM/dd/yyyy").format(date);
        String formattedDate1 = new SimpleDateFormat("dd/MM/yyyy hh:mm").format(date1);

        sharedPrefAutoBackup = PreferenceManager.getDefaultSharedPreferences(mContext /* Activity context */);
        SharedPreferences.Editor editor = sharedPrefAutoBackup.edit();
        editor.putString(mContext.getResources().getString(R.string.settings_pref_last_sync), formattedDate1);
        editor.apply();

    }

    FileOutputStream createZIP(String jsonSTRING) {

        String sourceFile = jsonSTRING;
        FileOutputStream fos = null;
        File fileToZip = null;
        try {
            fos = new FileOutputStream("backup.zip");
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            fileToZip = new File(sourceFile);
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            zipOut.close();
            fis.close();
            fos.close();


        } catch (Exception e) {
            Log.d(TAG, "createZIP: ERROR #2345 " + e);
            Crashlytics.logException(e);

        }

        return fos;
    }

    void unzip(File zipFile) {
        // String fileZip = "src/main/resources/unzipTest/compressed.zip";
        // File destDir = new File("/");
        byte[] buffer = new byte[1024];

        try {
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = newFile(zipEntry);
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        } catch (Exception e) {
            Log.d(TAG, "unzip: ERROR #324 " + e);
            Crashlytics.logException(e);

        }

    }

    File inputstreamtofile(InputStream inputStream) {
        File file = null;
        try (OutputStream outputStream = new FileOutputStream(file)) {
            IOUtils.copy(inputStream, outputStream);
        } catch (FileNotFoundException e) {
            // handle exception here
        } catch (IOException e) {
            Crashlytics.logException(e);

            // handle exception here
        }
        return file;
    }

    File convertFileOutputStreamToFile(FileOutputStream out, File file) {
        File fileOutputStream = null;
        FileOutputStream fos = out;

        try {

            fos = new FileOutputStream(file, true);

            // Writes bytes from the specified byte array to this file output stream
            //  fos.write(s.getBytes());

        } catch (FileNotFoundException e) {
            System.out.println("File not found" + e);
        } catch (IOException ioe) {
            System.out.println("Exception while writing file " + ioe);
        } finally {
            // close the streams using close method
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ioe) {
                System.out.println("Error while closing stream: " + ioe);
            }

        }

        return fileOutputStream;
    }

/**
 File directory = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Images");
 localFile = new File(directory, filename);

 * */


}

