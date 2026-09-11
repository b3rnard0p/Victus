function initCalculoFCFooter() {
  const pbInput = document.getElementById("pb");
  const plInput = document.getElementById("pl");
  const fcDisplayElem = document.querySelector(".fc-display");
  const fcValueInput = document.querySelector(".fc-value");

  function atualizarFC() {
    const pb = parseFloat(pbInput?.value) || 0;
    const pl = parseFloat(plInput?.value) || 0;
    const fc = pl === 0 ? 0 : pb / pl;
    if (fcDisplayElem)
      fcDisplayElem.textContent = fc === 0 ? "0.00" : fc.toFixed(2);
    if (fcValueInput) fcValueInput.value = fc.toFixed(2);
  }
  if (pbInput) pbInput.addEventListener("input", atualizarFC);
  if (plInput) plInput.addEventListener("input", atualizarFC);
}

function initCalculoCustoUsadoFooter() {
  const pbInput = document.getElementById("pb");
  const custoKgInput = document.getElementById("custoKg");
  const custoUsadoDisplay = document.getElementById("custoUsado");

  const form = document.getElementById("form-ficha");
  let custoUsadoValue = document.querySelector('input[name="custoUsadoValue"]');
  if (!custoUsadoValue && form) {
    custoUsadoValue = document.createElement("input");
    custoUsadoValue.type = "hidden";
    custoUsadoValue.name = "custoUsadoValue";
    form.appendChild(custoUsadoValue);
  }

  function atualizarCustoUsado() {
    const pb = parseFloat(pbInput?.value) || 0;
    const custoKg = parseFloat(custoKgInput?.value) || 0;
    const usado = (custoKg * pb) / 1000;
    if (custoUsadoDisplay)
      custoUsadoDisplay.value = usado === 0 ? "" : usado.toFixed(2);
    if (custoUsadoValue) custoUsadoValue.value = usado.toFixed(2);
  }

  if (pbInput) pbInput.addEventListener("input", atualizarCustoUsado);
  if (custoKgInput) custoKgInput.addEventListener("input", atualizarCustoUsado);
}

function initCalculoFCC() {
  const rendimentoInput = document.getElementById("rendimento");
  const qntdAguaInput = document.getElementById("qntdAgua");
  const porcentAguaInput = document.getElementById("porcentAgua");
  const fccInput = document.getElementById("fcc");

  const aplicarBloqueioFCC = () => {
    if (!fccInput) return;
    fccInput.readOnly = true;
    fccInput.setAttribute("readonly", "readonly");
    fccInput.setAttribute("aria-readonly", "true");
    fccInput.setAttribute("title", "Campo calculado automaticamente");
    fccInput.classList.add("bg-gray-100", "cursor-not-allowed", "select-none");

    fccInput.addEventListener(
      "wheel",
      (e) => {
        fccInput.blur();
      }
    );
  };

  function calcularFCC() {
    let somaPLs = 0;
    document
      .querySelectorAll('#ingredientesAdicionados input[name$=".pl"]')
      .forEach((input) => {
        somaPLs += parseFloat(input.value) || 0;
      });

    const rendimento = parseFloat(rendimentoInput?.value) || 0;
    const qntdAgua = parseFloat(qntdAguaInput?.value) || 0;
    const porcentAgua = parseFloat(porcentAguaInput?.value) || 0;

    const aguaNoPrato = (qntdAgua * porcentAgua) / 100;
    const pesoCru = somaPLs + aguaNoPrato;

    let fcc = pesoCru > 0 ? rendimento / pesoCru : 0;
    if (fccInput)
      fccInput.value = pesoCru > 0 && rendimento > 0 ? fcc.toFixed(2) : "";
  }

  aplicarBloqueioFCC();

  if (rendimentoInput) rendimentoInput.addEventListener("input", calcularFCC);
  if (qntdAguaInput) qntdAguaInput.addEventListener("input", calcularFCC);
  if (porcentAguaInput) porcentAguaInput.addEventListener("input", calcularFCC);

  document.addEventListener("ingredienteAdicionado", calcularFCC);
  document.addEventListener("ingredienteRemovido", calcularFCC);
  document.addEventListener("ingredienteModificado", calcularFCC);
}

