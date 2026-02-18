package com.clearsky.weather.ui.util

import com.clearsky.weather.domain.model.PrecipitationUnit
import com.clearsky.weather.domain.model.TemperatureUnit
import com.clearsky.weather.domain.model.WindSpeedUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class UnitConverterTest {

    @Test
    fun `convertTemperature celsius returns identity`() {
        assertEquals(25.0, UnitConverter.convertTemperature(25.0, TemperatureUnit.CELSIUS), 0.01)
    }

    @Test
    fun `convertTemperature 0C to F is 32`() {
        assertEquals(32.0, UnitConverter.convertTemperature(0.0, TemperatureUnit.FAHRENHEIT), 0.01)
    }

    @Test
    fun `convertTemperature 100C to F is 212`() {
        assertEquals(212.0, UnitConverter.convertTemperature(100.0, TemperatureUnit.FAHRENHEIT), 0.01)
    }

    @Test
    fun `convertTemperature negative celsius to fahrenheit`() {
        assertEquals(-4.0, UnitConverter.convertTemperature(-20.0, TemperatureUnit.FAHRENHEIT), 0.01)
    }

    @Test
    fun `convertWindSpeed kmh returns identity`() {
        assertEquals(50.0, UnitConverter.convertWindSpeed(50.0, WindSpeedUnit.KMH), 0.01)
    }

    @Test
    fun `convertWindSpeed to mph`() {
        assertEquals(31.07, UnitConverter.convertWindSpeed(50.0, WindSpeedUnit.MPH), 0.1)
    }

    @Test
    fun `convertWindSpeed to ms`() {
        assertEquals(13.89, UnitConverter.convertWindSpeed(50.0, WindSpeedUnit.MS), 0.1)
    }

    @Test
    fun `convertWindSpeed to knots`() {
        assertEquals(27.0, UnitConverter.convertWindSpeed(50.0, WindSpeedUnit.KNOTS), 0.1)
    }

    @Test
    fun `convertWindSpeed zero returns zero for all units`() {
        WindSpeedUnit.entries.forEach { unit ->
            assertEquals(0.0, UnitConverter.convertWindSpeed(0.0, unit), 0.01)
        }
    }

    @Test
    fun `convertPrecipitation mm returns identity`() {
        assertEquals(10.0, UnitConverter.convertPrecipitation(10.0, PrecipitationUnit.MM), 0.01)
    }

    @Test
    fun `convertPrecipitation mm to inches`() {
        assertEquals(1.0, UnitConverter.convertPrecipitation(25.4, PrecipitationUnit.INCH), 0.01)
    }

    @Test
    fun `convertPrecipitation zero returns zero`() {
        PrecipitationUnit.entries.forEach { unit ->
            assertEquals(0.0, UnitConverter.convertPrecipitation(0.0, unit), 0.01)
        }
    }

    @Test
    fun `temperatureSymbol returns degree sign for both units`() {
        assertEquals("°", UnitConverter.temperatureSymbol(TemperatureUnit.CELSIUS))
        assertEquals("°", UnitConverter.temperatureSymbol(TemperatureUnit.FAHRENHEIT))
    }

    @Test
    fun `windSpeedLabel returns correct symbol for each unit`() {
        assertEquals("km/h", UnitConverter.windSpeedLabel(WindSpeedUnit.KMH))
        assertEquals("mph", UnitConverter.windSpeedLabel(WindSpeedUnit.MPH))
        assertEquals("m/s", UnitConverter.windSpeedLabel(WindSpeedUnit.MS))
        assertEquals("kn", UnitConverter.windSpeedLabel(WindSpeedUnit.KNOTS))
    }

    @Test
    fun `precipitationLabel returns correct symbol for each unit`() {
        assertEquals("mm", UnitConverter.precipitationLabel(PrecipitationUnit.MM))
        assertEquals("in", UnitConverter.precipitationLabel(PrecipitationUnit.INCH))
    }

    @Test
    fun `convertTemperature large negative value`() {
        assertEquals(-40.0, UnitConverter.convertTemperature(-40.0, TemperatureUnit.FAHRENHEIT), 0.01)
    }

    @Test
    fun `convertWindSpeed large value to ms`() {
        assertEquals(27.78, UnitConverter.convertWindSpeed(100.0, WindSpeedUnit.MS), 0.1)
    }
}
