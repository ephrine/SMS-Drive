package devesh.ephrine.backup.sms;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.lifeofcoding.cacheutlislibrary.CacheUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.fabric.sdk.android.Fabric;

public class CheckSubscriptionService extends Service implements BillingProcessor.IBillingHandler {
    BillingProcessor bp;
    String LICENSE_KEY = BuildConfig.GLicenseKey; // YOUR LICENSE KEY FROM GOOGLE PLAY CONSOLE HERE
    String SUBSCRIPTION_ID1 = BuildConfig.Google_Play_Subscription_ID1;//YOUR SUBSCRIPTION ID FROM GOOGLE PLAY CONSOLE HERE

    String TAG = "CheckSubscriptionService";
    // IBillingHandler implementation
    String purchaseToken;
    String productId;
    String orderId;
    Date purchaseTime;
    Long purchaseTimeTimeMillis;
    SharedPreferences sharedPrefAutoBackup;
    SharedPreferences sharedPrefAppGeneral;
    private FirebaseFunctions mFunctions;

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
        Fabric.with(this, new Crashlytics());
        mFunctions = FirebaseFunctions.getInstance();
        CacheUtils.configureCache(this);
        sharedPrefAutoBackup = PreferenceManager.getDefaultSharedPreferences(this /* Activity context */);
        sharedPrefAppGeneral = PreferenceManager.getDefaultSharedPreferences(this /* Activity context */);


