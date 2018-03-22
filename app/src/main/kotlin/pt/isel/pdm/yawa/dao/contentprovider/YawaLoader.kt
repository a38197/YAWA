package pt.isel.pdm.yawa.dao.contentprovider

import android.app.LoaderManager
import android.content.AsyncTaskLoader
import android.content.Context
import android.content.Loader
import android.os.Bundle
import android.widget.ArrayAdapter
import pt.isel.pdm.yawa.dao.IDataValue
import pt.isel.pdm.yawa.dao.contentprovider.dao.IContentProviderKey

/**
 * Created by ncaro on 11/15/2016.
 */

/**
 * A cursor is by definition a similar to a bidirectional Iterable, so this loader returns a Iterable of the IDataValue chosen
 * */
class YawaLoader<D:IDataValue>(ctx:Context, val key:IContentProviderKey<out D>) : AsyncTaskLoader<List<D>>(ctx){

    //Forces new load on async task loader
    private val mObserver = ForceLoadContentObserver()
    private val notifyDescendants = false

    override fun loadInBackground(): List<D> {
        val cursor = context.contentResolver.query(key.uri, key.projection, key.selection, key.selectionArgs, key.sortOrder)
        try{
            val ret = key.mapList(cursor)
            context.contentResolver.registerContentObserver(key.uri, notifyDescendants, mObserver)
            return ret
        } finally {
            cursor.close()
        }
    }


    companion object{
        const val CITY_LOADER = 1
    }

}

class YawaLoaderCallback<D:IDataValue>(val adp:YawaAdapter<D>, val ctx:Context, val key: IContentProviderKey<out D>) : LoaderManager.LoaderCallbacks<List<D>> {
    override fun onLoaderReset(loader: Loader<List<D>>?) {
        adp.clear()
        adp.notifyDataSetChanged()
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<D>> {
        return YawaLoader<D>(ctx,key)
    }

    override fun onLoadFinished(loader: Loader<List<D>>?, data: List<D>?) {
        adp.clear()
        adp.addAll(data)
        adp.notifyDataSetChanged()
    }
}

abstract class YawaAdapter<D>(ctx:Context, resource:Int) : ArrayAdapter<D>(ctx,resource){
    constructor(context: Context, resource: Int, objects: List<D>) : this(context, resource){
        addAll(objects)
    }
}