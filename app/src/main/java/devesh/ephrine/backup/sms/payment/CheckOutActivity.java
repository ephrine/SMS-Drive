package devesh.ephrine.backup.sms.payment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.PaymentIntentResult;
import com.stripe.android.Stripe;
import com.stripe.android.model.ConfirmPaymentIntentParams;
import com.stripe.android.model.PaymentIntent;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.view.CardInputWidget;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import devesh.ephrine.backup.sms.BuildConfig;
import devesh.ephrine.backup.sms.R;
import io.fabric.sdk.android.Fabric;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CheckOutActivity extends AppCompatActivity {

    private static final String BACKEND_URL = BuildConfig.Heroku_Server_URL;
    public String CardHolderNameSTR, BillingAddressSTR, EmailIdSTR, MoneyAmount, Currency, PostalCode;
    public String UserUID, PurchaseDate, ExpiryDate, PurchaseId;
    String TAG = "CheckOutActivity ";
    CardInputWidget cardInputWidget;
    EditText CardHolderNameET,
            BillingAddressET,
            EmailIdET;
    OkHttpClient httpClient;
    String PaymentDesc;
    FirebaseDatabase database;
    String amount;
    String created;
    private String paymentIntentClientSecret;
    private Stripe stripe;
    private FirebaseFunctions mFunctions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_out);
        Fabric.with(this, new Crashlytics());

        mFunctions = FirebaseFunctions.getInstance();

        httpClient = new OkHttpClient.Builder()
                .connectTimeout(50, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        PurchaseDate = "";
        ExpiryDate = "";
        PurchaseId = "";

        database = FirebaseDatabase.getInstance();

        cardInputWidget = findViewById(R.id.cardInputWidget);

        CardHolderNameET = findViewById(R.id.CardHolderNameET);
        BillingAddressET = findViewById(R.id.BillingAddressET);
        EmailIdET = findViewById(R.id.EmailIdET);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            UserUID = user.getPhoneNumber().replace("+", "x");
        }
        //startCheckout();

    }

    public void pay(View v) {

        CardHolderNameSTR = CardHolderNameET.getText().toString();
        BillingAddressSTR = BillingAddressET.getText().toString();
        EmailIdSTR = EmailIdET.getText().toString();
        Log.d(TAG, "pay: CardHolderNameSTR:" + CardHolderNameSTR +
                "\nBillingAddressSTR" + BillingAddressSTR +
                "\nEmailIdSTR:" + EmailIdSTR);

        if (CardHolderNameET.getText().toString().equals("") || CardHolderNameET.getText().toString().equals(null)) {
            CardHolderNameET.setError("Enter Card Holder's Name");
            Toast.makeText(this, "Please Enter your Card Holder's Name", Toast.LENGTH_SHORT).show();
        } else {
            if (BillingAddressET.getText().toString().equals("") || BillingAddressET.getText().toString().equals(null)) {
                BillingAddressET.setError("Enter Billing Address");
                Toast.makeText(this, "Please Enter your Billing Address", Toast.LENGTH_SHORT).show();
            } else {
                if (EmailIdET.getText().toString().equals("") || EmailIdET.getText().toString().equals(null)) {
                    EmailIdET.setError("Enter Email ID");
                    Toast.makeText(this, "Please Enter your Email ID", Toast.LENGTH_SHORT).show();

                } else {
                    Log.d(TAG, "pay: Form is good! ");
                    startCheckout();


                }
            }
        }


    }

    private void startCheckout() {
        PaymentDesc = "SMS Drive Subscription of Mr. ABC Person";
        MoneyAmount = "100";
        Currency = "inr";


        //   CardHolderNameSTR="Mr Demo";
        //   BillingAddressSTR="Address0000";
        // Create a PaymentIntent by calling the sample server's /create-payment-intent endpoint.
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        String json = "{"
                + "\"currency\":\"inr\","
                + "\"description\":\"" + PaymentDesc + "\","
                + "\"name\":\"" + CardHolderNameSTR + "\","
                + "\"address\":\"" + BillingAddressSTR + "\","
                + "\"amount\":\"" + MoneyAmount + "\","
                + "\"currency\":\"" + Currency + "\","
                + "\"items\":["
                + "{\"id\":\"sms_drive_subscription\"}"
                + "]"
                + "}";
        Log.d(TAG, "startCheckout: JSON Prep:\n\n" + json + "\n\n");
        RequestBody body = RequestBody.create(json, mediaType);
        Request request = new Request.Builder()
                .url(BACKEND_URL + "create-payment-intent")
                .post(body)
                .build();


        httpClient.newCall(request)

                .enqueue(new PayCallback(this));

        // Hook up the pay button to the card widget and stripe instance
        //    Button payButton = findViewById(R.id.payButton);


    }

    private void displayAlert(@NonNull String title,
                              @Nullable String message,
                              boolean restartDemo) {
        Log.d("STRIPE ", "displayAlert: " + title + "\n" + message);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message);
        if (restartDemo) {
            /*builder.setPositiveButton("Restart demo",
                    (DialogInterface dialog, int index) -> {
                        CardInputWidget cardInputWidget = findViewById(R.id.cardInputWidget);
                        cardInputWidget.clear();
                        startCheckout();
                    });
            */
        } else {
            builder.setPositiveButton("Ok", null);
        }
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle the result of stripe.confirmPayment
        stripe.onPaymentResult(requestCode, data, new PaymentResultCallback(this));
    }

    private void onPaymentSuccess(@NonNull final Response response) throws IOException {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> responseMap = gson.fromJson(
                Objects.requireNonNull(response.body()).string(),
                type
        );

        // The response from the server includes the Stripe publishable key and
        // PaymentIntent details.
        // For added security, our sample app gets the publishable key from the server
        String stripePublishableKey = responseMap.get("publishableKey");
        paymentIntentClientSecret = responseMap.get("clientSecret");

        // Configure the SDK with your Stripe publishable key so that it can make requests to the Stripe API
        stripe = new Stripe(
                getApplicationContext(),
                Objects.requireNonNull(stripePublishableKey)
        );

        initPayment();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public void initPayment() {
        PaymentMethodCreateParams params = cardInputWidget.getPaymentMethodCreateParams();
        if (params != null) {
            ConfirmPaymentIntentParams confirmParams = ConfirmPaymentIntentParams
                    .createWithPaymentMethodCreateParams(params, paymentIntentClientSecret);
            stripe.confirmPayment(this, confirmParams);
        } else {
            Toast.makeText(this, "Please Enter Card Number for Payment", Toast.LENGTH_LONG).show();
        }
    }

    private void saveResult(String g) {
        try {
            // get JSONObject from JSON file
            JSONObject obj = new JSONObject(g);
            // fetch JSONObject named employee
            //  JSONObject employee = obj.getJSONObject("employee");
            // get employee name and salary
            //    name = employee.getString("name");
            //    salary = employee.getString("salary");
            // set employee name and salary in TextView's
            //   employeeName.setText("Name: "+name);
            //  employeeSalary.setText("Salary: "+salary);

            PurchaseDate = obj.getString("created");
            PurchaseId = obj.getString("id");
            //  Log.d(TAG, "saveResult: \nPurhcase Date:"+PurchaseDate+"\nPurchase ID:"+PurchaseId);
            SavePurchase();

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "saveResult: ERROR: \n" + e.toString());
        }

    }

    private Task<String> SavePurchase() {

        ExpiryDate = "x";
        java.util.Date time = new java.util.Date((long) Long.parseLong(PurchaseDate) * 1000);

        // Calendar cal = Calendar.getInstance();
        // cal.setTime(time);
        // add 12 months
        //  cal.add(Calendar.YEAR, 1);
        //  ExpiryDate=String.valueOf(cal.getTime());

        // java.util.Date exptime=new java.util.Date((long)Long.parseLong(ExpiryDate)*1000);
        Long epochTimeExp = Long.parseLong(PurchaseDate) + 31556926;// Add 1 Year='31556926' seconds
        ExpiryDate = String.valueOf(epochTimeExp);
        java.util.Date Exptime = new java.util.Date((long) Long.parseLong(ExpiryDate) * 1000);

        Map<String, String> data = new HashMap<>();
        data.put("userid", UserUID);
        data.put("date", PurchaseDate);
        data.put("purchaseid", PurchaseId);
        data.put("expirydate", ExpiryDate);
        data.put("fdate", String.valueOf(time));
        data.put("fexpdate", String.valueOf(Exptime));

        Log.d(TAG, "SavePurchase: " + data);

        //     DatabaseReference UpdateSubscription = database.getReference("/users/" + UserUID + "/payments/smsdrive");
        //   UpdateSubscription.setValue(data);

        return mFunctions
                .getHttpsCallable("smsDrivePurchaseSave")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        String result = (String) task.getResult().getData();
                        Log.d(TAG, "FUNCTIONS then: resut:" + result);
                        return result;
                    }
                });


    }

    private static final class PayCallback implements Callback {
        @NonNull
        private final WeakReference<CheckOutActivity> activityRef;

        PayCallback(@NonNull CheckOutActivity activity) {
            activityRef = new WeakReference<>(activity);
        }

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            final CheckOutActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }

            Log.d("PAYMENT ", "onFailure: Error: " + e.toString());


        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull final Response response)
                throws IOException {
            final CheckOutActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }

            if (!response.isSuccessful()) {
                Log.d("PAYMENT ", "onResponse: Error: " + response.toString());

            } else {
                activity.onPaymentSuccess(response);
            }
        }
    }

    private static final class PaymentResultCallback
            implements ApiResultCallback<PaymentIntentResult> {
        @NonNull
        private final WeakReference<CheckOutActivity> activityRef;

        PaymentResultCallback(@NonNull CheckOutActivity activity) {
            activityRef = new WeakReference<>(activity);
        }

        @Override
        public void onSuccess(@NonNull PaymentIntentResult result) {
            final CheckOutActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }

            PaymentIntent paymentIntent = result.getIntent();
            PaymentIntent.Status status = paymentIntent.getStatus();
            if (status == PaymentIntent.Status.Succeeded) {
                // Payment completed successfully
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                activity.displayAlert(
                        "Payment completed",
                        gson.toJson(paymentIntent),
                        true
                );

                Log.d("STRIPE ", "onSuccess:\n\n " + gson.toJson(paymentIntent));
                activity.saveResult(gson.toJson(paymentIntent));

            } else if (status == PaymentIntent.Status.RequiresPaymentMethod) {
                // Payment failed – allow retrying using a different payment method
                activity.displayAlert(
                        "Payment failed",
                        Objects.requireNonNull(paymentIntent.getLastPaymentError()).getMessage(),
                        false
                );
            }
        }

        @Override
        public void onError(@NonNull Exception e) {
            final CheckOutActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }

            // Payment request failed – allow retrying using the same payment method
            activity.displayAlert("Error", e.toString(), false);
        }
    }


}
