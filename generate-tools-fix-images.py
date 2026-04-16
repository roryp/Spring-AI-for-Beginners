"""
Regenerate all Module 04 images that still contain LangChain4j references.
Uses LIGHT_STYLE (white background, corporate presentation) to match existing images.

Images to regenerate:
1. tool-definitions-anatomy.png — title says "LangChain4j", uses @P, @AiService
2. session-management.png — uses @MemoryId, "AI Service"
3. spring-boot-sequence.png — uses "Assistant Proxy"
4. tool-calling-sequence.png — uses "MathAssistant Proxy"
5. spring-boot-wiring.png — content correct but wrong style (was dark, should be light)
"""
import json, base64, urllib.request, os, time

from azure.identity import DefaultAzureCredential

endpoint = "https://aoai-x5xg67c2eo74m.openai.azure.com/"
deployment = "gpt-image-1.5"
api_version = "2025-04-01-preview"

cred = DefaultAzureCredential()
token = cred.get_token("https://cognitiveservices.azure.com/.default").token

url = f"{endpoint}openai/deployments/{deployment}/images/generations?api-version={api_version}"

LIGHT_STYLE = """Create a clean, professional technical diagram in a 16:9 presentation-slide format.
Background: pure white or very light cream, clean and uncluttered.
Title: bold, large, dark navy-blue sans-serif text centered at top.
Decorative lines: thin horizontal rule lines in dark blue or gold/amber running across the full width, used as section separators above and below the title area.
Containers: rounded rectangles with thin blue or gray borders, white fill, optional very subtle drop shadow.
Typography: clean geometric sans-serif. Dark navy blue for headers, dark gray for body text.
Accent colors: warm orange/coral for highlights and emphasis, navy blue for primary labels and arrows, amber/yellow for placeholder highlights, teal/green for Spring-related elements.
Arrows: solid dark blue directional arrows showing data flow, clean and sharp.
Labels: clean text in colored pill-shaped or rounded-rectangle badges where appropriate.
Overall: professional educational slide aesthetic — clean, minimal, corporate presentation quality. NOT dark themed. No glow effects. No gradient backgrounds. No futuristic styling. Think PowerPoint / Keynote professional deck."""

