package pt.isel.pdm.yawa.activities.settings

import android.os.Bundle
import android.preference.PreferenceFragment
import pt.isel.pdm.yawa.R

class SettingsFragment : PreferenceFragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.yawa_preferences)

    }

}
