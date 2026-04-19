# Module 06: Agentic Patterns

This module demonstrates five fundamental **agentic workflow patterns** for building effective LLM-based systems using Spring AI. Each pattern is implemented as an interactive demo you can run and explore in a web UI.

## What Are Agents?

In previous modules, every interaction with the LLM followed the same pattern: **you send one prompt, you get one response**. That works for simple tasks — answering a question, classifying sentiment, generating a summary. But real-world problems are rarely that simple.

Consider these tasks:
- "Analyze this quarterly report and produce a formatted summary" — requires **multiple sequential steps** (extract data → normalize → sort → format)
- "Translate this document into French, Spanish, and German" — requires **parallel processing** of independent subtasks
- "Handle this customer support ticket" — requires **classifying** the issue first, then routing to a specialized handler
- "Write production-ready code for a thread-safe counter" — requires **iterative refinement** with quality checks

No single LLM call can reliably handle these. You need to **orchestrate multiple LLM calls** — chaining them, running them in parallel, routing between them, or looping until quality criteria are met. This orchestration is what makes a system "agentic."

### From Single Calls to Agentic Systems



Think of it as a progression:

| Level | What It Does | Example |
|-------|-------------|---------|
| **Single LLM call** | One prompt → one response | "Summarize this text" |
| **Enhanced LLM** | LLM enhanced with retrieval, tools, memory | "Search the docs and answer my question" (RAG, Module 03) |
| **Workflow** | Multiple LLM calls orchestrated through **predefined code paths** | "Extract → standardize → sort → format" (this module) |
| **Autonomous Agent** | LLM **dynamically decides** its own next steps and tool usage | "Figure out how to complete this task on your own" |

In [Module 01](../01-introduction/README.md) you built stateless and stateful chat. In [Module 02](../02-prompt-engineering/README.md) you learned prompt engineering. In [Module 03](../03-rag/README.md) you added retrieval. In [Module 04](../04-tools/README.md) you gave the LLM tools. Each module added a new capability. **This module combines them into orchestrated workflows** — the final step before fully autonomous agents.

### The Enhanced LLM

Before we look at workflows, start with the core unit you'll orchestrate: an **enhanced LLM**.

An enhanced LLM is a normal LLM call with extra capabilities around it, such as retrieval, tool access, and memory. Unlike a plain LLM that only generates text from its training data, an enhanced LLM can reach outside itself — querying live data sources, invoking external tools, and maintaining conversational context — making it far more capable for real-world tasks:

![Enhanced LLM — the building block of agentic systems](images/agentic-systems-overview.png)

In this course, augmentation means the model can:
- **Retrieve** relevant context from external data (RAG from [Module 03](../03-rag/README.md))
- **Call tools** to perform actions (APIs, databases, code execution from [Module 04](../04-tools/README.md))
- **Remember** prior turns in a conversation (chat memory from [Module 01](../01-introduction/README.md))

One enhanced LLM is enough for many single-step tasks. But real applications often need multi-step execution: one result feeds the next step, several steps run in parallel, or output is evaluated and refined before returning.

That orchestration layer is what this module focuses on. The five patterns below show practical ways to combine enhanced LLM calls into reliable workflows.

## Patterns

| # | Pattern | Description | Key Benefit |
|---|---------|-------------|-------------|
| 1 | **Chain Workflow** | Sequential LLM calls — each step transforms the previous output | High accuracy via decomposition |
| 2 | **Parallelization** | Concurrent LLM calls for independent subtasks or voting | Throughput & multi-perspective |
| 3 | **Routing Workflow** | Classifies input and routes to the best-fit handler | Specialization |
| 4 | **Orchestrator-Workers** | Central LLM decomposes tasks, delegates to workers | Adaptive problem-solving |
| 5 | **Evaluator-Optimizer** | Iterative generate → evaluate → refine loop | Best quality via refinement |

## Prerequisites

- Completed [Module 01 - Introduction](../01-introduction/README.md) (Azure OpenAI resources deployed)
- Java 21+
- Maven 3.6+
- `.env` file in root directory with Azure credentials (created by `azd up` in Module 01)

## How This Uses Spring AI

