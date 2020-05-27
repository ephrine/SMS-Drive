package devesh.ephrine.backup.sms;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.functions.FirebaseFunctions;
import com.lifeofcoding.cacheutlislibrary.CacheUtils;
import com.samsung.android.sdk.iap.lib.helper.HelperDefine;
import com.samsung.android.sdk.iap.lib.helper.IapHelper;
import com.samsung.android.sdk.iap.lib.listener.OnGetProductsDetailsListener;
import com.samsung.android.sdk.iap.lib.vo.ErrorVo;
import com.samsung.android.sdk.iap.lib.vo.ProductVo;

import java.util.ArrayList;
import java.util.Date;

import devesh.ephrine.backup.sms.samsung.adapter.OwnedListAdapter;
import devesh.ephrine.backup.sms.samsung.adapter.PaymentAdapter;
import devesh.ephrine.backup.sms.samsung.constants.ItemDefine;
import io.fabric.sdk.android.Fabric;

public class PaymentActivity extends AppCompatActivity implements OnGetProductsDetailsListener {


    private static HelperDefine.OperationMode IAP_MODE = HelperDefine.OperationMode.OPERATION_MODE_PRODUCTION;
   // private static HelperDefine.OperationMode IAP_MODE = HelperDefine.OperationMode.OPERATION_MODE_TEST;
    public boolean isSubscribed;
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
    private IapHelper mIapHelper = null;
    private PaymentAdapter mPaymentAdapter = null;
    private OwnedListAdapter mOwnedListAdapter = null;
    private ArrayList<ProductVo> mProductList = new ArrayList<ProductVo>();

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

        mIapHelper = IapHelper.getInstance(this.getApplicationContext());
        mIapHelper.setOperationMode(IAP_MODE);

        mOwnedListAdapter = new OwnedListAdapter(this, mIapHelper);
        mPaymentAdapter = new PaymentAdapter(this, mIapHelper);
        mPaymentAdapter.setPassThroughParam("");//"TEST_PASS_THROUGH"

        getOwnedList();
        getSubscriptionDetails();
        //----------
        //  subscriptionStatusText.setText("Subscribed(Free Welcome Offer)");
        // subscriptionStatusText.setTextColor(getResources().getColor(R.color.material_green));
        //  subscribeNowButton.setVisibility(View.GONE);
        //OrderDetailsCardView.setVisibility(View.VISIBLE);

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
                Toast.makeText(this, "Loading Subscription Details", Toast.LENGTH_SHORT).show();

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

    @Override
    protected void onResume() {
        super.onResume();
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

    protected void getOwnedList() {
        Log.v(TAG, "getOwnedList");
        if (mOwnedListAdapter != null) {

            mIapHelper.getOwnedList(IapHelper.PRODUCT_TYPE_ALL,
                    mOwnedListAdapter);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //   setPreferences();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIapHelper.dispose();
    }

    public void SubscribeNow(View v) {
        purchaseProduct(ItemDefine.ITEM_ID_SUBSCRIPTION);
    }

    protected void purchaseProduct(String itemId) {
        if (mPaymentAdapter != null) {
            mIapHelper.startPayment(itemId,
                    mPaymentAdapter.getPassThroughParam(),
                    true,
                    mPaymentAdapter);
        }
    }

    void getSubscriptionDetails() {
        mIapHelper = IapHelper.getInstance(this);
        if (mIapHelper != null) {
            mIapHelper.getProductsDetails(ItemDefine.ITEM_ID_SUBSCRIPTION,
                    this);

            initView();

        } else {
            //   finish();
        }


    }

    public void initView() {
        // 1. set views of Product Details
        // ====================================================================
  /*      mProductListView = (ListView) findViewById(R.id.productList);
        mNoDataTextView = (TextView) findViewById(R.id.noDataText);

        mSelectedProductType = (TextView)findViewById(
                R.id.txt_selected_product_type );
        mSelectedProductType.setText( mProductType );

        mProductAdapter = new ProductsDetailsAdapter(this,
                R.layout.product_row,
                mProductList);

        mProductListView.setAdapter(mProductAdapter);
        mProductListView.setEmptyView(mNoDataTextView);
        mProductListView.setVisibility(View.GONE);
*/
    }

    public void paymentSuccess(String orderID, String purchasedate, String price) {
        String orderid;
        if (orderID != null) {
            if (orderID.equals("0")) {
                orderid = sharedPrefAppGeneral.getString(getString(R.string.cache_Sub_OrderId), "00");

            } else {

                SharedPreferences.Editor editor = sharedPrefAppGeneral.edit();
                editor.putString(getString(R.string.cache_Sub_OrderId), orderID).apply();
                orderid = orderID;
            }

        } else {
            orderid = sharedPrefAppGeneral.getString(getString(R.string.cache_Sub_OrderId), "00");
        }


        subscriptionStatusText.setText("Subscribed");
        subscriptionStatusText.setTextColor(getResources().getColor(R.color.material_green));
        subscribeNowButton.setVisibility(View.GONE);
        OrderDetailsCardView.setVisibility(View.VISIBLE);

        textView8OrderID.setText("Order ID: " + orderid);
        textView9PurchaseDate.setText("Purchase Date: " + purchasedate);
        textView11AutoRenew.setVisibility(View.GONE);
        textView12Price.setText("Price: " + price);

    }

    @Override
    public void onGetProducts(ErrorVo _errorVo, ArrayList<ProductVo> _productList) {
        if (_errorVo != null) {
            if (_errorVo.getErrorCode() == IapHelper.IAP_ERROR_NONE) {
                if (_productList != null) {
                    for (ProductVo item : _productList) {
                        // TODO: Get details of the item
                        Log.d(TAG, "onGetProducts: ITEM: " + item.dump());
                        if (item.getItemId().equals(ItemDefine.ITEM_ID_SUBSCRIPTION)) {
                            Log.d(TAG, "onGetProducts: SMS Drive Subscription");
                            textView12Price.setText("Price: " + item.getItemPriceString());
                            CacheUtils.writeFile(getString(R.string.cache_Sub_Details_Price), item.getItemPriceString());

                        }
                    }
                }
            } else {
                // TODO: Handle the error
            }
        }
    }


}
