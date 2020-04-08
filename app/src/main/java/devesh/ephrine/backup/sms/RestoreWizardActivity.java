package devesh.ephrine.backup.sms;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Telephony;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import io.fabric.sdk.android.Fabric;

public class RestoreWizardActivity extends AppCompatActivity {
    public String RESTORE_PROGRESS;
    //boolean isDefaultSmsApp;
    String UserUID;
    String TAG = "RestoreWizardActivity ";
    DatabaseReference SMSBackupDB;
    FirebaseUser user;
    FirebaseDatabase database;
    ArrayList<HashMap<String, String>> CloudSms = new ArrayList<>();
    ArrayList<HashMap<String, String>> DeviceSMS = new ArrayList<>();
    LottieAnimationView lottieAnimationView1;
    LottieAnimationView lottieAnimationView2;
    LottieAnimationView lottieSyncing;
    LottieAnimationView lottieDoneAnim;
    ImageView smsbotIMG;
    LinearLayout LLDefaultSmsAppStep1;
    String OldDefaultSMSApp;
    boolean isRestoreInProgress;
    TextView RestoreProgressTextView;
    LinearLayout LLButtonsR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore_wizard);
        Fabric.with(this, new Crashlytics());

        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        isRestoreInProgress = false;
        lottieAnimationView1 = findViewById(R.id.animation_view);
        lottieAnimationView2 = findViewById(R.id.animation_view2);
        lottieSyncing = findViewById(R.id.lottiesyncanim);
        lottieDoneAnim = findViewById(R.id.lottiedoneanim);
        RestoreProgressTextView = findViewById(R.id.textView6ProgressStatus);
        lottieSyncing.setVisibility(View.GONE);
        RestoreProgressTextView.setVisibility(View.GONE);
        LLDefaultSmsAppStep1 = findViewById(R.id.LLDefaultSmsAppStep1);

        smsbotIMG = findViewById(R.id.imageView3SMSBot);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final String myPackageName = getPackageName();
            OldDefaultSMSApp = Telephony.Sms.getDefaultSmsPackage(this);

            if (!Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName)) {
                //isDefaultSmsApp = false;
                lottieAnimationView1.setVisibility(View.VISIBLE);
                lottieAnimationView2.setVisibility(View.INVISIBLE);
            } else {
                //  isDefaultSmsApp = true;
                lottieAnimationView1.setVisibility(View.INVISIBLE);
                lottieAnimationView2.setVisibility(View.VISIBLE);
                smsbotIMG.setVisibility(View.VISIBLE);
                LLDefaultSmsAppStep1.setVisibility(View.GONE);
            }
        } else {
            // isDefaultSmsApp = true;
            lottieAnimationView1.setVisibility(View.INVISIBLE);
            lottieAnimationView2.setVisibility(View.VISIBLE);
            smsbotIMG.setVisibility(View.VISIBLE);
            LLDefaultSmsAppStep1.setVisibility(View.GONE);
            // saveSms("111111", "mmmmssssggggg", "0", "", "inbox");
        }
        Log.d(TAG, "onCreate: isDefault SMS Handler: " + isDefaultSmsApp());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            smsbotIMG.setVisibility(View.GONE);
            LLDefaultSmsAppStep1.setVisibility(View.VISIBLE);

        } else {
            smsbotIMG.setVisibility(View.VISIBLE);
            LLDefaultSmsAppStep1.setVisibility(View.GONE);

        }


    }

    @Override
    protected void onResume() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final String myPackageName = getPackageName();
            if (!Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName)) {
                if (!isRestoreInProgress) {
                    //  isDefaultSmsApp = false;
                    lottieAnimationView1.setVisibility(View.VISIBLE);
                    lottieAnimationView2.setVisibility(View.INVISIBLE);
                }

            } else {
                if (!isRestoreInProgress) {
                    //isDefaultSmsApp = true;
                    lottieAnimationView1.setVisibility(View.INVISIBLE);
                    lottieAnimationView2.setVisibility(View.VISIBLE);

                    smsbotIMG.setVisibility(View.VISIBLE);
                    LLDefaultSmsAppStep1.setVisibility(View.GONE);

                }

            }
        } else {
            if (!isRestoreInProgress) {
                // isDefaultSmsApp = true;
                lottieAnimationView1.setVisibility(View.INVISIBLE);
                lottieAnimationView2.setVisibility(View.VISIBLE);
                // saveSms("111111", "mmmmssssggggg", "0", "", "inbox");
                smsbotIMG.setVisibility(View.VISIBLE);
                LLDefaultSmsAppStep1.setVisibility(View.GONE);

            }

        }

        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                final String myPackageName = getPackageName();
                if (!Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName)) {
                    //  isDefaultSmsApp = false;
                    lottieAnimationView1.setVisibility(View.VISIBLE);
                    lottieAnimationView2.setVisibility(View.INVISIBLE);
                } else {
                    //isDefaultSmsApp = true;
                    lottieAnimationView1.setVisibility(View.INVISIBLE);
                    lottieAnimationView2.setVisibility(View.VISIBLE);
                }
            } else {
                // isDefaultSmsApp = true;
                lottieAnimationView1.setVisibility(View.INVISIBLE);
                lottieAnimationView2.setVisibility(View.VISIBLE);
                // saveSms("111111", "mmmmssssggggg", "0", "", "inbox");
            }

        }

    }

    public void StartSaving(View v) {
        LLButtonsR = findViewById(R.id.LLButtonsR);
        if (isDefaultSmsApp() && !isRestoreInProgress) {
            LLButtonsR.setVisibility(View.GONE);
            isRestoreInProgress = true;
            RestoreProgressTextView.setVisibility(View.VISIBLE);
            RestoreProgressTextView.setText("Preparing.....");
            //  DownloadCloud();
            new RestoreTask().execute("url1", "url2", "url3");
          /*  runOnUiThread(new Runnable() {
                public void run() {
                }
            });*/
            //  new RestoreTask().execute("url1", "url2", "url3");

            lottieSyncing.setVisibility(View.VISIBLE);
            lottieAnimationView2.setVisibility(View.INVISIBLE);
            smsbotIMG.setVisibility(View.GONE);
            Toast.makeText(this, "Restoring... Please wait", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "First set as Default SMS App", Toast.LENGTH_SHORT).show();
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
                getContentResolver().insert(uri, values);
            } else {
                getContentResolver().insert(Uri.parse("content://sms/" + folderName), values);
            }

            ret = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            ret = false;
            Crashlytics.logException(ex);

        }
        return ret;
    }

    //SMS Scan
    void getSMS() {
        // getSMSOutbox();

        //    Log.d(TAG, "getSMS: SMSBackup: " + SMSBackup);
        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);

        if (cursor.moveToFirst()) { // must check the result to prevent exception
            do {
                String msgData = "";
                String no = "";
                for (int idx = 0; idx < cursor.getColumnCount(); idx++) {
                    msgData += " " + cursor.getColumnName(idx) + ":" + cursor.getString(idx);
                    //                Log.d(TAG, "getSMS: \n" + msgData);
                }
                // use msgData
            } while (cursor.moveToNext());
        } else {
            // empty box, no SMS
        }

        //    sms1();


        // public static final String INBOX = "content://sms/inbox";
