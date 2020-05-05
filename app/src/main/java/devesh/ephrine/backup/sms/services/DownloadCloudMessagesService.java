package devesh.ephrine.backup.sms.services;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.room.Room;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import devesh.ephrine.backup.sms.Function;
import devesh.ephrine.backup.sms.MapComparator;
import devesh.ephrine.backup.sms.R;
import devesh.ephrine.backup.sms.room.AppDatabase;
import devesh.ephrine.backup.sms.room.Sms;
import io.fabric.sdk.android.Fabric;

public class DownloadCloudMessagesService extends JobIntentService {
    public static final int JOB_ID = 2;
    String TAG = "Cloud_MSG_Service";
    boolean isSubscribed;
    //   HashMap<String, ArrayList<HashMap<String, String>>> SmsList = new HashMap<>();
    String UserUID;
    ArrayList<HashMap<String, String>> CloudSms = new ArrayList<>();
    ArrayList<HashMap<String, String>> CloudThreadSms = new ArrayList<>();
    String BackupStorageDB;
    File localFile;
    Gson gson;
    AppDatabase db;
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
    PowerManager.WakeLock wakeLock;
    SharedPreferences sharedPrefAppGeneral;
    NotificationManagerCompat notificationManager;
    NotificationCompat.Builder nmbuilder;
    int PROGRESS_MAX = 100;
    //  Trace myTrace;


    /**
     * The service is starting, due to a call to startService()
     */
  /*  @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
    */
    int PROGRESS_CURRENT = 0;
    private FirebaseAuth mAuth;

