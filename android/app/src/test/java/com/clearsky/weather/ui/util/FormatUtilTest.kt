package com.clearsky.weather.ui.util

import com.clearsky.weather.domain.model.PrecipitationUnit
import com.clearsky.weather.domain.model.TemperatureUnit
import com.clearsky.weather.domain.model.TimeFormat
import com.clearsky.weather.domain.model.WindSpeedUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FormatUtilTest {

    @Test
    fun `formatTemperature celsius rounds correctly`() {
        val result = FormatUtil.formatTemperature(25.6, TemperatureUnit.CELSIUS)
        assertEquals("26\u00B0", result)
    }

    @Test
    fun `formatTemperature fahrenheit converts and rounds`() {
        val result = FormatUtil.formatTemperature(0.0, TemperatureUnit.FAHRENHEIT)
        assertEquals("32\u00B0", result)
    }

    @Test
    fun `formatTemperature negative rounds correctly`() {
        val result = FormatUtil.formatTemperature(-5.4, TemperatureUnit.CELSIUS)
        assertEquals("-5\u00B0", result)
    }

    @Test
    fun `formatWindDirection returns correct cardinal directions`() {
        assertEquals("N", FormatUtil.formatWindDirection(0))
        assertEquals("N", FormatUtil.formatWindDirection(360))
        assertEquals("NE", FormatUtil.formatWindDirection(45))
        assertEquals("E", FormatUtil.formatWindDirection(90))
        assertEquals("SE", FormatUtil.formatWindDirection(135))
        assertEquals("S", FormatUtil.formatWindDirection(180))
        assertEquals("SW", FormatUtil.formatWindDirection(225))
        assertEquals("W", FormatUtil.formatWindDirection(270))
        assertEquals("NW", FormatUtil.formatWindDirection(315))
    }

    @Test
    fun `formatVisibility returns km for large values`() {
        assertEquals("10 km", FormatUtil.formatVisibility(10000))
    }

    @Test
    fun `formatVisibility returns meters for small values`() {
        assertEquals("500 m", FormatUtil.formatVisibility(500))
    }

    @Test
    fun `formatPercentage formats correctly`() {
        assertEquals("75%", FormatUtil.formatPercentage(75))
        assertEquals("0%", FormatUtil.formatPercentage(0))
        assertEquals("100%", FormatUtil.formatPercentage(100))
    }

    @Test
    fun `uvIndexLevel returns correct levels`() {
        assertEquals("Low", FormatUtil.uvIndexLevel(1.0))
        assertEquals("Low", FormatUtil.uvIndexLevel(2.0))
        assertEquals("Moderate", FormatUtil.uvIndexLevel(3.0))
        assertEquals("Moderate", FormatUtil.uvIndexLevel(5.0))
        assertEquals("High", FormatUtil.uvIndexLevel(6.0))
        assertEquals("High", FormatUtil.uvIndexLevel(7.0))
        assertEquals("Very High", FormatUtil.uvIndexLevel(8.0))
        assertEquals("Very High", FormatUtil.uvIndexLevel(10.0))
        assertEquals("Extreme", FormatUtil.uvIndexLevel(11.0))
    }

    @Test
    fun `formatPressure rounds and adds unit`() {
        assertEquals("1013 hPa", FormatUtil.formatPressure(1013.25))
    }

    @Test
    fun `formatRelativeTime returns just now for recent`() {
        val now = System.currentTimeMillis()
        assertEquals("Just now", FormatUtil.formatRelativeTime(now))
    }

    @Test
    fun `formatRelativeTime returns minutes ago`() {
        val fiveMinAgo = System.currentTimeMillis() - 5 * 60_000
        assertEquals("5m ago", FormatUtil.formatRelativeTime(fiveMinAgo))
    }

    @Test
    fun `formatRelativeTime returns hours ago`() {
        val twoHoursAgo = System.currentTimeMillis() - 2 * 60 * 60_000
        assertEquals("2h ago", FormatUtil.formatRelativeTime(twoHoursAgo))
    }

    @Test
    fun `formatRelativeTime returns days ago`() {
        val twoDaysAgo = System.currentTimeMillis() - 2 * 24 * 60 * 60_000L
        assertEquals("2d ago", FormatUtil.formatRelativeTime(twoDaysAgo))
    }

    @Test
    fun `formatWindSpeed kmh formats correctly`() {
        val result = FormatUtil.formatWindSpeed(15.7, WindSpeedUnit.KMH)
        assertEquals("16 km/h", result)
    }

    @Test
    fun `formatPrecipitation mm formats correctly`() {
        val result = FormatUtil.formatPrecipitation(5.3, PrecipitationUnit.MM)
        assertEquals("5.3 mm", result)
    }

    @Test
    fun `formatDayOfWeek returns Today for current date`() {
        val today = java.time.LocalDate.now().toString()
        assertEquals("Today", FormatUtil.formatDayOfWeek(today))
    }

    @Test
    fun `formatDayOfWeek returns Tomorrow for next day`() {
        val tomorrow = java.time.LocalDate.now().plusDays(1).toString()
        assertEquals("Tomorrow", FormatUtil.formatDayOfWeek(tomorrow))
    }

    @Test
    fun `formatTemperatureValue celsius returns integer string`() {
        assertEquals("26", FormatUtil.formatTemperatureValue(25.6, TemperatureUnit.CELSIUS))
    }

    @Test
    fun `formatTemperatureValue fahrenheit converts correctly`() {
        assertEquals("32", FormatUtil.formatTemperatureValue(0.0, TemperatureUnit.FAHRENHEIT))
    }

    @Test
    fun `formatWindSpeedValue returns integer string`() {
        assertEquals("16", FormatUtil.formatWindSpeedValue(15.7, WindSpeedUnit.KMH))
    }

    @Test
    fun `formatWindSpeedValue mph converts correctly`() {
        assertEquals("31", FormatUtil.formatWindSpeedValue(50.0, WindSpeedUnit.MPH))
    }

    @Test
    fun `formatDate returns month and day`() {
        assertEquals("Jan 15", FormatUtil.formatDate("2024-01-15"))
    }

    @Test
    fun `formatTime H12 format`() {
        val result = FormatUtil.formatTime("2024-01-15T14:30:00", TimeFormat.H12)
        assertTrue(result.contains("2:30") && result.uppercase().contains("PM"))
    }

    @Test
    fun `formatTime H24 format`() {
        val result = FormatUtil.formatTime("2024-01-15T14:30:00", TimeFormat.H24)
        assertEquals("14:30", result)
    }

    @Test
    fun `formatHour H24 format`() {
        val result = FormatUtil.formatHour("2024-01-15T09:00:00", TimeFormat.H24)
        assertEquals("09:00", result)
    }

    @Test
    fun `formatHour H12 format`() {
        val result = FormatUtil.formatHour("2024-01-15T14:00:00", TimeFormat.H12)
        assertTrue(result.contains("2") && result.uppercase().contains("PM"))
    }

    @Test
    fun `formatUvIndex formats with one decimal`() {
        assertEquals("5.0", FormatUtil.formatUvIndex(5.0))
        assertEquals("8.3", FormatUtil.formatUvIndex(8.3))
    }

    @Test
    fun `formatPrecipitation inches converts correctly`() {
        val result = FormatUtil.formatPrecipitation(25.4, PrecipitationUnit.INCH)
        assertEquals("1.0 in", result)
    }

    @Test
    fun `formatDayOfWeek returns day name for other dates`() {
        val futureDate = java.time.LocalDate.now().plusDays(5).toString()
        val result = FormatUtil.formatDayOfWeek(futureDate)
        assertTrue(result != "Today" && result != "Tomorrow")
    }
}
