package pt.isel.pdm.yawa.activities.settings

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import pt.isel.pdm.yawa.R
import java.util.*


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        fragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsFragment())
                .commit()
    }

    companion object {
        private const val TAG = "SettingsActivity"

        fun getUpdateInterval(context: Context): Long {
            val default = context.resources.getString(R.string.update_interval_default)

            val intervalMinutes = Integer.parseInt(
                    PreferenceManager.getDefaultSharedPreferences(context)
                            .getString(context.getString(R.string.update_interval_key), default))

            return fromMinutesToMs(intervalMinutes.toLong())
        }

        fun fromMinutesToMs(minutes: Long) = minutes * 60L * 1000L

        private fun updatesByWifiOnly(context: Context): Boolean {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(context.getString(R.string.preference_update_wifi_only_key), false)
        }

        fun areUpdatesActive(context: Context): Boolean {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(context.getString(R.string.update_interval_onoff_key), false)
        }

        /**
         * Checks the availability of networks against the current configuration
         * */
        //TODO check which network is used when more than one is available
        fun isNetworkAvailable(context: Context): Boolean {
            val con = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val info = con.activeNetworkInfo
            if (null == info || !info.isConnected) {
                Log.d(TAG, "No network available.")
            } else {
                val type = info.type
                val update = when (type) {
                    ConnectivityManager.TYPE_MOBILE -> !SettingsActivity.updatesByWifiOnly(context)
                    ConnectivityManager.TYPE_WIFI -> true
                    ConnectivityManager.TYPE_WIMAX -> true
                    ConnectivityManager.TYPE_ETHERNET -> true
                    else -> false
                }
                Log.d(TAG, "Default network found of type $type returning $update for network availability")
                return update
            }
            return false
        }

        fun isNotificationActive(context: Context): Boolean {
            val active = PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(context.getString(R.string.receive_notifications_onoff_key), false)

            if (!active) return false

            val from = PreferenceManager.getDefaultSharedPreferences(context)
                    .getString(context.getString(R.string.notification_from_key), "0").toInt()
            var to = PreferenceManager.getDefaultSharedPreferences(context)
                    .getString(context.getString(R.string.notification_to_key), "0").toInt()

            if (to == from)
                return true

            val calendar = Calendar.getInstance()
            var currHour = calendar.get(Calendar.HOUR_OF_DAY)

            if (to < from) {
                to += 24
                if (currHour < from)
                    currHour += 24
            }

            if (currHour >= from && currHour <= to - 1)
                return true

            return false
        }

        fun isDebugRandomTemperature(context: Context): Boolean {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(context.getString(R.string.debug_random_temp_key), false)
        }
    }
}
