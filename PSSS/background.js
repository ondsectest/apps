/**
 * No host permission is granted at install time. The content script is
 * registered dynamically, only for origins the user has explicitly granted
 * via chrome.permissions.request() (triggered from a click in popup.js) —
 * either a single site (per-site mode) or <all_urls> (global mode, opted
 * into explicitly rather than assumed).
 */
const DEFAULT_SETTINGS = { mode: 'global', globalEnabled: false, siteOverrides: {} };
const CONTENT_SCRIPT_ID = 'ss-shield-content';

const ICONS = {
  open: {
    16: 'icons/icon-open-16.png',
    32: 'icons/icon-open-32.png',
    48: 'icons/icon-open-48.png',
    128: 'icons/icon-open-128.png',
  },
  closed: {
    16: 'icons/icon-closed-16.png',
    32: 'icons/icon-closed-32.png',
    48: 'icons/icon-closed-48.png',
    128: 'icons/icon-closed-128.png',
  },
};

function getSettings() {
  return new Promise((resolve) => chrome.storage.sync.get(DEFAULT_SETTINGS, resolve));
}

function setSettings(partial) {
  return new Promise((resolve) => chrome.storage.sync.set(partial, resolve));
}

function hostnameToOriginPattern(hostname) {
  return `*://${hostname}/*`;
}

function hostnameFromOriginPattern(pattern) {
  const m = /^\*:\/\/([^/]+)\/\*$/.exec(pattern);
  return m ? m[1] : null;
}

// Which origin patterns the user's current settings call for, intersected
// with what's actually been granted — never assume a requested toggle state
// means the browser's permission prompt was actually accepted.
async function desiredGrantedMatches(settings) {
  if (settings.mode === 'global') {
    if (!settings.globalEnabled) return [];
    const has = await chrome.permissions.contains({ origins: ['<all_urls>'] });
    return has ? ['<all_urls>'] : [];
  }

  const hosts = Object.entries(settings.siteOverrides || {})
    .filter(([, enabled]) => enabled)
    .map(([host]) => host);
  if (!hosts.length) return [];

  const granted = [];
  for (const host of hosts) {
    const pattern = hostnameToOriginPattern(host);
    if (await chrome.permissions.contains({ origins: [pattern] })) granted.push(pattern);
  }
  return granted;
}

async function syncRegisteredContentScript() {
  const settings = await getSettings();
  const matches = await desiredGrantedMatches(settings);

  if (matches.length === 0) {
    try {
      await chrome.scripting.unregisterContentScripts({ ids: [CONTENT_SCRIPT_ID] });
    } catch (e) {
      /* nothing registered — nothing to unregister */
    }
    return;
  }

  const config = [{
    id: CONTENT_SCRIPT_ID,
    matches,
    js: ['content.js'],
    css: ['content.css'],
    runAt: 'document_start',
    allFrames: true,
    persistAcrossSessions: true,
  }];

  // Deliberately not "check with getRegisteredContentScripts, then decide"
  // — that check can go stale right after an extension update/reload, since
  // a persistAcrossSessions registration from the previous service worker
  // instance can still be there even when the check briefly reports
  // nothing. That's exactly what caused "Duplicate script ID" here: the
  // check said "not registered," so this called registerContentScripts,
  // which then failed because it actually still was. Trying update() first
  // and only falling back to register() on failure sidesteps the stale
  // check entirely — and the inner catch covers the reverse race (a
  // concurrent call already registered it between the two calls here).
  try {
    await chrome.scripting.updateContentScripts(config);
  } catch (e) {
    try {
      await chrome.scripting.registerContentScripts(config);
    } catch (e2) {
      /* a concurrent call already registered it — fine either way */
    }
  }
}

// registerContentScripts only affects future navigations. Without this, a
// tab that's already open on a site wouldn't get protected until reloaded.
async function injectIntoOpenTabsMatching(originPatterns) {
  if (!originPatterns.length) return;
  const tabs = await chrome.tabs.query({ url: originPatterns });
  await Promise.all(tabs.map(async (tab) => {
    if (!tab.id) return;
    try {
      await chrome.scripting.insertCSS({ target: { tabId: tab.id, allFrames: true }, files: ['content.css'] });
      await chrome.scripting.executeScript({ target: { tabId: tab.id, allFrames: true }, files: ['content.js'] });
    } catch (e) {
      /* restricted page (chrome://, the Web Store, etc.) — nothing to do */
    }
  }));
}

function isEnabledForHost(settings, hostname) {
  if (settings.mode === 'per-site') {
    return !!(hostname && settings.siteOverrides && settings.siteOverrides[hostname]);
  }
  return !!settings.globalEnabled;
}

const ATTENTION_BADGE_COLOR = '#f59e0b'; // amber
const DEFAULT_TITLE = 'Password Shoulder-Surf Shield';
const ATTENTION_TITLE = `${DEFAULT_TITLE} — refresh this tab to activate protection`;

// Same ping content.js already answers for popup.js — reused here so the
// toolbar icon can tell "permission granted" apart from "actually running
// on this tab right now" (permission is granted per-origin, not per already
// -open tab, so a tab that was open before the grant needs a refresh).
function pingTab(tabId) {
  return new Promise((resolve) => {
    try {
      chrome.tabs.sendMessage(tabId, { type: 'ss-shield-ping' }, (response) => {
        void chrome.runtime.lastError;
        resolve(!!(response && response.active));
      });
    } catch (e) {
      resolve(false);
    }
  });
}

