package pt.isel.pdm.yawa.activities

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ListView
import android.widget.SearchView
import android.widget.SimpleAdapter
import pt.isel.pdm.yawa.R
import pt.isel.pdm.yawa.model
import pt.isel.pdm.yawa.model.*
import pt.isel.pdm.yawa.model.exception.NoCitiesFoundException
import pt.isel.pdm.yawa.model.exception.YawaException
import pt.isel.pdm.yawa.model.network.NetworkErrors
import pt.isel.pdm.yawa.showErrorDialog

private const val citiesKey = "pt.isel.pdm.yawa.activities.citiesKey"

class AddCityActivity : AppCompatActivity() {

    val progress: ProgressDialog by lazy { ProgressDialog(this) }
    var citiesToSave: Cities? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_city_add)

        restoreInstanceState(savedInstanceState)
        searchViewSetProperties()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putParcelable(citiesKey, citiesToSave)
        super.onSaveInstanceState(outState)
    }

    private fun restoreInstanceState(savedInstanceState: Bundle?) {
        citiesToSave = savedInstanceState?.getParcelable<Cities>(citiesKey)
        if (citiesToSave != null) {
            draw(citiesToSave as Cities)
        }
    }

    private fun searchViewSetProperties() {
        val searchview = findViewById(R.id.search_location) as SearchView

        progress.setTitle(getString(R.string.progress_title))
        progress.setMessage(getString(R.string.progress_message))
        progress.setCancelable(true)

        searchview.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(q: String): Boolean {
                return false
            }

            override fun onQueryTextSubmit(cityName: String): Boolean {
                progress.show()
                application.model.getCities(cityName, citiesCallback())
                return false
            }
        })

    }

    private fun citiesCallback(): ICallback<YawaException, Cities> {
        return CbBuilder<YawaException, Cities> {
            progress.dismiss()
            citiesToSave = it;
            draw(it);
        }.onError {
            progress.dismiss()
            Log.e("citiesCallback", "Error getting cities", it)
            val message: String
            if (it is NoCitiesFoundException) {
                message = "${getString(R.string.error_no_cities_found)}."
            } else {
                message = "${getString(R.string.error_cities_data)}. ${NetworkErrors.getNetworkError(this, it)}"
            }

            showErrorDialog(activity = this, message = message)
        }
    }

    private fun draw(cities: Cities) {
        val listview = findViewById(R.id.cities_list) as ListView

        val from = arrayOf("cityName", "coordinates")
        val to = intArrayOf(android.R.id.text1, android.R.id.text2)
        val data = cities.list.map {
            mapOf(
                    Pair("cityName", it.name + ", " + it.sys.country),
                    Pair("coordinates", "Coordinates: " + it.coord.lat + ", " + it.coord.lon),
                    Pair("city", it))
        }

        listview.adapter = SimpleAdapter(
                this,
                data,
                android.R.layout.simple_list_item_2,
                from,
                to)

        listview.setOnItemClickListener { adapterView, view, i, l ->
            val city = (listview.getItemAtPosition(i) as Map<String,*>).get("city") as City
            ManageCities(this).addCity(CityPref(city.id, city.name, city.sys.country))
            finish()
        }

    }

}
