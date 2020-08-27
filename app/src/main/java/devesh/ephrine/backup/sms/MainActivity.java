package devesh.ephrine.backup.sms;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.role.RoleManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MergeCursor;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Telephony;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.adcolony.sdk.AdColony;
import com.adcolony.sdk.AdColonyAppOptions;
import com.crashlytics.android.Crashlytics;
import com.google.ads.mediation.adcolony.AdColonyMediationAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;
import com.jirbo.adcolony.AdColonyAdapter;
import com.jirbo.adcolony.AdColonyBundleBuilder;
import com.lifeofcoding.cacheutlislibrary.CacheUtils;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;
import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import devesh.ephrine.backup.sms.pushnotification.EpNotificationActivity;
import devesh.ephrine.backup.sms.room.AppDatabase;
import devesh.ephrine.backup.sms.room.Sms;
import devesh.ephrine.backup.sms.services.CloudSMS2DBService;
import devesh.ephrine.backup.sms.services.DeviceScanIntentService;
import devesh.ephrine.backup.sms.services.DownloadCloudMessagesService;
import devesh.ephrine.backup.sms.services.SyncIntentService;
import io.fabric.sdk.android.Fabric;


public class MainActivity extends AppCompatActivity {
    final int PERMISSION_ALL = 00000001;
    final int PERMISSION_CONTACT = 00000002;
    final Boolean isDefaultSmsApp = true;
    final String FirebaseMesagingTAG = "FB";
    //   final String DBRoot = "SMSDrive/";
    public HashMap<String, ArrayList<HashMap<String, String>>> iThread;
    DatabaseReference SMSBackupDB;
    DatabaseReference UserDB;
    String TAG = "SMS Drive|MainActivity";
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
    SharedPreferences sharedPrefAppGeneral;
    boolean SMSAutoBackup;
    boolean isSubscribed;
    ProgressBar loadingCircle;
    LoadSms loadsmsTask;
    ProcessCloudSmsThread processCloudThread;
    String[] PERMISSIONS = {
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.READ_SMS,

            Manifest.permission.RECEIVE_SMS,
            //Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_STATE
    };
    int onceOpen;
    BottomNavigationView navigation;
    SwipeRefreshLayout mySwipeRefreshLayout;
    View LayoutHome;
    View LayoutCloud;
    DatabaseReference CloudSMSDB;
    ArrayList<HashMap<String, String>> CloudSms = new ArrayList<>();
    ArrayList<HashMap<String, String>> CloudThreadSms = new ArrayList<>();
    ArrayList<HashMap<String, String>> DeviceSMS = new ArrayList<>();
    RecyclerView CloudRecycleView;
    ArrayList<HashMap<String, String>> contactMap = new ArrayList<>();
    CardView defaultSMSAppCardViewWarning;
    TextView textView5CloudEmpty;
    FirebaseRemoteConfig mFirebaseRemoteConfig;
    RelativeLayout CloudViewUnSub;
    String BackupStorageDB;
    CardView LLSubCardView;
    CardView LLSyncCardView;
    File localFile;
    Gson gson;
    AppDatabase db;
    AppDatabase ThreadSmsDB;
    ProgressBar cloudRefreshProgressBar;
    LinearLayout LLCloudPanelIdeal;
    LinearLayout LLCloudRefreshing;
    LinearLayout LLCloudEmpty;
    LinearLayout LLBGmsgprocessing;
    SharedPreferences.OnSharedPreferenceChangeListener AppGenPrefListener;
    String bg_TASK_STATUS;
    NotificationManagerCompat notificationManager;
    NotificationCompat.Builder nmbuilder;
    int PROGRESS_MAX = 100;
    int PROGRESS_CURRENT = 0;
    TextView textNoInternerError;
    List<String> testDeviceIds;
    boolean isIntAdShowed;
    RelativeLayout RLGettingStarted;
    WebView webviewGettingStarted;
    boolean isFirstStart;
    private FirebaseAuth mAuth;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.NavDevieMenu:
                    LayoutCloud.setVisibility(View.GONE);
                    LayoutHome.setVisibility(View.VISIBLE);
                    checkBGRunningServices();

                    break;

                case R.id.NavCloudMenu:

                    LayoutCloud.setVisibility(View.VISIBLE);
                    LayoutHome.setVisibility(View.GONE);

                    LoadCloudRecycleView();

                    checkBGRunningServices();

                    break;

                case R.id.NavToolsMenu:


