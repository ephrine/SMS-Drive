package devesh.ephrine.backup.sms;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import devesh.ephrine.backup.sms.room.AppDatabase;
import devesh.ephrine.backup.sms.room.Sms;


public class ThreadSmsActivity extends AppCompatActivity {
    public static Boolean isDeleteMsgMode;
    //  final String DBRoot = "SMSDrive/";
    ArrayList<HashMap<String, String>> SmsThreadHashMap = new ArrayList<>();
    String id;
    String TAG = "BackUp|SMS: ";
    FirebaseAuth mAuth;
    String UserUID;
    ArrayList<String> mContacts;
    String subFolder = "/userdata";
    String file = "ContactFiles";
    FirebaseDatabase database;
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    String name;
    String address;
    EditText new_message;
    ImageButton send_message;
    int thread_id_main;
    Thread t;
    ArrayList<HashMap<String, String>> smsList = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> customList = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> tmpList = new ArrayList<HashMap<String, String>>();
    LoadSms loadsmsTask;
    LoadCloudSms loadCloudSmsTask;
    String storage;
    //LinearLayout NewMsgBoxLL;
    ArrayList<HashMap<String, String>> CloudSMS;
    ArrayList<HashMap<String, String>> SortSMS = new ArrayList<>();
    ProgressBar progressBarLoading;
    ProgressBar progressBarHorizontal;
    AppDatabase db;
    LinearLayout LLDeleteMSG;
    ArrayList<Sms> toDelete = new ArrayList<>();
    List<String> testDeviceIds;
    AdView mAdView;
    SharedPreferences sharedPrefAppGeneral;
    boolean isSubscribed;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isSubscribed = false;
        Intent intent = getIntent();
        setContentView(R.layout.sms_activity_thread);

        testDeviceIds = Arrays.asList(getString(R.string.Admob_TestDeviceID));
        //      SmsThreadHashMap = (HashMap<String, DataSnapshot>)intent.getSerializableExtra("smsthread");
        //  SmsThreadHashMap = Parcels.unwrap(getIntent().getParcelableExtra("mylist"))(HashMap<String, DataSnapshot>)intent.getBundleExtra("smsthread");
        //  id = intent.getStringExtra("smsthreadid");
        if (intent.getStringExtra("name") != null) {
            name = intent.getStringExtra("name");
        }
        if (intent.getStringExtra("address") != null) {
            address = intent.getStringExtra("address");
        }
        if (intent.getStringExtra("thread_id") != null) {
            thread_id_main = Integer.parseInt(intent.getStringExtra("thread_id"));
        }
        if (intent.getStringExtra("storage") != null) {
            storage = intent.getStringExtra("storage");
        }
        sharedPrefAppGeneral = PreferenceManager.getDefaultSharedPreferences(this /* Activity context */);

        String sub = sharedPrefAppGeneral.getString(getString(R.string.cache_Sub_isSubscribe), "0");
        if (sub.equals("1")) {
            isSubscribed = true;
        } else {
            isSubscribed = false;
        }

