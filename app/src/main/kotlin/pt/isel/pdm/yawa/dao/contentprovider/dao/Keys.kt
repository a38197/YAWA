package pt.isel.pdm.yawa.dao.contentprovider.dao

import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import pt.isel.pdm.yawa.dao.IDataValue
import pt.isel.pdm.yawa.dao.contentprovider.sql.ConstraintBuilder
import pt.isel.pdm.yawa.dao.contentprovider.sql.ConstraintOperation
import pt.isel.pdm.yawa.dao.contentprovider.sql.ConstraintValue
import pt.isel.pdm.yawa.model.City
import pt.isel.pdm.yawa.model.Forecast
import pt.isel.pdm.yawa.model.json.JsonFactory
import java.util.*

/**
 * Created by nuno on 11/12/16.
 */

abstract class ContentProviderKeyBase<V : IDataValue, TBL : DbTable> : IContentProviderKey<V>{

    constructor()

    constructor(item:V) {
        values = fillValues(item)
    }

    protected abstract val table : TBL
    protected abstract fun map(cursor:Cursor): V
    protected abstract fun fillValues(item:V):ContentValues

    override fun mapSingle(cursor: Cursor): V {
        return map(cursor)
    }

    override fun mapList(cursor: Cursor): List<V> {
        val cities = LinkedList<V>()
        if(cursor.count > 0)
            //Cursors start on -1
            while (cursor.moveToNext())
                cities.add(map(cursor))


        return cities
    }

    private var values:ContentValues? = null
    override val contentValues: ContentValues
        get() = values ?: ContentValues()

    override fun toString(): String {
        return StringBuilder()
                .append("Uri = ").append(uri).append("; ")
                .append("Projection = ").append(Arrays.toString(projection)).append("; ")
                .append("Selection = ").append(selection).append("; ")
                .append("SelectionArgs = ").append(Arrays.toString(selectionArgs)).append("; ")
                .append("SortOrder = ").append(sortOrder).append("; ")
                .append("ContentValues = ").append(contentValues).append("; ")
                .toString()
    }

    override val projection: Array<out String>?
        get() = table.projection

    override val sortOrder: String?
        get() = table.sort

    override val uri: Uri
        get() = table.uri
}



class CityContentProviderKey : ContentProviderKeyBase<City, DbSpec.CityTable>{

    constructor(id:Int) {
        val constraint = ConstraintBuilder.value(table.columns.id, id, ConstraintValue.Equals)
        sel = constraint.getSelectionString()
        args = constraint.getSectionArgs()
    }

    constructor(name:String) {
        val const = ConstraintBuilder.value(table.columns.cityName, name, ConstraintValue.Like)
        sel = const.getSelectionString()
        args = const.getSectionArgs()
    }

    constructor(item:City) : super(item){
        fillSelection(item.id, item.name)
    }

    override val table: DbSpec.CityTable
        get() = DbSpec.CityTable

    private fun fillSelection(id:Int, name:String){

        val constraint = with(ConstraintBuilder){
            constraint(ConstraintOperation.And, arrayOf(
                    value(table.columns.id, id, ConstraintValue.Equals),
                    value(table.columns.cityName, name, ConstraintValue.Like)
            ))
        }
        sel = constraint.getSelectionString()
        args = constraint.getSectionArgs()
    }

    private var sel : String? = null
    private var args : Array<out String>? = null

    override fun map(cursor:Cursor): City{
        return JsonFactory.getCity(cursor.getString(table.columns.serializedData.name))
    }

    override val selection: String?
        get() = sel

    override val selectionArgs: Array<out String>?
        get() = args

    override fun fillValues(item: City): ContentValues {
        val values = ContentValues()
        values.put(DbSpec.CityTable.CityColumns.id.name, item.id)
        values.put(DbSpec.CityTable.CityColumns.cityName.name, item.name)
        values.put(DbSpec.CityTable.CityColumns.serializedData.name, JsonFactory.jsonCity(item))
        return values
    }
}

class ForecastContentProviderKey : ContentProviderKeyBase<Forecast, DbSpec.ForecastTable>{
    constructor(cityId:Int) {
        fillSelection(cityId)
    }

    constructor(item:Forecast) : super(item){
        fillSelection(item.city.id)
    }

    private fun fillSelection(cityId:Int){
        val const = ConstraintBuilder.value(table.columns.cityId, cityId, ConstraintValue.Equals)

        sel = const.getSelectionString()
        args = const.getSectionArgs()
    }

    private var sel : String? = null

    private var args : Array<out String>? = null
    override fun map(cursor:Cursor): Forecast{
        return JsonFactory.getForecast(cursor.getString(table.columns.serializedData.name))
    }

    override val table: DbSpec.ForecastTable
        get() = DbSpec.ForecastTable

    override val selection: String?
        get() = sel

    override val selectionArgs: Array<out String>?
        get() = args

    override fun fillValues(item: Forecast): ContentValues {
        val values = ContentValues()
        values.put(table.columns.cityId.name, item.city.id)
        values.put(table.columns.serializedData.name, JsonFactory.jsonForecast(item))
        return values
    }

}