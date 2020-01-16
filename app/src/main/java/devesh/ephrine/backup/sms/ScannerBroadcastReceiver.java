package devesh.ephrine.backup.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;

public class ScannerBroadcastReceiver extends BroadcastReceiver {
    final String DBRoot = "SMSDrive/";
    Context mContext;
    SharedPreferences sharedPrefAutoBackup;
    String UserUID;
    boolean SMSAutoBackup;
    SMSScan smsscan;
    HashMap<String, ArrayList<HashMap<String, String>>> iThread;
    DatabaseReference SMSBackupDB;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //  throw new UnsupportedOperationException("Not yet implemented");
        mContext = context;
        iThread = new HashMap<>();
        sharedPrefAutoBackup = PreferenceManager.getDefaultSharedPreferences(mContext /* Activity context */);
        SMSAutoBackup = sharedPrefAutoBackup.getBoolean(mContext.getResources().getString(R.string.settings_sync), false);

        smsscan = new SMSScan(mContext);
        smsscan.ScanNow();
        //     iThread=smsscan.GetList();


    }


}
