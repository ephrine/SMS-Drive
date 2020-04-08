package devesh.ephrine.backup.sms;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.lifeofcoding.cacheutlislibrary.CacheUtils;

import devesh.ephrine.backup.sms.payment.GPlayBillingCheckoutActivity;
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
        final String TAG = "Settings Activity";
        // final String DBRoot = "SMSDrive/";
        SharedPreferences sharedPrefAutoBackup;
        boolean SMSAutoBackup;
        DatabaseReference UserDB;
        String UserUID;
        DatabaseReference DeleteDB;
        boolean isSubscribed;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            Fabric.with(getActivity(), new Crashlytics());
            try {
                if (CacheUtils.readFile(getString(R.string.cache_Sub_isSubscribe)).toString().equals("1")) {
                    isSubscribed = true;
                } else {
                    isSubscribed = false;
                }
            } catch (Exception e) {
                isSubscribed = false;
            }
            sharedPrefAutoBackup = PreferenceManager.getDefaultSharedPreferences(getActivity() /* Activity context */);
            SMSAutoBackup = sharedPrefAutoBackup.getBoolean(getResources().getString(R.string.settings_sync), false);

            if (SMSAutoBackup && isSubscribed) {
                getPreferenceScreen().findPreference(getString(R.string.settings_sync_interval)).setEnabled(true);
            } else {
                getPreferenceScreen().findPreference(getString(R.string.settings_sync_interval)).setEnabled(false);
            }

            Preference pref = findPreference("pref_userphno");
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
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
                prefSyncTitleSummary.setSummary("Need Subscription Plan for Sync");
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

            Preference PrefManageSubscription = findPreference("sub");
            PrefManageSubscription.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {

                    Intent intent = new Intent(getActivity(), GPlayBillingCheckoutActivity.class);
                    //  String message = editText.getText().toString();
                    //intent.putExtra(EXTRA_MESSAGE, message);
                    startActivity(intent);

                    return true;
                }
            });


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

            Preference PrefOpenSource = findPreference("osl");
            PrefOpenSource.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    //open browser or intent here


                    return true;
                }
            });


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
                Toast.makeText(getContext(), "Delete SMS Backup: Successful", Toast.LENGTH_SHORT).show();
            }


        }


    }
}