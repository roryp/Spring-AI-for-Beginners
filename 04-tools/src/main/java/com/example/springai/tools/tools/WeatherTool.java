package com.example.springai.tools.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import com.example.springai.tools.model.dto.ToolExecutionInfo;

import java.util.List;
import java.util.Random;

/**
 * WeatherTool - Demonstration of AI Function Calling
 * Run: ./start.sh (from module directory, after deploying Azure resources with azd up)
 * 
 * Weather tool demonstrating function calling with Spring AI.
 * In production, this would integrate with a real weather API.
 * 
 * Shows how to create tools that AI agents can discover and call:
 * - @Tool annotation makes methods discoverable by the AI
 * - @ToolParam annotation describes parameters for the AI
 * - Clear descriptions help AI decide when to use the tool
 * 
 * Key Concepts:
 * - @Tool annotation for function calling
 * - Parameter descriptions with @ToolParam
 * - Tool description best practices
 * - Mock vs real API integration patterns
 * 
 * 💡 Ask GitHub Copilot:
 * - "How would I integrate a real weather API like OpenWeatherMap instead of mock data?"
 * - "What makes a good tool description that helps the AI use it correctly?"
 * - "How do I handle API errors and rate limits in tool implementations?"
 * - "Can I create tools that call other tools internally for complex operations?"
 */
public class WeatherTool {

    private static final Logger log = LoggerFactory.getLogger(WeatherTool.class);
    private final Random random = new Random();
    private final ToolExecutionInfo.Recorder toolExecutionRecorder;

    public WeatherTool() {
        this(new ToolExecutionInfo.Recorder());
    }

    public WeatherTool(ToolExecutionInfo.Recorder toolExecutionRecorder) {
        this.toolExecutionRecorder = toolExecutionRecorder;
    }

    /**
     * Get the current weather for a given location.
     * 
     * @param location the location to get weather for
     * @return weather description
     */
    @Tool(description = "Get the current weather for a given location")
    public String getCurrentWeather(@ToolParam(description = "Location name") String location) {
        return toolExecutionRecorder.record("getCurrentWeather", List.of("location=" + location), () -> {
            log.info("Getting weather for location: {}", location);

            // Simulate weather data (in production, call real API)
            int temperature = 15 + random.nextInt(20); // 15-35 degrees
            String[] conditions = {"sunny", "cloudy", "partly cloudy", "rainy"};
            String condition = conditions[random.nextInt(conditions.length)];

            return String.format("The weather in %s is currently %d°C and %s.",
                location, temperature, condition);
        });
    }

    /**
     * Get the weather forecast for a location.
     * 
     * @param location the location to get forecast for
     * @param days number of days to forecast
     * @return weather forecast
     */
    @Tool(description = "Get the weather forecast for a given location and number of days")
    public String getWeatherForecast(
            @ToolParam(description = "Location name") String location,
            @ToolParam(description = "Number of days (1-7)") int days) {
        return toolExecutionRecorder.record("getWeatherForecast", List.of("location=" + location, "days=" + days), () -> {
            log.info("Getting {}-day forecast for location: {}", days, location);

            // Validate input
            if (days < 1 || days > 7) {
                return "Forecast is available for 1 to 7 days only.";
            }

            StringBuilder forecast = new StringBuilder();
            forecast.append(String.format("%d-day forecast for %s:\n", days, location));

            for (int i = 1; i <= days; i++) {
                int temp = 15 + random.nextInt(20);
                String[] conditions = {"sunny", "cloudy", "rainy", "partly cloudy"};
                String condition = conditions[random.nextInt(conditions.length)];
                forecast.append(String.format("Day %d: %d°C, %s\n", i, temp, condition));
            }

            return forecast.toString();
        });
    }
}
