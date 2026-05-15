function atualizarTotaisDaFicha() {
  atualizarCustoTotal();
  calcularPerfilNutricional();
}

function notificarMudancaDeIngredientes(eventName, eventOptions = {}) {
  atualizarTotaisDaFicha();
  document.dispatchEvent(new Event(eventName, eventOptions));
}

function recalcularTotaisDaFichaComProtecao() {
  if (typeof atualizarCustoTotal === "function") {
    try {
      atualizarCustoTotal();
    } catch (e) {
      console.error("Erro ao atualizar custo:", e);
    }
  }
  if (typeof calcularPerfilNutricional === "function") {
    try {
      calcularPerfilNutricional();
    } catch (e) {
      console.error("Erro ao calcular perfil:", e);
    }
  }
}

window.removerIngrediente = function (element) {
  const row = element.closest("tr");
  if (row) {
    row.remove();
    notificarMudancaDeIngredientes("ingredienteRemovido");
  }
};

window.toggleFicha = function (header) {
  const content = header.nextElementSibling;
  const chevronIcon = header.querySelector(".chevron-down");
  const isOpen = content.classList.contains("max-h-[3000px]");

  if (!isOpen) {
    content.classList.remove(
      "max-h-0",
      "opacity-0",
      "py-0",
      "border-transparent",
    );
    content.classList.add(
      "max-h-[3000px]",
      "opacity-100",
      "pb-6",
      "pt-4",
      "sm:pt-6",
      "border-black/20",
    );
    if (chevronIcon) chevronIcon.classList.add("rotate-180");
  } else {
    content.classList.add("max-h-0", "opacity-0", "py-0", "border-transparent");
    content.classList.remove(
      "max-h-[3000px]",
      "opacity-100",
      "pb-6",
      "pt-4",
      "sm:pt-6",
      "border-black/20",
    );
    if (chevronIcon) chevronIcon.classList.remove("rotate-180");
  }
};

