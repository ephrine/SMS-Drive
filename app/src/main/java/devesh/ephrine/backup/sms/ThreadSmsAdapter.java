/*
 * Copyright (c) 2019. Ephrine Apps
 * Code written by Devesh Chaudhari
 * Website: https://www.ephrine.in
 */

package devesh.ephrine.backup.sms;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;


public class ThreadSmsAdapter extends RecyclerView.Adapter<ThreadSmsAdapter.MyViewHolder> {
    public static final String EXTRA_MESSAGE = "ReadFileID";
    public static final double SPACE_KB = 1024;
    public static final double SPACE_MB = 1024 * SPACE_KB;
    public static final double SPACE_GB = 1024 * SPACE_MB;
    public static final double SPACE_TB = 1024 * SPACE_GB;
    public String TAG = String.valueOf(R.string.app_name);
    public Context mContext;
    // ArrayList<String> SmsList = new ArrayList<>();
    //HashMap<String, DataSnapshot> SmsThreadHashMap=new HashMap<>();
    ArrayList<HashMap<String, String>> SmsThreadHashMap = new ArrayList<>();


    // Provide a suitable constructor (depends on the kind of dataset)
    public ThreadSmsAdapter(Context mContext, ArrayList<HashMap<String, String>> h) {

        SmsThreadHashMap = h;

        this.mContext = mContext;
        //  mUser = new UserProfileManager(mContext);
        // mUser.Download();
        Log.d(TAG, "ThreadSmsAdapter: START");

    }


    // Create new views (invoked by the layout manager)
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {


        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sms_recycleview_message_item, parent, false);


        // Give the view as it is
        MyViewHolder vh = new MyViewHolder(v);


        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        //  holder.SmsTimeTx.setText(SmsThreadHashMap.get(position).get("time"));
        Log.d(TAG, "onBindViewHolder: Get MSG: " + SmsThreadHashMap.get(position).toString());

        HashMap<String, String> song = new HashMap<String, String>();
        song = SmsThreadHashMap.get(position);

        //1 =Inbox
        // 0=Outbox
        if (SmsThreadHashMap.get(position).get(Function.KEY_TYPE).equals("1")) {

            holder.InboxMSGCard.setVisibility(View.VISIBLE);
            holder.OutboxMSGCard.setVisibility(View.GONE);

            holder.SmsMsgTx.setText(song.get(Function.KEY_MSG));
            holder.SmsTimeTx.setText(song.get(Function.KEY_TIME));
        } else {

            holder.InboxMSGCard.setVisibility(View.GONE);
            holder.OutboxMSGCard.setVisibility(View.VISIBLE);

            holder.SmsOutBoxMsgTx.setText(song.get(Function.KEY_MSG));
            holder.SmsOutBoxTimeTx.setText(song.get(Function.KEY_TIME));

        }

        Log.d(TAG, "onBindViewHolder: " + song.get(Function.KEY_MSG));

      /*  if(position==getItemCount()-1){
holder.bottomSpace.setVisibility(View.VISIBLE);
            Log.d(TAG, "onBindViewHolder: at END ____");
        }
        holder.LLSmsItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //view.getTag();
                Log.d(TAG, "onClick: " + view.getTag());



            }
        });


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

        return SmsThreadHashMap.size();

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
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case

        TextView SmsMsgTx;
        TextView SmsTimeTx;
        LinearLayout LLSmsItem;
        CardView InboxMSGCard;
        CardView OutboxMSGCard;

        TextView SmsOutBoxMsgTx;
        TextView SmsOutBoxTimeTx;

      //  Space bottomSpace;

        public MyViewHolder(View v) {
            super(v);

            //Inbox
            SmsMsgTx = v.findViewById(R.id.textViewSmsMSG1);
            SmsTimeTx = v.findViewById(R.id.textView2SmsDateTime1);
            LLSmsItem = v.findViewById(R.id.ScreenSmsThread1);
            InboxMSGCard = v.findViewById(R.id.cardViewinbox);

            //Outbox
            OutboxMSGCard = v.findViewById(R.id.cardViewoutbox);
            SmsOutBoxMsgTx = v.findViewById(R.id.textViewSmsMSGOutbox);
            SmsOutBoxTimeTx = v.findViewById(R.id.textView2SmsDateTime1Outbox);

          //  bottomSpace=v.findViewById(R.id.threadBottomSpace);
        }
    }
}
