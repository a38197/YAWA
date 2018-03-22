package pt.isel.pdm.yawa.broadcastreceiver

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import pt.isel.pdm.yawa.activities.settings.SettingsActivity
import pt.isel.pdm.yawa.services.UpdateService

private const val TAG = "UpdateReceiver"

class UpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "onReceive with action $action")

        if(Intent.ACTION_BOOT_COMPLETED == action && !UpdatesManager.areUpdatesActive(context)) {
            Log.d(TAG, "Updates not active, boot completed action ignored.")
            return
        }

        context.startService(Intent(context, UpdateService::class.java))
    }
}

/**
 * Utility object that contains logic to manage Automatic Updates Alarm settings
 * */
object UpdatesManager {

    private const val TAG = "UpdateManager"

    fun areUpdatesActive(context: Context):Boolean{
        return SettingsActivity.areUpdatesActive(context) &&
                getLastRecordedBatteryStatus(context) == BatteryStatus.Okay
    }

    fun setUpdates(context: Context){
        Log.i(TAG, "Setting up update service")
        if(SettingsActivity.areUpdatesActive(context)){
            if(getLastRecordedBatteryStatus(context) == BatteryStatus.Okay){
                val updateInterval = SettingsActivity.getUpdateInterval(context)
                setupAutomaticUpdates(context, updateInterval)
            } else
                Log.d(TAG, "Updates not active by low battery")
        } else
            Log.d(TAG, "Updates not active by the user configuration")

    }

    fun cancelUpdates(context: Context){
        Log.i(TAG, "Removing alarm for update service")
        val updateIntent = Intent(context, UpdateReceiver::class.java)
        //TODO the existing intent is not removed, even with the flags cancel current and one shot
        val pendingIntent = PendingIntent.getBroadcast(context, ALARM_SERVICE_REQUEST, updateIntent, NO_FLAGS )
        cancelAlarmManager(context, pendingIntent)
    }

    private fun setupAutomaticUpdates(context: Context, interval: Long) {
        val updateItent = Intent(context, UpdateReceiver::class.java)

        val existing = PendingIntent.getBroadcast(context, ALARM_SERVICE_REQUEST, updateItent, PendingIntent.FLAG_NO_CREATE)
        if (existing == null) {
            Log.d(TAG, "Pending intent not exists, registering alarm")
            val pendingUpdateIntent = PendingIntent.getBroadcast(context, ALARM_SERVICE_REQUEST, updateItent, NO_FLAGS)
            setAlarmManager(context, pendingUpdateIntent, interval)
        } else{
            Log.d(TAG, "Pending intent already exists, updating interval")
            updateAutomaticUpdateInterval(context, interval, existing)
        }
    }

    private fun updateAutomaticUpdateInterval(context: Context, interval: Long, existing: PendingIntent) {
        cancelAlarmManager(context, existing)
        setAlarmManager(context, existing, interval)
    }

    private fun setAlarmManager(context: Context, pendingIntent: PendingIntent, interval: Long) {
        Log.d(TAG, "Setting alarm for update service with interval $interval")
        val firstUpdateAfterBoot = SystemClock.elapsedRealtime() + 15 * 1000
        val alarmManager = context.getSystemService(Application.ALARM_SERVICE) as AlarmManager
        alarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                firstUpdateAfterBoot,
                interval,
                pendingIntent
        )
    }

    private fun cancelAlarmManager(context: Context, pendingIntent: PendingIntent) {
        Log.d(TAG, "Canceling existing alarm for update service")
        val alarmManager = context.getSystemService(Application.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    private const val ALARM_SERVICE_REQUEST = 12345
    private const val NO_FLAGS = 0
}


