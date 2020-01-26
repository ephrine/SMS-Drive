package devesh.ephrine.backup.sms;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.crashlytics.android.Crashlytics;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lifeofcoding.cacheutlislibrary.CacheUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import io.fabric.sdk.android.Fabric;


public class MainActivity extends AppCompatActivity {
    final int PERMISSION_ALL = 00000001;
    final int PERMISSION_CONTACT = 00000002;
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
    LoadSms loadsmsTask;
    String[] PERMISSIONS = {
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.READ_SMS
    };
    int onceOpen;
    BottomNavigationView navigation;
    SwipeRefreshLayout mySwipeRefreshLayout;
    View LayoutHome;
    View LayoutCloud;
    DatabaseReference CloudSMSDB;
    ArrayList<HashMap<String, String>> CloudSms = new ArrayList<>();
    ArrayList<HashMap<String, String>> CloudThreadSms = new ArrayList<>();
    RecyclerView CloudRecycleView;
    ArrayList<HashMap<String, String>> contactMap = new ArrayList<>();

    Boolean isDefaultSmsApp;

    private FirebaseAuth mAuth;

    CardView defaultSMSAppCardViewWarning;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.NavDevieMenu:
                    LayoutCloud.setVisibility(View.GONE);
                    LayoutHome.setVisibility(View.VISIBLE);

                    break;

                case R.id.NavCloudMenu:

                    LayoutCloud.setVisibility(View.VISIBLE);
                    LayoutHome.setVisibility(View.GONE);


                    LoadCloudRecycleView();
                    break;

                case R.id.NavToolsMenu:


                    break;


            }


            return true;
        }
    };
/*
    @Override
    protected void onResume() {
        super.onResume();

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }else{
            AppStart();

        }
    }
    */

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
isDefaultSmsApp=false;
        setContentView(R.layout.activity_main_home);
        Fabric.with(this, new Crashlytics());
        onceOpen = 0;
        isSubscribed = true;
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        LayoutHome = findViewById(R.id.layoutHome);
        LayoutCloud = findViewById(R.id.layoutCloud);

        defaultSMSAppCardViewWarning=findViewById(R.id.defaultSMSAppCardViewWarning);

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
                    != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {


                if (!hasPermissions(this, PERMISSIONS)) {
                    ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
                } else {
                    AppStart();

                }

            } else {


                AppStart();

                // Permission has already been granted
            }

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final String myPackageName = getPackageName();
            if (!Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName)) {

                isDefaultSmsApp=false;
           //     Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
             //   intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, myPackageName);
               // startActivityForResult(intent, 1);

            }else {
                isDefaultSmsApp=true;
            }

        } else {
            isDefaultSmsApp=true;
            // saveSms("111111", "mmmmssssggggg", "0", "", "inbox");
        }





