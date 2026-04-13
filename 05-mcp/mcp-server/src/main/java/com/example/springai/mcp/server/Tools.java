package com.example.springai.mcp.server;

import java.time.LocalDateTime;

import io.modelcontextprotocol.spec.McpSchema.TextContent;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.stereotype.Service;

@Service
public class Tools {

	@McpTool(description = "Greeting response")
	public String hello(String myName) {
		return "Hello " + myName + "!";
	}

	@McpTool(description = "Get the temperature (in celsius) for a specific location")
	public String poeticWeatherForecast(McpSyncRequestContext context,
			@McpToolParam(description = "The location latitude") double latitude,
			@McpToolParam(description = "The location longitude") double longitude) {

		// Mock weather response to avoid external API dependency
		WeatherResponse weather = new WeatherResponse(
				new WeatherResponse.Current(LocalDateTime.now(), 900, 13.7));

		var weatherJson = ModelOptionsUtils.toJsonStringPrettyPrinter(weather);

		context.info("Raw weather response: " + weatherJson);

		String weatherPoem = "none";

		if (context.sampleEnabled()) {

			context.info("Start sampling");

			var sampleResponse = context.sample(spec -> spec.systemPrompt("You are a poet!")
				.message(
						"Please write a poem about this weather forecast (temperature is in Celsius). Use markdown format:\n"
								+ weatherJson));

			weatherPoem = ((TextContent) sampleResponse.content()).text();

			context.info("Finish Sampling");
		}

		context.info("Weather poem is done!");

		return "Poem about the weather: " + weatherPoem + "\n" + weatherJson;
	}

	public record WeatherResponse(Current current) {
		public record Current(LocalDateTime time, int interval, double temperature_2m) {
		}
	}

}