        //Admob
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Log.d(TAG, "onInitializationComplete: AdMob has been initialize");

            }
        });

        /*   */
        RequestConfiguration configuration =
                new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
        MobileAds.setRequestConfiguration(configuration);

        mAdView = findViewById(R.id.adView1);
        if (isSubscribed) {
            mAdView.setVisibility(View.GONE);
        } else {
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
        //  listView = (ListView) findViewById(R.id.listView);
        //   new_message = (EditText) findViewById(R.id.newTextBox);
//        send_message = (ImageButton) findViewById(R.id.send_message);
        //  NewMsgBoxLL = (LinearLayout) findViewById(R.id.msgTextBoxView);
        // setContentView(R.layout.sms_activity_thread);

        if (name != null) {
            getSupportActionBar().setTitle(name);
        } else {
            name = Function.getContactbyPhoneNumber(this, address);
            getSupportActionBar().setTitle(name);
        }


        recyclerView = findViewById(R.id.SmsThreadRecycleView);

        progressBarLoading = findViewById(R.id.progressBarLoading45);
        progressBarLoading.setVisibility(View.VISIBLE);

        progressBarHorizontal = findViewById(R.id.progressBar2Horizontal45);
        progressBarHorizontal.setVisibility(View.VISIBLE);

        LLDeleteMSG = findViewById(R.id.LLDeleteMSG);
        LLDeleteMSG.setVisibility(View.GONE);
        isDeleteMsgMode = false;
        //   DataSnapshot g=SmsThreadHashMap.get(id);
        // Log.d(TAG, "onCreate: Datasnapshot: "+g.toString());
        mAuth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance();


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {

            UserUID = user.getPhoneNumber().replace("+", "x");
            Log.d(TAG, "onStart: User UID:" + UserUID);
            if (storage.equals("D")) {
                startLoadingDeviceSms();
                // NewMsgBoxLL = (LinearLayout) findViewById(R.id.msgTextBoxView);

                // NewMsgBoxLL.setVisibility(View.VISIBLE);
            } else {
                startLoadingCloudSms();
                //  NewMsgBoxLL = (LinearLayout) findViewById(R.id.msgTextBoxView);

                // NewMsgBoxLL.setVisibility(View.GONE);
            }

            //  DownloadThread();
        } else {
            Intent intent1 = new Intent(this, StartActivity.class);

            //  String message = editText.getText().toString();
            //intent.putExtra(EXTRA_MESSAGE, message);
            startActivity(intent1);
        }

       /* if (!isDefaultApp()) {
            NewMsgBoxLL.setVisibility(View.GONE);
        } else {
            NewMsgBoxLL.setVisibility(View.VISIBLE);
        }
        */

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (storage.equals("D")) {

        } else {

            // MenuInflater inflater = getMenuInflater();
            // inflater.inflate(R.menu.thread_sms_activity_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.MENU_deletemsg:
                Log.d(TAG, "onOptionsItemSelected: Menu: Delete Message");
                LLDeleteMSG.setVisibility(View.VISIBLE);
                isDeleteMsgMode = true;
                return true;
           /* case R.id.help:
                showHelp();
                return true;
                */
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void startLoadingDeviceSms() {
        progressBarHorizontal.setVisibility(View.GONE);
        final Runnable r = new Runnable() {
            public void run() {

                loadsmsTask = new LoadSms();
                loadsmsTask.execute();

                handler.postDelayed(this, 5000);
            }
        };
        handler.postDelayed(r, 0);
    }

    public void startLoadingCloudSms() {
        progressBarLoading.setVisibility(View.GONE);
        Toast.makeText(this, "Loading....", Toast.LENGTH_SHORT).show();

        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, getString(R.string.DATABASE_CLOUD_SMS_DB)).allowMainThreadQueries()
                .fallbackToDestructiveMigration().build();
        //  CloudSMS=db.userDao().loadAllByPhoneNo("address");
        LoadCloudSms1 loadCloudSmsTask1 = new LoadCloudSms1();
        loadCloudSmsTask1.execute();
  /*      final Runnable r = new Runnable() {
            public void run() {

                try {


                } catch (Exception e) {
                    Log.e(TAG, "startLoadingCloudSms: Room #ERROR 56465 ", e);
                    Crashlytics.logException(e);

                }


                handler.postDelayed(this, 5000);
            }
        };
        handler.postDelayed(r, 0);
*/

/*
            try {

            CloudSMS = (ArrayList<HashMap<String, String>>) Function.readCachedFile(this, getString(R.string.file_cloud_sms));
                double t = CloudSMS.size();

                double end = t - 1;
            Log.d(TAG, "startLoadingCloudSms: Sorting SMS Started \n" + "Total:" + t + "\n End:" + end);

            for (int i = 0; i < t; i++) {
                //    Log.d(TAG, "startLoadingCloudSms: Sorting SMS...");
                double ii= i / t * 100;
                progress = (int)ii;
                Log.d(TAG, "startLoadingCloudSms: PROGRESS "+progress+"%");
                progressBarHorizontal.setProgress(progress);
                if (CloudSMS.get(i).get(Function.KEY_PHONE).equals(address)) {

                    if (SortSMS.contains(CloudSMS.get(i))) {

                    } else {

                        SortSMS.add(CloudSMS.get(i));
                      //  loadCloudMsgRecycleView();
                    }
                    Log.d(TAG, "startLoadingCloudSms: Cloud MSG: " + CloudSMS.get(i));

                    if (i == end) {
                        Collections.sort(SortSMS, new MapComparator(Function.KEY_TIMESTAMP, "asc"));

                        Log.d(TAG, "startLoadingCloudSms: END OF SORTING---------");
                        progressBarHorizontal.setVisibility(View.GONE);

                    }

                } else {
                }

            }
            Collections.sort(SortSMS, new MapComparator(Function.KEY_TIMESTAMP, "asc"));

            layoutManager = new LinearLayoutManager(ThreadSmsActivity.this);

            recyclerView.removeAllViews();
            //                      recyclerView.removeAllViewsInLayout();
            recyclerView.setHasFixedSize(true);

            // use a linear layout manager
            recyclerView.setLayoutManager(layoutManager);
            // specify an adapter (see also next example)

            ThreadSmsAdapter mAdapter = new ThreadSmsAdapter(ThreadSmsActivity.this, SortSMS);

            recyclerView.setAdapter(mAdapter);
            layoutManager.scrollToPosition(smsList.size() - 1); // yourList is the ArrayList that you are passing to your RecyclerView Adapter.

        } catch (Exception e) {
            Log.d(TAG, "startLoadingCloudSms: ERROR #65 \n" + e);
        }

*/
    }

   /* public void SendMSG(View v) {

        new_message = (EditText) findViewById(R.id.newTextBox);

        String msgtext = new_message.getText().toString();

        if (msgtext.length() > 0) {
            String tmp_msg = msgtext;
            new_message.setText("Sending....");
            new_message.setEnabled(false);

            if (Function.sendSMS(address, tmp_msg)) {
                new_message.setText("");
                new_message.setEnabled(true);
                // Creating a custom list for newly added sms
                customList.clear();
                customList.addAll(smsList);
                customList.add(Function.mappingInbox(null, null, null, null, tmp_msg, "2", null, "Sending...", "1"));
                long smsReceiveTime = System.currentTimeMillis();

                saveSms(address, tmp_msg, "1", String.valueOf(smsReceiveTime), "outbox");
                startLoadingDeviceSms();
            } else {
                new_message.setText(tmp_msg);
                new_message.setEnabled(true);
                Log.d(TAG, "SendMSG: ERROR !!");
            }


        } else {
            Log.d(TAG, "SendMSG: MSG text too short");
        }

    }
    */

    void loadCloudMsgRecycleView() {
        Collections.sort(SortSMS, new MapComparator(Function.KEY_TIMESTAMP, "asc"));

        layoutManager = new LinearLayoutManager(ThreadSmsActivity.this);

        recyclerView.removeAllViews();
        //  recyclerView.removeAllViewsInLayout();
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        recyclerView.setLayoutManager(layoutManager);
        // specify an adapter (see also next example)

        ThreadSmsAdapter mAdapter = new ThreadSmsAdapter(ThreadSmsActivity.this, SortSMS);

        recyclerView.setAdapter(mAdapter);
        layoutManager.scrollToPosition(smsList.size() - 1); // yourList is the ArrayList that you are passing to your RecyclerView Adapter.

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
        }
        return ret;
    }

    private void markMessageRead(Context context, String number, String body) {

        Uri uri = Uri.parse("content://sms/inbox");
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        try {

            while (cursor.moveToNext()) {
                if ((cursor.getString(cursor.getColumnIndex("address")).equals(number)) && (cursor.getInt(cursor.getColumnIndex("read")) == 0)) {
                    if (cursor.getString(cursor.getColumnIndex("body")).startsWith(body)) {
                        String SmsMessageId = cursor.getString(cursor.getColumnIndex("_id"));
                        ContentValues values = new ContentValues();
                        values.put("read", "1");
                        context.getContentResolver().update(Uri.parse("content://sms/inbox"), values, "_id=" + SmsMessageId, null);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            Log.e("Mark Read", "Error in Read: " + e.toString());
        }
    }

    boolean isDefaultApp() {
        /*
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
        */
        return false;
    }

    void updateProgress(int progress) {
        progressBarHorizontal.setProgress(progress);
    }

    public void cancelDeleteMSG(View v) {
        LLDeleteMSG.setVisibility(View.GONE);
        isDeleteMsgMode = false;
        setCloudRecycleView();
    }
    //ArrayList<String> toDelete2=new ArrayList<>();

    public void confirmDeleteMsg(View v) {
        int total = toDelete.size();
        for (int i = 0; i < total; i++) {
//int del=toDelete.get(i).uid;

            db.userDao().deleteByUid(toDelete.get(i).uid);
            Log.d(TAG, "confirmDeleteMsg: Deleting..");
        }
        LLDeleteMSG.setVisibility(View.GONE);
        isDeleteMsgMode = false;
        LoadCloudSms1 loadCloudSmsTask1 = new LoadCloudSms1();
        loadCloudSmsTask1.execute();
    }

    public void addToDeleteList(HashMap<String, String> sms) {
        Sms s = new Sms();
        //  s.ID = sms.get(Function._ID);
        //  s.KEY_READ = sms.get(Function.KEY_READ);
        s.KEY_TIMESTAMP = sms.get(Function.KEY_TIMESTAMP);
        s.KEY_TYPE = sms.get(Function.KEY_TYPE);
        s.KEY_PHONE = sms.get(Function.KEY_PHONE);
        //      s.KEY_THREAD_ID = sms.get(Function.KEY_THREAD_ID);
        s.KEY_MSG = sms.get(Function.KEY_MSG);
        //       s.KEY_NAME = sms.get(Function.KEY_NAME);
        //     s.KEY_TIME = sms.get(Function.KEY_TIME);
        s.uid = Integer.parseInt(sms.get("uid"));
        toDelete.add(s);

        //   toDelete2.add(sms.get("uid"));

        Log.d(TAG, "addToDeleteList: ADDED to Delete List: " + toDelete.toString());

    }

    public void removeFromDeleteList(HashMap<String, String> sms) {
        Sms s = new Sms();
        //     s.ID = sms.get(Function._ID);
        //     s.KEY_READ = sms.get(Function.KEY_READ);
        s.KEY_TIMESTAMP = sms.get(Function.KEY_TIMESTAMP);
        s.KEY_TYPE = sms.get(Function.KEY_TYPE);
        s.KEY_PHONE = sms.get(Function.KEY_PHONE);
        //    s.KEY_THREAD_ID = sms.get(Function.KEY_THREAD_ID);
        s.KEY_MSG = sms.get(Function.KEY_MSG);
        //     s.KEY_NAME = sms.get(Function.KEY_NAME);
        //     s.KEY_TIME = sms.get(Function.KEY_TIME);
        s.uid = Integer.parseInt(sms.get("uid"));

        toDelete.remove(s);

        //  toDelete2.remove(sms.get("uid"));
        Log.d(TAG, "addToDeleteList: REMOVED from Delete List: " + toDelete.toString());

    }

    void setCloudRecycleView() {
        layoutManager = new LinearLayoutManager(ThreadSmsActivity.this);

        recyclerView.removeAllViews();
        //                      recyclerView.removeAllViewsInLayout();
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        recyclerView.setLayoutManager(layoutManager);
        // specify an adapter (see also next example)

        ThreadSmsAdapter mAdapter = new ThreadSmsAdapter(ThreadSmsActivity.this, SortSMS);

        recyclerView.setAdapter(mAdapter);
        layoutManager.setStackFromEnd(true);

        layoutManager.scrollToPosition(smsList.size() - 1); // yourList is the ArrayList that you are passing to your RecyclerView Adapter.

    }

    //--- ASYNC
    class LoadSms extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tmpList.clear();
        }

        protected String doInBackground(String... args) {
            String xml = "";

            try {
                Uri uriInbox = Uri.parse("content://sms/inbox");
                Cursor inbox = getContentResolver().query(uriInbox, null, "thread_id=" + thread_id_main, null, null);
                Uri uriSent = Uri.parse("content://sms/sent");
                Cursor sent = getContentResolver().query(uriSent, null, "thread_id=" + thread_id_main, null, null);
                Cursor c = new MergeCursor(new Cursor[]{inbox, sent}); // Attaching inbox and sent sms


                if (c.moveToFirst()) {
                    for (int i = 0; i < c.getCount(); i++) {
                        String phone = "";
                        String _id = c.getString(c.getColumnIndexOrThrow("_id"));
                        String thread_id = c.getString(c.getColumnIndexOrThrow("thread_id"));
                        String msg = c.getString(c.getColumnIndexOrThrow("body"));
                        String type = c.getString(c.getColumnIndexOrThrow("type"));
                        String timestamp = c.getString(c.getColumnIndexOrThrow("date"));
                        phone = c.getString(c.getColumnIndexOrThrow("address"));

                        // markMessageRead(ThreadSmsActivity.this,phone,msg);
                        ContentValues values = new ContentValues();
                        values.put("read", "1");
                        getContentResolver().update(Uri.parse("content://sms/inbox"), values, "_id=" + _id, null);

                        tmpList.add(Function.mappingInbox(_id, thread_id, name, phone, msg, type, timestamp, Function.converToTime(timestamp), "1"));
                        c.moveToNext();
                    }
                }
                c.close();

            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();

            }
            Collections.sort(tmpList, new MapComparator(Function.KEY_TIMESTAMP, "asc"));

            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {

            if (!tmpList.equals(smsList)) {
                smsList.clear();
                smsList.addAll(tmpList);
                //    adapter = new ChatAdapter(Chat.this, smsList);
                //  listView.setAdapter(adapter);

                layoutManager = new LinearLayoutManager(ThreadSmsActivity.this);

                recyclerView.removeAllViews();
                //                      recyclerView.removeAllViewsInLayout();
                recyclerView.setHasFixedSize(true);

                // use a linear layout manager
                recyclerView.setLayoutManager(layoutManager);
                // specify an adapter (see also next example)

                ThreadSmsAdapter mAdapter = new ThreadSmsAdapter(ThreadSmsActivity.this, smsList);

                recyclerView.setAdapter(mAdapter);
                layoutManager.scrollToPosition(smsList.size() - 1); // yourList is the ArrayList that you are passing to your RecyclerView Adapter.
                progressBarLoading.setVisibility(View.GONE);


            }


        }
    }

    class LoadCloudSms extends AsyncTask<String, Integer, String> {
        int progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            tmpList.clear();
            Log.d(TAG, "LoadCloudSms: onPreExecute ");
            progress = 0;
        }

        protected String doInBackground(String... args) {
            Log.d(TAG, "LoadCloudSms: doInBackground() ");
            try {

                CloudSMS = (ArrayList<HashMap<String, String>>) Function.readCachedFile(ThreadSmsActivity.this, getString(R.string.file_cloud_sms));
                double t = CloudSMS.size();

                double end = t - 1;
                Log.d(TAG, "startLoadingCloudSms: Sorting SMS Started \n" + "Total:" + t + "\n End:" + end);

                for (int i = 0; i < t; i++) {

                    double ii = i / t * 100;
                    progress = (int) ii;
                    Log.d(TAG, "doInBackground: PROGRESS :" + progress);
                    updateProgress(progress);
                    //    Log.d(TAG, "startLoadingCloudSms: Sorting SMS...");

                    if (CloudSMS.get(i).get(Function.KEY_PHONE).equals(address)) {

                        if (SortSMS.contains(CloudSMS.get(i))) {

                        } else {

                            SortSMS.add(CloudSMS.get(i));
                        }
                        Log.d(TAG, "startLoadingCloudSms: Cloud MSG: " + CloudSMS.get(i));

                        if (i == end) {
                            Collections.sort(SortSMS, new MapComparator(Function.KEY_TIMESTAMP, "asc"));

                            Log.d(TAG, "startLoadingCloudSms: END OF SORTING---------");


                        }

                    } else {
                    }

                }
                Collections.sort(SortSMS, new MapComparator(Function.KEY_TIMESTAMP, "asc"));


            } catch (Exception e) {
                Log.e(TAG, "doInBackground: ERROR #673445 " + e);
            }

            return "Done";
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBarHorizontal.setProgress(progress);
            Log.d(TAG, "doInBackground: PROGRESS update :" + progress);

            //Main.getApp().progressBar.setProgress(values[0]);
            //Main.getApp().txt_percentage.setText("downloading" + values[0] + "%");
        }

        @Override
        protected void onPostExecute(String xml) {
            Log.d(TAG, "LoadCloudSms: onPostExecute ");

            layoutManager = new LinearLayoutManager(ThreadSmsActivity.this);

            recyclerView.removeAllViews();
            //                      recyclerView.removeAllViewsInLayout();
            recyclerView.setHasFixedSize(true);

            // use a linear layout manager
            recyclerView.setLayoutManager(layoutManager);
            // specify an adapter (see also next example)

            ThreadSmsAdapter mAdapter = new ThreadSmsAdapter(ThreadSmsActivity.this, SortSMS);

            recyclerView.setAdapter(mAdapter);
            layoutManager.scrollToPosition(smsList.size() - 1); // yourList is the ArrayList that you are passing to your RecyclerView Adapter.

            progressBarLoading.setVisibility(View.GONE);

            progressBarHorizontal.setVisibility(View.GONE);
        }


    }

    class LoadCloudSms1 extends AsyncTask<String, Integer, String> {
        int progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            tmpList.clear();
            Log.d(TAG, "LoadCloudSms: onPreExecute ");
            progress = 0;
        }

        protected String doInBackground(String... args) {
            Log.d(TAG, "LoadCloudSms: doInBackground() ");
            try {

                //     CloudSMS = (ArrayList<HashMap<String, String>>) Function.readCachedFile(ThreadSmsActivity.this, getString(R.string.file_cloud_sms));
                List<Sms> smsL = db.userDao().loadAllByPhoneNo(address);
                double t = smsL.size();

                double end = t - 1;
                Log.d(TAG, "startLoadingCloudSms: Sorting SMS Started \n" + "Total:" + t + "\n End:" + end);
//address
                for (int i = 0; i < t; i++) {

                    double ii = i / t * 100;
                    progress = (int) ii;
                    Log.d(TAG, "doInBackground: PROGRESS :" + progress);
                    updateProgress(progress);
                    //    Log.d(TAG, "startLoadingCloudSms: Sorting SMS...");
                    HashMap<String, String> hm = new HashMap<>();
                    //        hm.put(Function._ID, smsL.get(i).ID);
                    //      hm.put(Function.KEY_READ, smsL.get(i).KEY_READ);
                    hm.put(Function.KEY_TIMESTAMP, smsL.get(i).KEY_TIMESTAMP);
                    //    hm.put(Function.KEY_TIME, smsL.get(i).KEY_TIME);
                    hm.put(Function.KEY_TYPE, smsL.get(i).KEY_TYPE);
                    hm.put(Function.KEY_PHONE, smsL.get(i).KEY_PHONE);
                    hm.put(Function.KEY_MSG, smsL.get(i).KEY_MSG);
                    //    hm.put(Function.KEY_NAME, smsL.get(i).KEY_NAME);
                    hm.put("uid", String.valueOf(smsL.get(i).uid));

                    SortSMS.add(hm);

                /*
                    if (CloudSMS.get(i).get(Function.KEY_PHONE).equals(address)) {

                        if (SortSMS.contains(CloudSMS.get(i))) {

                        } else {

                            SortSMS.add(CloudSMS.get(i));
                        }
                        Log.d(TAG, "startLoadingCloudSms: Cloud MSG: " + CloudSMS.get(i));

                        if (i == end) {
                            Collections.sort(SortSMS, new MapComparator(Function.KEY_TIMESTAMP, "asc"));

                            Log.d(TAG, "startLoadingCloudSms: END OF SORTING---------");


                        }

                    } else {
                    }
                    */

                }
                Collections.sort(SortSMS, new MapComparator(Function.KEY_TIMESTAMP, "asc"));


            } catch (Exception e) {
                Log.e(TAG, "doInBackground: ERROR #673445 " + e);
                Crashlytics.logException(e);

            }

            return "Done";
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBarHorizontal.setProgress(progress);
            Log.d(TAG, "doInBackground: PROGRESS update :" + progress);

            //Main.getApp().progressBar.setProgress(values[0]);
            //Main.getApp().txt_percentage.setText("downloading" + values[0] + "%");
        }

        @Override
        protected void onPostExecute(String xml) {
            Log.d(TAG, "LoadCloudSms: onPostExecute ");

            layoutManager = new LinearLayoutManager(ThreadSmsActivity.this);

            recyclerView.removeAllViews();
            //                      recyclerView.removeAllViewsInLayout();
            recyclerView.setHasFixedSize(true);

            // use a linear layout manager
            recyclerView.setLayoutManager(layoutManager);
            // specify an adapter (see also next example)

            ThreadSmsAdapter mAdapter = new ThreadSmsAdapter(ThreadSmsActivity.this, SortSMS);

            recyclerView.setAdapter(mAdapter);
            layoutManager.setStackFromEnd(true);

            layoutManager.scrollToPosition(smsList.size() - 1); // yourList is the ArrayList that you are passing to your RecyclerView Adapter.

            progressBarLoading.setVisibility(View.GONE);

            progressBarHorizontal.setVisibility(View.GONE);
        }


    }

}