async function setAttentionState(tabId, needsAttention) {
  try {
    if (needsAttention) {
      chrome.action.setBadgeText({ tabId, text: '!' });
      chrome.action.setBadgeBackgroundColor({ tabId, color: ATTENTION_BADGE_COLOR });
      chrome.action.setTitle({ tabId, title: ATTENTION_TITLE });
    } else {
      chrome.action.setBadgeText({ tabId, text: '' });
      chrome.action.setTitle({ tabId, title: DEFAULT_TITLE });
    }
  } catch (e) {
    /* tab may already be gone */
  }
}

async function refreshIconForTab(tabId, url) {
  if (!tabId || !url || !/^https?:/.test(url)) {
    try {
      chrome.action.setIcon({ tabId, path: ICONS.open });
    } catch (e) {
      /* tab may already be gone */
    }
    await setAttentionState(tabId, false);
    return;
  }

  let hostname = null;
  try {
    hostname = new URL(url).hostname;
  } catch (e) {
    /* ignore */
  }

  const settings = await getSettings();
  const wantsProtection = isEnabledForHost(settings, hostname);
  // The icon should reflect ACTUAL protection, not just the stored toggle —
  // a user can toggle something on and then decline the permission prompt.
  const originPattern = settings.mode === 'global' ? '<all_urls>' : hostnameToOriginPattern(hostname || '');
  const actuallyGranted = wantsProtection && hostname
    ? await chrome.permissions.contains({ origins: [originPattern] })
    : false;

  // Permission being granted for the origin doesn't mean THIS tab is
  // actually protected yet — a tab open before the grant needs a refresh
  // (see popup.js's own "Refresh this tab" prompt for the same check).
  const needsAttention = actuallyGranted && !(await pingTab(tabId));

  try {
    chrome.action.setIcon({ tabId, path: actuallyGranted && !needsAttention ? ICONS.closed : ICONS.open });
  } catch (e) {
    /* tab may already be gone */
  }
  await setAttentionState(tabId, needsAttention);
}

async function refreshActiveTab() {
  chrome.tabs.query({ active: true }, (tabs) => {
    tabs.forEach((t) => refreshIconForTab(t.id, t.url));
  });
}

chrome.tabs.onActivated.addListener(({ tabId }) => {
  chrome.tabs.get(tabId, (tab) => {
    if (tab) refreshIconForTab(tab.id, tab.url);
  });
});

chrome.tabs.onUpdated.addListener((tabId, changeInfo, tab) => {
  if (changeInfo.url || changeInfo.status === 'complete') {
    refreshIconForTab(tabId, tab.url);
  }
});

chrome.storage.onChanged.addListener(() => {
  syncRegisteredContentScript();
  refreshActiveTab();
});

// These two listeners are the single source of truth for translating an
// actual browser permission grant/revoke into stored settings. They must
// not assume popup.js already wrote the matching setting — showing the
// native permission dialog steals focus from the extension popup, and
// Chrome closes popups the instant they lose focus. That kills popup.js
// mid-await, before it can write globalEnabled/siteOverrides, even though
// the permission itself was genuinely granted. This listener runs in the
// background service worker, which has no such focus-loss lifecycle, so it
// reliably persists the real state and activates the content script
// regardless of whether the popup survived long enough to do it itself.
chrome.permissions.onAdded.addListener(async ({ origins }) => {
  if (origins && origins.length) {
    const settings = await getSettings();
    const changed = {};

    if (origins.includes('<all_urls>')) {
      changed.globalEnabled = true;
      changed.siteOverrides = {}; // mutual exclusivity, enforced here too as a safety net
    } else {
      const siteOverrides = { ...(settings.siteOverrides || {}) };
      let touched = false;
      for (const pattern of origins) {
        const host = hostnameFromOriginPattern(pattern);
        if (host) {
          siteOverrides[host] = true;
          touched = true;
        }
      }
      if (touched) changed.siteOverrides = siteOverrides;
    }

    if (Object.keys(changed).length) await setSettings(changed);
  }

  await syncRegisteredContentScript();
  if (origins && origins.length) await injectIntoOpenTabsMatching(origins);
  refreshActiveTab();
});

chrome.permissions.onRemoved.addListener(async ({ origins }) => {
  if (origins && origins.length) {
    const settings = await getSettings();
    const changed = {};

    if (origins.includes('<all_urls>')) {
      changed.globalEnabled = false;
    } else {
      const siteOverrides = { ...(settings.siteOverrides || {}) };
      let touched = false;
      for (const pattern of origins) {
        const host = hostnameFromOriginPattern(pattern);
        if (host && siteOverrides[host]) {
          siteOverrides[host] = false;
          touched = true;
        }
      }
      if (touched) changed.siteOverrides = siteOverrides;
    }

    if (Object.keys(changed).length) await setSettings(changed);
  }

  await syncRegisteredContentScript();
  refreshActiveTab();
});

chrome.runtime.onInstalled.addListener(() => {
  syncRegisteredContentScript();
  refreshActiveTab();
});

chrome.runtime.onStartup.addListener(() => {
  syncRegisteredContentScript();
  refreshActiveTab();
});
