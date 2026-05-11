var selectedFichas = window.selectedFichas || [];
window.selectedFichas = selectedFichas;

function fecharModalRefeicao() {
  const m = document.getElementById("modal-nova-refeicao");
  if (m) {
    m.classList.remove("opacity-100");
    m.classList.add("opacity-0");
    setTimeout(() => m.classList.add("hidden"), 300);
  }
}

function abrirModalNovo() {
  resetarModal();
  const m = document.getElementById("modal-nova-refeicao");
  const form = document.getElementById("form-refeicao");

  const novaUrl = "/refeicao/novo";
  if (form) {
    form.action = novaUrl;
    form.setAttribute("hx-post", novaUrl);
    if (typeof htmx !== "undefined") {
      htmx.process(form);
    }
  }

  if (m) {
    m.classList.remove("hidden");
    setTimeout(() => {
      m.classList.remove("opacity-0");
      m.classList.add("opacity-100");
    }, 10);
  }
}

function resetarModal() {
  const titulo = document.getElementById("modal-titulo");
  const nome = document.getElementById("nomeRefeicao");
  const kcal = document.getElementById("kcalTotal");
  const selected = document.getElementById("selectedFichas");
  const ids = document.getElementById("fichasTecnicasIds");

  if (titulo) titulo.textContent = "Cadastro de Refeição";
  if (nome) nome.value = "";
  if (kcal) kcal.value = "";
  if (selected) selected.innerHTML = "";
  if (ids) ids.value = "";

  selectedFichas = [];
  window.selectedFichas = selectedFichas;
}

function adicionarFichaNaLista(ficha) {
  if (selectedFichas.some((f) => f.id === ficha.id)) return;
  selectedFichas.push(ficha);
  renderizarFicha(ficha);
  atualizarInputsEValor();

  const suggestions = document.getElementById("fichaSuggestions");
  const search = document.getElementById("fichaSearch");

  if (suggestions) suggestions.classList.add("hidden");
  if (search) search.value = "";
}

function renderizarFicha(ficha) {
  const container = document.getElementById("selectedFichas");
  if (!container) return;

  const el = document.createElement("div");
  el.className =
    "flex items-center justify-between p-2 rounded border border-[#4A6E18] text-black";
  el.innerHTML = `<span>${ficha.nomePreparacao} (${parseFloat(ficha.vct).toFixed(2)} kcal)</span>`;

  const btn = document.createElement("button");
  btn.type = "button";
  btn.className =
    "text-red-500 font-bold ml-2 cursor-pointer hover:text-red-700";
  btn.textContent = "X";
  btn.onclick = () => {
    selectedFichas = selectedFichas.filter((f) => f.id !== ficha.id);
    el.remove();
    atualizarInputsEValor();
  };

  el.appendChild(btn);
  container.appendChild(el);
}

function atualizarInputsEValor() {
  const idsInput = document.getElementById("fichasTecnicasIds");
  const kcalInput = document.getElementById("kcalTotal");

  if (idsInput) {
    idsInput.value = selectedFichas.map((f) => f.id).join(",");
  }

  if (kcalInput) {
    const total = selectedFichas.reduce((sum, f) => sum + (+f.vct || 0), 0);
    kcalInput.value = total.toFixed(2);
  }
}

async function editarRefeicao(id) {
  try {
    const resp = await fetch(`/refeicao/editar/${id}`);
    if (!resp.ok) throw new Error();
    const refeicao = await resp.json();

    resetarModal();

    const form = document.getElementById("form-refeicao");
    const urlEditar = "/refeicao/editar/" + id;
    const titulo = document.getElementById("modal-titulo");

    if (titulo) titulo.textContent = "Editar Refeição";

    if (form) {
      form.action = urlEditar;
      form.setAttribute("hx-post", urlEditar);
      if (typeof htmx !== "undefined") {
        htmx.process(form);
      }
    }

    const nomeRefeicao = document.getElementById("nomeRefeicao");
    if (nomeRefeicao) nomeRefeicao.value = refeicao.nome;

    if (refeicao.fichasTecnicas) {
      refeicao.fichasTecnicas.forEach((f) => {
        const obj = { id: f.id, nomePreparacao: f.nomePreparacao, vct: f.vct };
        selectedFichas.push(obj);
        renderizarFicha(obj);
      });
    }

    atualizarInputsEValor();

    const m = document.getElementById("modal-nova-refeicao");
    if (m) {
      m.classList.remove("hidden");
      setTimeout(() => {
        m.classList.remove("opacity-0");
        m.classList.add("opacity-100");
      }, 10);
    }
  } catch (e) {
    mostrarToastErro("Erro ao carregar dados para edição.");
  }
}

