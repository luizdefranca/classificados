package classify.domain.com.classify;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class MyAds extends AppCompatActivity {

    /* Views */
    ProgressDialog pd;



    /* Variables */
    List<ParseObject> myAdsArray;
    List<ParseObject> favArray;
    ParseUser currUser = ParseUser.getCurrentUser();


    @Override
    protected void onStart() {
        super.onStart();

        //Call query
        queryMyAds();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_ads);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Set Title of the ActionBar
        getSupportActionBar().setTitle("My Ads");


        // Init a ProgressDialog
        pd = new ProgressDialog(this);
        pd.setTitle(R.string.app_name);
        pd.setIndeterminate(false);


        // Init AdMob banner
        AdView mAdView = (AdView) findViewById(R.id.admobBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);



    }//end onCreate()




    // MARK: - QUERY MY ADS
    void queryMyAds() {
        pd.setMessage("Loading My Ads...");
        pd.show();

        ParseQuery query = ParseQuery.getQuery(Configs.CLASSIF_CLASS_NAME);
        query.whereEqualTo(Configs.CLASSIF_USER_POINTER, currUser);
        query.orderByDescending(Configs.CLASSIF_UPDATED_AT);

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    myAdsArray = objects;
                    pd.dismiss();

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
                            ParseObject adObj = myAdsArray.get(position);


                            // Get Title
                            TextView titleTxt = (TextView) cell.findViewById(R.id.adTitleTxt);
                            titleTxt.setText(adObj.getString(Configs.CLASSIF_TITLE));

                            // Get Description
                            TextView descTxt = (TextView) cell.findViewById(R.id.adDescTxt);
                            descTxt.setText(adObj.getString(Configs.CLASSIF_DESCRIPTION));


                            // Get Image
                            final ImageView adImage = (ImageView) cell.findViewById(R.id.adImage);
                            ParseFile fileObject = (ParseFile) adObj.get(Configs.CLASSIF_IMAGE1);
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
                        public int getCount() { return myAdsArray.size(); }

                        @Override
                        public Object getItem(int position) { return myAdsArray.get(position); }

                        @Override
                        public long getItemId(int position) { return position; }
                    }


                    // Init ListView and set its adapter
                    ListView storesList = (ListView) findViewById(R.id.myAdsListView);
                    storesList.setAdapter(new ListAdapter(MyAds.this, myAdsArray));
                    storesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                            ParseObject adObj = myAdsArray.get(position);

                            // Pass adPointer to the other Activity
                            Intent i = new Intent(MyAds.this, PostAd.class);
                            i.putExtra("objectID", adObj.getObjectId().toString());
                            startActivity(i);
                    }});



                    // MARK: - LONG TAP TO REMOVE A FAVORITE
                    ListView myAdsListView = (ListView)findViewById(R.id.myAdsListView);
                    myAdsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {

                            // Ask to remove selected Ad
                            final ParseObject adObj = myAdsArray.get(pos);


                            AlertDialog.Builder alert = new AlertDialog.Builder(MyAds.this);
                            alert.setMessage("Are you sure you want to remove this ad?")
                                .setTitle(R.string.app_name)
                                .setPositiveButton("Cancel", null)
                                .setNegativeButton("Remove", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

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


                                        // Delete the Ad
                                        adObj.deleteInBackground(new DeleteCallback() {
                                            @Override
                                            public void done(ParseException error) {
                                                if (error == null) {

                                                    AlertDialog.Builder alert = new AlertDialog.Builder(MyAds.this);
                                                    alert.setMessage("Ad successfully deleted!")
                                                            .setTitle(R.string.app_name)
                                                            .setPositiveButton("OK", null)
                                                            .setIcon(R.drawable.logo);
                                                    alert.create().show();

                                                    // Recall query
                                                    queryMyAds();
                                                }}});

                                    }})
                                .setIcon(R.drawable.logo);
                            alert.create().show();




                            return true;  }});



                    // Error in query
                } else {
                    Toast.makeText(getApplicationContext(), error.getMessage().toString(), Toast.LENGTH_LONG).show();
        }}});


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
