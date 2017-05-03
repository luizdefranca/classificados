package classify.domain.com.classify;


import android.app.Application;
import android.content.Context;
import android.support.v7.app.AlertDialog;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import java.lang.reflect.Array;
import java.util.List;


public class Configs extends  Application {


    // ARRAY OF CATEGORIES FOR HOME SCREEN
    public static String[] categoriesArray = new String[] {
            "Jobs",
            "Real Estate",
            "Services",
            "Electronics",
            "Vehicles",
            "Shopping",
            "Community",
            "Pets",
            "Free stuff"

            // You can add more Categories here....
    };




    // IMPORTANT: Change the red string below with the path where you've stored the sendReply.php file (in this case we've stored it into a directory in our website called "classify")
    public static String PATH_TO_PHP_FILE = "http://fvimagination.com/classify/";

    // IMPORTANT: You must replace the red email address below with the one you'll dedicate to Report emails from Users, in order to also agree with EULA Terms (Required by Apple)
    public static String MY_REPORT_EMAIL_ADDRESS = "report@example.com";



    // PARSE KEYS ----------------------------------------------------------------------------
    public static String PARSE_APP_KEY = "QHHo02vFWw7QbArlVvh8S435O0QLcOz1kJD5s3D4";
    public static String PARSE_CLIENT_KEY = "o0Du2CdgBhkOQBdf28IS60PLvFObBLEI2Y3zbPhh";





    /************** DO NOT EDIT THE CODE BELOW! **************/

    /* USER CLASS */
    public static String USER_CLASS_NAME = "User";
    public static String USER_USERNAME = "username";
    public static String USER_FULLNAME = "fullName";
    public static String USER_PHONE = "phone";
    public static String USER_EMAIL = "email";
    public static String USER_WEBSITE = "website";
    public static String USER_AVATAR = "avatar";

    /* CLASSIFIEDS CLASS */
    public static String CLASSIF_CLASS_NAME = "Classifieds";
    public static String CLASSIF_USER_POINTER = "user"; // User Pointer
    public static String CLASSIF_TITLE = "title";
    public static String CLASSIF_CATEGORY = "category";
    public static String CLASSIF_ADDRESS = "address"; // GeoPoint
    public static String CLASSIF_ADDRESS_STRING = "addressString";
    public static String CLASSIF_PRICE = "price";
    public static String CLASSIF_DESCRIPTION = "description";
    public static String CLASSIF_DESCRIPTION_LOWERCASE = "descriptionLowercase";
    public static String CLASSIF_KEYWORDS = "keywords";
    public static String CLASSIF_IMAGE1 = "image1"; // File
    public static String CLASSIF_IMAGE2 = "image2"; // File
    public static String CLASSIF_IMAGE3 = "image3"; // File
    public static String CLASSIF_CREATED_AT = "createdAt";
    public static String CLASSIF_UPDATED_AT = "updatedAt";

    /* FAVORITES CLASS */
    public static String FAV_CLASS_NAME = "Favorites";
    public static String FAV_USERNAME = "username";
    public static String FAV_USER_POINTER = "userPointer"; // Pointer
    public static String FAV_AD_POINTER = "adPointer"; // Pointer




    boolean isParseInitialized = false;

    public void onCreate() {
        super.onCreate();

        if (isParseInitialized == false) {
            Parse.initialize(new Parse.Configuration.Builder(this)
                    .applicationId(String.valueOf(PARSE_APP_KEY))
                    .clientKey(String.valueOf(PARSE_CLIENT_KEY))
                    .server("https://parseapi.back4app.com")
                    .build()
            );
            Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);
            ParseUser.enableAutomaticUser();
            isParseInitialized = true;

            // Init Facebook Utils
            ParseFacebookUtils.initialize(this);
        }

    }// end onCreate()



} //@end

