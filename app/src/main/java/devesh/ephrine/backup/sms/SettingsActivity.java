package devesh.ephrine.backup.sms;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.room.Room;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

import devesh.ephrine.backup.sms.room.AppDatabase;
import io.fabric.sdk.android.Fabric;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        Fabric.with(this, new Crashlytics());

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


    }


    public static class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
        public static final String AUTHORITY = "devesh.ephrine.backup.sms";
        public static final String ACCOUNT_TYPE = "devesh.ephrine.backup.sms.ACCOUNT";

        public static final String FLAVOUR_MASTER = "master";
        public static final String FLAVOUR_GALAXY = "galaxy";
        public static final String FLAVOUR_HUAWEI = "huawei";

        final String TAG = "Settings Activity";
        public String ACCOUNT = "my_custom_account_name";
        // final String DBRoot = "SMSDrive/";
        SharedPreferences sharedPrefAutoBackup;
        SharedPreferences sharedPrefAppGeneral;
        boolean SMSAutoBackup;
        DatabaseReference UserDB;
        String UserUID;
        DatabaseReference DeleteDB;
        boolean isSubscribed;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            Fabric.with(getActivity(), new Crashlytics());
            sharedPrefAutoBackup = PreferenceManager.getDefaultSharedPreferences(getActivity() /* Activity context */);
            sharedPrefAppGeneral = PreferenceManager.getDefaultSharedPreferences(getActivity() /* Activity context */);
            String sub = sharedPrefAppGeneral.getString(getString(R.string.cache_Sub_isSubscribe), "0");

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            ACCOUNT = user.getPhoneNumber();

            try {

                isSubscribed = sub.equals("1");
            } catch (Exception e) {
                isSubscribed = false;
            }
            SMSAutoBackup = sharedPrefAutoBackup.getBoolean(getResources().getString(R.string.settings_sync), false);

            if (SMSAutoBackup && isSubscribed) {
                getPreferenceScreen().findPreference(getString(R.string.settings_sync_interval)).setEnabled(true);
            } else {
                getPreferenceScreen().findPreference(getString(R.string.settings_sync_interval)).setEnabled(false);
            }

            Preference pref = findPreference("pref_userphno");
            // FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                pref.setSummary(user.getPhoneNumber());

            }


            Preference prefSync = findPreference(getString(R.string.settings_sync));
            Preference prefSyncTitleSummary = findPreference("pc_sync_title");

            if (isSubscribed) {
                prefSync.setEnabled(true);
                prefSync.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        //open browser or intent here
                        SMSAutoBackup = sharedPrefAutoBackup.getBoolean(getResources().getString(R.string.settings_sync), false);

                        if (SMSAutoBackup) {
                            getPreferenceScreen().findPreference(getString(R.string.settings_sync_interval)).setEnabled(true);

                        } else {
                            getPreferenceScreen().findPreference(getString(R.string.settings_sync_interval)).setEnabled(false);

                        }

                        return true;
                    }
                });

            } else {
                getPreferenceScreen().findPreference(getString(R.string.settings_sync_interval)).setEnabled(false);

                prefSync.setEnabled(false);
                prefSyncTitleSummary.setSummary("Need Subscription Plan for Auto-Sync");
            }


            Preference PrefSignout = findPreference("signout");
            PrefSignout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    //open browser or intent here

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Do you want to Sign-out ?");
// Add the buttons
                    builder.setPositiveButton("Sign out", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked OK button
                            // Global Variables

                        /*    // Account Manager definition
                            AccountManager accountManager = (AccountManager) getActivity().getSystemService(ACCOUNT_SERVICE);

                            // loop through all accounts to remove them
                            Account[] accounts = accountManager.getAccounts();
                            for (int index = 0; index < accounts.length; index++) {
                                if (accounts[index].type.intern() == AUTHORITY) {
                                    accountManager.removeAccount(accounts[index], null, null);
                                    Log.d(TAG, "onClick: Account Deleted from Main");
                                }
                            }*/
                            FirebaseAuth.getInstance().signOut();
                            deleteAppData();
                            Toast.makeText(getContext(), "Signed out !", Toast.LENGTH_SHORT).show();

                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
// Set other dialog properties

// Create the AlertDialog
                    AlertDialog dialog = builder.create();
                    dialog.show();


                    return true;
                }
            });

            Preference PrefNotifi = findPreference("notif");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PrefNotifi.setVisible(true);
                PrefNotifi.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        Intent settingsIntent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                // .putExtra(Settings.EXTRA_CHANNEL_ID, "001")
                                .putExtra(Settings.EXTRA_APP_PACKAGE, "devesh.ephrine.backup.sms");

                        startActivity(settingsIntent);

                        return true;
                    }
                });

            } else {
                PrefNotifi.setVisible(false);
            }

       /*     Preference PrefPromoCodeKey = findPreference("promocodekey");
            PrefPromoCodeKey.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    Toast.makeText(getActivity(), "Beta Feature", Toast.LENGTH_SHORT).show();
                    // Build an AlertDialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                    LayoutInflater inflater = getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.alertdialog_custom_view, null);

                    // Specify alert dialog is not cancelable/not ignorable
                    builder.setCancelable(false);

                    // Set the custom layout as alert dialog view
                    builder.setView(dialogView);

                    // Get the custom alert dialog view widgets reference
                    Button btn_positive = (Button) dialogView.findViewById(R.id.dialog_positive_btn);
                    Button btn_negative = (Button) dialogView.findViewById(R.id.dialog_negative_btn);
                    final EditText et_name = (EditText) dialogView.findViewById(R.id.et_name);

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


                    return true;
                }
            });
*/
            Preference PrefManageSubscription = findPreference("sub");
            PrefManageSubscription.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    if (BuildConfig.FLAVOR.equals(FLAVOUR_MASTER)) {

                        Intent intent = new Intent(getActivity(), PaymentActivity.class);
                        //  String message = editText.getText().toString();
                        //intent.putExtra(EXTRA_MESSAGE, message);
                        startActivity(intent);

                    } else if (BuildConfig.FLAVOR.equals(FLAVOUR_GALAXY)) {

                        Intent intent = new Intent(getActivity(), PaymentActivity.class);
                        //  String message = editText.getText().toString();
                        //intent.putExtra(EXTRA_MESSAGE, message);
                        startActivity(intent);

                    } else if (BuildConfig.FLAVOR.equals(FLAVOUR_HUAWEI)) {
                        Log.d(TAG, "onPreferenceClick: " + FLAVOUR_HUAWEI);
                    }
                    return true;
                }
            });

            Preference PrefDisablePowerSave = findPreference("disablepowersave");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PrefDisablePowerSave.setVisible(true);

                PrefDisablePowerSave.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                        startActivity(intent);
                        Toast.makeText(getActivity(), "Add to Whitelist or Disable Battery Optimization for SMS Drive", Toast.LENGTH_LONG).show();

                        return true;
                    }
                });
            } else {
                PrefDisablePowerSave.setVisible(false);
            }

            Preference ChangeDefaultSMSApp = findPreference("changedefaultsmsapp");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ChangeDefaultSMSApp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        //open browser or intent here
                        Intent intent = new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
                        startActivity(intent);

                        return true;
                    }
                });
            } else {
                ChangeDefaultSMSApp.setVisible(false);

            }


            Preference PrefContact = findPreference("contact");
            PrefContact.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    //open browser or intent here
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://www.ephrine.in/contact"));
                    startActivity(intent);


                    return true;
                }
            });

            Preference Prefprivacy = findPreference("privacyp");
            Prefprivacy.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    //open browser or intent here
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://www.ephrine.in/privacy-policy"));
                    startActivity(intent);

                    return true;
                }
            });

   /*         Preference PrefOpenSource = findPreference("osl");
            PrefOpenSource.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    //open browser or intent here


                    return true;
                }
            });
*/

            Preference Prefwebsite = findPreference("epwebsite");
            Prefwebsite.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    //open browser or intent here
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://www.ephrine.in/"));
                    startActivity(intent);

                    return true;
                }
            });


            Preference Preffb = findPreference("fb");
            Preffb.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    //open browser or intent here
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(getString(R.string.social_media_fb)));
                    startActivity(intent);

                    return true;
                }
            });


            Preference Prefinsta = findPreference("insta");
            Prefinsta.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    //open browser or intent here
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(getString(R.string.social_media_instagram)));
                    startActivity(intent);

                    return true;
                }
            });

            Preference Prefyoutube = findPreference("youtube");
            Prefyoutube.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    //open browser or intent here
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(getString(R.string.social_media_youtube)));
                    startActivity(intent);

                    return true;
                }
            });


            Preference Preftwitter = findPreference("twitter");
            Preftwitter.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    //open browser or intent here
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(getString(R.string.social_media_twitter)));
                    startActivity(intent);

                    return true;
                }
            });


            Preference Preflinkedin = findPreference("linkedin");
            Preflinkedin.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    //open browser or intent here
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(getString(R.string.social_media_linkedin)));
                    startActivity(intent);

                    return true;
                }
            });


            Preference PrefDeleteBooks = findPreference("deletebooks");
            PrefDeleteBooks.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    //open browser or intent here

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Do you want to Delete Backup ?");
                    builder.setIcon(R.drawable.ic_warning_black_24dp);
