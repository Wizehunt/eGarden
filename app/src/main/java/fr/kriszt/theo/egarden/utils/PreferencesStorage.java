package fr.kriszt.theo.egarden.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import fr.kriszt.theo.egarden.R;

/**
 * Created by wizehunt on 12/03/18.
 */

public class PreferencesStorage {

    private SharedPreferences sharedPref;
    private Context context;
    private static PreferencesStorage O = null;

    private PreferencesStorage(Activity activity){
        context = activity.getApplicationContext();

        String creds = String.valueOf(R.string.credentials_preference_file);
        sharedPref = context.getSharedPreferences(creds, Context.MODE_PRIVATE);
//        SharedPreferences sharedPref = PreferenceManager
//                .getDefaultSharedPreferences(this);

        if (sharedPref == null)
            Toast.makeText(context, "sharedPref is NULL !", Toast.LENGTH_LONG).show();
    }

    public static PreferencesStorage O(Activity activity){
        if (O == null){
            O = new PreferencesStorage(activity);
        }
        return O;
    }

    public static String getSavedPort() {
        return O.sharedPref.getString(String.valueOf(R.string.saved_port_key), "1880");

    }

    public static String getSavedIP() {
        return O.sharedPref.getString(String.valueOf(R.string.saved_ip_key), "192.168.1.12");

    }

    public String getSavedUsername() {
        return O.sharedPref.getString(String.valueOf(R.string.saved_username_key), "userTest");
    }

    public static String getSavedPassword() {
        return O.sharedPref.getString(String.valueOf(R.string.saved_password_key), "secretPassword");
    }

    public static void savePreferences(String port, String ip, String username, String password) {

        SharedPreferences.Editor prefsEditor = O.sharedPref.edit();

        prefsEditor.putString   (String.valueOf((R.string.saved_port_key)), port);
        prefsEditor.putString(String.valueOf((R.string.saved_ip_key)), ip);
        prefsEditor.putString(String.valueOf((R.string.saved_username_key)), username);
        prefsEditor.putString(String.valueOf((R.string.saved_password_key)), password);

        prefsEditor.apply();

    }
}