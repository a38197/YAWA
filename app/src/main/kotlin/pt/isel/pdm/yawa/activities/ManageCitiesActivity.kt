package pt.isel.pdm.yawa.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleAdapter
import pt.isel.pdm.yawa.R
import pt.isel.pdm.yawa.model.CityPref
import pt.isel.pdm.yawa.model.ManageCities

class ManageCitiesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_cities)

        draw()

    }

    override fun onResume() {
        super.onResume()
        draw()
    }

    private fun draw() {
        val listview = findViewById(R.id.cities_list) as ListView

        val from = arrayOf("cityName")
        val to = intArrayOf(android.R.id.text1)
        val data = ManageCities(this).getCities().map {
            mapOf(
                    Pair("cityName", it.name + ", " + it.country),
                    Pair("city", it))
        }

        listview.adapter = SimpleAdapter(
                this,
                data,
                android.R.layout.simple_list_item_1,
                from,
                to
        )

        listview.onItemClickListener = clickListener(listview)
    }


    fun clickListener(listview: ListView): AdapterView.OnItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
        val city = (listview.getItemAtPosition(position) as Map<String, *>)["city"] as CityPref

        AlertDialog.Builder(listview.context)
                .setMessage(getString(R.string.delete_message))
                .setTitle(getString(R.string.delete_title))
                .setPositiveButton(getString(R.string.generic_yes),
                        { dialogInterface, i ->
                            ManageCities(this).removeCity(city)
                            draw()
                        })
                .setNegativeButton(getString(R.string.generic_no),
                        { dialogInterface, i -> /* Do nothing */ })
                .create()
                .show()
    }


    fun btnAddCity(view: View) {
        startActivity(Intent(this, AddCityActivity::class.java))
    }
}
