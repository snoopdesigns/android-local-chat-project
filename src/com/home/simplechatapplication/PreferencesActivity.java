package com.home.simplechatapplication;

import com.home.simplechatapplication.R;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.util.Log;

public class PreferencesActivity extends PreferenceActivity {
	public static final String TAG = "PreferencesActivity";
    @Override
	    protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    addPreferencesFromResource(R.xml.preferences);
	    EditTextPreference editUsernamePref = (EditTextPreference) findPreference("editUsername");
	    editUsernamePref.setDefaultValue(MainActivity.getController().username);
	    editUsernamePref.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
			public boolean onPreferenceChange(Preference arg0, Object arg1) {
				Log.i(TAG,"arg1 = "+arg1.toString());
				MainActivity.getController().setUsername(arg1.toString());
				MainActivity.getConfiguration().WriteNodeValue("UIConfiguration", "username", arg1.toString());
				return false;
			}});
    }
}
