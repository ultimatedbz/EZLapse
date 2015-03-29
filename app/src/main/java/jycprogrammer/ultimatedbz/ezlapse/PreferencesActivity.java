package jycprogrammer.ultimatedbz.ezlapse;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by jeffreychen on 3/28/15.
 */
public class PreferencesActivity  extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
}