package pt.isel.pdm.yawa.model

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.preference.PreferenceManager
import com.google.gson.annotations.SerializedName
import pt.isel.pdm.yawa.R
import pt.isel.pdm.yawa.dao.IMemoryDaoKey
import pt.isel.pdm.yawa.dao.contentprovider.dao.DaoObserver
import pt.isel.pdm.yawa.dao.contentprovider.dao.IObservableValue
import java.util.*

/**
 * Created by ncaro on 10/18/2016.
 */

data class CoordinateInfo(val lon: Double, val lat: Double) : Parcelable{

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeDouble(lon)
        dest?.writeDouble(lat)
    }

    override fun describeContents(): Int = 1

    companion object{
        @JvmField
        val CREATOR = object : Parcelable.Creator<CoordinateInfo>{
            override fun createFromParcel(source: Parcel): CoordinateInfo {
                return CoordinateInfo(source.readDouble(), source.readDouble())
            }

            override fun newArray(size: Int): Array<out CoordinateInfo?> {
                return kotlin.arrayOfNulls<CoordinateInfo>(size)
            }
        }
    }
}

data class WeatherInfo(val id: Int,
                       val main: String,
                       val description: String,
                       val icon: String) : Parcelable {

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(id)
        dest?.writeString(main)
        dest?.writeString(description)
        dest?.writeString(icon)
    }

    override fun describeContents(): Int = 1

    companion object{
        @JvmField
        val CREATOR = object : Parcelable.Creator<WeatherInfo>{
            override fun createFromParcel(source: Parcel): WeatherInfo {
                return WeatherInfo(
                        source.readInt(),
                        source.readString(),
                        source.readString(),
                        source.readString()
                )
            }

            override fun newArray(size: Int): Array<out WeatherInfo?> = kotlin.arrayOfNulls<WeatherInfo>(size)

        }
    }
}

private const val metricStr = "metric"
private const val imperialStr = "imperial"
private const val kelvinStr = "kelvin"

fun roundDouble(value:Double, places:Int): Double{
    val pow = Math.pow(10.0, places.toDouble())
    return Math.round(value * pow).toDouble() / pow
}

enum class MetricSystem : Parcelable{
    kelvin {
        override val temperatureUnit = TemperatureUnit.Kelvin
        override val apiString = kelvinStr
    }, metric{
        override val temperatureUnit = TemperatureUnit.Celsius
        override val apiString = metricStr
    }, imperial {
        override val temperatureUnit = TemperatureUnit.Fahrenheit
        override val apiString = imperialStr
    };

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(this.apiString)
    }

    override fun describeContents(): Int = 1

    abstract val temperatureUnit:TemperatureUnit
    abstract val apiString: String

    companion object{

        @JvmField
        val CREATOR = object : Parcelable.Creator<MetricSystem>{
            override fun createFromParcel(source: Parcel?): MetricSystem = fromString(source!!.readString())

            override fun newArray(size: Int): Array<out MetricSystem?> = kotlin.arrayOfNulls(size)
        }

        fun fromString(str:String):MetricSystem{
            return when(str.toLowerCase().trim()){
                metricStr -> metric
                kelvinStr -> kelvin
                imperialStr -> imperial
                else -> throw IllegalArgumentException("Unit not supported $str")
            }
        }
        fun fromPreferences(context:Context):MetricSystem{
            val systemStr = PreferenceManager.getDefaultSharedPreferences(context)
                    .getString(context.getString(R.string.unit_preference_key),
                            context.getString(R.string.temperature_format_default))
            return MetricSystem.Companion.fromString(systemStr)
        }
    }

}

enum class TemperatureUnit {
    Kelvin{
        override val symbol: String = "Kelvin"
        override val metricSystem: MetricSystem = MetricSystem.kelvin

        override fun convertTo(value: Double, unit: TemperatureUnit):Double {
            absoluteZeroKelvin(value)
            return when(unit){
                Kelvin -> value
                Celsius -> value - 273.15
                else -> {
                    return value * (9.0/5.0) - 459.67
                }
            }
        }
    },
    Celsius{
        override val symbol: String = "ºC"
        override val metricSystem: MetricSystem = MetricSystem.metric

        override fun convertTo(value: Double, unit: TemperatureUnit):Double {
            return when(unit){
                Kelvin -> {
                    val temp = value + 273.15
                    return absoluteZeroKelvin(temp)
                }
                Celsius -> value
                else -> value  * (9.0/5.0) + 32.0
            }
        }
    }, Fahrenheit{
        override val symbol: String = "ºF"
        override val metricSystem: MetricSystem = MetricSystem.imperial

        override fun convertTo(value: Double, unit: TemperatureUnit):Double {
            return when(unit){
                Kelvin -> {
                    val temp = (value + 459.67) * (5.0/9.0)
                    return absoluteZeroKelvin(temp)
                }
                Celsius -> (value - 32.0) * (5.0/9.0)
                else -> value
            }
        }
    };

