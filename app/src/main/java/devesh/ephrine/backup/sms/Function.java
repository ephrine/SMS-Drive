package devesh.ephrine.backup.sms;

import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.crashlytics.android.Crashlytics;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;

/**
 * Created by SHAJIB on 7/10/2017.
 */

public class Function {


    static public final String _ID = "_id";
    static public final String KEY_THREAD_ID = "thread_id";
    static public final String KEY_NAME = "name";
    static public final String KEY_PHONE = "phone";
    static public final String KEY_MSG = "msg";
    static public final String KEY_TYPE = "type";
    static public final String KEY_TIMESTAMP = "timestamp";
    static public final String KEY_TIME = "time";
    static public final String KEY_READ = "read";
    static public final String TAG = "Function:";


    public static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public static String converToTime(String timestamp) {
        long datetime = Long.parseLong(timestamp);
        Date date = new Date(datetime);
        //  DateFormat formatter = new SimpleDateFormat("dd/MM HH:mm");
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
        return formatter.format(date);
    }


    public static HashMap<String, String> mappingInbox(String _id, String thread_id, String name, String phone, String msg, String type, String timestamp, String time, String read) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(_ID, _id);
        map.put(KEY_THREAD_ID, thread_id);
        map.put(KEY_NAME, name);
        map.put(KEY_PHONE, phone);
        map.put(KEY_MSG, msg);
        map.put(KEY_TYPE, type);
        map.put(KEY_TIMESTAMP, timestamp);
        map.put(KEY_TIME, time);
        map.put(KEY_READ, read);

        return map;
    }


    public static ArrayList<HashMap<String, String>> removeDuplicates(ArrayList<HashMap<String, String>> smsList) {
        ArrayList<HashMap<String, String>> gpList = new ArrayList<HashMap<String, String>>();
        double total = smsList.size();
        double progress;
        for (int i = 0; i < smsList.size(); i++) {

            progress = i / total * 100;
            Log.d(TAG, "removeDuplicates: " + progress + "% | " + i + "/" + total);
            boolean available = false;
            for (int j = 0; j < gpList.size(); j++) {
                if (gpList.get(j).get(KEY_THREAD_ID) != null && smsList.get(i).get(KEY_THREAD_ID) != null) {
                    if (Integer.parseInt(gpList.get(j).get(KEY_THREAD_ID)) == Integer.parseInt(smsList.get(i).get(KEY_THREAD_ID))) {
                        available = true;
                        break;
                    }
                }

            }

            if (!available) {
                gpList.add(mappingInbox(smsList.get(i).get(_ID), smsList.get(i).get(KEY_THREAD_ID),
                        smsList.get(i).get(KEY_NAME), smsList.get(i).get(KEY_PHONE),
                        smsList.get(i).get(KEY_MSG), smsList.get(i).get(KEY_TYPE),
                        smsList.get(i).get(KEY_TIMESTAMP), smsList.get(i).get(KEY_TIME)
                        , smsList.get(i).get(KEY_READ)
                ));
            }

        }
        return gpList;
    }

    public static boolean sendSMS(String toPhoneNumber, String smsMessage) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            //--
            // Set the service center address if needed, otherwise null.
            String scAddress = null;
// Set pending intents to broadcast
// when message sent and when delivered, or set to null.
            PendingIntent sentIntent = null, deliveryIntent = null;
            //-
            smsManager.sendTextMessage(toPhoneNumber, scAddress, smsMessage, sentIntent, deliveryIntent);
            Log.d(TAG, "sendSMS: SUCCESS Send !!");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "sendSMS: ERROR !!\n" + e);
            Crashlytics.logException(e);

            return false;
        }
    }


    public static String getContactbyPhoneNumber(Context c, String phoneNumber) {

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        String[] projection = {ContactsContract.PhoneLookup.DISPLAY_NAME};
        Cursor cursor = c.getContentResolver().query(uri, projection, null, null, null);

        if (cursor == null) {
            return phoneNumber;
        } else {
            String name = phoneNumber;
            try {

                if (cursor.moveToFirst()) {
                    name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                }

            } finally {
                cursor.close();
            }

            return name;
        }
    }


    public static void createCachedFile(Context context, String key, ArrayList<HashMap<String, String>> dataList) throws IOException {
        FileOutputStream fos = context.openFileOutput(key, Context.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(dataList);
        oos.close();
        fos.close();
    }
    public static void createCachedNotificationFile(Context context, String key, LinkedHashSet<HashMap<String, String>> dataList) throws IOException {
        FileOutputStream fos = context.openFileOutput(key, Context.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(dataList);
        oos.close();
        fos.close();
    }

    public static Object readCachedFile(Context context, String key) throws IOException, ClassNotFoundException {
        FileInputStream fis = context.openFileInput(key);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object object = ois.readObject();
        return object;
    }


    //------

    public static void getDefaultLocal() {
        Locale defaultLocale = Locale.getDefault();
        // displayCurrencyInfoForLocale(defaultLocale);

        Locale swedishLocale = new Locale("sv", "SE");
        // displayCurrencyInfoForLocale(swedishLocale);
        Log.d(TAG, "getDefaultLocal: ");
        Log.d(TAG, "Locale: " + defaultLocale.getDisplayName());
        Currency currency = Currency.getInstance(defaultLocale);
        Log.d(TAG, "Currency Code: " + currency.getCurrencyCode());
        Log.d(TAG, "Symbol: " + currency.getSymbol());
        Log.d(TAG, "Default Fraction Digits: " + currency.getDefaultFractionDigits());

    }

}
