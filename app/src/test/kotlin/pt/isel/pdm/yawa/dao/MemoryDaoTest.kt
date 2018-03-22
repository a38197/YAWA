package pt.isel.pdm.yawa.dao

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by ncaro on 10/13/2016.
 */
class MemoryDaoTest{

    private val dao = MemoryDao<DaoValue>()

    @Test
    fun testCRUDSync(){
        val key = StringDaoKey("myKey")
        var value = DaoValue(1)

        dao.addOrUpdate(key, value)
        assertEquals(value, dao.get(key))
        assertEquals(value, dao.get(StringDaoKey("myKey")))

        //Update
        value = DaoValue(2)
        dao.addOrUpdate(key, value)
        assertEquals(value, dao.get(key))

        //Delete
        dao.delete(key)
        assertEquals(null, dao.get(key))
    }


    @Test
    fun testPartialKey(){
        val key = StringDaoKey("myKey")
        val value = DaoValue(1)

        dao.addOrUpdate(key, value)
        val all = dao.getAll(StringDaoKey("Key"))
        assertEquals(1, all.size)
        assertEquals(1, all.elementAt(0).value)
    }

}

private class StringDaoKey(val key: String) : IMemoryDaoKey{
    override fun contains(partialKey: IMemoryDaoKey): Boolean {
        if(partialKey !is StringDaoKey){
            return false
        }
        return key.contains(partialKey.key)
    }

    override fun equals(other: Any?): Boolean {
        if(other === this) return true
        if(other === null || other.javaClass!=javaClass) return false

        val mKey = other as StringDaoKey
        return key == mKey.key
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }
}

private class DaoValue(val value:Any) : IDataValue{
    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as DaoValue

        if (value != other.value) return false

        return true
    }
}