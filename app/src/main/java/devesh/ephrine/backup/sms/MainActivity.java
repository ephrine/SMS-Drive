package devesh.ephrine.backup.sms;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import io.fabric.sdk.android.Fabric;


public class MainActivity extends AppCompatActivity {
    final int PERMISSION_SMS = 00000001;
    //   final String DBRoot = "SMSDrive/";
    public HashMap<String, ArrayList<HashMap<String, String>>> iThread;
    DatabaseReference SMSBackupDB;
    DatabaseReference UserDB;
    String TAG = "SMS Drive";
    String UserUID;
    String UserName;
    String UserEmail;
    //   HashMap<String, ArrayList<HashMap<String, String>>> SmsList = new HashMap<>();
    String UserAge;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FirebaseDatabase database;
    ArrayList<String> ThreadList;
    ArrayList<HashMap<String, String>> thread;
    SharedPreferences sharedPrefAutoBackup;
    boolean SMSAutoBackup;
    boolean isSubscribed;
    ProgressBar loadingCircle;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Fabric.with(this, new Crashlytics());

        isSubscribed = true;
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        loadingCircle = findViewById(R.id.progressBar1);

        iThread = new HashMap<>();


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Crashlytics.setUserIdentifier(user.getUid());

            UserUID = user.getPhoneNumber().replace("+", "x");
            SMSBackupDB = database.getReference("/users/" + UserUID + "/sms");

// Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_SMS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_SMS},
                        PERMISSION_SMS);
            } else {
                // Permission has already been granted
            }
            AppStart();

        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_SMS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    Toast.makeText(this, "Please Grant Permission otherwise App will not work", Toast.LENGTH_SHORT).show();
                    MainActivity.this.finish();

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        ///  FirebaseUser user = mAuth.getCurrentUser();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
        } else {
            Intent intent = new Intent(this, StartActivity.class);
            //  String message = editText.getText().toString();
            //intent.putExtra(EXTRA_MESSAGE, message);
            startActivity(intent);
            MainActivity.this.finish();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.syncnow:
                if (isSubscribed) {
                    SMSScan s = new SMSScan(this);
                    s.ScanNow();
                    Toast.makeText(this, "Syncing...", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Please Subscribe before Sync", Toast.LENGTH_LONG).show();
                }

                Log.d(TAG, "onOptionsItemSelected: Sync Now menu");
                return true;

            case R.id.tutorial:
                Log.d(TAG, "onOptionsItemSelected: Help Menu");
                return true;

            case R.id.settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                Log.d(TAG, "onOptionsItemSelected: Settings Menu");
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //SMS Scan
  /*  public void getSMS() {
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

        sms1();


        // public static final String INBOX = "content://sms/inbox";
// public static final String SENT = "content://sms/sent";
// public static final String DRAFT = "content://sms/draft";


    }
    void sms1() {
        // Write a message to the database

        Uri smsUri = Uri.parse("content://sms/inbox");
        Cursor cursor = getContentResolver().query(smsUri, null, null, null, null);

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
        Log.d(TAG, "getSMS Sent: SMSBackup: " + SMSBackup);
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

        sms2();


    }
    void sms2() {

        List<String> lstSms = new ArrayList<String>();
        Uri smsUri = Uri.parse("content://sms/sent");
        Cursor cursor = getContentResolver().query(smsUri, null, null, null, null);

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
                    SyncMessages();
                }

                //     LoadRecycleView();
                //  writeSettings();
            }
        }
    }
*/

    //Download SMS from Database
    void DownloadSMS() {
        ThreadList = new ArrayList<>();

        // Read from the database
        SMSBackupDB.addValueEventListener(new ValueEventListener() {
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

                        ThreadList.add(threadName);
                        //     GetThread(postSnapshot, threadName);
                        Log.d(TAG, "i:" + i + "\n Total:" + total);
                        if (i == total) {
                            LoadRecycleView(ThreadList);
                            loadingCircle.setVisibility(View.GONE);
                        }

                    }


                } else {
                    Log.d(TAG, "onDataChange: Backup Database EMPTY");
                    loadingCircle.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
                loadingCircle.setVisibility(View.GONE);

            }
        });


    }

    void LoadRecycleView(ArrayList<String> list) {
        recyclerView = findViewById(R.id.smsrecycle);
        layoutManager = new LinearLayoutManager(this);

        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        recyclerView.setLayoutManager(layoutManager);
        // specify an adapter (see also next example)

        SmsAdapter mAdapter = new SmsAdapter(MainActivity.this, list);

        recyclerView.setAdapter(mAdapter);

    }

    void GetThread(DataSnapshot postSnapshot, String threadName) {

        thread = new ArrayList<>();

        for (DataSnapshot DS : postSnapshot.getChildren()) {

            String msg = DS.getKey();
            String MsgBody = DS.child("body").getValue().toString();

            Log.d(TAG, "onDataChange: msg:" + msg + "\nMSG: " + MsgBody);
        }

    }

    void AppStart() {
        if (isSubscribed) {
            DownloadSMS();

        }

        sharedPrefAutoBackup = PreferenceManager.getDefaultSharedPreferences(this /* Activity context */);
        SMSAutoBackup = sharedPrefAutoBackup.getBoolean(getResources().getString(R.string.settings_sync), false);

        Switch SyncSwitch = findViewById(R.id.switch1);

        if (SMSAutoBackup && isSubscribed) {
            SyncSwitch.setChecked(true);
            FileAutoBackUpBroadCast();

        } else {
            SyncSwitch.setChecked(false);
        }

        SyncSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPrefAutoBackup.edit();
                if (isChecked) {
                    // The toggle is enabled
                    editor.putBoolean(getResources().getString(R.string.settings_sync), true);
                    SMSAutoBackup = true;
                    //  ScanNow();
                    if (isSubscribed) {
                        FileAutoBackUpBroadCast();
                    }
                } else {
                    // The toggle is disabled
                    editor.putBoolean(getResources().getString(R.string.settings_sync), false);
                    SMSAutoBackup = false;
                }
                editor.apply();

            }
        });


        UserDB = database.getReference("/users/" + UserUID + "/profile");
        UserDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                SharedPreferences.Editor editor = sharedPrefAutoBackup.edit();

                if (dataSnapshot.child("UserName").getValue(String.class) != null) {
                    UserName = dataSnapshot.child("UserName").getValue(String.class);
                    editor.putString(getString(R.string.settings_pref_username), UserName).apply();
                    Log.d(TAG, "onDataChange: UserName " + UserName);

                } else {
                    UserName = sharedPrefAutoBackup.getString(getResources().getString(R.string.settings_pref_username), null);

                    Log.d(TAG, "onDataChange: UserName NULL");
                }

                if (dataSnapshot.child("UserEmail").getValue(String.class) != null) {
                    UserEmail = dataSnapshot.child("UserEmail").getValue(String.class);
                    editor.putString(getString(R.string.settings_pref_useremail), UserEmail).apply();
                    Log.d(TAG, "onDataChange: UserEmail " + UserEmail);

                } else {
                    UserEmail = sharedPrefAutoBackup.getString(getResources().getString(R.string.settings_pref_useremail), null);

                    Log.d(TAG, "onDataChange: UserEmail NULL");
                }

                if (dataSnapshot.child("UserAge").getValue(String.class) != null) {
                    UserAge = dataSnapshot.child("UserAge").getValue(String.class);
                    editor.putString(getString(R.string.settings_pref_userage), UserAge).apply();
                    Log.d(TAG, "onDataChange: UserAge " + UserAge);

                } else {
                    UserAge = sharedPrefAutoBackup.getString(getResources().getString(R.string.settings_pref_userage), null);

                    Log.d(TAG, "onDataChange: UserAge NULL");
                }


            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });


    }

    void SyncMessages() {

        if (SMSAutoBackup) {
            SMSBackupDB.setValue(iThread);
            Log.d(TAG, "Sync COMPLETE: Auto Sync Enabled");
        }
    }


    public void FileAutoBackUpBroadCast() {
        Log.d(TAG, "BroadCast");

        int i = 10;
        Intent intent = new Intent(this, ScannerBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this.getApplicationContext(), 234324243, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        //  alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
        //        + (i * 1000), pendingIntent);

        Log.d(TAG, "FileAutoBackUpBroadCast: FileSyncIntervals: " + sharedPrefAutoBackup.getString(getResources().getString(R.string.settings_sync_interval), null));
        String syncinterval = sharedPrefAutoBackup.getString(getResources().getString(R.string.settings_sync_interval), null);
        String[] syncintervalArray = getResources().getStringArray(R.array.auto_sync_intervals);
        if (syncinterval == null) {
            syncinterval = syncintervalArray[4];
        }

        if (syncinterval.equals(syncintervalArray[0])) {
            // 24 Hrs Sync
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_DAY,
                    AlarmManager.INTERVAL_DAY, pendingIntent);

        } else if (syncinterval.equals(syncintervalArray[1])) {
            // 12 Hrs Sync
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HALF_DAY,
                    AlarmManager.INTERVAL_HALF_DAY, pendingIntent);

        } else if (syncinterval.equals(syncintervalArray[2])) {
            // 1 Hrs Sync
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HOUR,
                    AlarmManager.INTERVAL_HOUR, pendingIntent);

        } else if (syncinterval.equals(syncintervalArray[3])) {
            // half Hrs Sync
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HALF_HOUR,
                    AlarmManager.INTERVAL_HALF_HOUR, pendingIntent);

        } else if (syncinterval.equals(syncintervalArray[4])) {
            // 15 min Sync
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                    AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);

        } else {
            // 15 min Sync
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                    AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);

        }


//        Toast.makeText(this, "File upload  " + i + " seconds", Toast.LENGTH_LONG).show();

    }


}
