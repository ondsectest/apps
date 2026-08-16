const DEFAULT_SETTINGS = { mode: 'global', globalEnabled: false, siteOverrides: {} };

const modeSelect = document.getElementById('modeSelect');
const globalRow = document.getElementById('globalRow');
const siteRow = document.getElementById('siteRow');
const globalToggle = document.getElementById('globalToggle');
const siteToggle = document.getElementById('siteToggle');
const siteHostEl = document.getElementById('siteHost');
const stateImage = document.getElementById('stateImage');
const statusText = document.getElementById('statusText');
const refreshBanner = document.getElementById('refreshBanner');
const refreshTabBtn = document.getElementById('refreshTabBtn');

let currentHost = null;
let currentTabId = null;
let lastEnabled = null;

function getSettings() {
  return new Promise((resolve) => chrome.storage.sync.get(DEFAULT_SETTINGS, resolve));
}

function setSettings(partial) {
  return new Promise((resolve) => chrome.storage.sync.set(partial, resolve));
}

function getActiveTab() {
  return new Promise((resolve) => {
    chrome.tabs.query({ active: true, currentWindow: true }, (tabs) => {
      const tab = tabs && tabs[0];
      let hostname = null;
      try {
        hostname = tab && tab.url ? new URL(tab.url).hostname : null;
      } catch (e) {
        /* ignore */
      }
      resolve({ id: tab ? tab.id : null, hostname });
    });
  });
}

function pingTab(tabId) {
  return new Promise((resolve) => {
    if (!tabId) {
      resolve(false);
      return;
    }
    chrome.tabs.sendMessage(tabId, { type: 'ss-shield-ping' }, (response) => {
      void chrome.runtime.lastError; // no listener yet — expected, not an error to surface
      resolve(!!(response && response.active));
    });
  });
}

