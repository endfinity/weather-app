package com.clearsky.weather.ui.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherCodeUtilTest {

    @Test
    fun `getDescription returns correct text for known codes`() {
        assertEquals("Clear sky", WeatherCodeUtil.getDescription(0))
        assertEquals("Mainly clear", WeatherCodeUtil.getDescription(1))
        assertEquals("Partly cloudy", WeatherCodeUtil.getDescription(2))
        assertEquals("Overcast", WeatherCodeUtil.getDescription(3))
        assertEquals("Fog", WeatherCodeUtil.getDescription(45))
        assertEquals("Depositing rime fog", WeatherCodeUtil.getDescription(48))
        assertEquals("Light drizzle", WeatherCodeUtil.getDescription(51))
        assertEquals("Moderate drizzle", WeatherCodeUtil.getDescription(53))
        assertEquals("Dense drizzle", WeatherCodeUtil.getDescription(55))
        assertEquals("Heavy rain", WeatherCodeUtil.getDescription(65))
        assertEquals("Thunderstorm", WeatherCodeUtil.getDescription(95))
        assertEquals("Thunderstorm with heavy hail", WeatherCodeUtil.getDescription(99))
    }

    @Test
    fun `getDescription returns clear sky for unknown code`() {
        assertEquals("Clear sky", WeatherCodeUtil.getDescription(999))
    }

    @Test
    fun `getGradient returns day gradient when isDay true`() {
        val gradient = WeatherCodeUtil.getGradient(0, isDay = true)
        assertEquals(2, gradient.size)
        val nightGradient = WeatherCodeUtil.getGradient(0, isDay = false)
        assertTrue(gradient != nightGradient)
    }

    @Test
    fun `getGradient returns night gradient when isDay false`() {
        val gradient = WeatherCodeUtil.getGradient(0, isDay = false)
        assertEquals(2, gradient.size)
    }

    @Test
    fun `getGradient falls back to code 0 for unknown codes`() {
        val unknownGradient = WeatherCodeUtil.getGradient(999, isDay = true)
        val clearGradient = WeatherCodeUtil.getGradient(0, isDay = true)
        assertEquals(clearGradient, unknownGradient)
    }

    @Test
    fun `getIconName returns day icon when isDay true`() {
        assertEquals("clear_day", WeatherCodeUtil.getIconName(0, isDay = true))
        assertEquals("partly_cloudy_day", WeatherCodeUtil.getIconName(1, isDay = true))
    }

    @Test
    fun `getIconName returns night icon when isDay false`() {
        assertEquals("clear_night", WeatherCodeUtil.getIconName(0, isDay = false))
        assertEquals("partly_cloudy_night", WeatherCodeUtil.getIconName(1, isDay = false))
    }

    @Test
    fun `getIconName same icon day and night for overcast`() {
        assertEquals("overcast", WeatherCodeUtil.getIconName(3, isDay = true))
        assertEquals("overcast", WeatherCodeUtil.getIconName(3, isDay = false))
    }

    @Test
    fun `getEmoji returns correct emoji for each category`() {
        assertEquals("☀️", WeatherCodeUtil.getEmoji(0))
        assertEquals("⛅", WeatherCodeUtil.getEmoji(1))
        assertEquals("⛅", WeatherCodeUtil.getEmoji(2))
        assertEquals("⛅", WeatherCodeUtil.getEmoji(3))
        assertEquals("🌫️", WeatherCodeUtil.getEmoji(45))
        assertEquals("🌫️", WeatherCodeUtil.getEmoji(48))
        assertEquals("🌦️", WeatherCodeUtil.getEmoji(51))
        assertEquals("🌧️", WeatherCodeUtil.getEmoji(61))
        assertEquals("🌨️", WeatherCodeUtil.getEmoji(71))
        assertEquals("🌧️", WeatherCodeUtil.getEmoji(80))
        assertEquals("🌨️", WeatherCodeUtil.getEmoji(85))
        assertEquals("⛈️", WeatherCodeUtil.getEmoji(95))
    }

    @Test
    fun `getEmoji returns fallback for unknown code`() {
        assertEquals("🌤️", WeatherCodeUtil.getEmoji(999))
    }

    @Test
    fun `getCondition returns non-null for all known codes`() {
        val knownCodes = listOf(0, 1, 2, 3, 45, 48, 51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 71, 73, 75, 77, 80, 81, 82, 85, 86, 95, 96, 99)
        knownCodes.forEach { code ->
            val condition = WeatherCodeUtil.getCondition(code)
            assertNotNull("Condition should not be null for code $code", condition)
            assertTrue("Description should not be empty for code $code", condition.description.isNotEmpty())
            assertTrue("Day icon should not be empty for code $code", condition.iconDay.isNotEmpty())
            assertTrue("Night icon should not be empty for code $code", condition.iconNight.isNotEmpty())
            assertEquals("Gradient day should have 2 colors for code $code", 2, condition.gradientDay.size)
            assertEquals("Gradient night should have 2 colors for code $code", 2, condition.gradientNight.size)
        }
    }

    @Test
    fun `snow codes return snow-related descriptions`() {
        assertTrue(WeatherCodeUtil.getDescription(71).contains("snow", ignoreCase = true))
        assertTrue(WeatherCodeUtil.getDescription(73).contains("snow", ignoreCase = true))
        assertTrue(WeatherCodeUtil.getDescription(75).contains("snow", ignoreCase = true))
        assertTrue(WeatherCodeUtil.getDescription(77).contains("snow", ignoreCase = true))
        assertTrue(WeatherCodeUtil.getDescription(85).contains("snow", ignoreCase = true))
        assertTrue(WeatherCodeUtil.getDescription(86).contains("snow", ignoreCase = true))
    }

    @Test
    fun `rain codes return rain-related descriptions`() {
        assertTrue(WeatherCodeUtil.getDescription(61).contains("rain", ignoreCase = true))
        assertTrue(WeatherCodeUtil.getDescription(63).contains("rain", ignoreCase = true))
        assertTrue(WeatherCodeUtil.getDescription(65).contains("rain", ignoreCase = true))
        assertTrue(WeatherCodeUtil.getDescription(80).contains("rain", ignoreCase = true))
        assertTrue(WeatherCodeUtil.getDescription(81).contains("rain", ignoreCase = true))
        assertTrue(WeatherCodeUtil.getDescription(82).contains("rain", ignoreCase = true))
    }

    @Test
    fun `thunderstorm codes return thunderstorm descriptions`() {
        assertTrue(WeatherCodeUtil.getDescription(95).contains("Thunderstorm", ignoreCase = true))
        assertTrue(WeatherCodeUtil.getDescription(96).contains("Thunderstorm", ignoreCase = true))
        assertTrue(WeatherCodeUtil.getDescription(99).contains("Thunderstorm", ignoreCase = true))
    }
}
