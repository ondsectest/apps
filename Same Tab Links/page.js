(() => {
  if (window.__sameTabLinksPatched) return;
  window.__sameTabLinksPatched = true;

  const nativeOpen = window.open;

  // Only redirects a bare window.open(url) fired by a real click. Calls that
  // pass window features (width=..., popup) are left alone so login and
  // payment popups keep working.
  window.open = function (url, target, features) {
    const on = document.documentElement.dataset.sameTabLinks === 'on';
    const plainNewTab = !target || String(target).trim().toLowerCase() === '_blank';
    const noFeatures = !features || !String(features).trim();
    const fromClick = navigator.userActivation && navigator.userActivation.isActive;
    if (on && plainNewTab && noFeatures && fromClick && url) {
      try {
        const dest = new URL(String(url), location.href);
        if (dest.protocol === 'http:' || dest.protocol === 'https:') {
          location.assign(dest.href);
          return null;
        }
      } catch (e) {
        /* unparseable URL: fall through to the native behaviour */
      }
    }
    return nativeOpen.apply(this, arguments);
  };
})();
