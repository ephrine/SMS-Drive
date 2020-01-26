package devesh.ephrine.backup.sms;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.crashlytics.android.Crashlytics;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

import io.fabric.sdk.android.Fabric;

public class StartActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        setContentView(R.layout.activity_start);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            Crashlytics.setUserIdentifier(currentUser.getUid());

            Intent intent = new Intent(this, MainActivity.class);
            //  String message = editText.getText().toString();
            //intent.putExtra(EXTRA_MESSAGE, message);
            startActivity(intent);
            StartActivity.this.finish();
        } else {
            // Choose authentication providers
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    //   new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.PhoneBuilder().build()
                    //   new AuthUI.IdpConfig.GoogleBuilder().build(),
                    //  new AuthUI.IdpConfig.FacebookBuilder().build(),
                    //        new AuthUI.IdpConfig.TwitterBuilder().build()
            );

// Create and launch sign-in intent
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.

        //  updateUI(currentUser);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Intent intent = new Intent(this, MainActivity.class);

                //  String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);
                startActivity(intent);
                StartActivity.this.finish();
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }

    public void loginbutton(View v) {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                //   new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build()
                //   new AuthUI.IdpConfig.GoogleBuilder().build(),
                //  new AuthUI.IdpConfig.FacebookBuilder().build(),
                //        new AuthUI.IdpConfig.TwitterBuilder().build()
        );

// Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }
}
