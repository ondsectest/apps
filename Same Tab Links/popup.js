const $ = (id) => document.getElementById(id);

const baseHost = (hostname) => hostname.replace(/^www\./, '');
const pattern = (site) => `*://*.${site}/*`;

const toggle = $('toggle');
let site = null;

function getSites() {
  return new Promise((resolve) =>
    chrome.storage.sync.get({ sites: {} }, (r) => resolve(r.sites || {}))
  );
}

function saveSites(sites) {
  return new Promise((resolve) => chrome.storage.sync.set({ sites }, resolve));
}

async function activeSites() {
  const sites = await getSites();
  const out = [];
  for (const s of Object.keys(sites).filter((k) => sites[k])) {
    if (await chrome.permissions.contains({ origins: [pattern(s)] })) out.push(s);
  }
  return out.sort();
}

function showNote(text) {
  const note = $('note');
  note.textContent = text || '';
  note.hidden = !text;
}

async function render() {
  const on = await activeSites();

  $('listSection').hidden = on.length === 0;
  const list = $('list');
  list.replaceChildren();
  for (const s of on) {
    const li = document.createElement('li');
    const name = document.createElement('span');
    name.textContent = s;
    const btn = document.createElement('button');
    btn.type = 'button';
    btn.textContent = 'Turn off';
    btn.setAttribute('aria-label', `Turn off for ${s}`);
    btn.addEventListener('click', () => turnOff(s));
    li.append(name, btn);
    list.append(li);
  }

  if (!site) return;
  const enabled = on.includes(site);
  toggle.checked = enabled;
  toggle.disabled = false;
  $('status').textContent = enabled ? 'On: links open in the same tab' : 'Off for this site';
  $('status').classList.toggle('on', enabled);
}

async function turnOff(s) {
  const sites = await getSites();
  delete sites[s];
  await saveSites(sites);
  try {
    await chrome.permissions.remove({ origins: [pattern(s)] });
  } catch (e) {
    /* already revoked */
  }
  showNote('');
  render();
}

toggle.addEventListener('change', async () => {
  if (!site) return;
  if (!toggle.checked) {
    toggle.disabled = true;
    await turnOff(site);
    return;
  }

  // request() must start inside this click. The setting is saved right after
  // without waiting, so it still lands if Chrome's permission dialog closes the
  // popup; the background worker only acts once the permission is really granted.
  const request = chrome.permissions.request({ origins: [pattern(site)] });
  const sites = await getSites();
  sites[site] = true;
  saveSites(sites);

  let granted = false;
  try {
    granted = await request;
  } catch (e) {
    granted = false;
  }
  showNote(granted ? '' : 'Chrome needs your permission for this site before links can be changed.');
  render();
});

async function init() {
  const [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
  let url = null;
  try {
    url = tab && tab.url ? new URL(tab.url) : null;
  } catch (e) {
    url = null;
  }

  if (url && (url.protocol === 'http:' || url.protocol === 'https:')) {
    site = baseHost(url.hostname);
    $('site').textContent = site;
  } else {
    $('site').textContent = 'No website open';
    $('status').textContent = 'Open a website, then click the icon again.';
  }
  render();
}

init();
