package devesh.ephrine.backup.sms.samsung.adapter;


import android.content.Intent;
import android.util.Log;

import com.samsung.android.sdk.iap.lib.helper.IapHelper;
import com.samsung.android.sdk.iap.lib.listener.OnConsumePurchasedItemsListener;
import com.samsung.android.sdk.iap.lib.listener.OnPaymentListener;
import com.samsung.android.sdk.iap.lib.vo.ConsumeVo;
import com.samsung.android.sdk.iap.lib.vo.ErrorVo;
import com.samsung.android.sdk.iap.lib.vo.PurchaseVo;

import java.util.ArrayList;

import devesh.ephrine.backup.sms.CheckSubscriptionService;
import devesh.ephrine.backup.sms.PaymentActivity;
import devesh.ephrine.backup.sms.samsung.constants.ItemDefine;

/**
 * Callback Interface is called
 * when Initialization of IAPService has been finished in successfully.
 */
public class PaymentAdapter extends ItemDefine implements OnPaymentListener, OnConsumePurchasedItemsListener {
    /**
     * Callback method to be invoked
     * when Initialization of IAPService has been finished in successfully.
     */

    private final String TAG = PaymentAdapter.class.getSimpleName();

    private PaymentActivity mPaymentActivity = null;
    private IapHelper mIapHelper = null;
    private String mPassThroughParam = "";//TEMP_PASS_THROUGH
    private String mConsumedItemId = "";

    public PaymentAdapter
            (
                    PaymentActivity _activity,
                    IapHelper _iapHelper
            ) {
        mPaymentActivity = _activity;
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
                                mIapHelper.consumePurchasedItems(_purchaseVo.getPurchaseId(), PaymentAdapter.this);
                            }

                            if (_purchaseVo.getItemId().equals(ITEM_ID_NONCONSUMABLE)) {
                            }
                            //  mPaymentActivity.setGunLevel(2);
                            else if (_purchaseVo.getItemId().equals(ITEM_ID_SUBSCRIPTION)) {
                                Log.d(TAG, "onPayment: SUBSCRIPTION SUCCESS ITEM_ID_SUBSCRIPTION !!");
                                String purchasedate = _purchaseVo.getPurchaseDate();
                                String OrderID = _purchaseVo.getOrderId();//getOrderId();
                                String price = _purchaseVo.getItemPriceString();

                                mPaymentActivity.paymentSuccess(OrderID, purchasedate, price);
                                try {
                                    Intent subscriptionCheck = new Intent(mPaymentActivity, CheckSubscriptionService.class);

                                    mPaymentActivity.startService(subscriptionCheck);


                                } catch (Exception e) {
                                    Log.e(TAG, "onCreate: #5465653 ", e);
                                }
                            }
                            //  mPaymentActivity.setInfiniteBullet(true);
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
                                // mPaymentActivity.plusBullet();
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