/*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final String myPackageName = getPackageName();
            if (!Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName)) {

                Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, myPackageName);
                startActivityForResult(intent, 1);
            }else {
                //saveSms("111111", "mmmmssssggggg", "0", "", "inbox");
            }
        }else {
           // saveSms("111111", "mmmmssssggggg", "0", "", "inbox");
        }*/


        navigation = findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ALL: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    loadsmsTask = new LoadSms();
                    loadsmsTask.execute();

                    LoadRecycleView();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    final String myPackageName = getPackageName();
                    if (Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName)) {

                        //Write to the default sms app
                        // saveSms("111111", "mmmmssssggggg", "0", "", "inbox");
                    }
                }
            }

            isDefaultApp();
            if(isDefaultSmsApp){
                defaultSMSAppCardViewWarning.setVisibility(View.GONE);
            }else {
                defaultSMSAppCardViewWarning.setVisibility(View.VISIBLE);

            }

        }
        super.onActivityResult(requestCode, resultCode, data);
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
        isDefaultApp();
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

            case R.id.newmsg:
                if(isDefaultSmsApp){

                    Intent intent1 = new Intent(this, NewMessageActivity.class);
                    startActivity(intent1);

                }else {
                    Toast.makeText(this, "Please set SMS Drive as your Default Messenger to send new message", Toast.LENGTH_SHORT).show();
                }
                Log.d(TAG, "onOptionsItemSelected: New Message Menu");
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Download SMS from Database
  /*  void DownloadSMS() {
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
*/
    void LoadRecycleView() {

        try {

            ArrayList<HashMap<String, String>> tmpList = (ArrayList<HashMap<String, String>>) Function.readCachedFile(MainActivity.this, "smsapp");

            recyclerView = findViewById(R.id.devicesmsrecycle);

            layoutManager = new LinearLayoutManager(this);

            recyclerView.setHasFixedSize(true);

            // use a linear layout manager
            recyclerView.setLayoutManager(layoutManager);
            // specify an adapter (see also next example)

            SmsAdapter mAdapter = new SmsAdapter(MainActivity.this, tmpList, "D");

            recyclerView.setAdapter(mAdapter);
            onceOpen = 1;

            if (findViewById(R.id.mainhome) != null) {
                mySwipeRefreshLayout.setRefreshing(false);
            }

        } catch (Exception e) {
            Log.d(TAG, "LoadRecycleView: " + e);
        }


    }

    void LoadCloudRecycleView() {
        // downloadCloudSMS();

        try {
            CloudThreadSms = (ArrayList<HashMap<String, String>>) Function.readCachedFile(MainActivity.this, getString(R.string.file_cloud_thread));
            Log.d(TAG, "LoadCloudRecycleView: Reading Offline CloudThreadSms ");
        } catch (Exception e) {
            Log.d(TAG, "LoadCloudRecycleView: Error #324 : " + e);
        }

        CloudRecycleView = findViewById(R.id.cloudsmsrecycle);

        layoutManager = new LinearLayoutManager(this);

        CloudRecycleView.setHasFixedSize(true);

        // use a linear layout manager
        CloudRecycleView.setLayoutManager(layoutManager);
        // specify an adapter (see also next example)

        SmsAdapter mAdapter = new SmsAdapter(MainActivity.this, CloudThreadSms, "C");
        // C = Cloud
        // D = Device

        CloudRecycleView.setAdapter(mAdapter);


    }

    public void AppStart() {
        loadingCircle.setVisibility(View.GONE);

        if (isSubscribed) {


        }

        loadsmsTask = new LoadSms();
        loadsmsTask.execute();
        LoadRecycleView();

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
        if (findViewById(R.id.swipeRefresh) != null) {

            mySwipeRefreshLayout = findViewById(R.id.swipeRefresh);
            mySwipeRefreshLayout.setOnRefreshListener(
                    new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            Log.i(TAG, "onRefresh called from SwipeRefreshLayout");

                            // This method performs the actual data-refresh operation.
                            // The method calls setRefreshing(false) when it's finished.
                            loadsmsTask = new LoadSms();
                            loadsmsTask.execute();
                            LoadRecycleView();
                            Log.d(TAG, "onRefresh: Swipe Down ! Refreshing..");
                        }
                    }
            );
        } else {
            Log.d(TAG, "AppStart: NULL SwipeRefreshLayout");
        }

        downloadCloudSMS();
        getContacts();

        isDefaultApp();

        if(isDefaultSmsApp){
            defaultSMSAppCardViewWarning.setVisibility(View.GONE);
        }else {
            defaultSMSAppCardViewWarning.setVisibility(View.VISIBLE);

        }
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

    void downloadCloudSMS() {


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
                                Function.createCachedFile(MainActivity.this, getString(R.string.file_cloud_sms), CloudSms);
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
                                Function.createCachedFile(MainActivity.this, getString(R.string.file_cloud_thread), CloudSms);
                                Log.d(TAG, "onDataChange: createCachedFile file_cloud_thread ");
                            } catch (Exception e) {
                                Log.d(TAG, "onDataChange: ERROR #5600 : " + e);
                            }

                        }
                        i++;

                    }
                } else {
                    Log.d(TAG, "onDataChange: Backup not Exists !! #021");
                }


            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });


    }

    void getContacts() {
        try {
            contactMap = (ArrayList<HashMap<String, String>>) Function.readCachedFile(MainActivity.this, getString(R.string.file_contact_list));
            if (contactMap == null) {
                getContactList();
                Log.d(TAG, "getContacts: Getting Contacts List");
            } else {
                Log.d(TAG, "getContacts: Contact List Already present");
            }
        } catch (Exception e) {
            Log.d(TAG, "getContacts: ERROR: #870" + e);
            getContactList();
        }

    }

    private void getContactList() {


        new Thread(new Runnable() {
            public void run() {

                ContentResolver cr = getContentResolver();
                Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                        null, null, null, null);

                if ((cur != null ? cur.getCount() : 0) > 0) {
                    while (cur != null && cur.moveToNext()) {
                        String id = cur.getString(
                                cur.getColumnIndex(ContactsContract.Contacts._ID));
                        String name = cur.getString(cur.getColumnIndex(
                                ContactsContract.Contacts.DISPLAY_NAME));

                        if (cur.getInt(cur.getColumnIndex(
                                ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                            Cursor pCur = cr.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                    new String[]{id}, null);
                            while (pCur.moveToNext()) {
                                String phoneNo = pCur.getString(pCur.getColumnIndex(
                                        ContactsContract.CommonDataKinds.Phone.NUMBER));
                                Log.i(TAG, "Name: " + name);
                                Log.i(TAG, "Phone Number: " + phoneNo);
                                HashMap<String, String> c = new HashMap<>();

                                c.put("name", name);
                                c.put("phone", phoneNo);
                                contactMap.add(c);

                                try {
                                    Function.createCachedFile(MainActivity.this, getString(R.string.file_contact_list), contactMap);
                                } catch (Exception e) {
                                    Log.d(TAG, "getContactList: Error #564" + e);
                                }

                            }
                            pCur.close();

                        }
                    }
                }
                if (cur != null) {
                    cur.close();
                }
            }
        }).start();


    }

