/*
 * Copyright (c) 2019. Ephrine Apps
 * Code written by Devesh Chaudhari
 * Website: https://www.ephrine.in
 */

package devesh.ephrine.backup.sms;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
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
import java.util.HashMap;


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
                Log.d(TAG, "onClick: Contact Click on: "+ContactThreadHashMap.get(Integer.parseInt(tag)));
                
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
    public static class MyViewHolder extends RecyclerView.ViewHolder {
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
