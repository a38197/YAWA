package pt.isel.pdm.yawa.services

import android.app.IntentService
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v4.app.NotificationCompat
import android.util.Log
import pt.isel.pdm.yawa.R
import pt.isel.pdm.yawa.YawaApplication
import pt.isel.pdm.yawa.activities.MainActivity
import pt.isel.pdm.yawa.activities.settings.SettingsActivity
import pt.isel.pdm.yawa.model
import pt.isel.pdm.yawa.model.City
import pt.isel.pdm.yawa.model.ICallback
import pt.isel.pdm.yawa.model.ManageCities
import pt.isel.pdm.yawa.model.exception.YawaException

const val DEBUG_TAG = "UpdateService"
const val BLINK_RATE = 1000

class UpdateService() : IntentService("UpdateService") {


    override fun onHandleIntent(intent: Intent?) {
        Log.d(DEBUG_TAG, "Update service started")
        val intent = Intent(YawaApplication.UPDATE_FINISH_INTENT_FILTER)

        application.model.updateCities {
            intent.putExtra(YawaApplication.UPDATE_FINISH_KEY, it)
            sendBroadcast(intent)
            notification()
        }

    }

    private fun notification() {
        if (!SettingsActivity.isNotificationActive(this)) return
        val homeCity = ManageCities(this).getHomeCity() ?: return

        application.model.getCity(homeCity.id, false, object : ICallback<YawaException, City> {
            override fun onData(d: City) {
                val currTemp = d.main.temp.toString()
                val currWeather = d.weather.first().description

                if (isWeatherChanged(currTemp, currWeather)) {
                    Log.d(DEBUG_TAG, "Sending notification")
                    val mBuilder = NotificationCompat.Builder(this@UpdateService)
                            .setSmallIcon(R.mipmap.yawa)
                            .setContentTitle(resources.getString(R.string.notification_title, homeCity.name))
                            .setContentText("${d.weather.first().description}, ${d.main.temp} ${d.main.metric_system?.temperatureUnit?.symbol}")
                            .setAutoCancel(true)
                            .setDefaults(Notification.DEFAULT_SOUND or Notification.FLAG_SHOW_LIGHTS)
                            .setLights(Color.GREEN, BLINK_RATE, BLINK_RATE)
                            .setContentIntent(getPendingIntent())

                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(0, mBuilder.build())
                } else {
                    Log.d(DEBUG_TAG, "Weather equals as before. No notifications sent")
                }
            }

            override fun onError(ex: YawaException) {
                Log.e(DEBUG_TAG, "Error getting city from storage")
            }

        })


    }

    private fun isWeatherChanged(currTemp: String, currWeather: String): Boolean {
        var lastTemp = ManageCities(this@UpdateService).getLastTemperature()
        val lastWeather = ManageCities(this@UpdateService).getLastWeather()

        return !lastTemp.equals(currTemp) || !lastWeather.equals(currWeather)
    }

    private fun getPendingIntent(): PendingIntent? {
        val intent = Intent(this, MainActivity::class.java)
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }


}