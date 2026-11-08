(() => {
  if (window.__sameTabLinksActive) return;
  window.__sameTabLinksActive = true;

  const FLAG = 'sameTabLinks';
  const root = document.documentElement;
  let enabled = true;
  root.dataset[FLAG] = 'on';

  const isBlank = (value) => String(value || '').trim().toLowerCase() === '_blank';

  function baseTarget() {
    const base = document.querySelector('base[target]');
    return base ? base.getAttribute('target') : '';
  }

  function effectiveTarget(el) {
    return el.hasAttribute('target') ? el.getAttribute('target') : baseTarget();
  }

  function onClick(e) {
    if (!enabled || e.button !== 0 || e.ctrlKey || e.metaKey || e.shiftKey || e.altKey) return;
    const link = e.target instanceof Element ? e.target.closest('a[href], area[href]') : null;
    if (link && isBlank(effectiveTarget(link))) link.setAttribute('target', '_self');
  }

  function onSubmit(e) {
    if (!enabled) return;
    const form = e.target;
    if (!(form instanceof HTMLFormElement)) return;
    const submitter = e.submitter;
    if (submitter && submitter.hasAttribute('formtarget')) {
      if (isBlank(submitter.getAttribute('formtarget'))) submitter.setAttribute('formtarget', '_self');
    } else if (isBlank(effectiveTarget(form))) {
      form.setAttribute('target', '_self');
    }
  }

  document.addEventListener('click', onClick, true);
  document.addEventListener('submit', onSubmit, true);

  function hostMatches(site, hostname) {
    return hostname === site || hostname.endsWith('.' + site);
  }

  try {
    chrome.storage.onChanged.addListener((changes, area) => {
      if (area !== 'sync' || !changes.sites) return;
      const sites = changes.sites.newValue || {};
      enabled = Object.keys(sites).some((s) => sites[s] && hostMatches(s, location.hostname));
      root.dataset[FLAG] = enabled ? 'on' : 'off';
    });
  } catch (e) {
    /* extension was reloaded; this page just keeps its last state until refreshed */
  }
})();
