package pt.isel.pdm.yawa.model.json

import com.google.gson.Gson
import pt.isel.pdm.yawa.model.Cities
import pt.isel.pdm.yawa.model.City
import pt.isel.pdm.yawa.model.CityPref
import pt.isel.pdm.yawa.model.Forecast

/**
 * Created by ncaro on 10/19/2016.
 */

object JsonFactory {
    private val gson = Gson()

    fun getCity(json: String): City = gson.fromJson(json, City::class.java)
    fun jsonCity(city: City): String = gson.toJson(city)
    fun getCities(json: String): Cities = gson.fromJson(json, Cities::class.java)
    fun getCityPref(json: String) : CityPref = gson.fromJson(json, CityPref::class.java)
    fun jsonCityPref(cityPref: CityPref): String = gson.toJson(cityPref)
    fun getForecast(json: String): Forecast = gson.fromJson(json, Forecast::class.java)
    fun jsonForecast(forecast: Forecast): String = gson.toJson(forecast)
}