window.adicionarIngrediente = function () {
  const select = document.getElementById("ingredienteSelect");
  const ingredienteId = select.value;
  const selectedOption = select.options[select.selectedIndex];
  if (!ingredienteId) return;

  const medidaCaseira = document.getElementById("medidaCaseira").value;
  const pb = parseFloat(document.getElementById("pb").value) || 0;
  const pl = parseFloat(document.getElementById("pl").value) || 0;
  const custoKg = parseFloat(document.getElementById("custoKg").value) || 0;

  if (!validarTextoMaximo(medidaCaseira, 100)) {
    dispararErroFicha(
      "A medida caseira do ingrediente pode ter no máximo 100 caracteres.",
    );
    return;
  }
  // Priorizar validação de campo obrigatório antes de validar formato/valor
  const pbField = document.getElementById("pb")?.value;
  if (!validarCampoObrigatorio(pbField)) {
    dispararErroFicha("O PB do ingrediente é obrigatório.");
    return;
  }
  if (!validarNumeroComMaximoDigitos(pbField, 4, 2)) {
    dispararErroFicha(
      "O PB do ingrediente deve ter no máximo 4 dígitos inteiros e 2 casas decimais.",
    );
    return;
  }

  const plField = document.getElementById("pl")?.value;
  if (!validarCampoObrigatorio(plField)) {
    dispararErroFicha("O PL do ingrediente é obrigatório.");
    return;
  }
  if (!validarNumeroComMaximoDigitos(plField, 4, 2)) {
    dispararErroFicha(
      "O PL do ingrediente deve ter no máximo 4 dígitos inteiros e 2 casas decimais.",
    );
    return;
  }

  const custoKgField = document.getElementById("custoKg")?.value;
  if (!validarCampoObrigatorio(custoKgField)) {
    dispararErroFicha("O custo por kg do ingrediente é obrigatório.");
    return;
  }
  if (!validarNumeroComMaximoDigitos(custoKgField, 4, 2)) {
    dispararErroFicha(
      "O custo por kg do ingrediente deve ter no máximo 4 dígitos inteiros e 2 casas decimais.",
    );
    return;
  }

  const fcValue = document.querySelector(".fc-value").value || 0;
  const custoUsadoVal =
    document.querySelector('[name="custoUsadoValue"]')?.value || "0.00";

  const rows = document.querySelectorAll(".ingrediente-row");
  const idx = rows.length;

  const row = document.createElement("tr");
  row.className =
    "ingrediente-row grid grid-cols-6 gap-x-2 gap-y-3 p-4 mb-4 border border-[#4A6E18] rounded-lg 950:table-row 950:gap-0 950:p-0 950:mb-0 950:border-none 950:rounded-none";

  row.innerHTML = `
      <input type="hidden" class="ingrediente-ptn" value="${parseFloat(selectedOption.dataset.ptn) || 0}">
      <input type="hidden" class="ingrediente-cho" value="${parseFloat(selectedOption.dataset.cho) || 0}">
      <input type="hidden" class="ingrediente-lip" value="${parseFloat(selectedOption.dataset.lip) || 0}">
      <input type="hidden" class="ingrediente-sodio" value="${parseFloat(selectedOption.dataset.sodio) || 0}">
      <input type="hidden" class="ingrediente-saturada" value="${parseFloat(selectedOption.dataset.saturada) || 0}">
      <input type="hidden" name="ingredientes[${idx}].ingredienteId" value="${ingredienteId}">

      <td class="ingrediente-nome-cell col-span-6 block 950:table-cell px-2 py-2 border-none 950:border 950:border-[#4A6E18] text-sm font-bold 950:font-normal bg-[#f3ffe5] 950:bg-transparent rounded-md 950:rounded-none">
        <div class="min-w-0 text-left" style="display:block;max-width:100%;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">
          <span style="display:block;max-width:100%;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;" data-truncate="true">${selectedOption.text}</span>
        </div>
      </td>
      
      <td class="col-span-6 block 950:table-cell px-2 py-2 border-none 950:border 950:border-[#4A6E18] text-center">
         <span class="950:hidden text-xs text-gray-800 font-bold block mb-1">Medida Caseira</span>
        <input type="text" name="ingredientes[${idx}].medidaCaseira" value="${medidaCaseira}" 
           class="w-full rounded p-1 text-center text-sm text-gray-800 border border-[#4A6E18] 950:border-none focus:ring-1 focus:ring-green-800 bg-transparent" />
      </td>
      
      <td class="col-span-2 block 950:table-cell px-1 py-2 border-none 950:border 950:border-[#4A6E18] text-center">
         <span class="950:hidden text-xs text-gray-800 font-bold block mb-1">PB (g)</span>
        <input type="number" step="0.01" name="ingredientes[${idx}].pb" value="${pb}" 
           class="w-full 950:w-16 rounded p-1 text-center text-sm text-gray-800 border border-[#4A6E18] 950:border-none bg-transparent" />
      </td>
      
      <td class="col-span-2 block 950:table-cell px-1 py-2 border-none 950:border 950:border-[#4A6E18] text-center">
         <span class="950:hidden text-xs text-gray-800 font-bold block mb-1">PL (g)</span>
        <input type="number" step="0.01" name="ingredientes[${idx}].pl" value="${pl}" 
           class="w-full 950:w-16 rounded p-1 text-center text-sm text-gray-800 border border-[#4A6E18] 950:border-none bg-transparent" />
      </td>
      
      <td class="col-span-2 block 950:table-cell px-1 py-2 border-none 950:border 950:border-[#4A6E18] text-center">
         <span class="950:hidden text-xs text-gray-800 font-bold block mb-1">FC</span>
        <input type="number" step="0.01" name="ingredientes[${idx}].fc" value="${fcValue}" 
          class="w-full 950:w-14 950:mx-auto rounded p-1 fc-edit text-center text-sm text-gray-800 border border-[#4A6E18] 950:border-none bg-transparent" readonly tabindex="-1" />
      </td>
      
      <td class="col-span-3 block 950:table-cell px-2 py-2 border-none 950:border 950:border-[#4A6E18] text-center">
         <span class="950:hidden text-xs text-gray-800 font-bold block mb-1">Custo/Kg (R$)</span>
        <input type="number" step="0.01" name="ingredientes[${idx}].custoKg" value="${custoKg.toFixed(2)}" 
           class="w-full 950:w-20 rounded p-1 text-center text-sm text-gray-800 border border-[#4A6E18] 950:border-none bg-transparent" />
      </td>
      
      <td class="col-span-3 block 950:table-cell px-2 py-2 border-none 950:border 950:border-[#4A6E18] text-center">
         <span class="950:hidden text-xs text-gray-800 font-bold block mb-1">Custo Usado</span>
        <input type="number" step="0.01" name="ingredientes[${idx}].custoUsado" value="${custoUsadoVal}" 
           class="w-full 950:w-20 rounded p-1 custo-usado-edit bg-[#f3ffe5] text-center text-sm text-gray-800 border border-[#4A6E18] 950:border-none" readonly tabindex="-1" />
      </td>
      
      <td class="col-span-6 block 950:table-cell 950:align-middle px-2 py-2 text-center border-none 950:border 950:border-[#4A6E18]">
        <button type="button" onclick="removerIngrediente(this)" class="text-red-600 hover:text-red-800 transition-colors w-full py-2 rounded-md 950:border-none 950:w-auto 950:p-0 950:mx-auto flex 950:inline-flex justify-center items-center gap-2">
            <i data-lucide="trash-2" class="text-lg"></i>
            <span class="950:hidden font-semibold">Remover</span>
        </button>
      </td>
  `;

  document.getElementById("ingredientesAdicionados").appendChild(row);
  if (window.renderLucideIcons) {
    window.renderLucideIcons(row);
  }
  if (typeof window.__applyTruncate === "function") {
    window.__applyTruncate();
  }

  select.value = "";
  document.getElementById("medidaCaseira").value = "";
  document.getElementById("pb").value = "";
  document.getElementById("pl").value = "";
  document.getElementById("custoKg").value = "";
  document.getElementById("custoUsado").value = "";
  document.querySelector(".fc-display").textContent = "1.00";
  document.getElementById("ingredienteSearch").value = "";

  select.focus();

  notificarMudancaDeIngredientes("ingredienteAdicionado");
};

