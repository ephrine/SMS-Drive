package devesh.ephrine.backup.sms.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class SyncService extends Service {

    final String TAG = "SyncWorkManager";
    final boolean isDefaultApp = true;
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
    ArrayList<HashMap<String, String>> smsList = new ArrayList<>();
    ArrayList<HashMap<String, String>> customList = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> tmpList = new ArrayList<HashMap<String, String>>();
    String name;


    public SyncService() {


    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
