package devesh.ephrine.backup.sms.payment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.functions.FirebaseFunctions;
import com.lifeofcoding.cacheutlislibrary.CacheUtils;

import java.util.Date;

import devesh.ephrine.backup.sms.BuildConfig;
import devesh.ephrine.backup.sms.R;
import io.fabric.sdk.android.Fabric;

public class GPlayBillingCheckoutActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {
    BillingProcessor bp;
    String LICENSE_KEY = BuildConfig.GLicenseKey; // YOUR LICENSE KEY FROM GOOGLE PLAY CONSOLE HERE
    String SUBSCRIPTION_ID1 = BuildConfig.Google_Play_Subscription_ID1;//YOUR SUBSCRIPTION ID FROM GOOGLE PLAY CONSOLE HERE
    String TAG = "GPlayBillingCheckoutActivity";
    String purchaseToken;
    String productId;
    String orderId;
    Date purchaseTime;
    Long purchaseTimeTimeMillis;
    String price;
    String SubDescription;
    SharedPreferences sharedPrefAutoBackup;
    TextView priceTextView;
    TextView subscriptionStatusText;
    TextView textView8OrderID;
    TextView textView9PurchaseDate;
    TextView textView11AutoRenew;
    TextView textView12Price;
    Button subscribeNowButton;
    CardView OrderDetailsCardView;
    SharedPreferences sharedPrefAppGeneral;
    private FirebaseFunctions mFunctions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_g_play_billing_checkout);
        Fabric.with(this, new Crashlytics());
        CacheUtils.configureCache(this);

        bp = new BillingProcessor(this, LICENSE_KEY, this);
        bp.initialize();
        priceTextView = findViewById(R.id.priceTextView4546);
        subscriptionStatusText = findViewById(R.id.subscriptionStatusText76);
        subscribeNowButton = findViewById(R.id.subscribeNowButton);

        textView8OrderID = findViewById(R.id.textView8OrderID);
        textView9PurchaseDate = findViewById(R.id.textView9PurchaseDate);
        textView11AutoRenew = findViewById(R.id.textView11AutoRenew);
        textView12Price = findViewById(R.id.textView12Price);
        OrderDetailsCardView = findViewById(R.id.OrderDetailsCardView);
        sharedPrefAppGeneral = PreferenceManager.getDefaultSharedPreferences(this /* Activity context */);

        try {
            if (CacheUtils.readFile(getString(R.string.cache_Sub_Details_Price)) != null) {
                price = CacheUtils.readFile(getString(R.string.cache_Sub_Details_Price));
                priceTextView.setText(" Price: " + price + " per Month");
            }
            String sub = sharedPrefAppGeneral.getString(getString(R.string.cache_Sub_isSubscribe), "0");

            if (sub.equals("1")) {
                subscriptionStatusText.setText("Subscribed");
                subscriptionStatusText.setTextColor(getResources().getColor(R.color.material_green));
                subscribeNowButton.setVisibility(View.GONE);
                OrderDetailsCardView.setVisibility(View.VISIBLE);
            } else {
                subscriptionStatusText.setText("Not Subscribed");
                subscriptionStatusText.setTextColor(getResources().getColor(R.color.material_red));
                subscribeNowButton.setVisibility(View.VISIBLE);
                OrderDetailsCardView.setVisibility(View.GONE);

            }


        } catch (Exception e) {
            Log.e(TAG, "onCreate: ERROR #576", e);
        }
    }

    // IBillingHandler implementation

    @Override
    public void onBillingInitialized() {
        /*
         * Called when BillingProcessor was initialized and it's ready to purchase
         */
        checkIfUserIsSusbcribed();
        loadBillingDetails();

    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        /*
         * Called when requested PRODUCT ID was successfully purchased
         */

        Log.d(TAG, "onProductPurchased: productId:" + productId + "\nTransactionDetails" + details);
        SharedPreferences.Editor editor = sharedPrefAppGeneral.edit();
        editor.putString(getString(R.string.cache_Sub_isSubscribe), "1").apply();

        subscriptionStatusText.setText("Subscribed");
        subscriptionStatusText.setTextColor(getResources().getColor(R.color.material_green));
        subscribeNowButton.setVisibility(View.GONE);
        OrderDetailsCardView.setVisibility(View.VISIBLE);
        loadBillingDetails();

    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        /*
         * Called when some error occurred. See Constants class for more details
         *
         * Note - this includes handling the case where the user canceled the buy dialog:
         * errorCode = Constants.BILLING_RESPONSE_RESULT_USER_CANCELED
         */
    }

    @Override
    public void onPurchaseHistoryRestored() {
        /*
         * Called when purchase history was restored and the list of all owned PRODUCT ID's
         * was loaded from Google Play
         */
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            Log.d(TAG, "onActivityResult: " + data);
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void SubscribeNow(View v) {
        bp.subscribe(this, SUBSCRIPTION_ID1);
    }

    @Override
    public void onDestroy() {
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }


    public void checkIfUserIsSusbcribed() {
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
                SharedPreferences.Editor editor = sharedPrefAppGeneral.edit();
                editor.putString(getString(R.string.cache_Sub_isSubscribe), "0").apply();

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

    public void cancleSubscription(View v) {
        String packageName = getPackageName();
        String subscriptionId = SUBSCRIPTION_ID1;
        String token = purchaseToken;

        // https://www.googleapis.com/androidpublisher/v3/applications/packageName/purchases/subscriptions/subscriptionId/tokens/token:cancel
        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/account/subscriptions"));
        startActivity(browserIntent);

    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            onBillingInitialized();
        } catch (Exception e) {
            Log.e(TAG, "onResume: ERROR #657 " + e);
        }

    }

    void loadBillingDetails() {
        SkuDetails subDetails = bp.getSubscriptionListingDetails(SUBSCRIPTION_ID1);

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
//                checkSubscriptionOnServer("0");
            }
            textView8OrderID.setText("Order ID: " + orderId);
            textView9PurchaseDate.setText("Purchase Date: " + purchaseTime.toString());
            textView11AutoRenew.setText("Auto renew: " + td.purchaseInfo.purchaseData.autoRenewing);
            textView12Price.setText("Price: " + subDetails.priceText);
            // write
            CacheUtils.writeFile(getString(R.string.cache_Sub_OrderId), orderId);
            CacheUtils.writeFile(getString(R.string.cache_Sub_ProductId), productId);
            CacheUtils.writeFile(getString(R.string.cache_Sub_PurchaseTimeMillis), String.valueOf(purchaseTimeTimeMillis));
            CacheUtils.writeFile(getString(R.string.cache_Sub_PurchaseToken), purchaseToken);
            CacheUtils.writeFile(getString(R.string.cache_Sub_PurchaseTime), String.valueOf(purchaseTime));
            SharedPreferences.Editor editor = sharedPrefAppGeneral.edit();
            editor.putString(getString(R.string.cache_Sub_isSubscribe), "1").apply();

            // read
            // String fileContent = CacheUtils.readFile(CACHE_FILE_STRING);

        } catch (Exception e) {
            Log.e(TAG, "onBillingInitialized: ERROR " + e);
            if (!isSusbcribed()) {
                CacheUtils.writeFile(getString(R.string.cache_Sub_OrderId), "0");
                CacheUtils.writeFile(getString(R.string.cache_Sub_ProductId), "0");
                CacheUtils.writeFile(getString(R.string.cache_Sub_PurchaseTimeMillis), "0");
                CacheUtils.writeFile(getString(R.string.cache_Sub_PurchaseToken), "0");
                CacheUtils.writeFile(getString(R.string.cache_Sub_PurchaseTime), "0");

            }

        }

        try {
            if (subDetails.priceText != null) {
                price = subDetails.priceText;
            }
        } catch (Exception e) {
            Log.e(TAG, "loadBillingDetails: ERROR #43546 ", e);
        }

        //  SubDescription = subDetails.description;

        CacheUtils.writeFile(getString(R.string.cache_Sub_Details_Price), price);
        //  CacheUtils.writeFile(getString(R.string.cache_Sub_Details_Description), SubDescription);

        Log.d(TAG, "onBillingInitialized: subDetails\n price:" + price);
        priceTextView.setText(" Price: " + price + " per Month");


    }
}
