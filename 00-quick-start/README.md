# Module 00: Quick Start

## Table of Contents

- [Introduction](#introduction)
- [What is Spring AI?](#what-is-spring-ai)
- [Spring AI Dependencies](#spring-ai-dependencies)
- [Prerequisites](#prerequisites)
- [Setup](#setup)
  - [1. Get Your GitHub Token](#1-get-your-github-token)
  - [2. Set Your Token](#2-set-your-token)
- [Run the Examples](#run-the-examples)
  - [1. Basic Chat](#1-basic-chat)
  - [2. Prompt Patterns](#2-prompt-patterns)
  - [3. Function Calling](#3-function-calling)
  - [4. Document Q&A](#4-document-qa)
  - [5. Responsible AI](#5-responsible-ai)
- [What Each Example Shows](#what-each-example-shows)
- [Next Steps](#next-steps)
- [Troubleshooting](#troubleshooting)

## Introduction

This quickstart is meant to get you up and running with Spring AI as quickly as possible. It covers the absolute basics of building AI applications with Spring AI and GitHub Models. In the next modules you'll switch to Azure OpenAI and GPT-5.2 and dive deeper into each concept.

## What is Spring AI?

Spring AI is a Java framework that simplifies building AI-powered applications. It provides a consistent API across different AI providers, leveraging the official OpenAI Java SDK for robust integration with OpenAI, Microsoft Foundry, and GitHub Models.

This quick start focuses on the fundamentals: sending prompts, using prompt templates, calling tools, grounding responses with documents, and applying safety guardrails.

We'll use these core components:

**OpenAiSdkChatModel** - The chat model implementation built on the official OpenAI Java SDK. It auto-detects the provider (OpenAI, Azure, GitHub Models) based on the base URL.

**Prompt & PromptTemplate** - Spring AI's prompt abstractions. `Prompt` wraps messages for the model, and `PromptTemplate` creates reusable prompts with `{variable}` placeholders.

**FunctionToolCallback** - Registers Java functions as tools the AI can call. The model decides when to invoke them based on the user's request.

## Spring AI Dependencies

This quick start uses one main Maven dependency in the [`pom.xml`](pom.xml):

```xml
<!-- Spring AI OpenAI SDK (Official OpenAI Java SDK integration) -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-sdk</artifactId> <!-- Version managed by Spring AI BOM in root pom.xml -->
</dependency>
```

The `spring-ai-openai-sdk` module provides `OpenAiSdkChatModel` and `OpenAiSdkChatOptions` that connect to OpenAI-compatible APIs. GitHub Models uses the same API format, so no special adapter is needed - just point the base URL to `https://models.github.ai/inference` and set `.gitHubModels(true)`.

## Prerequisites

**Using the Dev Container?** Java and Maven are already installed. You only need a GitHub Personal Access Token.

**Local Development:**
- Java 21+, Maven 3.9+
- GitHub Personal Access Token (instructions below)

> **Note:** This module uses `gpt-4.1-nano` from GitHub Models. Do not modify the model name in the code - it's configured to work with GitHub's available models.
>
> **Note:** Spring AI 2.0.0-M4 (milestone) is used. The Spring Milestones repository is configured in the root `pom.xml`.

## Setup

### 1. Get Your GitHub Token

1. Go to [GitHub Settings → Personal Access Tokens](https://github.com/settings/personal-access-tokens)
2. Click "Generate new token"
3. Set a descriptive name (e.g., "Spring AI Demo")
4. Set expiration (7 days recommended)
5. Under "Account permissions", find "Models" and set to "Read-only"
6. Click "Generate token"
7. Copy and save your token - you won't see it again

### 2. Set Your Token

**Option 1: Using VS Code (Recommended)**

If you're using VS Code, add your token to the `.env` file in the project root:

If the `.env` file does not exist, copy `.env.example` to `.env` or create a new `.env` file in the project root.

**Example `.env` file:**
```bash
# In /workspaces/Spring-AI-for-Beginners/.env
GITHUB_TOKEN=your_token_here
```

Then you can simply right-click on any demo file (e.g., `BasicChatDemo.java`) in the Explorer and select **"Run Java"** or use the launch configurations from the Run and Debug panel.

**Option 2: Using Terminal**

Set the token as an environment variable:

**Bash:**
```bash
export GITHUB_TOKEN=your_token_here
```

**PowerShell:**
```powershell
$env:GITHUB_TOKEN=your_token_here
```

## Run the Examples

**Using VS Code:** Simply right-click on any demo file in the Explorer and select **"Run Java"**, or use the launch configurations from the Run and Debug panel (make sure you've added your token to the `.env` file first).

**Using Maven:** Alternatively, you can run from the command line:

### 1. Basic Chat

**Bash:**
```bash
mvn compile exec:java -Dexec.mainClass=com.example.springai.quickstart.BasicChatDemo
```

**PowerShell:**
```powershell
mvn --% compile exec:java -Dexec.mainClass=com.example.springai.quickstart.BasicChatDemo
```

### 2. Prompt Patterns

**Bash:**
```bash
mvn compile exec:java -Dexec.mainClass=com.example.springai.quickstart.PromptEngineeringDemo
```

**PowerShell:**
```powershell
mvn --% compile exec:java -Dexec.mainClass=com.example.springai.quickstart.PromptEngineeringDemo
```

Shows zero-shot, few-shot, chain-of-thought, role-based prompting, and conversational memory.

### 3. Function Calling

**Bash:**
```bash
mvn compile exec:java -Dexec.mainClass=com.example.springai.quickstart.ToolIntegrationDemo
```

**PowerShell:**
```powershell
mvn --% compile exec:java -Dexec.mainClass=com.example.springai.quickstart.ToolIntegrationDemo
```

AI automatically calls your Java methods when needed.

### 4. Document Q&A

**Bash:**
```bash
mvn compile exec:java -Dexec.mainClass=com.example.springai.quickstart.SimpleReaderDemo
```

**PowerShell:**
```powershell
mvn --% compile exec:java -Dexec.mainClass=com.example.springai.quickstart.SimpleReaderDemo
```

Ask questions about your documents using context-grounded chat.

### 5. Responsible AI

**Bash:**
```bash
mvn compile exec:java -Dexec.mainClass=com.example.springai.quickstart.ResponsibleAIDemo
```

**PowerShell:**
```powershell
mvn --% compile exec:java -Dexec.mainClass=com.example.springai.quickstart.ResponsibleAIDemo
```

See how AI safety filters block harmful content.

## What Each Example Shows

**Basic Chat** - [BasicChatDemo.java](src/main/java/com/example/springai/quickstart/BasicChatDemo.java)

Start here to see Spring AI at its simplest. You'll create an `OpenAiSdkChatModel`, send a prompt, and get back a `ChatResponse`. This demonstrates the foundation: how to initialize models with custom endpoints and API keys. Once you understand this pattern, everything else builds on it.

```java
var chatOptions = OpenAiSdkChatOptions.builder()
    .baseUrl("https://models.github.ai/inference")
    .apiKey(System.getenv("GITHUB_TOKEN"))
    .model("gpt-4.1-nano")
    .gitHubModels(true)
    .build();

var chatModel = OpenAiSdkChatModel.builder()
    .options(chatOptions)
    .build();

ChatResponse response = chatModel.call(new Prompt("What is Spring AI?"));
System.out.println(response.getResult().getOutput().getText());
```

> **🤖 Try with [GitHub Copilot](https://github.com/features/copilot) Chat:** Open [`BasicChatDemo.java`](src/main/java/com/example/springai/quickstart/BasicChatDemo.java) and ask:
> - "How would I switch from GitHub Models to Azure OpenAI in this code?"
> - "What other parameters can I configure in OpenAiSdkChatOptions.builder()?"
> - "How do I add streaming responses instead of waiting for the complete response?"

**Prompt Engineering** - [PromptEngineeringDemo.java](src/main/java/com/example/springai/quickstart/PromptEngineeringDemo.java)

Now that you know how to talk to a model, let's explore what you say to it. This demo uses the same model setup but shows six different prompting patterns. Try zero-shot prompts for direct instructions, few-shot prompts that learn from examples, chain-of-thought prompts that reveal reasoning steps, role-based prompts that set context, and prompt templates for reusable prompts with variables.

The below example shows a prompt using Spring AI's `PromptTemplate` to fill in variables. The AI will answer based on the provided destination and activity.

```java
PromptTemplate template = new PromptTemplate(
    "What's the best time to visit {destination} for {activity}?"
);

Prompt prompt = template.create(Map.of(
    "destination", "Paris",
    "activity", "sightseeing"
));

ChatResponse response = chatModel.call(prompt);
```

The demo also includes a conversational memory pattern using Spring AI's `MessageWindowChatMemory` — showing how the model can remember context across multiple turns:

```java
ChatMemory chatMemory = MessageWindowChatMemory.builder()
        .maxMessages(10)
        .build();
String conversationId = "demo-session";

chatMemory.add(conversationId, new UserMessage("My name is Alex and I'm learning Spring AI."));
ChatResponse response1 = chatModel.call(new Prompt(chatMemory.get(conversationId)));
chatMemory.add(conversationId, response1.getResult().getOutput());

// Second turn — the model remembers your name and previous context
chatMemory.add(conversationId, new UserMessage("What's my name?"));
ChatResponse response2 = chatModel.call(new Prompt(chatMemory.get(conversationId)));
// response2 correctly recalls "Alex"
```

> **🤖 Try with [GitHub Copilot](https://github.com/features/copilot) Chat:** Open [`PromptEngineeringDemo.java`](src/main/java/com/example/springai/quickstart/PromptEngineeringDemo.java) and ask:
> - "What's the difference between zero-shot and few-shot prompting, and when should I use each?"
> - "How does the temperature parameter affect the model's responses?"
> - "What are some techniques to prevent prompt injection attacks in production?"
> - "How can I create reusable PromptTemplate objects for common patterns?"

**Tool Integration** - [ToolIntegrationDemo.java](src/main/java/com/example/springai/quickstart/ToolIntegrationDemo.java)

This is where Spring AI gets powerful. You register Java functions as `FunctionToolCallback` instances, and the AI automatically decides when to call them based on the user's request. This demonstrates function calling, a key technique for building AI that can take actions, not just answer questions.

```java
record TwoNumbers(double a, double b) {}

List<ToolCallback> toolCallbacks = List.of(
    FunctionToolCallback.builder("add", (TwoNumbers input) -> input.a() + input.b())
        .description("Performs addition of two numeric values")
        .inputType(TwoNumbers.class)
        .build()
);

var chatOptions = OpenAiSdkChatOptions.builder()
    .model("gpt-4.1-nano")
    .toolCallbacks(toolCallbacks)
    .build();
```

> **🤖 Try with [GitHub Copilot](https://github.com/features/copilot) Chat:** Open [`ToolIntegrationDemo.java`](src/main/java/com/example/springai/quickstart/ToolIntegrationDemo.java) and ask:
> - "How does FunctionToolCallback work and what does Spring AI do with it behind the scenes?"
> - "Can the AI call multiple tools in sequence to solve complex problems?"
> - "What happens if a tool throws an exception - how should I handle errors?"
> - "How would I integrate a real API instead of this calculator example?"

**Document Q&A** - [SimpleReaderDemo.java](src/main/java/com/example/springai/quickstart/SimpleReaderDemo.java)

Here you'll see document-grounded chat using a context-stuffing approach. The document is loaded and included in the system message, so the AI answers based on your document content rather than its general knowledge. This is a lightweight approach suitable for small documents — for full RAG with vector stores and embeddings, see the `03-rag` module.

```java
String documentContent = Files.readString(Paths.get("document.txt"));

SystemMessage systemMessage = new SystemMessage(
    "Answer questions based on this document:\n" + documentContent
);

List<Message> messages = List.of(systemMessage, new UserMessage("What is the main topic?"));
ChatResponse response = chatModel.call(new Prompt(messages));
```

> **🤖 Try with [GitHub Copilot](https://github.com/features/copilot) Chat:** Open [`SimpleReaderDemo.java`](src/main/java/com/example/springai/quickstart/SimpleReaderDemo.java) and ask:
> - "How does this differ from a full RAG pipeline with vector stores?"
> - "What are the limitations of context-stuffing vs. embedding-based retrieval?"
> - "How would I scale this to handle larger documents or multiple files?"

**Responsible AI** - [ResponsibleAIDemo.java](src/main/java/com/example/springai/quickstart/ResponsibleAIDemo.java)

Build AI safety with defense in depth. This demo shows two layers of protection working together:

**Part 1: Application-level Input Guardrails** - Block dangerous prompts before they reach the LLM. A simple validation method checks for prohibited keywords or patterns. These run in your code, so they're fast and free.

```java
private static String validateInput(String text) {
    String lower = text.toLowerCase();
    for (String keyword : BLOCKED_KEYWORDS) {
        if (lower.contains(keyword)) {
            return "Blocked: contains prohibited keyword '" + keyword + "'";
        }
    }
    return null; // safe
}
```

**Part 2: Provider Safety Filters** - GitHub Models has built-in filters that catch what your guardrails might miss. You'll see hard blocks (HTTP 400 errors) for severe violations and soft refusals where the AI politely declines.

> **🤖 Try with [GitHub Copilot](https://github.com/features/copilot) Chat:** Open [`ResponsibleAIDemo.java`](src/main/java/com/example/springai/quickstart/ResponsibleAIDemo.java) and ask:
> - "How can I use Spring AI advisors for output guardrails?"
> - "What is the difference between a hard block and a soft refusal?"
> - "Why use both guardrails and provider filters together?"

## Next Steps

**Next Module:** [01-introduction - Getting Started with Spring AI](../01-introduction/README.md)

---

**Navigation:** [← Back to Main](../README.md) | [Next: Module 01 - Introduction →](../01-introduction/README.md)

---

## Troubleshooting

### First-Time Maven Build

**Issue**: Initial `mvn clean compile` or `mvn package` takes a long time (10-15 minutes)

**Cause**: Maven needs to download all project dependencies (Spring Boot, Spring AI libraries, Azure SDKs, etc.) on the first build.

**Solution**: This is normal behavior. Subsequent builds will be much faster as dependencies are cached locally. Download time depends on your network speed.

### PowerShell Maven Command Syntax

**Issue**: Maven commands fail with error `Unknown lifecycle phase ".mainClass=..."`

**Cause**: PowerShell interprets `=` as a variable assignment operator, breaking Maven property syntax

**Solution**: Use the stop-parsing operator `--%` before the Maven command:

**PowerShell:**
```powershell
mvn --% compile exec:java -Dexec.mainClass=com.example.springai.quickstart.BasicChatDemo
```

**Bash:**
```bash
mvn compile exec:java -Dexec.mainClass=com.example.springai.quickstart.BasicChatDemo
```

The `--%` operator tells PowerShell to pass all remaining arguments literally to Maven without interpretation.

### Windows PowerShell Emoji Display

**Issue**: AI responses show garbage characters (e.g., `????` or `â??`) instead of emojis in PowerShell

**Cause**: PowerShell's default encoding doesn't support UTF-8 emojis

**Solution**: Run this command before executing Java applications:
```cmd
chcp 65001
```

This forces UTF-8 encoding in the terminal. Alternatively, use Windows Terminal which has better Unicode support.

### Debugging API Calls

**Issue**: Authentication errors, rate limits, or unexpected responses from the AI model

**Solution**: Set the `OPENAI_LOG` environment variable to enable debug logging from the OpenAI Java SDK:
```bash
export OPENAI_LOG=debug
```
This shows HTTP requests and responses in the console, helping troubleshoot authentication errors, rate limits, or unexpected responses. Remove this variable in production to reduce log noise.