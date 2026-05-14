window.mostrarToastErro = function (mensagem) {
  const container = document.getElementById("toast-container");
  const template = document.getElementById("template-toast-error");
  if (!container || !template) {
    console.warn("Toast container ou template de erro não encontrados!");
    return;
  }

  const toast = template.content.firstElementChild.cloneNode(true);
  const messageEl = toast.querySelector(".toast-message");
  if (messageEl) messageEl.textContent = mensagem || "Ocorreu um erro.";

  container.prepend(toast);
  if (typeof lucide !== "undefined") lucide.createIcons({ root: toast });

  setTimeout(() => {
    if (toast.parentElement) {
      toast.style.opacity = "0";
      toast.style.transform = "translateX(100%)";
      setTimeout(() => toast.remove(), 500);
    }
  }, 5000);
};

window.mostrarToastSucesso = function (mensagem) {
  const container = document.getElementById("toast-container");
  const template = document.getElementById("template-toast-success");
  if (!container || !template) {
    console.warn("Toast container ou template de sucesso não encontrados!");
    return;
  }

  const toast = template.content.firstElementChild.cloneNode(true);
  const messageEl = toast.querySelector(".toast-message");
  if (messageEl)
    messageEl.textContent = mensagem || "Operação realizada com sucesso!";

  container.prepend(toast);
  if (typeof lucide !== "undefined") lucide.createIcons({ root: toast });

  setTimeout(() => {
    if (toast.parentElement) {
      toast.style.opacity = "0";
      toast.style.transform = "translateX(100%)";
      setTimeout(() => toast.remove(), 500);
    }
  }, 5000);
};

document.addEventListener("DOMContentLoaded", function () {
  atualizarMenuAtivo();
  verificarPaginaLogin();

  const menuToggle = document.getElementById("menu-toggle");
  const menuClose = document.getElementById("menu-close");
  const mobileMenu = document.getElementById("mobile-menu");

  if (menuToggle && mobileMenu && !menuToggle.dataset.listener) {
    menuToggle.addEventListener("click", () =>
      mobileMenu.classList.toggle("hidden"),
    );
    menuToggle.dataset.listener = "true";
  }

  if (menuClose && mobileMenu && !menuClose.dataset.listener) {
    menuClose.addEventListener("click", () =>
      mobileMenu.classList.add("hidden"),
    );
    menuClose.dataset.listener = "true";
  }
});

document.addEventListener("htmx:afterSwap", function () {
  atualizarMenuAtivo();
});

function inicializarComboBoxes(root) {
  const scope = root || document;
  const wrappers = Array.from(scope.querySelectorAll(".glow-wrapper"));

  wrappers.forEach((wrapper) => {
    if (wrapper.dataset.cbInitialized === "true") return;

    const hidden = wrapper.querySelector('input[type="hidden"]');
    const control = wrapper.querySelector('[role="combobox"]');
    const menu = wrapper.querySelector('[id$="-menu"]');
    const display = wrapper.querySelector('[id$="-display"]');
    const label = wrapper.querySelector('[id$="-label"]');
    const chevron = wrapper.querySelector('[id$="-chevron"]');

    if (!hidden || !control || !menu || !display || !label || !chevron) return;

    const options = Array.from(menu.querySelectorAll(".cb-option"));

    function floatLabel(active) {
      if (active) {
        label.classList.remove("top-1/2", "-translate-y-1/2", "text-base");
        label.classList.add("-top-[11px]", "translate-y-0", "text-xs");
      } else {
        label.classList.add("top-1/2", "-translate-y-1/2", "text-base");
        label.classList.remove("-top-[11px]", "translate-y-0", "text-xs");
      }
    }

    function isOpen() {
      return !menu.classList.contains("hidden");
    }

    function openMenu() {
      menu.classList.remove("hidden");
      chevron.style.transform = "rotate(180deg)";
      control.setAttribute("aria-expanded", "true");
      floatLabel(true);
    }

    function closeMenu() {
      menu.classList.add("hidden");
      chevron.style.transform = "rotate(0deg)";
      control.setAttribute("aria-expanded", "false");
      floatLabel(Boolean(hidden.value));
    }

    function selectOption(optionValue, optionLabel) {
      hidden.value = optionValue || "";
      display.textContent = optionLabel || "";
      options.forEach((opt) => {
        opt.classList.toggle(
          "bg-[#d6eebe]",
          opt.getAttribute("data-value") === optionValue,
        );
      });
      closeMenu();
      hidden.dispatchEvent(new Event("change", { bubbles: true }));
      floatLabel(true);
    }

    control.addEventListener("click", function (event) {
      event.stopPropagation();
      isOpen() ? closeMenu() : openMenu();
    });

    options.forEach((opt) => {
      opt.addEventListener("click", function (event) {
        event.stopPropagation();
        selectOption(
          opt.getAttribute("data-value"),
          opt.getAttribute("data-label"),
        );
      });
    });

    document.addEventListener("click", function (event) {
      if (!wrapper.contains(event.target)) closeMenu();
    });

    // Adicionado para suportar restauração de rascunhos e outras mudanças programáticas
    hidden.addEventListener("input", function () {
      const val = hidden.value;
      const selected = options.find(
        (opt) => opt.getAttribute("data-value") === val,
      );
      if (selected) {
        display.textContent = selected.getAttribute("data-label");
        floatLabel(true);
      } else {
        display.textContent = "";
        floatLabel(false);
      }
    });

    const selected = options.find(
      (opt) => opt.getAttribute("data-value") === hidden.value,
    );
    if (selected) {
      display.textContent = selected.getAttribute("data-label");
      floatLabel(true);
    } else {
      floatLabel(!!hidden.value);
    }

    wrapper.dataset.cbInitialized = "true";
  });
}

