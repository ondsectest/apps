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

async function showTip(force = false) {
  if (!force && (await isSuppressed())) return { ok: false, reason: "suppressed" };

  const osFamily = await getOsFamily();
  const list = SHORTCUT_DATA[osFamily];
  const stored = await chrome.storage.local.get(STORAGE_KEYS.lastIndex);
  const { tip, index } = pickRandomTip(list, stored[STORAGE_KEYS.lastIndex]);
  await chrome.storage.local.set({ [STORAGE_KEYS.lastIndex]: index });

  // A native OS notification, not an injected in-page card: it needs no
  // per-tab access, so it doesn't require the "tabs", "scripting", or
  // broad "host_permissions" grants that in-page injection would.
  await chrome.notifications.create("", {
    type: "basic",
    iconUrl: "icons/icon128.png",
    title: tip.action,
    message: tip.keys,
    contextMessage: tip.category
  });

  return { ok: true };
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
