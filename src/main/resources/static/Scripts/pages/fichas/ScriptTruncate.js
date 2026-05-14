(function initTruncate() {
  const targetsSelector = '[data-truncate="true"]';
  const truncateEl = (el) => {
    if (!el) return;
    const original = el.dataset.fullText || el.textContent || "";
    el.dataset.fullText = original;
    const full = original;
    el.textContent = full;
    if (el.scrollWidth <= el.clientWidth) return;
    let left = 0,
      right = full.length,
      mid,
      fit = "";
    while (left <= right) {
      mid = Math.floor((left + right) / 2);
      el.textContent = full.slice(0, mid) + "…";
      if (el.scrollWidth <= el.clientWidth) {
        fit = el.textContent;
        left = mid + 1;
      } else {
        right = mid - 1;
      }
    }
    el.textContent =
      fit || full.slice(0, Math.max(0, Math.floor(el.clientWidth / 8))) + "…";
  };

  const applyAll = () => {
    const nodes = Array.from(document.querySelectorAll(targetsSelector));
    nodes.forEach((n) => truncateEl(n));
  };

  let rAF;
  const debouncedApply = () => {
    if (rAF) cancelAnimationFrame(rAF);
    rAF = requestAnimationFrame(applyAll);
  };

  document.addEventListener("DOMContentLoaded", applyAll);
  window.addEventListener("resize", debouncedApply);
  document.addEventListener("htmx:afterSwap", applyAll);
  document.addEventListener("load", applyAll, true);

  window.__applyTruncate = applyAll;
})();
