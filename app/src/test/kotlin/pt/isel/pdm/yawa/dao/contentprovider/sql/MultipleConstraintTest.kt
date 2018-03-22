package pt.isel.pdm.yawa.dao.contentprovider.sql

import org.junit.Assert.assertEquals
import org.junit.Test
import pt.isel.pdm.yawa.dao.contentprovider.dao.DbSpec

/**
 * Created by nuno on 11/13/16.
 */
class MultipleConstraintTest {
    @Test
    fun getSelectionString() {
        val str = with(ConstraintBuilder){
            constraint(ConstraintOperation.And, arrayOf(
                  value(DbSpec.CityTable.columns.id, 1, ConstraintValue.Equals),
                  value(DbSpec.CityTable.CityColumns.cityName, "", ConstraintValue.Lesser),
                  value(DbSpec.CityTable.CityColumns.serializedData, 1, ConstraintValue.GreaterOrEquals)
            ))
        }.getSelectionString()
        assertEquals(" (  _id  =  ?  and  city_name  <  ?  and  serialized_data  >=  ?  ) ", str)
    }

    @Test
    fun multipleDegreeTest() {
        val str = with(ConstraintBuilder){
            constraint(ConstraintOperation.And, arrayOf(
                    value(DbSpec.CityTable.CityColumns.id, 1, ConstraintValue.Equals),
                    constraint(ConstraintOperation.Or, arrayOf(
                            value(DbSpec.CityTable.CityColumns.cityName, "", ConstraintValue.Lesser),
                            value(DbSpec.CityTable.CityColumns.serializedData, 1, ConstraintValue.GreaterOrEquals)
                    ))
            ))
        }.getSelectionString()
        assertEquals(" (  _id  =  ?  and  (  city_name  <  ?  or  serialized_data  >=  ?  )  ) ", str)
    }
}