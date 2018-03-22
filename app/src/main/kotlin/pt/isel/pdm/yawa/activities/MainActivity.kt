package pt.isel.pdm.yawa.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.*
import com.android.volley.toolbox.NetworkImageView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import pt.isel.pdm.yawa.*
import pt.isel.pdm.yawa.adapters.ForecastWeatherAdapter
import pt.isel.pdm.yawa.model.*
import pt.isel.pdm.yawa.model.exception.YawaException
import pt.isel.pdm.yawa.model.network.NetworkErrors
import pt.isel.pdm.yawa.parcelables.ParcelDayForecast
import pt.isel.pdm.yawa.services.UpdateService

private const val CITY_KEY = "pt.isel.pdm.yawa.activities.cityKey"
private const val FORECAST_KEY = "pt.isel.pdm.yawa.activities.forecastKey"
private const val CURR_CITY_KEY = "pt.isel.pdm.yawa.activities.currCityKey"
private const val SWIPE_SENSITIVE = 200
private const val DEBUG_TAG = "MAIN_ACTIVITY"
private const val CURRENT_LOCATION = -1
private const val HOME = 0


class MainActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private var city: City? = null
    private var forecast: Forecast? = null
    private val progress: ProgressDialog by lazy { ProgressDialog(this) }
    private val layout by lazy { findViewById(R.id.swiperefresh) as SwipeRefreshLayout }
    private var posXBefore: Float = 0f
    private var posXAfter: Float = 0f
    private var currCity: Int = CURRENT_LOCATION
    private val navImage  by lazy { findViewById(R.id.gps_image) as ImageView }
    private val homeImage by lazy { findViewById(R.id.home_image) as ImageView }

    companion object {
        val CITY_ID = "city_id"
        fun getIntent(caller: Activity): Intent {
            val intent = Intent(caller, MainActivity::class.java)
            return intent
        }
    }

    private val updateFinishReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(DEBUG_TAG, "update finish broadcast receive")
            layout.isRefreshing = false
            showCity() //Refresh activity
            val msg = intent.getStringExtra(YawaApplication.UPDATE_FINISH_KEY)
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private val mGoogleApiClient by lazy {
        GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_city_weather)
        restoreState(savedInstanceState)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        PreferenceManager.setDefaultValues(this, R.xml.yawa_preferences, false)

        layout.setOnRefreshListener {
            forceUpdate()
        }
        layout.setOnTouchListener { view, motionEvent -> touchEvent(motionEvent) }

        mGoogleApiClient //Just to call lazy implementation

        Toast.makeText(this, getString(R.string.pull_down_to_refresh), Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        mGoogleApiClient.connect()
        registerReceiver(updateFinishReceiver, IntentFilter(YawaApplication.UPDATE_FINISH_INTENT_FILTER))
        city?.observer?.onContentChange = { refreshByContentChange(city!!) }
    }


    override fun onStop() {
        super.onStop()
        mGoogleApiClient.disconnect()
        unregisterReceiver(updateFinishReceiver)
    }

    override fun onResume() {
        showCity()
        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_city, menu)
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_refresh -> {
                forceUpdate()
                return true
            }
        }
        return optionsItemSelected(this, item)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putParcelable(CITY_KEY, city)
        outState?.putParcelable(FORECAST_KEY, forecast)
        outState?.putInt(CURR_CITY_KEY, currCity)

        super.onSaveInstanceState(outState)
    }

    override fun onConnected(connectionHint: Bundle?) {
        Log.i(DEBUG_TAG, "Connection to Google API with success")
        saveCurrLocation()
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.i(DEBUG_TAG, "Connection to Google API was suspended: $p0")
    }


    override fun onConnectionFailed(result: ConnectionResult) {
        Log.e(DEBUG_TAG, "Connection to Google API failed with error: $result")
        val message: String
        when (result.errorCode) {
            ConnectionResult.SERVICE_DISABLED,
            ConnectionResult.SERVICE_INVALID,
            ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> message = "Cannot get current location because your Google API version is outdated. Please update."
            else -> message = result.toString()
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    /**************PRIVATE METHODS ***********************/

    private fun refreshByContentChange(c:City) {
        // Every city or forecast update will trigger this listener, making the activity refresh many times
        // in a single update cicle.
        //
        // As this activity already are listening for a broadcast on update finish, we decided to make
        // the activity refresh there.

//        if(!updateAware){
//            Log.d(DEBUG_TAG, "Detected a content change for city table, but activity is on stopped state, ignoring")
//            return
//        }
//
//        Log.d(DEBUG_TAG, "Detected a content change for city table, refreshing")
//        application.model.getCity(c.id, false, CbBuilder<YawaException, City>{ newCity ->
//            application.model.getForecast(c.id, false, CbBuilder<YawaException, Forecast>{ newForecast ->
//                Log.d(DEBUG_TAG, "Refreshing screen with new data for city ${newCity.id}")
//                draw(newCity, newForecast)
//            }.build())
//        }.build())

    }

    private fun forceUpdate() {
        layout.isRefreshing = true
        saveCurrLocation()
        startService(Intent(this, UpdateService::class.java))
    }

    private fun touchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> posXBefore = event.x
            MotionEvent.ACTION_UP -> {
                posXAfter = event.x
                return detectSwipeLeftRight()
            }
        }
        return super.onTouchEvent(event)
    }

    private fun detectSwipeLeftRight(): Boolean {
        var dif: Float = 0f
        var message: String = ""
        var isSwipeLeft: Boolean = false

        if (posXBefore < posXAfter) {
            dif = posXAfter - posXBefore
            message = "Swipe right"
            isSwipeLeft = false
        } else if (posXBefore > posXAfter) {
            dif = posXBefore - posXAfter
            message = "Swipe left"
            isSwipeLeft = true
        }
        if (dif > SWIPE_SENSITIVE) {
            Log.d(DEBUG_TAG, message)
            swipeLeftRightAction(isSwipeLeft)
            return true
        }
        return false
    }

    private fun swipeLeftRightAction(isSwipeLeft: Boolean) {
        if (ManageCities(this).getCities().isEmpty()) return
        val city: CityPref?

        if (isSwipeLeft) {
            city = ManageCities(this).getNextCity(currCity)
        } else {
            city = if (currCity == CURRENT_LOCATION) {
                ManageCities(this).getLastCity()
            } else {
                ManageCities(this).getPreviousCity(currCity)
            }
        }

        if (city == null) {
            currCity = CURRENT_LOCATION
        } else {
            currCity = city.index
        }
        showCity()
    }

    private fun showCity() {
        val cities = ManageCities(this).getCities()
        if (currCity == CURRENT_LOCATION || cities.isEmpty()) {
            return getCityCurrLocation()
        }

        val cityId = cities.elementAtOrElse(currCity, {
            currCity = HOME
            cities.elementAt(0)
        }).id
        if (cityId != 0) {
            application.model.getCity(cityId, false, onCity())
        }
    }

    private fun showImages(nav: Boolean, home: Boolean) {
        navImage.visibility = if (nav) View.VISIBLE else View.GONE
        homeImage.visibility = if (home) View.VISIBLE else View.GONE
    }

    private fun drawImages() {
        when (currCity) {
            CURRENT_LOCATION -> showImages(nav = true, home = false)
            HOME -> showImages(nav = false, home = true)
            else -> showImages(nav = false, home = false)
        }
    }


    private fun getCityCurrLocation() {
        val cityId = ManageCities(this).getCurrLocation()
        if (cityId == 0) {
            saveCurrLocation()
        } else {
            application.model.getCity(cityId, false, onCity())
        }
    }

    private fun saveCurrLocation() {
        val mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)

        if (mLastLocation != null) {
            val coordinateInfo = CoordinateInfo(mLastLocation.longitude, mLastLocation.latitude)
            application.model.getCity(coordinateInfo, object : ICallback<YawaException, City> {
                override fun onData(d: City) {
                    ManageCities(this@MainActivity).addCurrLocation(d.id)
                }

                override fun onError(ex: YawaException) {
                    Log.e(DEBUG_TAG, "Failed to update current location")
                }
            })
        } else {
            Log.e(DEBUG_TAG, "Can't get last location")
        }
    }


    private fun restoreState(savedInstanceState: Bundle?) {
        val savedCity = savedInstanceState?.getParcelable<City>(CITY_KEY)
        val savedForecast = savedInstanceState?.getParcelable<Forecast>(FORECAST_KEY)
        currCity = savedInstanceState?.getInt(CURR_CITY_KEY, CURRENT_LOCATION) ?: CURRENT_LOCATION
        if (savedCity != null && savedForecast != null) {
            draw(savedCity, savedForecast)
        }
    }

    private fun onCity(): ICallback<YawaException, City> {
        return CbBuilder<YawaException, City> {
            getForecast(it)
        }.onError {
            progress.dismiss()
            Log.e("onCity", "Error getting city", it)
            val message = "${getString(R.string.error_city_data)}. ${NetworkErrors.getNetworkError(this, it)}"
            showErrorDialog(activity = this, message = message)
        }
    }

    private fun getForecast(c: City) {
        application.model.getForecast(c.id, false, onForecast(c))
    }

    private fun onForecast(c: City): ICallback<YawaException, Forecast> {
        return CbBuilder<YawaException, Forecast> {
            progress.dismiss()
            draw(c, it)
        }.onError {
            progress.dismiss()
            Log.e("onForecast", "Error launching forecast", it)
            val message = "${getString(R.string.error_forecast_data)}. ${NetworkErrors.getNetworkError(this, it)}"
            showErrorDialog(activity = this, message = message)
        }
    }

    private fun draw(c: City, f: Forecast) {
        drawImages()

        city?.observer?.unregister?.invoke()
        c.observer?.onContentChange = { refreshByContentChange(c) }
        city = c
        forecast = f
        val textCity = findViewById(R.id.city_name) as TextView
        val textWeatherDesc = findViewById(R.id.txtWeatherDesc) as TextView
        val textCurrTemp = findViewById(R.id.txtCurrTemp) as TextView
        textCity.text = "${c.name}, ${c.sys.country}"
        if (c.weather.isNotEmpty()) {
            val imgV = findViewById(R.id.imgWeatherIcon) as NetworkImageView
            application.model.setImage(c.weather.elementAt(0).icon, imgV)

            textWeatherDesc.text = c.weather.iterator().next().description
            textCurrTemp.text = "${c.main.temp} ${c.main.metric_system?.temperatureUnit?.symbol}"
        }

        val listview = findViewById(R.id.weather_list_forecast) as ListView

        val adp = ForecastWeatherAdapter(this
                , R.layout.weather_list
                , f)

        listview.adapter = adp
        listview.setOnTouchListener { view, motionEvent ->
            touchEvent(motionEvent)
        }
        listview.setOnItemClickListener { adapterView: AdapterView<*>, view1: View, i: Int, l: Long ->

            val dayForecast = listview.getItemAtPosition(i) as DayForecast
            val wdIntent = WeatherDetails.getIntent(this, ParcelDayForecast(dayForecast, f.city))

            startActivity(wdIntent)
        }
    }
}