if (typeof window.sincronizarLabelFlutuanteInput !== "function") {
  window.sincronizarLabelFlutuanteInput = function (inputId) {
    const input = document.getElementById(inputId);
    if (!input) return;

    const label = document.querySelector(`label[for="${inputId}"]`);
    if (!label) return;

    const temValor = String(input.value || "").trim() !== "";
    if (temValor) {
      label.classList.remove("top-1/2", "-translate-y-1/2", "text-base");
      label.classList.add("-top-[11px]", "translate-y-0", "text-xs");
    } else {
      label.classList.add("top-1/2", "-translate-y-1/2", "text-base");
      label.classList.remove("-top-[11px]", "translate-y-0", "text-xs");
    }
  };
}

if (typeof window.setComboValue !== "function") {
  window.setComboValue = function (comboId, value) {
    const hidden =
      document.getElementById(`${comboId}-hidden`) ||
      document.getElementById(comboId);
    const display = document.getElementById(`${comboId}-display`);
    const menu = document.getElementById(`${comboId}-menu`);
    if (!hidden || !menu) return;

    hidden.value = value || "";

    const options = Array.from(menu.querySelectorAll(".cb-option"));
    let selected = null;
    options.forEach((opt) => {
      const isSelected = opt.getAttribute("data-value") === hidden.value;
      opt.classList.toggle("bg-[#d6eebe]", isSelected);
      if (isSelected) selected = opt;
    });

    if (display) {
      display.textContent = selected ? selected.getAttribute("data-label") : "";
    }

    hidden.dispatchEvent(new Event("change", { bubbles: true }));
  };
}

if (typeof window.aplicarEstadoBotoesPesquisa !== "function") {
  window.aplicarEstadoBotoesPesquisa = function (
    btnPesquisaEspecifica,
    btnPesquisaTag,
    tipo,
  ) {
    if (!btnPesquisaEspecifica || !btnPesquisaTag) return;

    const ativo =
      "group relative flex items-center justify-center w-7 h-7 rounded-full border-2 bg-[#7AB648] border-[#4A6E18] text-green-950 shadow transition-all duration-200 focus:outline-none";
    const inativo =
      "group relative flex items-center justify-center w-7 h-7 rounded-full border-2 bg-[#f3ffe5] border-[#4A6E18] text-green-950 transition-all duration-200 focus:outline-none";

    if (tipo === "especifico") {
      btnPesquisaEspecifica.className = ativo;
      btnPesquisaTag.className = inativo;
    } else {
      btnPesquisaEspecifica.className = inativo;
      btnPesquisaTag.className = ativo;
    }
  };
}

