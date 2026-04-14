# 06 - Agentic Patterns

This module demonstrates five fundamental **agentic workflow patterns** for building effective LLM-based systems using Spring AI. Each pattern is implemented as an interactive demo you can run and explore in a web UI.

## Patterns

| # | Pattern | Description | Key Benefit |
|---|---------|-------------|-------------|
| 1 | **Chain Workflow** | Sequential LLM calls — each step transforms the previous output | High accuracy via decomposition |
| 2 | **Parallelization** | Concurrent LLM calls for independent subtasks or voting | Throughput & multi-perspective |
| 3 | **Routing Workflow** | Classifies input and routes to the best-fit handler | Specialization |
| 4 | **Orchestrator-Workers** | Central LLM decomposes tasks, delegates to workers | Adaptive problem-solving |
| 5 | **Evaluator-Optimizer** | Iterative generate → evaluate → refine loop | Best quality via refinement |

## Prerequisites

- Java 21+
- Maven 3.6+
- Azure OpenAI endpoint (or any OpenAI-compatible API)

## Quick Start

1. **Set environment variables** in the root `.env` file:
   ```
   AZURE_OPENAI_ENDPOINT=https://your-endpoint.openai.azure.com/
   AZURE_OPENAI_API_KEY=your-api-key
   AZURE_OPENAI_DEPLOYMENT=your-deployment-name
   ```

2. **Build and run:**
   ```bash
   # From this directory
   ./start.sh        # Linux/Mac
   .\start.ps1       # Windows PowerShell
   ```

3. **Open the dashboard:** [http://localhost:8086](http://localhost:8086)

## Project Structure

```
06-agents/
├── pom.xml
├── start.sh / start.ps1
├── stop.sh / stop.ps1
├── src/main/java/com/example/springai/agents/
│   ├── app/Application.java              # Spring Boot entry point
│   ├── config/SpringAiConfig.java         # Azure OpenAI + ChatClient config
│   ├── controller/
│   │   ├── DemoWebController.java         # Thymeleaf web routes
│   │   └── AgentPatternsController.java   # REST API endpoints
│   ├── patterns/                          # Core workflow implementations
│   │   ├── ChainWorkflow.java
│   │   ├── ParallelizationWorkflow.java
│   │   ├── RoutingWorkflow.java
│   │   ├── OrchestratorWorkers.java
│   │   └── EvaluatorOptimizer.java
│   └── service/
│       └── AgentPatternsService.java      # Orchestrates all patterns
├── src/main/resources/
│   ├── application.yaml
│   ├── static/css/agent-demo.css
│   ├── static/js/pattern-demo.js
│   └── templates/
│       ├── dashboard.html
│       └── patterns/{chain,parallelization,routing,orchestrator,evaluator}.html
└── src/test/java/.../SimpleAgentPatternsTest.java
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/agents/chain` | Run chain workflow |
| POST | `/api/agents/parallelization` | Run parallelization workflow |
| POST | `/api/agents/routing` | Run routing workflow |
| POST | `/api/agents/orchestrator` | Run orchestrator-workers workflow |
| POST | `/api/agents/evaluator` | Run evaluator-optimizer workflow |

## How Each Pattern Works

### 1. Chain Workflow
Processes input through a 4-step pipeline: **Extract → Standardize → Sort → Format**. Each LLM call receives the previous step's output and transforms it further.

### 2. Parallelization Workflow
Sends the same prompt to multiple inputs concurrently using a thread pool. Results are returned in the same order as inputs.

### 3. Routing Workflow
An LLM classifier analyzes the input and selects the best route (billing, technical, or general). The input is then processed by a specialized prompt for that route.

### 4. Orchestrator-Workers
The orchestrator LLM analyzes a complex task and breaks it into subtasks with different approaches (e.g., formal vs. conversational). Workers execute each subtask independently.

### 5. Evaluator-Optimizer
A generator LLM produces a solution, then an evaluator LLM grades it (PASS / NEEDS_IMPROVEMENT / FAIL). If not passing, feedback is incorporated and the cycle repeats.

## References

- [Spring AI Agentic Patterns Examples](https://github.com/spring-projects/spring-ai-examples/tree/main/agentic-patterns)
- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
