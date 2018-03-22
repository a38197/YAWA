package pt.isel.pdm.yawa.model

import android.content.Context
import android.util.Log
import com.android.volley.toolbox.NetworkImageView
import pt.isel.pdm.yawa.R
import pt.isel.pdm.yawa.activities.settings.SettingsActivity
import pt.isel.pdm.yawa.dao.IDataAccess
import pt.isel.pdm.yawa.dao.contentprovider.dao.*
import pt.isel.pdm.yawa.model.exception.YawaException
import pt.isel.pdm.yawa.model.network.INetwork
import pt.isel.pdm.yawa.model.network.Network

const private val DEBUG_TAG = "ModelDomain"

interface ICallback<in EX, in DATA> {
    fun onData(d: DATA): Unit
    fun onError(ex: EX): Unit
}

fun <T, EX> getDefaultCallback(cbData: (T) -> Unit, cbError: (EX) -> Unit): ICallback<EX, T>
        where EX : Exception {
    return object : ICallback<EX, T> {
        override fun onData(d: T) {
            cbData(d)
        }

        override fun onError(ex: EX) {
            cbError(ex)
        }
    }
}

class CbBuilder<EX, T>(val message: String = "Error on callback", val data: (T) -> Unit) where EX : Exception {
    var error: (EX) -> Unit = { Log.e("DefaultCallback", message, it) }
    fun onError(error: (EX) -> Unit): ICallback<EX, T> {
        this.error = error
        return build()
    }

    fun build(): ICallback<EX, T> {
        return getDefaultCallback(data, error)
    }
}

interface IModelDomain {


    /**
     * Gets a list of cities that matches the given search string
     * @param search The search string
     * @param callback The code to execute on success or on failure
     */
    fun getCities(search: String, callback: ICallback<YawaException, Cities>): Unit

    /**
     * Gets a city by its name. The API results for this are unknown if name is ambiguous or not full match
     * @param cityName The name of the city to obtain
     * @param update to force a data update even if the requested data is in storage
     * @param callback the code to execute with the city or on error case
     * */
    fun getCity(cityName: String, update: Boolean, callback: ICallback<YawaException, City>): Unit

    /**
     * Gets a city by its ID.
     * @param cityId The ID of the city to obtain
     * @param update to force a data update even if the requested data is in storage
     * @param callback the code to execute with the city or on error case
     * */
    fun getCity(cityId: Int, update: Boolean, callback: ICallback<YawaException, City>): Unit

    /**
     * Gets a city by GPS Coordinates.
     * @param coordinateInfo The GPS coordinates of city
     * @param callback the code to execute with the city or on error case
     * */
    fun getCity(coordinateInfo: CoordinateInfo, callback: ICallback<YawaException, City>): Unit

    /**
     * Gets a forecast to a city. Currently is only supported a fixed 3 hour separated forecast
     * @param cityId The ID of the city to obtain the forecast
     * @param update to force a data update even if the requested data is in storage
     * @param callback the code to execute with the city or on error case
     * */
    fun getForecast(cityId: Int, update: Boolean, callback: ICallback<YawaException, Forecast>): Unit

    /**
     * Sets an icon obtained through the network given its API code. By using the NetworkImageView we can take
     * advantage of Volley cache capabilities
     * @param code icon code according to the API
     * @param netImageView the network image view to set
     * */
    fun setImage(code: String, netImageView: NetworkImageView): Unit

    fun updateCities(cb: (message: String) -> Unit)

}

internal class ModelDomain(private val context: Context) : IModelDomain {

    private val network: INetwork = Network(context)
    private val storage: IDataAccess<IContentProviderKey<IObservableValue>, IObservableValue> = ContentProviderDao<IObservableValue>(context)

    override fun updateCities(callback: (message: String) -> Unit) {
        var cities = ManageCities(context).getCities()
        var error = false

        cities = addCurrentLocation(cities)

        var count = cities.size
        if (count == 0) {
            Log.d(DEBUG_TAG, "nothing to update")
            callback(context.getString(R.string.add_city_before_update))
            return
        }

        cities.forEach {
            if (it.index == 0) {
                updateLastWeather(it)
            }
            getCity(it.id, true, object : ICallback<YawaException, City> {
                override fun onData(d: City) {
                    Log.d(DEBUG_TAG, "Current weather on ${d.name} updated")
                    getForecast(d.id, true, CbBuilder<YawaException, Forecast> {
                        Log.d(DEBUG_TAG, "Forecast for ${d.name} updated")
                        finish()
                    }.onError {
                        Log.e(DEBUG_TAG, "Forecast for ${d.name} not updated")
                        error = true
                        finish()
                    })
                }

                override fun onError(ex: YawaException) {
                    Log.e(DEBUG_TAG, "Current weather on ${it.name} not updated", ex)
                    error = true
                    finish()
                }

                fun finish() {
                    if (--count <= 0) {
                        Log.d(DEBUG_TAG, "end refresh")
                        if (error) {
                            callback(context.getString(R.string.cities_updated_with_error))
                        } else {
                            callback(context.getString(R.string.cities_updated))
                        }
                    }
                }
            })
        }
    }

