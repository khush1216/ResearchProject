package edu.uic.kdurge2.cs478.proj1_temp;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;


public class SignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private String userDetails[];
    ConnectivityManager cm;
    public static String staticUserDetailsBase[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(checkForInternetConn()) {
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build());

            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN);
        }
        else {
            Toast.makeText(this, "Connect to Internet and Re-start!", Toast.LENGTH_SHORT).show();

        }

        storeFileFromRawToInternal();
    }

    private boolean checkForInternetConn(){
            cm  = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if(ni == null){
            return false;

        }
        else
            return true;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                userDetails = new String[3];
                staticUserDetailsBase = new String[3];
                userDetails[0] = user.getUid();
                userDetails[1] = user.getDisplayName();
                userDetails[2] = user.getEmail();
                staticUserDetailsBase = userDetails;
                Intent mainActivityLaunch = new Intent(this, UserProfile.class);
                mainActivityLaunch.putExtra("userdetails",userDetails);
                startActivity(mainActivityLaunch);
                // ...
            } else {
                //sign in failed
                Toast.makeText(this, "Unable to Sign in! :(", Toast.LENGTH_SHORT).show();
                Intent a = new Intent(Intent.ACTION_MAIN);
                a.addCategory(Intent.CATEGORY_HOME);
                a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(a);
                //Log.i("error",response.toString());
                //

            }
        }
    }


    public void storeFileFromRawToInternal(){
        InputStream databaseInputStream = getResources().openRawResource(R.raw.random_forests);
        InputStream databaseInputStreamFile2 = getResources().openRawResource(R.raw.logistic_regression_classifier);

        String path1 = this.getFilesDir() + "/" + "RandomForests.model";
        String path2 = this.getFilesDir() + "/" + "LogisticRegressionClassifier.model";


        try {
            byte[] buffer = new byte[databaseInputStream.available()];
            databaseInputStream.read(buffer);
            File targetFile = new File(path1);
            OutputStream outStream1 = new FileOutputStream(targetFile);
            outStream1.write(buffer);

            byte[] buffer2 = new byte[databaseInputStreamFile2.available()];
            databaseInputStreamFile2.read(buffer2);
            File classifierFile2 = new File(path2);
            OutputStream outStream2 = new FileOutputStream(classifierFile2);
            outStream2.write(buffer2);
        }
        catch (Exception e){
            Toast.makeText(this, "Cannot store classifier!", Toast.LENGTH_SHORT).show();
        }

        finally {

            Toast.makeText(this, "Uploaded classifiers to internal storage!", Toast.LENGTH_SHORT).show();
            //close output streams
        }

    }
}
