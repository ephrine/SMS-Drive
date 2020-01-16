package devesh.ephrine.backup.sms;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ThreadSmsActivity extends AppCompatActivity {
    //  final String DBRoot = "SMSDrive/";
    ArrayList<HashMap<String, String>> SmsThreadHashMap = new ArrayList<>();
    String id;
    String TAG = "BackUp|SMS: ";
    FirebaseAuth mAuth;
    String UserUID;
    ArrayList<String> mContacts;
    String subFolder = "/userdata";
    String file = "ContactFiles";
    FirebaseDatabase database;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        //      SmsThreadHashMap = (HashMap<String, DataSnapshot>)intent.getSerializableExtra("smsthread");
        //  SmsThreadHashMap = Parcels.unwrap(getIntent().getParcelableExtra("mylist"))(HashMap<String, DataSnapshot>)intent.getBundleExtra("smsthread");
        id = intent.getStringExtra("smsthreadid");

        setContentView(R.layout.sms_activity_thread);
        getSupportActionBar().setTitle(id);

        //   DataSnapshot g=SmsThreadHashMap.get(id);
        // Log.d(TAG, "onCreate: Datasnapshot: "+g.toString());
        mAuth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {

            UserUID = user.getPhoneNumber().replace("+", "x");
            Log.d(TAG, "onStart: User UID:" + UserUID);

            DownloadThread();
        } else {
            Intent intent1 = new Intent(this, StartActivity.class);

            //  String message = editText.getText().toString();
            //intent.putExtra(EXTRA_MESSAGE, message);
            startActivity(intent1);
        }
    }
    //HashMap<String,DataSnapshot> SmsThreadHashMap=new HashMap<>();

    void DownloadThread() {

        recyclerView = findViewById(R.id.SmsThreadRecycleView);
        layoutManager = new LinearLayoutManager(this);

        DatabaseReference GetSMS = database.getReference("/users/" + UserUID + "/sms/" + id);
        //final ArrayList<String> SMSList= new ArrayList<>();
// My top posts by number of stars

        //     Query myTopPostsQuery = database.getReference("DBRoot" + "/users/" + UserUID + "/sms/" + id).orderByChild("date");

  /*      myTopPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Log.d(TAG, "Download Value is: " + dataSnapshot.toString());
                long t;
                if (dataSnapshot.getChildrenCount() == 1) {
                    t = dataSnapshot.getChildrenCount();
                } else {
                    t = dataSnapshot.getChildrenCount() - 1;
                }


                long i = 0;
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    // TODO: handle the post

                    i = i + 1;
                    Log.d(TAG, "i:" + i + "\nt:" + t);


                    String MsgTime = postSnapshot.child("date").getValue().toString();
                    Date date = new Date(Long.parseLong(MsgTime));

                    String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
                    //String formattedDate = new SimpleDateFormat("MM/dd/yyyy").format(date);
                    String MsgText = postSnapshot.child("body").getValue().toString();
                    String folder=postSnapshot.child("folder").getValue().toString();
                    HashMap<String, String> SMS = new HashMap<>();

                    SMS.put("msg", MsgText);
                    SMS.put("time", formattedDate);
                    SMS.put("folder", folder);


                    Log.d(TAG, "onDataChange: SMS Thread: " + MsgText + "\n" + MsgTime);


                    if (i == t) {
                        Log.d(TAG, "onDataChange: at last:" + i + " t:" + t);
//
                    }


                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
*/

        GetSMS.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Log.d(TAG, "Download Sort Value is: " + dataSnapshot.toString());
                long t;
                if (dataSnapshot.getChildrenCount() == 1) {
                    t = dataSnapshot.getChildrenCount();
                } else {
                    t = dataSnapshot.getChildrenCount() - 1;
                }


                long i = 0;
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    // TODO: handle the post

                    i = i + 1;
                    Log.d(TAG, "i:" + i + "\nt:" + t);


                    String MsgTime = postSnapshot.child("date").getValue().toString();
                    Date date = new Date(Long.parseLong(MsgTime));

                    String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
                    //String formattedDate = new SimpleDateFormat("MM/dd/yyyy").format(date);
                    String MsgText = postSnapshot.child("body").getValue().toString();
                    String folder = postSnapshot.child("folder").getValue().toString();
                    HashMap<String, String> SMS = new HashMap<>();

                    SMS.put("msg", MsgText);
                    SMS.put("time", formattedDate);
                    SMS.put("folder", folder);

                    SmsThreadHashMap.add(SMS);

                    Log.d(TAG, "onDataChange: SMS Thread: " + MsgText + "\n" + MsgTime);


                    if (i == t) {
                        Log.d(TAG, "onDataChange: at last:" + i + " t:" + t);
//                        recyclerView.removeAllViews();
                        //                      recyclerView.removeAllViewsInLayout();
                        recyclerView.setHasFixedSize(true);

                        // use a linear layout manager
                        recyclerView.setLayoutManager(layoutManager);
                        // specify an adapter (see also next example)

                        ThreadSmsAdapter mAdapter = new ThreadSmsAdapter(ThreadSmsActivity.this, SmsThreadHashMap);

                        recyclerView.setAdapter(mAdapter);

                    }


                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });


    }


}
