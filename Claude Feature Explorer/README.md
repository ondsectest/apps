# Claude Feature Explorer

A slideshow, in the browser, that teaches Claude's core features by showing the actual request
you'd send and the actual response you'd get back — not just prose describing what a feature does.

Static HTML/CSS/JS, no build step, no dependencies, no API key required. Every code sample on
every slide is fictional-but-realistic: shaped exactly like a real Messages API call, but no
network request is ever made.

## What it covers

15 slides, in order:

1. Title
2. The Claude model family (Opus 5 / Sonnet 5 / Fable 5 / Haiku 4.5) and what each is for
3. The Messages API — one request, one response
4. Multi-turn conversations and why history has to be resent each time
5. Vision — mixing image and text content blocks
6. Tool use — a four-step walkthrough of the define → request → run → result loop
7. Extended thinking — the `thinking` block and its token budget
8. Prompt caching — `cache_control` and the usage fields it changes
9. Streaming — the SSE event sequence behind token-by-token output
10. Claude Code — CLAUDE.md, slash commands, hooks, subagents, with a sample terminal transcript
11. The Claude Agent SDK — building a custom agent instead of using the CLI
12. MCP (Model Context Protocol) — one server, many hosts
13. Computer use — operating a screen via screenshots and coordinate-based actions
14. A simulated chat demo — scripted, offline, but shows the turn-taking shape live
15. Links to the real docs, console, and tools

## Design

- Plain data + rendering: all slide content lives in `slides.js` as one array of objects. Adding a
  slide means adding an object; the layout is picked by its `kind` field
  (`title | concept | code | terminal | demoTool | demoChat | links`).
- `app.js` renders the current slide, wires up its interactive bits (copy buttons, the tool-use
  step stepper, the chat input), and handles navigation — arrow keys, on-screen buttons, dot
  indicators, and an overview modal (press `S`).
- JSON in the code panels is syntax-highlighted with a small regex tokenizer — no highlighting
  library.
- Light/dark theme follows the OS by default; the toggle in the top bar overrides it and
  remembers the choice in `localStorage`.

## Running it

See [INSTALL.md](./INSTALL.md).
