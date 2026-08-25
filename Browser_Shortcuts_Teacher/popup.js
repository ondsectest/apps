const KEYS = { paused: "st_paused", pausedUntil: "st_pausedUntil" };

const statusEl = document.getElementById("status");
const toggleBtn = document.getElementById("toggleBtn");

function fmtTime(ms) {
  const mins = Math.ceil((ms - Date.now()) / 60000);
  return mins <= 1 ? "1 min" : `${mins} min`;
}

async function render() {
  const stored = await chrome.storage.local.get([KEYS.paused, KEYS.pausedUntil]);
  const paused = !!stored[KEYS.paused];
  const until = stored[KEYS.pausedUntil] || 0;
  const snoozed = until && Date.now() < until;

  if (paused) {
    statusEl.textContent = "Paused";
    statusEl.className = "status paused";
    toggleBtn.textContent = "Resume tips";
  } else if (snoozed) {
    statusEl.textContent = `Snoozed for ${fmtTime(until)}`;
    statusEl.className = "status paused";
    toggleBtn.textContent = "Resume now";
  } else {
    statusEl.textContent = "Active — tips every 5-10 min";
    statusEl.className = "status active";
    toggleBtn.textContent = "Pause tips";
  }
}

toggleBtn.addEventListener("click", async () => {
  const stored = await chrome.storage.local.get([KEYS.paused, KEYS.pausedUntil]);
  const paused = !!stored[KEYS.paused];
  const until = stored[KEYS.pausedUntil] || 0;
  const snoozed = until && Date.now() < until;

  if (paused || snoozed) {
    await chrome.storage.local.set({ [KEYS.paused]: false, [KEYS.pausedUntil]: 0 });
  } else {
    await chrome.storage.local.set({ [KEYS.paused]: true, [KEYS.pausedUntil]: 0 });
  }
  render();
});

const tipNowBtn = document.getElementById("tipNowBtn");
const tipNowDefaultLabel = tipNowBtn.textContent;
let tipNowResetTimer = null;

tipNowBtn.addEventListener("click", async () => {
  tipNowBtn.disabled = true;
  let result;
  try {
    result = await chrome.runtime.sendMessage({ type: "showTipNow" });
  } catch (e) {
    result = { ok: false, reason: "no-response" };
  }
  tipNowBtn.disabled = false;

  if (!result || !result.ok) {
    tipNowBtn.textContent =
      result?.reason === "unsupported-tab"
        ? "Open a regular webpage tab first"
        : "Couldn't show a tip here";
    clearTimeout(tipNowResetTimer);
    tipNowResetTimer = setTimeout(() => {
      tipNowBtn.textContent = tipNowDefaultLabel;
    }, 2000);
  }
});

document.querySelectorAll("button[data-mins]").forEach((btn) => {
  btn.addEventListener("click", async () => {
    const mins = parseInt(btn.dataset.mins, 10);
    await chrome.storage.local.set({
      [KEYS.paused]: false,
      [KEYS.pausedUntil]: Date.now() + mins * 60000
    });
    render();
  });
});

render();
