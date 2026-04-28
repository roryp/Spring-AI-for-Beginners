# Glossary

A reference of the key terms, Spring AI abstractions, and AI/ML concepts used throughout this course. Terms are grouped by the module that introduces them; concepts that reappear later are defined where they are first taught.

## Table of Contents

- [Module 00: Quick Start](#module-00-quick-start)
- [Module 01: Introduction](#module-01-introduction)
- [Module 02: Prompt Engineering](#module-02-prompt-engineering)
- [Module 03: RAG (Retrieval-Augmented Generation)](#module-03-rag-retrieval-augmented-generation)
- [Module 04: Tools](#module-04-tools)
- [Module 05: MCP (Model Context Protocol)](#module-05-mcp-model-context-protocol)
- [Module 06: Agents](#module-06-agents)
- [Cross-Cutting Terms](#cross-cutting-terms)

---

## Module 00: Quick Start

- **Spring AI** — A Java framework that provides a consistent API across AI providers (OpenAI, Microsoft Foundry, GitHub Models, etc.) so you can swap providers without rewriting application code.
- **GitHub Models** — GitHub's hosted catalog of language models (e.g., `gpt-4.1-nano`) accessed via a Personal Access Token; used in this module so you can try Spring AI without an Azure subscription.
- **ChatModel** — Spring AI's core abstraction for a chat-capable language model. You send it a `Prompt` and receive a `ChatResponse`.
- **OpenAiChatModel** — Spring AI's concrete `ChatModel` implementation built on the OpenAI Java SDK; works with both GitHub Models and Microsoft Foundry by configuring the base URL and API key.
- **OpenAiChatOptions** — Builder-style configuration for `OpenAiChatModel` (model name, base URL, API key, tool callbacks, etc.).
- **Prompt** — The structured request sent to a `ChatModel`, made up of one or more `Message` objects.
- **ChatResponse** — The structured result returned by a `ChatModel`, containing the generated text, token usage, and metadata.
- **PromptTemplate** — A reusable prompt with `{variable}` placeholders that Spring AI fills in at call time so you don't rebuild prompts by hand.
- **SystemMessage / UserMessage / AssistantMessage** — Spring AI message types that represent the roles in a conversation: instructions to the model, the user's input, and the model's reply.
- **FunctionToolCallback** — A wrapper that registers a Java function (often expressed via a record input) as a tool the model can call automatically when it decides one is needed.
- **ChatMemory** — Spring AI abstraction for storing conversation history so the model can remember earlier turns.
- **MessageWindowChatMemory** — A `ChatMemory` implementation that keeps the most recent N messages per conversation ID, dropping older ones as the window fills.
- **Conversation ID** — A string key used by `ChatMemory` to keep different users' or sessions' histories separate.
- **Context-stuffing** — The lightweight document Q&A approach used in the quick start: load a document and place it into the system message, instead of using a vector store.
- **Guardrails** — Application-level checks (such as keyword filters) that run before sending input to the LLM to block disallowed content.
- **Provider Safety Filters** — Built-in moderation in the model provider (e.g., GitHub Models) that can hard-block or soft-refuse unsafe content even after your guardrails pass.

## Module 01: Introduction

- **Microsoft Foundry** — Microsoft's enterprise hosting of OpenAI models on Azure, used by modules 1–6. You configure a custom endpoint, deployment name, and API key.
- **gpt-5.2** — The Microsoft Foundry model used by the introduction and later modules.
- **Spring Boot Auto-configuration** — Spring Boot's mechanism for creating beans (such as `OpenAiChatModel`) automatically from `application.yaml` properties when the matching starter is on the classpath.
- **`spring-ai-starter-model-openai`** — The Spring AI starter that auto-configures the OpenAI SDK–based `ChatModel`.
- **Stateless Chat** — A chat pattern where each request is independent and the model has no memory of prior turns.
- **Stateful Conversation** — A chat pattern where prior turns are stored in `ChatMemory` and replayed so the model can reason about earlier context.
- **Token** — The basic unit of text the model processes; words, subwords, and punctuation are split into tokens, and models impose limits on how many tokens fit in a request.
- **Context Window** — The maximum number of tokens the model can consider in a single call (input plus output).
- **Token Usage / Metadata** — Information returned in `ChatResponse` about how many input and output tokens the call consumed; useful for cost and limit monitoring.

## Module 02: Prompt Engineering

- **Prompt Engineering** — The practice of designing model inputs (instructions, examples, structure) to produce reliable, high-quality outputs.
- **Zero-Shot Prompting** — Giving the model a direct instruction with no examples and relying on its training to perform the task.
- **Few-Shot Prompting** — Including a small number of input/output examples in the prompt so the model learns the desired pattern.
- **Chain-of-Thought (CoT) Prompting** — Asking the model to show its reasoning step by step, which improves accuracy on multi-step or arithmetic problems.
- **Role-Based Prompting** — Setting a persona (e.g., "act as a senior security auditor") to shape tone, depth, and expertise of the response.
- **Reasoning Control** — A GPT-5.2 capability that lets you tell the model how much to think before answering.
- **Low Eagerness** — A reasoning-control setting for fast, direct answers; suitable for lookups and simple calculations.
- **High Eagerness** — A reasoning-control setting for thorough, deep analysis; suitable for design and research tasks.
- **Task Execution Pattern** — A prompt pattern in which the model restates the goal, plans steps, executes them, then summarizes the result.
- **Self-Reflection Pattern** — A prompt pattern in which the model critiques and refines its own draft answer.
- **Tool Preambles** — Structured (often XML-style) prompt sections such as `<task_execution>` or `<context_gathering>` that organize the model's reasoning and behavior.
- **Conversational Memory** — Using `MessageWindowChatMemory` so multi-turn prompt patterns remember earlier user and assistant messages.

## Module 03: RAG (Retrieval-Augmented Generation)

- **RAG (Retrieval-Augmented Generation)** — A pattern where relevant documents are retrieved from a knowledge base and inserted into the prompt so the model answers using your data instead of its training alone.
- **ChatClient** — Spring AI's fluent, builder-style API on top of `ChatModel`. It chains prompt, system text, tools, advisors, and memory in a readable way.
- **Advisor** — A Spring AI extension point that runs before and/or after a chat call to modify the prompt or response (e.g., to inject retrieved context or apply guardrails).
- **QuestionAnswerAdvisor** — A built-in advisor that performs RAG automatically: it queries the vector store for relevant chunks and adds them to the prompt as context.
- **Embedding** — A numerical vector representation of text where semantically similar texts produce vectors that are close together.
- **EmbeddingModel** — A Spring AI abstraction for a model that converts text into embeddings (used for search and retrieval, not for generating text).
- **VectorStore** — A storage and search abstraction for embeddings; supports adding documents and querying for the most similar ones.
- **SimpleVectorStore** — Spring AI's in-memory `VectorStore` implementation, useful for small-to-medium datasets and demos without external infrastructure.
- **Document** — Spring AI's representation of a piece of text plus metadata (source, ID, etc.) that can be embedded and stored.
- **Chunking** — Splitting larger documents into smaller, overlapping pieces that fit in the context window and embed meaningfully.
- **TokenTextSplitter** — A Spring AI splitter that chunks text by token count, with configurable chunk size and overlap.
- **Semantic Search** — Searching by meaning rather than keywords by embedding the query and finding the nearest document vectors.
- **Cosine Similarity** — The mathematical measure (0.0–1.0) used to rank how close two embedding vectors are; the basis for relevance scores in the vector store.

## Module 04: Tools

- **Tool** — A Java method exposed to the LLM as a callable capability so the model can take actions, not just produce text.
- **`@Tool` Annotation** — Marks a Java method as available to the model and supplies the natural-language description the model uses to decide when to call it.
- **`@ToolParam` Annotation** — Describes a tool's parameters (name and meaning) so the model can fill them in correctly.
- **Tool Calling / Function Calling** — The full request/response cycle in which the LLM proposes a tool and arguments, the framework executes the Java method, and the result is returned to the model to continue the conversation.
- **Tool Description** — The natural-language explanation of a tool (typically supplied via `@Tool`); precise descriptions are critical for the model to pick the right tool.
- **ReAct (Reason + Act)** — The agent loop the model follows when using tools: reason about what's needed, act by calling a tool, observe the result, then repeat or respond.
- **Tool Chaining** — When the model autonomously calls multiple tools in sequence within a single user turn (e.g., fetch weather, then convert units).
- **Multi-Turn Orchestration** — Spring AI's automatic management of the back-and-forth between the model and tool executions across one or more turns.
- **Stateless Agent** — An agent that handles each request independently, with no memory of prior interactions.
- **Stateful Agent** — An agent that uses `ChatMemory` to remember earlier interactions across turns.
- **Graceful Failure** — A tool error-handling style where exceptions are caught and returned as informative messages so the agent can recover and respond helpfully.

## Module 05: MCP (Model Context Protocol)

- **Model Context Protocol (MCP)** — An open, language-agnostic standard for letting AI clients discover and call tools that live in separate server processes.
- **MCP Server** — An application that exposes tools through MCP so any compatible client can use them.
- **MCP Client** — An application that connects to one or more MCP servers, discovers their tools, and invokes them (often on behalf of an LLM).
- **`@McpTool` Annotation** — Spring AI annotation that exposes a Java method as an MCP-discoverable tool on a server.
- **`@McpToolParam` Annotation** — Spring AI annotation (from `org.springframework.ai.mcp.annotation`) that describes an MCP tool's parameters so clients and LLMs can understand what to pass; the MCP equivalent of `@ToolParam`.
- **ToolCallbackProvider** — A Spring AI interface used by MCP clients to discover remote tools and present them to a `ChatClient` like local tools.
- **Tool Discovery** — The MCP step in which a client asks a server for its list of tools, including names, descriptions, and parameter schemas.
- **Tool Invocation** — The MCP step in which a client sends a structured call (tool name + arguments) to the server and receives the result.
- **Streamable HTTP** — The recommended modern MCP transport for web-deployed servers, using HTTP streaming (preferred over stdio for networked services).
- **stdio Transport** — An alternative MCP transport that communicates over standard input/output, useful for locally launched server processes.
- **`spring-ai-starter-mcp-server-webmvc`** — Starter that turns a Spring MVC application into an MCP server exposing `@McpTool` methods over HTTP.
- **`spring-ai-starter-mcp-client-webflux`** — Starter that lets a Spring application act as an MCP client, connecting to remote MCP servers over HTTP.
- **Direct Tool Calls vs. LLM-Orchestrated Calls** — You can either invoke MCP tools directly from code or let the LLM choose and call them via `ChatClient`; this module shows both styles.
- **Service Separation** — The architectural benefit of MCP: tools live in their own services, with independent deployment, scaling, and lifecycle from the AI application.

## Module 06: Agents

- **Agentic Workflow** — A system that orchestrates multiple LLM calls — chained, parallelized, routed, delegated, or looped — to handle tasks that a single call can't solve well.
- **Enhanced LLM** — An LLM augmented with retrieval (RAG), tools, memory, and guardrails so it becomes substantially more capable than a plain chat model.
- **Workflow vs. Agent** — A *workflow* follows a predefined orchestration of LLM calls; an *agent* dynamically decides its own next step and tool usage at runtime.
- **Chain Workflow** — A sequential pattern where each LLM call's output feeds the next (e.g., extract → standardize → sort → format).
- **Parallelization Workflow** — A pattern that runs the same or similar prompts over many inputs concurrently, or aggregates multiple parallel attempts (e.g., voting).
- **Routing Workflow** — A pattern where a classifier LLM inspects the input and dispatches it to the most appropriate specialized handler.
- **Orchestrator-Workers Pattern** — A pattern where one LLM decomposes a complex task into subtasks and delegates each to worker LLMs that run independently.
- **Evaluator-Optimizer Pattern** — An iterative loop where a generator LLM produces output, an evaluator LLM grades it (e.g., PASS / NEEDS_IMPROVEMENT / FAIL), and feedback drives the next iteration.
- **Autonomous Agent** — An LLM system that chooses its own actions and tools dynamically rather than following a fixed code path.
- **Quality Criteria** — Explicit rules an evaluator uses to decide whether a generated answer is acceptable or needs another iteration.
- **Latency vs. Accuracy Trade-off** — Simple workflows are faster but less thorough; multi-step agentic workflows are slower but typically produce higher-quality results.

## Cross-Cutting Terms

These terms appear in multiple modules and are worth knowing throughout the course.

- **LLM (Large Language Model)** — A neural network trained on large amounts of text that can generate and reason about natural language.
- **Provider** — The service that hosts the LLM (GitHub Models, Microsoft Foundry, etc.); Spring AI lets you switch providers without changing application code.
- **Dev Container / Codespaces** — The pre-configured development environment for this course (Java 21, Maven 3.9+, Copilot) that you can launch as a GitHub Codespace.
- **Maven** — The Java build tool used to compile and run every example (`mvn compile exec:java -Dexec.mainClass=...`).
- **`.env` / `GITHUB_TOKEN`** — The environment file and variable used by the quick start to authenticate against GitHub Models.
- **`application.yaml`** — The Spring Boot configuration file used by modules 1–6 to wire up Microsoft Foundry credentials, model names, and other settings.
