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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.util.ArrayList;
import java.util.List;

import devesh.ephrine.backup.sms.room.Sms;


public class CloudSmsAdapter extends RecyclerView.Adapter<CloudSmsAdapter.MyViewHolder> {
    public static final String EXTRA_MESSAGE = "ReadFileID";
    public static final double SPACE_KB = 1024;
    public static final double SPACE_MB = 1024 * SPACE_KB;
    public static final double SPACE_GB = 1024 * SPACE_MB;
    public static final double SPACE_TB = 1024 * SPACE_GB;
    public String TAG = String.valueOf(R.string.app_name);
    public Context mContext;
    List<Sms> SmsList = new ArrayList<>();

    // Provide a suitable constructor (depends on the kind of dataset)
    public CloudSmsAdapter(Context mContext, List<Sms> Sms_list) {

        this.mContext = mContext;

        SmsList = Sms_list;

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

        Sms sms = SmsList.get(position);
        holder.LLSmsList.setTag(position);

        holder.MSGText.setText(sms.KEY_MSG);
        holder.timeTxt.setText(Function.converToTime(sms.KEY_TIMESTAMP));

        String personName = Function.getContactbyPhoneNumber(mContext, sms.KEY_PHONE);
        holder.SmsThreadTx.setText(personName);

        String firstLetter;
        firstLetter = String.valueOf(personName.charAt(0));
        ColorGenerator generator = ColorGenerator.MATERIAL;
        int color = generator.getColor(getItem(position));
        TextDrawable drawable = TextDrawable.builder()
                .buildRound(firstLetter, color);
        holder.imgThumb.setImageDrawable(drawable);


        holder.LLSmsList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //view.getTag();

                String tag = view.getTag().toString();
                int i = Integer.parseInt(tag);
                Log.d(TAG, "onClick: Device Message \n " + view.getTag() + "\n Name: " +SmsList.get(i).KEY_PHONE);
                Intent intent = new Intent(mContext, ThreadSmsActivity.class);
            //    intent.putExtra("name", SmsList.get(i).get(Function.KEY_NAME));
                intent.putExtra("address", SmsList.get(i).KEY_PHONE);
              //  intent.putExtra("thread_id", SmsList.get(i).get(Function.KEY_THREAD_ID));
                intent.putExtra("storage", "C");

                mContext.startActivity(intent);

            }
        });

/*
        HashMap<String, String> song = new HashMap<String, String>();
        song = SmsThreadHashMap.get(position);

        holder.SmsThreadTx.setText(song.get(Function.KEY_NAME));
        holder.LLSmsList.setTag(position);

        if (song.get(Function.KEY_READ) != null) {
            if (song.get(Function.KEY_READ).equals("0")) {
                Log.d(TAG, "onBindViewHolder: Found MSG UnRead");
                holder.SmsThreadTx.setTypeface(holder.SmsThreadTx.getTypeface(), Typeface.BOLD_ITALIC);
                holder.MSGText.setTypeface(holder.MSGText.getTypeface(), Typeface.BOLD_ITALIC);
                holder.timeTxt.setTypeface(holder.timeTxt.getTypeface(), Typeface.BOLD_ITALIC);

            }
        }

        try{

            String firstLetter;
            String.valueOf(song.get(Function.KEY_NAME).charAt(0));
            firstLetter = "0";
            Log.d(TAG, "onBindViewHolder: First Letter NULL : " + song.get(Function.KEY_NAME).charAt(0));
            firstLetter = String.valueOf(song.get(Function.KEY_NAME).charAt(0));
            ColorGenerator generator = ColorGenerator.MATERIAL;
            int color = generator.getColor(getItem(position));
            TextDrawable drawable = TextDrawable.builder()
                    .buildRound(firstLetter, color);
            holder.imgThumb.setImageDrawable(drawable);

        }catch (Exception e){
            Log.e(TAG, "onBindViewHolder: ERROR #567", e);
        }




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

        return SmsList.size();

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

        TextView SmsThreadTx;
        LinearLayout LLSmsList;
        ImageView imgThumb;
        TextView MSGText;
        TextView timeTxt;


        public MyViewHolder(View v) {
            super(v);
            //     textView = v.findViewById(R.id.textView3);
            SmsThreadTx = v.findViewById(R.id.TxSmsList);
            LLSmsList = v.findViewById(R.id.LLSmsList);
            MSGText = v.findViewById(R.id.textViewMSG);
            imgThumb = v.findViewById(R.id.imageViewSMSThumb);
            timeTxt = v.findViewById(R.id.textView4Time);

            //   CloudDownloadIMG= v.findViewById(R.id.cloudDownloadimageView3);
            //      CheckIMG = v.findViewById(R.id.CheckICOimageView4);
            //    AddToLibraryChip=v.findViewById(R.id.AddToLibraryChipchip4);

        }
    }


}
