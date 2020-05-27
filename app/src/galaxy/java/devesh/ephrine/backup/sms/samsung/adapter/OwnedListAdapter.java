package devesh.ephrine.backup.sms.samsung.adapter;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.lifeofcoding.cacheutlislibrary.CacheUtils;
import com.samsung.android.sdk.iap.lib.helper.IapHelper;
import com.samsung.android.sdk.iap.lib.listener.OnConsumePurchasedItemsListener;
import com.samsung.android.sdk.iap.lib.listener.OnGetOwnedListListener;
import com.samsung.android.sdk.iap.lib.vo.ConsumeVo;
import com.samsung.android.sdk.iap.lib.vo.ErrorVo;
import com.samsung.android.sdk.iap.lib.vo.OwnedProductVo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import devesh.ephrine.backup.sms.PaymentActivity;
import devesh.ephrine.backup.sms.R;
import devesh.ephrine.backup.sms.samsung.constants.ItemDefine;

/**
 * Created by sangbum7.kim on 2017-08-17.
 */

public class OwnedListAdapter extends ItemDefine implements OnGetOwnedListListener, OnConsumePurchasedItemsListener {
    private final String TAG = OwnedListAdapter.class.getSimpleName();
    SharedPreferences sharedPrefAppGeneral;
    private PaymentActivity mPaymentActivity = null;
    private IapHelper mIapHelper = null;
    private String mConsumablePurchaseIDs = "";
    private Map<String, String> consumeItemMap = new HashMap<String, String>();

    public OwnedListAdapter
            (
                    PaymentActivity _activity,
                    IapHelper _iapHelper
            ) {
        mPaymentActivity = _activity;
        mIapHelper = _iapHelper;
        sharedPrefAppGeneral = PreferenceManager.getDefaultSharedPreferences(mPaymentActivity /* Activity context */);

    }

    @Override
    public void onGetOwnedProducts(ErrorVo _errorVo, ArrayList<OwnedProductVo> _ownedList) {
        Log.v(TAG, "onGetOwnedProducts !");
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
                            Log.d(TAG, "onGetOwnedProducts: ITEM_ID_SUBSCRIPTION");
                            String purchasedate = product.getPurchaseDate();
                            String OrderID = product.getPaymentId();
                            String price = product.getItemPriceString();

                            mPaymentActivity.paymentSuccess("0", purchasedate, price);
                        }
                    }

                    if (isSub) {
                        mPaymentActivity.isSubscribed = true;

                        Log.d(TAG, "onGetOwnedProducts: USER HAS SUBSCRIBED TO SMS DRIVE");
                        SharedPreferences.Editor editor = sharedPrefAppGeneral.edit();
                        editor.putString(mPaymentActivity.getString(R.string.cache_Sub_isSubscribe), "1").apply();
                        CacheUtils.writeFile(mPaymentActivity.getString(R.string.cache_Sub_isSubscribe), "1");
                    } else {
                        Log.d(TAG, "onGetOwnedProducts: USER HAS NOT SUBSCRIBED TO SMS DRIVE");
                        SharedPreferences.Editor editor = sharedPrefAppGeneral.edit();
                        editor.putString(mPaymentActivity.getString(R.string.cache_Sub_isSubscribe), "0").apply();
                        CacheUtils.writeFile(mPaymentActivity.getString(R.string.cache_Sub_isSubscribe), "0");
                        mPaymentActivity.isSubscribed = false;
                    }

                } else {
                    Log.e(TAG, "onGetOwnedProducts: NOT SUBSCRIBED !! GALAXY");
                }

                /* ----------------------------------------------------- */
                //      mPaymentActivity.setGunLevel(gunLevel);
                //     mPaymentActivity.setInfiniteBullet(infiniteBullet);

                if (mConsumablePurchaseIDs.length() > 0) {
                    mIapHelper.consumePurchasedItems(mConsumablePurchaseIDs, OwnedListAdapter.this);
                    mConsumablePurchaseIDs = "";
                }
            } else {
                Log.e(TAG, "onGetOwnedProducts ErrorCode [" + _errorVo.getErrorCode() + "]");
                if (_errorVo.getErrorString() != null)
                    Log.e(TAG, "onGetOwnedProducts ErrorString[" + _errorVo.getErrorString() + "]");
            }
        } else {
            Log.d(TAG, "onGetOwnedProducts: NOT SUBSCRIBED !! #43445");
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
                                        //         mPaymentActivity.plusBullet();
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
