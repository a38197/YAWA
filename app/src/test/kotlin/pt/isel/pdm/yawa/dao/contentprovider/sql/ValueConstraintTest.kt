package pt.isel.pdm.yawa.dao.contentprovider.sql

import org.junit.Assert.assertEquals
import org.junit.Test
import pt.isel.pdm.yawa.dao.contentprovider.dao.DbSpec

/**
 * Created by nuno on 11/13/16.
 */
class ValueConstraintTest {
    @Test
    fun getSelectionString() {
        val selectionString = ValueConstraint(DbSpec.CityTable.CityColumns.id, 1, ConstraintValue.Equals).getSelectionString()
        assertEquals(" _id  =  ? ", selectionString)
    }

}