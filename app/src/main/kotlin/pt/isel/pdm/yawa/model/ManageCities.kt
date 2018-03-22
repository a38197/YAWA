package pt.isel.pdm.yawa.model

import android.content.Context
import pt.isel.pdm.yawa.R
import pt.isel.pdm.yawa.model.json.JsonFactory
import java.util.*

const private val DEBUG_TAG = "ManageCities"

interface IManageCities {
    fun getCities(): Collection<CityPref>
    fun addCity(city: CityPref)
    fun removeCity(city: CityPref)
    fun getLastWeather(): String
    fun getLastTemperature(): String
    fun setLastWeather(weather: String)
    fun setLastTemperature(temp: String)

    /**
     * Return next city if any.
     * Otherwise returns null
     */
    fun getNextCity(currIndex: Int): CityPref?

    /**
     * Return previous city if any.
     * Otherwise returns null
     */
    fun getPreviousCity(currIndex: Int): CityPref?

    fun getHomeCity(): CityPref?
    fun getLastCity(): CityPref?
    fun addCurrLocation(cityId: Int)
    fun getCurrLocation(): Int
}

class ManageCities(val context: Context) : IManageCities {
    private val savedCitieskey: String
    private val currLocationkey: String
    private val prefFile: String;

    companion object {
        const val LAST_WEATHER_FILE = "LAST_WEATHER_FILE"
        const val LAST_WEATHER_KEY = "LAST_WEATHER_KWY"
        const val LAST_TEMP_KEY = "LAST_TEMP_KEY"
    }


    init {
        savedCitieskey = context.getString(R.string.saved_cities_list_key)
        currLocationkey = context.getString(R.string.current_location_key)
        prefFile = context.getString(R.string.saved_cities_pref_file)
    }

    override fun getCities(): Collection<CityPref> {
        val cities = context.getSharedPreferences(prefFile, Context.MODE_PRIVATE).getStringSet(savedCitieskey, null)
        if (cities == null)
            return listOf(); //empty list

        return cities.map { JsonFactory.getCityPref(it) }
                .sortedWith(Comparator { t1: CityPref, t2: CityPref ->
                    t1.index.compareTo(t2.index)
                })
    }

    override fun addCity(cityPref: CityPref) {
        val citiesColl = getCities()
        cityPref.index = citiesColl.size
        val cities = getCities()
                .plus(cityPref)
                .map { JsonFactory.jsonCityPref(it) }
                .toSet()
        saveCities(cities)
    }

    override fun removeCity(cityPref: CityPref) {
        var index: Int = 0
        val cities = getCities()
                .filter { !it.equals(cityPref) }
                .map {
                    it.index = index++
                    JsonFactory.jsonCityPref(it)
                }
                .toSet()
        saveCities(cities)
    }


    override fun getNextCity(currIndex: Int): CityPref? {
        val cities = getCities()
        return cities.elementAtOrElse(currIndex + 1, { null })
    }

    override fun getPreviousCity(currIndex: Int): CityPref? {
        val cities = getCities()
        return cities.elementAtOrElse(currIndex - 1, { null })
    }

    override fun getHomeCity(): CityPref? {
        val cities = getCities()
        if (!cities.isEmpty()) {
            return cities.elementAt(0)
        }
        return null
    }

    override fun getLastCity(): CityPref? {
        val cities = getCities()
        if (!cities.isEmpty()) {
            return cities.last()
        }
        return null
    }

    override fun getLastWeather(): String {
        return context.getSharedPreferences(LAST_WEATHER_FILE, Context.MODE_PRIVATE)
                .getString(LAST_WEATHER_KEY, "")
    }

    override fun getLastTemperature(): String {
        return context.getSharedPreferences(LAST_WEATHER_FILE, Context.MODE_PRIVATE)
                .getString(LAST_TEMP_KEY, "")
    }

    override fun setLastWeather(weather: String) {
        saveLastWeatherPref(LAST_WEATHER_KEY, weather)
    }

    override fun setLastTemperature(temp: String) {
        saveLastWeatherPref(LAST_TEMP_KEY, temp)
    }

    private fun saveLastWeatherPref(key: String, value: String) {
        context.getSharedPreferences(LAST_WEATHER_FILE, Context.MODE_PRIVATE)
                .edit()
                .putString(key, value)
                .commit() //TODO HF consider using apply()
    }

    override fun addCurrLocation(cityId: Int) {
        context.getSharedPreferences(prefFile, Context.MODE_PRIVATE)
                .edit()
                .putInt(currLocationkey, cityId)
                .commit()
    }

    override fun getCurrLocation(): Int {
        return context.getSharedPreferences(prefFile, Context.MODE_PRIVATE)
        .getInt(currLocationkey, 0)
    }

    private fun saveCities(values: Set<String>) {
        context.getSharedPreferences(prefFile, Context.MODE_PRIVATE)
                .edit()
                .putStringSet(savedCitieskey, values)
                .commit()
    }

}
