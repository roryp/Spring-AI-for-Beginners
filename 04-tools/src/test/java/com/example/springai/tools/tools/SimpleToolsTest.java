package com.example.springai.tools.tools;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Simple beginner-friendly tests demonstrating tool functionality.
 * <p>
 * These tests focus on:
 * 1. Individual tool method behavior
 * 2. Parameter handling
 * 3. Conceptual tool chaining (how one tool's output feeds into another)
 * <p>
 * Note: Real tool chaining happens when the AI model decides which tools to call.
 * These tests demonstrate the tool logic independently.
 */
@DisplayName("Module 04 - Tools Tests")
class SimpleToolsTest {

    // ===== WeatherTool Tests =====

    @Test
    @DisplayName("Should get current weather for a location")
    void shouldGetCurrentWeather() {
        // given
        WeatherTool weatherTool = new WeatherTool();
        String location = "Paris";

        // when
        String weatherResult = weatherTool.getCurrentWeather(location);

        // then
        assertThat(weatherResult)
                .isNotNull()
                .contains(location)
                .containsPattern("\\d+°C") // Contains temperature like "25°C"
                .containsAnyOf("sunny", "cloudy", "rainy");
    }

    @Test
    @DisplayName("Should get weather forecast for multiple days")
    void shouldGetWeatherForecast() {
        // given
        WeatherTool weatherTool = new WeatherTool();
        String location = "Tokyo";
        int days = 3;

        // when
        String forecastResult = weatherTool.getWeatherForecast(location, days);

        // then
        assertThat(forecastResult)
                .isNotNull()
                .contains(location)
                .contains("Day 1:")
                .contains("Day 2:")
                .contains("Day 3:")
                .doesNotContain("Day 4:"); // Only 3 days requested
    }

    @Test
    @DisplayName("Should handle 7-day forecast (maximum)")
    void shouldHandleMaximumForecastDays() {
        // given
        WeatherTool weatherTool = new WeatherTool();
        String location = "London";
        int maxDays = 7;

        // when
        String forecastResult = weatherTool.getWeatherForecast(location, maxDays);

        // then
        assertThat(forecastResult)
                .contains("Day 7:") // Should include all 7 days
                .doesNotContain("Day 8:");
    }

    // ===== TemperatureTool Tests =====

    @Test
    @DisplayName("Should convert Celsius to Fahrenheit")
    void shouldConvertCelsiusToFahrenheit() {
        // given
        TemperatureTool tempTool = new TemperatureTool();
        double celsius = 25.0;

        // when
        String result = tempTool.celsiusToFahrenheit(celsius);

        // then
        assertThat(result)
                .isNotNull()
                .containsPattern("25[.,]0°C") // Handles both 25.0 and 25,0 (locale-specific)
                .containsPattern("77[.,]0°F"); // 25°C = 77°F
    }

    @Test
    @DisplayName("Should convert Fahrenheit to Celsius")
    void shouldConvertFahrenheitToCelsius() {
        // given
        TemperatureTool tempTool = new TemperatureTool();
        double fahrenheit = 77.0;

        // when
        String result = tempTool.fahrenheitToCelsius(fahrenheit);

        // then
        assertThat(result)
                .containsPattern("77[.,]0°F")
                .containsPattern("25[.,]0°C"); // 77°F = 25°C
    }

    @Test
    @DisplayName("Should convert Celsius to Kelvin")
    void shouldConvertCelsiusToKelvin() {
        // given
        TemperatureTool tempTool = new TemperatureTool();
        double celsius = 0.0;

        // when
        String result = tempTool.celsiusToKelvin(celsius);

        // then
        assertThat(result)
                .containsPattern("0[.,]0°C")
                .containsPattern("273[.,]15 K"); // 0°C = 273.15K (absolute zero offset)
    }

    @Test
    @DisplayName("Should convert Kelvin to Celsius")
    void shouldConvertKelvinToCelsius() {
        // given
        TemperatureTool tempTool = new TemperatureTool();
        double kelvin = 273.15;

        // when
        String result = tempTool.kelvinToCelsius(kelvin);

        // then
        assertThat(result)
                .containsPattern("273[.,]15 K")
                .containsPattern("0[.,]0°C");
    }

    @Test
    @DisplayName("Should convert Fahrenheit to Kelvin")
    void shouldConvertFahrenheitToKelvin() {
        // given
        TemperatureTool tempTool = new TemperatureTool();
        double fahrenheit = 32.0; // Freezing point of water

        // when
        String result = tempTool.fahrenheitToKelvin(fahrenheit);

        // then
        assertThat(result)
                .containsPattern("32[.,]0°F")
                .containsPattern("273[.,]15 K"); // 32°F = 0°C = 273.15K
    }

    @Test
    @DisplayName("Should convert Kelvin to Fahrenheit")
    void shouldConvertKelvinToFahrenheit() {
        // given
        TemperatureTool tempTool = new TemperatureTool();
        double kelvin = 273.15; // 0°C

        // when
        String result = tempTool.kelvinToFahrenheit(kelvin);

        // then
        assertThat(result)
                .containsPattern("273[.,]15 K")
                .containsPattern("32[.,]0°F"); // 273.15K = 0°C = 32°F
    }

    @Test
    @DisplayName("Should reject negative Kelvin temperature")
    void shouldRejectNegativeKelvin() {
        // given
        TemperatureTool tempTool = new TemperatureTool();
        double invalidKelvin = -10.0;

        // when/then
        assertThatThrownBy(() -> tempTool.kelvinToCelsius(invalidKelvin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("absolute zero");

        assertThatThrownBy(() -> tempTool.kelvinToFahrenheit(invalidKelvin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("absolute zero");
    }

    // ===== Tool Chaining Concept Tests =====

    @Test
    @DisplayName("Should demonstrate weather-to-temperature tool chaining concept")
    void shouldDemonstrateToolChainingConcept() {
        // This test shows how tool chaining would work conceptually:
        // Step 1: Get weather (returns temperature in Celsius)
        // Step 2: Convert that temperature to Fahrenheit
        //
        // In a real AI agent, the model would decide to call these tools sequentially.
        // Here we demonstrate the manual flow for educational purposes.

        // given
        WeatherTool weatherTool = new WeatherTool();
        TemperatureTool tempTool = new TemperatureTool();

        // Step 1: Get current weather (AI decides to call this tool)
        String weatherResult = weatherTool.getCurrentWeather("Seattle");

        // Extract the temperature from weather result (simulating what an LLM would do)
        // Weather format: "The weather in Seattle is currently 22°C and sunny."
        assertThat(weatherResult)
                .as("Weather tool should return temperature in Celsius")
                .containsPattern("\\d+°C");

        // Step 2: AI recognizes temperature is in Celsius, decides to convert to Fahrenheit
        // For this test, we'll use a known temperature
        double celsiusTemp = 22.0;
        String conversionResult = tempTool.celsiusToFahrenheit(celsiusTemp);

        // then
        assertThat(conversionResult)
                .as("Temperature tool should convert Celsius to Fahrenheit")
                .containsPattern("22[.,]0°C")
                .containsPattern("71[.,]6°F");

        // This demonstrates the chaining concept:
        // weatherTool.getCurrentWeather() → returns "22°C"
        // → LLM extracts "22" → tempTool.celsiusToFahrenheit(22)
        // → returns "22.0°C = 71.6°F"
    }

    @Test
    @DisplayName("Should demonstrate forecast-to-temperature chaining concept")
    void shouldDemonstrateForecastChainingConcept() {
        // Another chaining example:
        // Step 1: Get 3-day forecast (returns temperatures in Celsius)
        // Step 2: Convert each day's temperature to Fahrenheit
        //
        // This shows how multiple tool calls can work together.

        // given
        WeatherTool weatherTool = new WeatherTool();
        TemperatureTool tempTool = new TemperatureTool();

        // Step 1: Get forecast
        String forecastResult = weatherTool.getWeatherForecast("Boston", 3);

        // Verify forecast contains multiple temperature readings
        assertThat(forecastResult)
                .as("Forecast should contain multiple days with Celsius temperatures")
                .contains("Day 1:")
                .contains("Day 2:")
                .contains("Day 3:")
                .containsPattern("\\d+°C");

        // Step 2: Convert a sample temperature (simulating what an LLM would do)
        String conversionResult = tempTool.celsiusToFahrenheit(20.0);

        assertThat(conversionResult)
                .as("Temperature conversion should produce valid Fahrenheit")
                .containsPattern("20[.,]0°C")
                .containsPattern("68[.,]0°F"); // 20°C = 68°F
    }
}
