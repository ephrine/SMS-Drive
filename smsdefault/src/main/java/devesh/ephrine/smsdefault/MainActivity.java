package devesh.ephrine.smsdefault;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {
String TAG="SMS App";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void setDefaultSmsApp(View v) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final String myPackageName = getPackageName();
            if (!Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName)) {
                Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
                startActivity(intent);
          /*  Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, myPackageName);
            startActivityForResult(intent, 1);
         */
//                isDefaultSmsApp = true;
                //      lottieAnimationView1.setVisibility(View.INVISIBLE);
                //    lottieAnimationView2.setVisibility(View.VISIBLE);
                Log.d(TAG, "setDefaultSmsApp: setting default SMS handler");
            } else {
  //              isDefaultSmsApp = true;
                //    lottieAnimationView1.setVisibility(View.INVISIBLE);
                //      lottieAnimationView2.setVisibility(View.VISIBLE);

            }

        } else {
    //        isDefaultSmsApp = true;
            // saveSms("111111", "mmmmssssggggg", "0", "", "inbox");
            //   lottieAnimationView1.setVisibility(View.INVISIBLE);
//        lottieAnimationView2.setVisibility(View.VISIBLE);

        }


    }

}
