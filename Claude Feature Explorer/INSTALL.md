# Install

No build step, no dependencies, no accounts, no API key. It's three static files.

## Option 1 — just open it

Open `index.html` directly in a browser. Everything runs client-side, so double-clicking the file
works.

## Option 2 — serve it locally

Some browsers restrict things like `localStorage` on `file://` URLs, so if the theme toggle
doesn't persist, serve it instead:

```bash
cd "Claude Feature Explorer"
python3 -m http.server 8000
# then open http://localhost:8000
```

Any static file server works equally well (`npx serve`, `php -S localhost:8000`, etc.).

## Option 3 — host it

The folder is a complete static site — push it to GitHub Pages, Netlify, Vercel, or any static
host with no configuration.

## Controls

- `→` / `←` or the Prev/Next buttons — move between slides
- Click a dot, or press `S` to open the overview and jump to any slide
- `R` — restart from slide 1
- `Esc` — close the overview
- The ☐ icon toggles light/dark theme
