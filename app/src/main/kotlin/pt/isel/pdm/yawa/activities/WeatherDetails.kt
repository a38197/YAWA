package pt.isel.pdm.yawa.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import com.android.volley.toolbox.NetworkImageView
import pt.isel.pdm.yawa.R
import pt.isel.pdm.yawa.model
import pt.isel.pdm.yawa.model.MetricSystem
import pt.isel.pdm.yawa.parcelables.ParcelDayForecast

class WeatherDetails : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_details)

        setScreenFields(getExtraForecast())

        Log.d("WeatherDetails","Test")

    }

    private fun setScreenFields(forecast: ParcelDayForecast) {
        var txtCityName : TextView = findViewById(R.id.txtCityName) as TextView
        var txtDay : TextView = findViewById(R.id.txtDay) as TextView

        var imgWeatherIcon : NetworkImageView = findViewById(R.id.imgWeatherIcon) as NetworkImageView

        //fields below defined as TextField, but in the layout as EditText (easier to assign values)
        var txtWeatherTemp : TextView = findViewById(R.id.etxtTemp) as TextView
        var txtCloud: TextView = findViewById(R.id.etxtCloud) as TextView
        var txtRain : TextView = findViewById(R.id.etxtRain) as TextView
        var txtWindDegree : TextView = findViewById(R.id.etxtWindDegree) as TextView
        var txtWindSpeed : TextView = findViewById(R.id.etxtWindSpeed) as TextView

        val currentSystem = MetricSystem.fromPreferences(this)

        txtCityName.text = forecast.cityName
        txtDay.text = forecast.weatherDate

        application.model.setImage(forecast.weatherIcon, imgWeatherIcon)

        txtWeatherTemp.text = forecast.weatherTempMin + " " + currentSystem.temperatureUnit.symbol + " / " + forecast.weatherTempMax + " " + currentSystem.temperatureUnit.symbol
        txtCloud.text = forecast.cloudPercentage + " %"
        txtRain.text = forecast.rainLast3h + " mm"
        txtWindSpeed.text = forecast.windSpeed + " m/s"
        txtWindDegree.text = forecast.windDegree + " º"

    }

    // functions related to Intent creation/usage
    fun getExtraForecast(): ParcelDayForecast = intent.getParcelableExtra<ParcelDayForecast>(DAY_FORECAST)

    companion object{

        val DAY_FORECAST = "day_forecast"

        fun getIntent(caller: Activity, dayForecast: ParcelDayForecast): Intent {
            val intent = Intent(caller, WeatherDetails::class.java)

            intent.putExtra(DAY_FORECAST, dayForecast)

            return intent
        }
    }
}