    protected fun absoluteZeroKelvin(value:Double):Double{
        if(value < 0.0) throw IllegalArgumentException("Kelvin cannot convert bellow absolute zero")
        return value
    }
    abstract fun convertTo(value:Double, unit:TemperatureUnit):Double
    abstract val metricSystem:MetricSystem
    abstract val symbol:String

}

enum class SpeedUnit {
    // 1 mph = 0.447 04 m/s.
    metersSec{
        override val symbol: String = "m/s"
        override val metricSystem: MetricSystem = MetricSystem.metric

        override fun convertTo(value: Double, unit: SpeedUnit): Double {
            return when(unit){
                milesHour -> (value / 0.44704)
                else -> value
            }
        }
    }, milesHour{
        override val symbol: String = "mi/h"
        override val metricSystem: MetricSystem = MetricSystem.imperial

        override fun convertTo(value: Double, unit: SpeedUnit): Double {
            return when(unit){
                metersSec -> (value * 0.44704)
                else -> value
            }
        }
    };

    abstract fun convertTo(value:Double, unit:SpeedUnit):Double
    abstract val metricSystem:MetricSystem
    abstract val symbol:String

}

data class MainInfo(var temp: Double,
                    val pressure: Double,
                    val humidity: Int,
                    val temp_min: Double,
                    val temp_max: Double,
                    var metric_system: MetricSystem?,
                    val sea_level: Double,
                    val grnd_level: Double) : Parcelable {
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeDouble(temp)
        dest.writeDouble(pressure)
        dest.writeInt(humidity)
        dest.writeDouble(temp_min)
        dest.writeDouble(temp_max)
        dest.writeParcelable(metric_system, flags)
        dest.writeDouble(sea_level)
        dest.writeDouble(grnd_level)
    }

    override fun describeContents(): Int = 1

    companion object{
        @JvmField
        val CREATOR = object : Parcelable.Creator<MainInfo>{
            override fun createFromParcel(source: Parcel): MainInfo {
                return MainInfo(
                        source.readDouble(),
                        source.readDouble(),
                        source.readInt(),
                        source.readDouble(),
                        source.readDouble(),
                        source.readParcelable(ClassLoader.getSystemClassLoader()),
                        source.readDouble(),
                        source.readDouble()
                )
            }

            override fun newArray(size: Int): Array<out MainInfo?> = kotlin.arrayOfNulls<MainInfo>(size)

        }
    }
}

data class WindInfo(val speed: Double,
                    val deg: Double) : Parcelable {
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeDouble(speed)
        dest.writeDouble(deg)
    }

    override fun describeContents(): Int = 1

    companion object{
        @JvmField
        val CREATOR = object : Parcelable.Creator<WindInfo>{
            override fun createFromParcel(source: Parcel): WindInfo {
                return WindInfo(
                        source.readDouble(),
                        source.readDouble()
                )
            }

            override fun newArray(size: Int): Array<out WindInfo?> = kotlin.arrayOfNulls(size)

        }
    }
}

data class CloudInfo(val all: Int) : Parcelable {
    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(all)
    }

    override fun describeContents(): Int = 1

    companion object{
        @JvmField
        val CREATOR = object : Parcelable.Creator<CloudInfo>{
            override fun newArray(size: Int): Array<out CloudInfo?> = kotlin.arrayOfNulls(size)

            override fun createFromParcel(source: Parcel): CloudInfo {
                return CloudInfo(source.readInt())
            }

        }
    }
}

data class RainInfo(@SerializedName("3h") val _3h: Double) : Parcelable{
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeDouble(_3h)
    }

    override fun describeContents(): Int = 1

    companion object{
        @JvmField
        val CREATOR = object : Parcelable.Creator<RainInfo>{
            override fun createFromParcel(source: Parcel): RainInfo {
                return RainInfo(source.readDouble())
            }

            override fun newArray(size: Int): Array<out RainInfo?> = kotlin.arrayOfNulls(size)

        }
    }
}

