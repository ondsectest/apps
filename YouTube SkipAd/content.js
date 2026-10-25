// Known class names YouTube has used for the skip button. YouTube renames these
// from time to time, so we also fall back to matching the button text below.
const SKIP_SELECTORS = [
  ".ytp-skip-ad-button",
  ".ytp-ad-skip-button",
  ".ytp-ad-skip-button-modern",
  ".ytp-ad-skip-button-slot button",
  "button[id^='skip-button']",
];

// Matches "Skip", "Skip Ad", "Skip Ads" (any case).
const SKIP_TEXT = /^\s*skip(\s+ads?)?\s*$/i;

function isClickable(el) {
  return el && !el.disabled && el.offsetParent !== null;
}

function findSkipButton() {
  for (const selector of SKIP_SELECTORS) {
    const el = document.querySelector(selector);
    if (isClickable(el)) return el;
  }

  // Text fallback, limited to the video player so we never click
  // other "Skip" buttons on the page (like "Skip navigation").
  const player = document.querySelector(".html5-video-player");
  if (!player) return null;
  for (const el of player.querySelectorAll("button")) {
    if (SKIP_TEXT.test(el.textContent) && isClickable(el)) return el;
  }
  return null;
}

let lastClick = 0;

function trySkip() {
  // Give the previous click time to land before trying again.
  if (Date.now() - lastClick < 1500) return;

  const button = findSkipButton();
  if (!button) return;

  const r = button.getBoundingClientRect();
  if (r.width === 0 || r.bottom < 0 || r.top > innerHeight) return;
  lastClick = Date.now();

  // Aim for a random spot near the middle of the button, like a person would.
  const x = r.left + r.width * (0.3 + Math.random() * 0.4);
  const y = r.top + r.height * (0.3 + Math.random() * 0.4);

  console.log("[SkipAd] Skip button found, clicking:", button);
  // background.js performs the actual mouse movement and click.
  chrome.runtime.sendMessage({ type: "skip", x, y });
}

// React as soon as the page changes (the skip button gets added or shown).
new MutationObserver(trySkip).observe(document.documentElement, {
  childList: true,
  subtree: true,
  attributes: true,
  attributeFilter: ["class", "style", "disabled"],
});

// Backup check in case a change slips past the observer.
setInterval(trySkip, 500);
