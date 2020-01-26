package devesh.ephrine.backup.sms;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.util.ArrayList;
import java.util.HashMap;

public class NewMessageActivity extends AppCompatActivity {

    final String TAG="NewMessageActivity ";
    ArrayList<HashMap<String, String>> contactMap=new ArrayList<>();
    public String sendTo;
    public String message;
    CardView selectedContactCardView;
    EditText editTextPhoneNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);

        selectedContactCardView=findViewById(R.id.selectedContactCardView);
        editTextPhoneNumber=findViewById(R.id.editTextPhoneNumber);

        getContacts();

    }

    void getContacts() {
        try {
            contactMap = (ArrayList<HashMap<String, String>>) Function.readCachedFile(this, getString(R.string.file_contact_list));
            if (contactMap == null) {
                getContactList();
                Log.d(TAG, "getContacts: Getting Contacts List");
            } else {
                Log.d(TAG, "getContacts: Contact List Already present");
                LoadRecycleView();
            }
        } catch (Exception e) {
            Log.d(TAG, "getContacts: ERROR: " + e);
        }

    }

    private void getContactList() {
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Log.i(TAG, "Name: " + name);
                        Log.i(TAG, "Phone Number: " + phoneNo);
                        HashMap<String, String> c = new HashMap<>();

                        c.put("name", name);
                        c.put("phone", phoneNo);
                        contactMap.add(c);

                        try {
                            Function.createCachedFile(this, getString(R.string.file_contact_list), contactMap);
                            LoadRecycleView();
                        } catch (Exception e) {
                            Log.d(TAG, "getContactList: Error #564" + e);
                        }

                    }
                    pCur.close();

                }
            }
        }
        if (cur != null) {
            cur.close();
        }
    }

    RecyclerView ContactRecycleView;
    RecyclerView.LayoutManager layoutManager;
    void LoadRecycleView(){
        ContactRecycleView = findViewById(R.id.contactRecycleView);

        layoutManager = new LinearLayoutManager(this);
        ContactRecycleView.setHasFixedSize(true);
        ContactRecycleView.setLayoutManager(layoutManager);
        NewMessageAdapter mAdapter = new NewMessageAdapter(this, contactMap);

        ContactRecycleView.setAdapter(mAdapter);



    }

    void showContactCard(HashMap<String,String> selected){

        String cName=selected.get("name");
        String cPhone=selected.get("phone");

        TextView contactName=findViewById(R.id.textView3ContactNameSendTo);
        TextView contactNumber=findViewById(R.id.textView2ContactNumberSendTo);

        contactName.setText(cName);
        contactNumber.setText(cPhone);

        editTextPhoneNumber.setVisibility(View.GONE);
        ContactRecycleView.setVisibility(View.INVISIBLE);
        selectedContactCardView.setVisibility(View.VISIBLE);

    }

    public void clearSelectedContact(View v){

        editTextPhoneNumber.setVisibility(View.VISIBLE);
        ContactRecycleView.setVisibility(View.VISIBLE);
        selectedContactCardView.setVisibility(View.GONE);
        sendTo=null;

    }

    EditText sendMsgTextBox;
    public void sendMessageButton(View v){

        sendMsgTextBox=findViewById(R.id.sendMsgTextBox);
        message=sendMsgTextBox.getText().toString();

        if(sendTo==null || sendTo.equals("") || sendTo.equals("null")){
        if(editTextPhoneNumber.getText().equals(null) || editTextPhoneNumber.getText().equals("null") || editTextPhoneNumber.getText().equals("")){
            Toast.makeText(this, "Please Select Contact to Send Mssage", Toast.LENGTH_SHORT).show();
        }else {
            sendTo=editTextPhoneNumber.getText().toString();
            sendMessage();
        }

        }else{
            sendMessage();

        }
      //  long smsSendingTime = System.currentTimeMillis();


    }

    void sendMessage(){

        if(message.length()>0) {
            String tmp_msg = message;
            //    new_message.setText("Sending....");
            //   new_message.setEnabled(false);

            if(Function.sendSMS(sendTo, tmp_msg))
            {
              /*  sendMsgTextBox.setText("");
                new_message.setEnabled(true);
                // Creating a custom list for newly added sms
                customList.clear();
                customList.addAll(smsList);
                customList.add(Function.mappingInbox(null, null, null, null, tmp_msg, "2", null, "Sending...","1"));
              */
                long smsReceiveTime = System.currentTimeMillis();

                saveSms(sendTo,tmp_msg,"1",String.valueOf(smsReceiveTime),"outbox");
                // startLoadingDeviceSms();
                Toast.makeText(this, "Success: Message has been Send", Toast.LENGTH_SHORT).show();
                NewMessageActivity.this.finish();

            }else{
                //    new_message.setText(tmp_msg);
                //  new_message.setEnabled(true);
                Log.d(TAG, "SendMSG: ERROR !!");
                Toast.makeText(this, "Error in Sending Message", Toast.LENGTH_SHORT).show();

            }


        }else{
            Log.d(TAG, "SendMSG: MSG text too short");
            Toast.makeText(this, "Error: Message text too short", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean saveSms(String phoneNumber, String message, String readState, String time, String folderName) {
        boolean ret = false;
        try {
            ContentValues values = new ContentValues();
            values.put("address", phoneNumber);
            values.put("body", message);
            values.put("read", readState); //"0" for have not read sms and "1" for have read sms
            values.put("date", time);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Uri uri = Telephony.Sms.Sent.CONTENT_URI;
                if (folderName.equals("inbox")) {
                    uri = Telephony.Sms.Inbox.CONTENT_URI;
                }
                getContentResolver().insert(uri, values);
            } else {
                getContentResolver().insert(Uri.parse("content://sms/" + folderName), values);
            }

            ret = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            ret = false;
        }
        return ret;
    }


    public class NewMessageAdapter extends RecyclerView.Adapter<NewMessageAdapter.MyViewHolder> {
        public String TAG = String.valueOf(R.string.app_name);
        public Context mContext;

        ArrayList<HashMap<String, String>> ContactThreadHashMap = new ArrayList<>();



        // Provide a suitable constructor (depends on the kind of dataset)
        public NewMessageAdapter(Context mContext, ArrayList<HashMap<String, String>> h) {
            ContactThreadHashMap = h;

            this.mContext = mContext;

            //        mUser = new UserProfileManager(mContext);
            // mUser.Download();
        }

        // Create new views (invoked by the layout manager)
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
            // create a new view
            // TextView v = (TextView) LayoutInflater.from(parent.getContext())
            //       .inflate(R.layout.recycleview_Files_list, parent, false);


            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.contact_list_recycleview_item, parent, false);


            // Give the view as it is
            MyViewHolder vh = new MyViewHolder(v);


            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element

            HashMap<String, String> song = new HashMap<String, String>();
            song = ContactThreadHashMap.get(position);

            holder.contactTitleName.setText(song.get("name"));
            holder.LLContact.setTag(position);
            holder.contactPhoneNo.setText(song.get("phone"));


            String firstLetter;
            if (String.valueOf(song.get(Function.KEY_NAME).charAt(0)) != null) {
                firstLetter = "0";
                Log.d(TAG, "onBindViewHolder: First Letter NULL : " + song.get(Function.KEY_NAME).charAt(0));
                firstLetter = String.valueOf(song.get("name").charAt(0));
            } else {
                firstLetter = String.valueOf(song.get("name").charAt(0));
            }

            ColorGenerator generator = ColorGenerator.MATERIAL;
            int color = generator.getColor(getItem(position));
            TextDrawable drawable = TextDrawable.builder()
                    .buildRound(firstLetter, color);
            holder.contactImgPic.setImageDrawable(drawable);


            holder.LLContact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //view.getTag();
                    String tag = view.getTag().toString();
                    HashMap<String, String> selected = new HashMap<String, String>();
                    selected=ContactThreadHashMap.get(Integer.parseInt(tag));

                    Log.d(TAG, "onClick: Contact Click on: "+ContactThreadHashMap.get(Integer.parseInt(tag)));
sendTo=selected.get("phone");
showContactCard(selected);

                }
            });

/*

       holder.AddToLibraryChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //view.getTag();
                Log.d(TAG, "onClick: Added to Library " + view.getTag());
                mUser.AddFileLibrary(view.getTag().toString());

            }
        });  */


        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {

            return ContactThreadHashMap.size();

        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public class MyViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case

            TextView contactTitleName;
            ImageView contactImgPic;
            TextView contactPhoneNo;
            LinearLayout LLContact;


            public MyViewHolder(View v) {
                super(v);
                //     textView = v.findViewById(R.id.textView3);
                contactTitleName = v.findViewById(R.id.contactListTitle);
                contactImgPic = v.findViewById(R.id.contactListThumbIMG);

                contactPhoneNo = v.findViewById(R.id.contactListPhNo);

                LLContact= v.findViewById(R.id.contactCard);
                //      CheckIMG = v.findViewById(R.id.CheckICOimageView4);
                //    AddToLibraryChip=v.findViewById(R.id.AddToLibraryChipchip4);

            }
        }


    }



}