data class SnowInfo(@SerializedName("3h") val _3h: Double) : Parcelable{
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeDouble(_3h)
    }

    override fun describeContents(): Int = 1

    companion object{
        @JvmField
        val CREATOR = object : Parcelable.Creator<SnowInfo>{
            override fun createFromParcel(source: Parcel): SnowInfo {
                return SnowInfo(source.readDouble())
            }

            override fun newArray(size: Int): Array<out SnowInfo?> = kotlin.arrayOfNulls(size)

        }
    }
}

data class SysInfo(val type: Int,
                   val id: Int,
                   val message: Double,
                   val country: String,
                   val sunrise: Long,
                   val sunset: Long) : Parcelable{
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(type)
        dest.writeInt(id)
        dest.writeDouble(message)
        dest.writeString(country)
        dest.writeLong(sunrise)
        dest.writeLong(sunset)
    }

    override fun describeContents(): Int = 1

    companion object{
        @JvmField
        val CREATOR = object : Parcelable.Creator<SysInfo>{
            override fun newArray(size: Int): Array<out SysInfo?> = kotlin.arrayOfNulls(size)

            override fun createFromParcel(source: Parcel): SysInfo {
                return SysInfo(
                        source.readInt(),
                        source.readInt(),
                        source.readDouble(),
                        source.readString(),
                        source.readLong(),
                        source.readLong()
                )
            }

        }
    }
}

/**
 * Some of the fields on the API may be null, not sure which
 * */
data class City(val coord: CoordinateInfo,
                val weather: Collection<WeatherInfo>,
                val base: String,
                val main: MainInfo,
                val wind: WindInfo,
                val clouds: CloudInfo,
                val rain: RainInfo,
                val snow: SnowInfo?,
                val dt: Long,
                val sys: SysInfo,
                val id: Int,
                val name: String,
                val cod: Int) : IObservableValue, Parcelable{

    @Transient
    override var observer: DaoObserver? = null

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(coord, flags)
        dest.writeParcelableArray(weather.toTypedArray(), flags)
        dest.writeString(base)
        dest.writeParcelable(main, flags)
        dest.writeParcelable(wind, flags)
        dest.writeParcelable(clouds, flags)
        dest.writeParcelable(rain, flags)
        dest.writeParcelable(snow, flags)
        dest.writeLong(dt)
        dest.writeParcelable(sys, flags)
        dest.writeInt(id)
        dest.writeString(name)
        dest.writeInt(cod)
    }

    override fun describeContents(): Int = 1

    companion object{
        @JvmField
        val CREATOR = object : Parcelable.Creator<City>{
            override fun createFromParcel(source: Parcel): City {
                return City(
                        source.readParcelable(ClassLoader.getSystemClassLoader()),
                        source.readParcelableArray(ClassLoader.getSystemClassLoader()).toCollection(LinkedList<Parcelable>()) as Collection<WeatherInfo>,
                        source.readString(),
                        source.readParcelable(ClassLoader.getSystemClassLoader()),
                        source.readParcelable(ClassLoader.getSystemClassLoader()),
                        source.readParcelable(ClassLoader.getSystemClassLoader()),
                        source.readParcelable(ClassLoader.getSystemClassLoader()),
                        source.readParcelable(ClassLoader.getSystemClassLoader()),
                        source.readLong(),
                        source.readParcelable(ClassLoader.getSystemClassLoader()),
                        source.readInt(),
                        source.readString(),
                        source.readInt()
                )
            }

            override fun newArray(size: Int): Array<out City?> = kotlin.arrayOfNulls(size)

        }
    }
}

data class Cities(val count: Int, val list: Collection<City>): Parcelable {
    override fun describeContents(): Int = 1

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(count)
        dest.writeParcelableArray(list.toTypedArray(), flags)
    }
}

data class CityPref(val id: Int, val name :String, val country: String, var index: Int = 0)

internal data class CityKey(val id: Int = 0, val name: String = "") : IMemoryDaoKey{

    override fun contains(partialKey: IMemoryDaoKey): Boolean {
        if(partialKey is CityKey){
            return partialKey.id == id ||
                    name.startsWith(partialKey.name, true)
        }
        return false;
    }

    override fun equals(other: Any?): Boolean {
        if(other is CityKey){
            return other.id == id && other.name == name
        }
        return false;
    }

    override fun hashCode(): Int = (3 * id.hashCode()) + (7 * name.hashCode()) + (5 * javaClass.hashCode())

}

