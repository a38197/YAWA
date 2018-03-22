package pt.isel.pdm.yawa.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.preference.PreferenceManager
import android.util.Log
import pt.isel.pdm.yawa.R

/**
 * Created by nuno on 12/18/16.
 */

private const val TAG = "BatteryStateReceiver"

class LowBatteryStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG,"Received low battery notification")
        setBatteryStatus(context, BatteryStatus.NotOkay)
        UpdatesManager.cancelUpdates(context)
    }
}

class OkayBatteryStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG,"Received okay battery notification")

        setBatteryStatus(context, BatteryStatus.Okay)
        UpdatesManager.setUpdates(context)
    }
}

/**
 * Manages updates by battery level. Reduces the time that the updates are out of sync with the config
 * */
class BatteryStatusReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 100)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
        Log.i(TAG,"Received battery level change notification with value $level and scale $scale")
        if(isLowThreshold(level, context)){
            if(getLastRecordedBatteryStatus(context) == BatteryStatus.Okay){
                Log.d(TAG, "Battery level OK and low threshold passed. Disabling updates.")
                setBatteryStatus(context, BatteryStatus.NotOkay)
                UpdatesManager.cancelUpdates(context)
            }
        } else {
            if(getLastRecordedBatteryStatus(context) == BatteryStatus.NotOkay){
                Log.d(TAG, "Battery level NotOK and high threshold passed. Enabling updates.")
                setBatteryStatus(context, BatteryStatus.Okay)
                UpdatesManager.setUpdates(context)
            }
        }
    }

    /**
     * This value was empirically obtained.
     * */
    private fun isLowThreshold(value:Int, context: Context):Boolean{
        val thr = context.resources.getInteger(R.integer.battery_low_threshold)
        return value < thr
    }
}

enum class BatteryStatus {
    Okay,
    NotOkay
}

private fun setBatteryStatus(context:Context, status: BatteryStatus):Unit{
    val sp = PreferenceManager.getDefaultSharedPreferences(context)
    val key = context.resources.getString(R.string.battery_status_key)
    sp.edit()
            .putString(key, status.name)
            .apply()
}

fun getLastRecordedBatteryStatus(context:Context):BatteryStatus {
    val sp = PreferenceManager.getDefaultSharedPreferences(context)
    val key = context.resources.getString(R.string.battery_status_key)
    val value = sp.getString(key, BatteryStatus.Okay.name)
    return BatteryStatus.valueOf(value)
}