"""
Regenerate spring-boot-wiring.png for Module 04.
Replaces LangChain4j @AiService architecture with Spring AI ChatClient.
"""
import json, base64, urllib.request, os

from azure.identity import DefaultAzureCredential

endpoint = "https://aoai-x5xg67c2eo74m.openai.azure.com/"
deployment = "gpt-image-1.5"
api_version = "2025-04-01-preview"

cred = DefaultAzureCredential()
token = cred.get_token("https://cognitiveservices.azure.com/.default").token

url = f"{endpoint}openai/deployments/{deployment}/images/generations?api-version={api_version}"

DARK_STYLE = """Create a clean, modern technical diagram in a 16:9 presentation-slide format.
Background: deep navy-to-indigo gradient with a subtle electric blue bloom in the center and darker vignetted edges.
Faint horizontal guide lines for a futuristic control interface look.
Containers: rounded rectangular panels with soft corners, thin glowing outlines, and subtle transparent fills — like glass-tech UI cards floating over the dark background.
Typography: bold geometric sans-serif, white text with selected keywords in cyan or light blue, slight glow but sharp and legible.
Labels: glossy pill-shaped tabs with a slight bevel, faint gradient, and outer glow matching each panel accent color.
Arrows/connections: thin glowing lines with soft bloom, using cyan/teal for primary flow and dimmer lines for secondary paths.
Icons: simple vector workflow icons with soft inner glow and subtle beveling, illuminated from within.
Overall: vector-clean, symmetrical, grid-aligned, premium digital feel. Not painterly, not hand-drawn, not photorealistic. No decorative clutter."""

prompt = f"""{DARK_STYLE}

Diagram: SPRING AI CHATCLIENT ARCHITECTURE — how ChatClient wires tools, memory, and the LLM together.
Title at top center: "Spring AI ChatClient Architecture" in a glowing cyan plaque.

Layout (center hub with three connected panels):

CENTER — a large rounded panel labeled "ChatClient" in bold cyan text, with the Spring Boot leaf icon (green, glowing).
  Inside the panel, show the fluent API call chain in white monospace text:
    chatClient.prompt()
      .system(systemPrompt)
      .user(message)
      .tools(weatherTool, temperatureTool)
      .advisors(memoryAdvisor)
      .call()
      .content()
  Subtitle below the code: "Fluent API — configures everything per request" in smaller white text.

TOP — a rounded panel labeled "ChatModel (Azure OpenAI)" in a blue pill tab.
  Inside: "OpenAiSdkChatModel" in white text.
  Subtitle: "Connects to the LLM provider" with a small cloud icon.
  Glowing cyan arrow from ChatClient UP to ChatModel, labeled "API Calls".

LEFT — a rounded panel labeled "@Tool Components" in a green pill tab.
  Inside, list two items in white text:
    - WeatherTool
    - TemperatureTool
  Subtitle: "Passed via .tools()" in smaller green text.
  Below that: small annotation "@Tool + @ToolParam annotations" in dimmer text.
  Glowing green arrow from this panel RIGHT to the ChatClient center panel.

RIGHT — a rounded panel labeled "ChatMemory" in a purple pill tab.
  Inside: "MessageWindowChatMemory" in white text.
  Subtitle: "Sliding window per session" in smaller purple text.
  Below that: small annotation "via MessageChatMemoryAdvisor" in dimmer text.
  Glowing purple arrow from this panel LEFT to the ChatClient center panel.

BOTTOM — a smaller caption box or floating label:
  "Zero boilerplate — Spring Boot auto-configures ChatClient.Builder" in white text with a subtle glow.

NO mention of LangChain4j, @AiService, @MemoryId, @UserMessage, or ChatMemoryProvider anywhere.
Keep it clean, symmetrical, grid-aligned. The ChatClient panel should be visually dominant in the center."""

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

resp = urllib.request.urlopen(req, timeout=120)
result = json.loads(resp.read())
img_b64 = result["data"][0]["b64_json"]

output_path = "04-tools/images/spring-boot-wiring.png"
os.makedirs(os.path.dirname(output_path), exist_ok=True)
with open(output_path, "wb") as f:
    f.write(base64.b64decode(img_b64))

print(f"Saved: {output_path}")