function initPesoPorcao() {
  const numEl =
    document.querySelector('input[name="numeroPorcoes"]') ||
    document.getElementById("numeroPorcoes");
  const rendEl =
    document.querySelector('input[name="preparacao.rendimento"]') ||
    document.getElementById("rendimento");
  const pesoPorcaoEl =
    document.querySelector('input[name="pesoPorcao"]') ||
    document.getElementById("pesoPorcao");

  const aplicarBloqueioNumeroPorcoes = () => {
    if (!numEl) return;
    numEl.readOnly = true;
    numEl.setAttribute("readonly", "readonly");
    numEl.setAttribute("aria-readonly", "true");
    numEl.setAttribute("title", "Campo calculado automaticamente");
    numEl.classList.add("bg-gray-100", "cursor-not-allowed", "select-none");
  };

  if (numEl) {
    aplicarBloqueioNumeroPorcoes();

    const bloquearEdicaoManual = (event) => {
      event.preventDefault();
      event.stopPropagation();
    };

    numEl.addEventListener("keydown", bloquearEdicaoManual);
    numEl.addEventListener("paste", bloquearEdicaoManual);
    numEl.addEventListener("drop", bloquearEdicaoManual);
    numEl.addEventListener(
      "wheel",
      (event) => {
        numEl.blur();
      }
    );
    numEl.addEventListener("focus", () => {
      aplicarBloqueioNumeroPorcoes();
      numEl.blur();
    });
  }

  function atualizarNumeroPorcoes() {
    const rendimento = parseFloat(rendEl?.value) || 0;
    const pesoPorcao = parseFloat(pesoPorcaoEl?.value) || 0;

    if (pesoPorcao > 0 && rendimento > 0 && numEl) {
      numEl.value = Math.ceil(rendimento / pesoPorcao).toString();
    } else if (numEl) {
      numEl.value = "";
    }
  }

  function atualizarAposMudancaDePesoOuRendimento() {
    atualizarNumeroPorcoes();
    triggerPerCapita();
    atualizarTotaisDaFicha();
  }

  function triggerPerCapita() {
    const custoTotalInput =
      document.querySelector('input[name="custoTotal"]') ||
      document.getElementById("custoTotal");
    if (custoTotalInput) custoTotalInput.dispatchEvent(new Event("input"));
  }

  if (pesoPorcaoEl)
    pesoPorcaoEl.addEventListener(
      "input",
      atualizarAposMudancaDePesoOuRendimento,
    );
  if (rendEl)
    rendEl.addEventListener("input", atualizarAposMudancaDePesoOuRendimento);
  if (numEl)
    numEl.addEventListener("input", atualizarAposMudancaDePesoOuRendimento);

  aplicarBloqueioNumeroPorcoes();
  atualizarNumeroPorcoes();
}

function atualizarCamposNutricionais(
  totalVTC,
  totais,
  porcentagens,
  divisor = 1,
) {
  const vtcDisplay = document.getElementById("vtc-display");
  if (vtcDisplay) vtcDisplay.textContent = (totalVTC / divisor).toFixed(2);

  const campos = {
    vtc: totalVTC / divisor,
    kcalPtn: totais.kcalPTN / divisor,
    kcalCho: totais.kcalCHO / divisor,
    kcalLip: totais.kcalLIP / divisor,
    gramasPtn: totais.gramasPTN / divisor,
    gramasCho: totais.gramasCHO / divisor,
    gramasLip: totais.gramasLIP / divisor,
    gramasSodio: totais.gramasSodio / divisor,
    gramasSaturada: totais.gramasSaturada / divisor,
    porcentPtn: porcentagens.porcentPTN,
    porcentCho: porcentagens.porcentCHO,
    porcentLip: porcentagens.porcentLIP,
  };

  Object.entries(campos).forEach(([id, valor]) => {
    const campo = document.getElementById(id);
    if (campo) campo.value = parseFloat(valor).toFixed(2);
  });
}

