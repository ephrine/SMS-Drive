/*
 * Copyright (c) 2019. Ephrine Apps
 * Code written by Devesh Chaudhari
 * Website: https://www.ephrine.in
 */

package devesh.ephrine.backup.sms.pushnotification;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

import devesh.ephrine.backup.sms.Function;
import devesh.ephrine.backup.sms.R;


public class EpNotificationAdapter extends RecyclerView.Adapter<EpNotificationAdapter.MyViewHolder> {

    public String TAG = String.valueOf(R.string.app_name);
    public Context mContext;

    ArrayList<HashMap<String, String>> notificationData = new ArrayList<>();


    // Provide a suitable constructor (depends on the kind of dataset)
    public EpNotificationAdapter(Context mContext, ArrayList<HashMap<String, String>> h) {
        notificationData = h;

        this.mContext = mContext;


        //        mUser = new UserProfileManager(mContext);
        // mUser.Download();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {


        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ep_notification_recycleview_item, parent, false);


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
        song = notificationData.get(position);
        Log.d(TAG, "onBindViewHolder: Song " + song);
        String title = song.get(EpNotificationsConstants.EP_FCM_TITLE);
        String desc = song.get(EpNotificationsConstants.EP_FCM_DESC);
        String timex = song.get("time");
        String url = song.get(EpNotificationsConstants.EP_FCM_URL);

        holder.title.setText(title);
        holder.LLNotificationItem.setTag(position);
        holder.desc.setText(desc);


        holder.time.setText(Function.converToTime(timex));
        holder.LLNotificationItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //view.getTag();

                String tag = view.getTag().toString();
                int i = Integer.parseInt(tag);
                if (notificationData.get(i).get(EpNotificationsConstants.EP_FCM_URL) != null) {
                    String url = notificationData.get(i).get(EpNotificationsConstants.EP_FCM_URL);

                    ((EpNotificationActivity) mContext).openBrowser(url);
                }


            }
        });


        //  String uri = "@drawable/myresource";  // where myresource (without the extension) is the file

        //    int imageResource = mContext.getResources().getIdentifier(uri, null, mContext.getPackageName());
        if (url != null) {
            if (url.contains("play.google.com") || url.contains("galaxy.store") || url.contains("apps.samsung.com")) {

                Drawable res = mContext.getResources().getDrawable(R.drawable.ic_system_update_50dp);
                holder.imgThumb.setImageDrawable(res);

            } else {

                Drawable res = mContext.getResources().getDrawable(R.drawable.ic_event_note);
                holder.imgThumb.setImageDrawable(res);

  /*      String firstLetter;
        firstLetter = String.valueOf(title.charAt(0));
        ColorGenerator generator = ColorGenerator.MATERIAL;
        int color = generator.getColor(getItem(position));
        TextDrawable drawable = TextDrawable.builder()
                .buildRound(firstLetter, color);
        holder.imgThumb.setImageDrawable(drawable);
*/
            }


        } else {
            Drawable res = mContext.getResources().getDrawable(R.drawable.ic_event_note);
            holder.imgThumb.setImageDrawable(res);

  /*  String firstLetter;
    firstLetter = String.valueOf(title.charAt(0));
    ColorGenerator generator = ColorGenerator.MATERIAL;
    int color = generator.getColor(getItem(position));
    TextDrawable drawable = TextDrawable.builder()
            .buildRound(firstLetter, color);
    holder.imgThumb.setImageDrawable(drawable);
*/
        }


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {

        return notificationData.size();

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

        TextView title;
        LinearLayout LLNotificationItem;
        ImageView imgThumb;
        TextView desc;
        TextView time;


        public MyViewHolder(View v) {
            super(v);
            //     textView = v.findViewById(R.id.textView3);
            title = v.findViewById(R.id.textView3680Title);
            LLNotificationItem = v.findViewById(R.id.LLNotificationItem354);
            imgThumb = v.findViewById(R.id.imageView5645);
            desc = v.findViewById(R.id.textView4578Desc);
            time = v.findViewById(R.id.textView895Time);


        }
    }


}
