package devesh.ephrine.backup.sms;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

public class CheckNetwork {
    public static boolean isNetworkConnected ;

    /*
    You need to call the below method once. It register the callback and fire it when there is a change in network state.
    Here I used a Global Static Variable, So I can use it to access the network state in anyware of the application.
    */
    Context mcontext;

    // You need to pass the context when creating the class
    public CheckNetwork(Context context) {

        mcontext = context;
    }

    // Network Check
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void registerNetworkCallback() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) mcontext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkRequest.Builder builder = new NetworkRequest.Builder();

            connectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                                                                   @Override
                                                                   public void onAvailable(Network network) {
                                                                       isNetworkConnected = true; // Global Static Variable
                                                                       Log.d("isNetworkAvailable", "registerNetworkCallback1: "+isNetworkConnected+" (TRUE)");

                                                                   }

                                                                   @Override
                                                                   public void onLost(Network network) {
                                                                       isNetworkConnected = false; // Global Static Variable
                                                                       Log.d("isNetworkAvailable", "registerNetworkCallback2: "+isNetworkConnected+" (FALSE)");

                                                                   }
                                                               }

            );
          //  isNetworkConnected = false;
            Log.d("isNetworkAvailable", "registerNetworkCallback3: "+isNetworkConnected+" (FALSE)");

        } catch (Exception e) {
            isNetworkConnected = false;
            Log.d("isNetworkAvailable", "registerNetworkCallback4: "+isNetworkConnected+" (FALSE)");
        }


    }
}
