package classify.domain.com.classify;

import android.accounts.Account;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetDataCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AccountScreen extends AppCompatActivity {

    /* Views */
    ProgressDialog pd;



    /* Variables */
    MarshMallowPermission marshMallowPermission = new MarshMallowPermission(this);




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_screen);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set Title of the ActionBar
        getSupportActionBar().setTitle("Account");


        final ParseUser currUser = ParseUser.getCurrentUser();


        // Init a ProgressDialog
        pd = new ProgressDialog(this);
        pd.setTitle(R.string.app_name);
        pd.setIndeterminate(false);


        // Init TabBar buttons
        Button tab_home = (Button)findViewById(R.id.tab_home);
        Button tab_fav = (Button)findViewById(R.id.tab_fav);

        tab_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AccountScreen.this, Favorites.class));
            }});

        tab_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AccountScreen.this, Home.class));
            }});





        // MARK: - SAVE ROFILE BUTTON  ----------------------------------------------------------------
        Button saveProfileButt = (Button)findViewById(R.id.saveProfileButt);
        saveProfileButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd.setMessage("Updating Profile...");
                pd.show();

                EditText fnTxt = (EditText)findViewById(R.id.acFullnameTxt);
                EditText emTxt = (EditText)findViewById(R.id.acEmailTxt);
                EditText phTxt = (EditText)findViewById(R.id.acPhoneTxt);
                EditText wTxt = (EditText)findViewById(R.id.acWebsiteTxt);

                currUser.put(Configs.USER_FULLNAME, fnTxt.getText().toString());
                currUser.put(Configs.USER_EMAIL, emTxt.getText().toString());

                if (!phTxt.getText().toString().matches("")) {
                    currUser.put(Configs.USER_PHONE, phTxt.getText().toString());
                }
                if (!wTxt.getText().toString().matches("")) {
                    currUser.put(Configs.USER_WEBSITE, wTxt.getText().toString());
                }

                // Save avatar image
                ImageView evImage = (ImageView) findViewById(R.id.avatarImage);
                Bitmap bitmap = ((BitmapDrawable) evImage.getDrawable()).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                byte[] byteArray = stream.toByteArray();
                ParseFile imageFile = new ParseFile("image.jpg", byteArray);
                currUser.put(Configs.USER_AVATAR, imageFile);

                // Saving block
                currUser.saveInBackground(new SaveCallback() {
                     @Override
                     public void done(ParseException error) {
                        if (error == null) {
                            pd.dismiss();

                                AlertDialog.Builder alert = new AlertDialog.Builder(AccountScreen.this);
                                alert.setMessage("Your Profile has been updated")
                                .setTitle(R.string.app_name)
                                .setIcon(R.drawable.logo)
                                .setPositiveButton("OK", null);
                                alert.create().show();

                        // error
                        } else {
                            Toast.makeText(getApplicationContext(), error.getMessage().toString(), Toast.LENGTH_SHORT).show();
                            pd.dismiss();
                }}});


            }});





        // MARK: - MY ADS BUTTON
        Button myAdsButt = (Button)findViewById(R.id.myAdsButt);
        myAdsButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AccountScreen.this, MyAds.class));
            }});



        // Show User's data
        showUserData();


    }// end onCreate()




    // MARK: - SHOW USER'S DATA
    void showUserData() {

        ParseUser currUser = ParseUser.getCurrentUser();

        // Get Avatar Image
        final ImageView avImage = (ImageView) findViewById(R.id.avatarImage);
        ParseFile fileObject = (ParseFile)currUser.get(Configs.USER_AVATAR);
        if (fileObject != null ) {
            fileObject.getDataInBackground(new GetDataCallback() {
                public void done(byte[] data, ParseException error) {
                    if (error == null) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        if (bmp != null) {
                            avImage.setImageBitmap(bmp);
            }}}});
        }

        // Get FullName
        TextView unTxt = (TextView) findViewById(R.id.acUsernameTxt);
        unTxt.setText(currUser.getString(Configs.USER_USERNAME));

        // Get FullName
        EditText fnTxt = (EditText)findViewById(R.id.acFullnameTxt);
        fnTxt.setText(currUser.getString(Configs.USER_FULLNAME));

        // Get Email
        EditText emTxt = (EditText)findViewById(R.id.acEmailTxt);
        emTxt.setText(currUser.getString(Configs.USER_EMAIL));

        // Get Phone
        EditText phTxt = (EditText)findViewById(R.id.acPhoneTxt);
        if (currUser.getString(Configs.USER_PHONE) != null) {
            phTxt.setText(currUser.getString(Configs.USER_PHONE));
        } else {
            phTxt.setText("");
        }

        // Get Website
        EditText wTxt = (EditText)findViewById(R.id.acWebsiteTxt);
        if (currUser.getString(Configs.USER_WEBSITE) != null) {
            wTxt.setText(currUser.getString(Configs.USER_WEBSITE));
        } else {
            wTxt.setText("");
        }



        // Set onClickListener to load an Avatar
        avImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert  = new AlertDialog.Builder(AccountScreen.this);
                alert.setTitle("SELECT SOURCE")
                        .setIcon(R.drawable.logo)
                        .setItems(new CharSequence[]
                                        {"Take a picture",
                                         "Pick from Gallery" },
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            // Open Camera
                                            case 0:
                                                if (!marshMallowPermission.checkPermissionForCamera()) {
                                                    marshMallowPermission.requestPermissionForCamera();
                                                } else {
                                                    openCamera();
                                                }
                                                break;

                                            // Open Gallery
                                            case 1:
                                                if (!marshMallowPermission.checkPermissionForReadExternalStorage()) {
                                                    marshMallowPermission.requestPermissionForReadExternalStorage();
                                                } else {
                                                    openGallery();
                                                }
                                                break;
                                        }
                                    }});
                alert.create().show();

            }});

    }







    // IMAGE HANDLING METHODS ------------------------------------------------------------------------
    int CAMERA = 0;
    int GALLERY = 1;

    // OPEN CAMERA
    public void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA);
    }

    // OPEN GALLERY
    public void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), GALLERY);
    }

    // IMAGE PICKED DELEGATE
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            Bitmap bm = null;
            if (requestCode == CAMERA) {
                bm = (Bitmap) data.getExtras().get("data");


            } else if (requestCode == GALLERY) {
                try {
                    bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                } catch (IOException e) { e.printStackTrace(); }
            }

            // Set image
            ImageView stImage = (ImageView)findViewById(R.id.avatarImage);
            stImage.setImageBitmap(bm);
        }
    }
    //---------------------------------------------------------------------------------------------












    // MENU BUTTON ON ACTION BAR ----------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.account_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            // Post Ad Button
            case R.id.postAdButt:
                // Go to Post Ad
                startActivity(new Intent(AccountScreen.this, PostAd.class));
                return true;



            // Logout Button
            case R.id.logoutButt:
                AlertDialog.Builder alert = new AlertDialog.Builder(AccountScreen.this);
                alert.setMessage("Are you sure you want to logout?")
                    .setTitle(R.string.app_name)
                    .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            pd.setMessage("Logging out...");
                            pd.show();

                            ParseUser.logOutInBackground(new LogOutCallback() {
                                @Override
                                public void done(ParseException e) {
                                    pd.dismiss();
                                    // Go back to Home
                                    startActivity(new Intent(AccountScreen.this, Home.class));
                                }});
                    }})
                    .setNegativeButton("Cancel", null)
                    .setIcon(R.drawable.logo);
                alert.create().show();

                return true;
        }

        return (super.onOptionsItemSelected(menuItem));
    }

}//@end
