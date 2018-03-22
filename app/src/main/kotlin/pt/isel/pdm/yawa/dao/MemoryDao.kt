package pt.isel.pdm.yawa.dao

import android.util.Log
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by ncaro on 10/22/2016.
 */

internal class MemoryDao<V>() : IDataAccess<IMemoryDaoKey,V> where V : IDataValue{
    private val store : ConcurrentHashMap<IMemoryDaoKey,ExpirableEntry> = ConcurrentHashMap()

    /**
     * Get a data associated with a key. Because the entries are in memory, deletes the expired ones
     * if encountered.
     * */
    override fun get(k: IMemoryDaoKey): V? {
        val entry = store[k]
        return when {
            entry == null -> null
            !entry.isExpired() -> entry.value
            else -> {
                Log.d("MemoryDao", "Entry expired, deleting key $k")
                delete(k)
                return null
            }
        }
    }

    /**
     * Gets all entries that match the key. Does not remove expired entries because it does always a full
     * search, and would render the cleanup useless on the memory DB case
     * */
    override fun getAll(partialKey: IMemoryDaoKey): Collection<V> {
        Log.d("MemoryDao", "Getting data for key ${partialKey}")
        val list = LinkedList<V>()
        for ((key, value) in store){
            if(key.contains(partialKey) && !value.isExpired()){
                list.add(value.value)
            }
        }
        return list
    }

    override fun addOrUpdate(k: IMemoryDaoKey, v: V) {
        Log.d("MemoryDao", "Storing data for key ${k}")
        store[k] = ExpirableEntry(v)
    }

    override fun delete(k: IMemoryDaoKey) {
        Log.d("MemoryDao", "Removing data for key ${k}")
        store.remove(k)
    }

    fun size():Int = store.size

    private inner class ExpirableEntry(val value:V) : IDataValue{
        fun isExpired(): Boolean{
            return false
        }

        override fun toString(): String {
            return value.toString()
        }
    }
}


/**
 * Key for the memory DAO. Keep in mind the keys will be stored on a hash aware structure.
 * */
internal interface IMemoryDaoKey : IDataKey{
    /**
     * Used for partial key matching
     * */
    fun contains(partialKey:IMemoryDaoKey) : Boolean
    /**
     * Implementers should specify the equals constraint for any key
     * */
    override fun equals(other:Any?):Boolean
    /**
     * Must override hashCode due to equals override
     * */
    override fun hashCode():Int
}

