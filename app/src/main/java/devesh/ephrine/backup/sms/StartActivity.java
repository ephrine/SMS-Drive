package devesh.ephrine.backup.sms;

import android.content.Context;
import android.content.Intent;
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

import java.util.Arrays;
import java.util.List;

import io.fabric.sdk.android.Fabric;

public class StartActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;
    private static final int DEFAULT_SMS_CODE = 1;
    final String TAG = "StartActivity ";
    final Boolean isDefaultSmsApp = true;
    FirebaseUser currentUser;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

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

        Intent intent = new Intent(StartActivity.this, MainActivity.class);
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
            StartActivity.this.finish();

        }
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                if (isDefaultSmsApp) {

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    Intent intent = new Intent(this, MainActivity.class);

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
            if (!Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName)) {
                a = false;
            } else {
                a = true;
            }
        } else {
            a = true;
            // saveSms("111111", "mmmmssssggggg", "0", "", "inbox");
        }

        //   isDefaultSmsApp=a;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
