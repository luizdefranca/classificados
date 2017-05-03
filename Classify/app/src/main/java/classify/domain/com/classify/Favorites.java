package classify.domain.com.classify;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
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
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class Favorites extends AppCompatActivity {

    /* Views */
    ProgressDialog pd;


    /* Variables */
    ParseUser currUser = ParseUser.getCurrentUser();
    List<ParseObject> favArray;




    // ON START() --------------------------------------------------------
    @Override
    protected void onStart() {
        super.onStart();

        if (currUser.getUsername() != null) {
            queryFavorites();

        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(Favorites.this);
            alert.setMessage("You must be logged in to see/edit Favorites.\nTap Account and login/sign up.")
            .setTitle(R.string.app_name)
            .setPositiveButton("OK", null)
            .setIcon(R.drawable.logo);
            alert.create().show();
        }
    }





    // ON CREATE() ---------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorites);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set Title of the ActionBar
        getSupportActionBar().setTitle("Favorites");


        // Init a ProgressDialog
        pd = new ProgressDialog(this);
        pd.setTitle(R.string.app_name);
        pd.setIndeterminate(false);


        // Init TabBar buttons
        Button tab_home = (Button)findViewById(R.id.tab_home);
        Button tab_account = (Button)findViewById(R.id.tab_account);

        tab_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Favorites.this, Home.class));
            }});

        tab_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ParseUser.getCurrentUser().getUsername() != null) {
                     startActivity(new Intent(Favorites.this, AccountScreen.class));
                } else {
                    startActivity(new Intent(Favorites.this, Login.class));
                }
        }});



        // Init AdMob banner
        AdView mAdView = (AdView) findViewById(R.id.admobBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


    }//end onCreate()





    // MARK: - SHOW FAVORITES ------------------------------------------------------
    void queryFavorites() {
        pd.setMessage("Loading Favorites...");
        pd.show();

        ParseQuery query = ParseQuery.getQuery(Configs.FAV_CLASS_NAME);
        query.whereEqualTo(Configs.FAV_USER_POINTER, currUser);
        query.include(Configs.CLASSIF_CLASS_NAME);

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    favArray = objects;
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
                            ParseObject favObj = favArray.get(position);

                            // Get adPointer
                            final View finalCell = cell;

                            favObj.getParseObject(Configs.FAV_AD_POINTER).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                                public void done(ParseObject adPointer, ParseException e) {

                                    // Get Title
                                    TextView titleTxt = (TextView) finalCell.findViewById(R.id.adTitleTxt);
                                    titleTxt.setText(adPointer.getString(Configs.CLASSIF_TITLE));

                                    // Get Description
                                    TextView descTxt = (TextView) finalCell.findViewById(R.id.adDescTxt);
                                    descTxt.setText(adPointer.getString(Configs.CLASSIF_DESCRIPTION));


                                    // Get Image
                                    final ImageView adImage = (ImageView) finalCell.findViewById(R.id.adImage);
                                    ParseFile fileObject = (ParseFile)adPointer.get(Configs.CLASSIF_IMAGE1);
                                    fileObject.getDataInBackground(new GetDataCallback() {
                                        public void done(byte[] data, ParseException error) {
                                            if (error == null) {
                                                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                                if (bmp != null) {
                                                    adImage.setImageBitmap(bmp);
                                                }
                                    }}});
                            }});





                            return cell;
                        }

                        @Override
                        public int getCount() { return favArray.size(); }

                        @Override
                        public Object getItem(int position) { return favArray.get(position); }

                        @Override
                        public long getItemId(int position) { return position; }
                    }


                    // Init ListView and set its adapter
                    ListView storesList = (ListView) findViewById(R.id.favListView);
                    storesList.setAdapter(new ListAdapter(Favorites.this, favArray));
                    storesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                            ParseObject favObj = favArray.get(position);

                            // Get adPointer
                            favObj.getParseObject(Configs.FAV_AD_POINTER).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                                public void done(ParseObject adPointer, ParseException e) {

                                    // Pass adPointer to the other Activity
                                    Intent i = new Intent(Favorites.this, ShowSingleAd.class);
                                    i.putExtra("objectID", adPointer.getObjectId().toString());
                                    startActivity(i);
                            }});
                    }});



                    // MARK: - LONG TAP TO REMOVE A FAVORITE
                    ListView favListView = (ListView)findViewById(R.id.favListView);
                    favListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {

                            // Ask to remove selected Favorite
                            final ParseObject favObj = favArray.get(pos);

                            AlertDialog.Builder alert = new AlertDialog.Builder(Favorites.this);
                            alert.setMessage("Are you sure you want to remove this ad from your Favorites?")
                                .setTitle(R.string.app_name)
                                .setPositiveButton("Cancel", null)
                                .setNegativeButton("Remove", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        favObj.deleteInBackground(new DeleteCallback() {
                                            @Override
                                            public void done(ParseException error) {
                                                if (error == null) {
                                                    // Recall query
                                                    queryFavorites();
                                        }}});
                                    }})
                                .setIcon(R.drawable.logo);
                            alert.create().show();

                            return true;
                    }});


                // Error in query
                } else {
                    Toast.makeText(getApplicationContext(), error.getMessage().toString(), Toast.LENGTH_LONG).show();
        }}});

    }





}//@end
