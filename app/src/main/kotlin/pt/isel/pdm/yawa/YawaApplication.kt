package pt.isel.pdm.yawa

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.multidex.MultiDexApplication
import android.util.Log
import android.view.MenuItem
import pt.isel.pdm.yawa.activities.AboutActivity
import pt.isel.pdm.yawa.activities.ManageCitiesActivity
import pt.isel.pdm.yawa.activities.YawaProviderDebug
import pt.isel.pdm.yawa.activities.settings.SettingsActivity
import pt.isel.pdm.yawa.broadcastreceiver.BatteryStatusReceiver
import pt.isel.pdm.yawa.broadcastreceiver.UpdatesManager
import pt.isel.pdm.yawa.model.IModelDomain
import pt.isel.pdm.yawa.model.ModelDomain

class YawaApplication : MultiDexApplication() {
    /**
     * Model access
     * */
    val model: IModelDomain by lazy { ModelDomain(this) }
    /**
     * The preferences changed listener to update DB update frequency and data validity
     * Google recommends to maintain a strong reference to this handler to avoid GC collection
     * */
    private val preferenceListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        when (key) {
            getString(R.string.update_interval_key) -> updateIntervalTime()
            getString(R.string.update_interval_onoff_key) -> updateIntervalOnOf(sharedPreferences, key)
        }
    }

    private fun updateIntervalTime() {
        Log.d(TAG, "Update recurrence preference changed")
        UpdatesManager.setUpdates(this)
    }

    private fun updateIntervalOnOf(sharedPreferences: SharedPreferences, key:String){
        Log.d(TAG,"UpdateInterval OnOff preference changed")
        val updateActive = sharedPreferences.getBoolean(key, false)
        if (updateActive) {
            UpdatesManager.setUpdates(this)
        } else {
            UpdatesManager.cancelUpdates(this)
        }
    }

    private val batteryReceiver = BatteryStatusReceiver();

    override fun onCreate() {
        super.onCreate()
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(preferenceListener)

        //Registered receiver here because google documentation says we can't use the manifest on this case
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        UpdatesManager.setUpdates(this)
    }

    override fun onTerminate() {
        unregisterReceiver(batteryReceiver)
    }

    companion object {
        const val UPDATE_FINISH_INTENT_FILTER = "UPDATE_FINISH_INTENT_FILTER"
        const val UPDATE_FINISH_KEY = "pt.isel.pdm.yawa.UPDATE_FINISH_KEY"
    }
}

val Application.model: IModelDomain
    get() = (this as YawaApplication).model


fun optionsItemSelected(activity: Activity, item: MenuItem): Boolean {
    when (item.itemId) {
        R.id.action_manage_cities -> activity.startActivity(Intent(activity, ManageCitiesActivity::class.java))
        R.id.action_settings -> activity.startActivity(Intent(activity, SettingsActivity::class.java))
        R.id.action_about -> activity.startActivity(Intent(activity, AboutActivity::class.java))
        R.id.action_debug -> activity.startActivity(Intent(activity, YawaProviderDebug::class.java))
    }
    return true

}

/**
 * Launches a standard error dialog for the activity
 * */
fun showErrorDialog(activity: Activity, title: String = "Error", message: String) {
    AlertDialog.Builder(activity)
            .setMessage(message)
            .setTitle(title)
            .setNeutralButton("Ok") { dialogInterface, i -> }
            .create().show()
}

private const val TAG = "YawaApplication"