        bp = new BillingProcessor(this, LICENSE_KEY, this);
        bp.initialize();
        Log.d(TAG, "onCreate: bp.initialize()");


    }

    @Override
    public void onBillingInitialized() {
        Log.d(TAG, "onBillingInitialized: BillingProcessor was initialized and it's ready to purchase");
        /*
         * Called when BillingProcessor was initialized and it's ready to purchase
         */
        checkIfUserIsSusbcribed();
        bp.getSubscriptionTransactionDetails(SUBSCRIPTION_ID1);

        try {
            TransactionDetails td = bp.getSubscriptionTransactionDetails(SUBSCRIPTION_ID1);
            purchaseToken = td.purchaseInfo.purchaseData.purchaseToken;
            productId = td.purchaseInfo.purchaseData.productId;
            orderId = td.purchaseInfo.purchaseData.orderId;
            purchaseTime = td.purchaseInfo.purchaseData.purchaseTime;
            purchaseTimeTimeMillis = purchaseTime.getTime();

            String dd = "purchaseToken:" + purchaseToken + "\nproductId:" + productId + "\norderId:" + orderId + "\npurchaseTime:" + purchaseTime;
            Log.d(TAG, "onBillingInitialized: purchaseInfo: " + dd);

            String temp_purchaseToken = CacheUtils.readFile(getString(R.string.cache_Sub_PurchaseToken));
            if (!temp_purchaseToken.equals(purchaseToken)) {
                checkSubscriptionOnServer("0");
            }

            // write
            CacheUtils.writeFile(getString(R.string.cache_Sub_OrderId), orderId);
            CacheUtils.writeFile(getString(R.string.cache_Sub_ProductId), productId);
            CacheUtils.writeFile(getString(R.string.cache_Sub_PurchaseTimeMillis), String.valueOf(purchaseTimeTimeMillis));
            CacheUtils.writeFile(getString(R.string.cache_Sub_PurchaseToken), purchaseToken);
            CacheUtils.writeFile(getString(R.string.cache_Sub_PurchaseTime), String.valueOf(purchaseTime));
            //  CacheUtils.writeFile(getString(R.string.cache_Sub_isSubscribe), "1");
            SharedPreferences.Editor editor = sharedPrefAppGeneral.edit();
            editor.putString(getString(R.string.cache_Sub_isSubscribe), "1").apply();

            // read
            // String fileContent = CacheUtils.readFile(CACHE_FILE_STRING);

        } catch (Exception e) {
            Log.e(TAG, "onBillingInitialized: ERROR #433235" + e);
            if (!isSusbcribed()) {

                CacheUtils.writeFile(getString(R.string.cache_Sub_OrderId), "0");
                CacheUtils.writeFile(getString(R.string.cache_Sub_ProductId), "0");
                CacheUtils.writeFile(getString(R.string.cache_Sub_PurchaseTimeMillis), "0");
                CacheUtils.writeFile(getString(R.string.cache_Sub_PurchaseToken), "0");
                CacheUtils.writeFile(getString(R.string.cache_Sub_PurchaseTime), "0");

                SharedPreferences.Editor editor = sharedPrefAppGeneral.edit();
                editor.putString(getString(R.string.cache_Sub_isSubscribe), "0").apply();


            }

        }
        try {
            SkuDetails subDetails = bp.getSubscriptionListingDetails(SUBSCRIPTION_ID1);
            String price = subDetails.priceText;
            CacheUtils.writeFile(getString(R.string.cache_Sub_PurchaseTime), "0");

            Log.d(TAG, "onBillingInitialized: subDetails\n price:" + price);

        } catch (Exception e) {
            Log.e(TAG, "onBillingInitialized: #ERROR 43253 ", e);
        }


    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        /*
         * Called when requested PRODUCT ID was successfully purchased
         */
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        /*
         * Called when some error occurred. See Constants class for more details
         *
         * Note - this includes handling the case where the user canceled the buy dialog:
         * errorCode = Constants.BILLING_RESPONSE_RESULT_USER_CANCELED
         */
        Log.d(TAG, "onBillingError: " + error);

    }

    @Override
    public void onPurchaseHistoryRestored() {
        /*
         * Called when purchase history was restored and the list of all owned PRODUCT ID's
         * was loaded from Google Play
         */
    }

    @Override
    public void onDestroy() {
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }

    void checkIfUserIsSusbcribed() {
        boolean purchaseResult = bp.loadOwnedPurchasesFromGoogle();
        if (purchaseResult) {
            TransactionDetails subscriptionTransactionDetails = bp.getSubscriptionTransactionDetails(SUBSCRIPTION_ID1);
            if (subscriptionTransactionDetails != null) {
                //User is still subscribed
                Log.d(TAG, "checkIfUserIsSusbcribed: User is still subscribed");
                SharedPreferences.Editor editor = sharedPrefAppGeneral.edit();
                editor.putString(getString(R.string.cache_Sub_isSubscribe), "1").apply();


            } else {
                //Not subscribed
                Log.d(TAG, "checkIfUserIsSusbcribed: Not subscribed");
                SharedPreferences.Editor editor1 = sharedPrefAppGeneral.edit();
                editor1.putString(getString(R.string.cache_Sub_isSubscribe), "0").apply();

                SharedPreferences.Editor editor = sharedPrefAutoBackup.edit();
                editor.putBoolean(getString(R.string.settings_sync), false).apply();

            }
        }
    }

    boolean isSusbcribed() {
        boolean purchaseResult = bp.loadOwnedPurchasesFromGoogle();
        boolean sub = false;
        if (purchaseResult) {
            TransactionDetails subscriptionTransactionDetails = bp.getSubscriptionTransactionDetails(SUBSCRIPTION_ID1);
            if (subscriptionTransactionDetails != null) {
                //User is still subscribed
                Log.d(TAG, "checkIfUserIsSusbcribed: User is still subscribed");
                sub = true;
                CacheUtils.writeFile(getString(R.string.cache_Sub_isSubscribe), "1");

            } else {
                //Not subscribed
                Log.d(TAG, "checkIfUserIsSusbcribed: Not subscribed");
                sub = false;
                CacheUtils.writeFile(getString(R.string.cache_Sub_isSubscribe), "0");


            }
        }
        return sub;
    }


    private Task<String> checkSubscriptionOnServer(String text) {
        Log.d(TAG, "checkSubscriptionOnServer: checkSubscriptionOnServer()");
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("packageName", "devesh.ephrine.backup.sms");
        data.put("productId", productId);
        data.put("purchaseToken", purchaseToken);

        return mFunctions
                .getHttpsCallable("verifyPurchaseGPlay")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        String result = (String) task.getResult().getData();
                        Log.d(TAG, "then: " + result);
                        return result;
                    }
                });
    }

}
