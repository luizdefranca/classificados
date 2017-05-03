package classify.domain.com.classify;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.ParseException;
import android.net.Uri;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ShowSingleAd extends AppCompatActivity {

    /* Views */
    ProgressDialog pd;


    /* Variables */
    ParseObject adObj;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_single_ad);

        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        // Get objectID from previous .java
        Intent intent = getIntent();
        String objectID = intent.getStringExtra("objectID");
        adObj = ParseObject.createWithoutData(Configs.CLASSIF_CLASS_NAME, objectID);
        try { adObj.fetchIfNeeded().getParseObject(Configs.CLASSIF_CLASS_NAME);
        } catch (com.parse.ParseException e) {
            e.printStackTrace();
        }


        // Set Title of the ActionBar
        getSupportActionBar().setTitle(adObj.getString(Configs.CLASSIF_TITLE));




        // MARK: - TAP ON IMAGES TO SHOW FULL SCREEN PREVIEW
        final ImageView prevImg = (ImageView)findViewById(R.id.prevImg);

        ImageView img1 = (ImageView)findViewById(R.id.image1);
        img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPreviewLayout();

                ImageView img1 = (ImageView)findViewById(R.id.image1);
                Bitmap bitmap = ((BitmapDrawable)img1.getDrawable()).getBitmap();
                prevImg.setImageBitmap(bitmap);

            }
        });

        ImageView img2 = (ImageView)findViewById(R.id.image2);
        img2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPreviewLayout();

                ImageView img1 = (ImageView)findViewById(R.id.image2);
                Bitmap bitmap = ((BitmapDrawable)img1.getDrawable()).getBitmap();
                prevImg.setImageBitmap(bitmap);

            }
        });

        ImageView img3 = (ImageView)findViewById(R.id.image3);
        img3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPreviewLayout();

                ImageView img1 = (ImageView)findViewById(R.id.image3);
                Bitmap bitmap = ((BitmapDrawable)img1.getDrawable()).getBitmap();
                prevImg.setImageBitmap(bitmap);
            }
        });





        // MARK: - TAP TO HIDE THE PREVIEW LAYOUT
        prevImg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(v.getId()) {
                    case R.id.prevImg:
                        switch(event.getAction()){
                            case MotionEvent.ACTION_DOWN:
                                hidePreviewLayout();
                                break;
                        }
                        break;
                }
                return true;
            }});



        // Call query
        showAdDetails();



        // Init AdMob banner
        AdView mAdView = (AdView) findViewById(R.id.admobBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);




    }//end onCreate()







    // MARK: - SHOW AD DETAILS
    void showAdDetails() {
        // Get User Pointer
        final ParseUser userPointer = adObj.getParseUser(Configs.CLASSIF_USER_POINTER);
        try {
            userPointer.fetchIfNeeded();
        } catch (com.parse.ParseException e) {
            e.printStackTrace();
        }


        // Get Title
        TextView titletxt = (TextView) findViewById(R.id.adTitleTxt);
        titletxt.setText(adObj.getString(Configs.CLASSIF_TITLE));


        // Get Image1
        final ImageView img1 = (ImageView) findViewById(R.id.image1);
        ParseFile fileObject = (ParseFile) adObj.get(Configs.CLASSIF_IMAGE1);
        if (fileObject != null) {
            fileObject.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, com.parse.ParseException error) {
                    if (error == null) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        if (bmp != null) {
                            img1.setImageBitmap(bmp);
                        }
                    }
                }
            });
        }

        // Get Image2
        final ImageView img2 = (ImageView) findViewById(R.id.image2);
        ParseFile fileObject2 = (ParseFile) adObj.get(Configs.CLASSIF_IMAGE2);
        if (fileObject2 != null) {
            fileObject2.getDataInBackground(new GetDataCallback() {
                public void done(byte[] data, com.parse.ParseException error) {
                    if (error == null) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        if (bmp != null) {
                            img2.setImageBitmap(bmp);
                        }
                    }
                }
            });
        }

        // Get Image3
        final ImageView img3 = (ImageView) findViewById(R.id.image3);
        ParseFile fileObject3 = (ParseFile) adObj.get(Configs.CLASSIF_IMAGE3);
        if (fileObject3 != null) {
            fileObject3.getDataInBackground(new GetDataCallback() {
                public void done(byte[] data, com.parse.ParseException error) {
                    if (error == null) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        if (bmp != null) {
                            img3.setImageBitmap(bmp);
                        }
                    }
                }
            });
        }


        // Get price
        TextView priceTxt = (TextView) findViewById(R.id.priceTxt);
        priceTxt.setText("PRICE: " + adObj.getString(Configs.CLASSIF_PRICE));

        // Get Description
        TextView descTxt = (TextView) findViewById(R.id.descriptionTxt);
        descTxt.setText(adObj.getString(Configs.CLASSIF_DESCRIPTION));

        // Get Address
        TextView addtxt = (TextView) findViewById(R.id.addressTxt);
        addtxt.setText("ADDRESS: " + adObj.getString(Configs.CLASSIF_ADDRESS_STRING));

        // Get PostedBy
        TextView pbTxt = (TextView) findViewById(R.id.postedByTxt);
        pbTxt.setText("POSTED BY: " + userPointer.getString(Configs.USER_FULLNAME));

        // Get website
        TextView websTxt = (TextView) findViewById(R.id.websiteTxt);
        if (userPointer.getString(Configs.USER_WEBSITE) != null) {
            websTxt.setText("WEBSITE: " + userPointer.getString(Configs.USER_WEBSITE));
        } else {
            websTxt.setText("WEBSITE: N/A");
        }


        // Show Google Maps in WebView
        WebView webView = (WebView) findViewById(R.id.mapWebView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        String locationStr = adObj.getString(Configs.CLASSIF_ADDRESS_STRING);
        String addStr = locationStr.replace(" ", "+");
        String mapStr = "https://google.com/maps/place/" + addStr;
        webView.loadUrl(mapStr);


        // MARK: - OPEN ADDRESS IN GOOGLE MAPS BUTTON
        Button openMapButt = (Button) findViewById(R.id.openMapsButt);
        final String addressStr = "http://maps.google.co.in/maps?q=" + adObj.getString(Configs.CLASSIF_ADDRESS_STRING);
        openMapButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(addressStr)));
            }
        });


        // Init EditText views for contacting the Advertiser
        final EditText messTxt = (EditText) findViewById(R.id.messageTxt);
        final EditText nameTxt = (EditText) findViewById(R.id.nameTxt);
        final EditText emailTxt = (EditText) findViewById(R.id.emailAddTxt);
        final EditText phoneTxt = (EditText) findViewById(R.id.phoneTxt);


        // MARK: - SEND MESSAGE TO ADVERTISER BUTTON
        Button sendMessButt = (Button) findViewById(R.id.sendMessButt);
        sendMessButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (messTxt.getText().toString().matches("") ||
                        nameTxt.getText().toString().matches("") ||
                        emailTxt.getText().toString().matches("")) {

                    AlertDialog.Builder alert = new AlertDialog.Builder(ShowSingleAd.this);
                    alert.setMessage("You must insert your name, a message and your email address to be contacted back from the Advertiser!")
                            .setTitle(R.string.app_name)
                            .setPositiveButton("OK", null)
                            .setIcon(R.drawable.logo);
                    alert.create().show();


                } else {
                    String strURL =
                            Configs.PATH_TO_PHP_FILE + "sendReply.php?name=" + nameTxt.getText().toString()
                                    + "&fromEmail=" + emailTxt.getText().toString()
                                    + "&tel=" + phoneTxt.getText().toString()
                                    + "&messageBody=" + messTxt.getText().toString()
                                    + "&receiverEmail=" + userPointer.getEmail().toString()
                                    + "&postTitle=" + adObj.getString(Configs.CLASSIF_TITLE);

                    strURL = strURL.replace(" ", "%20");
                    Log.d("PHP STRING: ", "\n" + strURL + "\n");


                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    try {
                        URL url;
                        url = new URL(strURL);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            InputStream is = conn.getInputStream();

                            AlertDialog.Builder alert = new AlertDialog.Builder(ShowSingleAd.this);
                            alert.setMessage("Your message has been sent!")
                                    .setTitle(R.string.app_name)
                                    .setPositiveButton("OK", null)
                                    .setIcon(R.drawable.logo);
                            alert.create().show();

                        } else {
                            InputStream err = conn.getErrorStream();

                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        });


        // MAKE A PHONE CALL TO ADVERTISER BUTTON
        final Button makeCallButt = (Button) findViewById(R.id.makeCallButt);
        makeCallButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userPointer.getString(Configs.USER_PHONE) != null) {

                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + userPointer.getString(Configs.USER_PHONE))));

                    // THE USER DOENS'T HAVE A PHONE NUMBER!
                } else {
                    AlertDialog.Builder alert = new AlertDialog.Builder(ShowSingleAd.this);
                    alert.setMessage("Sorry, " + userPointer.getString(Configs.USER_FULLNAME) + " doesn't have a phone number.")
                            .setTitle(R.string.app_name)
                            .setPositiveButton("OK", null)
                            .setIcon(R.drawable.logo);
                    alert.create().show();
                }
            }
        });

    }




    // MARK: - SHOW/HIDE IMAGE PREVIEW LAYOUT
    void showPreviewLayout() {
        LinearLayout prevLayout = (LinearLayout) findViewById(R.id.prevLayout);
        ViewGroup.MarginLayoutParams marginParams = new ViewGroup.MarginLayoutParams(prevLayout.getLayoutParams());
        marginParams.setMargins(0, 0, 0, 0);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(marginParams);
        prevLayout.setLayoutParams(layoutParams);
    }

    void hidePreviewLayout() {
        LinearLayout prevLayout = (LinearLayout) findViewById(R.id.prevLayout);
        ViewGroup.MarginLayoutParams marginParams = new ViewGroup.MarginLayoutParams(prevLayout.getLayoutParams());
        marginParams.setMargins(0, 2000, 0, 0);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(marginParams);
        prevLayout.setLayoutParams(layoutParams);
    }









    // MENU BUTTON ON ACTION BAR ----------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.singlead_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            // DEFAULT BACK BUTTON
            case android.R.id.home:
                this.finish();
                return true;


            // Report Button
            case R.id.reportButt:

                // Get User Pointer
                ParseUser userPointer = adObj.getParseUser(Configs.CLASSIF_USER_POINTER);
                try { userPointer.fetchIfNeeded();
                } catch (com.parse.ParseException e) { e.printStackTrace(); }

                Intent intent = new Intent (Intent.ACTION_VIEW , Uri.parse("mailto:" + Configs.MY_REPORT_EMAIL_ADDRESS));
                intent.putExtra(Intent.EXTRA_SUBJECT, "Reporting abusive content on Classify");
                intent.putExtra(Intent.EXTRA_TEXT,
                        Html.fromHtml(new StringBuilder()
                                .append("Hello,<br>I'm reporting this ad: <strong>" + adObj.getString(Configs.CLASSIF_TITLE)
                                        +"</strong><br>By User: <strong>" +  userPointer.getString(Configs.USER_FULLNAME)
                                + "</strong><br><br>This ad is inappropriate/abusive. Please take action against it.<br><br>Thanks,<br>Regards.")
                                .toString())
                );

                startActivity(intent);

                return true;

        }
        return (super.onOptionsItemSelected(menuItem));
    }


}//@end
