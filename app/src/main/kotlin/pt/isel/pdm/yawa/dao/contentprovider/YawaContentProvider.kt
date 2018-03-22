package pt.isel.pdm.yawa.dao.contentprovider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.util.Log
import pt.isel.pdm.yawa.dao.contentprovider.dao.DbSpec
import pt.isel.pdm.yawa.dao.contentprovider.dao.DbTable
import pt.isel.pdm.yawa.dao.contentprovider.sql.DbHelper
import pt.isel.pdm.yawa.model.exception.YawaException
import java.util.*

const val TAG = "YawaContentProvider"

/**
 * It was decided not to specify the full URI (with item id) in order to facilitate URI manipulation.
 * This simplification avoids problems like updating an URI with ID X, would produce the same results with updating its collection
 * with an ID constraint to the same value, but would notify different listeners
 * */
class YawaContentProvider : ContentProvider(){

    lateinit var db : DbHelper

    override fun insert(uri: Uri?, values: ContentValues?): Uri {
        Log.d(TAG, "Inserting item on URI $uri")
        val ret: Long = db.writableDatabase.insert(
                getTable(uri).name,
                null,
                values
        )
        context.contentResolver.notifyChange(uri, null)
        return ContentUris.withAppendedId(uri, ret)
    }

    override fun query(uri: Uri?, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor {
        Log.d(TAG, "Querying items on URI $uri with selection $selection and args ${Arrays.toString(selectionArgs)}")
        return db.readableDatabase.query(
                getTable(uri).name,
                projection,
                selection,
                selectionArgs,
                null, null,
                sortOrder
        )
    }

    override fun onCreate(): Boolean {
        db = DbHelper(context)
        return true
    }

    override fun update(uri: Uri?, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        Log.d(TAG, "Updating items on URI $uri with selection $selection and args ${Arrays.toString(selectionArgs)}")
        val ret = db.writableDatabase.update(
                getTable(uri).name,
                values,
                selection,
                selectionArgs
        )
        if(ret > 0)
            context.contentResolver.notifyChange(uri, null)

        return ret
    }

    override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<out String>?): Int {
        Log.d(TAG, "Deleting items on URI $uri with selection $selection and args ${Arrays.toString(selectionArgs)}")
        val ret = db.writableDatabase.delete(
                getTable(uri).name,
                selection,
                selectionArgs
        )
        if(ret > 0)
            context.contentResolver.notifyChange(uri, null)

        return ret
    }

    override fun getType(uri: Uri?): String {
        return when(matcher.match(uri)){
            CITY -> YawaContract.City.CONTENT_TYPE
            FORECAST -> YawaContract.Forecast.CONTENT_TYPE
            else -> badUri(uri)
        }
    }

    companion object {
        val matcher = UriMatcher(UriMatcher.NO_MATCH)
        const val CITY = 1
        const val FORECAST = 2
        //see class documentation for an URI explanation
        init {
            matcher.addURI(YawaContract.AUTHORITY, YawaContract.City.RESOURCE, CITY)
            matcher.addURI(YawaContract.AUTHORITY, YawaContract.Forecast.RESOURCE, FORECAST)
        }

        private fun getTable(uri: Uri?) : DbTable {
            return when(matcher.match(uri)){
                CITY -> DbSpec.CityTable
                FORECAST -> DbSpec.ForecastTable
                else -> badUri(uri)
            }
        }

    }
}

private fun <T> badUri(uri:Uri?):T{
    throw ContentProviderException("URI [$uri] unknown")
}

open class ContentProviderException : YawaException{

    constructor() : super()
    constructor(msg: String) : super(msg)
    constructor(msg: String, error: Throwable) : super(msg, error)

}