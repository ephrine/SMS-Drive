package devesh.ephrine.backup.sms.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.PowerManager;
import android.os.Process;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.room.Room;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import devesh.ephrine.backup.sms.Function;
import devesh.ephrine.backup.sms.R;
import devesh.ephrine.backup.sms.room.AppDatabase;

public class ApplyCloudChangesService extends JobIntentService {
    /**
     * Unique job ID for this service.
     */
    static final int JOB_ID = 1000;
    //--
    public static boolean isIntentServiceRunning = false;
    final String TAG = "SyncIntentService";
    final boolean isDefaultApp = true;
    //  final String DBRoot = "SMSDrive/";
    public HashMap<String, LinkedHashSet<HashMap<String, String>>> iThread;
    AppDatabase db;
    ArrayList<HashMap<String, String>> smsList = new ArrayList<HashMap<String, String>>();
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
    // LinkedHashSet<HashMap<String, String>> smsList = new LinkedHashSet<>();
    LinkedHashSet<HashMap<String, String>> customList = new LinkedHashSet<HashMap<String, String>>();
    LinkedHashSet<HashMap<String, String>> tmpList = new LinkedHashSet<HashMap<String, String>>();
    String BackupStorageDB;
    String name;
    /*
        public SyncIntentService() {
            super("SyncIntentService");
        }*/
    File localFile;
    Gson gson;
    double TOTAL_DEVICE_SMS;
    NotificationManagerCompat notificationManager;
    NotificationCompat.Builder builder;
    int PROGRESS_MAX = 100;
    int PROGRESS_CURRENT = 0;
    PowerManager.WakeLock wakeLock;
    File cache_temp1;
    File cache_temp2;
    SharedPreferences sharedPrefAppGeneral;
    // Trace myTrace;


    /**
     * Convenience method for enqueuing work in to this service.
     */
    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, ApplyCloudChangesService.class, JOB_ID, work);
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
    protected void onHandleWork(Intent intent) {
        // We have received work to do.  The system or framework is already
        // holding a wake lock for us at this point, so we can just go.
        Log.i("SimpleJobIntentService", "Executing work: " + intent);
     /*   String label = intent.getStringExtra("label");
        if (label == null) {
            label = intent.toString();
        }
        toast("Executing: " + label);
        for (int i = 0; i < 5; i++) {
            Log.i("SimpleJobIntentService", "Running service " + (i + 1)
                    + "/5 @ " + SystemClock.elapsedRealtime());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
        Log.i("SimpleJobIntentService", "Completed service @ " + SystemClock.elapsedRealtime());
   */

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // toast("All work complete");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, getString(R.string.DATABASE_CLOUD_SMS_DB)).allowMainThreadQueries()
                .fallbackToDestructiveMigration().build();


    }


    /*
    final Handler mHandler = new Handler();

    // Helper for showing tests
    void toast(final CharSequence text) {
        mHandler.post(new Runnable() {
            @Override public void run() {
                Toast.makeText(ApplyCloudChangesService.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }
    */

    public void DownloadFromCloud() {
        builder.setContentText("Syncing with cloud")
                .setProgress(0, 0, true);
        notificationManager.notify(001, builder.build());
        if (isNetworkAvailable()) {

            new Thread(new Runnable() {
                public void run() {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

                    Log.d(TAG, "DownloadFromCloud");
                    StorageReference mStorageRef;
                    mStorageRef = FirebaseStorage.getInstance().getReference();
                    StorageReference riversRef = mStorageRef.child(BackupStorageDB);
                    localFile = null;
                    gson = new Gson();

                    try {
                        tmpList.clear();
                        tmpList = (LinkedHashSet<HashMap<String, String>>) Function.readCachedFile(mContext, getString(R.string.file_device_sms));
                        Log.d(TAG, "DownloadFromCloud: tmpList.clear() ");
                    } catch (Exception e) {
                        Log.d(TAG, "DownloadFromCloud: ERROR " + e);
                        Crashlytics.logException(e);

                    }

                    try {
                        localFile = File.createTempFile("smscloud", "backup");
                        Log.d(TAG, "DownloadFromCloud: localFile smscloud.backup");
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


                                    File unziped = unzipFile(localFile);
                                    Log.d(TAG, "onSuccess: Unziped: " + unziped.getPath());
                                    String JsonStr = ConvertFileToStrng(unziped);
                                    Type type = new TypeToken<LinkedHashSet<HashMap<String, String>>>() {
                                    }.getType();

                                    //   JsonReader reader = new JsonReader(new StringReader(JsonStr));
                                    //   reader.setLenient(true);

                                    LinkedHashSet<HashMap<String, String>> jj = gson.fromJson(JsonStr, type);
                                    smsList.addAll(jj);

                                    LinkedHashSet<HashMap<String, String>> CleanHash = new LinkedHashSet<>(smsList);
                                    //     CleanHash.addAll(smsList);
                                    //CleanHash = RemoveDuplicateHashMaps(smsList);

                                    //CleanHash =  smsList;
                                    //       UploadToCloud(CleanHash);

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
                            LinkedHashSet<HashMap<String, String>> CleanHash = new LinkedHashSet<>(smsList);
//CleanHash.addAll(smsList);

                            //       UploadToCloud(CleanHash);

                        }
                    });

                }
            }).start();


        } else {
            cancelAllNotification();
            stopSelf();
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

    void setNotification() {

        builder = new NotificationCompat.Builder(this, "001")
                .setSmallIcon(R.drawable.app_logo)
                .setContentTitle("Auto-Backup")
                .setContentText("Syncing Messages....")
                .setOngoing(true)
                .setProgress(PROGRESS_MAX, PROGRESS_CURRENT, true)
                .setNotificationSilent()
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.FLAG_ONGOING_EVENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Auto Backup & Sync";
            String description = "Syncing Messages..";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("001", name, importance);
            channel.setDescription(description);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

        }

        notificationManager = NotificationManagerCompat.from(this);
        builder.setContentText("Preparing...");

// notificationId is a unique int for each notification that you must define
        notificationManager.notify(001, builder.build());


    }


    private boolean isNetworkAvailable() {
        return true;
       /* try {
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

}