This module reuses `spring-ai-starter-model-openai-sdk` from [Module 01](../01-introduction/README.md#how-this-uses-spring-ai) and `spring-ai-client-chat` introduced in [Module 03](../03-rag/README.md#how-this-uses-spring-ai). No new Spring AI dependencies are added — all five agentic patterns are orchestrated through `ChatClient` ([pom.xml](pom.xml)).

The `application.yaml` is the same chat-model configuration as earlier modules ([application.yaml](src/main/resources/application.yaml)):

```yaml
spring:
  ai:
    openai-sdk:
      base-url: ${AZURE_OPENAI_ENDPOINT}
      api-key: ${AZURE_OPENAI_API_KEY}
      chat:
        options:
          model: ${AZURE_OPENAI_DEPLOYMENT}
```

The difference in this module is how `ChatClient` calls are **orchestrated** — chained, parallelized, routed, delegated, or looped — rather than executed as single calls.

## Quick Start

1. **Set environment variables** in the root `.env` file:
   ```
   AZURE_OPENAI_ENDPOINT=https://your-endpoint.openai.azure.com/
   AZURE_OPENAI_API_KEY=your-api-key
   AZURE_OPENAI_DEPLOYMENT=your-deployment-name
   ```

2. **Build and run:**

   **Option 1: Using Spring Boot Dashboard (Recommended for VS Code users)**

   The dev container includes the Spring Boot Dashboard extension, which provides a visual interface to manage all Spring Boot applications. You can find it in the Activity Bar on the left side of VS Code (look for the Spring Boot icon).

   From the Spring Boot Dashboard, you can:
   - See all available Spring Boot applications in the workspace
   - Start/stop applications with a single click
   - View application logs in real-time
   - Monitor application status

   Simply click the play button next to "spring-ai-agents" to start this module, or start all modules at once.

   <img src="images/dashboard.png" alt="Spring Boot Dashboard" width="800"/>

   **Option 2: Using shell scripts**

   ```bash
   # From this directory
   ./start.sh        # Linux/Mac
   .\start.ps1       # Windows PowerShell
   ```

3. **Open the dashboard:** [http://localhost:8086](http://localhost:8086)


## How Each Pattern Works

### 1. Chain Workflow

![Chain Workflow](images/chain-workflow.png)

Processes input through a 4-step pipeline: **Extract → Standardize → Sort → Format**. Each LLM call receives the previous step's output and transforms it further.

**When to use:** Tasks with clear sequential steps where you want to trade latency for higher accuracy, and each step builds on the previous step's output.

![Chain Workflow demo page](images/chain-ui.png)

### 2. Parallelization Workflow

![Parallelization Workflow](images/parallelization-workflow.png)

Sends the same prompt to multiple inputs concurrently using a thread pool. Results are returned in the same order as inputs.

**When to use:** Processing large volumes of similar but independent items, tasks requiring multiple independent perspectives, or when processing time is critical and tasks are parallelizable.

![Parallelization Workflow demo page](images/parallelization-ui.png)

### 3. Routing Workflow

![Routing Workflow](images/routing-workflow.png)

An LLM classifier analyzes the input and selects the best route (billing, technical, or general). The input is then processed by a specialized prompt for that route.

**When to use:** Complex tasks with distinct categories of input that require different handling or specialized processing.

![Routing Workflow demo page](images/routing-ui.png)

### 4. Orchestrator-Workers

![Orchestrator-Workers](images/orchestrator-workers.png)

The orchestrator LLM analyzes a complex task and breaks it into subtasks with different approaches (e.g., formal vs. conversational). Workers execute each subtask independently.

**When to use:** Complex tasks where subtasks can't be predicted upfront and require adaptive problem-solving.

![Orchestrator-Workers demo page](images/orchestrator-ui.png)

### 5. Evaluator-Optimizer

![Evaluator-Optimizer](images/evaluator-optimizer.png)

A generator LLM produces a solution, then an evaluator LLM grades it (PASS / NEEDS_IMPROVEMENT / FAIL). If not passing, feedback is incorporated and the cycle repeats.

**When to use:** Tasks with clear evaluation criteria where iterative refinement provides measurable value (e.g., code generation, translation, content creation).

![Evaluator-Optimizer demo page](images/evaluator-ui.png)

## Best Practices

- **Start simple** — begin with basic workflows before adding complexity. Use the simplest pattern that meets your requirements.
- **Design for reliability** — implement clear error handling, use type-safe responses where possible, and build in validation at each step.
- **Consider trade-offs** — balance latency vs. accuracy, evaluate when to use parallel processing, and choose between fixed workflows and dynamic agents.

## References

- [Building Effective Agents — Spring AI Documentation](https://docs.spring.io/spring-ai/reference/2.0/api/effective-agents.html)
- [Spring AI Agentic Patterns Examples](https://github.com/spring-projects/spring-ai-examples/tree/main/agentic-patterns)
- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)

## Next Steps

Congratulations — you've completed the Spring AI for Beginners course! You now have hands-on experience with chat, memory, prompt engineering, RAG, tools, MCP, and agentic workflows.

To go further, explore the [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/) and the [Spring AI Examples Repository](https://github.com/spring-projects/spring-ai-examples).

---

**Navigation:** [← Previous: Module 05 - MCP](../05-mcp/README.md) | [Back to Main](../README.md)