if (typeof window.alternarCardAccordion !== "function") {
  window.alternarCardAccordion = function (header, config) {
    if (!header || !config) return;

    const modal = config.modalId
      ? document.getElementById(config.modalId)
      : null;
    if (modal && !modal.classList.contains("hidden")) return;

    const content = header.nextElementSibling;
    const chevronIcon = header.querySelector(
      config.chevronSelector || ".chevron-down",
    );
    if (!content) return;

    const openClasses = config.openClasses || [];
    const closedClasses = config.closedClasses || [];
    const isOpen = content.classList.contains(
      config.openMarker || openClasses[0] || "max-h-0",
    );

    if (isOpen) {
      content.classList.add(...closedClasses);
      content.classList.remove(...openClasses);
      if (config.openReplacement && config.closedReplacement) {
        content.classList.replace(
          config.openReplacement,
          config.closedReplacement,
        );
      }
      if (chevronIcon && config.chevronClosedClass)
        chevronIcon.classList.remove(config.chevronClosedClass);
      if (config.onClose) config.onClose(content);
    } else {
      content.classList.remove(...closedClasses);
      content.classList.add(...openClasses);
      if (config.closedReplacement && config.openReplacement) {
        content.classList.replace(
          config.closedReplacement,
          config.openReplacement,
        );
      }
      if (chevronIcon && config.chevronClosedClass)
        chevronIcon.classList.add(config.chevronClosedClass);
      if (config.onOpen) config.onOpen(content);
    }
  };
}

document.addEventListener("DOMContentLoaded", function () {
  inicializarComboBoxes(document);
});

document.addEventListener("htmx:afterSettle", function (event) {
  inicializarComboBoxes(
    event && event.detail && event.detail.target
      ? event.detail.target
      : document,
  );
});

if (typeof window.verificarLogin !== "function") {
  window.verificarLogin = function () {
    fetch("/auth/status")
      .then((response) => response.json())
      .then((data) => {
        if (data.autenticado) {
          const currentPath = window.location.pathname;

          if (currentPath === "/" || currentPath === "/login") {
            window.location.href = "/home";
          }
          return true;
        }
        return false;
      })
      .catch((error) => {
        console.error("Erro ao verificar status de autenticação:", error);
        return false;
      });
  };
}

if (typeof window.fazerLogout !== "function") {
  window.fazerLogout = function () {
    localStorage.removeItem("rememberMe");
    const form = document.createElement("form");
    form.method = "POST";
    form.action = "/sair-do-sistema";
    document.body.appendChild(form);
    form.submit();
  };
}

if (typeof window.verificarPaginaLogin !== "function") {
  window.verificarPaginaLogin = function () {
    const currentPath = window.location.pathname;
    if (currentPath === "/" || currentPath === "/login") {
      verificarLogin();
    }
  };
}

if (typeof window.abrirModalFetch !== "function") {
  window.abrirModalFetch = function (url) {
    document.body.classList.add("overflow-hidden");
    fetch(url)
      .then((response) => response.text())
      .then((html) => {
        const parser = new DOMParser();
        const doc = parser.parseFromString(html, "text/html");
        const formContainer = doc.querySelector("main > div");

        if (!formContainer) {
          window.location.href = url;
          return;
        }

        const modalBg = document.createElement("div");
        modalBg.id = "dynamic-fetch-modal";
        modalBg.className =
          "fixed inset-0 bg-black/60 flex items-center justify-center z-[100] p-4 opacity-0 transition-opacity duration-300";

        modalBg.addEventListener("click", function (e) {
          if (e.target === modalBg) fecharModal();
        });

        formContainer.classList.add(
          "relative",
          "max-h-[90vh]",
          "overflow-y-auto",
        );

        const btnCancelar = formContainer.querySelector("a.bg-[#297e1d]");
        if (btnCancelar) {
          btnCancelar.removeAttribute("href");
          btnCancelar.addEventListener("click", (e) => {
            e.preventDefault();
            fecharModal();
          });
        }

        const closeBtn = document.createElement("button");
        closeBtn.className =
          "absolute top-4 right-6 text-white text-3xl font-bold hover:text-red-300 transition-colors z-[110] cursor-pointer";
        closeBtn.innerHTML = "×";
        closeBtn.onclick = fecharModal;
        formContainer.appendChild(closeBtn);

        modalBg.appendChild(formContainer);
        document.body.appendChild(modalBg);

        requestAnimationFrame(() => {
          modalBg.classList.remove("opacity-0");
        });

        const allScripts = Array.from(doc.querySelectorAll("script")).filter(
          (s) =>
            !s.src ||
            (!s.src.includes("tailwindcss") && !s.src.includes("ScriptGeral")),
        );

        allScripts.forEach((s) => {
          const newScript = document.createElement("script");
          if (s.src) newScript.src = s.src;
          else newScript.textContent = s.textContent;
          modalBg.appendChild(newScript);
        });

        function fecharModal() {
          modalBg.classList.add("opacity-0");
          setTimeout(() => modalBg.remove(), 300);
        }
      })
      .catch((err) => {
        window.location.href = url;
        console.error(err);
      });
  };
}

document.addEventListener("wheel", function (event) {
  if (document.activeElement && document.activeElement.type === "number") {
    document.activeElement.blur();
  }
});
