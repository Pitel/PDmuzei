package cz.pitel.pdmuzei;

import android.os.Bundle;
import android.preference.PreferenceActivity;

@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName("muzeiartsource_" + getString(R.string.app_name));
        addPreferencesFromResource(R.xml.settings);
    }
}
