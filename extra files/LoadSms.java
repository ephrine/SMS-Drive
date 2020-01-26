package devesh.ephrine.backup.sms;

import android.content.Context;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.lifeofcoding.cacheutlislibrary.CacheUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class LoadSms extends AsyncTask<String, Void, String> {
    final String TAG = "LoadSms | ";
    ArrayList<HashMap<String, String>> smsList = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> tmpList = new ArrayList<HashMap<String, String>>();
    Context mContext;

    public LoadSms(Context context) {
        CacheUtils.configureCache(context);

        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        smsList.clear();
    }

    protected String doInBackground(String... args) {
        String xml = "";

        try {
            Uri uriInbox = Uri.parse("content://sms/inbox");

            Cursor inbox = mContext.getContentResolver().query(uriInbox, null, "address IS NOT NULL) GROUP BY (thread_id", null, null); // 2nd null = "address IS NOT NULL) GROUP BY (address"
            Uri uriSent = Uri.parse("content://sms/sent");
            Cursor sent = mContext.getContentResolver().query(uriSent, null, "address IS NOT NULL) GROUP BY (thread_id", null, null); // 2nd null = "address IS NOT NULL) GROUP BY (address"
            Cursor c = new MergeCursor(new Cursor[]{inbox, sent}); // Attaching inbox and sent sms


            if (c.moveToFirst()) {
                for (int i = 0; i < c.getCount(); i++) {
                    String name = null;
                    String phone = "";
                    String _id = c.getString(c.getColumnIndexOrThrow("_id"));
                    String thread_id = c.getString(c.getColumnIndexOrThrow("thread_id"));
                    String msg = c.getString(c.getColumnIndexOrThrow("body"));
                    String type = c.getString(c.getColumnIndexOrThrow("type"));
                    String timestamp = c.getString(c.getColumnIndexOrThrow("date"));
                    phone = c.getString(c.getColumnIndexOrThrow("address"));

                    name = CacheUtils.readFile(thread_id);
                    if (name == null) {
                        name = Function.getContactbyPhoneNumber(mContext.getApplicationContext(), c.getString(c.getColumnIndexOrThrow("address")));
                        CacheUtils.writeFile(thread_id, name);
                    }


                    smsList.add(Function.mappingInbox(_id, thread_id, name, phone, msg, type, timestamp, Function.converToTime(timestamp)));
                    c.moveToNext();

                    Log.d(TAG, "-------\ndoInBackground: \n" + name +
                            "\n" + phone + "\n"
                            + _id + "\n"
                            + thread_id + "\n"
                            + msg + "\n"
                            + type + "\n"
                            + timestamp + "\n"
                            + phone);

                }

            }
            c.close();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Collections.sort(smsList, new MapComparator(Function.KEY_TIMESTAMP, "dsc")); // Arranging sms by timestamp decending
        ArrayList<HashMap<String, String>> purified = Function.removeDuplicates(smsList); // Removing duplicates from inbox & sent
        smsList.clear();
        smsList.addAll(purified);

        // Updating cache data
        try {
            Function.createCachedFile(mContext, "smsapp", smsList);
            Log.d(TAG, "doInBackground: createCachedFile CREATED");
        } catch (Exception e) {
        }
        // Updating cache data

        return xml;
    }

    @Override
    protected void onPostExecute(String xml) {

        if (!tmpList.equals(smsList)) {
                /*
        adapter = new InboxAdapter(MainActivity.this, smsList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
public void onItemClick(AdapterView<?> parent, View view,
final int position, long id) {
        Intent intent = new Intent(MainActivity.this, Chat.class);
        intent.putExtra("name", smsList.get(+position).get(Function.KEY_NAME));
        intent.putExtra("address", tmpList.get(+position).get(Function.KEY_PHONE));
        intent.putExtra("thread_id", smsList.get(+position).get(Function.KEY_THREAD_ID));
        startActivity(intent);
        }
        });
        */


        }


    }
}

