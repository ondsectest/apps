/**
 * Password Shoulder-Surf Shield — content script.
 *
 * Never modifies the real <input type="password"> element's type or value.
 * That keeps native browser autofill / "save password" prompts working
 * normally, and means the real, exact password the user typed is always
 * what ends up in the form and gets submitted.
 *
 * What it does instead:
 *  - Makes the real input's own text AND caret invisible (color/caret-color:
 *    transparent), which also hides the browser's native masking dots
 *    (they're drawn in the text color).
 *  - Draws a separate, non-interactive overlay on top showing a decoy dot
 *    count (randomized every keystroke, independent of the real length)
 *    plus its own fake blinking caret positioned after the decoy dots — the
 *    real caret is never shown, so its position can't leak the real length.
 *  - The overlay is visible whenever the field has a value, regardless of
 *    focus (matching how native masking dots behave), so toggling a site's
 *    "hide password" control back to type=password doesn't leave the field
 *    looking empty until refocused.
 *  - Watches for the field's type flipping to "text" (native or site
 *    "reveal password" toggle) and disables the mask/overlay while revealed.
 */
(function () {
  const BULLET = '•';
  const DEFAULT_SETTINGS = { mode: 'global', globalEnabled: false, siteOverrides: {} };

  function getSettings() {
    return new Promise((resolve) => {
      try {
        chrome.storage.sync.get(DEFAULT_SETTINGS, resolve);
      } catch (e) {
        resolve(DEFAULT_SETTINGS);
      }
    });
  }

  function isEnabledForThisPage(settings) {
    if (settings.mode === 'per-site') {
      const host = location.hostname;
      return !!(settings.siteOverrides && settings.siteOverrides[host]);
    }
    return !!settings.globalEnabled;
  }

  class DecoyGuard {
    constructor(input) {
      this.input = input;
      this.decoyLength = 0;
      this.maskingActive = false;
      this._buildOverlay();
      this._bind();
      this.updateMaskState();
    }

    _buildOverlay() {
      this.overlay = document.createElement('div');
      this.overlay.className = 'ss-shield-overlay';

      this.bulletsEl = document.createElement('span');
      this.bulletsEl.className = 'ss-shield-bullets';

      this.caretEl = document.createElement('span');
      this.caretEl.className = 'ss-shield-caret ss-hidden';

      this.overlay.appendChild(this.bulletsEl);
      this.overlay.appendChild(this.caretEl);
      (document.body || document.documentElement).appendChild(this.overlay);
    }

    _bind() {
      this._onInput = () => this.onInput();
      this._onFocus = () => {
        this.syncPosition();
        this._updateOverlayVisibility();
      };
      this._onBlur = () => this._updateOverlayVisibility();
      this._onScrollResize = () => this.syncPosition();
      // Cmd/Ctrl+A (or any selection) on the real input fires this — it's how
      // we notice a selection happened at all, since the real selection
      // itself is made invisible via CSS (see content.css).
      this._onSelectionChange = () => {
        if (document.activeElement === this.input) this._updateOverlayVisibility();
      };

      this.input.addEventListener('input', this._onInput);
      this.input.addEventListener('focus', this._onFocus);
      this.input.addEventListener('blur', this._onBlur);
      window.addEventListener('scroll', this._onScrollResize, true);
      window.addEventListener('resize', this._onScrollResize);
      document.addEventListener('selectionchange', this._onSelectionChange);

      this.mo = new MutationObserver(() => this.updateMaskState());
      this.mo.observe(this.input, { attributes: true, attributeFilter: ['type', 'style', 'class'] });

      if (window.ResizeObserver) {
        this.ro = new ResizeObserver(() => this.syncPosition());
        this.ro.observe(this.input);
      }
    }

    updateMaskState() {
      // Capture the field's real (pre-mask) text color once, so the decoy
      // overlay text is readable instead of a fixed guess — but only before
      // our own transparent-color class has ever been applied.
      if (this._capturedColor === undefined && !this.input.classList.contains('ss-shield-hidden-text')) {
        this._capturedColor = getComputedStyle(this.input).color;
        this.overlay.style.color = this._capturedColor;
      }

      this.maskingActive = this.input.type === 'password';
      this.input.classList.toggle('ss-shield-hidden-text', this.maskingActive);
      this._updateOverlayVisibility();
      this.syncPosition();
      if (this.maskingActive) this.onInput();
    }

    _updateOverlayVisibility() {
      const isFocused = document.activeElement === this.input;
      const hasValue = this.input.value.length > 0;
      const hasSelection = isFocused && this.input.selectionStart !== this.input.selectionEnd;
      const shouldShow = this.maskingActive && (hasValue || isFocused);
      this.overlay.style.display = shouldShow ? 'flex' : 'none';
      // No blinking caret while a range is selected — matches how a real
      // text field behaves (the caret only reappears once the selection
      // collapses back to a single point).
      this.caretEl.classList.toggle('ss-hidden', !(this.maskingActive && isFocused) || hasSelection);
      this.bulletsEl.classList.toggle('ss-shield-selected', this.maskingActive && hasSelection);
    }

    // Cheap: just the on-screen rect. Called every animation frame so the
    // overlay tracks the input even when something ELSE on the page shifts
    // layout (e.g. an error banner inserted above the form) — that doesn't
    // fire focus/input/resize events on the input itself, so without a
    // continuous sync the overlay is left floating at its old position.
    syncGeometry() {
      const r = this.input.getBoundingClientRect();
      this.overlay.style.left = `${r.left + window.scrollX}px`;
      this.overlay.style.top = `${r.top + window.scrollY}px`;
      this.overlay.style.width = `${r.width}px`;
      this.overlay.style.height = `${r.height}px`;
    }

    // Fuller sync including styles that only change when the input's own
    // font/box styling changes — more expensive, so only called from
    // discrete events rather than every frame.
    syncPosition() {
      this.syncGeometry();
      const cs = getComputedStyle(this.input);
      this.overlay.style.fontSize = cs.fontSize;
      this.overlay.style.fontFamily = cs.fontFamily;
      this.overlay.style.paddingLeft = cs.paddingLeft;
      this.overlay.style.paddingRight = cs.paddingRight;
      this.overlay.style.letterSpacing = cs.letterSpacing;
      this.overlay.style.textAlign = cs.textAlign;
      this.overlay.style.borderRadius = cs.borderRadius;
      this.overlay.style.justifyContent = cs.textAlign === 'right' ? 'flex-end' : 'flex-start';
    }

    onInput() {
      if (!this.maskingActive) return;
      const realLen = this.input.value.length;
      if (realLen === 0) {
        this.decoyLength = 0;
      } else {
        // Fully random each keystroke: no correlation with the previous
        // decoy count or a fixed offset from the real length.
        const spread = Math.max(4, Math.ceil(realLen * 0.75));
        const min = Math.max(1, realLen - spread);
        const max = realLen + spread;
        this.decoyLength = Math.floor(Math.random() * (max - min + 1)) + min;
      }
      this.bulletsEl.textContent = BULLET.repeat(this.decoyLength);
      this._restartCaretBlink();
      this._updateOverlayVisibility();
      this.syncPosition();
    }

    _restartCaretBlink() {
      // Make the fake caret snap solid right after a keystroke, like a real
      // caret, then resume blinking — restart the CSS animation by forcing
      // a reflow between removing and re-adding the class.
      this.caretEl.classList.remove('ss-shield-blink-restart');
      void this.caretEl.offsetWidth;
      this.caretEl.classList.add('ss-shield-blink-restart');
    }

    detach() {
      this.input.removeEventListener('input', this._onInput);
      this.input.removeEventListener('focus', this._onFocus);
      this.input.removeEventListener('blur', this._onBlur);
      window.removeEventListener('scroll', this._onScrollResize, true);
      window.removeEventListener('resize', this._onScrollResize);
      document.removeEventListener('selectionchange', this._onSelectionChange);
      this.mo && this.mo.disconnect();
      this.ro && this.ro.disconnect();
      this.overlay && this.overlay.remove();
      this.input.classList.remove('ss-shield-hidden-text');
      delete this.input.dataset.ssShieldAttached;
    }
  }

  const guards = new Map();
  // Set once the page starts navigating away (see pagehide below) and never
  // unset — this document is on its way out, so nothing should re-attach a
  // guard from this point on. Without this, removing the overlay to tear
  // down is itself a DOM mutation, which re-triggers scan() via the body
  // observer, which promptly creates a brand new guard on the same
  // still-present <input type="password"> — silently undoing the teardown
  // moments after it happened.
  let torndown = false;

  // The overlay lives on document.body, not inside the input, so it survives
  // even if the page removes/replaces the input (e.g. a site re-rendering
  // its login form after a failed submit). Without this, that overlay is
  // orphaned and keeps floating on the page indefinitely.
  function pruneDisconnected() {
    guards.forEach((guard, input) => {
      if (!input.isConnected) {
        guard.detach();
        guards.delete(input);
      }
    });
  }

  async function scan() {
    if (torndown) return;
    const settings = await getSettings();
    const enabled = isEnabledForThisPage(settings);
    const fields = document.querySelectorAll('input[type="password"], input[data-ss-shield-attached]');

    fields.forEach((f) => {
      if (enabled && f.type === 'password' && !guards.has(f)) {
        f.dataset.ssShieldAttached = '1';
        guards.set(f, new DecoyGuard(f));
      } else if (!enabled && guards.has(f)) {
        guards.get(f).detach();
        guards.delete(f);
      }
    });

    pruneDisconnected();

    // scan() itself runs off a document-wide mutation observer, so any DOM
    // change anywhere on the page (e.g. an error banner appearing above the
    // form) lands here — resync every surviving guard's position right away
    // instead of waiting on the next animation frame (which browsers can
    // throttle in a background/inactive tab).
    guards.forEach((guard) => {
      if (guard.input.isConnected) guard.syncGeometry();
    });
  }

  function start() {
    scan();
    // Watch for elements being added/removed AND for style/class changes
    // (many sites show/hide a hidden error banner by toggling a class or
    // inline style rather than inserting a new node) — either can shift the
    // password field's on-screen position without touching the field itself.
    // Mutations caused by our OWN overlay/caret updates (and the input's own
    // ss-shield-hidden-text class, already handled by each guard's private
    // observer) are filtered out below to avoid an infinite self-triggered
    // loop: syncGeometry() writes overlay.style -> would otherwise re-fire
    // this observer -> scan() -> syncGeometry() again, forever.
    const bodyObserver = new MutationObserver((records) => {
      const relevant = records.some((r) => {
        const t = r.target;
        if (t.nodeType !== 1) return true;
        if (typeof t.closest === 'function' && t.closest('.ss-shield-overlay')) return false;
        if (guards.has(t) && (r.attributeName === 'style' || r.attributeName === 'class')) return false;
        return true;
      });
      if (relevant) scan();
    });
    bodyObserver.observe(document.documentElement, {
      childList: true,
      subtree: true,
      attributes: true,
      attributeFilter: ['style', 'class'],
    });
    // Safety net in case a removal happens somewhere the mutation observer
    // doesn't see (e.g. inside a shadow root).
    const pruneIntervalId = setInterval(pruneDisconnected, 2000);

    // Continuously keep every overlay pinned to its input's actual on-screen
    // position, so any layout shift caused by content elsewhere on the page
    // (banners, validation messages, etc.) can't leave it stranded.
    (function tick() {
      if (torndown) return;
      guards.forEach((guard) => {
        if (guard.input.isConnected) guard.syncGeometry();
      });
      requestAnimationFrame(tick);
    })();

    // The instant the page starts navigating away (including a plain
    // reload), stop reacting to anything on the page and remove the overlay
    // immediately. Without this, the rAF loop above can catch one last frame
    // mid-teardown — the page's own layout is being torn down at that point,
    // so a position it computes then can be stale or wildly wrong — and
    // since the overlay is an absolutely-positioned element on document.body,
    // a bad value there can expand the page's scrollable area and visibly
    // shift/clip content for that last rendered frame before the navigation
    // completes. bodyObserver is disconnected first and torndown is set
    // before detaching, so removing the overlays can't itself trigger scan()
    // to immediately recreate them on the still-present password inputs.
    window.addEventListener('pagehide', () => {
      torndown = true;
      bodyObserver.disconnect();
      clearInterval(pruneIntervalId);
      guards.forEach((guard) => guard.detach());
      guards.clear();
    });
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', start);
  } else {
    start();
  }

  try {
    chrome.storage.onChanged.addListener(() => scan());
  } catch (e) {
    /* storage API unavailable in this context */
  }

  // Lets the popup check "did the shield actually reach this tab?" after
  // granting a permission. If the popup gets no reply at all, that itself
  // means this script never loaded on the page — the popup uses that to
  // offer a one-click page refresh instead of leaving the user guessing.
  try {
    chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
      if (message && message.type === 'ss-shield-ping') {
        sendResponse({ active: true });
      }
    });
  } catch (e) {
    /* runtime API unavailable in this context */
  }
})();