function initCalculoPerCapita() {
  const custoTotalOculto = document.getElementById("custoTotalHidden");
  const numeroPorcoesInput =
    document.querySelector('input[name="numeroPorcoes"]') ||
    document.getElementById("numeroPorcoes");
  const visivel = document.getElementById("custoPerCapitaVisivel");
  const oculto = document.getElementById("custoPerCapitaHidden");

  function atualizarPerCapita() {
    const total = parseFloat(custoTotalOculto?.value) || 0;
    const porcoes = parseFloat(numeroPorcoesInput?.value) || 1;
    const resultado = porcoes > 0 ? total / porcoes : 0;

    if (visivel) visivel.value = resultado > 0 ? resultado.toFixed(2) : "";
    if (oculto) oculto.value = resultado > 0 ? resultado.toFixed(2) : "";
  }
  if (custoTotalOculto)
    custoTotalOculto.addEventListener("input", atualizarPerCapita);
  if (numeroPorcoesInput)
    numeroPorcoesInput.addEventListener("input", atualizarPerCapita);
}

function initIngredienteAutocomplete() {
  const searchInput = document.getElementById("ingredienteSearch");
  const suggestionsContainer = document.getElementById(
    "ingredienteSuggestions",
  );
  const select = document.getElementById("ingredienteSelect");

  if (!searchInput || !select) return;

  let debounceTimer;
  let nomeCriadoPendente = "";

  function selecionarIngrediente(ing) {
    if (!ing || !ing.id) return;

    select.innerHTML = `<option value="">Selecione...</option>`;
    const opt = document.createElement("option");
    opt.value = ing.id;
    opt.text = ing.nome;
    opt.dataset.ptn = ing.ptn;
    opt.dataset.cho = ing.cho;
    opt.dataset.lip = ing.lip;
    opt.dataset.sodio = ing.sodio;
    opt.dataset.saturada = ing.gorduraSaturada;
    select.appendChild(opt);

    select.value = String(ing.id);
    searchInput.value = ing.nome || "";
    suggestionsContainer.classList.add("hidden");

    const medidaCaseiraEl = document.getElementById("medidaCaseira");
    if (medidaCaseiraEl) medidaCaseiraEl.focus();
  }

  function renderSuggestions(ingredientes) {
    if (ingredientes.length === 0) {
      suggestionsContainer.innerHTML = `
          <div class="px-4 py-3 font-semibold text-base text-gray-500 border-b border-[#d4e6be] last:border-0">
            Nenhum ingrediente encontrado
          </div>
        `;
    } else {
      suggestionsContainer.innerHTML = ingredientes
        .map(
          (ing) => `
          <div class="group/opt flex items-center px-4 py-3 font-semibold text-base text-black cursor-pointer hover:translate-x-1 transition-all duration-150 relative border-b border-[#d4e6be] last:border-0"
               data-id="${ing.id}" data-nome="${ing.nome}" data-ptn="${ing.ptn}" data-cho="${ing.cho}" data-lip="${ing.lip}" data-sodio="${ing.sodio}" data-saturada="${ing.gorduraSaturada}">
            <span>${ing.nome}</span>
            <span class="absolute bottom-[6px] left-4 h-[2px] w-0 bg-[#4A6E18] rounded-full transition-all duration-200 group-hover/opt:w-[calc(100%-2rem)]"></span>
          </div>`,
        )
        .join("");
    }
    suggestionsContainer.classList.remove("hidden");
  }

  searchInput.addEventListener("input", (e) => {
    const query = e.target.value.trim();

    clearTimeout(debounceTimer);

    if (!query) {
      suggestionsContainer.classList.add("hidden");
      select.value = "";
      return;
    }

    suggestionsContainer.innerHTML = `
        <div class="px-4 py-3 font-semibold text-base text-gray-500 border-b border-[#d4e6be] last:border-0 flex items-center gap-2">
          <i data-lucide="loader-2" class="animate-spin h-4 w-4"></i> Buscando...
        </div>
      `;
    suggestionsContainer.classList.remove("hidden");
    if (window.renderLucideIcons)
      window.renderLucideIcons(suggestionsContainer);

    debounceTimer = setTimeout(() => {
      fetch("/ingrediente/api/buscar?q=" + encodeURIComponent(query), {
        method: "GET",
        headers: {
          Accept: "application/json",
        },
        credentials: "same-origin",
      })
        .then((res) => {
          if (!res.ok) throw new Error("Network response was not ok");
          return res.json();
        })
        .then((data) => {
          renderSuggestions(data);

          if (nomeCriadoPendente) {
            const alvo = data.find(
              (ing) =>
                (ing.nome || "").trim().toLowerCase() ===
                nomeCriadoPendente.toLowerCase(),
            );
            if (alvo) {
              selecionarIngrediente(alvo);
            }
            nomeCriadoPendente = "";
          }
        })
        .catch((err) => {
          console.error("Erro ao buscar ingredientes:", err);
          suggestionsContainer.innerHTML = `
              <div class="px-4 py-3 font-semibold text-base text-red-500 border-b border-[#d4e6be] last:border-0">
                Erro ao buscar ingredientes.
              </div>
            `;
        });
    }, 300);
  });

  suggestionsContainer.addEventListener("click", (e) => {
    const item = e.target.closest("[data-id]");
    if (!item) return;

    selecionarIngrediente({
      id: item.dataset.id,
      nome: item.dataset.nome,
      ptn: item.dataset.ptn,
      cho: item.dataset.cho,
      lip: item.dataset.lip,
      sodio: item.dataset.sodio,
      gorduraSaturada: item.dataset.saturada,
    });
  });

  document.addEventListener("click", (e) => {
    if (
      !searchInput.contains(e.target) &&
      !suggestionsContainer.contains(e.target)
    ) {
      suggestionsContainer.classList.add("hidden");
    }
  });

  document.addEventListener("ingrediente:salvo", function (ev) {
    try {
      const nomeCriado = ev?.detail?.nome ? String(ev.detail.nome).trim() : "";
      if (nomeCriado) {
        nomeCriadoPendente = nomeCriado;
        searchInput.value = nomeCriado;
        searchInput.focus();
      }

      const q =
        searchInput && searchInput.value ? searchInput.value.trim() : "";
      if (!q) return;

      const event = new Event("input", { bubbles: true, cancelable: true });
      searchInput.dispatchEvent(event);
    } catch (err) {
      console.error(
        "Erro ao atualizar autocomplete após ingrediente salvo",
        err,
      );
    }
  });
}

