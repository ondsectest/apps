(function () {
  "use strict";

  const slideArea = document.getElementById("slideArea");
  const slideIndexEl = document.getElementById("slideIndex");
  const slideTotalEl = document.getElementById("slideTotal");
  const progressFill = document.getElementById("progressFill");
  const dotsEl = document.getElementById("dots");
  const prevBtn = document.getElementById("prevBtn");
  const nextBtn = document.getElementById("nextBtn");
  const overview = document.getElementById("overview");
  const overviewList = document.getElementById("overviewList");
  const overviewBtn = document.getElementById("overviewBtn");
  const closeOverviewBtn = document.getElementById("closeOverview");
  const themeBtn = document.getElementById("themeBtn");

  let current = 0;
  const total = SLIDES.length;
  const toolStepState = {}; // slideIndex -> current step
  const chatState = {}; // slideIndex -> [{role, text}]

  slideTotalEl.textContent = total;

  // ---- theme ----
  function applyTheme(theme) {
    if (theme === "light" || theme === "dark") {
      document.documentElement.setAttribute("data-theme", theme);
    } else {
      document.documentElement.removeAttribute("data-theme");
    }
  }
  (function initTheme() {
    try {
      const saved = localStorage.getItem("cfe-theme");
      if (saved) applyTheme(saved);
    } catch (e) {
      /* localStorage unavailable — fall back to system theme */
    }
  })();
  themeBtn.addEventListener("click", function () {
    const prefersDark = window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches;
    const currentAttr = document.documentElement.getAttribute("data-theme") || (prefersDark ? "dark" : "light");
    const next = currentAttr === "dark" ? "light" : "dark";
    applyTheme(next);
    try {
      localStorage.setItem("cfe-theme", next);
    } catch (e) {
      /* ignore */
    }
  });

  // ---- JSON syntax highlighting ----
  function escapeHtml(str) {
    return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
  }

  function highlightCode(str) {
    const escaped = escapeHtml(str);
    return escaped.replace(
      /("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+-]?\d+)?)/g,
      function (match) {
        let cls = "num";
        if (/^"/.test(match)) {
          cls = /:$/.test(match) ? "key" : "str";
        } else if (/^(true|false)$/.test(match)) {
          cls = "bool";
        } else if (/^null$/.test(match)) {
          cls = "null";
        }
        return '<span class="' + cls + '">' + match + "</span>";
      }
    );
  }

  function codePanel(title, code) {
    return (
      '<div class="code-panel">' +
      '<div class="code-panel-header"><span class="code-panel-title">' +
      escapeHtml(title) +
      '</span><button class="copy-btn" type="button" data-copy>Copy</button></div>' +
      '<pre class="code-block">' +
      highlightCode(code) +
      "</pre></div>"
    );
  }

  // ---- slide builders ----
  function buildTitle(s) {
    return (
      '<div class="slide title-slide">' +
      '<div class="eyebrow">' + escapeHtml(s.eyebrow || "") + "</div>" +
      "<h1>" + escapeHtml(s.title) + "</h1>" +
      '<p class="slide-subtitle">' + escapeHtml(s.subtitle) + "</p>" +
      (s.footer ? '<div class="title-footer">' + escapeHtml(s.footer) + "</div>" : "") +
      "</div>"
    );
  }

  function buildConcept(s) {
    let html =
      '<div class="slide">' +
      '<div class="section-label">' + escapeHtml(s.section) + "</div>" +
      "<h1>" + escapeHtml(s.title) + "</h1>" +
      '<p class="slide-subtitle">' + escapeHtml(s.subtitle) + "</p>";

    if (s.bullets) {
      html += '<ul class="bullets">' + s.bullets.map((b) => "<li>" + escapeHtml(b) + "</li>").join("") + "</ul>";
    }

    if (s.table) {
      html +=
        '<div class="table-wrap"><table class="feature-table">' +
        (s.table.caption ? "<caption>" + escapeHtml(s.table.caption) + "</caption>" : "") +
        "<thead><tr>" +
        s.table.columns.map((c) => "<th>" + escapeHtml(c) + "</th>").join("") +
        "</tr></thead><tbody>" +
        s.table.rows
          .map((row) => "<tr>" + row.map((cell) => "<td>" + escapeHtml(cell) + "</td>").join("") + "</tr>")
          .join("") +
        "</tbody></table></div>";
    }

    html += "</div>";
    return html;
  }

  function buildCode(s) {
    return (
      '<div class="slide">' +
      '<div class="section-label">' + escapeHtml(s.section) + "</div>" +
      "<h1>" + escapeHtml(s.title) + "</h1>" +
      '<p class="slide-subtitle">' + escapeHtml(s.subtitle) + "</p>" +
      '<div class="code-grid">' +
      codePanel("Request", s.request) +
      codePanel("Response", s.response) +
      "</div>" +
      (s.note ? '<div class="slide-note">' + escapeHtml(s.note) + "</div>" : "") +
      "</div>"
    );
  }

  function buildTerminal(s) {
    let html =
      '<div class="slide">' +
      '<div class="section-label">' + escapeHtml(s.section) + "</div>" +
      "<h1>" + escapeHtml(s.title) + "</h1>" +
      '<p class="slide-subtitle">' + escapeHtml(s.subtitle) + "</p>";

    if (s.bullets) {
      html += '<ul class="bullets">' + s.bullets.map((b) => "<li>" + escapeHtml(b) + "</li>").join("") + "</ul>";
    }

    html += '<div class="terminal">' + escapeHtml(s.transcript) + "</div></div>";
    return html;
  }

  function buildToolDemo(s, slideIdx) {
    if (toolStepState[slideIdx] === undefined) toolStepState[slideIdx] = 0;
    const active = toolStepState[slideIdx];
    const step = s.steps[active];

    let html =
      '<div class="slide">' +
      '<div class="section-label">' + escapeHtml(s.section) + "</div>" +
      "<h1>" + escapeHtml(s.title) + "</h1>" +
      '<p class="slide-subtitle">' + escapeHtml(s.subtitle) + "</p>" +
      '<div class="tool-demo">' +
      '<div class="step-tabs">' +
      s.steps
        .map(
          (st, i) =>
            '<button class="step-tab' +
            (i === active ? " active" : "") +
            '" type="button" data-step="' +
            i +
            '">' +
            escapeHtml("Step " + (i + 1)) +
            "</button>"
        )
        .join("") +
      "</div>" +
      '<div class="code-grid" style="grid-template-columns: 1fr;">' +
      codePanel(step.label, step.code) +
      "</div>" +
      '<div class="step-nav">' +
      '<button class="nav-btn" type="button" data-step-nav="prev"' +
      (active === 0 ? " disabled" : "") +
      ">← Prev step</button>" +
      '<button class="nav-btn" type="button" data-step-nav="next"' +
      (active === s.steps.length - 1 ? " disabled" : "") +
      ">Next step →</button>" +
      "</div></div>" +
      (s.note ? '<div class="slide-note">' + escapeHtml(s.note) + "</div>" : "") +
      "</div>";
    return html;
  }

  function buildChatDemo(s, slideIdx) {
    if (!chatState[slideIdx]) chatState[slideIdx] = [];
    const log = chatState[slideIdx];

    let html =
      '<div class="slide">' +
      '<div class="section-label">' + escapeHtml(s.section) + "</div>" +
      "<h1>" + escapeHtml(s.title) + "</h1>" +
      '<p class="slide-subtitle">' + escapeHtml(s.subtitle) + "</p>" +
      '<div class="chat-demo">' +
      '<div class="chat-log" id="chatLog">' +
      (log.length === 0
        ? '<div class="chat-empty">No messages yet — try a suggestion below.</div>'
        : log
            .map(
              (m) =>
                '<div class="chat-msg ' + m.role + '">' + escapeHtml(m.text) + "</div>"
            )
            .join("")) +
      "</div>" +
      '<div class="chat-input-row">' +
      '<input id="chatInput" type="text" placeholder="Ask something..." autocomplete="off" />' +
      '<button class="chat-send" id="chatSend" type="button">Send</button>' +
      "</div>" +
      '<div class="suggestion-row">' +
      s.suggestions
        .map((sug) => '<button class="suggestion-chip" type="button" data-suggestion="' + escapeHtml(sug) + '">' + escapeHtml(sug) + "</button>")
        .join("") +
      "</div>" +
      "</div></div>";
    return html;
  }

  function buildLinks(s) {
    return (
      '<div class="slide">' +
      '<div class="section-label">' + escapeHtml(s.section) + "</div>" +
      "<h1>" + escapeHtml(s.title) + "</h1>" +
      '<p class="slide-subtitle">' + escapeHtml(s.subtitle) + "</p>" +
      '<div class="link-list">' +
      s.items
        .map(
          (item) =>
            '<a class="link-card" href="' +
            escapeHtml(item.url) +
            '" target="_blank" rel="noopener noreferrer">' +
            '<div><div class="link-card-label">' +
            escapeHtml(item.label) +
            '</div><div class="link-card-desc">' +
            escapeHtml(item.desc) +
            "</div></div>" +
            '<div class="link-card-arrow">↗</div>' +
            "</a>"
        )
        .join("") +
      "</div>" +
      (s.footer ? '<div class="title-footer">' + escapeHtml(s.footer) + "</div>" : "") +
      "</div>"
    );
  }

  function renderSlide(idx) {
    const s = SLIDES[idx];
    let html;
    switch (s.kind) {
      case "title":
        html = buildTitle(s);
        break;
      case "concept":
        html = buildConcept(s);
        break;
      case "code":
        html = buildCode(s);
        break;
      case "terminal":
        html = buildTerminal(s);
        break;
      case "demoTool":
        html = buildToolDemo(s, idx);
        break;
      case "demoChat":
        html = buildChatDemo(s, idx);
        break;
      case "links":
        html = buildLinks(s);
        break;
      default:
        html = '<div class="slide"><p>Unknown slide type.</p></div>';
    }
    slideArea.innerHTML = html;
    wireSlideInteractions(idx);
  }

  function wireSlideInteractions(idx) {
    const s = SLIDES[idx];

    // copy buttons
    slideArea.querySelectorAll("[data-copy]").forEach((btn) => {
      btn.addEventListener("click", function () {
        const pre = btn.closest(".code-panel").querySelector("pre");
        const text = pre.textContent;
        if (navigator.clipboard && navigator.clipboard.writeText) {
          navigator.clipboard.writeText(text).then(() => flashCopied(btn));
        } else {
          flashCopied(btn);
        }
      });
    });

    if (s.kind === "demoTool") {
      slideArea.querySelectorAll("[data-step]").forEach((tab) => {
        tab.addEventListener("click", function () {
          toolStepState[idx] = parseInt(tab.getAttribute("data-step"), 10);
          renderSlide(idx);
        });
      });
      const prevStepBtn = slideArea.querySelector('[data-step-nav="prev"]');
      const nextStepBtn = slideArea.querySelector('[data-step-nav="next"]');
      if (prevStepBtn)
        prevStepBtn.addEventListener("click", function () {
          if (toolStepState[idx] > 0) {
            toolStepState[idx]--;
            renderSlide(idx);
          }
        });
      if (nextStepBtn)
        nextStepBtn.addEventListener("click", function () {
          if (toolStepState[idx] < s.steps.length - 1) {
            toolStepState[idx]++;
            renderSlide(idx);
          }
        });
    }

    if (s.kind === "demoChat") {
      const input = document.getElementById("chatInput");
      const sendBtn = document.getElementById("chatSend");

      function send(text) {
        const trimmed = (text || "").trim();
        if (!trimmed) return;
        chatState[idx].push({ role: "user", text: trimmed });
        const key = trimmed.toLowerCase().replace(/[?.!]+$/, "");
        const reply = s.canned[key] || s.fallback;
        chatState[idx].push({ role: "assistant", text: reply });
        renderSlide(idx);
        const log = document.getElementById("chatLog");
        if (log) log.scrollTop = log.scrollHeight;
        const freshInput = document.getElementById("chatInput");
        if (freshInput) freshInput.focus();
      }

      if (sendBtn) sendBtn.addEventListener("click", () => send(input.value) || (input.value = ""));
      if (input)
        input.addEventListener("keydown", function (e) {
          if (e.key === "Enter") {
            e.stopPropagation();
            send(input.value);
            input.value = "";
          }
        });
      slideArea.querySelectorAll("[data-suggestion]").forEach((chip) => {
        chip.addEventListener("click", function () {
          send(chip.getAttribute("data-suggestion"));
        });
      });
      const log = document.getElementById("chatLog");
      if (log) log.scrollTop = log.scrollHeight;
    }
  }

  function flashCopied(btn) {
    const original = btn.textContent;
    btn.textContent = "Copied!";
    setTimeout(() => (btn.textContent = original), 1200);
  }

  function renderDots() {
    dotsEl.innerHTML = SLIDES.map((_, i) => '<button class="dot" data-goto="' + i + '" aria-label="Go to slide ' + (i + 1) + '"></button>').join("");
    dotsEl.querySelectorAll("[data-goto]").forEach((dot) => {
      dot.addEventListener("click", () => goTo(parseInt(dot.getAttribute("data-goto"), 10)));
    });
  }

  function updateChrome() {
    slideIndexEl.textContent = current + 1;
    progressFill.style.width = ((current + 1) / total) * 100 + "%";
    dotsEl.querySelectorAll(".dot").forEach((dot, i) => dot.classList.toggle("active", i === current));
    prevBtn.disabled = current === 0;
    nextBtn.disabled = current === total - 1;
    nextBtn.textContent = current === total - 1 ? "Done ✓" : "Next →";
  }

  function goTo(idx) {
    current = Math.max(0, Math.min(total - 1, idx));
    renderSlide(current);
    updateChrome();
    slideArea.scrollTop = 0;
    window.scrollTo({ top: 0, behavior: "instant" in window ? "instant" : "auto" });
  }

  prevBtn.addEventListener("click", () => goTo(current - 1));
  nextBtn.addEventListener("click", () => {
    if (current < total - 1) goTo(current + 1);
  });

  // ---- overview modal ----
  function renderOverview() {
    overviewList.innerHTML = SLIDES.map((s, i) => {
      const label = s.title || "Slide " + (i + 1);
      return (
        '<button class="overview-item' +
        (i === current ? " current" : "") +
        '" type="button" data-goto-overview="' +
        i +
        '"><span class="idx">' +
        String(i + 1).padStart(2, "0") +
        "</span><span>" +
        escapeHtml(label) +
        "</span></button>"
      );
    }).join("");
    overviewList.querySelectorAll("[data-goto-overview]").forEach((item) => {
      item.addEventListener("click", function () {
        goTo(parseInt(item.getAttribute("data-goto-overview"), 10));
        closeOverview();
      });
    });
  }

  function openOverview() {
    renderOverview();
    overview.classList.remove("hidden");
  }
  function closeOverview() {
    overview.classList.add("hidden");
  }

  overviewBtn.addEventListener("click", openOverview);
  closeOverviewBtn.addEventListener("click", closeOverview);
  overview.addEventListener("click", function (e) {
    if (e.target === overview) closeOverview();
  });

  // ---- keyboard ----
  document.addEventListener("keydown", function (e) {
    const tag = (e.target && e.target.tagName) || "";
    if (tag === "INPUT" || tag === "TEXTAREA") return;

    if (e.key === "Escape") {
      closeOverview();
      return;
    }
    if (!overview.classList.contains("hidden")) return;

    if (e.key === "ArrowRight" || e.key === " ") {
      e.preventDefault();
      if (current < total - 1) goTo(current + 1);
    } else if (e.key === "ArrowLeft") {
      e.preventDefault();
      if (current > 0) goTo(current - 1);
    } else if (e.key.toLowerCase() === "s") {
      openOverview();
    } else if (e.key.toLowerCase() === "r") {
      goTo(0);
    }
  });

  // ---- init ----
  renderDots();
  goTo(0);
})();
