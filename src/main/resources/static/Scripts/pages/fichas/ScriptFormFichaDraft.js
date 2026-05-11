(function() {
  const formFicha = document.getElementById("form-ficha");
  let limparRascunhoAposSalvarNova = false;
  if (formFicha) {
    formFicha.addEventListener("submit", function (e) {
      if (document.querySelectorAll(".ingrediente-row").length === 0) {
        e.preventDefault();
        return;
      }
      const isNova = !document.querySelector('input[name="id"]')?.value;
      limparRascunhoAposSalvarNova = isNova;
    });
  }

  const RASCUNHO_KEY = "ficha_rascunho_nova";

  const idInput = document.querySelector('input[name="id"]');
  if (idInput?.value) {
    try {
      localStorage.removeItem(RASCUNHO_KEY);
    } catch (e) {}
  }


  function rascunhoTemDados(dados) {
    if (dados.ingredientes && dados.ingredientes.length > 0) {
      return true;
    }

    const valoresPadrao = ["", "0.00", "1.00", "0", "ATIVA", "INCOMPLETA", "false", "undefined", null];

    for (const [name, value] of Object.entries(dados.campos || {})) {
      const valorStr = String(value).trim();

      if (name.startsWith("id") || name === "status") continue;

      if (!valoresPadrao.includes(valorStr) && valorStr !== "") {
        return true;
      }
    }
    
    return false;
  }

  function salvarRascunho() {
    const isNova = !document.querySelector('input[name="id"]')?.value;
    if (!isNova) return;

    const form = document.getElementById("form-ficha");
    if (!form) return;

    const dados = { campos: {}, ingredientes: [] };
    form
      .querySelectorAll("input[name], select[name], textarea[name]")
      .forEach((el) => {
        const name = el.name;
        if (!name || name.startsWith("ingredientes[")) return;
        if (el.type === "checkbox") {
          dados.campos[name] = el.checked;
        } else {
          dados.campos[name] = el.value;
        }
      });

    document.querySelectorAll(".ingrediente-row").forEach((row) => {
      const ingId = row.querySelector('input[name$=".ingredienteId"]')?.value;
      if (!ingId) return;
      dados.ingredientes.push({
        ingId,
        nome: row.querySelector("td:first-of-type")?.textContent?.trim(),
        medida: row.querySelector('input[name$=".medidaCaseira"]')?.value,
        pb: row.querySelector('input[name$=".pb"]')?.value,
        pl: row.querySelector('input[name$=".pl"]')?.value,
        custoKg: row.querySelector('input[name$=".custoKg"]')?.value,
        ptn: row.querySelector(".ingrediente-ptn")?.value,
        cho: row.querySelector(".ingrediente-cho")?.value,
        lip: row.querySelector(".ingrediente-lip")?.value,
        sodio: row.querySelector(".ingrediente-sodio")?.value,
        saturada: row.querySelector(".ingrediente-saturada")?.value,
      });
    });

    if (!rascunhoTemDados(dados)) {
      try {
        localStorage.removeItem(RASCUNHO_KEY);
      } catch (e) {}
      return;
    }

    try {
      localStorage.setItem(RASCUNHO_KEY, JSON.stringify(dados));
    } catch (e) {}
  }

  function restaurarRascunho() {
    const idInput = document.querySelector('input[name="id"]');
    if (idInput?.value) return;

    let dados;
    try {
      const raw = localStorage.getItem(RASCUNHO_KEY);
      if (!raw) return;
      dados = JSON.parse(raw);
    } catch (e) { return; }

    if (!dados) return;

    if (!rascunhoTemDados(dados)) {
      limparRascunho();
      return;
    }

    const form = document.getElementById("form-ficha");
    if (!form) return;

    const valoresPadrao = ["", "0.00", "1.00", "0", "ATIVA", "INCOMPLETA", "false", "undefined", null];
    let fezAlteracaoReal = false;

    Object.entries(dados.campos || {}).forEach(([name, value]) => {
      const el = form.querySelector(`[name="${name}"]`);
      if (!el) return;

      const valorRascunho = String(value);
      const valorAtualNaTela = String(el.type === "checkbox" ? el.checked : el.value);

        if (!valoresPadrao.includes(valorRascunho) && valorRascunho !== valorAtualNaTela) {
          if (el.type === "checkbox") {
            el.checked = !!value;
            el.dispatchEvent(new Event("change", { bubbles: true }));
          } else {
            el.value = value;
            el.dispatchEvent(new Event("input", { bubbles: true }));
            
            // Tratamento especial para ComboBox
            const isCombo = form.querySelector(`#${name.split(".").pop()}-hidden`);
            if (isCombo) {
              const comboId = name.split(".").pop();
              if (window.setComboValue) {
                window.setComboValue(comboId, value);
              }
            }
          }
          fezAlteracaoReal = true;
        }
    });

    const corpoTabela = document.getElementById("ingredientesAdicionados");
    const temIngredientes = dados.ingredientes && dados.ingredientes.length > 0;

    if (temIngredientes && corpoTabela) {
      fezAlteracaoReal = true;
      
      dados.ingredientes.forEach((ing, index) => {
        if (document.querySelector(`input[name="ingredientes[${index}].ingredienteId"]`)) return;

        const tr = document.createElement("tr");
        tr.className = "ingrediente-row grid grid-cols-6 gap-x-2 gap-y-3 p-4 mb-4 border border-[#4A6E18] rounded-lg 950:table-row 950:gap-0 950:p-0 950:mb-0 950:border-none 950:rounded-none";
        tr.setAttribute("data-ingrediente-id", ing.ingId);

        tr.innerHTML = `
          <input type="hidden" class="ingrediente-ptn" value="${ing.ptn}" />
          <input type="hidden" class="ingrediente-cho" value="${ing.cho}" />
          <input type="hidden" class="ingrediente-lip" value="${ing.lip}" />
          <input type="hidden" class="ingrediente-sodio" value="${ing.sodio}" />
          <input type="hidden" class="ingrediente-saturada" value="${ing.saturada}" />

          <td class="col-span-6 block 950:table-cell px-2 py-2 border-none 950:border 950:border-[#4A6E18] text-center text-sm font-bold 950:font-normal bg-[#f3ffe5] 950:bg-transparent rounded-md 950:rounded-none">
            ${ing.nome}
          </td>

          <td class="col-span-6 block 950:table-cell px-2 py-2 border-none 950:border 950:border-[#4A6E18] text-center">
            <span class="950:hidden text-xs text-gray-800 font-bold block mb-1">Medida Caseira</span>
            <input type="text" name="ingredientes[${index}].medidaCaseira" value="${ing.medida}"
                   class="w-full rounded p-1 text-center text-sm text-gray-800 border border-[#4A6E18] 950:border-none focus:ring-1 focus:ring-green-800 bg-transparent" />
          </td>

          <td class="col-span-2 block 950:table-cell px-1 py-2 border-none 950:border 950:border-[#4A6E18] text-center">
            <span class="950:hidden text-xs text-gray-800 font-bold block mb-1">PB (g)</span>
            <input type="number" step="0.01" name="ingredientes[${index}].pb" value="${ing.pb}"
                   class="w-full 950:w-16 rounded p-1 text-center text-sm text-gray-800 border border-[#4A6E18] 950:border-none bg-transparent" />
          </td>

          <td class="col-span-2 block 950:table-cell px-1 py-2 border-none 950:border 950:border-[#4A6E18] text-center">
            <span class="950:hidden text-xs text-gray-800 font-bold block mb-1">PL (g)</span>
            <input type="number" step="0.01" name="ingredientes[${index}].pl" value="${ing.pl}"
                   class="w-full 950:w-16 rounded p-1 text-center text-sm text-gray-800 border border-[#4A6E18] 950:border-none bg-transparent" />
          </td>

          <td class="col-span-2 block 950:table-cell px-1 py-2 border-none 950:border 950:border-[#4A6E18] text-center">
            <span class="950:hidden text-xs text-gray-800 font-bold block mb-1">FC</span>
            <input type="number" step="0.01" name="ingredientes[${index}].fc" value="${(ing.pb / ing.pl || 1).toFixed(2)}"
                   class="w-full 950:w-14 950:mx-auto rounded p-1 fc-edit text-center text-sm text-gray-800 border border-[#4A6E18] 950:border-none bg-transparent" readonly tabindex="-1" />
          </td>

          <td class="col-span-3 block 950:table-cell px-2 py-2 border-none 950:border 950:border-[#4A6E18] text-center">
            <span class="950:hidden text-xs text-gray-800 font-bold block mb-1">Custo/Kg (R$)</span>
            <input type="number" step="0.01" name="ingredientes[${index}].custoKg" value="${ing.custoKg}"
                   class="w-full 950:w-20 rounded p-1 text-center text-sm text-gray-800 border border-[#4A6E18] 950:border-none bg-transparent" />
          </td>

          <td class="col-span-3 block 950:table-cell px-2 py-2 border-none 950:border 950:border-[#4A6E18] text-center">
            <span class="950:hidden text-xs text-gray-800 font-bold block mb-1">Custo Usado</span>
            <input type="number" step="0.01" name="ingredientes[${index}].custoUsado" value="${((ing.pb / 1000) * ing.custoKg || 0).toFixed(2)}"
                   class="w-full 950:w-20 rounded p-1 custo-usado-edit bg-[#f3ffe5] text-center text-sm text-gray-800 border border-[#4A6E18] 950:border-none" readonly tabindex="-1" />
          </td>

          <td class="col-span-6 block 950:table-cell 950:align-middle px-2 py-2 text-center border-none 950:border 950:border-[#4A6E18]">
            <button type="button" onclick="removerIngrediente(this)"
                    class="text-red-600 hover:text-red-800 transition-colors w-full py-2 rounded-md 950:border-none 950:w-auto 950:p-0 950:mx-auto flex 950:inline-flex justify-center items-center gap-2">
              <i data-lucide="trash-2" class="text-lg"></i>
              <span class="950:hidden font-semibold">Remover</span>
            </button>
          </td>

          <input type="hidden" name="ingredientes[${index}].ingredienteId" value="${ing.ingId}" />
        `;

        corpoTabela.appendChild(tr);
      });

      if (window.lucide) window.lucide.createIcons();

      recalcularTotaisDaFichaComProtecao();
      if (typeof initCalculoFCC === "function") {
        notificarMudancaDeIngredientes("ingredienteAdicionado", { bubbles: true });
      }
    }

    if (fezAlteracaoReal) {
      mostrarBannerRascunho();
    } else {
      limparRascunho();
    }
  }

  function limparRascunho() {
    try {
      localStorage.removeItem(RASCUNHO_KEY);
    } catch (e) {}
  }

  function limparFormularioCompletamente() {
    const form = document.getElementById("form-ficha");
    if (!form) return;

    form.querySelectorAll("input[name], select[name], textarea[name]").forEach((el) => {
      if (el.name.startsWith("id") || el.name === "status" || el.type === "hidden") return;
      
      if (el.type === "checkbox") {
        el.checked = false;
      } else if (el.type === "number" || el.type === "text" || el.tagName === "TEXTAREA") {
        el.value = "";
      }
      el.dispatchEvent(new Event("change"));
      el.dispatchEvent(new Event("input"));
    });

    const tbody = document.getElementById("ingredientesAdicionados");
    if (tbody) {
      tbody.querySelectorAll(".ingrediente-row").forEach((row) => {
        row.remove();
      });
    }

    recalcularTotaisDaFichaComProtecao();
  }

  function mostrarBannerRascunho() {
    if (document.getElementById("banner-rascunho")) return;
    const banner = document.createElement("div");
    banner.id = "banner-rascunho";
    banner.className =
      "fixed top-4 right-4 z-[9999] bg-[#d6f5b0] border border-[#4A6E18] rounded-xl px-4 py-3 shadow-lg flex items-center gap-3 text-sm font-medium text-[#2d4a0e]";
    banner.innerHTML = `
            <i data-lucide="history" class="text-[#4A6E18] shrink-0"></i>
            <span>Rascunho restaurado</span>
            <button type="button" onclick="document.getElementById('banner-rascunho')?.remove(); window.limparRascunhoComFormulario();"
                class="ml-2 text-[#4A6E18] hover:text-red-600 transition-colors font-bold text-base leading-none">✕</button>
        `;
    document.body.appendChild(banner);
    if (window.renderLucideIcons) window.renderLucideIcons(banner);
    setTimeout(
      () => document.getElementById("banner-rascunho")?.remove(),
      7000,
    );
  }
  
  window.limparRascunho = limparRascunho;
  window.limparRascunhoComFormulario = function() {
    limparRascunho();
    limparFormularioCompletamente();
  };

  let _rascunhoTimer = null;
  const form = document.getElementById("form-ficha");
  if (form) {
    form.addEventListener("input", () => {
      clearTimeout(_rascunhoTimer);
      _rascunhoTimer = setTimeout(salvarRascunho, 800);
    });
    form.addEventListener("change", () => {
      clearTimeout(_rascunhoTimer);
      _rascunhoTimer = setTimeout(salvarRascunho, 800);
    });
  }

  document.addEventListener("htmx:beforeRequest", salvarRascunho);

  document.addEventListener("htmx:afterSwap", (event) => {
    if (!limparRascunhoAposSalvarNova) return;
    if (event?.detail?.target?.id !== "slot-conteudo") return;

    const formAindaAberto = document.getElementById("form-ficha");
    if (!formAindaAberto) {
      limparRascunho();
      limparRascunhoAposSalvarNova = false;
    }
  });

  window.addEventListener("beforeunload", salvarRascunho);

  function agendarSalvamentoRascunho() {
    clearTimeout(_rascunhoTimer);
    _rascunhoTimer = setTimeout(salvarRascunho, 300);
  }

  document.addEventListener("ingredienteAdicionado", agendarSalvamentoRascunho);
  document.addEventListener("ingredienteRemovido", agendarSalvamentoRascunho);

  restaurarRascunho();
})();