function initEventDelegationTabela() {
  const tbody = document.getElementById("ingredientesAdicionados");
  if (!tbody) return;

  tbody.addEventListener("input", function (e) {
    const row = e.target.closest("tr");
    if (!row) return;

    const targetName = e.target.name || "";
    if (
      targetName.endsWith(".pb") ||
      targetName.endsWith(".pl") ||
      targetName.endsWith(".custoKg")
    ) {
      const pbInput = row.querySelector('input[name$=".pb"]');
      const plInput = row.querySelector('input[name$=".pl"]');
      const custoKgInput = row.querySelector('input[name$=".custoKg"]');
      const fcInput = row.querySelector('input[name$=".fc"]');
      const custoUsadoInput = row.querySelector('input[name$=".custoUsado"]');

      const pb = parseFloat(pbInput?.value) || 0;
      const pl = parseFloat(plInput?.value) || 0;
      const custoKg = parseFloat(custoKgInput?.value) || 0;

      if (
        fcInput &&
        (targetName.endsWith(".pb") || targetName.endsWith(".pl"))
      ) {
        fcInput.value = pl === 0 ? "0.00" : (pb / pl).toFixed(2);
      }
      if (
        custoUsadoInput &&
        (targetName.endsWith(".pb") || targetName.endsWith(".custoKg"))
      ) {
        custoUsadoInput.value = ((custoKg * pb) / 1000).toFixed(2);
      }

      atualizarCustoTotal();
      calcularPerfilNutricional();
      document.dispatchEvent(new Event("ingredienteModificado"));
    }
  });
}

function atualizarCustoTotal() {
  let total = 0;
  document
    .querySelectorAll('#ingredientesAdicionados input[name$=".custoUsado"]')
    .forEach((input) => {
      total += parseFloat(input.value) || 0;
    });

  const visivel = document.getElementById("custoTotalVisivel");
  const oculto = document.getElementById("custoTotalHidden");

  if (visivel) visivel.value = total.toFixed(2);
  if (oculto) {
    oculto.value = total.toFixed(2);
    oculto.dispatchEvent(new Event("input"));
  }
}

