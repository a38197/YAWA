package pt.isel.pdm.yawa.model.network

import android.graphics.Bitmap
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.NetworkImageView
import pt.isel.pdm.yawa.model.*
import pt.isel.pdm.yawa.model.exception.YawaException

interface INetwork {

    fun getCities(search: String, cb: ICallback<YawaException, Cities>): Unit
    fun getCity(name: String, cb: ICallback<YawaException, City>): Unit
    fun getCity(id: Int, cb: ICallback<YawaException, City>): Unit
    fun getCity(coordinateInfo: CoordinateInfo, cb: ICallback<YawaException, City>): Unit
    fun getForecast(id: Int, callback: ICallback<YawaException, Forecast>): Unit

    /**
     * Use this to request an image without use of cache.
     *
     * To use cache, use instead the property [imageLoader]
     */
    fun getImage(code: String, cb: ICallback<YawaException, Bitmap>): Unit

    /**
     * ImageLoader property that uses a built in cache provided by volley.
     *
     * This property is to be used with a [NetworkImageView].
     *
     * **Example:**
     *      networkImageView.setImageUrl(uri, imageLoader)
     *
     * See more at [Android API](https://developer.android.com/training/volley/request.html#request-image)
     */
    val imageLoader: ImageLoader

    fun getImageUri(code: String): String

}
