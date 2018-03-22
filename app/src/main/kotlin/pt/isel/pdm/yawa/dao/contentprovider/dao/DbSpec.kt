package pt.isel.pdm.yawa.dao.contentprovider.dao

import android.net.Uri
import android.provider.BaseColumns
import pt.isel.pdm.yawa.dao.contentprovider.YawaContract

object DbSpec{

    const val DB_NAME = "YawaDatabase"
    const val DB_VERSION = 2

    object CityTable : DbTable {
        override val name = YawaContract.City.RESOURCE

        override val columns: CityColumns
            get() = CityColumns

        object CityColumns : AColumns(){
            val cityName = DbColumn(YawaContract.City.CITY_NAME, DbType.Text)

            override fun getColumns(): Iterable<DbColumn> = listOf(
                    id, stamp, serializedData, cityName
            )
        }

        override val projection: Array<out String>
            get() = YawaContract.City.SELECT_ALL

        override val uri: Uri
            get() = YawaContract.City.CONTENT_URI

        override val sort: String
            get() = YawaContract.City.DEFAULT_SORT_ORDER
    }

    object ForecastTable : DbTable {
        override val name = YawaContract.Forecast.RESOURCE

        override val columns: ForecastColumns
            get() = ForecastColumns

        object ForecastColumns : AColumns(){
            val cityId = DbColumn(YawaContract.Forecast.CITY_ID, DbType.Integer)

            override fun getColumns(): Iterable<DbColumn> = listOf(
                    id, stamp, serializedData, cityId
            )
        }

        override val projection: Array<out String>
            get() = YawaContract.Forecast.SELECT_ALL

        override val sort: String
            get() = YawaContract.Forecast.DEFAULT_SORT_ORDER

        override val uri: Uri
            get() = YawaContract.Forecast.CONTENT_URI
    }

}

interface DbTable{
    val name : String
    val columns : AColumns
    val uri : Uri
    val projection : Array<out String>
    val sort : String
}

abstract class AColumns {
    val id = DbColumn(BaseColumns._ID, DbType.Integer, true)
    val stamp = DbColumn(YawaContract.STAMP, DbType.Integer)
    val serializedData = DbColumn(YawaContract.DATA, DbType.Text)

    abstract fun getColumns():Iterable<DbColumn>
}

class DbColumn(
        val name : String,
        val type : DbType,
        val key : Boolean = false
)

enum class DbType{ Integer, Text }