//HashMap<String,DataSnapshot> SmsThreadHashMap=new HashMap<>();

  /*  void DownloadThread() {

        recyclerView = findViewById(R.id.SmsThreadRecycleView);
        layoutManager = new LinearLayoutManager(this);

        DatabaseReference GetSMS = database.getReference("/users/" + UserUID + "/sms/" + id);
        //final ArrayList<String> SMSList= new ArrayList<>();
// My top posts by number of stars

        //     Query myTopPostsQuery = database.getReference("DBRoot" + "/users/" + UserUID + "/sms/" + id).orderByChild("date");

  /*      myTopPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Log.d(TAG, "Download Value is: " + dataSnapshot.toString());
                long t;
                if (dataSnapshot.getChildrenCount() == 1) {
                    t = dataSnapshot.getChildrenCount();
                } else {
                    t = dataSnapshot.getChildrenCount() - 1;
                }


                long i = 0;
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    // TODO: handle the post

                    i = i + 1;
                    Log.d(TAG, "i:" + i + "\nt:" + t);


                    String MsgTime = postSnapshot.child("date").getValue().toString();
                    Date date = new Date(Long.parseLong(MsgTime));

                    String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
                    //String formattedDate = new SimpleDateFormat("MM/dd/yyyy").format(date);
                    String MsgText = postSnapshot.child("body").getValue().toString();
                    String folder=postSnapshot.child("folder").getValue().toString();
                    HashMap<String, String> SMS = new HashMap<>();

                    SMS.put("msg", MsgText);
                    SMS.put("time", formattedDate);
                    SMS.put("folder", folder);


                    Log.d(TAG, "onDataChange: SMS Thread: " + MsgText + "\n" + MsgTime);


                    if (i == t) {
                        Log.d(TAG, "onDataChange: at last:" + i + " t:" + t);
//
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

  /*      GetSMS.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Log.d(TAG, "Download Sort Value is: " + dataSnapshot.toString());
                long t;
                if (dataSnapshot.getChildrenCount() == 1) {
                    t = dataSnapshot.getChildrenCount();
                } else {
                    t = dataSnapshot.getChildrenCount() - 1;
                }


                long i = 0;
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    // TODO: handle the post

                    i = i + 1;
                    Log.d(TAG, "i:" + i + "\nt:" + t);


                    String MsgTime = postSnapshot.child("date").getValue().toString();
                    Date date = new Date(Long.parseLong(MsgTime));

                    String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
                    //String formattedDate = new SimpleDateFormat("MM/dd/yyyy").format(date);
                    String MsgText = postSnapshot.child("body").getValue().toString();
                    String folder = postSnapshot.child("folder").getValue().toString();
                    HashMap<String, String> SMS = new HashMap<>();

                    SMS.put("msg", MsgText);
                    SMS.put("time", formattedDate);
                    SMS.put("folder", folder);

                    SmsThreadHashMap.add(SMS);

                    Log.d(TAG, "onDataChange: SMS Thread: " + MsgText + "\n" + MsgTime);


                    if (i == t) {
                        Log.d(TAG, "onDataChange: at last:" + i + " t:" + t);
//                        recyclerView.removeAllViews();
                        //                      recyclerView.removeAllViewsInLayout();
                        recyclerView.setHasFixedSize(true);

                        // use a linear layout manager
                        recyclerView.setLayoutManager(layoutManager);
                        // specify an adapter (see also next example)

                        ThreadSmsAdapter mAdapter = new ThreadSmsAdapter(ThreadSmsActivity.this, SmsThreadHashMap);

                        recyclerView.setAdapter(mAdapter);

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
*/