function calcularPerfilNutricional() {
  const tabelaNutricional = document.getElementById("nutricionalIngredientes");
  if (!tabelaNutricional) return;
  tabelaNutricional.innerHTML = "";

  let totais = {
    gramasPTN: 0,
    gramasCHO: 0,
    gramasLIP: 0,
    gramasSodio: 0,
    gramasSaturada: 0,
    kcalPTN: 0,
    kcalCHO: 0,
    kcalLIP: 0,
  };
  const divisor = obterDivisorNutricional();

  document.querySelectorAll(".ingrediente-row").forEach((row) => {
    const plInput = row.querySelector('input[name$=".pl"]');
    if (!plInput || !plInput.value || parseFloat(plInput.value) <= 0) return;

    const nutriData = calcularNutrientesPorIngrediente(row);
    adicionarLinhaNutricional(tabelaNutricional, nutriData, divisor);

    Object.keys(totais).forEach((key) => {
      totais[key] += nutriData[key] || 0;
    });
  });

  const totalVTC = totais.kcalPTN + totais.kcalCHO + totais.kcalLIP;
  const porcentagens = calcularPorcentagensNutrientes(
    totalVTC,
    totais.kcalPTN,
    totais.kcalCHO,
    totais.kcalLIP,
  );
  const totaisExibidos = aplicarDivisorNutrientes(totais, divisor);

  adicionarLinhasTotais(tabelaNutricional, totaisExibidos, porcentagens);
  atualizarCamposNutricionais(totalVTC, totais, porcentagens, divisor);
}

function obterDivisorNutricional() {
  const numeroPorcoesInput =
    document.querySelector('input[name="numeroPorcoes"]') ||
    document.getElementById("numeroPorcoes");
  const porcoes = parseFloat(numeroPorcoesInput?.value) || 0;
  return porcoes > 0 ? porcoes : 1;
}

function aplicarDivisorNutrientes(nutrientes, divisor) {
  const fator = divisor > 0 ? divisor : 1;
  return Object.fromEntries(
    Object.entries(nutrientes).map(([chave, valor]) => [
      chave,
      (parseFloat(valor) || 0) / fator,
    ]),
  );
}

function calcularNutrientesPorIngrediente(row) {
  const pl = parseFloat(row.querySelector('input[name$=".pl"]')?.value) || 0;

  const ptnBase = parseFloat(row.querySelector(".ingrediente-ptn")?.value) || 0;
  const choBase = parseFloat(row.querySelector(".ingrediente-cho")?.value) || 0;
  const lipBase = parseFloat(row.querySelector(".ingrediente-lip")?.value) || 0;
  const sodioBase =
    parseFloat(row.querySelector(".ingrediente-sodio")?.value) || 0;
  const saturadaBase =
    parseFloat(row.querySelector(".ingrediente-saturada")?.value) || 0;

  return {
    nome: row.querySelector("td:first-of-type").textContent,
    pl,
    gramasPTN: (ptnBase / 100) * pl,
    gramasCHO: (choBase / 100) * pl,
    gramasLIP: (lipBase / 100) * pl,
    gramasSodio: (sodioBase / 100) * pl,
    gramasSaturada: (saturadaBase / 100) * pl,
    kcalPTN: (ptnBase / 100) * pl * 4,
    kcalCHO: (choBase / 100) * pl * 4,
    kcalLIP: (lipBase / 100) * pl * 9,
  };
}

function adicionarLinhaNutricional(tabela, data, divisor = 1) {
  const row = document.createElement("tr");
  row.className = "border border-black text-sm";
  row.innerHTML = `
            <td class="px-2 py-2 border border-black">${data.nome}</td>
            <td class="px-2 py-2 border border-black text-center">${(data.pl / divisor).toFixed(2)}</td>
            <td class="px-2 py-2 border border-black text-center">${(data.gramasPTN / divisor).toFixed(2)}</td>
            <td class="px-2 py-2 border border-black text-center">${(data.gramasCHO / divisor).toFixed(2)}</td>
            <td class="px-2 py-2 border border-black text-center">${(data.gramasLIP / divisor).toFixed(2)}</td>
            <td class="px-2 py-2 border border-black text-center">${(data.gramasSodio / divisor).toFixed(2)}</td>
            <td class="px-2 py-2 border border-black text-center">${(data.gramasSaturada / divisor).toFixed(2)}</td>
        `;
  tabela.appendChild(row);
}

