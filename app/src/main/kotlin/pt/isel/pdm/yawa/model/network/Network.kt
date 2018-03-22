package pt.isel.pdm.yawa.model.network


import android.content.Context
import android.graphics.Bitmap
import android.preference.PreferenceManager
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import pt.isel.pdm.yawa.R
import pt.isel.pdm.yawa.activities.settings.SettingsActivity
import pt.isel.pdm.yawa.model.*
import pt.isel.pdm.yawa.model.exception.NoCitiesFoundException
import pt.isel.pdm.yawa.model.exception.YawaException
import pt.isel.pdm.yawa.model.json.JsonFactory
import pt.isel.pdm.yawa.utils.StringUtils
import java.util.*


class Network(val context: Context) : INetwork {
    val imageCache:IImageCache = BitmapFilesystemImageCache(context)
    val requestQueue: RequestQueue
    override val imageLoader: ImageLoader

    init {
        /*
        * Taking advantage of Volley's ability to use our own image cache
        * */
        this.requestQueue = Volley.newRequestQueue(context)
        this.imageLoader = ImageLoader(requestQueue, imageCache)
    }


    override fun getCities(search: String, cb: ICallback<YawaException, Cities>) {
        val system = getUnitType()
        val uri = context.getString(R.string.baseApiUri) +
                "find?q=${StringUtils.replaceSpaces(search)}" +
                "&type=like" +
                "&units=${system.apiString}" +
                "&lang=${context.getString(R.string.language_code)}" +
                "&APPID=${context.resources.getString(R.string.appid)}"

        Log.i(TAG, "Getting cities with name $search")
        requestQueue.add(
                JsonObjectRequest(
                        uri,
                        null,
                        {
                            //On success
                            val cities = JsonFactory.getCities(it.toString())
                            if (cities.count == 0) {
                                cb.onError(NoCitiesFoundException("Search returns zero items"))
                            } else {
                                cities.list.forEach { it.main.metric_system = system }
                                cb.onData(cities)
                            }
                        },
                        {
                            //On Failure
                            cb.onError(YawaException(it.toString(), it))
                        }

                )
        )
    }

    override fun getCity(name: String, cb: ICallback<YawaException, City>) {
        val system = getUnitType()
        val uri = context.getString(R.string.baseApiUri) +
                "weather?q=${StringUtils.replaceSpaces(name)}" +
                "&units=${system.apiString}" +
                "&lang=${context.getString(R.string.language_code)}" +
                "&APPID=${context.resources.getString(R.string.appid)}"

        Log.i(TAG, "Getting city with name $name")
        requestCity(uri, system, cb)
    }

    override fun getCity(id: Int, cb: ICallback<YawaException, City>) {
        val system = getUnitType()
        val uri = context.getString(R.string.baseApiUri) +
                "weather?id=$id" +
                "&units=${system.apiString}" +
                "&lang=${context.getString(R.string.language_code)}" +
                "&APPID=${context.resources.getString(R.string.appid)}"

        Log.i(TAG, "Getting city with id $id")
        requestCity(uri, system, cb)
    }

    override fun getCity(coordinateInfo: CoordinateInfo, cb: ICallback<YawaException, City>) {
        val system = getUnitType()
        val uri = context.getString(R.string.baseApiUri) +
                "weather?lat=${coordinateInfo.lat}" +
                "&lon=${coordinateInfo.lon}" +
                "&units=${system.apiString}" +
                "&lang=${context.getString(R.string.language_code)}" +
                "&APPID=${context.resources.getString(R.string.appid)}"

        Log.i(TAG, "Getting city with coordinates ${coordinateInfo.lat}, ${coordinateInfo.lon}")
        requestCity(uri, system, cb)
    }




    private fun requestCity(uri: String, system: MetricSystem, cb: ICallback<YawaException, City>) {
        requestQueue.add(
                JsonObjectRequest(
                        uri,
                        null,
                        {
                            //On success
                            val city = JsonFactory.getCity(it.toString())
                            city.main.metric_system = system
                            randomTemp(city)
                            cb.onData(city)
                        },
                        {
                            //On Failure
                            cb.onError(YawaException(it.toString(), it))
                        }
                )
        )
    }

    override fun getForecast(id: Int, cb: ICallback<YawaException, Forecast>) {
        val system = getUnitType()
        val uri = context.getString(R.string.baseApiUri) +
                "forecast?id=$id" +
                "&units=${system.apiString}" +
                "&lang=${context.getString(R.string.language_code)}" +
                "&APPID=${context.resources.getString(R.string.appid)}"

        Log.i(TAG, "Getting forecast for city with id $id")
        requestQueue.add(
                JsonObjectRequest(
                        uri,
                        null,
                        {
                            //On success
                            val forecast = JsonFactory.getForecast(it.toString())
                            forecast.list.forEach { it.main.metric_system = system }
                            cb.onData(forecast)
                        },
                        {
                            //On Failure
                            cb.onError(YawaException(it.toString(), it))
                        }
                )
        )


    }

    override fun getImage(code: String, cb: ICallback<YawaException, Bitmap>) {
        val uri = getImageUri(code)

        Log.i(TAG, "Getting image with code $code")
        requestQueue.add(
                ImageRequest(
                        uri,
                        Response.Listener<Bitmap> { bitmap ->
                            cb.onData(bitmap) //On Success
                        }, 0, 0, null, Bitmap.Config.ALPHA_8,
                        Response.ErrorListener { error ->
                            cb.onError(YawaException(error.toString())) //On Failure
                        })
        )

    }

    override fun getImageUri(code: String): String {
        return context.getString(R.string.baseImageUri) + "$code.png"
    }

    fun getUnitType(): MetricSystem {
        val systemStr = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.unit_preference_key),
                        context.getString(R.string.temperature_format_default))
        return MetricSystem.Companion.fromString(systemStr)
    }

    private companion object {
        private const val TAG = "Network"
    }

    //Just for debug purposes
    private fun randomTemp(city: City){
        if(SettingsActivity.isDebugRandomTemperature(context)) {
            val rnd = Random().nextInt(10 - 1 + 1) + 1
            city.main.temp += rnd
        }
    }
}