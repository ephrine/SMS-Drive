package devesh.ephrine.backup.sms;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.lifeofcoding.cacheutlislibrary.CacheUtils;
import com.samsung.android.sdk.iap.lib.helper.HelperDefine;
import com.samsung.android.sdk.iap.lib.helper.IapHelper;
import com.samsung.android.sdk.iap.lib.listener.OnConsumePurchasedItemsListener;
import com.samsung.android.sdk.iap.lib.listener.OnGetOwnedListListener;
import com.samsung.android.sdk.iap.lib.listener.OnPaymentListener;
import com.samsung.android.sdk.iap.lib.vo.ConsumeVo;
import com.samsung.android.sdk.iap.lib.vo.ErrorVo;
import com.samsung.android.sdk.iap.lib.vo.OwnedProductVo;
import com.samsung.android.sdk.iap.lib.vo.PurchaseVo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import devesh.ephrine.backup.sms.samsung.constants.ItemDefine;

public class CheckSubscriptionService extends Service {
    private static HelperDefine.OperationMode IAP_MODE = HelperDefine.OperationMode.OPERATION_MODE_PRODUCTION;

    //private static HelperDefine.OperationMode IAP_MODE = HelperDefine.OperationMode.OPERATION_MODE_TEST;
    SharedPreferences sharedPrefAutoBackup;
    SharedPreferences sharedPrefAppGeneral;
    String TAG = "CheckSubService";
    private IapHelper mIapHelper = null;
    private SubPaymentAdapter mPaymentAdapter = null;
    private SubOwnedListAdapter mOwnedListAdapter = null;


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
        //      SharedPreferences.Editor editor = sharedPrefAppGeneral.edit();
        //     editor.putString(getString(R.string.cache_Sub_isSubscribe), "1").apply();
        //    CacheUtils.writeFile(getString(R.string.cache_Sub_isSubscribe), "1");

        //   sharedPrefAppGeneral = PreferenceManager.getDefaultSharedPreferences(this /* Activity context */);

        mIapHelper = IapHelper.getInstance(this.getApplicationContext());
        mIapHelper.setOperationMode(IAP_MODE);

        mOwnedListAdapter = new SubOwnedListAdapter(this, mIapHelper);
        mPaymentAdapter = new SubPaymentAdapter(this, mIapHelper);
        mPaymentAdapter.setPassThroughParam("");//"TEST_PASS_THROUGH"

