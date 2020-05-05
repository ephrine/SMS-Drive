package devesh.ephrine.backup.sms;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.functions.FirebaseFunctions;
import com.lifeofcoding.cacheutlislibrary.CacheUtils;

import java.util.Date;

import io.fabric.sdk.android.Fabric;

public class PaymentActivity extends AppCompatActivity {
    String TAG = "PaymentActivity";
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
        setContentView(R.layout.activity_samsung_paymeny);
        Fabric.with(this, new Crashlytics());
        CacheUtils.configureCache(this);

        //   bp = new BillingProcessor(this, LICENSE_KEY, this);
        // bp.initialize();
        priceTextView = findViewById(R.id.priceTextView4546);
        subscriptionStatusText = findViewById(R.id.subscriptionStatusText76);
        subscribeNowButton = findViewById(R.id.subscribeNowButton);

        textView8OrderID = findViewById(R.id.textView8OrderID);
        textView9PurchaseDate = findViewById(R.id.textView9PurchaseDate);
        textView11AutoRenew = findViewById(R.id.textView11AutoRenew);
        textView12Price = findViewById(R.id.textView12Price);
        OrderDetailsCardView = findViewById(R.id.OrderDetailsCardView);
        sharedPrefAppGeneral = PreferenceManager.getDefaultSharedPreferences(this /* Activity context */);

        subscriptionStatusText.setText("Subscribed(Free Welcome Offer)");
        subscriptionStatusText.setTextColor(getResources().getColor(R.color.material_green));
        //  subscribeNowButton.setVisibility(View.GONE);
        //OrderDetailsCardView.setVisibility(View.VISIBLE);

       /* try {
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
        */
    }
}
