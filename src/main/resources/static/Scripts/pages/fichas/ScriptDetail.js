(function initDetailScript() {
  const currentPath = window.location.pathname;

  const navItems = document.querySelectorAll("nav a");
  navItems.forEach((item) => item.classList.remove("active"));

  const fichasLink = document.querySelector('a[href="/ficha"]');
  const isFichaListaOuDetalhe =
    currentPath === "/ficha" || /^\/ficha\/\d+$/.test(currentPath);

  if (fichasLink && isFichaListaOuDetalhe) {
    fichasLink.classList.add("active");
  }

  navItems.forEach((item) => {
    const href = item.getAttribute("href");
    if (href && href === currentPath) {
      item.classList.add("active");
    }
  });

  const menuToggle = document.getElementById("menu-toggle");
  const menuClose = document.getElementById("menu-close");
  const mobileMenu = document.getElementById("mobile-menu");

  if (menuToggle && mobileMenu) {
    const newToggle = menuToggle.cloneNode(true);
    menuToggle.parentNode.replaceChild(newToggle, menuToggle);

    newToggle.addEventListener("click", () =>
      mobileMenu.classList.toggle("hidden"),
    );
  }

  if (menuClose && mobileMenu) {
    const newClose = menuClose.cloneNode(true);
    menuClose.parentNode.replaceChild(newClose, menuClose);

    newClose.addEventListener("click", () =>
      mobileMenu.classList.add("hidden"),
    );
  }

  // Modal Functions
  window.abrirModalEquipamentos = function () {
    const modal = document.getElementById("modal-equipamentos");
    const content = document.getElementById("modal-equipamentos-content");
    modal.classList.remove("hidden");
    setTimeout(() => {
      content.classList.remove("scale-95", "opacity-0");
      content.classList.add("scale-100", "opacity-100");
      if (window.lucide) window.lucide.createIcons();
    }, 10);
  };

  window.fecharModalEquipamentos = function () {
    const modal = document.getElementById("modal-equipamentos");
    const content = document.getElementById("modal-equipamentos-content");
    content.classList.remove("scale-100", "opacity-100");
    content.classList.add("scale-95", "opacity-0");
    setTimeout(() => {
      modal.classList.add("hidden");
    }, 300);
  };

  window.abrirModalPreparo = function () {
    const modal = document.getElementById("modal-preparo");
    const content = document.getElementById("modal-preparo-content");
    modal.classList.remove("hidden");
    setTimeout(() => {
      content.classList.remove("scale-95", "opacity-0");
      content.classList.add("scale-100", "opacity-100");
      if (window.lucide) window.lucide.createIcons();
    }, 10);
  };

  window.fecharModalPreparo = function () {
    const modal = document.getElementById("modal-preparo");
    const content = document.getElementById("modal-preparo-content");
    content.classList.remove("scale-100", "opacity-100");
    content.classList.add("scale-95", "opacity-0");
    setTimeout(() => {
      modal.classList.add("hidden");
    }, 300);
  };

  const btnEquip = document.getElementById("btn-modal-equipamentos");
  if (btnEquip) {
    btnEquip.addEventListener("click", window.abrirModalEquipamentos);
  }

  const btnPreparo = document.getElementById("btn-modal-preparo");
  if (btnPreparo) {
    btnPreparo.addEventListener("click", window.abrirModalPreparo);
  }

  window.onclick = function (event) {
    const modalEquip = document.getElementById("modal-equipamentos");
    const modalPreparo = document.getElementById("modal-preparo");
    if (event.target == modalEquip) fecharModalEquipamentos();
    if (event.target == modalPreparo) fecharModalPreparo();
  };

  let _globalTooltipEl = null;
  const showGlobalTooltip = (e) => {
    _globalTooltipEl =
      _globalTooltipEl || document.getElementById("global-tooltip");
    const globalTooltipText = document.getElementById("global-tooltip-text");

    if (!_globalTooltipEl || !globalTooltipText) return;

    const group = e.target.closest(".tooltip-group");
    if (!group) return;

    const textEl = group.querySelector(".tooltip-text-target");
    if (!textEl) return;

    const isTruncated = textEl.scrollWidth > textEl.clientWidth;
    if (!isTruncated) {
      hideGlobalTooltip();
      return;
    }

    globalTooltipText.textContent = textEl.textContent;
    _globalTooltipEl.classList.remove("hidden");

    const rect = textEl.getBoundingClientRect();

    requestAnimationFrame(() => {
      const tooltipHeight = _globalTooltipEl.offsetHeight;
      const tooltipWidth = _globalTooltipEl.offsetWidth;

      let top = rect.top - tooltipHeight - 12;
      let left = rect.left + rect.width / 2 - tooltipWidth / 2;

      if (left < 10) left = 10;
      if (left + tooltipWidth > window.innerWidth - 10) {
        left = window.innerWidth - tooltipWidth - 10;
      }

      if (top < 10) {
        top = rect.bottom + 12;
        const arrow = document.getElementById("global-tooltip-arrow");
        if (arrow) {
          arrow.classList.remove("-bottom-2", "border-b-2", "border-r-2");
          arrow.classList.add("-top-2", "border-t-2", "border-l-2");
        }
      } else {
        const arrow = document.getElementById("global-tooltip-arrow");
        if (arrow) {
          arrow.classList.remove("-top-2", "border-t-2", "border-l-2");
          arrow.classList.add("-bottom-2", "border-b-2", "border-r-2");
        }
      }

      _globalTooltipEl.style.top = `${top}px`;
      _globalTooltipEl.style.left = `${left}px`;
      _globalTooltipEl.style.opacity = "1";
    });
  };

  const hideGlobalTooltip = () => {
    const globalTooltip =
      _globalTooltipEl || document.getElementById("global-tooltip");
    if (!globalTooltip) return;
    globalTooltip.style.opacity = "0";
    setTimeout(() => {
      if (globalTooltip.style.opacity === "0") {
        globalTooltip.classList.add("hidden");
      }
    }, 200);
  };

  document.addEventListener("mouseover", showGlobalTooltip);
  document.addEventListener("mouseout", (e) => {
    if (!e.target.closest(".tooltip-group")) hideGlobalTooltip();
  });
  document.addEventListener("touchstart", showGlobalTooltip, { passive: true });
  window.addEventListener("scroll", hideGlobalTooltip, { passive: true });
})();