prompts = {

    # ── 1. tool-definitions-anatomy.png ──────────────────────
    # FIX: "LangChain4j" in title, @P annotation, @AiService → Spring AI @Tool, @ToolParam, ChatClient
    "04-tools/images/tool-definitions-anatomy.png": f"""{LIGHT_STYLE}

Diagram: ANATOMY OF A TOOL DEFINITION IN SPRING AI.
Title at top center: "Anatomy of a Tool Definition in Spring AI" in large bold dark navy text.
Thin decorative horizontal lines above and below the title.

CENTER — a dark-themed code editor panel (dark gray/charcoal background, like VS Code) showing this Java code with syntax highlighting:

    public class WeatherTool {{

        @Tool("Get the current weather for a given location")
        public String getWeather(@ToolParam("Location name") String location) {{
            // ... fetch weather data
            return weatherInfo;
        }}
    }}

Use syntax coloring: green for @Tool and @ToolParam annotations, white for keywords like public/class/String, orange/amber for the string literals in quotes, gray for comments.

Callout annotations pointing to specific parts of the code (use arrows from labels outside the code box pointing inward):

TOP-RIGHT callout (blue label box): "Description helps the AI decide WHEN to use this tool" — arrow pointing to the @Tool annotation string.

LEFT callout (green label box): "Spring Boot auto-discovers this as a tool" — arrow pointing to the class declaration.

LEFT-LOWER callout (teal label box): "Parameter description tells AI WHAT to pass" — arrow pointing to @ToolParam.

RIGHT callout (blue label box): "Return value goes back to the model as context" — arrow pointing to the return statement.

BELOW the code editor — a separate smaller panel showing the ChatClient usage:

    chatClient.prompt()
        .tools(new WeatherTool())
        .user(message)
        .call().content();

With a label below: "ChatClient passes tool instances via .tools()" in dark blue text.

NO mention of LangChain4j, @AiService, @P, @Component, or ChatMemoryProvider anywhere.
Keep it clean, professional, white background.""",


    # ── 2. session-management.png ────────────────────────────
    # FIX: @MemoryId, "AI Service" → ChatMemory with session IDs
    "04-tools/images/session-management.png": f"""{LIGHT_STYLE}

Diagram: SESSION MANAGEMENT WITH CHATMEMORY.
Title at top center: "Session Management with ChatMemory" in large bold dark navy text.
Thin decorative horizontal lines above and below the title.

Layout (left → center → right):

LEFT SIDE — three user icons stacked vertically, each with a different color accent:
  User A (blue) with small chat bubble icons
  User B (green/teal) with small chat bubble icons
  User C (navy/dark) with small chat bubble icons

CENTER — a large rounded rectangle labeled "ChatClient + ChatMemory" in bold dark blue text.
  Inside, show: "MessageWindowChatMemory" in smaller text.
  Below that, three bullet points:
    - Each session has isolated conversation history
    - Users cannot see each other messages
    - Session ID maps to unique memory window

  Dark blue arrows from each user pointing to the center box.

RIGHT SIDE — three separate memory store boxes, each connected from the center:
  Top box: "session-abc123" header in blue pill tab, containing "Message 1, Message 2"
  Middle box: "session-def456" header in green pill tab, containing "Message A, Message B"
  Bottom box: "session-ghi789" header in navy pill tab, containing "Message X, Message Y"

  Arrows from center to each memory store.

BOTTOM — a code snippet in a small rounded box:
    chatMemory.add(sessionId, userMessage);
    chatMemory.get(sessionId, lastN);
  With label: "Routes to the correct memory store" and an arrow pointing right.

NO mention of @MemoryId, AI Service, @AiService, LangChain4j, or ChatMemoryProvider anywhere.
Keep it clean, white background, matching the professional slide style.""",


    # ── 3. spring-boot-wiring.png ────────────────────────────
    # FIX: Regenerated with dark style, should be LIGHT style
    "04-tools/images/spring-boot-wiring.png": f"""{LIGHT_STYLE}

Diagram: SPRING AI CHATCLIENT ARCHITECTURE.
Title at top center: "Spring AI ChatClient Architecture" in large bold dark navy text.
Thin decorative horizontal lines above and below the title.

Layout (center hub with three connected panels):

TOP — a rounded rectangle with light blue fill labeled "ChatModel (Azure OpenAI)" in dark blue text.
  Inside: "OpenAiSdkChatModel" in smaller text.
  Subtitle: "Connects to the LLM provider" with a small cloud icon.

CENTER — a larger rounded rectangle with a subtle green/teal border, labeled "ChatClient" in bold dark blue text, with the Spring Boot leaf icon (green).
  Inside the panel, show the fluent API call chain in dark monospace text:
    chatClient.prompt()
      .system(systemPrompt)
      .user(message)
      .tools(weatherTool, temperatureTool)
      .advisors(memoryAdvisor)
      .call()
      .content()
  Subtitle: "Fluent API — configures everything per request" in smaller gray text.

LEFT — a rounded rectangle with green accent, labeled "@Tool Components" in dark blue.
  Inside, list:
    - WeatherTool
    - TemperatureTool
  Subtitle: "Passed via .tools()" in green text.
  Below: "@Tool + @ToolParam annotations" in muted gray.

RIGHT — a rounded rectangle with purple accent, labeled "ChatMemory" in dark blue.
  Inside: "MessageWindowChatMemory" in text.
  Subtitle: "Sliding window per session" in purple text.
  Below: "via MessageChatMemoryAdvisor" in muted gray.

Arrows:
  - Dark blue arrow from ChatClient UP to ChatModel, labeled "API Calls"
  - Green arrow from @Tool Components RIGHT to ChatClient
  - Purple arrow from ChatMemory LEFT to ChatClient

BOTTOM — caption: "Zero boilerplate — Spring Boot auto-configures ChatClient.Builder" in dark blue text.

NO mention of LangChain4j, @AiService, @MemoryId, @UserMessage, or ChatMemoryProvider anywhere.
White background, clean professional style.""",


    # ── 4. spring-boot-sequence.png ──────────────────────────
    # FIX: "Assistant Proxy" → "AgentService / ChatClient"
    "04-tools/images/spring-boot-sequence.png": f"""{LIGHT_STYLE}

Diagram: UML SEQUENCE DIAGRAM — Spring Boot Tool Calling Lifecycle.
Title: no explicit title, this is a clean UML sequence diagram.

Five participants across the top as light purple/lavender rounded rectangles:
  "HTTP Client" | "Controller" | "AgentService" | "Azure OpenAI" | "WeatherTool"

The sequence (top to bottom, with arrows between participants):

1. HTTP Client → Controller: solid arrow labeled 'POST /api/agent/chat'
2. Controller → AgentService: solid arrow labeled 'chat(sessionId, "Weather in Paris?")'
3. AgentService → Azure OpenAI: solid arrow labeled 'message + tool schemas'
4. Azure OpenAI → AgentService: dashed return arrow labeled 'call getCurrentWeather("Paris")'
5. AgentService → WeatherTool: solid arrow labeled 'WeatherTool.getCurrentWeather("Paris")'
6. WeatherTool → AgentService: dashed return arrow labeled '"22 degrees C and sunny"'
7. AgentService → Azure OpenAI: solid arrow labeled 'tool result'
8. Azure OpenAI → AgentService: dashed return arrow labeled '"The weather in Paris is 22 degrees C and sunny."'
9. AgentService → Controller: dashed return arrow labeled 'answer'
10. Controller → HTTP Client: dashed return arrow labeled 'HTTP 200 JSON'

Same five participants repeated at the bottom as light purple rectangles.
Vertical dashed lifelines connecting top and bottom participant boxes.

Style: clean UML sequence diagram. Solid arrows for calls, dashed arrows for returns.
Light purple/lavender participant boxes. Black text. White background.
NO mention of Assistant Proxy, @AiService, LangChain4j anywhere.""",


    # ── 5. tool-calling-sequence.png ─────────────────────────
    # FIX: "MathAssistant Proxy" → "ChatClient"
    "04-tools/images/tool-calling-sequence.png": f"""{LIGHT_STYLE}

Diagram: UML SEQUENCE DIAGRAM — Tool Calling Loop from Quick Start Demo.
Title: no explicit title, clean UML sequence diagram.

Four participants across the top as light purple/lavender rounded rectangles:
  "main()" | "ChatClient" | "LLM" | "Calculator"

The sequence (top to bottom):

1. main() → ChatClient: solid arrow labeled 'chat("42 plus 58?")'
2. ChatClient → LLM: solid arrow labeled 'message + tool schemas'
3. LLM → ChatClient: dashed return arrow labeled 'call add(42, 58)'
4. ChatClient → Calculator: solid arrow labeled 'Calculator.add(42, 58)'
5. Calculator → ChatClient: dashed return arrow labeled '100.0'
6. ChatClient → LLM: solid arrow labeled 'tool result: 100.0'
7. LLM → ChatClient: dashed return arrow labeled '"42 plus 58 equals 100"'
8. ChatClient → main(): dashed return arrow labeled '"42 plus 58 equals 100"'

Same four participants repeated at the bottom.
Vertical dashed lifelines connecting top and bottom.

Style: clean UML sequence diagram. Solid arrows for calls, dashed for returns.
Light purple/lavender participant boxes. Black text. White background.
NO mention of MathAssistant Proxy, AiServices, @AiService, LangChain4j anywhere.""",
}


# ─────────────────────────────────────────────────────────────
# Generate all images
# ─────────────────────────────────────────────────────────────
for filepath, prompt in prompts.items():
    print(f"\n{'='*60}")
    print(f"Generating: {filepath}")
    print(f"{'='*60}")

    body = json.dumps({
        "prompt": prompt,
        "n": 1,
        "size": "1536x1024",
        "quality": "high",
        "output_format": "png"
    }).encode()

    req = urllib.request.Request(url, data=body, headers={
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    })

    try:
        resp = urllib.request.urlopen(req, timeout=180)
        result = json.loads(resp.read())
        img_b64 = result["data"][0]["b64_json"]

        os.makedirs(os.path.dirname(filepath), exist_ok=True)
        with open(filepath, "wb") as f:
            f.write(base64.b64decode(img_b64))

        print(f"  -> Saved: {filepath}")
    except Exception as e:
        print(f"  -> ERROR: {e}")

    # Brief pause between API calls
    time.sleep(2)

print("\nDone! All images regenerated.")
