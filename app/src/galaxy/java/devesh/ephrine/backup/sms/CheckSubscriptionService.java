package devesh.ephrine.backup.sms;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import androidx.preference.PreferenceManager;

import com.lifeofcoding.cacheutlislibrary.CacheUtils;

public class CheckSubscriptionService extends Service {
    SharedPreferences sharedPrefAutoBackup;
    SharedPreferences sharedPrefAppGeneral;

    public CheckSubscriptionService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //sharedPrefAutoBackup = PreferenceManager.getDefaultSharedPreferences(this /* Activity context */);
        sharedPrefAppGeneral = PreferenceManager.getDefaultSharedPreferences(this /* Activity context */);
        SharedPreferences.Editor editor = sharedPrefAppGeneral.edit();
        editor.putString(getString(R.string.cache_Sub_isSubscribe), "1").apply();
        CacheUtils.writeFile(getString(R.string.cache_Sub_isSubscribe), "1");

    }
}
