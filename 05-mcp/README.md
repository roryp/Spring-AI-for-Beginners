# Module 05: Model Context Protocol (MCP)

## Table of Contents

- [What You'll Learn](#what-youll-learn)
- [What is MCP?](#what-is-mcp)
- [How MCP Works](#how-mcp-works)
- [Project Structure](#project-structure)
- [Running the Examples](#running-the-examples)
  - [Prerequisites](#prerequisites)
  - [Quick Start](#quick-start)
- [MCP Server](#mcp-server)
  - [Defining Tools](#defining-tools)
  - [MCP Sampling](#mcp-sampling)
  - [Server Configuration](#server-configuration)
- [MCP Client](#mcp-client)
  - [Connecting to Tools](#connecting-to-tools)
  - [Logging and Sampling Handlers](#logging-and-sampling-handlers)
  - [Client Configuration](#client-configuration)
- [Key Concepts](#key-concepts)
- [Congratulations!](#congratulations)

## What You'll Learn

You've built conversational AI, mastered prompts, grounded responses in documents, and created agents with tools. But all those tools were custom-built for your specific application. What if you could give your AI access to a standardized ecosystem of tools that anyone can create and share?

In this module, you'll learn how to build both an **MCP server** (that exposes tools) and an **MCP client** (that discovers and uses those tools) using Spring AI's MCP support. The server exposes weather forecast tools via the Streamable HTTP transport, and the client connects to it, discovers available tools, and lets Azure OpenAI call them automatically.

## What is MCP?

The Model Context Protocol (MCP) provides a standard way for AI applications to discover and use external tools. Instead of writing custom integrations for each data source or service, you connect to MCP servers that expose their capabilities in a consistent format. Your AI agent can then discover and use these tools automatically.

The diagram below shows the difference — without MCP, every integration requires custom point-to-point wiring; with MCP, a single protocol connects your app to any tool:

<img src="images/mcp-comparison.png" alt="MCP Comparison" width="800"/>

*Before MCP: Complex point-to-point integrations. After MCP: One protocol, endless possibilities.*

MCP standardizes this. An MCP server exposes tools with clear descriptions and schemas. Any MCP client can connect, discover available tools, and use them. Build once, use everywhere.

The diagram below illustrates this architecture — a single MCP client (your AI application) connects to multiple MCP servers, each exposing their own set of tools through the standard protocol:

<img src="images/mcp-architecture.png" alt="MCP Architecture" width="800"/>

*Model Context Protocol architecture - standardized tool discovery and execution*

## How MCP Works

Under the hood, MCP uses a layered architecture. Your Java application (the MCP client) discovers available tools, sends JSON-RPC requests through a transport layer (Stdio or HTTP), and the MCP server executes operations and returns results. The following diagram breaks down each layer of this protocol:

<img src="images/mcp-protocol-detail.png" alt="MCP Protocol Detail" width="800"/>

*How MCP works under the hood — clients discover tools, exchange JSON-RPC messages, and execute operations through a transport layer.*

**Server-Client Architecture**

MCP uses a client-server model. Servers provide tools — reading files, querying databases, calling APIs. Clients (your AI application) connect to servers and use their tools.

**Tool Discovery**

When your client connects to an MCP server, it asks "What tools do you have?" The server responds with a list of available tools, each with descriptions and parameter schemas. Your AI agent can then decide which tools to use based on user requests:

<img src="images/tool-discovery.png" alt="MCP Tool Discovery" width="800"/>

*The AI discovers available tools at startup — it now knows what capabilities are available and can decide which ones to use.*

**Transport Mechanisms**

MCP supports different transport mechanisms. This module uses Streamable HTTP for client-server communication over the network:

<img src="images/transport-mechanisms.png" alt="Transport Mechanisms" width="800"/>

*MCP transport mechanisms: HTTP for remote servers, Stdio for local processes*

## Project Structure

This module is a multi-module Maven project with two Spring Boot applications:

```
05-mcp/
├── pom.xml                    # Parent POM (multi-module)
├── mcp-server/                # MCP Server - exposes tools
│   ├── pom.xml
│   └── src/main/
│       ├── java/.../server/
│       │   ├── McpServerApplication.java
│       │   └── Tools.java     # Weather + greeting tools
│       └── resources/
│           └── application.yaml
├── mcp-client/                # MCP Client - discovers and uses tools
│   ├── pom.xml
│   └── src/main/
│       ├── java/.../client/
│       │   ├── McpClientApplication.java
│       │   ├── McpClientHandlers.java   # Logging + sampling
│       │   └── SpringAiConfig.java      # Azure OpenAI config
│       └── resources/
│           └── application.yaml
├── start-server.ps1 / .sh     # Start the MCP server
└── start-client.ps1 / .sh     # Start the MCP client
```

**Data Flow:**

```
┌──────────────┐    Streamable HTTP    ┌──────────────┐
│  MCP Client  │ ◄──────────────────► │  MCP Server  │
│              │    (port 8080)        │              │
│  Azure OpenAI│                      │  @McpTool    │
│  ChatClient  │                      │  hello()     │
│  Sampling    │  ◄── sampling ───    │  weather()   │
│  Logging     │  ◄── logging ────    │              │
└──────────────┘                      └──────────────┘
```

## Running the Examples

### Prerequisites

- Completed [Module 04 - Tools](../04-tools/README.md)
- `.env` file in root directory with Azure credentials (created by `azd up` in Module 01)
- Java 21+, Maven 3.9+

> **Note:** If you haven't set up your environment variables yet, see [Module 01 - Introduction](../01-introduction/README.md) for deployment instructions (`azd up` creates the `.env` file automatically), or copy `.env.example` to `.env` in the root directory and fill in your values.

### Quick Start

This demo requires two terminals — one for the server and one for the client.

**Step 1: Start the MCP Server**

**Bash:**
```bash
cd 05-mcp
chmod +x start-server.sh
./start-server.sh
```

**PowerShell:**
```powershell
cd 05-mcp
.\start-server.ps1
```

You should see output like:
```
Started McpServerApplication in 1.4 seconds
Registered tools: 2
Tomcat started on port 8080
```

**Step 2: Start the MCP Client (in a separate terminal)**

**Bash:**
```bash
cd 05-mcp
chmod +x start-client.sh
./start-client.sh
```

**PowerShell:**
```powershell
cd 05-mcp
.\start-client.ps1
```

**Expected output:**
```
> USER: What is the weather in Amsterdam right now?
> ASSISTANT: The current temperature in Amsterdam is 13.7°C...
```

The client discovers the server's tools via Streamable HTTP, the LLM decides to call `poeticWeatherForecast`, the server executes it (including a sampling callback to the client for a poem), and the final response is returned.

## MCP Server

The MCP server is a Spring Boot application that exposes tools via the Streamable HTTP transport. It uses `spring-ai-starter-mcp-server-webmvc` to auto-configure everything.

### Defining Tools

Tools are defined using the `@McpTool` annotation on Spring `@Service` methods. Spring AI automatically discovers, registers, and exposes them to MCP clients:

```java
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
        // ... fetch weather, optionally request a poem via sampling
    }
}
```

Key points:
- `@McpTool` — Marks a method as an MCP-exposed tool with a description the LLM can read
- `@McpToolParam` — Describes each parameter so the LLM knows what to pass
- `McpSyncRequestContext` — Provides access to MCP features like logging and sampling

> **🤖 Try with [GitHub Copilot](https://github.com/features/copilot) Chat:** Open [`Tools.java`](mcp-server/src/main/java/com/example/springai/mcp/server/Tools.java) and ask:
> - "How does the `@McpTool` annotation work?"
> - "What is the `McpSyncRequestContext` used for?"
> - "How could I add a new tool to this server?"

### MCP Sampling

**Sampling** is one of MCP's most powerful features — it allows the server to request an LLM completion from the client. This enables creative workflows where the server orchestrates AI-generated content.

In our weather tool, the server fetches weather data, then asks the client's LLM to write a poem about it:

```java
if (context.sampleEnabled()) {
    var sampleResponse = context.sample(spec -> spec
        .systemPrompt("You are a poet!")
        .message("Please write a poem about this weather forecast:\n" + weatherJson));

    weatherPoem = ((TextContent) sampleResponse.content()).text();
}
```

The sampling flow:
1. **Server** calls `context.sample(...)` with a prompt
2. **MCP protocol** sends a `sampling/createMessage` request back to the client
3. **Client's** `@McpSampling` handler receives the request, calls its own LLM, and returns the result
4. **Server** receives the poem and includes it in the tool response

### Server Configuration

The server is configured in [`application.yaml`](mcp-server/src/main/resources/application.yaml):

```yaml
spring:
  ai:
    mcp:
      server:
        name: mcp-weather-server
        version: 0.0.1
        protocol: STREAMABLE        # Use Streamable HTTP transport
        request-timeout: 120s       # Allow time for sampling round-trips
```

## MCP Client

The MCP client is a Spring Boot command-line application that connects to the MCP server, discovers its tools, and uses them through a `ChatClient` backed by Azure OpenAI.

### Connecting to Tools

The client uses `spring-ai-starter-mcp-client-webflux` which auto-configures MCP client connections and exposes discovered tools as `ToolCallbackProvider`:

```java
@Bean
public CommandLineRunner predefinedQuestions(ChatClient.Builder chatClientBuilder,
        ToolCallbackProvider toolCallbackProvider) {

    return args -> {
        ChatClient chatClient = chatClientBuilder
            .defaultToolCallbacks(toolCallbackProvider) // MCP tools injected here
            .build();

        String response = chatClient.prompt("What is the weather in Amsterdam?")
            .call().content();
    };
}
```

The `ToolCallbackProvider` is auto-configured by Spring AI — it connects to all configured MCP servers, discovers their tools, and makes them available to the `ChatClient`. No manual tool registration needed.

> **🤖 Try with [GitHub Copilot](https://github.com/features/copilot) Chat:** Open [`McpClientApplication.java`](mcp-client/src/main/java/com/example/springai/mcp/client/McpClientApplication.java) and ask:
> - "How does the client discover tools from the MCP server?"
> - "What happens when the LLM decides to call a tool?"
> - "How is `ToolCallbackProvider` auto-configured?"

### Logging and Sampling Handlers

The client registers handlers for MCP logging notifications and sampling requests using annotations:

```java
@Service
public class McpClientHandlers {

    @McpLogging(clients = "weather-server")
    public void loggingHandler(LoggingMessageNotification loggingMessage) {
        logger.info("MCP LOGGING: [{}] {}", loggingMessage.level(), loggingMessage.data());
    }

    @McpSampling(clients = "weather-server")
    public CreateMessageResult samplingHandler(CreateMessageRequest llmRequest) {
        // Forward the server's sampling request to our LLM
        String response = chatClientBuilder.build()
            .prompt()
            .system(llmRequest.systemPrompt())
            .user(userPrompt)
            .call().content();

        return CreateMessageResult.builder()
            .content(new McpSchema.TextContent(response)).build();
    }
}
```

- `@McpLogging` — Receives log messages from the server (e.g., `context.info(...)` calls in tools)
- `@McpSampling` — Handles LLM completion requests from the server, enabling the server to leverage the client's AI model

### Client Configuration

The client is configured in [`application.yaml`](mcp-client/src/main/resources/application.yaml):

```yaml
spring:
  main:
    web-application-type: none              # CLI app, no web server
  ai:
    mcp:
      client:
        request-timeout: 120s               # Allow time for LLM calls
        streamable-http:
          connections:
            weather-server:
              url: http://localhost:8080     # MCP server URL
```

The `weather-server` connection name is used in `@McpLogging(clients = "weather-server")` and `@McpSampling(clients = "weather-server")` to bind handlers to specific server connections.

## Key Concepts

To help you decide between the custom `@Tool` methods from Module 04 and MCP tools from this module, here's a comparison of the key trade-offs:

<img src="images/custom-vs-mcp-tools.png" alt="Custom Tools vs MCP Tools" width="800"/>

*When to use custom @Tool methods vs MCP tools — custom tools for app-specific logic with full type safety, MCP tools for standardized integrations that work across applications.*

| Feature | Spring AI `@Tool` (Module 04) | Spring AI MCP (Module 05) |
|---------|-------------------------------|---------------------------|
| **Scope** | In-process, app-specific | Cross-process, standardized protocol |
| **Transport** | Direct method call | Streamable HTTP or Stdio |
| **Discovery** | Compile-time | Runtime via `tools/list` |
| **Reusability** | Within one app | Any MCP-compatible client |
| **Sampling** | N/A | Server can request LLM completions from client |
| **Use when** | App-specific business logic | Shared tool ecosystems, multi-app integration |

**MCP** is ideal when you want to leverage existing tool ecosystems, build tools that multiple applications can share, integrate third-party services with standard protocols, or swap tool implementations without changing code.

One of MCP's biggest advantages is its growing ecosystem:

<img src="images/mcp-ecosystem.png" alt="MCP Ecosystem" width="800"/>

*MCP creates a universal protocol ecosystem — any MCP-compatible server works with any MCP-compatible client, enabling tool sharing across applications.*

## Congratulations!

You've completed the Spring AI for Beginners course! Here's a look at the full learning journey:

<img src="images/course-completion.png" alt="Course Completion" width="800"/>

You've learned:

- How to build conversational AI with Spring AI (Module 01)
- Prompt engineering patterns for different tasks (Module 02)
- Grounding responses in your documents with RAG (Module 03)
- Creating AI agents with custom tools (Module 04)
- Building MCP servers and clients with Spring AI for standardized tool integration (Module 05)

**Official Resources:**
- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/) - Comprehensive guides and API reference
- [Spring AI MCP Documentation](https://docs.spring.io/spring-ai/reference/api/mcp/) - MCP-specific guides
- [MCP Specification](https://modelcontextprotocol.io/) - The Model Context Protocol specification

Thank you for completing this course!

---

**Navigation:** [← Previous: Module 04 - Tools](../04-tools/README.md) | [Back to Main](../README.md)

