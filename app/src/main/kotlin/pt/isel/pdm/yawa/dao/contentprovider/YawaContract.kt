package pt.isel.pdm.yawa.dao.contentprovider

import android.content.ContentResolver
import android.net.Uri
import android.provider.BaseColumns

/**
 * Created by nuno on 11/12/16.
 */

object YawaContract {

    val AUTHORITY = "pt.isel.pdm.yawa.provider"

    val CONTENT_URI = Uri.parse("content://" + AUTHORITY)

    val MEDIA_BASE_SUBTYPE = "/vnd.yawa."

    val STAMP = "stamp"
    val DATA = "serialized_data"

    object City : BaseColumns {
        val RESOURCE = "city"

        val CONTENT_URI = Uri.withAppendedPath(
                YawaContract.CONTENT_URI,
                RESOURCE)

        val CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE +
                        MEDIA_BASE_SUBTYPE + RESOURCE

        val CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE +
                        MEDIA_BASE_SUBTYPE + RESOURCE

        val CITY_NAME = "city_name"
        val STAMP = YawaContract.STAMP
        val DATA = YawaContract.DATA

        val SELECT_ALL = arrayOf(BaseColumns._ID, CITY_NAME, STAMP, DATA)

        val DEFAULT_SORT_ORDER = CITY_NAME + " ASC"
    }

    object Forecast : BaseColumns {
        val RESOURCE = "forecast"

        val CONTENT_URI = Uri.withAppendedPath(
                YawaContract.CONTENT_URI,
                RESOURCE)

        val CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE +
                        MEDIA_BASE_SUBTYPE + RESOURCE

        val CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE +
                        MEDIA_BASE_SUBTYPE + RESOURCE

        val CITY_ID = "city_id"
        val STAMP = YawaContract.STAMP
        val DATA = YawaContract.DATA

        val SELECT_ALL = arrayOf(BaseColumns._ID, CITY_ID, STAMP, DATA)

        val DEFAULT_SORT_ORDER = CITY_ID + " ASC"
    }

}