package devesh.ephrine.backup.sms;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */

public class HeadlessSmsSendService extends IntentService {
    final String TAG = "HeadlessSmsSendService ";

    public HeadlessSmsSendService() {

        super(HeadlessSmsSendService.class.getName());
        Log.d(TAG, "HeadlessSmsSendService: HeadlessSmsSendService() ");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent: ");
        //  throw new UnsupportedOperationException("HeadlessSmsSendService");
    }


}
