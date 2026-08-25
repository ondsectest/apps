importScripts("shortcuts-data.js");

const ALARM_NAME = "shortcut-tip-alarm";
const STORAGE_KEYS = {
  paused: "st_paused",
  pausedUntil: "st_pausedUntil", // epoch ms, 0/undefined = not snoozed
  lastIndex: "st_lastIndex",
  osFamily: "st_osFamily"
};

function randomMinutes(min, max) {
  return min + Math.random() * (max - min);
}

function scheduleNextAlarm() {
  chrome.alarms.create(ALARM_NAME, { delayInMinutes: randomMinutes(5, 10) });
}

async function detectOsFamily() {
  const info = await chrome.runtime.getPlatformInfo();
  const family = info.os === "mac" ? "mac" : "other";
  await chrome.storage.local.set({ [STORAGE_KEYS.osFamily]: family });
  return family;
}

async function getOsFamily() {
  const stored = await chrome.storage.local.get(STORAGE_KEYS.osFamily);
  return stored[STORAGE_KEYS.osFamily] || (await detectOsFamily());
}

function pickRandomTip(list, avoidIndex) {
  if (list.length <= 1) return { tip: list[0], index: 0 };
  let index;
  do {
    index = Math.floor(Math.random() * list.length);
  } while (index === avoidIndex);
  return { tip: list[index], index };
}

async function isSuppressed() {
  const stored = await chrome.storage.local.get([STORAGE_KEYS.paused, STORAGE_KEYS.pausedUntil]);
  if (stored[STORAGE_KEYS.paused]) return true;
  const until = stored[STORAGE_KEYS.pausedUntil] || 0;
  if (until && Date.now() < until) return true;
  return false;
}

async function getTargetTab() {
  // The last active tab in the last focused window (works even if Chrome isn't in foreground).
  let [tab] = await chrome.tabs.query({ active: true, lastFocusedWindow: true });
  if (!tab) {
    [tab] = await chrome.tabs.query({ active: true });
  }
  return tab;
}

function injectedShowTip(tip) {
  const HOST_ID = "__shortcut_tip_host__";
  const existing = document.getElementById(HOST_ID);
  if (existing) existing.remove();

  const host = document.createElement("div");
  host.id = HOST_ID;
  host.style.all = "initial";
  host.style.position = "fixed";
  host.style.top = "16px";
  host.style.left = "50%";
  host.style.transform = "translateX(-50%)";
  host.style.zIndex = "2147483647";
  document.documentElement.appendChild(host);

  const shadow = host.attachShadow({ mode: "open" });
  const style = document.createElement("style");
  style.textContent = `
    .card {
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Arial, sans-serif;
      background: #ffffff;
      color: #1f2328;
      border: 1px solid #e3e6ea;
      border-radius: 10px;
      box-shadow: 0 6px 20px rgba(0,0,0,0.10);
      padding: 12px 16px;
      min-width: 260px;
      max-width: 380px;
      opacity: 0;
      transform: translateY(-8px);
      transition: opacity 180ms ease, transform 180ms ease;
    }
    .card.show { opacity: 1; transform: translateY(0); }
    .row { display: flex; align-items: flex-start; gap: 10px; }
    .icon { font-size: 18px; line-height: 1; margin-top: 1px; }
    .body { flex: 1; min-width: 0; }
    .cat { font-size: 10.5px; font-weight: 600; letter-spacing: 0.04em; text-transform: uppercase; color: #6b7280; margin: 0 0 2px 0; }
    .action { font-size: 13.5px; font-weight: 600; margin: 0 0 4px 0; color: #111827; }
    .keys { display: inline-block; font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace; font-size: 12px; background: #f3f4f6; border: 1px solid #e5e7eb; border-radius: 6px; padding: 2px 7px; color: #111827; }
    .close { cursor: pointer; border: none; background: transparent; color: #9ca3af; font-size: 15px; line-height: 1; padding: 2px; }
    .close:hover { color: #4b5563; }
  `;
  shadow.appendChild(style);

  const card = document.createElement("div");
  card.className = "card";
  card.innerHTML = `
    <div class="row">
      <div class="icon">⌨️</div>
      <div class="body">
        <p class="cat">${tip.category}</p>
        <p class="action">${tip.action}</p>
        <span class="keys">${tip.keys}</span>
      </div>
      <button class="close" aria-label="Dismiss">✕</button>
    </div>
  `;
  shadow.appendChild(card);

  requestAnimationFrame(() => card.classList.add("show"));

  const remove = () => {
    card.classList.remove("show");
    setTimeout(() => host.remove(), 200);
  };
  card.querySelector(".close").addEventListener("click", remove);
}

async function showTip(force = false) {
  if (!force && (await isSuppressed())) return { ok: false, reason: "suppressed" };

  const osFamily = await getOsFamily();
  const list = SHORTCUT_DATA[osFamily];
  const stored = await chrome.storage.local.get(STORAGE_KEYS.lastIndex);
  const { tip, index } = pickRandomTip(list, stored[STORAGE_KEYS.lastIndex]);
  await chrome.storage.local.set({ [STORAGE_KEYS.lastIndex]: index });

  const tab = await getTargetTab();
  if (!tab || !tab.id || !/^https?:/.test(tab.url || "")) {
    return { ok: false, reason: "unsupported-tab" };
  }

  try {
    await chrome.scripting.executeScript({
      target: { tabId: tab.id },
      func: injectedShowTip,
      args: [tip]
    });
    return { ok: true };
  } catch (e) {
    // Tab may not be injectable (chrome:// pages, web store, etc.)
    return { ok: false, reason: "injection-failed" };
  }
}

chrome.runtime.onInstalled.addListener(async () => {
  await detectOsFamily();
  await chrome.storage.local.set({ [STORAGE_KEYS.paused]: false, [STORAGE_KEYS.pausedUntil]: 0 });
  scheduleNextAlarm();
});

chrome.runtime.onStartup.addListener(() => {
  scheduleNextAlarm();
});

chrome.alarms.onAlarm.addListener((alarm) => {
  if (alarm.name === ALARM_NAME) {
    showTip();
    scheduleNextAlarm();
  }
});

// Manual trigger, bypassing pause/snooze so it always shows a tip on demand.
// Reachable two ways, since Chrome's suggested_key auto-binding for the
// Cmd+Shift+9 / Ctrl+Shift+9 command can silently fail to register (e.g. if
// another extension already claims it) with no warning to the user:
//   1. The keyboard shortcut, when Chrome did bind it.
//   2. The "Show a tip now" button in the popup, which always works.
chrome.commands.onCommand.addListener((command) => {
  if (command === "trigger-tip-now") {
    showTip(true);
  }
});

chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
  if (message?.type === "showTipNow") {
    showTip(true).then(sendResponse);
    return true; // keep the message channel open for the async response
  }
});
