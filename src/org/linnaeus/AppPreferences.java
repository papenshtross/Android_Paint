package org.linnaeus;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by IntelliJ IDEA.
 * User: Immortality
 * Date: 08.11.2010
 * Time: 18:47:38
 */

public class AppPreferences {

    public static final String PREFERENCES_NAME = "Preferences";

    private Context _context;

    private SharedPreferences _preferences;

    private AppPreferences(Context context){
        _context = context;
        _preferences = _context.getSharedPreferences(PREFERENCES_NAME, 0);
    }

    public static AppPreferences getAppPreferences(Context context){
        return new AppPreferences(context);
    }

    public Boolean isShakeFeatureEnabled(){
        return _preferences.getBoolean("shakeFeatureEnabled", false);
    }

    public void setShakeFeatureEnabled(Boolean value){
        SharedPreferences.Editor editor = _preferences.edit();
        editor.putBoolean("shakeFeatureEnabled", value);
        editor.commit();
    }

    public Boolean isSaveStateOnExit(){
        return _preferences.getBoolean("saveStateOnExit", false);
    }

    public void setSaveStateOnExit(Boolean value){
        SharedPreferences.Editor editor = _preferences.edit();
        editor.putBoolean("saveStateOnExit", value);
        editor.commit();
    }
}
