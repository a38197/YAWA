package pt.isel.pdm.yawa.dao.contentprovider.dao

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.util.Log
import pt.isel.pdm.yawa.dao.IDataAccess
import pt.isel.pdm.yawa.dao.IDataKey
import pt.isel.pdm.yawa.dao.IDataValue
import pt.isel.pdm.yawa.dao.contentprovider.ContentProviderException
import pt.isel.pdm.yawa.dao.contentprovider.YawaContract
import java.util.*

/**
 * Created by nuno on 11/5/16.
 */



/**
 * Observer for anyone listen to content item changes on its URI
 * */
class DaoObserver(handler: Handler) : ContentObserver(handler) {
    /**
     * Constructor that registers for notifications on content resolver and sets the method for unregister
     * */
    constructor(handler: Handler, resolver:ContentResolver, uri:Uri) : this(handler){
        resolver.registerContentObserver(uri, false, this)
        unregister = {resolver.unregisterContentObserver(this@DaoObserver)}
    }

    var onContentChange : ()->Unit = empty
    var unregister : Function0<Unit> = empty
        get() = field
        private set(value){field = value}

    override fun onChange(selfChange: Boolean) {
        onContentChange()
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        onContentChange()
    }

    private companion object{
        val empty : ()->Unit = {}
    }
}

/**
 * Observable DAO value. Observer not parcelable
 * */
interface IObservableValue : IDataValue {
    var observer : DaoObserver?
    //GC collects the reference for the user
    fun finalize():Unit {
        observer?.unregister?.invoke()
    }
}

interface ICursorMapper<out V: IDataValue>{
    fun mapSingle(cursor: Cursor):V
    fun mapList(cursor: Cursor):List<V>
}

interface IContentProviderKey<out V: IDataValue> : IDataKey, ICursorMapper<V> {
    val projection: Array<out String>?
    val selection : String?
    val selectionArgs: Array<out String>?
    val sortOrder: String?
    val contentValues: ContentValues
    val uri: Uri
}

const val TAG = "ContentProviderDao"

class ContentProviderDao<V : IObservableValue>(val context: Context)
    : IDataAccess<IContentProviderKey<V>, V> {

    /**
     * Handler attached to main thread for content observers
     * */
    val mHandler = Handler()

    override fun get(k: IContentProviderKey<V>): V? {
        val list = getExpirableList(k)
        val item = if (!list.isEmpty()) list.first() else return null

        Log.d(TAG, "Cache hit, returning item")
        item.item.observer = DaoObserver(mHandler, context.contentResolver, k.uri)
        return item.item
    }

    private fun getExpirableList(k:IContentProviderKey<V>):MutableList<ExpirableItem>{
        val cursor = context.contentResolver.query(k.uri, k.projection, k.selection, k.selectionArgs, k.sortOrder)
        try{
            if(cursor.count == 0) {
                Log.d(TAG, "Cache miss, returning empty list")
                return mutableListOf()
            }

            val list = ArrayList<ExpirableItem>(cursor.count)
            while (cursor.moveToNext()) {
                val stamp = cursor.getLong(YawaContract.STAMP)
                list.add(ExpirableItem(stamp, k.mapSingle(cursor)))
            }

            return list
        } finally {
            cursor?.close()
        }

    }

    /**
     * Gets a collection on items for a partial key.
     * This implementation returns the same DaoObserver for all the items. Caution is needed when setting {@link DaoObserver#onContentChange}
     * */
    override fun getAll(partialKey: IContentProviderKey<V>): Collection<V> {
        val list = getExpirableList(partialKey)

        //The observer is the same because typically one API call is consumed by the same entity
        if(list.isNotEmpty()) {
            val observer = DaoObserver(mHandler, context.contentResolver, partialKey.uri)
            list.forEach { it.item.observer = observer }
        }
        Log.d(TAG, "Returning list with ${list.size} items")
        return list.map { it.item }
    }

    override fun addOrUpdate(k: IContentProviderKey<V>, v: V) {
        k.contentValues.put(YawaContract.STAMP, System.currentTimeMillis())
        val get = getExpirableList(k)
        if(get.isEmpty()){
            Log.d(TAG, "Inserting item with key $k")
            context.contentResolver.insert(k.uri, k.contentValues)
        } else {
            Log.d(TAG, "Updating item with key $k")
            context.contentResolver.update(k.uri, k.contentValues, k.selection, k.selectionArgs)
        }
    }

    override fun delete(k: IContentProviderKey<V>) {
        Log.d(TAG, "Deleting item with key $k")
        context.contentResolver.delete(k.uri, k.selection, k.selectionArgs)
    }

    private inner class ExpirableItem(val stamp:Long, val item:V)
}

fun Cursor.getString(columnName: String):String{
    return getT(columnName, Cursor::getString)
}

fun Cursor.getLong(columnName: String):Long{
    return getT(columnName, Cursor::getLong)
}

private fun <T> Cursor.getT(columnName:String, func:(Cursor,Int)->T):T{
    for(i in this.columnNames.indices){
        if(this.columnNames[i] == columnName)
            return func(this, i)
    }
    throw ContentProviderException("Expected a column with name ${columnName}")
}