        getOwnedList();
        // getSubscriptionDetails();

    }

    protected void getOwnedList() {
        Log.v(TAG, "getOwnedList");
        if (mOwnedListAdapter != null) {
            mIapHelper.getOwnedList(IapHelper.PRODUCT_TYPE_ALL,
                    mOwnedListAdapter);
        } else {
            Log.v(TAG, "getOwnedList: mOwnedListAdapter NULL");

        }
    }

    class SubOwnedListAdapter extends ItemDefine implements OnGetOwnedListListener, OnConsumePurchasedItemsListener {
        private final String TAG = devesh.ephrine.backup.sms.samsung.adapter.OwnedListAdapter.class.getSimpleName();

        private CheckSubscriptionService mCheckSubscriptionService = null;
        private IapHelper mIapHelper = null;

        private String mConsumablePurchaseIDs = "";
        private Map<String, String> consumeItemMap = new HashMap<String, String>();

        public SubOwnedListAdapter
                (
                        CheckSubscriptionService _activity,
                        IapHelper _iapHelper
                ) {
            mCheckSubscriptionService = _activity;
            mIapHelper = _iapHelper;
        }

        @Override
        public void onGetOwnedProducts(ErrorVo _errorVo, ArrayList<OwnedProductVo> _ownedList) {
            Log.v(TAG, "onGetOwnedProducts");
            if (_errorVo != null) {
                if (_errorVo.getErrorCode() == IapHelper.IAP_ERROR_NONE) {
                    int gunLevel = 0;
                    boolean isSub = false;
                    if (_ownedList != null) {
                        for (int i = 0; i < _ownedList.size(); i++) {
                            OwnedProductVo product = _ownedList.get(i);

                            Log.d(TAG, "onGetOwnedProducts: \n" + product.dump());
                            if (product.getIsConsumable()) {
                                try {
                                    if (consumeItemMap.get(product.getPurchaseId()) == null) {
                                        consumeItemMap.put(product.getPurchaseId(), product.getItemId());
                                        if (mConsumablePurchaseIDs.length() == 0)
                                            mConsumablePurchaseIDs = product.getPurchaseId();
                                        else
                                            mConsumablePurchaseIDs = mConsumablePurchaseIDs + "," + product.getPurchaseId();
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "exception" + e);
                                }
                            }

                            if (product.getItemId().equals(ITEM_ID_NONCONSUMABLE)) {
                                gunLevel = 1;
                            } else if (product.getItemId().equals(ITEM_ID_CONSUMABLE)) {
                                Log.d(TAG, "onGetOwnedProducts: consumePurchasedItems" + product.getPurchaseId());
                            }

                            if (product.getItemId().equals(ITEM_ID_SUBSCRIPTION)) {
                                isSub = true;
                                Log.d(TAG, "onGetOwnedProducts: USER HAS SUBSCRIBED TO SMS DRIVE");

                            }

                        }

                        if (isSub) {

                            Log.d(TAG, "onGetOwnedProducts: USER HAS SUBSCRIBED TO SMS DRIVE");
                            SharedPreferences.Editor editor = sharedPrefAppGeneral.edit();
                            editor.putString(getString(R.string.cache_Sub_isSubscribe), "1").apply();
                            CacheUtils.writeFile(getString(R.string.cache_Sub_isSubscribe), "1");
                        } else {
                            Log.d(TAG, "onGetOwnedProducts: USER HAS NOT SUBSCRIBED TO SMS DRIVE");
                            SharedPreferences.Editor editor = sharedPrefAppGeneral.edit();
                            editor.putString(getString(R.string.cache_Sub_isSubscribe), "0").apply();
                            CacheUtils.writeFile(getString(R.string.cache_Sub_isSubscribe), "0");

                        }
                    } else {
                        Log.e(TAG, "onGetOwnedProducts: NOT SUBSCRIBED !! GALAXY");
                    }

                    /* ----------------------------------------------------- */
                    //      mCheckSubscriptionService.setGunLevel(gunLevel);
                    //     mCheckSubscriptionService.setInfiniteBullet(infiniteBullet);

                    if (mConsumablePurchaseIDs.length() > 0) {
                        mIapHelper.consumePurchasedItems(mConsumablePurchaseIDs, this);
                        mConsumablePurchaseIDs = "";
                    }
                } else {
                    Log.e(TAG, "onGetOwnedProducts ErrorCode [" + _errorVo.getErrorCode() + "]");
                    if (_errorVo.getErrorString() != null)
                        Log.e(TAG, "onGetOwnedProducts ErrorString[" + _errorVo.getErrorString() + "]");
                }
            } else {
                Log.e(TAG, "onGetOwnedProducts: ERROR: " + _errorVo.dump());
            }
        }

        @Override
        public void onConsumePurchasedItems(ErrorVo _errorVo, ArrayList<ConsumeVo> _consumeList) {
            if (_errorVo != null) {
                if (_errorVo.getErrorCode() == IapHelper.IAP_ERROR_NONE) {
                    try {
                        if (_consumeList != null) {
                            for (ConsumeVo consumeVo : _consumeList) {
                                if (consumeVo.getStatusCode() == CONSUME_STATUS_SUCCESS) {
                                    String itemId = consumeItemMap.get(consumeVo.getPurchaseId());
                                    if (itemId != null) {
                                        if (itemId.equals(ITEM_ID_CONSUMABLE))
                                            //         mCheckSubscriptionService.plusBullet();
                                            consumeItemMap.remove(consumeVo.getPurchaseId());
                                    }
                                } else
                                    Log.e(TAG, "onConsumePurchasedItems: statuscode " + consumeVo.getStatusCode());
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "onConsumePurchasedItems: Exception :" + e);
                    }
                } else {
                    Log.e(TAG, "onConsumePurchasedItems > ErrorCode [" + _errorVo.getErrorCode() + "]");
                    if (_errorVo.getErrorString() != null)
                        Log.e(TAG, "onConsumePurchasedItems > ErrorString[" + _errorVo.getErrorString() + "]");
                }
            }
            consumeItemMap.clear();
        }
    }

    class SubPaymentAdapter extends ItemDefine implements OnPaymentListener, OnConsumePurchasedItemsListener {
        /**
         * Callback method to be invoked
         * when Initialization of IAPService has been finished in successfully.
         */

        private final String TAG = "SubPaymentAdapter";

        private CheckSubscriptionService mMainActivity = null;
        private IapHelper mIapHelper = null;
        private String mPassThroughParam = "";//TEMP_PASS_THROUGH
        private String mConsumedItemId = "";

        public SubPaymentAdapter
                (
                        CheckSubscriptionService _activity,
                        IapHelper _iapHelper
                ) {
            mMainActivity = _activity;
            mIapHelper = _iapHelper;
        }

        @Override
        public void onPayment(ErrorVo _errorVo, PurchaseVo _purchaseVo) {
            if (_errorVo != null) {
                if (_errorVo.getErrorCode() == IapHelper.IAP_ERROR_NONE) {
                    if (_purchaseVo != null) {
                        // ====================================================================
                        String message = "";
                        if (mPassThroughParam != null && _purchaseVo.getPassThroughParam() != null) {
                            if (mPassThroughParam.equals(_purchaseVo.getPassThroughParam())) {
                                message = "passThroughParam is matched";

                                Log.d(TAG, "PAYMENT SUCCESS #323 \n" + _purchaseVo.dump());
                                if (_purchaseVo.getIsConsumable()) {
                                    mConsumedItemId = _purchaseVo.getItemId();
                                    mIapHelper.consumePurchasedItems(_purchaseVo.getPurchaseId(), this);
                                }

                                if (_purchaseVo.getItemId().equals(ITEM_ID_NONCONSUMABLE)) {
                                }
                                //  mMainActivity.setGunLevel(2);
                                else if (_purchaseVo.getItemId().equals(ITEM_ID_SUBSCRIPTION)) {
                                    Log.d(TAG, "onPayment: SUBSCRIPTION SUCCESS ITEM_ID_SUBSCRIPTION !!");
                                }
                                //  mMainActivity.setInfiniteBullet(true);
                                else if (_purchaseVo.getItemId().equals(ITEM_ID_CONSUMABLE)) {
                                    Log.d(TAG, "onPayment consumePurchasedItems" + _purchaseVo.getPurchaseId());
                                }
                            } else
                                message = "passThroughParam is mismatched";
                        }
                    } else
                        Log.e(TAG, "onPayment > _purchaseVo: null");
                } else {
                    Log.e(TAG, "onPayment > ErrorCode [" + _errorVo.getErrorCode() + "]");
                    if (_errorVo.getErrorString() != null)
                        Log.e(TAG, "onPayment > ErrorString[" + _errorVo.getErrorString() + "]");
                }
            }

            // ====================================================================
            if (_errorVo != null) {
                Log.e(TAG, _errorVo.getErrorString());
            }
        }

        @Override
        public void onConsumePurchasedItems(ErrorVo _errorVo, ArrayList<ConsumeVo> _consumeList) {
            if (_errorVo != null) {
                if (_errorVo.getErrorCode() == IapHelper.IAP_ERROR_NONE) {
                    try {
                        if (_consumeList != null) {
                            for (ConsumeVo consumeVo : _consumeList) {
                                if (consumeVo.getStatusCode() == CONSUME_STATUS_SUCCESS) {
                                    if (mConsumedItemId.equals(ITEM_ID_CONSUMABLE)) {
                                    }
                                    // mMainActivity.plusBullet();
                                } else {
                                    Log.e(TAG, "onConsumePurchasedItems: statuscode " + consumeVo.getStatusCode());
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "onConsumePurchasedItems: Exception " + e);
                    }
                } else {
                    Log.e(TAG, "onConsumePurchasedItems > ErrorCode [" + _errorVo.getErrorCode() + "]");
                    if (_errorVo.getErrorString() != null)
                        Log.e(TAG, "onConsumePurchasedItems > ErrorString[" + _errorVo.getErrorString() + "]");
                }
            }
            mConsumedItemId = "";
        }

        public String getPassThroughParam() {
            return mPassThroughParam;
        }

        public void setPassThroughParam(String _passThroughParam) {
            mPassThroughParam = _passThroughParam;
        }
    }

}
