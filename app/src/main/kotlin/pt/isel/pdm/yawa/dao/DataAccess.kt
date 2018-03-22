package pt.isel.pdm.yawa.dao

import android.os.AsyncTask
import pt.isel.pdm.yawa.model.ICallback
import pt.isel.pdm.yawa.model.exception.YawaException

/**
 * Created by ncaro on 10/11/2016.
 */

/*
* Provides a abstraction layer for the storage mechanism.
* This interface already provides a simple implementation through the use of Async Tasks.
* */
//TODO check projection out parameters. would avoid type casts
interface IDataAccess<KEY,VAL>
where 	KEY : IDataKey, VAL : IDataValue {

    /*
    * Synchronous get
    * */
    fun get(k:KEY):VAL?
    /*
    * Asynchronous get with ICallback
    * */
    fun get(k:KEY, callback:ICallback<YawaException,VAL?>):Unit{
        safeAsyncTask<VAL?>(callback){
            get(k)
        }.execute()
    }

    /**
     * Synchronous get collection that match the key
     * */
    fun getAll(partialKey: KEY) : Collection<VAL>
    /*
    * Asynchronous get with ICallback
    * */
    fun getAll(k:KEY, callback:ICallback<YawaException,Collection<VAL>>):Unit{
        safeAsyncTask<Collection<VAL>>(callback){
            getAll(k)
        }.execute()
    }

    /*
    * Synchronous store
    * */
    fun addOrUpdate(k:KEY, v:VAL):Unit
    /*
    * Asynchronous store with ICallback
    * */
    fun addOrUpdate(k:KEY, v:VAL, callback:ICallback<YawaException,Unit>):Unit{
        safeAsyncTask<Unit>(callback){
            addOrUpdate(k,v)
        }.execute()
    }

    /*
    * Synchronous delete
    * */
    fun delete(k:KEY):Unit
    /*
    * Asynchronous delete with ICallback
    * */
    fun delete(k:KEY, callback:ICallback<YawaException,Unit>):Unit{
        safeAsyncTask<Unit>(callback){
            delete(k)
        }.execute()
    }

}

interface IDataKey{}
interface IDataValue{}

/*
* Returns a simple task with the aid of closure. inlined to avoid function object clutter
* */
private inline fun <RETURN> simpleAsyncTask(crossinline background:()->RETURN, crossinline post:(RETURN)->Unit)
        = object : AsyncTask<Unit,Unit,RETURN>(){
            override fun doInBackground(vararg params: Unit?): RETURN = background()

            override fun onPostExecute(result: RETURN) = post(result)
        }

/*
* Returns a simple task with the aid of closure. inlined to avoid function object clutter
* */
private inline fun <RETURN> simpleAsyncTask(crossinline background:()->RETURN)
        = object : AsyncTask<Unit,Unit,RETURN>(){
    override fun doInBackground(vararg params: Unit?): RETURN = background()
}

/*
* Returns an async task protected for exceptions
* */
private fun <RETURN> safeAsyncTask(callback: ICallback<YawaException, RETURN>, background: () -> RETURN)
        = object : AsyncTask<Unit,Unit,RETURN>(){

    var exception:YawaException? = null

    override fun doInBackground(vararg params: Unit?): RETURN? {
        try {
            return background()
        } catch (ex:Exception){
            exception = YawaException("Error running background work",ex)
            return null
        }
    }

    override fun onPostExecute(result: RETURN) {
        val ex = exception
        if(ex!=null)
            callback.onError(ex)
        else
            callback.onData(result)
    }
}