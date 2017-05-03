package classify.domain.com.classify;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.security.AccessController.getContext;

public class Home extends AppCompatActivity  {



    /* Views */
    EditText keywTxt;





    @Override
    protected void onStart() {
        super.onStart();
        keywTxt.setText("");
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);



        // Init TabBar buttons
        Button tab_fav = (Button)findViewById(R.id.tab_fav);
        Button tab_account = (Button)findViewById(R.id.tab_account);

        tab_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Home.this, Favorites.class));
        }});

        tab_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ParseUser.getCurrentUser().getUsername() != null) {
                    startActivity(new Intent(Home.this, AccountScreen.class));
                } else {
                    startActivity(new Intent(Home.this, Login.class));
                }
        }});


        // Init views
        keywTxt = (EditText) findViewById(R.id.keywordTxt);




        // MARK: - SEARCH BY KEYWORDS ------------------------------------------
        keywTxt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    dismisskeyboard();

                    // Pass strings for query to BrowseAds.java
                    Intent i = new Intent(Home.this, BrowseAds.class);
                    Bundle extras = new Bundle();
                    extras.putString("keywordsStr", keywTxt.getText().toString().toLowerCase());
                    i.putExtras(extras);
                    startActivity(i);

                    return true;
                }
                return false;
        }});




        // Init the Categories GridView
        initCatGridView();



    }// end onCreate()







    // MARK: - SHOW CATEGORIES
    void initCatGridView() {
        final List<String> catArray = new ArrayList<String>(Arrays.asList(Configs.categoriesArray));


        // CUSTOM GRID ADAPTER
        class GridAdapter extends BaseAdapter {
            private Context context;
            public GridAdapter(Context context, List<String> objects) {
                super();
                this.context = context;
            }


            // CONFIGURE CELL
            @Override
            public View getView(int position, View cell, ViewGroup parent) {
                if (cell == null) {
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    cell = inflater.inflate(R.layout.cat_cell, null);
                }

                // Get category image
                ImageView catImg = (ImageView)cell.findViewById(R.id.chCatImage);

                String catName = catArray.get(position);
                String resName = "";

                if (catName.contains(" ")) {
                    String[] separated = catName.toLowerCase().split(" ");
                    separated[0].toString();
                    separated[1].toString();
                    resName = separated[0] + "_" + separated[1];
                } else {
                    resName = catName.toLowerCase();
                }
                int resID = getResources().getIdentifier(resName, "drawable", getPackageName());
                catImg.setImageResource(resID);


                // Get category's name
                TextView catTitleTxt = (TextView)cell.findViewById(R.id.chCatTitleTxt);
                catTitleTxt.setText(catName);


            return cell;
            }

            @Override
            public int getCount() { return catArray.size(); }
            @Override
            public Object getItem(int position) { return catArray.get(position); }
             @Override
             public long getItemId(int position) { return position; }
        }


        // Init GridView and set its adapter
        GridView aGrid = (GridView) findViewById(R.id.catGridView);
        aGrid.setAdapter(new GridAdapter(Home.this, catArray));

        // Set number of Columns accordingly to the device used
        float scalefactor = getResources().getDisplayMetrics().density * 120; // 120 is the cell's width
        int number = getWindowManager().getDefaultDisplay().getWidth();
        int columns = (int) ((float) number / (float) scalefactor);
        aGrid.setNumColumns(columns);

        aGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                String cat = catArray.get(position);

                Intent i = new Intent(Home.this, BrowseAds.class);
                Bundle extras = new Bundle();
                extras.putString("categoryStr", cat);
                i.putExtras(extras);
                startActivity(i);
        }});

    }





    // MARK: - DISMISS KEYBOARD
    void dismisskeyboard() {
       InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
       imm.hideSoftInputFromWindow(keywTxt.getWindowToken(), 0);
    }






    // MENU BUTTON ON ACTION BAR ----------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            // POST A NEW AD BUTTON
            case R.id.postAdButt:

                if (ParseUser.getCurrentUser().getUsername() != null) {
                     startActivity(new Intent(Home.this, PostAd.class));
                } else {
                    AlertDialog.Builder alert = new AlertDialog.Builder(Home.this);
                    alert.setMessage("You must be logged in to post Ads!\nTap Account and login or sign up.")
                        .setTitle(R.string.app_name)
                        .setPositiveButton("OK", null)
                        .setIcon(R.drawable.logo);
                    alert.create().show();
                }
                return true;



            // TERMS OF USE BUTTON
            case R.id.touButt:
                // Go to Terms of Use
                startActivity(new Intent(Home.this, TermsOfUse.class));
                return true;
        }

        return (super.onOptionsItemSelected(menuItem));
    }



}//@end
