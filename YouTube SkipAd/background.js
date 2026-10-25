// YouTube SkipAd
// Built by Santhosh Tuppad

// Sends real mouse input to the tab through Chrome's debugger API. Unlike a
// script calling button.click(), these clicks look to the page exactly like a
// person using the mouse.

// Tabs we've attached the debugger to.
const attached = new Set();

chrome.debugger.onDetach.addListener(({ tabId }) => attached.delete(tabId));
chrome.tabs.onRemoved.addListener((tabId) => attached.delete(tabId));

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const rand = (min, max) => min + Math.random() * (max - min);

function mouse(tabId, params) {
  return chrome.debugger.sendCommand({ tabId }, "Input.dispatchMouseEvent", params);
}

async function attach(tabId) {
  if (attached.has(tabId)) return;
  try {
    await chrome.debugger.attach({ tabId }, "1.3");
  } catch (err) {
    // Still attached from before the service worker restarted.
    if (!/already attached/i.test(err.message)) throw err;
  }
  attached.add(tabId);
}

async function humanClick(tabId, x, y) {
  await attach(tabId);

  // Glide the pointer in from a nearby spot, slowing down as it arrives.
  const startX = Math.max(0, x + rand(-150, 150));
  const startY = Math.max(0, y + rand(-100, 100));
  const steps = 12;
  for (let i = 1; i <= steps; i++) {
    const t = i / steps;
    const ease = t * (2 - t);
    await mouse(tabId, {
      type: "mouseMoved",
      x: startX + (x - startX) * ease,
      y: startY + (y - startY) * ease,
    });
    await sleep(rand(10, 25));
  }

  await sleep(rand(40, 100));
  await mouse(tabId, { type: "mousePressed", x, y, button: "left", buttons: 1, clickCount: 1 });
  await sleep(rand(50, 120));
  await mouse(tabId, { type: "mouseReleased", x, y, button: "left", buttons: 0, clickCount: 1 });
}

chrome.runtime.onMessage.addListener((msg, sender) => {
  if (msg.type !== "skip" || !sender.tab) return;
  const tabId = sender.tab.id;

  // The page reports CSS pixels; the debugger expects them scaled by page zoom.
  chrome.tabs
    .getZoom(tabId)
    .then((zoom) => humanClick(tabId, msg.x * zoom, msg.y * zoom))
    .catch((err) => console.warn("[SkipAd] Click failed:", err.message));
});
