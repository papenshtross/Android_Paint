package org.linnaeus.activity;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import org.linnaeus.AppPreferences;

/**
 * Created by IntelliJ IDEA.
 * User: Immortality
 * Date: 07.11.2010
 * Time: 23:17:09
 */

public class PreferencesActivity extends PreferenceActivity {

    private CheckBoxPreference _saveStatePref;
    private CheckBoxPreference _shakeFeaturePref;
    private AppPreferences _appPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        _appPreferences = AppPreferences.getAppPreferences(this);

        _saveStatePref = (CheckBoxPreference) findPreference("save_state");
        _saveStatePref.setChecked(_appPreferences.isSaveStateOnExit());
        _saveStatePref.setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener() {

                    @Override
                    public boolean onPreferenceChange(Preference p, Object newValue) {
                        _appPreferences.setSaveStateOnExit((Boolean)newValue);
                        return true;
                    }
                });

        _shakeFeaturePref = (CheckBoxPreference) findPreference("shake_feature");
        _shakeFeaturePref.setChecked(_appPreferences.isShakeFeatureEnabled());
        _shakeFeaturePref.setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener() {

                    @Override
                    public boolean onPreferenceChange(Preference p, Object newValue) {
                        _appPreferences.setShakeFeatureEnabled((Boolean)newValue);
                        return true;
                    }
                });
    }
}