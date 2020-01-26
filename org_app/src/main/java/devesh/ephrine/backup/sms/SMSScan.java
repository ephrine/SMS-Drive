package devesh.ephrine.backup.sms;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import devesh.ephrine.backup.sms.tools.Function;
import io.fabric.sdk.android.Fabric;

public class SMSScan {

    final String TAG = "SMSScan";
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


    public SMSScan(Context context) {
        mContext = context;
        Fabric.with(mContext, new Crashlytics());

        iThread = new HashMap<>();
        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();

        isSubscribed = true;
        sharedPrefAutoBackup = PreferenceManager.getDefaultSharedPreferences(mContext /* Activity context */);
        SMSAutoBackup = sharedPrefAutoBackup.getBoolean(mContext.getResources().getString(R.string.settings_sync), false);

    }

    public void ScanNow() {
        isFinished = false;
        //  getSMS();

        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.READ_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            if (user != null && isSubscribed) {
                UserUID = user.getPhoneNumber().replace("+", "x");
                //Download Full Backup First to Prevent DataLoss
                SMSBackupDB = database.getReference("/users/" + UserUID + "/sms");
            //    SMSScanDevice();

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
                                Log.d(TAG, "onDataChange: threadName: " + threadName);

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


    void GetThread(DataSnapshot postSnapshot, String threadName) {

        for (DataSnapshot DS : postSnapshot.getChildren()) {

            //   String msg = DS.getKey();
            // String MsgBody = DS.child("body").getValue().toString();

            //---

            //String formattedDate = new SimpleDateFormat("MM/dd/yyyy").format(date);
            String MsgText = DS.child("body").getValue().toString();
            String folder = DS.child("folder").getValue().toString();

  /*          SMS.put("msg", MsgText);
            SMS.put("time", formattedDate);
            SMS.put("folder", folder);
*/
            //  Log.d(TAG, "onDataChange: msg:" + msg + "\nMSG: " + MsgBody);
            Log.d(TAG, "onDataChange: msg:");

            String MsgTime = DS.child("date").getValue().toString();
            Date date = new Date(Long.parseLong(MsgTime));
            String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);

            HashMap<String, String> SMS = new HashMap<>();
            SMS.put("date", DS.child("date").getValue().toString());
            SMS.put("Formatdate", formattedDate);
            SMS.put("body", DS.child("body").getValue().toString());
            SMS.put("address", DS.child("address").getValue().toString());
            SMS.put("folder", DS.child("folder").getValue().toString());
            SMS.put("dd", new SimpleDateFormat("dd").format(date));
            SMS.put("mm", new SimpleDateFormat("MM").format(date));
            SMS.put("yyyy", new SimpleDateFormat("yyyy").format(date));
            SMS.put("hh", new SimpleDateFormat("hh").format(date));
            SMS.put("min", new SimpleDateFormat("mm").format(date));


            if (iThread.containsKey(threadName)) {
                iThread.get(threadName).add(SMS);
            } else {
                ArrayList<HashMap<String, String>> temp1 = new ArrayList<>();
                temp1.add(SMS);
                iThread.put(threadName, temp1);
            }

        }

    }

    //SMS Scan
    void getSMS() {
        // getSMSOutbox();

        //    Log.d(TAG, "getSMS: SMSBackup: " + SMSBackup);
        Cursor cursor = mContext.getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);

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

        sms1();


        // public static final String INBOX = "content://sms/inbox";
// public static final String SENT = "content://sms/sent";
// public static final String DRAFT = "content://sms/draft";


    }

    void sms1() {
        // Write a message to the database

        Uri smsUri = Uri.parse("content://sms/inbox");
        Cursor cursor = mContext.getContentResolver().query(smsUri, null, null, null, null);

        int i = cursor.getCount();

        int ii = 0;
        Log.d(TAG, "sms1: Cursor Count: " + i);
        while (cursor.moveToNext()) {
            ii++;


            HashMap<String, String> sms = new HashMap<>();

            String body = cursor.getString(cursor.getColumnIndex("body"));
            String address = cursor.getString(cursor.getColumnIndex("address"));
            String xdate = cursor.getString(cursor.getColumnIndex("date"));

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
                Log.d(TAG, "-------New SMS Algo END .:\n iThread:" + iThread.toString());
                //    SMSBackupDB.setValue(iThread);
                getSMSOutbox();

            }
            //  Log.d(TAG,"-------New SMS Algo:\n iThread:"+iThread);

        }
    }

    void getSMSOutbox() {
        Log.d(TAG, "getSMS Sent: SMSBackup: ");
        Cursor cursor = mContext.getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, null);

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

        sms2();


    }

    void sms2() {

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
                if (SMSAutoBackup) {
                    SMSBackupDB.setValue(iThread);
                    Log.d(TAG, "sms1 sent: END ---------" + ii + "\n SMS:Backup ");
                    // SMSDB = database.getReference(DBRoot + "/users/" + UserUID + "/sms/"+address+"/");
                    //  SMSDB.setValue(sms);

                }
            }
        }
    }

    //New Code to get SMS


    ArrayList<HashMap<String, String>> smsList = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> customList = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> tmpList = new ArrayList<HashMap<String, String>>();
    String name;

    void SMSScanDevice(){

        try {
            Uri uriInbox = Uri.parse("content://sms/inbox");
            Cursor inbox = mContext.getContentResolver().query(uriInbox, null, "thread_id=" + null, null, null);
            Uri uriSent = Uri.parse("content://sms/sent");
            Cursor sent = mContext.getContentResolver().query(uriSent, null, "thread_id=" + null, null, null);
            Cursor c = new MergeCursor(new Cursor[]{inbox,sent}); // Attaching inbox and sent sms

            if (c.moveToFirst()) {
                for (int i = 0; i < c.getCount(); i++) {
                    String phone = "";
                    String _id = c.getString(c.getColumnIndexOrThrow("_id"));
                    String thread_id = c.getString(c.getColumnIndexOrThrow("thread_id"));
                    String msg = c.getString(c.getColumnIndexOrThrow("body"));
                    String type = c.getString(c.getColumnIndexOrThrow("type"));
                    String timestamp = c.getString(c.getColumnIndexOrThrow("date"));
                    phone = c.getString(c.getColumnIndexOrThrow("address"));

                    tmpList.add(Function.mappingInbox(_id, thread_id, name, phone, msg, type, timestamp, Function.converToTime(timestamp)));
                    Log.d(TAG, "sms:" +tmpList);

                    c.moveToNext();
                    if (SMSAutoBackup) {
                        SMSBackupDB.setValue(tmpList);
                        Log.d(TAG, "sms1 sent: END ---------SMS:Backup ");
                        // SMSDB = database.getReference(DBRoot + "/users/" + UserUID + "/sms/"+address+"/");
                        //  SMSDB.setValue(sms);

                    }
                }
            }
            c.close();

        }catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