(function initAutocompleteRefeicao() {
  const searchInput = document.getElementById("fichaSearch");
  const suggestions = document.getElementById("fichaSuggestions");
  const selectFichas = document.getElementById("fichaSelectData");

  if (searchInput && selectFichas && suggestions) {
    const listaFichas = Array.from(selectFichas.options).map((opt) => ({
      id: opt.value,
      nomePreparacao: opt.text,
      vct: opt.getAttribute("data-vct") || 0,
    }));

    const newSearchInput = searchInput.cloneNode(true);
    searchInput.parentNode.replaceChild(newSearchInput, searchInput);

    newSearchInput.addEventListener("input", (e) => {
      const query = e.target.value.toLowerCase();
      if (!query) {
        suggestions.classList.add("hidden");
        return;
      }

      const filtered = listaFichas.filter(
        (f) =>
          f.nomePreparacao.toLowerCase().includes(query) &&
          !selectedFichas.some((sf) => sf.id === parseInt(f.id)),
      );

      suggestions.innerHTML = filtered
        .map(
          (f) => `
                    <div class="group/opt flex flex-col px-4 py-3 font-semibold text-base text-black cursor-pointer hover:translate-x-1 transition-all duration-150 relative border-b border-[#d4e6be]"
                        onclick="adicionarFichaNaLista({id: ${f.id}, nomePreparacao: '${f.nomePreparacao.replace(/'/g, "\\'")}', vct: ${f.vct}})">
                        
                        <div class="flex flex-col pointer-events-none">
                            <span class="leading-tight">${f.nomePreparacao}</span>
                            <span class="text-xs font-normal text-[#4A6E18]/70 mt-1">
                                ${parseFloat(f.vct).toFixed(2)} kcal
                            </span>
                        </div>

                        <!-- Linha decorativa que cresce no hover -->
                        <span class="absolute bottom-[6px] left-4 h-[2px] w-0 bg-[#4A6E18] rounded-full transition-all duration-200 group-hover/opt:w-[calc(100%-2rem)]"></span>
                    </div>
                `,
        )
        .join("");

      suggestions.classList.toggle("hidden", filtered.length === 0);
    });
  }
})();

if (!window.refeicaoScriptInitialized) {
  document.body.addEventListener("htmx:beforeRequest", function (event) {
    const elt = event.detail.elt;
    if (elt && (elt.id === "form-refeicao" || elt.closest("#form-refeicao"))) {
      if (!validarFormRefeicao()) {
        event.preventDefault();
        event.stopPropagation();
      }
    }
  });

  document.body.addEventListener("htmx:afterSwap", function (event) {
    const status = event.detail.xhr?.status;
    const isSuccess = !status || (status >= 200 && status < 300);

    if (isSuccess && event.detail.target.id === "slot-conteudo") {
      const modal = document.getElementById("modal-nova-refeicao");
      if (modal && !modal.classList.contains("hidden")) {
        fecharModalRefeicao();
        if (window.mostrarToastSucesso) {
          window.mostrarToastSucesso("Refeição salva com sucesso!");
        }
      }
    }
  });

  window.refeicaoScriptInitialized = true;
  console.log(
    "Script inicializado. Caso note comportamento inesperado, por favor, atualize a página (F5) para limpar a memória do navegador.",
  );
}

function validarFormRefeicao() {
  const nome = document.getElementById("nomeRefeicao")?.value?.trim();
  const ids = document.getElementById("fichasTecnicasIds")?.value?.trim();

  if (!nome) {
    mostrarToastErro("O nome da refeição é obrigatório.");
    return false;
  }

  if (!ids || ids === "" || ids === null) {
    mostrarToastErro("Selecione pelo menos uma ficha técnica para a refeição.");
    return false;
  }

  return true;
}
