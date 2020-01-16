/*
 * Copyright (c) 2019. Ephrine Apps
 * Code written by Devesh Chaudhari
 * Website: https://www.ephrine.in
 */

package devesh.ephrine.backup.sms;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import devesh.ephrine.backup.sms.ThreadSmsActivity;


public class SmsAdapter extends RecyclerView.Adapter<SmsAdapter.MyViewHolder> {
    public static final String EXTRA_MESSAGE = "ReadFileID";
    public static final double SPACE_KB = 1024;
    public static final double SPACE_MB = 1024 * SPACE_KB;
    public static final double SPACE_GB = 1024 * SPACE_MB;
    public static final double SPACE_TB = 1024 * SPACE_GB;
    public String TAG = String.valueOf(R.string.app_name);
    public Context mContext;

    ArrayList<String> SmsThreadHashMap = new ArrayList<>();


    // Provide a suitable constructor (depends on the kind of dataset)
    public SmsAdapter(Context mContext, ArrayList<String> h) {
        SmsThreadHashMap = h;

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
                .inflate(R.layout.sms_recycleview_item, parent, false);


        // Give the view as it is
        MyViewHolder vh = new MyViewHolder(v);


        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        holder.SmsThreadTx.setText(SmsThreadHashMap.get(position));
        holder.LLSmsList.setTag(SmsThreadHashMap.get(position));
        holder.LLSmsList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //view.getTag();
                Log.d(TAG, "onClick: " + view.getTag());
                String tag = view.getTag().toString();
                Intent intent = new Intent(mContext, ThreadSmsActivity.class);
                //    intent.putExtra("smsthread",SmsThreadHashMap);
                intent.putExtra("smsthreadid", tag);

                mContext.startActivity(intent);


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

        return SmsThreadHashMap.size();

    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case

        TextView SmsThreadTx;
        LinearLayout LLSmsList;


        public MyViewHolder(View v) {
            super(v);
            //     textView = v.findViewById(R.id.textView3);
            SmsThreadTx = v.findViewById(R.id.TxSmsList);
            LLSmsList = v.findViewById(R.id.LLSmsList);


            //   CloudDownloadIMG= v.findViewById(R.id.cloudDownloadimageView3);
            //      CheckIMG = v.findViewById(R.id.CheckICOimageView4);
            //    AddToLibraryChip=v.findViewById(R.id.AddToLibraryChipchip4);

        }
    }


}
