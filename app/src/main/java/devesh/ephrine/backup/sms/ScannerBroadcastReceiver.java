package devesh.ephrine.backup.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lifeofcoding.cacheutlislibrary.CacheUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import devesh.ephrine.backup.sms.services.SyncIntentService;
import io.fabric.sdk.android.Fabric;

public class ScannerBroadcastReceiver extends BroadcastReceiver {
    // final String DBRoot = "SMSDrive/";
    final String TAG = "BroadcastReceiver";
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

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //  throw new UnsupportedOperationException("Not yet implemented");
        mContext = context;
        iThread = new HashMap<>();
        sharedPrefAutoBackup = PreferenceManager.getDefaultSharedPreferences(mContext /* Activity context */);
        SMSAutoBackup = sharedPrefAutoBackup.getBoolean(mContext.getResources().getString(R.string.settings_sync), false);
        Fabric.with(mContext, new Crashlytics());

        mContext = context;
        iThread = new HashMap<>();
        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();

        try {
            if (CacheUtils.readFile(context.getString(R.string.cache_Sub_isSubscribe)).toString().equals("1")) {
                isSubscribed = true;
            } else {
                isSubscribed = false;
            }
        } catch (Exception e) {
            isSubscribed = false;
        }
        sharedPrefAutoBackup = PreferenceManager.getDefaultSharedPreferences(mContext /* Activity context */);
        SMSAutoBackup = sharedPrefAutoBackup.getBoolean(mContext.getResources().getString(R.string.settings_sync), false);

        isFinished = false;

        if (isSubscribed && SMSAutoBackup) {
            Log.d(TAG, "onReceive: ScannerBroadcastReceiver SYNC STARTED ");
            //   Intent intent1 = new Intent(mContext, SyncIntentService.class);
            // context.startService(intent1);
            SyncIntentService.enqueueWork(mContext, new Intent());

          /*       new Thread(new Runnable() {
                public void run() {
                    // a potentially time consuming task
                    SMSScan s = new SMSScan(mContext);
                    s.ScanNow();

                }
            }).start();


       
            OneTimeWorkRequest syncWorkRequest = new OneTimeWorkRequest.Builder(SyncWorkManager.class)
                    .build();
            WorkManager.getInstance(this).enqueue(syncWorkRequest);*/
            Log.d(TAG, "onReceive: Syncing...");
        } else {
            Log.d(TAG, "onReceive: Please Subscribe before Sync ");
        }

        //  getSMS();
        /**      if (isDefaultApp) {

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
        @Override public void onDataChange(DataSnapshot dataSnapshot) {
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

        DataSnapshot DS = postSnapshot;


        Log.d(TAG, "GetThread: DS :  DS.getChildren().toString()");

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

        @Override public void onCancelled(DatabaseError error) {
        // Failed to read value
        Log.w(TAG, "Failed to read value.", error.toException());
        }
        });


         }

         }

         }

         */
        //     iThread=smsscan.GetList();


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


}