    private fun  addCurrentLocation(cities: Collection<CityPref>): Collection<CityPref> {
        val cityID = ManageCities(context).getCurrLocation()
        if(cityID == 0){
            return cities
        }else{
            val currLocationCity = CityPref(cityID, "", "", -1)
            return cities.plus(currLocationCity)
        }
    }

    private fun updateLastWeather(cityPref: CityPref) {
        getCityFromStorage(cityPref.id, false, object : ICallback<YawaException, City> {
            override fun onError(ex: YawaException) {
                Log.e(DEBUG_TAG, "Error updating last weather")
            }

            override fun onData(d: City) {
                ManageCities(context).setLastWeather(d.weather.iterator().next().description)
                ManageCities(context).setLastTemperature(d.main.temp.toString())
            }

        })
    }


    override fun getCities(search: String, callback: ICallback<YawaException, Cities>) {
        Log.d(DEBUG_TAG, "Get cities with search string: $search")

        network.getCities(search, CbBuilder<YawaException, Cities>() {
            storeCities(it)
            callback.onData(it)
        }.onError {
            Log.e(DEBUG_TAG, "Error getting city from network", it)
            callback.onError(it)
        })
    }

    private fun storeCities(cities: Cities) {
        //TODO HF: change this to a background task
        cities.list.forEach {
            storage.addOrUpdate(CityContentProviderKey(it), it)
        }
    }

    override fun getCity(cityName: String, update: Boolean, callback: ICallback<YawaException, City>) {
        Log.d(DEBUG_TAG, "Get city with name $cityName and forced update to $update")
        if (update)
            getCityFromNetwork(cityName, true, callback)
        else
            getCityFromStorage(cityName, true, callback)
    }

    private fun getCityFromStorage(cityName: String, network: Boolean, callback: ICallback<YawaException, City>) {
        val key = CityContentProviderKey(name = cityName)
        storage.getAll(key, CbBuilder<YawaException, Collection<IObservableValue>> {
            if (it.isNotEmpty()) {
                Log.d(DEBUG_TAG, "Cache hit for city with name $cityName")
                callback.onData(it.elementAt(0) as City)
            } else if (network) {
                Log.d(DEBUG_TAG, "Get city with name $cityName not found on storage, trying network")
                getCityFromNetwork(cityName, true, callback)
            }
        }.onError {
            Log.e(DEBUG_TAG, "Error on storage getting city with name $cityName", it)
            if (network)
                getCityFromNetwork(cityName, false, callback)
        })
    }

    override fun getCity(cityId: Int, update: Boolean, callback: ICallback<YawaException, City>) {
        Log.d(DEBUG_TAG, "Get city with id $cityId and forced update to $update")
        if (update)
            getCityFromNetwork(cityId, true, callback)
        else
            getCityFromStorage(cityId, true, callback)
    }

    override fun getCity(coordinateInfo: CoordinateInfo, callback: ICallback<YawaException, City>) {
        Log.d(DEBUG_TAG, "Get city with coordinates ${coordinateInfo.lat}, ${coordinateInfo.lon}")
        getCityFromNetwork(coordinateInfo, true, callback)
    }

    private fun getCityFromStorage(cityId: Int, network: Boolean, callback: ICallback<YawaException, City>) {
        val key = CityContentProviderKey(id = cityId)
        storage.get(key, CbBuilder<YawaException, IObservableValue?> {
            if (it != null) {
                Log.d(DEBUG_TAG, "Cache hit for city with id $cityId")
                callback.onData(it as City)
            } else if (network) {
                Log.d(DEBUG_TAG, "Get city with id $cityId not found on storage, trying network")
                getCityFromNetwork(cityId, true, callback)
            }
        }.onError {
            Log.e(DEBUG_TAG, "Error on storage getting city with id $cityId", it)
            if (network)
                getCityFromNetwork(cityId, false, callback)
        })
    }