function calcularPorcentagensNutrientes(totalVTC, kcalPTN, kcalCHO, kcalLIP) {
  return {
    porcentPTN: totalVTC === 0 ? 0 : (kcalPTN / totalVTC) * 100,
    porcentCHO: totalVTC === 0 ? 0 : (kcalCHO / totalVTC) * 100,
    porcentLIP: totalVTC === 0 ? 0 : (kcalLIP / totalVTC) * 100,
  };
}

function adicionarLinhasTotais(tabela, totais, porcentagens) {
  adicionarLinhaTotal(
    tabela,
    "Gramas",
    totais.gramasPTN,
    totais.gramasCHO,
    totais.gramasLIP,
    totais.gramasSodio,
    totais.gramasSaturada,
  );
  adicionarLinhaTotal(
    tabela,
    "Kcal",
    totais.kcalPTN,
    totais.kcalCHO,
    totais.kcalLIP,
  );
  adicionarLinhaTotal(
    tabela,
    "%",
    porcentagens.porcentPTN,
    porcentagens.porcentCHO,
    porcentagens.porcentLIP,
    null,
    null,
    1,
  );
}

function adicionarLinhaTotal(
  tabela,
  tipo,
  ptn,
  cho,
  lip,
  sodio = null,
  saturada = null,
  divisor = 1,
) {
  const row = document.createElement("tr");
  row.className = "border border-black text-sm text-center";
  const cols =
    sodio !== null
      ? `<td class='border border-black'>${(sodio / divisor).toFixed(2)}</td><td class='border border-black'>${(saturada / divisor).toFixed(2)}</td>`
      : `<td colspan="2" class='border border-black'></td>`;
  row.innerHTML = `
            <td class="px-2 py-2 border border-black text-left">${tipo}</td>
            <td class="px-2 py-2 border border-black"></td>
            <td class="px-2 py-2 border border-black">${(ptn / divisor).toFixed(2)}</td>
            <td class="px-2 py-2 border border-black">${(cho / divisor).toFixed(2)}</td>
            <td class="px-2 py-2 border border-black">${(lip / divisor).toFixed(2)}</td>
            ${cols}
        `;
  tabela.appendChild(row);
}

function atualizarCamposNutricionais(
  totalVTC,
  totais,
  porcentagens,
  divisor = 1,
) {
  const vtcDisplay = document.getElementById("vtc-display");
  if (vtcDisplay) vtcDisplay.textContent = (totalVTC / divisor).toFixed(2);
}

function dispararErroFicha(msg) {
  if (window.mostrarToastErro) window.mostrarToastErro(msg);
  window.lockFichaSubmit = true;
  setTimeout(() => {
    window.lockFichaSubmit = false;
  }, 500);
}

function validarTextoMaximo(valor, maximo) {
  return typeof valor === "string" && valor.trim().length <= maximo;
}

function validarNumeroComMaximoDigitos(
  valor,
  maxInteiros,
  maxFracionarios = 2,
) {
  if (valor === null || valor === undefined || String(valor).trim() === "") {
    return false;
  }

  const normalizado = String(valor).trim().replace(",", ".");
  const numero = Number(normalizado);
  if (Number.isNaN(numero) || numero < 0) {
    return false;
  }

  const partes = normalizado.split(".");
  const parteInteira = partes[0].replace(/^0+(?=\d)/, "");

  if (parteInteira.length > maxInteiros) return false;

  if (partes.length > 1 && partes[1].length > maxFracionarios) {
    return false;
  }

  return true;
}