    public DownloadCloudMessagesService() {
    }

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, DownloadCloudMessagesService.class, JOB_ID, work);
    }

    /*
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;

    }
    */
    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        // your code

        Fabric.with(getApplicationContext(), new Crashlytics());

        Log.d(TAG, "onCreate: SyncIntentService() #9086");


        Fabric.with(getApplicationContext(), new Crashlytics());
        //    startSync();

    }

    /**
     * Called when The service is no longer used and is being destroyed
     */
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        CloudSms.clear();
/*
try {
    wakeLock.release();
}catch (Exception e){
    Log.e(TAG, "onDestroy: ",e );
    Crashlytics.logException(e);
}*/
        notificationManager.cancel(002);

        try {
            //      myTrace.stop();
        } catch (Exception e) {
            Log.e(TAG, "onDestroy: ERROR #564 ", e);
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        //    myTrace = FirebasePerformance.getInstance().newTrace("SyncIntentService");
        //    myTrace.start();

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                TAG + "::MyWakelockTag");
        wakeLock.acquire();
        sharedPrefAppGeneral = PreferenceManager.getDefaultSharedPreferences(this);

        Log.d(TAG, "downloadCloudSMS:  downloadCloudSMS()");

        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, getString(R.string.DATABASE_CLOUD_SMS_DB)).build();


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Crashlytics.setUserIdentifier(user.getUid());

            UserUID = user.getPhoneNumber().replace("+", "x");

            AppStart();
        }


    }

    void AppStart() {

        String sub = sharedPrefAppGeneral.getString(getString(R.string.cache_Sub_isSubscribe), "0");

        isSubscribed = sub.equals("1");


        if (isSubscribed) {
            if (isNetworkAvailable()) {
                Log.d(TAG, "downloadCloudSMS:  downloadCloudSMS() isSubscribed");
                setNotificationCloudRefresh();
                BackupStorageDB = "SMSDrive/Users/" + UserUID + "/backup/file_cloud_sms.zip";

                StorageReference mStorageRef;
                mStorageRef = FirebaseStorage.getInstance().getReference();
                StorageReference riversRef = mStorageRef.child(BackupStorageDB);
                localFile = null;
                gson = new Gson();

                ArrayList<HashMap<String, String>> tmpList = null;
                try {
                    tmpList.clear();
                    tmpList = (ArrayList<HashMap<String, String>>) Function.readCachedFile(this, getString(R.string.file_device_sms));

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

                                new Thread(new Runnable() {
                                    public void run() {
                                        File unziped = unzipFile(localFile);
                                        Log.d(TAG, "onSuccess: Unziped: " + unziped.getPath());
                                        String JsonStr = ConvertFileToStrng(unziped);
                                        Type type = new TypeToken<ArrayList<HashMap<String, String>>>() {
                                        }.getType();

                                        ArrayList<HashMap<String, String>> jj = gson.fromJson(JsonStr, type);
                                        CloudSms.addAll(jj);

                                        //  ArrayList<HashMap<String, String>> CleanHash = new ArrayList<>();
                                        //  CleanHash = RemoveDuplicateHashMaps(CloudSms);

                                        //  CloudSms.clear();
                                        // CloudSms = CleanHash;
                                        //CleanHash =  smsList;
                                        //    UploadToCloud(CleanHash);

                                        //Unzip File

                                        Log.d(TAG, "onDataChange: END of CLOUD SMS");
                                        //  Collections.sort(CloudSms, new MapComparator(Function.KEY_TIMESTAMP, "dsc")); // Arranging sms by timestamp decending
                                        try {
                                            Function.createCachedFile(DownloadCloudMessagesService.this, getString(R.string.file_cloud_sms), jj);
                                            Log.d(TAG, "onDataChange: createCachedFile file_cloud_sms ");

                                        } catch (Exception e) {
                                            Log.d(TAG, "onDataChange: ERROR #56 : " + e);
                                            Crashlytics.logException(e);

                                        }
                                        CloudThreadSms = CloudSms;
                                        ArrayList<HashMap<String, String>> purified = Function.removeDuplicates(CloudThreadSms); // Removing duplicates from inbox & sent
                                        Collections.sort(CloudThreadSms, new MapComparator(Function.KEY_TIMESTAMP, "dsc")); // Arranging sms by timestamp decending
                                        CloudThreadSms.clear();
                                        CloudThreadSms.addAll(purified);
                                        try {
                                            Function.createCachedFile(DownloadCloudMessagesService.this, getString(R.string.file_cloud_thread), CloudSms);
                                            Log.d(TAG, "onDataChange: createCachedFile file_cloud_thread ");
                                            //   stopSelf();
                                            //    Intent intentStop = new Intent(DownloadCloudMessagesService.this, DownloadCloudMessagesService.class);
                                            //  CloudSMS2DBService.enqueueWork(getApplicationContext(), new Intent());

                                            //  Intent intent = new Intent(DownloadCloudMessagesService.this, CloudSMS2DBService.class);

                                            //  String message = editText.getText().toString();
                                            //intent.putExtra(EXTRA_MESSAGE, message);

                                            //      notificationManager.cancel(002);
                                            // startService(intent);
                                            AddtoDatabaseTask();
                                            //stopSelf();
                                        } catch (Exception e) {
                                            Log.d(TAG, "onDataChange: ERROR #5600 : " + e);
                                            Crashlytics.logException(e);
                                            stopSelf();
                                            //       wakeLock.release();

                                        }


                                    }
                                }).start();




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
                        Log.d(TAG, "onFailure: ERROR #546766 " + exception + " \n message: " + exception.getMessage());


                        Log.d(TAG, "onFailure: ERROR CloudSms.clear();");
                        CloudSms.clear();
                        CloudThreadSms.clear();
                        try {
                            Function.createCachedFile(DownloadCloudMessagesService.this, getString(R.string.file_cloud_sms), CloudSms);

                            Function.createCachedFile(DownloadCloudMessagesService.this, getString(R.string.file_cloud_thread), CloudThreadSms);
                            Log.d(TAG, "onDataChange: createCachedFile file_cloud_thread ");
                            // stopSelf();
                        } catch (Exception e) {
                            Log.d(TAG, "onDataChange: ERROR #5600 : " + e);
                            Crashlytics.logException(e);
                            // stopSelf();
                        }
                        AddtoDatabaseTask();
                        //   wakeLock.release();
                        //   notificationManager.cancel(002);

                        //    Intent intent = new Intent(DownloadCloudMessagesService.this, CloudSMS2DBService.class);

                        //  String message = editText.getText().toString();
                        //intent.putExtra(EXTRA_MESSAGE, message);
                        // startActivity(intent);
                        //    UploadToCloud(smsList);
                        // stopSelf();

                    }
                });

            } else {
                cancelAllNotification();
                stopSelf();
            }



