package pt.isel.pdm.yawa.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import pt.isel.pdm.yawa.R
import pt.isel.pdm.yawa.dao.contentprovider.dao.CityContentProviderKey
import pt.isel.pdm.yawa.dao.contentprovider.dao.ContentProviderDao
import pt.isel.pdm.yawa.dao.contentprovider.dao.ForecastContentProviderKey
import pt.isel.pdm.yawa.dao.contentprovider.dao.IObservableValue
import pt.isel.pdm.yawa.model.CbBuilder
import pt.isel.pdm.yawa.model.Cities
import pt.isel.pdm.yawa.model.City
import pt.isel.pdm.yawa.model.Forecast
import pt.isel.pdm.yawa.model.exception.YawaException
import pt.isel.pdm.yawa.model.network.Network
import java.util.*

private const val TAG = "YawaProviderDebug"

class YawaProviderDebug : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    lateinit var listView: ListView
    lateinit var citySearch: EditText
    var cityLoaded: City? = null

    val provider by lazy { ContentProviderDao<IObservableValue>(this) }
    val network by lazy { Network(this) }

    val mGoogleApiClient by lazy {
        GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_yawa_provider_debug)

        listView = findViewById(R.id.yawa_provider_debug_listview) as ListView
        citySearch = findViewById(R.id.yawa_provider_debug_cityName) as EditText

        mGoogleApiClient //just to call lazy implementation
    }

    override fun onStart() {
        mGoogleApiClient.connect()
        super.onStart()
    }

    override fun onStop() {
        mGoogleApiClient.disconnect()
        super.onStop()
    }

    override fun onConnected(connectionHint: Bundle?) {
        val message = "Conection to Google API with success: $connectionHint"
        Log.i(TAG, message)
    }

    override fun onConnectionSuspended(p0: Int) {
        val message = "Conection to Google API was suspended: $p0"
        Log.i(TAG, message)
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        val message = "Conection to Google API failed with error: $p0"
        Log.e(TAG, message)
    }

    /************************** CLICK FUNCTIONS ******************************/

    fun btnGetCities(view: View) {
        val cityName = citySearch.text.toString()
        network.getCities(cityName, CbBuilder<YawaException, Cities> {
            Toast.makeText(this, it.list.toString(), Toast.LENGTH_LONG).show()
        }.build())
    }

    fun btnCityClick(view: View) {
        val cityName = citySearch.text.toString()
        network.getCity(cityName, CbBuilder<YawaException, City> {
            val key = CityContentProviderKey(it)
            provider.addOrUpdate(key, it)
        }.build())
    }

    fun btnProviderGetClick(view: View) {
        val cityName = citySearch.text.toString()
        val key = CityContentProviderKey(cityName)
        val item = provider.get(key)
        item?.observer?.onContentChange = { Toast.makeText(this@YawaProviderDebug, "URI City changed", Toast.LENGTH_LONG).show() }
        val mapData: Map<String, String> = getMapData(item as City?)
        cityLoaded = item
        listView.adapter = getAdapter(mapData)
    }

    fun btnProviderDeleteClick(view: View) {
        val cityName = citySearch.text.toString()
        val key = CityContentProviderKey(cityName)
        provider.delete(key)
    }

    fun btnGetForecast(view: View) {
        val city = cityLoaded ?: return

        network.getForecast(city.id, CbBuilder<YawaException, Forecast> {
            val key = ForecastContentProviderKey(it)
            provider.addOrUpdate(key, it)
        }.build())
    }

    fun btnDeleteForecast(view: View) {
        val city = cityLoaded ?: return

        val key = ForecastContentProviderKey(city.id)
        provider.delete(key)
    }

    fun btnLoadForecast(view: View) {
        val city = cityLoaded ?: return

        val key = ForecastContentProviderKey(city.id)
        val item = provider.get(key)
        item?.observer?.onContentChange = { Toast.makeText(this@YawaProviderDebug, "URI Forecast changed", Toast.LENGTH_LONG).show() }
        val mapData: Map<String, String> = getMapData(item as Forecast?)
        listView.adapter = getAdapter(mapData)
    }

    fun btnGetLocationClick(view: View) {
        val mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
        var message = "Can't get location"
        if (mLastLocation != null) {
            message = "${mLastLocation.latitude}, ${mLastLocation.longitude}"
        }
        Log.d(TAG, message)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    }

    /************************** PRIVATE FUNCTIONS ******************************/

    private fun getAdapter(mapData: Map<String, String>): SimpleAdapter {
        return SimpleAdapter(
                this,
                listOf(mapData),
                R.layout.yawa_provider_debug_list_item,
                arrayOf("id", "name", "stamp", "data"),
                intArrayOf(
                        R.id.yawa_provider_debug_item_id,
                        R.id.yawa_provider_debug_item_name,
                        R.id.yawa_provider_debug_item_stamp,
                        R.id.yawa_provider_debug_item_data
                )
        )
    }

    private fun getMapData(item: City?): Map<String, String> {
        if (item == null) return mapOf()
        val map = HashMap<String, String>()
        map["id"] = item.id.toString()
        map["name"] = item.name
        map["stamp"] = item.dt.toString()
        map["data"] = item.toString()
        return map
    }

    private fun getMapData(item: Forecast?): Map<String, String> {
        if (item == null) return mapOf()
        val map = HashMap<String, String>()
        map["id"] = item.city.id.toString()
        map["name"] = "Forecast"
        map["stamp"] = ""
        map["data"] = item.toString()
        return map
    }

}
