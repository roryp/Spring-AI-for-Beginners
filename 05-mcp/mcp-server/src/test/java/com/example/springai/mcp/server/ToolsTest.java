package com.example.springai.mcp.server;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;

class ToolsTest {

    @Test
    @DisplayName("Should return greeting message")
    void testHelloTool() {
        Tools tools = new Tools();
        String result = tools.hello("World");
        assertThat(result).isEqualTo("Hello World!");
    }

    @Test
    @DisplayName("Should return personalized greeting")
    void testHelloToolWithCustomName() {
        Tools tools = new Tools();
        String result = tools.hello("Spring AI");
        assertThat(result).isEqualTo("Hello Spring AI!");
    }
}
