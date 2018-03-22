package pt.isel.pdm.yawa.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by nuno on 10/29/16.
 */
class TemperatureUnitTest{
    @Test(expected = IllegalArgumentException::class)
    fun testKelvin(){
        val temp = 204.35
        assertEquals(-68.8,TemperatureUnit.Kelvin.convertTo(temp, TemperatureUnit.Celsius), tempDelta)
        assertEquals(temp,TemperatureUnit.Kelvin.convertTo(temp, TemperatureUnit.Kelvin), tempDelta)
        assertEquals(-91.84,TemperatureUnit.Kelvin.convertTo(temp, TemperatureUnit.Fahrenheit), tempDelta)

        val negTemp = -204.35
        assertEquals(-68.8,TemperatureUnit.Kelvin.convertTo(negTemp, TemperatureUnit.Celsius), tempDelta)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCelcius(){
        val temp = 204.35
        assertEquals(temp,TemperatureUnit.Celsius.convertTo(temp, TemperatureUnit.Celsius), tempDelta)
        assertEquals(477.5,TemperatureUnit.Celsius.convertTo(temp, TemperatureUnit.Kelvin), tempDelta)
        assertEquals(399.83,TemperatureUnit.Celsius.convertTo(temp, TemperatureUnit.Fahrenheit), tempDelta)

        val negTemp = -204.35
        assertEquals(negTemp,TemperatureUnit.Celsius.convertTo(negTemp, TemperatureUnit.Celsius), tempDelta)
        assertEquals(68.8,TemperatureUnit.Celsius.convertTo(negTemp, TemperatureUnit.Kelvin), tempDelta)
        assertEquals(-335.83,TemperatureUnit.Celsius.convertTo(negTemp, TemperatureUnit.Fahrenheit), tempDelta)

        assertEquals(0.0,TemperatureUnit.Celsius.convertTo(-500.0, TemperatureUnit.Kelvin), tempDelta)
    }

    @Test
    fun testFahrenheit(){
        val temp = 204.35
        assertEquals(95.75,TemperatureUnit.Fahrenheit.convertTo(temp, TemperatureUnit.Celsius), tempDelta)
        assertEquals(368.9,TemperatureUnit.Fahrenheit.convertTo(temp, TemperatureUnit.Kelvin), tempDelta)
        assertEquals(temp,TemperatureUnit.Fahrenheit.convertTo(temp, TemperatureUnit.Fahrenheit), tempDelta)

        val negTemp = -204.35
        assertEquals(-131.30,TemperatureUnit.Fahrenheit.convertTo(negTemp, TemperatureUnit.Celsius), tempDelta)
        assertEquals(141.84,TemperatureUnit.Fahrenheit.convertTo(negTemp, TemperatureUnit.Kelvin), tempDelta)
        assertEquals(negTemp,TemperatureUnit.Fahrenheit.convertTo(negTemp, TemperatureUnit.Fahrenheit), tempDelta)
    }

    companion object{
        val tempDelta = 0.01
    }
}