public void setDefaultSmsApp(View v){

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        final String myPackageName = getPackageName();
        if (!Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName)) {

            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, myPackageName);
            startActivityForResult(intent, 1);
            isDefaultSmsApp=true;

        }else {
            isDefaultSmsApp=true;
        }

    } else {
        isDefaultSmsApp=true;
        // saveSms("111111", "mmmmssssggggg", "0", "", "inbox");
    }


}

void isDefaultApp(){
        boolean a;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        final String myPackageName = getPackageName();
        if (!Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName)) {
            a=false;
        }else {
            a=true;
        }
    } else {
        a=true;
        // saveSms("111111", "mmmmssssggggg", "0", "", "inbox");
    }

         isDefaultSmsApp=a;
}

    //---------------- LoadSms Async Task
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
            // loadingCircle.setVisibility(View.VISIBLE);

        }

        protected String doInBackground(String... args) {
            String xml = "";
if(isDefaultSmsApp){

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


                smsList.add(Function.mappingInbox(_id, thread_id, name, phone, msg, type, timestamp, Function.converToTime(timestamp), read));
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
    }

    try {
        Function.createCachedFile(MainActivity.this, "orgsms", smsList);
        Log.d(TAG, "doInBackground: createCachedFile ORG SMS CREATED");
    } catch (Exception e) {
    }

    Collections.sort(smsList, new MapComparator(Function.KEY_TIMESTAMP, "dsc")); // Arranging sms by timestamp decending
    ArrayList<HashMap<String, String>> purified = Function.removeDuplicates(smsList); // Removing duplicates from inbox & sent
    smsList.clear();
    smsList.addAll(purified);

    // Updating cache data
    try {
        Function.createCachedFile(MainActivity.this, "smsapp", smsList);
        Log.d(TAG, "doInBackground: createCachedFile CREATED");
    } catch (Exception e) {
    }
    // Updating cache data

}
            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {

            if (!tmpList.equals(smsList)) {
                /*
        adapter = new InboxAdapter(MainActivity.this, smsList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
public void onItemClick(AdapterView<?> parent, View view,
final int position, long id) {
        Intent intent = new Intent(MainActivity.this, Chat.class);
        intent.putExtra("name", smsList.get(+position).get(Function.KEY_NAME));
        intent.putExtra("address", tmpList.get(+position).get(Function.KEY_PHONE));
        intent.putExtra("thread_id", smsList.get(+position).get(Function.KEY_THREAD_ID));
        startActivity(intent);
        }
        });
        */
                mySwipeRefreshLayout = findViewById(R.id.swipeRefresh);

                mySwipeRefreshLayout.setRefreshing(false);

                if (onceOpen == 0) {
                    AppStart();
                    onceOpen = 1;
                }


            }


        }
    }

}
