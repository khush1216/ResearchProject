package edu.uic.kdurge2.cs478.proj1_temp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;


public class SignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private String userDetails[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build());

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//                mDatabase = FirebaseDatabase.getInstance().getReference();
//                mDatabase.child("users").child(user.toString()).child("emailid").setValue(user.getEmail());
                userDetails = new String[3];
                userDetails[0] = user.getUid();
                userDetails[1] = user.getDisplayName();
                userDetails[2] = user.getEmail();
                Intent mainActivityLaunch = new Intent(this, UserProfile.class);
                mainActivityLaunch.putExtra("userdetails",userDetails);
                startActivity(mainActivityLaunch);
                // ...
            } else {
                //sign in failed
                Log.i("error",response.toString());
                Toast.makeText(this, "Unable to Sign in! :(", Toast.LENGTH_SHORT).show();

            }
        }
    }

}
