package classify.domain.com.classify;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Login extends AppCompatActivity {

        /* Views */
        ProgressDialog progDialog;
        EditText usernameTxt;
        EditText passwordTxt;




        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.login);
            super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            // Set Back button
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);

            // Set Title on the ActionBar
            getSupportActionBar().setTitle("Login");


            // Init a ProgressDialog
            progDialog = new ProgressDialog(this);
            progDialog.setTitle(R.string.app_name);
            progDialog.setMessage("Logging in...");
            progDialog.setIndeterminate(false);

            usernameTxt = (EditText)findViewById(R.id.usernameTxt);
            passwordTxt = (EditText)findViewById(R.id.passwordTxt);



            // Login Button
            Button loginButt = (Button)findViewById(R.id.loginButt);
            loginButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progDialog.show();

                    ParseUser.logInInBackground(usernameTxt.getText().toString(), passwordTxt.getText().toString(),
                            new LogInCallback() {
                                public void done(ParseUser user, ParseException error) {
                                    if (user != null) {
                                        progDialog.dismiss();
                                        startActivity(new Intent(Login.this, Home.class));
                                    } else {
                                        progDialog.dismiss();
                                        Toast.makeText(getApplicationContext(), error.getMessage().toString(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                }
            });



            // Sign Up Button
            Button signupButt = (Button)findViewById(R.id.signUpButt);
            signupButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Login.this, SignUp.class));
                }
            });



            // Forgot Password Button
            Button fpButt = (Button)findViewById(R.id.forgotPassButt);
            fpButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(Login.this);
                    alert.setTitle(R.string.app_name);
                    alert.setMessage("Type the valid email address you've used to register on this app");

                    // Add an EditTxt
                    final EditText editTxt = new EditText (Login.this);
                    editTxt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                    alert.setView(editTxt);

                    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                            // Reset password
                            ParseUser.requestPasswordResetInBackground(editTxt.getText().toString(), new RequestPasswordResetCallback() {
                                public void done(ParseException error) {
                                    if (error == null) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                                        builder.setMessage("We've sent you an email to reset your password!")
                                                .setTitle(R.string.app_name)
                                                .setPositiveButton("OK", null);
                                        AlertDialog dialog = builder.create();
                                        dialog.setIcon(R.drawable.logo);
                                        dialog.show();

                                    } else {
                                        Toast.makeText(getApplicationContext(), error.getMessage().toString(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                        }});

                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }});

                    alert.show();

                }});






            // MARK: - FACEBOOK LOGIN BUTTON ------------------------------------------------------------------
            Button fbButt = (Button)findViewById(R.id.facebookButt);
            fbButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<String> permissions = Arrays.asList("public_profile", "email");
                    progDialog.setMessage("Please wait...");
                    progDialog.show();

                    ParseFacebookUtils.logInWithReadPermissionsInBackground(Login.this, permissions, new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if (user == null) {
                                Log.i("log-", "Uh oh. The user cancelled the Facebook login.");
                                progDialog.dismiss();

                            } else if (user.isNew()) {
                                getUserDetailsFromFB();

                            } else {
                                Log.i("log-", "RETURNING User logged in through Facebook!");
                                progDialog.dismiss();
                                startActivity(new Intent(Login.this, Home.class));
                    }}});
            }});


            // This code generates a KeyHash that you'll have to copy from your Logcat console and paste it into Key Hashes field in the 'Settings' section of your Facebook Android App
            try {
                PackageInfo info = getPackageManager().getPackageInfo(
                        // IMPORTANT: REPLACE THE PACKAGE NAME BELOW WITH YOUR OWN ONE!
                        "classify.domain.com.classify",

                        PackageManager.GET_SIGNATURES);
                for (Signature signature : info.signatures) {
                    MessageDigest md = MessageDigest.getInstance("SHA");
                    md.update(signature.toByteArray());
                    Log.i("log-", "keyhash: " + Base64.encodeToString(md.digest(), Base64.DEFAULT));
                }
            } catch (PackageManager.NameNotFoundException e) {
            } catch (NoSuchAlgorithmException e) {}



        }// end onCreate()






    // MARK: - FACEBOOK GRAPH REQUEST --------------------------------------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }


    void getUserDetailsFromFB() {
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),new GraphRequest.GraphJSONObjectCallback(){
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                String facebookID = "";
                String name = "";
                String email = "";
                String username = "";

                try{
                    name = object.getString("name");
                    email = object.getString("email");
                    facebookID = object.getString("id");

                    String[] one = name.toLowerCase().split(" ");
                    for (String word : one) { username += word; }
                    Log.i("log-", "USERNAME: " + username + "\n");
                    Log.i("log-", "email: " + email + "\n");
                    Log.i("log-", "name: " + name + "\n");

                } catch(JSONException e){ e.printStackTrace(); }


                // SAVE NEW USER IN YOUR PARSE DASHBOARD -> USER CLASS
                final String finalFacebookID = facebookID;
                final String finalUsername = username;
                final String finalEmail = email;
                final String finalName = name;

                final ParseUser currUser = ParseUser.getCurrentUser();
                currUser.put(Configs.USER_USERNAME, finalUsername);
                currUser.put(Configs.USER_EMAIL, finalEmail);
                currUser.put(Configs.USER_FULLNAME, finalName);

                currUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        progDialog.dismiss();
                        Log.i("log-", "NEW USER signed up and logged in through Facebook...");

                        // Get and Save avatar from Facebook
                        new Timer().schedule(new TimerTask() {
                            @Override public void run() {
                                try {
                                    URL imageURL = new URL("https://graph.facebook.com/" + finalFacebookID + "/picture?type=large");
                                    Bitmap avatarBm = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());

                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                    avatarBm.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                    byte[] byteArray = stream.toByteArray();
                                    ParseFile imageFile = new ParseFile("image.jpg", byteArray);
                                    currUser.put(Configs.USER_AVATAR, imageFile);
                                    currUser.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException error) {
                                            Log.i("log-", "...AND AVATAR SAVED!");
                                            startActivity(new Intent(Login.this, Home.class));
                                        }});
                                } catch (IOException error) { error.printStackTrace(); }

                            }}, 1000); // 1 second


                    }}); // end saveInBackground

            }}); // end graphRequest


        Bundle parameters = new Bundle();
        parameters.putString("fields", "email, name, picture.type(large)");
        request.setParameters(parameters);
        request.executeAsync();
    }
// END FACEBOOK GRAPH REQUEST --------------------------------------------------------------------







        // BACK BUTTON ----------------------------------------------------------------
        @Override
        public boolean onOptionsItemSelected(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                // DEFAULT BACK BUTTON
                case android.R.id.home:
                    this.finish();
                    return true;
            }
            return (super.onOptionsItemSelected(menuItem));
        }

    }//@end