    private fun getCityFromNetwork(cityId: Int, store: Boolean, callback: ICallback<YawaException, City>) {
        if (!SettingsActivity.isNetworkAvailable(context)) {
            callback.onError(YawaException(context.getString(R.string.no_available_network)))
            return
        }

        network.getCity(cityId, CbBuilder<YawaException, City> { city ->
            if (store)
                storage.addOrUpdate(CityContentProviderKey(city), city, CbBuilder<YawaException, Unit> {
                    callback.onData(city)
                }.onError {
                    Log.e(DEBUG_TAG, "Error storing city from network", it)
                    callback.onData(city)
                })
        }
                .onError {
                    Log.e(DEBUG_TAG, "Error getting city from network", it)
                    callback.onError(it)
                })
    }

    private fun getCityFromNetwork(cityName: String, store: Boolean, callback: ICallback<YawaException, City>) {
        if (!SettingsActivity.isNetworkAvailable(context)) {
            callback.onError(YawaException(context.getString(R.string.no_available_network)))
            return
        }

        network.getCity(cityName, CbBuilder<YawaException, City> { city ->
            if (store)
                storage.addOrUpdate(CityContentProviderKey(city), city, CbBuilder<YawaException, Unit> {
                    callback.onData(city)
                }.onError {
                    Log.e(DEBUG_TAG, "Error storing city from network", it)
                    callback.onData(city)
                })
        }
                .onError {
                    Log.e(DEBUG_TAG, "Error getting city from network", it)
                    callback.onError(it)
                })
    }

    private fun getCityFromNetwork(coordinateInfo: CoordinateInfo, store: Boolean, callback: ICallback<YawaException, City>) {
        if (!SettingsActivity.isNetworkAvailable(context)) {
            callback.onError(YawaException(context.getString(R.string.no_available_network)))
            return
        }

        network.getCity(coordinateInfo, CbBuilder<YawaException, City> { city ->
            if (store)
                storage.addOrUpdate(CityContentProviderKey(city), city, CbBuilder<YawaException, Unit> {
                    callback.onData(city)
                }.onError {
                    Log.e(DEBUG_TAG, "Error storing city from network", it)
                    callback.onData(city)
                })
        }
                .onError {
                    Log.e(DEBUG_TAG, "Error getting city from network", it)
                    callback.onError(it)
                })
    }


    override fun getForecast(cityId: Int, update: Boolean, callback: ICallback<YawaException, Forecast>) {
        Log.d(DEBUG_TAG, "Get forecast for city with id $cityId and forced update to $update")
        if (update)
            getForecastFromNetwork(cityId, true, callback)
        else
            getForecastFromStorage(cityId, true, callback)
    }

    private fun getForecastFromStorage(cityId: Int, network: Boolean, callback: ICallback<YawaException, Forecast>) {
        val key = ForecastContentProviderKey(cityId)
        storage.get(key, CbBuilder<YawaException, IObservableValue?> {
            if (it != null) {
                Log.d(DEBUG_TAG, "Cache hit for forecast with id $cityId")
                callback.onData(it as Forecast)
            } else if (network) {
                Log.d(DEBUG_TAG, "Get forecast with city id $cityId not found on storage, trying network")
                getForecastFromNetwork(cityId, true, callback)
            }
        }.onError {
            Log.e(DEBUG_TAG, "Error on storage getting forecast with id $cityId", it)
            if (network)
                getForecastFromNetwork(cityId, false, callback)
        })
    }

    private fun getForecastFromNetwork(cityId: Int, store: Boolean, callback: ICallback<YawaException, Forecast>): Unit {
        if (!SettingsActivity.isNetworkAvailable(context)) {
            callback.onError(YawaException(context.getString(R.string.no_available_network)))
            return
        }

        network.getForecast(cityId, CbBuilder<YawaException, Forecast> { forecast ->
            if (store)
                storage.addOrUpdate(ForecastContentProviderKey(forecast), forecast, CbBuilder<YawaException, Unit> {
                    callback.onData(forecast)
                }.onError {
                    Log.e(DEBUG_TAG, "Error storing forecast from network", it)
                    callback.onData(forecast)
                })
        }
                .onError {
                    Log.e(DEBUG_TAG, "Error getting forecast from network", it)
                    callback.onError(it)
                })
    }

    override fun setImage(code: String, netImageView: NetworkImageView) {
        val uri = network.getImageUri(code);

        netImageView.setImageUrl(uri, network.imageLoader);
    }

}