/*
        CloudSMSDB = database.getReference("/users/" + UserUID + "/sms/backup");
        CloudSMSDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {


                    long t = dataSnapshot.getChildrenCount();
                    int i = 0;
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        HashMap<String, String> msg = new HashMap<>();
                        msg.put(Function._ID, postSnapshot.child(Function._ID).getValue(String.class));
                        msg.put(Function.KEY_THREAD_ID, postSnapshot.child(Function.KEY_THREAD_ID).getValue(String.class));
                        msg.put(Function.KEY_NAME, postSnapshot.child(Function.KEY_NAME).getValue(String.class));
                        msg.put(Function.KEY_PHONE, postSnapshot.child(Function.KEY_PHONE).getValue(String.class));
                        msg.put(Function.KEY_MSG, postSnapshot.child(Function.KEY_MSG).getValue(String.class));
                        msg.put(Function.KEY_TYPE, postSnapshot.child(Function.KEY_TYPE).getValue(String.class));
                        msg.put(Function.KEY_TIMESTAMP, postSnapshot.child(Function.KEY_TIMESTAMP).getValue(String.class));
                        msg.put(Function.KEY_TIME, postSnapshot.child(Function.KEY_TIME).getValue(String.class));

                        CloudSms.add(msg);
                        Log.d(TAG, "onDataChange: Downloading CloudSMS....." + i);
                        if (i == t - 1) {
                            Log.d(TAG, "onDataChange: END of CLOUD SMS");
                            Collections.sort(CloudSms, new MapComparator(Function.KEY_TIMESTAMP, "dsc")); // Arranging sms by timestamp decending

                            try {
                                Function.createCachedFile(DownloadCloudMessagesService.this, getString(R.string.file_cloud_sms), CloudSms);
                                Log.d(TAG, "onDataChange: createCachedFile file_cloud_sms ");

                            } catch (Exception e) {
                                Log.d(TAG, "onDataChange: ERROR #56 : " + e);
                            }
                            CloudThreadSms = CloudSms;
                            Collections.sort(CloudThreadSms, new MapComparator(Function.KEY_TIMESTAMP, "dsc")); // Arranging sms by timestamp decending
                            ArrayList<HashMap<String, String>> purified = Function.removeDuplicates(CloudThreadSms); // Removing duplicates from inbox & sent
                            CloudThreadSms.clear();
                            CloudThreadSms.addAll(purified);
                            try {
                                Function.createCachedFile(DownloadCloudMessagesService.this, getString(R.string.file_cloud_thread), CloudSms);
                                Log.d(TAG, "onDataChange: createCachedFile file_cloud_thread ");
                            } catch (Exception e) {
                                Log.d(TAG, "onDataChange: ERROR #5600 : " + e);
                            }

                        }
                        i++;

                    }
                    textView5CloudEmpty.setVisibility(View.GONE);

                } else {
                    Log.d(TAG, "onDataChange: Backup not Exists !! #021");
                    textView5CloudEmpty.setVisibility(View.VISIBLE);
                    try {
                        Function.createCachedFile(DownloadCloudMessagesService.this, getString(R.string.file_cloud_thread), CloudSms);
                        Log.d(TAG, "onDataChange: createCachedFile file_cloud_thread ");
                    } catch (Exception e) {
                        Log.d(TAG, "onDataChange: ERROR #5600 : " + e);
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

    File unzipFile(File zipfile) {
        InputStream is;
        ZipInputStream zis;
        File unzip_file = null;

        try {
            unzip_file = File.createTempFile("backuprestore", "json");
            String filename;
            is = new FileInputStream(zipfile);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null) {
                filename = ze.getName();

                // Need to create directories if not exists, or
                // it will generate an Exception...
                if (ze.isDirectory()) {
                    File fmd = new File(getFilesDir(), filename);
                    fmd.mkdirs();
                    continue;
                }

                OutputStream fout = new FileOutputStream(unzip_file);

                while ((count = zis.read(buffer)) != -1) {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zis.closeEntry();
            }

            zis.close();
        } catch (IOException e) {
            e.printStackTrace();
            Crashlytics.logException(e);


        }
        return unzip_file;
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

    void setNotificationCloudRefresh() {

        nmbuilder = new NotificationCompat.Builder(this, "002")
                .setSmallIcon(R.drawable.app_logo)
                .setContentTitle("Getting Messages from Cloud")
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


    void AddtoDatabaseTask() {
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

    private boolean isNetworkAvailable() {
        return true;
      /*     try {
            InetAddress ipAddr = InetAddress.getByName("google.com");
            //You can replace it with your name
            return !ipAddr.equals("");

        } catch (Exception e) {
            return false;
        }
     ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        */
    }

    void cancelAllNotification() {
        Log.d(TAG, "cancelAllNotification: NO INTERNET !!");
        if (notificationManager != null) {
            Log.d(TAG, "cancelAllNotification: CLEARING ALL NOTIFICATION");

            try {
                notificationManager.cancel(001);
            } catch (Exception e) {
                Log.e(TAG, "cancelNotification: ERROR #5423 ", e);
            }

            try {
                notificationManager.cancel(002);
            } catch (Exception e) {
                Log.e(TAG, "cancelNotification: ERROR #32424 ", e);
            }

        }


    }

    class AddSmsDB extends AsyncTask<String, Void, String> {
        final String TAG = "CloudSMS2DBService ";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "onPreExecute");

            //   CacheUtils.writeFile(getString(R.string.BG_Task_Status), "1");
           /* if (getCurProcessName(getApplicationContext()).equals(getPackageName())) {
                // initialize the database
                db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, getString(R.string.DATABASE_CLOUD_SMS_DB))
                        //.setJournalMode(RoomDatabase.JournalMode.AUTOMATIC)

                        .build();
            }
            */
            nmbuilder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, true)
                    .setContentText("starting..").setContentTitle("Processing Messages from Cloud");
            notificationManager.notify(002, nmbuilder.build());


        }

        protected String doInBackground(String... args) {
            SharedPreferences.Editor editor = sharedPrefAppGeneral.edit();
            editor.putString(getString(R.string.BG_Task_Status), "1").apply();


            String xml = "";
            ArrayList<HashMap<String, String>> al = null;
            try {
                CloudSms = (ArrayList<HashMap<String, String>>) Function.readCachedFile(DownloadCloudMessagesService.this, getString(R.string.file_cloud_sms));

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
                if (wakeLock.isHeld()) {
                    wakeLock.release();
                }
            } catch (Exception e) {
                Log.e(TAG, "onPostExecute: ERROR #21039", e);
                Crashlytics.logException(e);
            }


            if (db != null) {
                db.close();
            }


            SharedPreferences.Editor editor = sharedPrefAppGeneral.edit();
            editor.putString(getString(R.string.BG_Task_Status), "0").apply();
            CloudSms.clear();
            notificationManager.cancel(002);

            try {
                //           myTrace.stop();
            } catch (Exception e) {
                Log.e(TAG, "onDestroy: ERROR #564 ", e);
            }
            stopSelf();
        }
    }


}
