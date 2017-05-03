package classify.domain.com.classify;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class PostAd extends AppCompatActivity implements LocationListener {

    /* Views */
    ProgressDialog pd;
    EditText titleTxt;
    TextView categoryTxt;
    EditText priceTxt;
    EditText descTxt;
    EditText addressTxt;
    ImageView img1;
    ImageView img2;
    ImageView img3;
    Button locationButt;
    Button postUpdateButt;
    Button deleteAdButt;
    ListView catListView;


    /* Variables */
    ParseUser currUser = ParseUser.getCurrentUser();
    ParseObject adObj;
    Location currentLocation;
    LocationManager locationManager;
    MarshMallowPermission mmp = new MarshMallowPermission(this);
    int imageUploaded;
    List<ParseObject> favArray;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_ad);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        // Init a ProgressDialog
        pd = new ProgressDialog(this);
        pd.setTitle(R.string.app_name);
        pd.setIndeterminate(false);


        // Init Views
        titleTxt = (EditText)findViewById(R.id.pTitleTxt);
        categoryTxt = (TextView) findViewById(R.id.pCatTxt);
        priceTxt = (EditText)findViewById(R.id.pPriceTxt);
        descTxt = (EditText)findViewById(R.id.pDescriptionTxt);
        addressTxt = (EditText)findViewById(R.id.pAddressTxt);
        locationButt = (Button)findViewById(R.id.pLocationButt);
        img1 = (ImageView) findViewById(R.id.pImg1);
        img2 = (ImageView) findViewById(R.id.pImg2);
        img3 = (ImageView) findViewById(R.id.pImg3);
        postUpdateButt = (Button)findViewById(R.id.pPostUpdateAdButt);
        deleteAdButt = (Button)findViewById(R.id.pDeleteAdButt);
        catListView = (ListView)findViewById(R.id.pCatListView);

        // Init the Categories ListView
        initCatListView();
        imageUploaded = 0;


        // Get objectID from previous .java
        Intent intent = getIntent();
        String objectID = intent.getStringExtra("objectID");
        adObj = ParseObject.createWithoutData(Configs.CLASSIF_CLASS_NAME, objectID);
        try { adObj.fetchIfNeeded().getParseObject(Configs.CLASSIF_CLASS_NAME);
        } catch (com.parse.ParseException e) { e.printStackTrace(); }


        // UPDATE THIS AD
        if (adObj.getObjectId() != null) {
            getSupportActionBar().setTitle("Edit your ad");

            showAdDetails();
            postUpdateButt.setText("Update this ad");

        // POST A NEW AD
        } else {
            getSupportActionBar().setTitle("Post a new ad");

            postUpdateButt.setText("Post ad");
            deleteAdButt.setVisibility(View.INVISIBLE);
        }






        // MARK: - SET CURRENT LOCATION AD BUTTON
        locationButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mmp.checkPermissionForLocation()) {
                    mmp.requestPermissionForLocation();
                } else {
                    getCurrentLocation();
                }
        }});




        // MARK: - POST/UPDATE AD BUTTON
        postUpdateButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // POST A NEW AD ------------------------------------------------------------------
                if (adObj.getObjectId() == null) {

                    // Create ParseObject
                    adObj = new ParseObject(Configs.CLASSIF_CLASS_NAME);
                    saveAdToParse("You've successfully posted an ad!");


                // UPDATE AD ----------------------------------------------------------------------
                } else {
                    saveAdToParse("Ad successfully updated!");
                }

            }});






        // MARK: - DELETE AD BUTTON -----------------------------------------------
        deleteAdButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd.setMessage("Deleting ad...");
                pd.show();

                //First, query all the Favorites of this ad to remove them (if any)
                ParseQuery query = ParseQuery.getQuery(Configs.FAV_CLASS_NAME);
                query.whereEqualTo(Configs.FAV_AD_POINTER, adObj);
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> objects, ParseException error) {
                        if (error == null) {
                            favArray = objects;

                            if (favArray.size() != 0) {
                                for (int i = 0; i < favArray.size(); i++) {
                                    // Remove all Favorites
                                    ParseObject favObj = favArray.get(i);
                                    favObj.deleteInBackground();
                                }
                            }
                }}});


                // Now delete the Ad
                adObj.deleteInBackground(new DeleteCallback() {
                    @Override
                    public void done(ParseException error) {
                        if (error == null) {
                            pd.dismiss();

                             AlertDialog.Builder alert  = new AlertDialog.Builder(PostAd.this);
                            alert.setTitle("SELECT SOURCE")
                                    .setIcon(R.drawable.logo)
                                    .setItems(new CharSequence[]
                                            {"OK"},
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    switch (which) {
                                                        case 0:
                                                            finish();
                                                            break;
                                                    }
                            }});
                            alert.create().show();


                        // error
                        } else {
                            Toast.makeText(getApplicationContext(), error.getMessage().toString(), Toast.LENGTH_LONG).show();
                            pd.dismiss();
                }}});

            }});




        // MARK: - UPLOAD IMAGES
        img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageUploaded = 1;
                openAlertForImages();
            }});

        img2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageUploaded = 2;
                openAlertForImages();
            }});

        img3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageUploaded = 3;
                openAlertForImages();
            }});

    }//end onCreate()







    // MARK: - GET CURRENT LOCATION ---------------------------------------------------------------
    protected void getCurrentLocation() {
        pd.setMessage("Getting Address from your current location...");
        pd.show();
        dismisskeyboard();


        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_LOW);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(criteria, true);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        currentLocation = locationManager.getLastKnownLocation(provider);

        if (currentLocation != null) {

            showCurrentAddress();

        } else { locationManager.requestLocationUpdates(provider, 1000, 0, this); }
    }


    @Override
    public void onLocationChanged(Location location) {
        //remove location callback:
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates(this);
        currentLocation = location;

        if (currentLocation != null) {

            showCurrentAddress();

        // NO GPS location found!
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(PostAd.this);
            alert.setMessage("Failed to get your Location.\nGo into Settings and make sure Location Service is enabled")
                    .setTitle(R.string.app_name)
                    .setPositiveButton("OK", null)
                    .setIcon(R.drawable.logo);
            alert.create().show();
        }

    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}




    // MARK: - SHOW CURRENT ADDRESS IN addressTxt
    void showCurrentAddress() {
        try {
            Geocoder geocoder = new Geocoder(PostAd.this, Locale.getDefault());
            List<Address> addresses = null;

            addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
            if (geocoder.isPresent()) {
                Address returnAddress = addresses.get(0);
                String address = returnAddress.getAddressLine(0);
                String city = returnAddress.getLocality();
                String country = returnAddress.getCountryName();
                String zipCode = returnAddress.getPostalCode();

                // Show Address
                addressTxt.setText(address + " " + city + " " + country + " " + zipCode);
                pd.dismiss();

            } else {
                Toast.makeText(getApplicationContext(), "Geocoder not present!", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), e.getMessage().toString(), Toast.LENGTH_SHORT).show();
            pd.dismiss();
        }
    }



    // MARK: - OPEN ALERT FOR UPLOADING IMAGES --------------------------------------------------------
    void openAlertForImages() {
        AlertDialog.Builder alert  = new AlertDialog.Builder(PostAd.this);
        alert.setTitle("SELECT SOURCE")
                .setIcon(R.drawable.logo)
                .setItems(new CharSequence[]
                                {"Take a picture", "Pick from Gallery" },
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    // Open Camera
                                    case 0:
                                        if (!mmp.checkPermissionForCamera()) {
                                            mmp.requestPermissionForCamera();
                                        } else {
                                            openCamera();
                                        }
                                        break;

                                    // Open Gallery
                                    case 1:
                                        if (!mmp.checkPermissionForReadExternalStorage()) {
                                            mmp.requestPermissionForReadExternalStorage();
                                        } else {
                                            openGallery();
                                        }
                                        break;
                                }
                            }});
        alert.create().show();
    }








    // MARK: - SHOW AD DETAILS (IN CASE OF UPDATE) --------------------------------------------------------
    void showAdDetails() {
        titleTxt.setText(adObj.getString(Configs.CLASSIF_TITLE));
        categoryTxt.setText(adObj.getString(Configs.CLASSIF_CATEGORY));
        priceTxt.setText(adObj.getString(Configs.CLASSIF_PRICE));
        descTxt.setText(adObj.getString(Configs.CLASSIF_DESCRIPTION));
        addressTxt.setText(adObj.getString(Configs.CLASSIF_ADDRESS_STRING));


        // Get Image1
        ParseFile fileObject = (ParseFile)adObj.get(Configs.CLASSIF_IMAGE1);
        if (fileObject != null ) {
            fileObject.getDataInBackground(new GetDataCallback() {
                public void done(byte[] data, ParseException error) {
                    if (error == null) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        if (bmp != null) {
                            img1.setImageBitmap(bmp);
            }}}});
        }

        // Get Image2
        ParseFile fileObject2 = (ParseFile)adObj.get(Configs.CLASSIF_IMAGE2);
        if (fileObject2 != null ) {
            fileObject2.getDataInBackground(new GetDataCallback() {
                public void done(byte[] data, ParseException error) {
                    if (error == null) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        if (bmp != null) {
                            img2.setImageBitmap(bmp);
                        }}}});
        }

        // Get Image3
        ParseFile fileObject3 = (ParseFile)adObj.get(Configs.CLASSIF_IMAGE3);
        if (fileObject3 != null ) {
            fileObject3.getDataInBackground(new GetDataCallback() {
                public void done(byte[] data, ParseException error) {
                    if (error == null) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        if (bmp != null) {
                            img3.setImageBitmap(bmp);
                        }}}});
        }

    }









    // MARK: - SAVE AD TO PARSE ----------------------------------------------------------------------
    void saveAdToParse(final String withMessage) {

        // Fill all fields to post and ad!
        if (    titleTxt.getText().toString().matches("") ||
                categoryTxt.getText().toString().matches("Select a category below!") ||
                priceTxt.getText().toString().matches("") ||
                descTxt.getText().toString().matches("") ||
                addressTxt.getText().toString().matches("") ||
                img1.getDrawable() == null) {


            AlertDialog.Builder alert = new AlertDialog.Builder(PostAd.this);
            alert.setMessage("You must fill all the fields and add at least 1 image to post an ad!")
                .setTitle(R.string.app_name)
                .setPositiveButton("OK", null)
                .setIcon(R.drawable.logo);
            alert.create().show();



        // SAVE DATA
        } else {

            pd.setMessage("Saving Ad...");
            pd.show();

            adObj.put(Configs.CLASSIF_USER_POINTER, currUser);
            adObj.put(Configs.CLASSIF_TITLE, titleTxt.getText().toString());
            adObj.put(Configs.CLASSIF_CATEGORY, categoryTxt.getText().toString());
            adObj.put(Configs.CLASSIF_PRICE, priceTxt.getText().toString());
            adObj.put(Configs.CLASSIF_DESCRIPTION, descTxt.getText().toString());
            adObj.put(Configs.CLASSIF_DESCRIPTION_LOWERCASE, descTxt.getText().toString().toLowerCase());
            adObj.put(Configs.CLASSIF_ADDRESS_STRING, addressTxt.getText().toString());


            // Add keywords
            List<String> keywords = new ArrayList<String>();
            String[] one = titleTxt.getText().toString().toLowerCase().split(" ");
            String[] two = descTxt.getText().toString().toLowerCase().split(" ");
            String[] three = addressTxt.getText().toString().toLowerCase().split(" ");
            for (String keyw : one) {
                keywords.add(keyw);
            }
            for (String keyw : two) {
                keywords.add(keyw);
            }
            for (String keyw : three) {
                keywords.add(keyw);
            }
            Log.d("KEYWORDS", "\n" + keywords + "\n");

            adObj.put(Configs.CLASSIF_KEYWORDS, keywords);




            // Save image1
            ImageView pImg1 = (ImageView) findViewById(R.id.pImg1);
            Bitmap bitmap = ((BitmapDrawable) pImg1.getDrawable()).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            ParseFile imageFile = new ParseFile("image.jpg", byteArray);
            adObj.put(Configs.CLASSIF_IMAGE1, imageFile);

            // Save image2
            if (img2.getDrawable() != null) {
                ImageView pImg2 = (ImageView) findViewById(R.id.pImg2);
                Bitmap bitmap2 = ((BitmapDrawable) pImg2.getDrawable()).getBitmap();
                ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
                bitmap2.compress(Bitmap.CompressFormat.JPEG, 100, stream2);
                byte[] byteArray2 = stream2.toByteArray();
                ParseFile imageFile2 = new ParseFile("image.jpg", byteArray2);
                adObj.put(Configs.CLASSIF_IMAGE2, imageFile2);
            }

            // Save image3
            if (img3.getDrawable() != null) {
                ImageView pImg3 = (ImageView) findViewById(R.id.pImg3);
                Bitmap bitmap3 = ((BitmapDrawable) pImg3.getDrawable()).getBitmap();
                ByteArrayOutputStream stream3 = new ByteArrayOutputStream();
                bitmap3.compress(Bitmap.CompressFormat.JPEG, 100, stream3);
                byte[] byteArray3 = stream3.toByteArray();
                ParseFile imageFile3 = new ParseFile("image.jpg", byteArray3);
                adObj.put(Configs.CLASSIF_IMAGE3, imageFile3);
            }


            // Saving block
            adObj.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException error) {
                    if (error == null) {
                        pd.dismiss();

                         AlertDialog.Builder alert  = new AlertDialog.Builder(PostAd.this);
                                         alert.setTitle(withMessage)
                                                 .setIcon(R.drawable.logo)
                                                 .setItems(new CharSequence[]
                                                         {"Ok" },
                                                 new DialogInterface.OnClickListener() {
                                                     public void onClick(DialogInterface dialog, int which) {
                                                         switch (which) {
                                                             // Dismiss Activity
                                                             case 0:
                                                                finish();
                                                                 break;


                                                 }}});
                                         alert.create().show();

                    // error
                    } else {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        pd.dismiss();
            }}});

        }

    }







    // MARK: - INIT CATEGORIES LIST VIEW
    void initCatListView() {

        // Make ListView scroll even if it's inside a ScrollView
        catListView.setOnTouchListener(new ListView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }

                // Handle ListView touch events.
                v.onTouchEvent(event);
                return true;
            }
        });


        // CUSTOM LIST ADAPTER
        class ListAdapter extends BaseAdapter {
            private Context context;

            public ListAdapter(Context context, String[] objects) {
                super();
                this.context = context;
            }

            // CONFIGURE CELL
            @Override
            public View getView(int position, View cell, ViewGroup parent) {
                if (cell == null) {
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    cell = inflater.inflate(R.layout.pa_cat_cell, null);
                }

                String catName = Configs.categoriesArray[position];

                // Get Category name
                TextView cCatTxt = (TextView) cell.findViewById(R.id.paCatCell);
                cCatTxt.setText(catName);

                return cell;
            }

            @Override
            public int getCount() { return Configs.categoriesArray.length; }

            @Override
            public Object getItem(int position) { return Configs.categoriesArray[position]; }

            @Override
            public long getItemId(int position) { return position; }
        }


        // Init ListView and set its adapter
        catListView.setAdapter(new ListAdapter(PostAd.this, Configs.categoriesArray));
        catListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                categoryTxt.setText(Configs.categoriesArray[position].toString());
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

            // Set images
            if (imageUploaded == 1) { img1.setImageBitmap(bm);
            } else if (imageUploaded == 2) { img2.setImageBitmap(bm);
            } else if (imageUploaded == 3) { img3.setImageBitmap(bm); }

        }
    }
    //---------------------------------------------------------------------------------------------








    // DISMISS KEYBOARD
    public void dismisskeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(titleTxt.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(categoryTxt.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(priceTxt.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(descTxt.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(addressTxt.getWindowToken(), 0);
    }




    // MENU BUTTONS --------------------------------------------
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