                    break;


            }


            return true;
        }
    };
    private FirebaseFunctions mFunctions;
    private InterstitialAd mInterstitialAd;

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
    protected void onResume() {
        super.onResume();
        String sub = sharedPrefAppGeneral.getString(getString(R.string.cache_Sub_isSubscribe), "0");

        try {
            if (sub.equals("1")) {
                isSubscribed = true;
            }
            //isSubscribed = sub.equals("1");
        } catch (Exception e) {
            isSubscribed = false;
        }


        if (isSubscribed) {
            LLSubCardView.setVisibility(View.GONE);
            LLSyncCardView.setVisibility(View.VISIBLE);
        } else {
            LLSubCardView.setVisibility(View.VISIBLE);
            LLSyncCardView.setVisibility(View.VISIBLE);
            //      SharedPreferences.Editor editor = sharedPrefAutoBackup.edit();
            //     editor.putBoolean(getString(R.string.settings_sync), false).apply();
        }

        bg_TASK_STATUS = sharedPrefAppGeneral.getString(getString(R.string.BG_Task_Status), "0");
        if (bg_TASK_STATUS.equals("1")) {
            //    LLBGmsgprocessing.setVisibility(View.VISIBLE);
        } else {
            //       LLBGmsgprocessing.setVisibility(View.GONE);
        }
        checkBGRunningServices();


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//isDefaultSmsApp=false;
        String flavour = BuildConfig.FLAVOR;


    setContentView(R.layout.activity_main_home);



        isFirstStart = false;
        isIntAdShowed = false;
        //   testDeviceIds = Arrays.asList(getString(R.string.Admob_TestDeviceID));

        Intent intent = getIntent();

        Log.d(TAG, "onCreate: FLAVOUR: " + flavour);

        Fabric.with(this, new Crashlytics());
        //Admob
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Log.d(TAG, "onInitializationComplete: AdMob has been initialize");

            }
        });

        String admobid = getString(R.string.AdMob_AppId);


  /*      RequestConfiguration configuration =
                new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
        MobileAds.setRequestConfiguration(configuration);
*/

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.AdMob_InitId));


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Register Callback - Call this in your app start!
            CheckNetwork network = new CheckNetwork(getApplicationContext());
            network.registerNetworkCallback();

        }

        boolean isAnalyticDataCollectionEnable = false;
        Resources res = getResources();
        isAnalyticDataCollectionEnable = res.getBoolean(R.bool.FIREBASE_ANALYTICS_DATA_COLLECTION);
        if (isAnalyticDataCollectionEnable) {
            if (BuildConfig.DEBUG) {

            } else {
                AppCenter.start(getApplication(), BuildConfig.MS_AppCenter_Key,
                        Analytics.class, Crashes.class);

            }
        }

        FirebaseMessagingServiceAct();

        if (intent.getStringExtra("firstopen") != null) {
            String firstopen = intent.getStringExtra("firstopen");
            if (firstopen.equals("1")) {
                Log.d(TAG, "FIRST OPEN:  downloadCloudSMS()");
                DownloadCloudMessagesService.enqueueWork(this, new Intent());
                isFirstStart = true;
            }
        }

        sharedPrefAppGeneral = PreferenceManager.getDefaultSharedPreferences(this /* Activity context */);
        sharedPrefAutoBackup = PreferenceManager.getDefaultSharedPreferences(this /* Activity context */);


        SMSAutoBackup = sharedPrefAutoBackup.getBoolean(getResources().getString(R.string.settings_sync), false);
        String sub = sharedPrefAppGeneral.getString(getString(R.string.cache_Sub_isSubscribe), "0");
        Log.d(TAG, "AppStart: isSubscribe Cache " + sub);
        isSubscribed = sub.equals("1");

        if (!isSubscribed) {
            SdkConfiguration sdkConfiguration =
                    new SdkConfiguration.Builder(getString(R.string.MoPub_AdUnit_ID)).build();

            MoPub.initializeSdk(this, sdkConfiguration, null);


            AdColonyAppOptions appOptions = AdColonyMediationAdapter.getAppOptions();
            appOptions.setGDPRConsentString("1");
            appOptions.setGDPRRequired(true);

            AdColony.configure(this,
                    getString(R.string.AdColony_App_ID),
                    getString(R.string.AdColony_ZoneID1), getString(R.string.AdColony_ZoneID2));
            AdColonyBundleBuilder.setShowPrePopup(true);
            AdColonyBundleBuilder.setShowPostPopup(true);
            AdRequest request = new AdRequest.Builder()
                    .addNetworkExtrasBundle(AdColonyAdapter.class, AdColonyBundleBuilder.build())
                    .build();

//            mInterstitialAd.loadAd(new AdRequest.Builder().build());
            mInterstitialAd.loadAd(request);

            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    // Code to be executed when an ad finishes loading.
                    Log.d(TAG, "onAdLoaded: ");
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    // Code to be executed when an ad request fails.
                }

                @Override
                public void onAdOpened() {
                    // Code to be executed when the ad is displayed.
                    Log.d(TAG, "onAdOpened: ");
                }

                @Override
                public void onAdClicked() {
                    // Code to be executed when the user clicks on an ad.
                    Log.d(TAG, "onAdClicked: ");
                    isIntAdShowed = true;
                }

                @Override
                public void onAdLeftApplication() {
                    // Code to be executed when the user has left the app.
                    finish();
                    Log.d(TAG, "onAdLeftApplication: ");
                }

                @Override
                public void onAdClosed() {
                    // Code to be executed when the interstitial ad is closed.
                    Log.d(TAG, "onAdClosed: ");
                    //     mInterstitialAd.loadAd(new AdRequest.Builder().build());
                    isIntAdShowed = true;
                }
            });
        }

        //Subscription Check
        try {
            Intent subscriptionCheck = new Intent(this, CheckSubscriptionService.class);

            startService(subscriptionCheck);


        } catch (Exception e) {
            Log.e(TAG, "onCreate: #5465653 ", e);
        }


        Function.getDefaultLocal();

        onceOpen = 0;
        //  isSubscribed = true;
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        LayoutHome = findViewById(R.id.layoutHome);
        LayoutCloud = findViewById(R.id.layoutCloud);
        CloudViewUnSub = findViewById(R.id.RLUNSub);

        textView5CloudEmpty = findViewById(R.id.textView5CloudEmpty);

        LLSubCardView = findViewById(R.id.LLSubCardView);
        LLSyncCardView = findViewById(R.id.LLSyncCardView);

        cloudRefreshProgressBar = findViewById(R.id.cloudRefreshProgressBar45763240);
        LLCloudPanelIdeal = findViewById(R.id.LLCloudPanelIdeal);
        LLCloudRefreshing = findViewById(R.id.LLCloudRefreshing);
        LLCloudRefreshing.setVisibility(View.GONE);

        LLCloudEmpty = findViewById(R.id.LLCloudEmpty);
        LLCloudEmpty.setVisibility(View.GONE);

        LLBGmsgprocessing = findViewById(R.id.LLBGmsgprocessing);
        LLBGmsgprocessing.setVisibility(View.GONE);

        // defaultSMSAppCardViewWarning=findViewById(R.id.defaultSMSAppCardViewWarning);
        mySwipeRefreshLayout = findViewById(R.id.swipeRefresh);

        textNoInternerError = findViewById(R.id.textView3InternetError);

        RLGettingStarted = findViewById(R.id.RLGettingStarted);
        webviewGettingStarted = findViewById(R.id.webviewGettingStarted);
        //   RLGettingStarted.setVisibility(View.GONE);

        loadingCircle = findViewById(R.id.progressBar1);
        iThread = new HashMap<>();

   /*     ThreadSmsDB= Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, getString(R.string.DATABASE_THREAD_SMS_DB)).allowMainThreadQueries().fallbackToDestructiveMigration()
                .build();


        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, getString(R.string.DATABASE_SMS_DB)).allowMainThreadQueries().fallbackToDestructiveMigration()
                .build();
*/


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


        if ( Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            final String myPackageName = getPackageName();
          boolean  a = Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName);
            Log.d(TAG, "isDefaultApp: api kitkat-Q: "+a);
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roleManager = null;
            roleManager = getSystemService(RoleManager.class);
            Log.d(TAG, "isDefaultApp(): API Q");

            if (roleManager.isRoleAvailable(RoleManager.ROLE_SMS)) {
                if (roleManager.isRoleHeld(RoleManager.ROLE_SMS)) {
                    Log.d(TAG, "isDefaultApp(): Default SMS App role");
               //     a=true;
                } else {
                    Log.d(TAG, "isDefaultApp(): Not Default SMS App role");
             //       a=false;
                }
            }else{
                Log.d(TAG, "isDefaultApp: api Q ");
                Log.d(TAG, "isDefaultApp(): api QNot Default SMS App role");
           //     a=false;
            }
        }else {
         //   a = true;
            Log.d(TAG, "isDefaultApp(): api Q Default SMS App role #32543");
            // saveSms("111111", "mmmmssssggggg", "0", "", "inbox");
        }

       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final String myPackageName = getPackageName();
            if (!Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName)) {

                //       isDefaultSmsApp=false;
                //     Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                //   intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, myPackageName);
                // startActivityForResult(intent, 1);

            } else {
                //     isDefaultSmsApp=true;
            }

        } else {
            //       isDefaultSmsApp=true;
            // saveSms("111111", "mmmmssssggggg", "0", "", "inbox");
        }
        */


        navigation = findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                // .setMinimumFetchIntervalInSeconds(3600)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config);
    /*     mFirebaseRemoteConfig.setDefaults(R.xml.remote_config);
       mFirebaseRemoteConfig.fetch(10)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //     Toast.makeText(MainActivity.this, "Fetch Succeeded",
                            //           Toast.LENGTH_SHORT).show();

                            // After config data is successfully fetched, it must be activated before newly fetched
                            // values are returned.
                            mFirebaseRemoteConfig.activateFetched();
                            boolean isFreeAccess = mFirebaseRemoteConfig.getBoolean("SMSDrive_free_access");
                            if (isFreeAccess) {
                                Log.d(TAG, "onComplete: USER HAS FREE ACCESS OFFER !!!");
                            } else {
                                Toast.makeText(MainActivity.this, "Please Update App from https://www.ephrine.in", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            //         Toast.makeText(MainActivity.this, "Fetch Failed",
                            //               Toast.LENGTH_SHORT).show();
                        }
                        //  displayWelcomeMessage();
                    }
                });
*/

        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            boolean updated = task.getResult();
                            Log.d(TAG, "Config params updated: " + updated);
                            /*Toast.makeText(MainActivity.this, "Fetch and activate succeeded",
                                    Toast.LENGTH_SHORT).show();  */

                        } else {
                            /*Toast.makeText(MainActivity.this, "Fetch failed",
                                    Toast.LENGTH_SHORT).show(); */
                        }
                        //  displayWelcomeMessage();
                        String LatestVersionCode = mFirebaseRemoteConfig.getString("SMSDrive_Latest_Version");
                        int currentVersion = BuildConfig.VERSION_CODE;
                        Log.d(TAG, "onComplete: mFirebaseRemoteConfig SMSDriveLatestVersionCode:" + LatestVersionCode + "\ncurrent versioncode:" + currentVersion);
                        if (Integer.parseInt(LatestVersionCode) > currentVersion) {
                            CreateNotification("New Update Available");
                            Log.d(TAG, "onComplete: mFirebaseRemoteConfig Update Available");
                        }

                    }
                });


        if (!isNetworkAvailable()) {
            textNoInternerError.setVisibility(View.VISIBLE);
            Log.d(TAG, "onCreate: NO INTERNET !");
        } else {
            textNoInternerError.setVisibility(View.GONE);
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
        if (ThreadSmsDB != null) {
            ThreadSmsDB.close();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
                    if (isFirstStart) {
                        showGettingStarted();
                    }

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
           /* if(isDefaultSmsApp){
                defaultSMSAppCardViewWarning.setVisibility(View.GONE);
            }else {
                defaultSMSAppCardViewWarning.setVisibility(View.VISIBLE);

            }
            */

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
                SMSScan s = new SMSScan(this);
                s.ScanNow();
                Toast.makeText(this, "Syncing...", Toast.LENGTH_LONG).show();

  /*              if (isSubscribed) {
                        } else {
                    Toast.makeText(this, "Please Subscribe before Sync", Toast.LENGTH_LONG).show();
                }
*/
                Log.d(TAG, "onOptionsItemSelected: Sync Now menu");
                return true;

            case R.id.Menu_gettingstarted:
                showGettingStarted();
                Log.d(TAG, "onOptionsItemSelected: Getting Started Menu");
                return true;

            case R.id.settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                Log.d(TAG, "onOptionsItemSelected: Settings Menu");
                return true;

            case R.id.Menu_notification:
                Intent intent1 = new Intent(this, EpNotificationActivity.class);
                startActivity(intent1);

                Log.d(TAG, "onOptionsItemSelected: New Message Menu");
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onBackPressed() {
        if (isIntAdShowed) {
            finish();
            super.onBackPressed();
        } else {
            if (mInterstitialAd.isLoaded() && !isSubscribed) {
                mInterstitialAd.show();
            } else {
                Log.d("TAG", "The interstitial wasn't loaded yet.");
                super.onBackPressed();
                finish();
            }
        }

    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */


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
            Crashlytics.logException(e);

            Intent intent = new Intent(this, DeviceScanIntentService.class);

            //  String message = editText.getText().toString();
            //intent.putExtra(EXTRA_MESSAGE, message);
            startService(intent);

        }


    }


    void LoadCloudRecycleView() {

try{
    ThreadSmsDB = Room.databaseBuilder(getApplicationContext(),
            AppDatabase.class, getString(R.string.DATABASE_THREAD_SMS_DB)).allowMainThreadQueries().fallbackToDestructiveMigration()
            .build();

    CloudRecycleView = findViewById(R.id.cloudsmsrecycle);

    layoutManager = new LinearLayoutManager(this);

    CloudRecycleView.setHasFixedSize(true);

    // use a linear layout manager
    CloudRecycleView.setLayoutManager(layoutManager);
    // specify an adapter (see also next example)
    List<Sms> ll = ThreadSmsDB.userDao().getAll();
    if (ll != null) {

    } else {
        Toast.makeText(this, "Tap Refresh", Toast.LENGTH_SHORT).show();
    }

    CloudSmsAdapter mAdapter = new CloudSmsAdapter(MainActivity.this, ll);
    // C = Cloud
    // D = Device

    CloudRecycleView.setAdapter(mAdapter);
    ThreadSmsDB.close();

}catch (Exception e){
Log.e(TAG,e.toString());
    Toast.makeText(this, "Error! Please Restart/Re-install App", Toast.LENGTH_LONG).show();
    Intent intent = new Intent(this, StartActivity.class);


    MainActivity.this.finish();

    startActivity(intent);
}


    }

    public void AppStart() {
        //  loadingCircle.setVisibility(View.GONE);
        mySwipeRefreshLayout.setRefreshing(false);


        SMSAutoBackup = sharedPrefAutoBackup.getBoolean(getResources().getString(R.string.settings_sync), false);
        String sub = sharedPrefAppGeneral.getString(getString(R.string.cache_Sub_isSubscribe), "0");
        Log.d(TAG, "AppStart: isSubscribe Cache " + sub);

        try {
            if (sub.equals("1")) {
                isSubscribed = true;
                LLSubCardView.setVisibility(View.GONE);
                LLSyncCardView.setVisibility(View.VISIBLE);

            } else {
                isSubscribed = false;
                LLSubCardView.setVisibility(View.VISIBLE);
                LLSyncCardView.setVisibility(View.VISIBLE);

            }
        } catch (Exception e) {
            // isSubscribed = false;
            Log.e(TAG, "AppStart: ERROR #01011 ", e);
        }


        checkBGRunningServices();

        loadsmsTask = new LoadSms();
        loadsmsTask.execute();

        processCloudThread = new ProcessCloudSmsThread();
        processCloudThread.execute();

        LoadRecycleView();



  /*      Switch SyncSwitch = findViewById(R.id.switch1);

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
*/

        UserDB = database.getReference("/users/" + UserUID + "/profile");
        UserDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
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


            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        if (findViewById(R.id.swipeRefresh) != null) {

            mySwipeRefreshLayout.setOnRefreshListener(
                    new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            Log.i(TAG, "onRefresh called from SwipeRefreshLayout");

                            if (!isNetworkAvailable()) {
                                textNoInternerError.setVisibility(View.VISIBLE);
                                Log.d(TAG, "onCreate: NO INTERNET !");
                            } else {
                                textNoInternerError.setVisibility(View.GONE);
                            }

                            // This method performs the actual data-refresh operation.
                            // The method calls setRefreshing(false) when it's finished.
                            loadsmsTask = new LoadSms();
                            loadsmsTask.execute();
                            Intent intent = new Intent(MainActivity.this, DeviceScanIntentService.class);

                            //  String message = editText.getText().toString();
                            //intent.putExtra(EXTRA_MESSAGE, message);
                            startService(intent);
                            LoadRecycleView();

                            refreshLastSync();


                            Log.d(TAG, "onRefresh: Swipe Down ! Refreshing..");
                        }
                    }
            );
        } else {
            Log.d(TAG, "AppStart: NULL SwipeRefreshLayout");
        }

        //  downloadCloudSMS();
        //    getContacts();

        isDefaultApp();

       /* if(isDefaultSmsApp){
            defaultSMSAppCardViewWarning.setVisibility(View.GONE);
        }else {
            defaultSMSAppCardViewWarning.setVisibility(View.VISIBLE);

        }
        */

        refreshLastSync();
        FileAutoBackUpBroadCast();
        setPreferenceListner();
        CheckMultiAppUsage();


  /*      AdLoader adLoader = new AdLoader.Builder(this, "ca-app-pub-3940256099942544/2247696110")
                .forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                    @Override
                    public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                        NativeTemplateStyle styles = new
                                NativeTemplateStyle.Builder().build();

                        TemplateView template = findViewById(R.id.my_template);
                        template.setStyles(styles);
                        template.setNativeAd(unifiedNativeAd);

                    }
                })
                .build();

        adLoader.loadAd(new AdRequest.Builder().build());
*/



    }

    void refreshLastSync() {

        String LastSyncDateSTR = sharedPrefAutoBackup.getString(getResources().getString(R.string.settings_pref_last_sync), null);
        TextView LastSyncTextView = findViewById(R.id.textView2LastSyncDate);
        if (LastSyncDateSTR != null) {
            LastSyncTextView.setText(getString(R.string.last_sync_at) + " " + LastSyncDateSTR);
        } else {
            LastSyncTextView.setText(getString(R.string.last_sync_at) + " --");
        }

    }

    public void syncNow(View v) {

        new Thread(new Runnable() {
            public void run() {
                // a potentially time consuming task
                SMSScan s = new SMSScan(MainActivity.this);
                s.ScanNow();

            }
        }).start();


            /*
            OneTimeWorkRequest syncWorkRequest = new OneTimeWorkRequest.Builder(SyncWorkManager.class)
                    .build();
            WorkManager.getInstance(this).enqueue(syncWorkRequest);*/
        Toast.makeText(this, "Syncing...", Toast.LENGTH_LONG).show();


    }

    public void FileAutoBackUpBroadCast() {
        Log.d(TAG, "BroadCast");

        boolean alarmUp = (PendingIntent.getBroadcast(this, 234324243,
                new Intent(this, ScannerBroadcastReceiver.class),
                PendingIntent.FLAG_NO_CREATE) != null);

        if (alarmUp) {
            Log.d("myTag", "#657456 Alarm is already active");
            if (!SMSAutoBackup) {
                Intent intent = new Intent(this, ScannerBroadcastReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this.getApplicationContext(), 234324243, intent, 0);
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
                Log.d(TAG, "FileAutoBackUpBroadCast: #657456 AlarmCancled: Auto Sync OFF");

            }
        } else {
            Log.d("myTag", "#657456 Alarm is not active");

            int i = 10;
            Intent intent = new Intent(this, ScannerBroadcastReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this.getApplicationContext(), 234324243, intent, 0);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

            //  alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
            //        + (i * 1000), pendingIntent);
            if (SMSAutoBackup && isSubscribed) {

                Log.d(TAG, "FileAutoBackUpBroadCast: FileSyncIntervals: " + sharedPrefAutoBackup.getString(getResources().getString(R.string.settings_sync_interval), null));
                String syncinterval = sharedPrefAutoBackup.getString(getResources().getString(R.string.settings_sync_interval), null);
                String[] syncintervalArray = getResources().getStringArray(R.array.auto_sync_intervals);

                int syncINT;
                if (syncinterval == null) {
                    syncinterval = syncintervalArray[3];
                }

                if (syncinterval.equals(syncintervalArray[0])) {
                    // 24 Hrs Sync
                    alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_DAY,
                            AlarmManager.INTERVAL_DAY, pendingIntent);
                    syncINT = 24;
                } else if (syncinterval.equals(syncintervalArray[1])) {
                    // 12 Hrs Sync
                    alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HALF_DAY,
                            AlarmManager.INTERVAL_HALF_DAY, pendingIntent);
                    syncINT = 12;
                } else if (syncinterval.equals(syncintervalArray[2])) {
                    // 1 Hrs Sync
                    alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HOUR,
                            AlarmManager.INTERVAL_HOUR, pendingIntent);

                    syncINT = 1;
                } else if (syncinterval.equals(syncintervalArray[3])) {
                    // half Hrs Sync
                    alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HALF_HOUR,
                            AlarmManager.INTERVAL_HALF_HOUR, pendingIntent);

                }/* else if (syncinterval.equals(syncintervalArray[4])) {
                    // 15 min Sync
                    alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                            AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);

                }
                */ else {
                    // 1 Hrs Sync
                    alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HOUR,
                            AlarmManager.INTERVAL_HOUR, pendingIntent);
                    syncINT = 12;
                }

            /*
 Constraints constraints = new Constraints.Builder()
                    .setRequiresCharging(true)
                    .build();
          */

  /*              PeriodicWorkRequest saveRequest =
                        new PeriodicWorkRequest.Builder(SyncWorkManager.class, syncINT, TimeUnit.HOURS)
                                //  .setConstraints(constraints)
                                .build();
                WorkManager.getInstance(this)
                        .enqueue(saveRequest);
*/
            } else {
                Log.d(TAG, "FileAutoBackUpBroadCast: #657456 AlarmCancled: Auto Sync OFF");
                alarmManager.cancel(pendingIntent);
            }

        }