function wait(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

// After a permission grant, the background script injects the shield into
// this tab immediately (no reload needed) — but that's a few async steps on
// its side, so give it a moment and a couple of retries before concluding it
// genuinely didn't make it here and offering a manual refresh.
async function offerRefreshIfNeeded(tabId) {
  for (let attempt = 0; attempt < 4; attempt++) {
    await wait(300);
    if (await pingTab(tabId)) {
      refreshBanner.style.display = 'none';
      return;
    }
  }
  refreshBanner.style.display = 'block';
}

refreshTabBtn.addEventListener('click', () => {
  if (currentTabId) chrome.tabs.reload(currentTabId);
  refreshBanner.style.display = 'none';
});

function hostnameToOriginPattern(hostname) {
  return `*://${hostname}/*`;
}

function requestOrigin(pattern) {
  return new Promise((resolve) => {
    chrome.permissions.request({ origins: [pattern] }, (granted) => resolve(!!granted));
  });
}

function removeOrigin(pattern) {
  return new Promise((resolve) => {
    chrome.permissions.remove({ origins: [pattern] }, (removed) => resolve(!!removed));
  });
}

function hasOrigin(pattern) {
  return new Promise((resolve) => {
    chrome.permissions.contains({ origins: [pattern] }, (has) => resolve(!!has));
  });
}

async function isActuallyProtectedNow(settings) {
  if (settings.mode === 'per-site') {
    if (!currentHost || !settings.siteOverrides || !settings.siteOverrides[currentHost]) return false;
    return hasOrigin(hostnameToOriginPattern(currentHost));
  }
  if (!settings.globalEnabled) return false;
  return hasOrigin('<all_urls>');
}

function updateStateVisual(enabled) {
  if (enabled === lastEnabled) return;
  lastEnabled = enabled;

  if (enabled) {
    // Cache-bust so the closing-eyes GIF always replays from frame 1.
    stateImage.src = `images/hacker-eyes-closing.gif?t=${Date.now()}`;
    statusText.textContent = 'Protection is ON — dot count is randomized';
    statusText.classList.add('on');
  } else {
    stateImage.src = 'images/hacker-eyes-open.png';
    statusText.textContent = 'Protection is OFF — hacker is watching';
    statusText.classList.remove('on');
  }
}

async function render(settings) {
  modeSelect.value = settings.mode;
  globalToggle.checked = !!settings.globalEnabled;

  const isPerSite = settings.mode === 'per-site';
  globalRow.style.display = isPerSite ? 'none' : 'flex';
  siteRow.style.display = isPerSite ? 'flex' : 'none';

  if (currentHost) {
    siteHostEl.textContent = currentHost;
    siteToggle.disabled = false;
    siteToggle.checked = !!(settings.siteOverrides && settings.siteOverrides[currentHost]);
  } else {
    siteHostEl.textContent = 'this page';
    siteToggle.checked = false;
    siteToggle.disabled = true;
  }

  updateStateVisual(await isActuallyProtectedNow(settings));
}

async function refresh() {
  render(await getSettings());
}

async function init() {
  const tab = await getActiveTab();
  currentHost = tab.hostname;
  currentTabId = tab.id;
  await refresh();

  // Catches the case where a permission was granted in a PREVIOUS popup
  // instance that got closed by the native permission dialog before it
  // could run the check the toggle handlers below do — a first-time grant
  // always shows that dialog, so this is the common path, not an edge case.
  const settings = await getSettings();
  if (await isActuallyProtectedNow(settings) && currentTabId) {
    refreshBanner.style.display = (await pingTab(currentTabId)) ? 'none' : 'block';
  }
}

modeSelect.addEventListener('change', async () => {
  refreshBanner.style.display = 'none';
  await setSettings({ mode: modeSelect.value });
  refresh();
});

// Global and per-site protection are mutually exclusive: granting one always
// revokes the other first. This matters for two reasons — it avoids leaving
// redundant overlapping permissions granted at once, and it guarantees a
// fresh, genuine Allow/Deny prompt every time. (Chrome silently auto-grants
// a permissions.request() for an origin that's already covered by a broader
// one you hold — e.g. requesting one site while <all_urls> is still active —
// so the wider grant has to be gone *before* the narrower one is requested,
// or no prompt appears at all.)

async function revokeAllPerSiteGrants(settings) {
  const hosts = Object.entries(settings.siteOverrides || {})
    .filter(([, enabled]) => enabled)
    .map(([host]) => host);
  await Promise.all(hosts.map((host) => removeOrigin(hostnameToOriginPattern(host))));
  await setSettings({ siteOverrides: {} });
}

globalToggle.addEventListener('change', async () => {
  refreshBanner.style.display = 'none';

  if (globalToggle.checked) {
    const settings = await getSettings();
    // Drop any per-site grants first so they don't sit around redundantly
    // once everything is covered by the broader permission.
    await revokeAllPerSiteGrants(settings);

    // Must be called directly from this click handler — Chrome only honors
    // permissions.request() as part of the same user gesture that triggered it.
    const granted = await requestOrigin('<all_urls>');
    if (!granted) {
      globalToggle.checked = false;
      await setSettings({ globalEnabled: false });
      refresh();
      return;
    }
    await setSettings({ globalEnabled: true });
    refresh();
    // This tab may have been open before permission was granted — the
    // background script auto-injects into it, but confirm rather than assume.
    offerRefreshIfNeeded(currentTabId);
    return;
  }

  await removeOrigin('<all_urls>');
  await setSettings({ globalEnabled: false });
  refresh();
});

siteToggle.addEventListener('change', async () => {
  refreshBanner.style.display = 'none';
  if (!currentHost) return;
  const pattern = hostnameToOriginPattern(currentHost);
  const settings = await getSettings();
  const siteOverrides = { ...(settings.siteOverrides || {}) };

  if (siteToggle.checked) {
    // Drop the global grant first, for the same reason as above — otherwise
    // this site is already covered by <all_urls> and Chrome won't prompt.
    if (settings.globalEnabled) {
      await removeOrigin('<all_urls>');
      await setSettings({ globalEnabled: false });
    }

    const granted = await requestOrigin(pattern);
    if (!granted) {
      siteToggle.checked = false;
      refresh();
      return;
    }
    siteOverrides[currentHost] = true;
    await setSettings({ siteOverrides });
    refresh();
    // This tab may have been open before permission was granted — the
    // background script auto-injects into it, but confirm rather than assume.
    offerRefreshIfNeeded(currentTabId);
    return;
  }

  await removeOrigin(pattern);
  siteOverrides[currentHost] = false;
  await setSettings({ siteOverrides });
  refresh();
});

// If this popup instance survives long enough to see storage change out from
// under it — most notably, background.js persisting a permission grant that
// happened after the popup itself lost focus to the native permission dialog
// and got closed/reopened — reflect that instead of stale local state.
chrome.storage.onChanged.addListener(() => {
  refresh();
});

init();
