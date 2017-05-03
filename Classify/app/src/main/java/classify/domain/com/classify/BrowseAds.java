package classify.domain.com.classify;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BrowseAds extends AppCompatActivity  {


    /* Views */
    ProgressDialog pd;



    /* Variables */
    List<ParseObject> adsArray = null;
    List<String> keywords = null;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browse_ads);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Set Title of the ActionBar
        getSupportActionBar().setTitle("Browse Ads");



        // Init a ProgressDialog
        pd = new ProgressDialog(this);
        pd.setTitle(R.string.app_name);
        pd.setIndeterminate(false);



        // Get strings from Home.java
        Bundle extras = getIntent().getExtras();
        String categoryStr = extras.getString("categoryStr");
        String keywordsStr = extras.getString("keywordsStr");


        // Get an array of keywords
        if (keywordsStr != null) {
            String[] tempArr = keywordsStr.split(" ");
            keywords = Arrays.asList(tempArr);
        }



        // MARK: - CALL QUERY FOR ADS
        pd.setMessage("Loading ads ...");
        pd.show();

        ParseQuery query = ParseQuery.getQuery(Configs.CLASSIF_CLASS_NAME);

        if (keywordsStr != null) {
            query.whereContainedIn(Configs.CLASSIF_KEYWORDS, keywords);
        }

        if (categoryStr != null) {
            query.whereEqualTo(Configs.CLASSIF_CATEGORY, categoryStr);
        }

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    adsArray = objects;
                    pd.dismiss();

                    // Show Alert if there are no Ads
                    if (adsArray.size() == 0) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(BrowseAds.this);
                        alert.setMessage("No ads found.\nGo back and try a new search.")
                            .setTitle(R.string.app_name)
                            .setPositiveButton("OK", null)
                            .setIcon(R.drawable.logo);
                        alert.create().show();
                    }



                    // CUSTOM LIST ADAPTER
                    class ListAdapter extends BaseAdapter {
                        private Context context;
                        public ListAdapter(Context context, List<ParseObject> objects) {
                            super();
                            this.context = context;
                        }


                        // CONFIGURE CELL
                        @Override
                        public View getView(int position, View cell, ViewGroup parent) {
                            if (cell == null) {
                                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                cell = inflater.inflate(R.layout.ad_cell, null);
                            }
                            // Get Parse object
                            ParseObject adObj = adsArray.get(position);

                            // Get Title
                            TextView titleTxt = (TextView) cell.findViewById(R.id.adTitleTxt);
                            titleTxt.setText(adObj.getString(Configs.CLASSIF_TITLE));

                            // Get Description
                            TextView descTxt = (TextView) cell.findViewById(R.id.adDescTxt);
                            descTxt.setText(adObj.getString(Configs.CLASSIF_DESCRIPTION));


                            // Get Image
                            final ImageView adImage = (ImageView) cell.findViewById(R.id.adImage);
                            ParseFile fileObject = (ParseFile)adObj.get(Configs.CLASSIF_IMAGE1);
                            fileObject.getDataInBackground(new GetDataCallback() {
                                public void done(byte[] data, ParseException error) {
                                    if (error == null) {
                                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                        if (bmp != null) {
                                            adImage.setImageBitmap(bmp);
                                        }
                            }}});


                            return cell;
                        }

                        @Override
                        public int getCount() { return adsArray.size(); }

                        @Override
                        public Object getItem(int position) { return adsArray.get(position); }

                        @Override
                        public long getItemId(int position) { return position; }
                    }


                    // Init ListView and set its adapter
                    ListView adsList = (ListView) findViewById(R.id.adsListView);
                    adsList.setAdapter(new ListAdapter(BrowseAds.this, adsArray));
                    adsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                            // Pass adObj to the other Activity
                            ParseObject adObj = adsArray.get(position);
                            Intent i = new Intent(BrowseAds.this, ShowSingleAd.class);
                            i.putExtra("objectID", adObj.getObjectId().toString());
                            startActivity(i);
                    }});




                    // MARK: - LONG TAP TO ADD TO FAVORITE ------------------------------------------------
                    adsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                            // Get ad Pointer
                            final ParseObject adPointer = adsArray.get(pos);

                            // YOU'RE LOGGED IN -> ADD TO FAVORITES
                            if (ParseUser.getCurrentUser().getUsername() != null) {
                                pd.setMessage("Checking Favorites...");
                                pd.show();

                                // CHECK IF YOU'VE ALREADY FAVORITED THIS AD...
                                ParseUser currUser = ParseUser.getCurrentUser();

                                ParseQuery query = ParseQuery.getQuery(Configs.FAV_CLASS_NAME);
                                query.whereEqualTo(Configs.FAV_USER_POINTER, currUser);
                                query.whereEqualTo(Configs.FAV_AD_POINTER, adPointer);
                                query.findInBackground(new FindCallback<ParseObject>() {
                                    public void done(List<ParseObject> objects, ParseException error) {
                                        if (error == null) {
                                            pd.dismiss();


                                            // YOU'VE ALREADY FAVORITED THIS AD!
                                            if (objects.size() != 0) {
                                                AlertDialog.Builder alert = new AlertDialog.Builder(BrowseAds.this);
                                                alert.setMessage("You've already favorited this Ad!")
                                                        .setTitle(R.string.app_name)
                                                        .setPositiveButton("OK", null)
                                                        .setIcon(R.drawable.logo);
                                                alert.create().show();



                                                // ADD THIS AD TO FAVORITES
                                            } else {
                                                ParseObject fObj = new ParseObject(Configs.FAV_CLASS_NAME);
                                                ParseUser currUser = ParseUser.getCurrentUser();
                                                fObj.put(Configs.FAV_USER_POINTER, currUser);
                                                fObj.put(Configs.FAV_AD_POINTER, adPointer);
                                                fObj.put(Configs.FAV_USERNAME, currUser.getUsername());

                                                // Saving block
                                                fObj.saveInBackground(new SaveCallback() {
                                                    @Override
                                                    public void done(ParseException error) {
                                                        if (error == null) {
                                                            pd.dismiss();

                                                            AlertDialog.Builder alert = new AlertDialog.Builder(BrowseAds.this);
                                                            alert.setMessage("You've added this ad to your Favorites!")
                                                                    .setTitle(R.string.app_name)
                                                                    .setIcon(R.drawable.logo)
                                                                    .setPositiveButton("OK", null);
                                                            alert.create().show();

                                                        // error
                                                        } else {
                                                            Toast.makeText(getApplicationContext(), error.getMessage().toString(), Toast.LENGTH_SHORT).show();
                                                            pd.dismiss();
                                                }}});
                                            }


                                        // Error in query
                                        } else {
                                            Toast.makeText(getApplicationContext(), error.getMessage().toString(), Toast.LENGTH_LONG).show();
                                            pd.dismiss();
                                }}});





                            // YOU'RE NOT LOGGED IN -> NO FAVORITES
                            } else {
                                AlertDialog.Builder alert = new AlertDialog.Builder(BrowseAds.this);
                                alert.setMessage("You must be logged in to add ads to Favorites!\nTap Account and login or sign up.")
                                        .setTitle(R.string.app_name)
                                        .setPositiveButton("OK", null)
                                        .setIcon(R.drawable.logo);
                                alert.create().show();
                            }


                        return  true; }});


                // Error in query
                } else {
                    Toast.makeText(getApplicationContext(), error.getMessage().toString(), Toast.LENGTH_LONG).show();
        }}});




        // Init AdMob banner
        AdView mAdView = (AdView) findViewById(R.id.admobBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


    }// end onCreate()














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