function initAutoResizeTextareas() {
  ["autoExpandTextarea", "autoExpandTextarea2"].forEach((id) => {
    const textarea = document.getElementById(id);
    if (textarea) {
      textarea.addEventListener("input", function () {
        this.style.height = "auto";
        this.style.height = this.scrollHeight + "px";
      });
    }
  });
}

function initStatusCriacao() {
  const checkbox = document.getElementById("statusCheckbox");
  const statusField = document.getElementById("statusCriacaoField");
  if (checkbox && statusField) {
    checkbox.addEventListener(
      "change",
      () => (statusField.value = checkbox.checked ? "COMPLETA" : "INCOMPLETA"),
    );
    statusField.value = checkbox.checked ? "COMPLETA" : "INCOMPLETA";
  }
}

function initAguaCheckbox() {
  const checkbox = document.getElementById("aguaCheckbox");
  const aguaInputs = document.getElementById("aguaInputs");
  if (checkbox && aguaInputs) {
    checkbox.addEventListener("change", function () {
      aguaInputs.classList.toggle("hidden", !this.checked);
      if (!this.checked) {
        document.getElementById("qntdAgua").value = "";
        document.getElementById("porcentAgua").value = "";
        document.dispatchEvent(new Event("ingredienteModificado"));
      }
    });
  }
}