function validarNumeroDe0a100(valor) {
  if (valor === null || valor === undefined || String(valor).trim() === "") {
    return false;
  }

  const numero = Number(String(valor).trim().replace(",", "."));
  return !Number.isNaN(numero) && numero >= 0 && numero <= 100;
}

function validarCampoObrigatorio(valor) {
  return valor !== null && valor !== undefined && String(valor).trim() !== "";
}

window.validarFormFicha = function () {
  const nome = document.getElementById("nomePreparacao")?.value?.trim();
  const categoria = document.getElementById("categoria-hidden")?.value;
  const numeroPreparacao = document
    .getElementById("numeroPreparacao")
    ?.value?.trim();
  const modoPreparo = document
    .getElementById("autoExpandTextarea2")
    ?.value?.trim();

  const rendimentoVal = document.getElementById("rendimento")?.value;
  const pesoPorcaoVal = document.getElementById("pesoPorcao")?.value;

  const rendimento = parseFloat(rendimentoVal) || 0;
  const pesoPorcao = parseFloat(pesoPorcaoVal) || 0;

  const medidaCaseiraFicha = document
    .getElementById("medidaCaseiraFicha")
    ?.value?.trim();
  const tempoPreparo = document.getElementById("tempoPreparo")?.value?.trim();

  const ingredientes = document.querySelectorAll(".ingrediente-row");

  const aguaCheckbox = document.getElementById("aguaCheckbox");
  const qntdAguaInput = document.getElementById("qntdAgua");
  const porcentAguaInput = document.getElementById("porcentAgua");
  const qntdAguaVal = qntdAguaInput?.value?.trim() ?? "";
  const porcentAguaVal = porcentAguaInput?.value?.trim() ?? "";
  const qntdAgua = parseFloat(qntdAguaVal);
  const porcentAgua = parseFloat(porcentAguaVal);

   if (!nome) {
    dispararErroFicha("O nome da preparação é obrigatório.");
    return false;
  }

  if (!categoria) {
    dispararErroFicha("A categoria da preparação é obrigatória.");
    return false;
  }

  if (!numeroPreparacao) {
    dispararErroFicha("O número da preparação é obrigatório.");
    return false;
  }

  if (ingredientes.length === 0) {
    dispararErroFicha("Adicione pelo menos um ingrediente à ficha técnica.");
    return false;
  }

  if (!modoPreparo) {
    dispararErroFicha("O modo de preparo é obrigatório.");
    return false;
  }

  if (!tempoPreparo) {
    dispararErroFicha("O tempo de preparo é obrigatório.");
    return false;
  }

  if (!rendimentoVal || rendimento <= 0) {
    dispararErroFicha("O rendimento é obrigatório e deve ser maior que zero.");
    return false;
  }

  if (!pesoPorcaoVal || pesoPorcao <= 0) {
    dispararErroFicha(
      "O peso da porção é obrigatório e deve ser maior que zero.",
    );
    return false;
  }

  if (!medidaCaseiraFicha) {
    dispararErroFicha("A medida caseira final é obrigatória.");
    return false;
  }

  if (aguaCheckbox && aguaCheckbox.checked) {
    if (qntdAguaVal === "" || Number.isNaN(qntdAgua) || qntdAgua <= 0) {
      dispararErroFicha("Informe a quantidade de água utilizada.");
      return false;
    }
    if (porcentAguaVal === "" || Number.isNaN(porcentAgua)) {
      dispararErroFicha("Informe a porcentagem de água que sobrou.");
      return false;
    }
  }

  if (!validarTextoMaximo(nome, 100)) {
    dispararErroFicha(
      "O nome da preparação deve ter no máximo 100 caracteres.",
    );
    return false;
  }

  if (!validarTextoMaximo(tempoPreparo, 10)) {
    dispararErroFicha("O tempo de preparo deve ter no máximo 10 caracteres.");
    return false;
  }

  if (!validarTextoMaximo(medidaCaseiraFicha, 100)) {
    dispararErroFicha(
      "A medida caseira final deve ter no máximo 100 caracteres.",
    );
    return false;
  }

  if (!validarNumeroComMaximoDigitos(numeroPreparacao, 4, 0)) {
    dispararErroFicha(
      "O número da preparação deve ter no máximo 4 dígitos inteiros.",
    );
    return false;
  }

  if (!validarNumeroComMaximoDigitos(rendimentoVal, 4, 2)) {
    dispararErroFicha(
      "O rendimento deve ter no máximo 4 dígitos inteiros e 2 casas decimais.",
    );
    return false;
  }

  if (!validarNumeroComMaximoDigitos(pesoPorcaoVal, 4, 2)) {
    dispararErroFicha(
      "O peso da porção deve ter no máximo 4 dígitos inteiros e 2 casas decimais.",
    );
    return false;
  }

  if (qntdAguaVal !== "" && !validarNumeroComMaximoDigitos(qntdAguaVal, 4, 2)) {
    dispararErroFicha(
      "A quantidade de água deve ter no máximo 4 dígitos inteiros e 2 casas decimais.",
    );
    return false;
  }

  if (porcentAguaVal !== "" && !validarNumeroDe0a100(porcentAguaVal)) {
    dispararErroFicha("A porcentagem de água deve estar entre 0 e 100.");
    return false;
  }

  for (let i = 0; i < ingredientes.length; i++) {
    const row = ingredientes[i];
    const pbInput = row.querySelector('input[name$=".pb"]');
    const plInput = row.querySelector('input[name$=".pl"]');
    const custoKgInput = row.querySelector('input[name$=".custoKg"]');
    const nomeIngrediente =
      row.querySelector("td:first-of-type")?.textContent?.trim() ||
      "Ingrediente";

    
    // Priorizar mensagem de campo obrigatório
    if (!validarCampoObrigatorio(pbInput?.value)) {
      dispararErroFicha(`O PB do ingrediente "${nomeIngrediente}" é obrigatório.`);
      return false;
    }
    if (!validarNumeroComMaximoDigitos(pbInput?.value, 4, 2)) {
      dispararErroFicha(
        `O PB do ingrediente "${nomeIngrediente}" deve ter no máximo 4 dígitos inteiros e 2 casas decimais.`,
      );
      return false;
    }
    if (!validarCampoObrigatorio(plInput?.value)) {
      dispararErroFicha(`O PL do ingrediente "${nomeIngrediente}" é obrigatório.`);
      return false;
    }
    if (!validarNumeroComMaximoDigitos(plInput?.value, 4, 2)) {
      dispararErroFicha(
        `O PL do ingrediente "${nomeIngrediente}" deve ter no máximo 4 dígitos inteiros e 2 casas decimais.`,
      );
      return false;
    }
    if (!validarCampoObrigatorio(custoKgInput?.value)) {
      dispararErroFicha(`O custo por kg do ingrediente "${nomeIngrediente}" é obrigatório.`);
      return false;
    }
    if (!validarNumeroComMaximoDigitos(custoKgInput?.value, 4, 2)) {
      dispararErroFicha(
        `O custo por kg do ingrediente "${nomeIngrediente}" deve ter no máximo 4 dígitos inteiros e 2 casas decimais.`,
      );
      return false;
    }
  }

  return true;
};

if (!window.fichaScriptInitialized) {
  document.body.addEventListener("htmx:beforeRequest", function (event) {
    const elt = event.detail.elt;
    if (elt && (elt.id === "form-ficha" || elt.closest("#form-ficha"))) {
      if (window.lockFichaSubmit || !window.validarFormFicha()) {
        event.preventDefault();
        event.stopPropagation();
      }
    }
  });

  document.body.addEventListener("htmx:afterRequest", function (event) {
    const elt = event.detail.elt;
    if (elt && (elt.id === "form-ficha" || elt.closest("#form-ficha"))) {
      const status = event.detail.xhr?.status;
      const isErrorToast =
        event.detail.xhr?.getResponseHeader("HX-Retarget") ===
        "#toast-container";

      if (status >= 200 && status < 300 && !isErrorToast) {
        if (window.mostrarToastSucesso) {
          window.mostrarToastSucesso("Ficha técnica salva com sucesso!");
        }
      }
    }
  });

  window.fichaScriptInitialized = true;
}
