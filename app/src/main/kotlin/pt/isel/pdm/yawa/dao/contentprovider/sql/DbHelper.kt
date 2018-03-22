package pt.isel.pdm.yawa.dao.contentprovider.sql

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import pt.isel.pdm.yawa.dao.contentprovider.dao.DbSpec

const val TAG = "DbHelper"

class DbHelper(context: Context) : SQLiteOpenHelper(context, DbSpec.DB_NAME, null, DbSpec.DB_VERSION){

    override fun onCreate(db: SQLiteDatabase) {
        Log.i(TAG, "Creating database with version ${DbSpec.DB_VERSION}")
        createDatabase(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.i(TAG, "Updating database from version $oldVersion to $newVersion")
        deleteDatabase(db)
        createDatabase(db)
    }

    private fun createDatabase(db: SQLiteDatabase) {
        db.execSQL(createTable(DbSpec.CityTable))
        db.execSQL(createTable(DbSpec.ForecastTable))
    }

    private fun deleteDatabase(db: SQLiteDatabase) {
        db.execSQL(dropTable(DbSpec.CityTable))
        db.execSQL(dropTable(DbSpec.ForecastTable))
    }

}