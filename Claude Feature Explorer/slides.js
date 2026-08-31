/*
 * Slide content for the Claude Feature Explorer.
 * Each slide is plain data — index.html/app.js turn it into markup.
 * `kind` picks the layout: title | concept | code | terminal | demoChat | demoTool | links
 */

const SLIDES = [
  {
    kind: "title",
    eyebrow: "An interactive tour",
    title: "Claude Feature Explorer",
    subtitle:
      "A slideshow that teaches Claude's core features by showing the actual request you'd send and the actual response you'd get back — not just descriptions.",
    footer: "Use ← → or the buttons below to move. Press “S” any time to jump to the overview.",
  },

  {
    kind: "concept",
    section: "1 / Orientation",
    title: "What is Claude?",
    subtitle:
      "Claude is a family of AI models by Anthropic, available through claude.ai, the Claude API, Claude Code (a terminal coding agent), and Claude apps for desktop and mobile.",
    bullets: [
      "You talk to Claude by exchanging turns: you send a message, Claude sends one back. That exchange is the foundation every feature in this deck builds on.",
      "The same underlying API powers a chat website, a coding assistant, and custom apps you build yourself — the difference is what tools and instructions surround it.",
      "Different models trade off speed, cost, and depth of reasoning, so you pick the one that fits the task.",
    ],
    table: {
      caption: "The current Claude 5 model family",
      columns: ["Model", "Best for"],
      rows: [
        ["Claude Opus 5", "The hardest, highest-stakes problems — deep multi-step reasoning, research, complex coding"],
        ["Claude Sonnet 5", "The default workhorse — strong reasoning at practical speed and cost"],
        ["Claude Fable 5", "Creative and narrative work — writing, storytelling, expressive tone"],
        ["Claude Haiku 4.5", "High-volume or latency-sensitive tasks — fast, lightweight, cheap"],
      ],
    },
  },

  {
    kind: "code",
    section: "2 / The basics",
    title: "Talking to Claude: the Messages API",
    subtitle:
      "Every conversation with Claude — chat apps, coding agents, everything — is built on one HTTP call: POST /v1/messages. You send a model name, a token budget, and a list of messages; Claude sends back a message.",
    request: `POST https://api.anthropic.com/v1/messages
Content-Type: application/json
x-api-key: $ANTHROPIC_API_KEY
anthropic-version: 2023-06-01

{
  "model": "claude-sonnet-5",
  "max_tokens": 1024,
  "messages": [
    {
      "role": "user",
      "content": "Explain what a REST API is in two sentences."
    }
  ]
}`,
    response: `{
  "id": "msg_01XFDUDYJgAACzvnptvVoYEL",
  "type": "message",
  "role": "assistant",
  "model": "claude-sonnet-5",
  "content": [
    {
      "type": "text",
      "text": "A REST API lets two systems talk over HTTP using a small set of operations (GET, POST, PUT, DELETE) applied to named resources, like /users/42. Each request is stateless — the server keeps no memory of previous requests, so the client sends everything needed to understand it every time."
    }
  ],
  "stop_reason": "end_turn",
  "usage": {
    "input_tokens": 15,
    "output_tokens": 61
  }
}`,
    note: "You consume the output by reading response.content[0].text. stop_reason tells you why generation ended — “end_turn” means Claude finished naturally.",
  },

  {
    kind: "code",
    section: "3 / The basics",
    title: "Multi-turn conversations",
    subtitle:
      "The API has no memory between calls. To continue a conversation, you resend the whole history — your last message, Claude's reply, and your new message — with roles alternating user / assistant.",
    request: `{
  "model": "claude-sonnet-5",
  "max_tokens": 1024,
  "messages": [
    { "role": "user", "content": "What's the capital of Portugal?" },
    { "role": "assistant", "content": "Lisbon." },
    { "role": "user", "content": "And what river runs through it?" }
  ]
}`,
    response: `{
  "id": "msg_01Q7mZBn2rGx8fVv3aLskT9C",
  "type": "message",
  "role": "assistant",
  "content": [
    { "type": "text", "text": "The Tagus River runs through Lisbon, reaching the Atlantic Ocean just west of the city." }
  ],
  "stop_reason": "end_turn",
  "usage": { "input_tokens": 34, "output_tokens": 22 }
}`,
    note: "Your app is responsible for storing and resending history. This is also exactly why cost grows with conversation length — see the prompt caching slide later.",
  },

  {
    kind: "code",
    section: "4 / Understanding input",
    title: "Vision: reading images",
    subtitle:
      "Messages aren't limited to text. A message's content can be a list of blocks — mix images and text in one turn, and Claude reasons about both together.",
    request: `{
  "model": "claude-sonnet-5",
  "max_tokens": 300,
  "messages": [
    {
      "role": "user",
      "content": [
        {
          "type": "image",
          "source": {
            "type": "base64",
            "media_type": "image/png",
            "data": "iVBORw0KGgoAAAANSUhEUgAA...(truncated)"
          }
        },
        { "type": "text", "text": "What's the trend in this chart, and what quarter should I worry about?" }
      ]
    }
  ]
}`,
    response: `{
  "id": "msg_01Mk4pTn7hWc2eXq9DfRz3Aa",
  "type": "message",
  "role": "assistant",
  "content": [
    {
      "type": "text",
      "text": "Revenue climbs steadily through Q1–Q3, then drops about 18% in Q4 — that's the quarter to dig into. Everything before it points up and to the right."
    }
  ],
  "stop_reason": "end_turn",
  "usage": { "input_tokens": 1204, "output_tokens": 38 }
}`,
    note: "Image tokens scale with resolution, which is why usage.input_tokens jumps here — a rough rule of thumb is (width × height) / 750.",
  },

  {
    kind: "demoTool",
    section: "5 / Taking action",
    title: "Tool use: letting Claude call your code",
    subtitle:
      "Claude doesn't execute anything itself. You describe functions it can ask for with a JSON schema; Claude decides when to request one, you run the real code, and you hand the result back. Step through the exchange below.",
    steps: [
      {
        label: "1. You define a tool and send a message",
        code: `{
  "model": "claude-sonnet-5",
  "max_tokens": 1024,
  "tools": [
    {
      "name": "get_weather",
      "description": "Get the current weather for a location",
      "input_schema": {
        "type": "object",
        "properties": {
          "location": { "type": "string", "description": "City and state, e.g. Austin, TX" },
          "unit": { "type": "string", "enum": ["celsius", "fahrenheit"] }
        },
        "required": ["location"]
      }
    }
  ],
  "messages": [
    { "role": "user", "content": "What's the weather in Austin, TX right now?" }
  ]
}`,
      },
      {
        label: "2. Claude replies asking to call the tool",
        code: `{
  "id": "msg_01T7z...",
  "role": "assistant",
  "content": [
    { "type": "text", "text": "I'll check that for you." },
    {
      "type": "tool_use",
      "id": "toolu_01T1xJp3vQe8mHk",
      "name": "get_weather",
      "input": { "location": "Austin, TX", "unit": "fahrenheit" }
    }
  ],
  "stop_reason": "tool_use"
}`,
      },
      {
        label: "3. Your code runs get_weather() and you send the result back",
        code: `{
  "model": "claude-sonnet-5",
  "max_tokens": 1024,
  "tools": [ /* same tool definition as step 1 */ ],
  "messages": [
    { "role": "user", "content": "What's the weather in Austin, TX right now?" },
    { "role": "assistant", "content": [ /* the tool_use block from step 2 */ ] },
    {
      "role": "user",
      "content": [
        {
          "type": "tool_result",
          "tool_use_id": "toolu_01T1xJp3vQe8mHk",
          "content": "82°F, sunny, light breeze from the SE"
        }
      ]
    }
  ]
}`,
      },
      {
        label: "4. Claude turns the tool result into an answer",
        code: `{
  "id": "msg_01Vb9...",
  "role": "assistant",
  "content": [
    { "type": "text", "text": "It's 82°F and sunny in Austin right now, with a light breeze from the southeast — a nice day to be outside." }
  ],
  "stop_reason": "end_turn"
}`,
      },
    ],
    note: "This loop — define tools, get a tool_use request, run real code, send a tool_result — is the mechanism behind everything from “check my calendar” bots to Claude Code editing files on your machine.",
  },

  {
    kind: "code",
    section: "6 / Deeper reasoning",
    title: "Extended thinking",
    subtitle:
      "For hard problems, Claude can be given a budget of tokens to reason in before answering. That reasoning is returned as its own block, separate from the final answer, so you can inspect it.",
    request: `{
  "model": "claude-opus-5",
  "max_tokens": 2048,
  "thinking": { "type": "enabled", "budget_tokens": 1024 },
  "messages": [
    { "role": "user", "content": "A train leaves city A at 60mph heading to city B, 300 miles away. Another leaves city B at 40mph heading to city A, 20 minutes later. When and where do they meet?" }
  ]
}`,
    response: `{
  "id": "msg_01Hn3...",
  "role": "assistant",
  "content": [
    {
      "type": "thinking",
      "thinking": "Let t = hours after train A departs. Train A position: 60t. Train B departs at t=1/3 hr, position from B: 40(t - 1/3), so from A: 300 - 40(t - 1/3). Set equal: 60t = 300 - 40(t-1/3) ... solving gives t = 3.13 hr, position ≈ 188 miles from A.",
      "signature": "EqACCkYIBRgCKkA3s...(redacted)"
    },
    {
      "type": "text",
      "text": "They meet about 3 hours 8 minutes after train A departs, roughly 188 miles from city A."
    }
  ],
  "stop_reason": "end_turn",
  "usage": { "input_tokens": 68, "output_tokens": 412 }
}`,
    note: "You typically show only the final text block to end users, but the thinking block is there if you want to log or display Claude's reasoning.",
  },

  {
    kind: "code",
    section: "7 / Efficiency",
    title: "Prompt caching",
    subtitle:
      "Resending a long system prompt or reference document on every turn is slow and expensive. Mark a block cache_control, and Anthropic caches it — later requests reusing it are cheaper and faster.",
    request: `{
  "model": "claude-sonnet-5",
  "max_tokens": 1024,
  "system": [
    {
      "type": "text",
      "text": "You are a support agent for Acme Cloud. Reference manual: \\n\\n[... 40,000 tokens of docs ...]",
      "cache_control": { "type": "ephemeral" }
    }
  ],
  "messages": [
    { "role": "user", "content": "How do I rotate an API key?" }
  ]
}`,
    response: `{
  "id": "msg_01Zp8...",
  "role": "assistant",
  "content": [
    { "type": "text", "text": "Go to Settings → API Keys, click Rotate next to the key, and confirm. The old key stays valid for 24 hours so in-flight requests don't break." }
  ],
  "stop_reason": "end_turn",
  "usage": {
    "input_tokens": 12,
    "cache_creation_input_tokens": 41300,
    "cache_read_input_tokens": 0,
    "output_tokens": 34
  }
}`,
    note: "First call pays cache_creation cost. Every call afterward within the cache window shows cache_read_input_tokens instead — billed at a fraction of the normal input price.",
  },

  {
    kind: "code",
    section: "8 / Real-time output",
    title: "Streaming responses",
    subtitle:
      "Instead of waiting for the whole reply, set \"stream\": true and Claude sends the response as a sequence of Server-Sent Events, so you can render text as it's generated.",
    request: `{
  "model": "claude-sonnet-5",
  "max_tokens": 200,
  "stream": true,
  "messages": [
    { "role": "user", "content": "Say hello in three languages." }
  ]
}`,
    response: `event: message_start
data: {"type":"message_start","message":{"id":"msg_01...","model":"claude-sonnet-5","usage":{"input_tokens":14}}}

event: content_block_start
data: {"type":"content_block_start","index":0,"content_block":{"type":"text","text":""}}

event: content_block_delta
data: {"type":"content_block_delta","index":0,"delta":{"type":"text_delta","text":"Hello"}}

event: content_block_delta
data: {"type":"content_block_delta","index":0,"delta":{"type":"text_delta","text":"! Bonjour! ¡Hola!"}}

event: content_block_stop
data: {"type":"content_block_stop","index":0}

event: message_delta
data: {"type":"message_delta","delta":{"stop_reason":"end_turn"},"usage":{"output_tokens":9}}

event: message_stop
data: {"type":"message_stop"}`,
    note: "Each content_block_delta carries a fragment of text — append them in order and you get the full reply, appearing token by token.",
  },

  {
    kind: "terminal",
    section: "9 / Agentic coding",
    title: "Claude Code: an agent in your terminal",
    subtitle:
      "Claude Code is a CLI agent that reads your repo, edits files, runs commands, and iterates — the same tool-use loop from slide 5, but with tools like Read, Edit, and Bash instead of get_weather.",
    bullets: [
      "CLAUDE.md — a file in your repo Claude reads automatically for project-specific context and conventions.",
      "Slash commands — reusable prompts you invoke with /name, like /code-review or a custom one your team writes.",
      "Hooks — shell commands the harness runs on events (before a tool call, after Claude stops) for things like auto-formatting.",
      "Subagents — specialized agents Claude can delegate a task to, each with its own tools and instructions.",
    ],
    transcript: `$ claude
> Fix the failing test in src/parser.test.ts

  Reading src/parser.test.ts...
  Reading src/parser.ts...

  The test expects parseDate("2026-02-30") to throw, but the
  parser currently clamps invalid dates instead of rejecting them.

  Editing src/parser.ts...
    - if (day > daysInMonth) day = daysInMonth;
    + if (day > daysInMonth) throw new Error("Invalid date");

  Running: npm test -- parser.test.ts
    ✓ 3 passed

  Done — parser.ts now rejects invalid days instead of clamping them.`,
  },

  {
    kind: "code",
    section: "10 / Building agents",
    title: "The Claude Agent SDK",
    subtitle:
      "The same loop that powers Claude Code — read context, call tools, act, repeat — is available as an SDK, so you can build your own custom agents instead of using the CLI directly.",
    request: `// TypeScript — Claude Agent SDK
import { query } from "@anthropic-ai/claude-agent-sdk";

for await (const message of query({
  prompt: "Summarize open issues labeled 'bug' and file a triage report.",
  options: {
    allowedTools: ["Read", "Grep", "WebSearch"],
    model: "claude-sonnet-5",
  },
})) {
  if (message.type === "assistant") {
    console.log(message.content);
  }
}`,
    response: `// Streamed message objects, in order:

{ type: "assistant", content: "Searching the issue tracker for label:bug..." }
{ type: "tool_use", name: "Grep", input: { pattern: "label:bug" } }
{ type: "tool_result", content: "14 open issues found" }
{ type: "assistant", content: "Found 14 bugs. Reading the top 5 by activity..." }
{ type: "assistant", content: "## Triage Report\\n\\n3 are duplicates of #482..." }
{ type: "result", subtype: "success", num_turns: 6 }`,
    note: "The SDK handles the tool-call loop, context management, and permissions for you — you supply the tools and the goal.",
  },

  {
    kind: "code",
    section: "11 / Connecting to the world",
    title: "MCP: the Model Context Protocol",
    subtitle:
      "Instead of wiring every tool into every app by hand, MCP defines a standard way for a Claude-based app (the “host”) to connect to servers that expose tools, so any MCP server works with any MCP-compatible host.",
    request: `// What an MCP server advertises when a host connects
{
  "method": "tools/list",
  "result": {
    "tools": [
      {
        "name": "search_issues",
        "description": "Search GitHub issues by query",
        "inputSchema": {
          "type": "object",
          "properties": {
            "query": { "type": "string" },
            "repo": { "type": "string" }
          },
          "required": ["query", "repo"]
        }
      }
    ]
  }
}`,
    response: `// Claude Code, Claude Desktop, and custom SDK apps
// can all use this exact server unmodified:

$ claude mcp add github -- npx -y @modelcontextprotocol/server-github

// Once connected, "search_issues" shows up as an ordinary
// tool Claude can call — same tool_use / tool_result loop
// from slide 5, just served over MCP instead of hand-wired.`,
    note: "One server, many hosts. That's the whole pitch: write the integration once, use it from any MCP-compatible client.",
  },

  {
    kind: "code",
    section: "12 / Acting in a UI",
    title: "Computer use",
    subtitle:
      "For tasks with no API, Claude can be given a virtual screen and asked to operate it directly — looking at screenshots and issuing mouse/keyboard actions as tool calls.",
    request: `{
  "model": "claude-opus-5",
  "max_tokens": 1024,
  "tools": [
    { "type": "computer_20250124", "name": "computer", "display_width_px": 1280, "display_height_px": 800 }
  ],
  "messages": [
    { "role": "user", "content": "Open the Files app and rename report.txt to report-final.txt" }
  ]
}`,
    response: `{
  "role": "assistant",
  "content": [
    { "type": "text", "text": "I'll take a screenshot first to see the current screen." },
    {
      "type": "tool_use",
      "id": "toolu_02Ab...",
      "name": "computer",
      "input": { "action": "screenshot" }
    }
  ],
  "stop_reason": "tool_use"
}

// Your code returns the screenshot as an image tool_result.
// Claude then requests the next action, e.g.:
//   { "action": "left_click", "coordinate": [640, 320] }
//   { "action": "type", "text": "report-final.txt" }
// One action per turn, until the task is done.`,
    note: "This is how an agent can use software that was never built with an API in mind — it operates the same pixels a person would.",
  },

  {
    kind: "demoChat",
    section: "13 / Try it yourself",
    title: "A simulated conversation",
    subtitle:
      "This is a scripted, offline demo — not a live call to Claude — but it shows the same request/response cycle from slide 2 happening turn by turn. Try one of the prompts.",
    suggestions: [
      "Explain quantum entanglement simply",
      "Write a haiku about debugging",
      "What's a good default HTTP timeout?",
    ],
    canned: {
      "explain quantum entanglement simply":
        "Two particles can become linked so that measuring one instantly tells you something about the other, no matter how far apart they are — not because a signal travels between them, but because they were never really separate to begin with.",
      "write a haiku about debugging":
        "Red test turns to green\none line was never the bug —\nit was my assumption",
      "what's a good default http timeout?":
        "A common default is 30 seconds for the overall request, with a shorter connect timeout (3–5s) so a dead server fails fast instead of hanging the whole budget.",
    },
    fallback:
      "(Simulated) I don't have a scripted answer for that exact prompt — try one of the suggestions above, or imagine this is where Claude's real reply would stream in.",
  },

  {
    kind: "links",
    section: "14 / Next steps",
    title: "Where to go from here",
    subtitle: "This deck covered the shape of things. These are the real docs and tools to go deeper.",
    items: [
      { label: "Claude Developer Platform docs", desc: "Full Messages API reference, guides, and cookbook", url: "https://docs.claude.com" },
      { label: "Claude Console", desc: "Get an API key and try requests interactively", url: "https://console.anthropic.com" },
      { label: "claude.ai", desc: "Chat with Claude directly, no code required", url: "https://claude.ai" },
      { label: "Claude Code", desc: "Install the terminal agent shown on slide 9", url: "https://claude.com/product/claude-code" },
      { label: "Model Context Protocol", desc: "Spec and server directory for MCP", url: "https://modelcontextprotocol.io" },
    ],
    footer: "Press “R” to restart the deck from the beginning.",
  },
];
