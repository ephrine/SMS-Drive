package devesh.ephrine.backup.sms.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
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
import com.lifeofcoding.cacheutlislibrary.CacheUtils;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import devesh.ephrine.backup.sms.Function;
import devesh.ephrine.backup.sms.MapComparator;
import devesh.ephrine.backup.sms.R;
import devesh.ephrine.backup.sms.room.AppDatabase;

public class DownloadCloudMessagesService extends Service {
    String TAG = "Cloud_MSG_Service";
    boolean isSubscribed;
    String UserUID;
    //   HashMap<String, ArrayList<HashMap<String, String>>> SmsList = new HashMap<>();

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
    private FirebaseAuth mAuth;

    public DownloadCloudMessagesService() {
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
        wakeLock.release();
    }
    SharedPreferences sharedPrefAppGeneral;

    @Override
    public void onCreate() {
        super.onCreate();

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                TAG + "::MyWakelockTag");
        wakeLock.acquire();

        Log.d(TAG, "downloadCloudSMS:  downloadCloudSMS()");

        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, getString(R.string.DATABASE_CLOUD_SMS_DB)).build();


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Crashlytics.setUserIdentifier(user.getUid());

            UserUID = user.getPhoneNumber().replace("+", "x");

            AppStart();
        }
        sharedPrefAppGeneral = PreferenceManager.getDefaultSharedPreferences(this);



    }

    void AppStart() {
        String sub = sharedPrefAppGeneral.getString(getString(R.string.cache_Sub_isSubscribe), "0");
if(sub.equals("1")){
    isSubscribed=true;
}else {
    isSubscribed=false;
}


        if (isSubscribed) {
            Log.d(TAG, "downloadCloudSMS:  downloadCloudSMS() isSubscribed");

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

                                    try {
                                        //     AddSmsDB addSmsDB = new AddSmsDB();
                                        //     addSmsDB.execute();

                                    } catch (Exception e) {
                                        Log.e(TAG, "AppStart:  #45631 ", e);
                                    }


                                    //  ArrayList<HashMap<String, String>> CleanHash = new ArrayList<>();
                                    //  CleanHash = RemoveDuplicateHashMaps(CloudSms);

                                    //  CloudSms.clear();
                                    // CloudSms = CleanHash;
                                    //CleanHash =  smsList;
                                    //    UploadToCloud(CleanHash);

                                    //Unzip File


                                    Log.d(TAG, "onDataChange: END of CLOUD SMS");
                                    Collections.sort(CloudSms, new MapComparator(Function.KEY_TIMESTAMP, "dsc")); // Arranging sms by timestamp decending

                                    try {
                                        Function.createCachedFile(DownloadCloudMessagesService.this, getString(R.string.file_cloud_sms), CloudSms);
                                        Log.d(TAG, "onDataChange: createCachedFile file_cloud_sms ");

                                    } catch (Exception e) {
                                        Log.d(TAG, "onDataChange: ERROR #56 : " + e);
                                        Crashlytics.logException(e);

                                    }
                                    CloudThreadSms = CloudSms;
                                    Collections.sort(CloudThreadSms, new MapComparator(Function.KEY_TIMESTAMP, "dsc")); // Arranging sms by timestamp decending
                                    ArrayList<HashMap<String, String>> purified = Function.removeDuplicates(CloudThreadSms); // Removing duplicates from inbox & sent
                                    CloudThreadSms.clear();
                                    CloudThreadSms.addAll(purified);
                                    try {
                                        Function.createCachedFile(DownloadCloudMessagesService.this, getString(R.string.file_cloud_thread), CloudSms);
                                        Log.d(TAG, "onDataChange: createCachedFile file_cloud_thread ");
                                        //   stopSelf();
                                    } catch (Exception e) {
                                        Log.d(TAG, "onDataChange: ERROR #5600 : " + e);
                                        Crashlytics.logException(e);
                                        // stopSelf();
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

                    wakeLock.release();

                    Intent intent = new Intent(DownloadCloudMessagesService.this, CloudSMS2DBService.class);

                    //  String message = editText.getText().toString();
                    //intent.putExtra(EXTRA_MESSAGE, message);
                    startActivity(intent);

                    //    UploadToCloud(smsList);

                }
            });


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


}