// Funções para o Modal de Configuração de Custo
window.abrirModalCustoConfig = function () {
  const modal = document.getElementById("modal-config-custo");
  if (modal) {
    modal.classList.remove("hidden");
    // Opcional: resetar os campos
    document.getElementById("modalCustoPeso").value = "";
    document.getElementById("modalCustoQtd").value = "";
    document.getElementById("modalCustoPreco").value = "";
    document.getElementById("modalCustoPesoUnidade").value = "";

    // Animação de entrada
    setTimeout(() => {
      modal.classList.remove("opacity-0");
      modal.classList.add("opacity-100");
      const content = document.getElementById("modal-config-custo-content");
      if (content) {
        content.classList.remove("scale-95");
        content.classList.add("scale-100");
      }
    }, 10);
  }
};

window.fecharModalCustoConfig = function () {
  const modal = document.getElementById("modal-config-custo");
  if (modal) {
    modal.classList.remove("opacity-100");
    modal.classList.add("opacity-0");
    const content = document.getElementById("modal-config-custo-content");
    if (content) {
      content.classList.remove("scale-100");
      content.classList.add("scale-95");
    }
    setTimeout(() => {
      modal.classList.add("hidden");
    }, 300);
  }
};

window.toggleCustoConfigInputs = function () {
  const tipoPeso = document.getElementById("tipo_peso").checked;
  const containerPesoComprado = document.getElementById("container_peso_comprado");
  const containerQtdComprada = document.getElementById("container_qtd_comprada");
  const containerPesoUnidade = document.getElementById("container_peso_unidade");

  if (tipoPeso) {
    containerPesoComprado.classList.remove("hidden");
    containerQtdComprada.classList.add("hidden");
    containerPesoUnidade.classList.add("hidden");
  } else {
    containerPesoComprado.classList.add("hidden");
    containerQtdComprada.classList.remove("hidden");
    containerPesoUnidade.classList.remove("hidden");
  }
};

window.aplicarCustoConfig = function () {
  const tipoPeso = document.getElementById("tipo_peso").checked;
  const precoPago = parseFloat(document.getElementById("modalCustoPreco").value);

  if (isNaN(precoPago) || precoPago <= 0) {
    if (typeof dispararErroFicha === "function") dispararErroFicha("O preço pago deve ser maior que zero.");
    else alert("O preço pago deve ser maior que zero.");
    return;
  }

  let custoPorKg = 0;

  if (tipoPeso) {
    const pesoComprado = parseFloat(document.getElementById("modalCustoPeso").value);
    if (isNaN(pesoComprado) || pesoComprado <= 0) {
      if (typeof dispararErroFicha === "function") dispararErroFicha("O peso comprado deve ser maior que zero.");
      else alert("O peso comprado deve ser maior que zero.");
      return;
    }
    // Preço por grama * 1000 = Preço por Kg
    custoPorKg = (precoPago / pesoComprado) * 1000;
  } else {
    const qtdComprada = parseFloat(document.getElementById("modalCustoQtd").value);
    const pesoPorUnidade = parseFloat(document.getElementById("modalCustoPesoUnidade").value);

    if (isNaN(qtdComprada) || qtdComprada <= 0) {
      if (typeof dispararErroFicha === "function") dispararErroFicha("A quantidade comprada deve ser maior que zero.");
      else alert("A quantidade comprada deve ser maior que zero.");
      return;
    }
    if (isNaN(pesoPorUnidade) || pesoPorUnidade <= 0) {
      if (typeof dispararErroFicha === "function") dispararErroFicha("O peso por unidade deve ser maior que zero.");
      else alert("O peso por unidade deve ser maior que zero.");
      return;
    }

    // Peso total = qtd * pesoPorUnidade
    const pesoTotal = qtdComprada * pesoPorUnidade;
    // Preço por grama * 1000 = Preço por Kg
    custoPorKg = (precoPago / pesoTotal) * 1000;
  }

  // Preencher o input principal de custo (id="custoKg")
  const inputCustoKg = document.getElementById("custoKg");
  if (inputCustoKg) {
    inputCustoKg.value = custoPorKg.toFixed(2);
    // Disparar o evento de input para que os scripts que ouvem 'custoKg' atualizem o 'Custo Usado' na tela
    inputCustoKg.dispatchEvent(new Event("input"));
  }

  fecharModalCustoConfig();
};
