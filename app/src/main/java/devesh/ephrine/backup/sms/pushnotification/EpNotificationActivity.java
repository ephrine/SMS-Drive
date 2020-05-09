package devesh.ephrine.backup.sms.pushnotification;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;

import devesh.ephrine.backup.sms.BuildConfig;
import devesh.ephrine.backup.sms.Flavours;
import devesh.ephrine.backup.sms.Function;
import devesh.ephrine.backup.sms.MainActivity;
import devesh.ephrine.backup.sms.MapComparator;
import devesh.ephrine.backup.sms.R;

public class EpNotificationActivity extends AppCompatActivity {
    LinkedHashSet<HashMap<String, String>> notificationsDataHash = new LinkedHashSet<>();
    String TAG = "EpNotiAct";
RecyclerView recyclerView;
RecyclerView.LayoutManager layoutManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ep_notification_activity);
        recyclerView = findViewById(R.id.notificationRecycleView453);
        layoutManager = new LinearLayoutManager(this);

      loadNotifications();


    }

    void loadNotifications(){
        try {
            notificationsDataHash = (LinkedHashSet<HashMap<String, String>>) Function.readCachedFile(this, getString(R.string.FCM_Notifications_Data));

        } catch (Exception e) {
            Log.e(TAG, "onCreate: ERROR #654", e);
        }

        ArrayList<HashMap<String, String>> notificationsDataHash2 = new ArrayList<>(notificationsDataHash);
        Collections.sort(notificationsDataHash2, new MapComparator("time", "dsc")); // Arranging sms by timestamp decending

        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        recyclerView.setLayoutManager(layoutManager);
        // specify an adapter (see also next example)

        EpNotificationAdapter mAdapter = new EpNotificationAdapter(this, notificationsDataHash2);

        recyclerView.setAdapter(mAdapter);
    }

    public void openBrowser(String url){
if(url.contains("https") || url.contains("http")){

}else {
    url="https://"+url;
}



        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();

/*if(BuildConfig.FLAVOR.equals(Flavours.FLAVOUR_GALAXY)){
    customTabsIntent.intent.setPackage("com.sec.android.app.sbrowser");
}
if(BuildConfig.FLAVOR.equals(Flavours.FLAVOUR_MASTER)){
    customTabsIntent.intent.setPackage("com.android.chrome");
}*/
        //customTabsIntent.intent.setPackage("com.android.chrome");
        if(url.contains("play.google.com") || url.contains("galaxy.store") || url.contains("apps.samsung.com")){
// App Update
            if(BuildConfig.FLAVOR.equals(Flavours.FLAVOUR_GALAXY)) {
                //url = "https://galaxy.store/smsdrive";
                url=getString(R.string.GalaxyUpdateURL);
            }
            if(BuildConfig.FLAVOR.equals(Flavours.FLAVOUR_MASTER)){

            }
        }else{
//News & updates
            if(isPackageInstalled("com.sec.android.app.sbrowser",getPackageManager())){
                customTabsIntent.intent.setPackage("com.sec.android.app.sbrowser");

            }else if(isPackageInstalled("com.android.chrome",getPackageManager())){
                customTabsIntent.intent.setPackage("com.android.chrome");

            }
        }

        builder.setToolbarColor(ContextCompat.getColor(this,R.color.colorPrimary));
        builder.setShowTitle(true);
        builder.addDefaultShareMenuItem();
        builder.build().launchUrl((Activity) this, Uri.parse(url));

    }
    private boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public void clearNotificationsData(View v){
        notificationsDataHash.clear();
        try {

            Function.createCachedNotificationFile(this,getString(R.string.FCM_Notifications_Data),notificationsDataHash);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "onMessageReceived: ERROR #2343 ",e );
        }

        loadNotifications();
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }
}
