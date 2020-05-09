package devesh.ephrine.backup.sms;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import devesh.ephrine.backup.sms.broadcastreceiver.MyBroadcastReceiver;
import devesh.ephrine.backup.sms.pushnotification.EpNotificationActivity;
import devesh.ephrine.backup.sms.pushnotification.EpNotificationsConstants;
import io.fabric.sdk.android.Fabric;

public class StartActivity extends AppCompatActivity {
    // Constants
    // The authority for the sync adapter's content provider
    public static final String AUTHORITY = "com.example.android.datasync.provider";
    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "devesh.ephrine.backup.sms";
    // The account name
    public static final String ACCOUNT = "dummyaccount";
    private static final int RC_SIGN_IN = 123;
    private static final int DEFAULT_SMS_CODE = 1;
    final String TAG = "StartActivity ";

    final Boolean isDefaultSmsApp = true;
    FirebaseUser currentUser;
    // Instance fields
    Account mAccount;
    private FirebaseAuth mAuth;

    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
    public static void CreateSyncAccount(Context context) {
        // Create the account type and default account
        Account newAccount = new Account(
                ACCOUNT, ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(
                        ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
       /*  if (accountManager.addAccountExplicitly(newAccount, null, null)) {

             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.

        } else {*/
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.

        }*/

    }
boolean openNotificationActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
openNotificationActivity=false;
        if (getIntent().getExtras() != null) {
            String url = null;
            String title = null;
            String desc = null;
            String time = null;

            LinkedHashSet<HashMap<String, String>> notificationsDataHash = new LinkedHashSet<>();

            for (String key : getIntent().getExtras().keySet()) {
                String value = getIntent().getExtras().getString(key);
                Log.d(TAG, "FCM DATALOAD \nKey: " + key + " Value: " + value);

                if (key.equals(EpNotificationsConstants.EP_FCM_URL)) {
                    url = value;
                }
                if (key.equals(EpNotificationsConstants.EP_FCM_TITLE)) {
                    title = value;
                }
                if (key.equals(EpNotificationsConstants.EP_FCM_DESC)) {
                    desc = value;
                }


            }
            time = String.valueOf(System.currentTimeMillis());
            if (title != null) {

                HashMap<String, String> data = new HashMap<>();
                data.put(EpNotificationsConstants.EP_FCM_URL, url);
                data.put(EpNotificationsConstants.EP_FCM_TITLE, title);
                data.put(EpNotificationsConstants.EP_FCM_DESC, desc);
                data.put("time", time);

                try {
                    notificationsDataHash = (LinkedHashSet<HashMap<String, String>>) Function.readCachedFile(this, getString(R.string.FCM_Notifications_Data));
                   
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "onMessageReceived: ERROR #5234 ", e);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    Log.e(TAG, "onMessageReceived: ERROR #65 ", e);
                }

                notificationsDataHash.add(data);

                try {
                    Function.createCachedNotificationFile(this, getString(R.string.FCM_Notifications_Data), notificationsDataHash);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                openNotificationActivity=true;

            }


        }

        Fabric.with(this, new Crashlytics());

        AppCenter.start(getApplication(), BuildConfig.MS_AppCenter_Key,
                Analytics.class, Crashes.class);
        setContentView(R.layout.activity_start);
        getSupportActionBar().hide();
        mAuth = FirebaseAuth.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();


        Thread background = new Thread() {
            public void run() {

                try {
                    // Thread will sleep for 5 seconds
                    sleep(2 * 1000);

                    if (currentUser != null) {

                        if (isDefaultSmsApp) {
                            appstart();
                        } else {
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    setContentView(R.layout.set_default);

                                    // Stuff that updates the UI

                                }
                            });
                        }

                    } else {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                setContentView(R.layout.activity_login);

                                // Stuff that updates the UI

                            }
                        });

                    }


                } catch (Exception e) {
                    Log.d(TAG, "run: ERROR \n" + e);
                }
            }
        };

        background.start();



/*
        if (currentUser != null) {
            Crashlytics.setUserIdentifier(currentUser.getUid());

            Intent intent = new Intent(this, MainActivity.class);
            //  String message = editText.getText().toString();
            //intent.putExtra(EXTRA_MESSAGE, message);
            startActivity(intent);
            StartActivity.this.finish();
        } else {
            // Choose authentication providers
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    //   new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.PhoneBuilder().build()
                    //   new AuthUI.IdpConfig.GoogleBuilder().build(),
                    //  new AuthUI.IdpConfig.FacebookBuilder().build(),
                    //        new AuthUI.IdpConfig.TwitterBuilder().build()
            );
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN);
        }
*/


    }

    @Override
    public void onStart() {
        super.onStart();
        isDefaultApp();

    }

    void appstart() {
        Crashlytics.setUserIdentifier(currentUser.getUid());
        Intent intent;
        if(openNotificationActivity){
            intent= new Intent(StartActivity.this, EpNotificationActivity.class);
}else{
            intent= new Intent(StartActivity.this, MainActivity.class);
        }

        startActivity(intent);
        StartActivity.this.finish();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DEFAULT_SMS_CODE) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            Intent intent = new Intent(this, MainActivity.class);

            //  String message = editText.getText().toString();
            //intent.putExtra(EXTRA_MESSAGE, message);
            startActivity(intent);
            //    CreateSyncAccount(this);

            StartActivity.this.finish();

        }
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                if (isDefaultSmsApp) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                  /*  AccountManager accountManager = AccountManager.get(this); //this is Activity
                    Account account = new Account(user.getPhoneNumber(),"devesh.ephrine.backup.sms.ACCOUNT");

                    boolean success = accountManager.addAccountExplicitly(account,"password",null);
                    if(success){
                        Log.d(TAG,"Account created");
                    }else{
                        Log.d(TAG,"Account creation failed. Look at previous logs to investigate");
                    }
                    */

                    // FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    Intent intent = new Intent(this, MainActivity.class);
                    //   CreateSyncAccount(this);

                    //  String message = editText.getText().toString();
                    //intent.putExtra(EXTRA_MESSAGE, message);
                    startActivity(intent);

                    StartActivity.this.finish();
                } else {
                    setContentView(R.layout.set_default);
                }
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }

    public void loginbutton(View v) {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                //   new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build()
                //   new AuthUI.IdpConfig.GoogleBuilder().build(),
                //  new AuthUI.IdpConfig.FacebookBuilder().build(),
                //        new AuthUI.IdpConfig.TwitterBuilder().build()
        );

// Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setLogo(R.drawable.ephrinelogo)
                        .build(),
                RC_SIGN_IN);
    }

    public void privacyClick(View v) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.ephrine.in/privacy-policy"));
        startActivity(intent);

    }

    public void setDefaultSmsApp1(View v) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final String myPackageName = getPackageName();
            if (!Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName)) {

                //          Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                //        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, myPackageName);
                //      startActivityForResult(intent, 1);
                //      isDefaultSmsApp=true;

            } else {
                //      isDefaultSmsApp=true;
                appstart();
            }

        } else {
            //      isDefaultSmsApp=true;
            // saveSms("111111", "mmmmssssggggg", "0", "", "inbox");
            appstart();
        }


    }

    void isDefaultApp() {
        boolean a;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final String myPackageName = getPackageName();
            a = Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName);
        } else {
            a = true;
            // saveSms("111111", "mmmmssssggggg", "0", "", "inbox");
        }

        //   isDefaultSmsApp=a;
    }

    void startEssentialsBGtask() {
        BroadcastReceiver br = new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        this.registerReceiver(br, filter);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