data class CityForecast(val id: Int,
                        val name: String,
                        val coord: CoordinateInfo,
                        val country: String,
                        val population: Int) : Parcelable{

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeString(name)
        dest.writeParcelable(coord, flags)
        dest.writeString(country)
        dest.writeInt(population)
    }

    override fun describeContents(): Int = 1

    companion object{
        @JvmField
        val CREATOR = object: Parcelable.Creator<CityForecast>{
            override fun newArray(size: Int): Array<out CityForecast?> = kotlin.arrayOfNulls(size)

            override fun createFromParcel(source: Parcel): CityForecast {
                return CityForecast(
                        source.readInt(),
                        source.readString(),
                        source.readParcelable(ClassLoader.getSystemClassLoader()),
                        source.readString(),
                        source.readInt()
                )
            }
        }
    }
}

data class SysForecast(val pod: String) : Parcelable{
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(pod)
    }

    override fun describeContents(): Int = 1

    companion object{
        @JvmField
        val CREATOR = object : Parcelable.Creator<SysForecast>{
            override fun createFromParcel(source: Parcel): SysForecast {
                return SysForecast(source.readString())
            }

            override fun newArray(size: Int): Array<out SysForecast?> = kotlin.arrayOfNulls(size)
        }
    }
}

data class DayForecast(val dt: Long,
                       val main: MainInfo,
                       val weather: Collection<WeatherInfo>,
                       val clouds: CloudInfo,
                       val wind: WindInfo,
                       val rain: RainInfo,
                       val snow: SnowInfo,
                       val sys: SysForecast,
                       val dt_txt: String) : Parcelable{
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(dt)
        dest.writeParcelable(main, flags)
        dest.writeList(weather.toMutableList())
        dest.writeParcelable(clouds, flags)
        dest.writeParcelable(wind, flags)
        dest.writeParcelable(rain, flags)
        dest.writeParcelable(snow, flags)
        dest.writeParcelable(sys, flags)
        dest.writeString(dt_txt)
    }

    override fun describeContents(): Int = 1

    companion object{
        @JvmField
        val CREATOR = object : Parcelable.Creator<DayForecast>{
            override fun newArray(size: Int): Array<out DayForecast?> = kotlin.arrayOfNulls(size)

            override fun createFromParcel(source: Parcel): DayForecast {
                return DayForecast(
                        source.readLong(),
                        source.readParcelable(ClassLoader.getSystemClassLoader()),
                        source.createTypedArrayList(WeatherInfo.CREATOR),
                        source.readParcelable(ClassLoader.getSystemClassLoader()),
                        source.readParcelable(ClassLoader.getSystemClassLoader()),
                        source.readParcelable(ClassLoader.getSystemClassLoader()),
                        source.readParcelable(ClassLoader.getSystemClassLoader()),
                        source.readParcelable(ClassLoader.getSystemClassLoader()),
                        source.readString()
                )
            }
        }
    }
}


data class Forecast(val city: CityForecast,
                    val cod: String,
                    val message: Double,
                    val cnt: Int,
                    val list: Collection<DayForecast>,
                    var metricSystem: MetricSystem?) : IObservableValue, Parcelable{

    @Transient
    override var observer: DaoObserver? = null

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(city, flags)
        dest.writeString(cod)
        dest.writeDouble(message)
        dest.writeInt(cnt)
        dest.writeTypedList(list.toMutableList())
        dest.writeParcelable(metricSystem, flags)
    }

    override fun describeContents(): Int = 1

    companion object{
        @JvmField
        val CREATOR = object : Parcelable.Creator<Forecast> {
            override fun newArray(size: Int): Array<out Forecast?> = kotlin.arrayOfNulls(size)

            override fun createFromParcel(source: Parcel): Forecast {
                return Forecast(
                        source.readParcelable(ClassLoader.getSystemClassLoader()),
                        source.readString(),
                        source.readDouble(),
                        source.readInt(),
                        source.createTypedArrayList(DayForecast.CREATOR),
                        source.readParcelable(ClassLoader.getSystemClassLoader())
                )
            }
        }
    }
}

internal data class ForecastKey(val id:Int) : IMemoryDaoKey{
    override fun contains(partialKey: IMemoryDaoKey): Boolean {
        return equals(partialKey)
    }

    override fun equals(other: Any?): Boolean {
        return other is ForecastKey && other.id == id
    }

    override fun hashCode(): Int {
        return id.hashCode() + (5 * javaClass.hashCode())
    }

}