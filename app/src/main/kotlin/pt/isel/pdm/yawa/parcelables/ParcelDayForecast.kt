package pt.isel.pdm.yawa.parcelables

import android.os.Parcel
import android.os.Parcelable
import pt.isel.pdm.yawa.model.CityForecast
import pt.isel.pdm.yawa.model.DayForecast

/**
 * Created by Angelo Mestre on 29/10/2016.
 */

class ParcelDayForecast() : Parcelable {

    var dayForecast: DayForecast? = null
    var cityForecast: CityForecast? = null

    //Parcelable Structure
    var cityId: String = ""
    var cityName: String = ""
    var weatherDate: String = ""
    var weatherIcon: String = ""
    var weatherTempMin: String = ""
    var weatherTempMax: String = ""
    var windSpeed: String = ""
    var windDegree: String = ""
    var cloudPercentage: String = ""
    var rainLast3h: String? = "0.0"

    constructor(input: Parcel) : this() {
        cityId = input.readString()
        cityName = input.readString()
        weatherDate = input.readString()
        weatherIcon = input.readString()
        weatherTempMin = input.readString()
        weatherTempMax = input.readString()
        windSpeed = input.readString()
        windDegree = input.readString()
        cloudPercentage = input.readString()
        rainLast3h = input.readString()
    }

    constructor(dayForecast: DayForecast, cityForecast: CityForecast) : this(){
        this.dayForecast = dayForecast
        this.cityForecast = cityForecast

        cityId = cityForecast.id.toString()
        cityName = cityForecast.name
        weatherDate = dayForecast.dt_txt
        weatherIcon = dayForecast.weather.elementAt(0).icon
        weatherTempMin = dayForecast.main.temp_min.toString()
        weatherTempMax = dayForecast.main.temp_max.toString()
        windSpeed = dayForecast.wind.speed.toString()
        windDegree = dayForecast.wind.deg.toString()
        cloudPercentage = dayForecast.clouds.all.toString()
        if (dayForecast.rain != null)
            rainLast3h = dayForecast.rain._3h.toString()
    }

    companion object{
        @JvmField val CREATOR: Parcelable.Creator<ParcelDayForecast> = object : Parcelable.Creator<ParcelDayForecast> {
            override fun createFromParcel(i: Parcel): ParcelDayForecast {
                return ParcelDayForecast(i)
            }

            override fun newArray(size: Int): Array<ParcelDayForecast?> {
                return arrayOfNulls<ParcelDayForecast>(size)
            }
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(cityId)
        dest?.writeString(cityName)
        dest?.writeString(weatherDate)
        dest?.writeString(weatherIcon)
        dest?.writeString(weatherTempMin)
        dest?.writeString(weatherTempMax)
        dest?.writeString(windSpeed)
        dest?.writeString(windDegree)
        dest?.writeString(cloudPercentage)
        dest?.writeString(rainLast3h)
    }


}