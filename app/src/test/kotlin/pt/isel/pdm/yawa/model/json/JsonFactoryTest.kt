package pt.isel.pdm.yawa.model.json

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import pt.isel.pdm.yawa.model.MetricSystem

/**
 * Created by ncaro on 10/20/2016.
 */
class JsonFactoryTest{

    companion object{
        const val cityJson = """
                            {"coord":
                                {"lon":145.77,"lat":-16.92},
                                "weather":[{"id":803,"main":"Clouds","description":"broken clouds","icon":"04n"}],
                                "base":"cmc stations",
                                "main":{"temp":293.25,"pressure":1019,"humidity":83,"temp_min":289.82,"temp_max":295.37},
                                "wind":{"speed":5.1,"deg":150},
                                "clouds":{"all":75},
                                "rain":{"3h":3},
                                "dt":1435658272,
                                "sys":{"type":1,"id":8166,"message":0.0166,"country":"AU","sunrise":1435610796,"sunset":1435650870},
                                "id":2172797,
                                "name":"Cairns",
                                "cod":200}
                            """

        const val forecastJson = """ {"city":{
                                "id":1851632,"name":"Shuzenji",
                                "coord":{"lon":138.933334,"lat":34.966671},
                                "country":"JP"
                            },
                            "cod":"200",
                            "message":0.0045,
                            "cnt":38,
                            "list":[{
                                    "dt":1406106000,
                                    "main":{
                                        "temp":298.77,
                                        "temp_min":298.77,
                                        "temp_max":298.774,
                                        "pressure":1005.93,
                                        "sea_level":1018.18,
                                        "grnd_level":1005.93,
                                        "humidity":87,
                                        "temp_kf":0.26},
                                    "weather":[{"id":804,"main":"Clouds","description":"overcast clouds","icon":"04d"}],
                                    "clouds":{"all":88},
                                    "wind":{"speed":5.71,"deg":229.501},
                                    "sys":{"pod":"d"},
                                    "dt_txt":"2014-07-23 09:00:00"
                                    }]
                            }"""
    }



    @Test
    fun testCityDeserialize(){

        val city = JsonFactory.getCity(cityJson)
        assertNotNull(city)
        assertEquals(145.77,city.coord.lon, 0.0)
        assertEquals(-16.92,city.coord.lat, 0.0)
        assertEquals(1,city.weather.size)
        assertEquals(803,city.weather.elementAt(0).id)
        assertEquals("Clouds",city.weather.elementAt(0).main)
        assertEquals("broken clouds",city.weather.elementAt(0).description)
        assertEquals("04n",city.weather.elementAt(0).icon)
        assertEquals("cmc stations",city.base)
        assertEquals(293.25,city.main.temp, 0.0)
        assertEquals(1019.0,city.main.pressure, 0.0)
        assertEquals(83,city.main.humidity)
        assertEquals(289.82,city.main.temp_min, 0.0)
        assertEquals(295.37,city.main.temp_max, 0.0)
        assertEquals(5.1,city.wind.speed, 0.0)
        assertEquals(150.0,city.wind.deg, 0.0)
        assertEquals(75,city.clouds.all)
        assertEquals(3.0,city.rain._3h, 0.0)
        assertEquals(1435658272,city.dt)
        assertEquals(1,city.sys.type)
        assertEquals(8166,city.sys.id)
        assertEquals(0.0166,city.sys.message, 0.0)
        assertEquals("AU",city.sys.country)
        assertEquals(1435610796,city.sys.sunrise)
        assertEquals(1435650870,city.sys.sunset)
        assertEquals(2172797,city.id)
        assertEquals("Cairns",city.name)
        assertEquals(200,city.cod)
    }



    @Test
    fun testForecastDeserialize(){

        val forecast = JsonFactory.getForecast(forecastJson)
        assertNotNull(forecast)
        assertEquals(1851632, forecast.city.id)
        assertEquals("Shuzenji", forecast.city.name)
        assertEquals(138.933334, forecast.city.coord.lon, 0.0)
        assertEquals(34.966671, forecast.city.coord.lat, 0.0)
        assertEquals("JP", forecast.city.country)
        assertEquals(0.0045, forecast.message, 0.0)
        assertEquals("200", forecast.cod)
        assertEquals(38, forecast.cnt)
        assertEquals(1, forecast.list.size)
        val cf = forecast.list.elementAt(0)
        assertEquals(1406106000, cf.dt)
        assertEquals(298.77, cf.main.temp, 0.0)
        assertEquals(298.77, cf.main.temp_min, 0.0)
        assertEquals(298.774, cf.main.temp_max, 0.0)
        assertEquals(1005.93, cf.main.pressure, 0.0)
        assertEquals(1018.18, cf.main.sea_level, 0.0)
        assertEquals(1005.93, cf.main.grnd_level, 0.0)
        assertEquals(87, cf.main.humidity)
        assertEquals(1, cf.weather.size)
        val weather = cf.weather.elementAt(0)
        assertEquals(804, weather.id)
        assertEquals("Clouds", weather.main)
        assertEquals("overcast clouds", weather.description)
        assertEquals("04d", weather.icon)
        assertEquals(88, cf.clouds.all)
        assertEquals(5.71, cf.wind.speed,0.0)
        assertEquals(229.501, cf.wind.deg,0.0)
        assertEquals("d", cf.sys.pod)
        assertEquals("2014-07-23 09:00:00", cf.dt_txt)
    }

    @Test
    fun testMissingInfoIsNull(){
        val city = JsonFactory.getCity(cityJson)
        assertEquals(null, city.snow)
    }

    @Test
    fun testCitySerialize(){
        val city = JsonFactory.getCity(cityJson)
        val citySerialized = JsonFactory.jsonCity(city)
        val cityClone = JsonFactory.getCity(citySerialized)
        assertEquals(city, cityClone)
    }

    @Test
    fun testCityWithMetricSystemSerialize(){
        val city = JsonFactory.getCity(cityJson)
        city.main.metric_system = MetricSystem.imperial
        val citySerialized = JsonFactory.jsonCity(city)
        val cityClone = JsonFactory.getCity(citySerialized)
        assertEquals(MetricSystem.imperial, cityClone.main.metric_system)
    }

    @Test
    fun testForecastSerialize(){
        val forecast = JsonFactory.getForecast(forecastJson)
        val forecastSerialized = JsonFactory.jsonForecast(forecast)
        val forecastClone = JsonFactory.getForecast(forecastSerialized)
        assertEquals(forecast, forecastClone)
    }


}