/**
 * No host permission is granted at install. The content scripts are registered
 * dynamically, only for sites the user has switched on in the popup, and only
 * once Chrome's own permission prompt for that site has been accepted.
 */
const ISOLATED_ID = 'stl-content';
const MAIN_ID = 'stl-page';

function getSites() {
  return new Promise((resolve) =>
    chrome.storage.sync.get({ sites: {} }, (r) => resolve(r.sites || {}))
  );
}

function originPattern(site) {
  return `*://*.${site}/*`;
}

function hostMatches(site, hostname) {
  return hostname === site || hostname.endsWith('.' + site);
}

async function grantedSites() {
  const sites = await getSites();
  const granted = [];
  for (const site of Object.keys(sites).filter((s) => sites[s])) {
    if (await chrome.permissions.contains({ origins: [originPattern(site)] })) granted.push(site);
  }
  return granted;
}

async function syncRegisteredScripts() {
  const matches = (await grantedSites()).map(originPattern);

  if (!matches.length) {
    try {
      await chrome.scripting.unregisterContentScripts({ ids: [ISOLATED_ID, MAIN_ID] });
    } catch (e) {
      /* nothing registered */
    }
    return matches;
  }

  const config = [
    { id: ISOLATED_ID, js: ['content.js'], world: 'ISOLATED' },
    { id: MAIN_ID, js: ['page.js'], world: 'MAIN' },
  ].map((s) => ({
    ...s,
    matches,
    runAt: 'document_start',
    allFrames: false,
    persistAcrossSessions: true,
  }));

  // update first, register on failure: a registration persisted from a previous
  // service-worker run can exist even when a getRegisteredContentScripts check
  // briefly says otherwise.
  try {
    await chrome.scripting.updateContentScripts(config);
  } catch (e) {
    try {
      await chrome.scripting.registerContentScripts(config);
    } catch (e2) {
      /* a concurrent call already registered them */
    }
  }
  return matches;
}

// Registered scripts only affect future navigations, so tabs that are already
// open need the scripts pushed in. Both scripts guard against double injection.
async function injectIntoOpenTabs(matches) {
  if (!matches.length) return;
  const tabs = await chrome.tabs.query({ url: matches });
  await Promise.all(
    tabs.map(async (tab) => {
      if (!tab.id) return;
      try {
        await chrome.scripting.executeScript({ target: { tabId: tab.id }, files: ['content.js'] });
        await chrome.scripting.executeScript({
          target: { tabId: tab.id },
          files: ['page.js'],
          world: 'MAIN',
        });
      } catch (e) {
        /* restricted page: nothing to do */
      }
    })
  );
}

async function updateBadge(tab) {
  if (!tab || !tab.id) return;
  let on = false;
  if (tab.url) {
    try {
      const { hostname } = new URL(tab.url);
      on = (await grantedSites()).some((s) => hostMatches(s, hostname));
    } catch (e) {
      on = false;
    }
  }
  try {
    await chrome.action.setBadgeText({ tabId: tab.id, text: on ? 'ON' : '' });
    if (on) await chrome.action.setBadgeBackgroundColor({ tabId: tab.id, color: '#16a34a' });
  } catch (e) {
    /* tab closed */
  }
}

async function refreshAllBadges() {
  const tabs = await chrome.tabs.query({});
  await Promise.all(tabs.map(updateBadge));
}

async function fullSync() {
  const matches = await syncRegisteredScripts();
  await injectIntoOpenTabs(matches);
  await refreshAllBadges();
}

chrome.runtime.onInstalled.addListener(fullSync);
chrome.runtime.onStartup.addListener(fullSync);

chrome.storage.onChanged.addListener((changes, area) => {
  if (area === 'sync' && changes.sites) fullSync();
});
chrome.permissions.onAdded.addListener(fullSync);
chrome.permissions.onRemoved.addListener(fullSync);

chrome.tabs.onUpdated.addListener((tabId, changeInfo, tab) => {
  if (changeInfo.url || changeInfo.status === 'complete') updateBadge(tab);
});
chrome.tabs.onActivated.addListener(async ({ tabId }) => {
  try {
    updateBadge(await chrome.tabs.get(tabId));
  } catch (e) {
    /* tab closed */
  }
});