// Add the buttons
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked OK button
                            DeleteBackup();

                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });

// Set other dialog properties

// Create the AlertDialog
                    AlertDialog dialog = builder.create();
                    dialog.show();


                    return true;
                }
            });


        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                String name = sharedPreferences.getString(getString(R.string.settings_pref_username), null);
                String email = sharedPreferences.getString(getString(R.string.settings_pref_useremail), null);
                String age = sharedPreferences.getString(getString(R.string.settings_pref_userage), null);

                UserUID = user.getPhoneNumber().replace("+", "x");
                UserDB = FirebaseDatabase.getInstance().getReference("/users/" + UserUID + "/profile");

                if (name != null) {
                    if (name.equals("")) {
                    } else {
                        UserDB.child("UserName").setValue(name);
                    }
                }

                if (email != null) {
                    if (email.equals("")) {
                    } else {
                        UserDB.child("UserEmail").setValue(email);
                    }
                }

                if (age != null) {
                    if (age.equals("")) {
                    } else {
                        UserDB.child("UserAge").setValue(age);
                    }
                }

                Log.d(TAG, "onSharedPreferenceChanged: SAVED ");

            }

            if (sharedPreferences.getString(getString(R.string.cache_Sub_isSubscribe), "0").equals("1")) {
                isSubscribed = true;
            }


        }


        private void deleteAppData() {
            try {
                // clearing app data
                String packageName = getContext().getApplicationContext().getPackageName();
                Runtime runtime = Runtime.getRuntime();
                runtime.exec("pm clear " + packageName);
                Log.i(TAG, "App Data Cleared !!");

                Intent intent = new Intent(getContext(), StartActivity.class);
                startActivity(intent);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        void DeleteBackup() {
            AppDatabase db;
            db = Room.databaseBuilder(getContext().getApplicationContext(),
                    AppDatabase.class, getString(R.string.DATABASE_CLOUD_SMS_DB))
                    //.setJournalMode(RoomDatabase.JournalMode.AUTOMATIC)
                    .allowMainThreadQueries()
                    .build();


            Toast.makeText(getContext(), "Delete SMS Backup: Deleting...", Toast.LENGTH_SHORT).show();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                UserUID = user.getPhoneNumber().replace("+", "x");
                // DeleteDB = FirebaseDatabase.getInstance().getReference("/users/" + UserUID + "/sms");
                // DeleteDB.removeValue();

                String BackupStorageDB = "SMSDrive/Users/" + UserUID + "/backup/file_cloud_sms.zip";

                StorageReference mStorageRef;
                mStorageRef = FirebaseStorage.getInstance().getReference();
                StorageReference riversRef = mStorageRef.child(BackupStorageDB);
                riversRef.delete();
                db.userDao().nukeTable();
                ArrayList<HashMap<String, String>> CloudThreadSms = new ArrayList<>();

                try {
                    Function.createCachedFile(getContext().getApplicationContext(), getString(R.string.file_cloud_thread), CloudThreadSms);

                } catch (Exception e) {
                    Log.e(TAG, "DeleteBackup: ERROR #456 ", e);
                }

                Toast.makeText(getContext(), "Delete SMS Backup: Successful", Toast.LENGTH_SHORT).show();

            }


        }


    }


}