// public static final String SENT = "content://sms/sent";
// public static final String DRAFT = "content://sms/draft";


    }


    public void finalout() {
        try {
            lottieSyncing.setVisibility(View.GONE);
            lottieDoneAnim.setVisibility(View.VISIBLE);
            lottieDoneAnim.playAnimation();
            Toast.makeText(RestoreWizardActivity.this, "Success: Restored Messages", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Log.d(TAG, "finalout: ERROR #56657 " + e);
            Crashlytics.logException(e);

        }
        //  RestoreProgressTextView.setText("Done");
//RestoreWizardActivity.this.finish();
        RestoreProgressTextView.setText("Success: Restored Messages ");
    }

    void GetThread(DataSnapshot postSnapshot, String threadName) {

        for (DataSnapshot DS : postSnapshot.getChildren()) {

            //   Log.d(TAG, "GetThread: DS : "+DS.getChildren().toString());

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


            CloudSms.add(SMS);
            Log.d(TAG, "onDataChange: msg:" + SMS + "\n-------------------------\n");

           /* if (iThread.containsKey(threadName)) {
                iThread.get(threadName).add(SMS);
            } else {
                ArrayList<HashMap<String, String>> temp1 = new ArrayList<>();
                temp1.add(SMS);
                iThread.put(threadName, temp1);
            }*/

        }

    }

    boolean isDefaultSmsApp() {
        boolean s = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final String myPackageName = getPackageName();
            if (!Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName)) {
                s = false;
                lottieAnimationView1.setVisibility(View.VISIBLE);
                lottieAnimationView2.setVisibility(View.INVISIBLE);
            } else {
                s = true;
                lottieAnimationView1.setVisibility(View.INVISIBLE);
                lottieAnimationView2.setVisibility(View.VISIBLE);
            }
        } else {
            s = true;
            lottieAnimationView1.setVisibility(View.INVISIBLE);
            lottieAnimationView2.setVisibility(View.VISIBLE);
            // saveSms("111111", "mmmmssssggggg", "0", "", "inbox");
        }

        return s;
    }

    public void setDefaultSmsApp(View v) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final String myPackageName = getPackageName();
            if (!Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName)) {
  /*              Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
                startActivity(intent);
*/

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
                    startActivity(intent);

                } else {
                    Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                    intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
                    startActivity(intent);
                }

                /*Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, myPackageName);
            startActivityForResult(intent, 1);  */
                //   isDefaultSmsApp = true;
                //    lottieAnimationView1.setVisibility(View.INVISIBLE);
                //    lottieAnimationView2.setVisibility(View.VISIBLE);
                Log.d(TAG, "setDefaultSmsApp: setting default SMS handler");
            } else {
                //    isDefaultSmsApp = true;
                //    lottieAnimationView1.setVisibility(View.INVISIBLE);
                //      lottieAnimationView2.setVisibility(View.VISIBLE);

            }

        } else {
            //  isDefaultSmsApp = true;
            // saveSms("111111", "mmmmssssggggg", "0", "", "inbox");
            //   lottieAnimationView1.setVisibility(View.INVISIBLE);
//        lottieAnimationView2.setVisibility(View.VISIBLE);

        }


    }

    class RestoreTask extends AsyncTask<String, String, String> {
        File localFile;
        Gson gson;
        String BackupStorageDB;

        protected String doInBackground(String... urls) {
            Log.d(TAG, "doInBackground: RestoreTask()");
            DownloadCloud();
            return "Done";
        }

        protected void onProgressUpdate(String... progress) {

            Log.d(TAG, "onProgressUpdate: RestoreTask()");
        }

        protected void onPostExecute(String result) {
            //    lottieSyncing.setVisibility(View.GONE);
            //    lottieDoneAnim.setVisibility(View.VISIBLE);
            //   lottieDoneAnim.playAnimation();
            //  Toast.makeText(RestoreWizardActivity.this, "Success: Restored Messages", Toast.LENGTH_LONG).show();
            Log.d(TAG, "onPostExecute: RestoreTask()");
        }

        void DownloadCloud() {
            RestoreProgressTextView.setText("Downloading....");


            user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                UserUID = user.getPhoneNumber().replace("+", "x");

                BackupStorageDB = "SMSDrive/Users/" + UserUID + "/backup/file_cloud_sms.zip";

                StorageReference mStorageRef;
                mStorageRef = FirebaseStorage.getInstance().getReference();
                StorageReference riversRef = mStorageRef.child(BackupStorageDB);
                localFile = null;
                gson = new Gson();

                ArrayList<HashMap<String, String>> tmpList = null;
                try {
                    tmpList.clear();
                    tmpList = (ArrayList<HashMap<String, String>>) Function.readCachedFile(RestoreWizardActivity.this, getString(R.string.file_device_sms));

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

                                        // ArrayList<HashMap<String, String>> CleanHash = new ArrayList<>();
                                        // CleanHash = RemoveDuplicateHashMaps(CloudSms);
                                        // CloudSms.clear();
                                        // CloudSms=CleanHash;

                                        Log.d(TAG, "onDataChange: END of CLOUD SMS");
                                        sms1();

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
                        Log.d(TAG, "onFailure: ERROR " + exception);
                        //  sms1();


                    }

                }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        //calculating progress percentage
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        //displaying percentage in progress dialog
                        //   yourProgressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                        RestoreProgressTextView.setText("Downloading: " + ((int) progress) + "%...");

                    }
                });


  /*
            SMSBackupDB = database.getReference("/users/" + UserUID + "/sms/backup");
            Log.d(TAG, "DownloadCloud: UserID:" + UserUID);
            SMSBackupDB.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    //String value = dataSnapshot.getValue(String.class);
                    // Log.d(TAG, "Value is: " + value);

                    if (dataSnapshot.exists()) {

                        long total = dataSnapshot.getChildrenCount();
                        long i;
                        i = 0;
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            // TODO: handle the post
                            i = i + 1;

                            String threadName = postSnapshot.getKey();
                            Log.d(TAG, "onDataChange: threadName: " + threadName);

                            String phone = postSnapshot.child(Function.KEY_PHONE).getValue(String.class);

                            String name = postSnapshot.child(Function.KEY_NAME).getValue(String.class);
                            String _id = postSnapshot.child(Function._ID).getValue(String.class);
                            String thread_id = postSnapshot.child(Function.KEY_THREAD_ID).getValue(String.class);
                            String msg = postSnapshot.child(Function.KEY_MSG).getValue(String.class);
                            String type = postSnapshot.child(Function.KEY_TYPE).getValue(String.class);
                            String timestamp = postSnapshot.child(Function.KEY_TIMESTAMP).getValue(String.class);
                            String time = postSnapshot.child(Function.KEY_TIME).getValue(String.class);

                            HashMap<String, String> SMS = new HashMap<>();
                            SMS.put(Function._ID, _id);
                            SMS.put(Function.KEY_THREAD_ID, thread_id);
                            SMS.put(Function.KEY_PHONE, phone);
                            SMS.put(Function.KEY_MSG, msg);
                            SMS.put(Function.KEY_TYPE, type);
                            SMS.put(Function.KEY_TIMESTAMP, timestamp);
                            SMS.put(Function.KEY_TIME, time);
                            SMS.put(Function.KEY_NAME, name);
                            CloudSms.add(SMS);
                            Log.d(TAG, "onDataChange: msg:" + SMS + "\n-------------------------\n");


                            //   GetThread(postSnapshot, threadName);
                            Log.d(TAG, "i:" + i + "\n Total:" + total);
                            if (i == total) {
                                sms1();
                            }
                        }

                    } else {
                        sms1();
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

        ArrayList<HashMap<String, String>> RemoveDuplicateHashMaps(ArrayList<HashMap<String, String>> X) {
            Log.d(TAG, "RemoveDuplicateHashMaps: Removing Duplicate...");
            final ArrayList<HashMap<String, String>> A = X;
            ArrayList<HashMap<String, String>> TempCleanHash = new ArrayList<>();


            // ArrayList<HashMap<String, String>> gpList = A;
            for (int i = 0; i < A.size(); i++) {
                boolean available = false;
                for (int j = 0; j < TempCleanHash.size(); j++) {
                    if (TempCleanHash.get(j).get(Function.KEY_MSG) == A.get(i).get(Function.KEY_MSG)
                            && TempCleanHash.get(j).get(Function.KEY_TIMESTAMP) == A.get(i).get(Function.KEY_TIMESTAMP)
                            && TempCleanHash.get(j).get(Function.KEY_PHONE) == A.get(i).get(Function.KEY_PHONE)
                            && TempCleanHash.get(j).get(Function.KEY_TYPE) == A.get(i).get(Function.KEY_TYPE)
                    ) {
                        available = true;
                        Log.d(TAG, "RemoveDuplicateHashMaps: Duplicate Found");
                        break;
                    }
                }

                if (!available) {
                    Log.d(TAG, "RemoveDuplicateHashMaps: Added Non-Duplicate");
                    TempCleanHash.add(A.get(i));
                }
            }


            return TempCleanHash;
        }


        void sms1() {
            // Write a message to the database
            RestoreProgressTextView.setText("Preparing Messages...");
            Uri smsUri = Uri.parse("content://sms/inbox");
            Cursor cursor = getContentResolver().query(smsUri, null, null, null, null);

            int i = cursor.getCount();

            int ii = 0;
            Log.d(TAG, "sms1: Cursor Count: " + i);
            while (cursor.moveToNext()) {
                ii++;

                RestoreProgressTextView.setText("Preparing Inbox Messages: " + ii + "/" + i);


                HashMap<String, String> sms = new HashMap<>();

                //     String body = cursor.getString(cursor.getColumnIndex("body"));
                //     String address = cursor.getString(cursor.getColumnIndex("address"));
                //   String xdate = cursor.getString(cursor.getColumnIndex("date"));

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
                    name = Function.getContactbyPhoneNumber(getApplicationContext(), cursor.getString(cursor.getColumnIndexOrThrow("address")));
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

                DeviceSMS.add(msgg);



  /*          address = address.replace("+", "x");
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
*/

  /*          if (iThread.containsKey(address)) {
                iThread.get(address).add(sms);

            } else {
                ArrayList<HashMap<String, String>> temp1 = new ArrayList<>();
                temp1.add(sms);
                iThread.put(address, temp1);
            }
*/

                //       SMSBackupDB.child(address).child(xdate).setValue(body);

                //         Log.d(TAG, "getSMS:\n \nbody:" + body + "\naddress:" + address + "\nDate: " + formattedDate);
                //    Log.d(TAG, "getSMS:\n \nSMS LIST:" + SmsList);

                if (ii == i) {
                    Log.d(TAG, "sms1: END ---------" + ii + "\n SMS: ");
                    Log.d(TAG, "-------New SMS Algo END .:\n iThread:");
                    //    SMSBackupDB.setValue(iThread);
                    getSMSOutbox();

                }
                //  Log.d(TAG,"-------New SMS Algo:\n iThread:"+iThread);

            }

            if (i == 0) {
                getSMSOutbox();

            }
        }

        void getSMSOutbox() {
  /*      Log.d(TAG, "getSMS Sent: SMSBackup: ");
        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, null);

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
            RestoreProgressTextView.setText("Preparing Outbox Messages...");

            List<String> lstSms = new ArrayList<String>();
            Uri smsUri = Uri.parse("content://sms/sent");
            Cursor cursor = getContentResolver().query(smsUri, null, null, null, null);

            int i = cursor.getCount();

            int ii = 0;
            Log.d(TAG, "sms1 sent: Cursor Count: " + i);
            while (cursor.moveToNext()) {
                ii++;
                RestoreProgressTextView.setText("Preparing Outbox Messages: " + ii + "/" + i);

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
                    name = Function.getContactbyPhoneNumber(getApplicationContext(), cursor.getString(cursor.getColumnIndexOrThrow("address")));
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

                DeviceSMS.add(msgg);


                //          if (SMSAutoBackup) {
                // SMSBackupDB.setValue(iThread);
                //          SMSDB = database.getReference(DBRoot + "/users/" + UserUID + "/sms/"+address+"/").push();
                //        SMSDB.setValue(sms);


//            }


  /*          if (iThread.containsKey(address)) {
                iThread.get(address).add(sms);

            } else {
                ArrayList<HashMap<String, String>> temp1 = new ArrayList<>();
                temp1.add(sms);
                iThread.put(address, temp1);
            }
*/

                //    SMSBackupDB.child(address).child(xdate).setValue(body);


                //   Log.d(TAG, "getSMS Sent:\n \nbody:" + body + "\naddress:" + address + "\nDate: " + formattedDate);
                //   Log.d(TAG, "getSMS:\n \nSMS LIST:" + SmsList);

                if (ii == i) {
                    Log.d(TAG, "sms1 sent: END ---------" + ii + "\n SMS: ");
                    sortCloudSMS(CloudSms, DeviceSMS);
                    //  SMSBackupDB = database.getReference("/users/" + UserUID + "/sms/");

                    //    Map<String, Object> jj = new HashMap<>();
                    //         jj.put("backup", smsList);
                    //      SMSBackupDB.setValue(jj);
                    //        Toast.makeText(this, "Sync Complete", Toast.LENGTH_SHORT).show();
                    //          Log.d(TAG, "sms1 sent: END ---------" + ii + "\n SMS:Backup DONE  ");
                    //    long smsReceiveTime = System.currentTimeMillis();
                    //      Date date1 = new Date(smsReceiveTime);
                    //String formattedDate = new SimpleDateFormat("MM/dd/yyyy").format(date);
                    //        String formattedDate1 = new SimpleDateFormat("dd/MM/yyyy hh:mm").format(date1);

                    //        sharedPrefAutoBackup = PreferenceManager.getDefaultSharedPreferences(mContext /* Activity context */);
                    //        SharedPreferences.Editor editor = sharedPrefAutoBackup.edit();
                    //      editor.putString(mContext.getResources().getString(R.string.settings_pref_last_sync), formattedDate1);
                    //    editor.apply();

                    //        if (SMSAutoBackup) {
                    // SMSDB = database.getReference(DBRoot + "/users/" + UserUID + "/sms/"+address+"/");
                    //  SMSDB.setValue(sms);
                    //      }


                }
            }

            if (i == 0) {
                sortCloudSMS(CloudSms, DeviceSMS);

            }

        }

        void sortCloudSMS(ArrayList<HashMap<String, String>> c, ArrayList<HashMap<String, String>> d) {
            RestoreProgressTextView.setText("Preparing to Restore Messages");

            ArrayList<HashMap<String, String>> cloudsms = new ArrayList<>();
            cloudsms = c;

            ArrayList<HashMap<String, String>> devicesms = new ArrayList<>();
            devicesms = d;

            int t = devicesms.size() - 1;
            int tc = cloudsms.size() - 1;
            Log.d(TAG, "sortCloudSMS: Data: Device:" + t + "\nCloud:" + tc + "\n\ndevice sms:");
            int saved;
            saved = 0;

            int end = 0;
            for (int i = 0; i <= tc; i++) {
                end++;
                String msg = cloudsms.get(i).get(Function.KEY_MSG);
                String phone = cloudsms.get(i).get(Function.KEY_PHONE);
                String timestamp = cloudsms.get(i).get(Function.KEY_TIMESTAMP);

                RestoreProgressTextView.setText("Restoring Messages: " + i + "/" + tc);

                HashMap<String, String> c1 = new HashMap<>();
                c1 = cloudsms.get(i);
                if (devicesms.contains(c1)) {
                    Log.d(TAG, "sortCloudSMS: Identical Messages: " + c1.get(Function.KEY_TIMESTAMP) + "-------------\n");
                } else {
                    Log.d(TAG, "sortCloudSMS: Saving on Device: " + c1.get(Function.KEY_MSG) + "----------------\n");
                    String folder;
                    if (c1.get(Function.KEY_TYPE).equals("1")) {
                        folder = "inbox";
                    } else {
                        folder = "outbox";
                    }
                    saveSms(c1.get(Function.KEY_PHONE), c1.get(Function.KEY_MSG), "1", c1.get(Function.KEY_TIMESTAMP), folder);
                }

                //   Log.d(TAG, "sortCloudSMS: Sorting FOR LOOP: \nMSG:"+msg
                // +"\nphone:"+phone+"\ntS:"+timestamp);

  /*        if(msg!=null && phone!=null && timestamp!=null){
                Log.d(TAG, "sortCloudSMS: Sorting MSG "+i+"Phone:"+phone);
                for(int x=0;x<=t;x++){
                    String msg1=devicesms.get(x).get(Function.KEY_MSG);
                    String phone1=devicesms.get(x).get(Function.KEY_PHONE);
                    String timestamp1=devicesms.get(x).get(Function.KEY_TIMESTAMP);

                    if(msg1!=null && phone1!=null && timestamp1!=null){
                     //   Log.d(TAG, "sortCloudSMS: #3243: msg:"+msg1+"\nphone:"+phone1+"ts:"+timestamp1);

                        if(msg.equals(msg1)
                                //&& phone.equals(phone1)
                                && timestamp.equals(timestamp1)){
                            Log.d(TAG, "sortCloudSMS: Identical Messages: "+ phone1+"\n t:"+timestamp1);
                        }else {
                            saved=saved+1;
                            // Save into Device
                            Log.d(TAG, "sortCloudSMS: Saving on Device: "+phone1+"\n t:"+timestamp1+"\n saved:"+saved);
                        }
                    }else {
                        Log.e(TAG, "sortCloudSMS: Corrupted MSG !! ID:"+x );
                    }



                }



            }else{
                Log.e(TAG, "sortCloudSMS: Corrupted Cloud Message"+i );
            }
*/
                if (end == tc) {
                    Log.d(TAG, "\nsortCloudSMS: END OF SAVING -----------------");
//                    finalout();
                }
            }

            finalout();
        }


    }
  /*  private class RestoreTask extends AsyncTask<String, String, String> {
        protected String doInBackground(String... urls) {
            Log.d(TAG, "doInBackground: RestoreTask()");

            return "Done";
        }

        protected void onProgressUpdate(String... progress) {

            Log.d(TAG, "onProgressUpdate: RestoreTask()");
        }

        protected void onPostExecute(String result) {
        //    lottieSyncing.setVisibility(View.GONE);
        //    lottieDoneAnim.setVisibility(View.VISIBLE);
         //   lottieDoneAnim.playAnimation();
          //  Toast.makeText(RestoreWizardActivity.this, "Success: Restored Messages", Toast.LENGTH_LONG).show();

            Log.d(TAG, "onPostExecute: RestoreTask()");
        }



    }
*/

}
