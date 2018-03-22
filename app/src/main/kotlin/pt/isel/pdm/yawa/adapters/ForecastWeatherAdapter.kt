package pt.isel.pdm.yawa.adapters

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.android.volley.toolbox.NetworkImageView
import pt.isel.pdm.yawa.R
import pt.isel.pdm.yawa.dao.contentprovider.YawaAdapter
import pt.isel.pdm.yawa.model
import pt.isel.pdm.yawa.model.DayForecast
import pt.isel.pdm.yawa.model.Forecast
import pt.isel.pdm.yawa.model.MetricSystem
import pt.isel.pdm.yawa.model.roundDouble
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Angelo Mestre on 16/10/2016.
 */

class ForecastWeatherAdapter(context: Context, val resource: Int, data: Forecast) : YawaAdapter<DayForecast>(context, resource, data.list.toList()) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        val holder: WeatherHolder

        if (row == null) {
            val inflater = (context as Activity).layoutInflater
            row = inflater.inflate(resource, parent, false)

            holder = WeatherHolder(
                    row.findViewById(R.id.txtWeatherDay) as TextView,
                    row.findViewById(R.id.imgWeatherIcon) as NetworkImageView,
                    row.findViewById(R.id.txtWeatherDesc) as TextView,
                    row.findViewById(R.id.txtWeatherMinTemp) as TextView,
                    row.findViewById(R.id.txtWeatherMaxTemp) as TextView
            )

            row.tag = holder
        } else {
            holder = row.tag as WeatherHolder
        }

        val roundPlaces = context.resources.getInteger(R.integer.temp_round_places)
        val currentSystem = MetricSystem.Companion.fromPreferences(this.context)
        val forecast = getItem(position)
        val weather = forecast.weather.elementAt(0)
        holder.txtWeatherDay.text = SimpleDateFormat("dd-MM HH:mm", Locale.UK).format(Date(forecast.dt * 1000))
        (context.applicationContext as Application).model.setImage(weather.icon, holder.imgWeatherIcon)
        holder.txtWeatherDesc.text = weather.description

        val forecastSystem = forecast.main.metric_system!!
        holder.txtWeatherMinTemp.text = getTempString(forecast.main.temp_min, roundPlaces, forecastSystem, currentSystem)
        holder.txtWeatherMaxTemp.text = getTempString(forecast.main.temp_max, roundPlaces, forecastSystem, currentSystem)

        return row!!
    }

    private fun getTempString(value:Double, round:Int, from:MetricSystem, to:MetricSystem):String{
        val converted = roundDouble(from.temperatureUnit.convertTo(value,to.temperatureUnit),round)
        Log.d("ForecastWeatherAdapter", "Converted value from $value ${from.apiString} to $converted ${to.apiString}")
        return converted.toString() + " " + to.temperatureUnit.symbol
    }

    class WeatherHolder(
        val txtWeatherDay: TextView,
        val imgWeatherIcon: NetworkImageView,
        val txtWeatherDesc: TextView,
        val txtWeatherMinTemp: TextView,
        val txtWeatherMaxTemp: TextView
    )
}