//        Toast.makeText(this, "File upload  " + i + " seconds", Toast.LENGTH_LONG).show();

    }

    void downloadCloudSMS() {

      /*   try{
            AddSmsDB addSmsDB = new AddSmsDB();
            addSmsDB.execute();

        }catch (Exception e){
            Log.e(TAG, "AppStart:  #45631 ", e);
        }

       Log.d(TAG, "downloadCloudSMS:  downloadCloudSMS()");
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
                                        Function.createCachedFile(MainActivity.this, getString(R.string.file_cloud_sms), CloudSms);
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
                                        Function.createCachedFile(MainActivity.this, getString(R.string.file_cloud_thread), CloudSms);
                                        Log.d(TAG, "onDataChange: createCachedFile file_cloud_thread ");
                                    } catch (Exception e) {
                                        Log.d(TAG, "onDataChange: ERROR #5600 : " + e);
                                        Crashlytics.logException(e);

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

     /*                   }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle failed download
                    Log.d(TAG, "onFailure: ERROR #546766 " + exception + " \n message: " + exception.getMessage());


                    Log.d(TAG, "onFailure: ERROR CloudSms.clear();");
                    CloudSms.clear();
                    CloudThreadSms.clear();
                    try {
                        Function.createCachedFile(MainActivity.this, getString(R.string.file_cloud_sms), CloudSms);

                        Function.createCachedFile(MainActivity.this, getString(R.string.file_cloud_thread), CloudThreadSms);
                        Log.d(TAG, "onDataChange: createCachedFile file_cloud_thread ");
                    } catch (Exception e) {
                        Log.d(TAG, "onDataChange: ERROR #5600 : " + e);
                        Crashlytics.logException(e);

                    }


                    //    UploadToCloud(smsList);

                }
            });
        }
        */


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
                    //    Log.d(TAG, "RemoveDuplicateHashMaps: Duplicate Found");
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

    /*  void getContacts() {
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
  */
    public void setDefaultSmsApp(View v) {
/*
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
*/

    }

    void isDefaultApp() {
  /*      boolean a;
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
        //  isDefaultSmsApp=a;
    }

    public void saveMSGtoDevice(View v) {
        Intent intent = new Intent(this, RestoreWizardActivity.class);

        //  String message = editText.getText().toString();
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    /*
    ArrayList<HashMap<String, String>> cSMS=new ArrayList<>();

    try {
        cSMS= (ArrayList<HashMap<String, String>>) Function.readCachedFile(MainActivity.this, getString(R.string.file_cloud_sms));
        DeviceSMS= (ArrayList<HashMap<String, String>>) Function.readCachedFile(MainActivity.this, getString(R.string.file_device_sms));
        sortCloudSMS(cSMS,DeviceSMS);

    }catch (Exception e){
        Log.d(TAG, "saveMSGtoDvice: ERROR #5643 "+e.toString());
    }
    */
    }

    void sortCloudSMS(ArrayList<HashMap<String, String>> c, ArrayList<HashMap<String, String>> d) {

        int t = d.size() - 1;
        int tc = c.size() - 1;
        Log.d(TAG, "sortCloudSMS: Data: Device:" + t + "\nCloud:" + tc + "\n\ndevice sms:" + d);
        int saved;
        saved = 0;
        for (int i = 0; i <= tc; i++) {
            String msg = c.get(i).get(Function.KEY_MSG);
            String phone = c.get(i).get(Function.KEY_PHONE);
            String timestamp = c.get(i).get(Function.KEY_TIMESTAMP);

            Log.d(TAG, "sortCloudSMS: Sorting MSG " + i);
            for (int x = 0; x <= t; x++) {

                if (msg.equals(d.get(x).get(Function.KEY_MSG))
                        && phone.equals(d.get(x).get(Function.KEY_PHONE))
                        && timestamp.equals(d.get(x).get(Function.KEY_TIMESTAMP))) {
                    Log.d(TAG, "sortCloudSMS: Identical Messages: " + c.get(x).get(Function.KEY_PHONE) + "\n t:" + c.get(x).get(Function.KEY_TIMESTAMP));
                } else {
                    saved = saved + 1;
                    // Save into Device
                    Log.d(TAG, "sortCloudSMS: Saving on Device: " + c.get(x).get(Function.KEY_PHONE) + "\n t:" + c.get(x).get(Function.KEY_TIMESTAMP) + "\n saved:" + saved);
                }

            }

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

    void fbFunction() {
        mFunctions = FirebaseFunctions.getInstance();
//addMessage("dd");
    }

    private Task<String> addMessage(String text) {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("date", "date");
        data.put("purchaseid", "purchaseid");
        data.put("expirydate", "expirydate");
        data.put("fdate", "fdate");
        data.put("fexpdate", "fexpdate");

        return mFunctions
                .getHttpsCallable("smsDrivePurchaseSave")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        String result = (String) task.getResult().getData();
                        Log.d(TAG, "FUNCTIONS then: resut:" + result);
                        return result;
                    }
                });
    }

    public void openMySubscription(View v) {
        Intent intent = new Intent(this, PaymentActivity.class);

        //  String message = editText.getText().toString();
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    public void cloudSMSRefresh(View v) {
        Toast.makeText(this, "Refreshing Cloud SMS Data in Background...Please wait", Toast.LENGTH_LONG).show();

        Log.d(TAG, "downloadCloudSMS:  downloadCloudSMS()");
        DownloadCloudMessagesService.enqueueWork(this, new Intent());


    }

  /*  public ArrayList<HashMap<String, String>> removeDuplicatesCreateList(ArrayList<HashMap<String, String>> smsList) {
        ArrayList<HashMap<String, String>> gpList = new ArrayList<HashMap<String, String>>();
        double total = smsList.size();
        double progress = 0;
        for (int i = 0; i < smsList.size(); i++) {

            double prg = i / total * 100;
            progress = (int) prg;
            cloudRefreshProgressBar.setProgress(String.valueOf(progress));

            Log.d("SMS Drive|MainActivity", "removeDuplicates: " + progress + "% | " + i + "/" + total);
            boolean available = false;
            for (int j = 0; j < gpList.size(); j++) {
                if (Integer.parseInt(gpList.get(j).get(Function.KEY_THREAD_ID)) == Integer.parseInt(smsList.get(i).get(Function.KEY_THREAD_ID))) {
                    available = true;
                    break;
                }
            }

            if (!available) {
                gpList.add(Function.mappingInbox(smsList.get(i).get(Function._ID), smsList.get(i).get(Function.KEY_THREAD_ID),
                        smsList.get(i).get(Function.KEY_NAME), smsList.get(i).get(Function.KEY_PHONE),
                        smsList.get(i).get(Function.KEY_MSG), smsList.get(i).get(Function.KEY_TYPE),
                        smsList.get(i).get(Function.KEY_TIMESTAMP), smsList.get(i).get(Function.KEY_TIME)
                        , smsList.get(i).get(Function.KEY_READ)
                ));
            }
        }
        return gpList;
    }

    public void cloudSmsRefreshFinish() {

    }
*/
    void setPreferenceListner() {
        //  LLBGmsgprocessing=findViewById(R.id.LLBGmsgprocessing);

        sharedPrefAppGeneral = PreferenceManager.getDefaultSharedPreferences(this);
        //SharedPreferences.Editor editor = sharedPrefAutoBackup.edit();
        //  editor.putString(getString(R.string.settings_pref_username), UserName).apply();

//Setup a shared preference listener for hpwAddress and restart transport
        AppGenPrefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if (key.equals(getString(R.string.BG_Task_Status))) {
                    //Do stuff; restart activity in your case
                    bg_TASK_STATUS = sharedPrefAppGeneral.getString(getString(R.string.BG_Task_Status), "0");
                    if (bg_TASK_STATUS.equals("1")) {
                        // LLBGmsgprocessing.setVisibility(View.VISIBLE);
                    } else {
                        //  LLBGmsgprocessing.setVisibility(View.GONE);
                    }
                }
                if (key.equals(getString(R.string.cache_Sub_isSubscribe))) {
                    String sub = sharedPrefAppGeneral.getString(getString(R.string.cache_Sub_isSubscribe), "0");
                    try {
                        if (sub.equals("1")) {
                            isSubscribed = true;
                            LLSubCardView.setVisibility(View.GONE);
                            LLSyncCardView.setVisibility(View.VISIBLE);

                        } else {
                            isSubscribed = false;
                            LLSubCardView.setVisibility(View.VISIBLE);
                            LLSyncCardView.setVisibility(View.VISIBLE);

                        }
                    } catch (Exception e) {
                        // isSubscribed = false;
                        Log.e(TAG, "AppStart: ERROR #01011 ", e);
                    }

                }

            }
        };

        sharedPrefAppGeneral.registerOnSharedPreferenceChangeListener(AppGenPrefListener);

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

    void checkBGRunningServices() {
        if (isMyServiceRunning(SyncIntentService.class) || isMyServiceRunning(CloudSMS2DBService.class)
                || isMyServiceRunning(DownloadCloudMessagesService.class)) {
            LLBGmsgprocessing.setVisibility(View.VISIBLE);
        } else {
            LLBGmsgprocessing.setVisibility(View.GONE);
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    private boolean isNetworkAvailable() {
        boolean isConnected = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Register Callback - Call this in your app start!
            //     CheckNetwork network = new CheckNetwork(getApplicationContext());
            //     network.registerNetworkCallback();

            // Check network connection
            // Internet Connected
            // Not Connected
            isConnected = CheckNetwork.isNetworkConnected;
        } else {

            ConnectivityManager cm =
                    (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
            try {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                //NetworkInfo activeNetwork1 = cm.NetworkCallback();
                isConnected = activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting();
            } catch (Exception e) {
                Log.e(TAG, "isNetworkAvailable: ERROR #54 ", e);
            }


        }
        Log.d(TAG, "isNetworkAvailable: isConnected: " + isConnected);

        return isConnected;

     /*     ConnectivityManager connectivityManager = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();

           boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
       try {
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

    public void stopLoadingRefreshView() {
        new Thread() {
            public void run() {
                try {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            mySwipeRefreshLayout = findViewById(R.id.swipeRefresh);

                            mySwipeRefreshLayout.setRefreshing(false);
                        }
                    });
//                    Thread.sleep(300);
                } catch (Exception e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }
            }
        }.start();


    }

    void FirebaseMessagingServiceAct() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(FirebaseMesagingTAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        // Log and toast
                        String msg = getString(R.string.msg_token_fmt, token);
                        Log.d(FirebaseMesagingTAG, msg);
                        // Toast.makeText(StartActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    void CheckMultiAppUsage() {
        String AppInstanceID = sharedPrefAppGeneral.getString(getString(R.string.App_InstanceID), "");
        DatabaseReference AppInstanceIDDB = database.getReference("/users/" + UserUID + "/smsdrive/instanceid");
// Read from the database
        AppInstanceIDDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String AppInstanceIDReged = dataSnapshot.getValue(String.class);
                Log.d(TAG, "DB AppInstanceID: " + AppInstanceIDReged);
                if (dataSnapshot.exists()) {
                    if (AppInstanceIDReged.equals(AppInstanceID)) {
                        Log.d(TAG, "onDataChange: App installed on single device");
                    } else {
                        if (isSubscribed) {
                            Log.d(TAG, "onDataChange: App installed on multiple device with subscription");
                        } else {
                            Log.d(TAG, "onDataChange: App installed on multiple device with non-subscription");
                            Toast.makeText(getApplicationContext(), "Please Login Again", Toast.LENGTH_LONG).show();
                            //       Toast.makeText(getApplicationContext(), "get Subscription of SMS Drive for Multi Device Sync", Toast.LENGTH_LONG).show();

                            FirebaseAuth.getInstance().signOut();
                            deleteAppData();

                        }
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

    private void deleteAppData() {
        try {
            // clearing app data
            String packageName = getApplicationContext().getPackageName();
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("pm clear " + packageName);
            Log.i(TAG, "App Data Cleared !!");

            Intent intent = new Intent(this, StartActivity.class);
            startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void showGettingStarted() {
        RLGettingStarted = findViewById(R.id.RLGettingStarted);
        webviewGettingStarted = findViewById(R.id.webviewGettingStarted);

        RLGettingStarted.setVisibility(View.VISIBLE);
        webviewGettingStarted.loadUrl("file:///android_asset/html/appabout.html");   // now it will not fail here
        WebSettings webSettings = webviewGettingStarted.getSettings();
        webSettings.setJavaScriptEnabled(true);
    }

    public void closeGettingStarted(View v) {
        RLGettingStarted.setVisibility(View.GONE);

    }

    public void NULLL(View v) {
        Log.d(TAG, "NULLL: NULL AREA CLICK");
    }

    void showSyncDialogue() {

        // Build an AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alertdialog_custom_view, null);

        // Specify alert dialog is not cancelable/not ignorable
        builder.setCancelable(false);

        // Set the custom layout as alert dialog view
        builder.setView(dialogView);

        // Get the custom alert dialog view widgets reference
        Button btn_positive = dialogView.findViewById(R.id.dialog_positive_btn);
        Button btn_negative = dialogView.findViewById(R.id.dialog_negative_btn);
        final EditText et_name = dialogView.findViewById(R.id.et_name);

        // Create the alert dialog
        final AlertDialog dialog = builder.create();

        // Set positive/yes button click listener
        btn_positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dismiss the alert dialog
                dialog.cancel();
                String name = et_name.getText().toString();
                Log.d(TAG, "onClick: Dialogue: " + name);

            }
        });

        // Set negative/no button click listener
        btn_negative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dismiss/cancel the alert dialog
                //dialog.cancel();
                dialog.dismiss();

            }
        });

        // Display the custom alert dialog on interface
        dialog.show();


    }


    void CreateNotification(String title) {

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, StartActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.notification_update_channel))
                .setSmallIcon(R.drawable.app_logo)
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setColor(getResources().getColor(R.color.colorAccent))
                //.setContentText(message)
                .setSound(null, AudioManager.STREAM_NOTIFICATION)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String CHANNEL_ID = getString(R.string.notification_update_channel);
            CharSequence name = "App Update";
            String Description = "Receive notification when Latest app version is available";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel mChannel;
            mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableVibration(true);

            notificationManager.createNotificationChannel(mChannel);
        }
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(Integer.parseInt(getString(R.string.notification_update_channel)), builder.build());


    }

    /**
    public boolean isDefaultSmsApp() {
        if (OsUtil.isAtLeastKLP()) {
            final String configuredApplication = Telephony.Sms.getDefaultSmsPackage(this);
            return  getPackageName().equals(configuredApplication);
        }
        return true;
    }

     * Get default SMS app package name
     *
     * @return the package name of default SMS app
     */




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

            Log.d(TAG, "onPreExecute: ");

            smsList.clear();
            //   loadingCircle.setVisibility(View.VISIBLE);
            try {
                mySwipeRefreshLayout.setRefreshing(true);
            } catch (Exception e) {
                Log.e(TAG, "onPreExecute: ERROR #45645 ", e);
                Crashlytics.logException(e);
            }

           /* try{
                Function.createCachedFile(MainActivity.this, "orgsms", null);
            }catch (Exception e){

            }
            */


        }

        protected String doInBackground(String... args) {
            Log.d(TAG, "doInBackground: ");
            String xml = "";
            if (isDefaultSmsApp) {

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

  /*                          Sms u=new Sms();
                            //u.uid= 1;

                            u.ID=_id;
                            u.KEY_THREAD_ID=thread_id;
                            u.KEY_NAME=name;
                            u.KEY_PHONE=phone;
                            u.KEY_MSG=msg;
                            u.KEY_TYPE=type;
                            u.KEY_TIMESTAMP=timestamp;
                            u.KEY_TIME=Function.converToTime(timestamp);
                            u.KEY_READ=read;

                            db.userDao().insertAllr(u);
*/

                            smsList.add(Function.mappingInbox(_id, thread_id, name, phone, msg, type, timestamp, Function.converToTime(timestamp), read));
                            DeviceSMS.add(Function.mappingInbox(_id, thread_id, name, phone, msg, type, timestamp, Function.converToTime(timestamp), read));
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
                    Crashlytics.logException(e);
                    stopLoadingRefreshView();
                }

                try {
                    Function.createCachedFile(MainActivity.this, "orgsms", smsList);
                    Function.createCachedFile(MainActivity.this, getString(R.string.file_device_sms), DeviceSMS);

                    Log.d(TAG, "doInBackground: createCachedFile ORG SMS CREATED");
                } catch (Exception e) {
                    Crashlytics.logException(e);
                    stopLoadingRefreshView();

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
                    Crashlytics.logException(e);
                    stopLoadingRefreshView();

                }
                // Updating cache data

            }
            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {
            Log.d(TAG, "onPostExecute: ");
            loadingCircle.setVisibility(View.GONE);
            stopLoadingRefreshView();

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
                try {
                    mySwipeRefreshLayout = findViewById(R.id.swipeRefresh);

                    mySwipeRefreshLayout.setRefreshing(false);
                } catch (Exception e) {
                    Log.e(TAG, "onPreExecute: ERROR #6645 ", e);
                    Crashlytics.logException(e);
                }

                if (onceOpen == 0) {
                    AppStart();
                    onceOpen = 1;
                }


            }

            processCloudThread = new ProcessCloudSmsThread();
            processCloudThread.execute();

            //  Intent intent = new Intent(MainActivity.this, DownloadCloudMessagesService.class);

            //  String message = editText.getText().toString();
            //intent.putExtra(EXTRA_MESSAGE, message);
            //  startService(intent);


        }
    }

    class ProcessCloudSmsThread extends AsyncTask<String, Void, String> {
        final String TAG = "PrsCldSmsThread";

        AppDatabase db1;
        AppDatabase ThreadSmsDB1;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "onPreExecute");
            ThreadSmsDB1 = Room.databaseBuilder(getApplicationContext(),
                    AppDatabase.class, getString(R.string.DATABASE_THREAD_SMS_DB)).fallbackToDestructiveMigration()
                    .build();

            db1 = Room.databaseBuilder(getApplicationContext(),
                    AppDatabase.class, getString(R.string.DATABASE_CLOUD_SMS_DB)).fallbackToDestructiveMigration()
                    .build();


        }

        protected String doInBackground(String... args) {
            Log.d(TAG, "doInBackground: ");
            String xml = "";
            List<Sms> ls = db1.userDao().getAll();
            Log.d(TAG, "doInBackground: db.userDao().getAll() :" + ls.toString());
            if (ls != null) {
                Log.d(TAG, "doInBackground: START");

                Log.d(TAG, "doInBackground: db.userDao().getAll() ");

                Log.d(TAG, "doInBackground: List<Sms> lsClean = Function.removeDuplicates1(ls);");
                ArrayList<HashMap<String, String>> CloudSMS = Function.ConvertListSms2Arraylist(ls);
                Collections.sort(CloudSMS, new MapComparator(Function.KEY_TIMESTAMP, "dsc")); // Arranging sms by timestamp decending
                List<Sms> lsClean = Function.ConvertArraylist2ListSms(CloudSMS);
                List<Sms> FinalSmsThread = Function.removeDuplicates1(lsClean);
                Log.d(TAG, "doInBackground: END");
                Log.d(TAG, "doInBackground: FinalSmsThread ");
                ThreadSmsDB1.userDao().insertAllThread(FinalSmsThread);
                Log.d(TAG, "doInBackground: END");


            }
            return "Done";
        }

        @Override
        protected void onPostExecute(String xml) {
            Log.d(TAG, "onPostExecute");

            if (ThreadSmsDB1 != null) {
                ThreadSmsDB1.close();
            }
            if (db1 != null) {
                db1.close();
            }
        }


    }



/*
isMyServiceRunning(MyService.class)

private boolean isMyServiceRunning(Class<?> serviceClass) {
    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
        if (serviceClass.getName().equals(service.service.getClassName())) {
            return true;
        }
    }
    return false;
}
    */
}
