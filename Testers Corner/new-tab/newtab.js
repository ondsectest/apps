// Testers’ Corner — New Tab. Built by Santhosh Tuppad.
// Shows a different testing tip on every new tab. Tips are dealt from a
// shuffled deck so you see every tip once before any repeats.

(() => {
  const STORE_KEY = "qst.deck.v1";
  const $ = (id) => document.getElementById(id);

  // ── Deck ────────────────────────────────────────────────
  function shuffled(n, avoidFirst) {
    const a = Array.from({ length: n }, (_, i) => i);
    for (let i = n - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [a[i], a[j]] = [a[j], a[i]];
    }
    // Don't show the same tip twice in a row across a reshuffle.
    if (n > 1 && a[0] === avoidFirst) [a[0], a[1]] = [a[1], a[0]];
    return a;
  }

  function loadState() {
    try {
      const s = JSON.parse(localStorage.getItem(STORE_KEY));
      if (s && Array.isArray(s.deck) && s.deck.length === TIPS.length) return s;
    } catch (_) { /* storage unavailable or corrupt: start fresh */ }
    return { deck: shuffled(TIPS.length), pos: -1 };
  }

  function saveState() {
    try { localStorage.setItem(STORE_KEY, JSON.stringify(state)); } catch (_) {}
  }

  function advance() {
    state.pos += 1;
    if (state.pos >= state.deck.length) {
      const last = state.deck[state.deck.length - 1];
      state.deck = shuffled(TIPS.length, last);
      state.pos = 0;
    }
    saveState();
  }

  function goBack() {
    if (state.pos > 0) {
      state.pos -= 1;
      saveState();
      return true;
    }
    return false;
  }

  // ── Rendering ───────────────────────────────────────────
  function render(animate) {
    const index = state.deck[state.pos];
    const tip = TIPS[index];
    const card = $("tip");

    card.dataset.area = tip.areaKey;
    $("tip-num").textContent = "#" + String(index + 1).padStart(3, "0");
    $("tip-area").textContent = tip.area;
    $("tip-title").textContent = tip.title;
    $("tip-text").textContent = tip.text;
    $("tip-source").textContent = tip.source;
    $("prev").disabled = state.pos === 0;

    if (animate) {
      card.classList.remove("is-changing");
      void card.offsetWidth; // restart the animation
      card.classList.add("is-changing");
    }
  }

  function next() { advance(); render(true); }
  function prev() { if (goBack()) render(true); }

  async function copyTip() {
    const tip = TIPS[state.deck[state.pos]];
    const text = `${tip.title}: ${tip.text}\n(${tip.source} mind map, software testing mind maps repository)`;
    const label = $("copy-label");
    try {
      await navigator.clipboard.writeText(text);
      label.textContent = "Copied";
    } catch (_) {
      label.textContent = "Copy failed";
    }
    setTimeout(() => { label.textContent = "Copy"; }, 1500);
  }

  // ── Clock and greeting ──────────────────────────────────
  function tick() {
    const now = new Date();
    $("time").textContent = now.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
    $("time").dateTime = now.toISOString();
    $("date").textContent = now.toLocaleDateString([], { weekday: "long", day: "numeric", month: "long" });

    const h = now.getHours();
    const part = h < 5 ? "Testing late" : h < 12 ? "Good morning" : h < 17 ? "Good afternoon" : "Good evening";
    $("greeting").textContent = `${part}. What will you test today?`;
  }

  // ── Search ──────────────────────────────────────────────
  // Uses the browser's default search engine when running as an extension.
  $("search").addEventListener("submit", (e) => {
    e.preventDefault();
    const text = $("q").value.trim();
    if (!text) return;
    if (typeof chrome !== "undefined" && chrome.search && chrome.search.query) {
      chrome.search.query({ text, disposition: "CURRENT_TAB" });
    } else {
      location.href = "https://www.google.com/search?q=" + encodeURIComponent(text);
    }
  });

  // ── People row ──────────────────────────────────────────
  // Shows a photo when one is set in people.js, otherwise the person's initials.
  function renderPeople() {
    const row = $("people");
    if (!row || typeof PEOPLE === "undefined") return;

    PEOPLE.forEach((person, i) => {
      const li = document.createElement("li");
      const a = document.createElement("a");
      a.className = "person";
      a.href = person.url;
      a.title = person.url.startsWith("mailto:")
        ? `Email ${person.name} (${person.url.slice(7)})`
        : `${person.name}: ${person.known}`;

      a.style.setProperty("--i", i); // staggers the entrance animation
      const avatar = document.createElement("span");
      avatar.className = "portrait";
      avatar.dataset.tone = String(i % 5);
      const initials = document.createElement("span");
      initials.className = "portrait-initials";
      initials.textContent = person.name.split(/\s+/).map((w) => w[0]).join("").slice(0, 2);
      avatar.appendChild(initials);
      if (person.photo) {
        const img = document.createElement("img");
        img.alt = "";
        img.decoding = "async";
        img.onload = () => avatar.classList.add("has-photo"); // fade in once loaded
        img.onerror = () => img.remove(); // keep the initials
        img.src = person.photo;
        avatar.appendChild(img);
      }

      const text = document.createElement("span");
      text.className = "person-text";
      const name = document.createElement("span");
      name.className = "person-name";
      name.textContent = person.name;
      const known = document.createElement("span");
      known.className = "person-known";
      known.textContent = person.known;
      text.append(name, known);

      a.append(avatar, text);
      li.appendChild(a);
      row.appendChild(li);
    });
  }

  // ── Wiring ──────────────────────────────────────────────
  $("next").addEventListener("click", next);
  $("prev").addEventListener("click", prev);
  $("copy").addEventListener("click", copyTip);

  document.addEventListener("keydown", (e) => {
    if (e.target === $("q") || e.metaKey || e.ctrlKey || e.altKey) return;
    if (e.key === "ArrowRight") { e.preventDefault(); next(); }
    else if (e.key === "ArrowLeft") { e.preventDefault(); prev(); }
    else if (e.key === "c" || e.key === "C") { copyTip(); }
    else if (e.key === "/") { e.preventDefault(); $("q").focus(); }
  });

  const state = loadState();
  advance(); // every new tab deals the next tip
  render(false);
  renderPeople();
  tick();
  setInterval(tick, 10_000);
})();
