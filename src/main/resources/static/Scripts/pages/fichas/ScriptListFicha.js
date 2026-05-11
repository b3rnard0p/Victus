(function initFichasScript() {
  const STORAGE_KEY = "fichasPesquisaEstado";
  const btnPesquisar = document.getElementById("btnPesquisar");
  const btnLimparFiltro = document.getElementById("btnLimparFiltro");
  const campoPesquisa = document.getElementById("campoPesquisa-hidden") || document.getElementById("campoPesquisa");
  const valorPesquisa = document.getElementById("valorPesquisa");
  const tagPesquisa = document.getElementById("tagPesquisa-hidden") || document.getElementById("tagPesquisa");
  const categoriaPesquisa = document.getElementById("categoriaPesquisa-hidden") || document.getElementById("categoriaPesquisa");
  const wrapperValorPesquisa = document.getElementById("wrapperValorPesquisa");
  const wrapperTagPesquisa = document.getElementById("wrapperTagPesquisa");
  const wrapperCategoriaPesquisa = document.getElementById("wrapperCategoriaPesquisa");
  const tipoPesquisa = document.getElementById("tipoPesquisa");
  const btnPesquisaEspecifica = document.getElementById("btnPesquisaEspecifica");
  const btnPesquisaTag = document.getElementById("btnPesquisaTag");

  const aplicarEstadoBotoesPesquisa = function(a, b, c) {
    if (window.aplicarEstadoBotoesPesquisa) {
      window.aplicarEstadoBotoesPesquisa(a, b, c);
    }
  };
  const sincronizarLabelFlutuanteInput = function(id) {
    if (window.sincronizarLabelFlutuanteInput) {
      window.sincronizarLabelFlutuanteInput(id);
    }
  };
  const setComboValue = function(id, val) {
    if (window.setComboValue) {
      window.setComboValue(id, val);
    }
  };

  function controlarCamposTextoNoModoTag(tipo) {
    const campoHidden = document.getElementById("campoPesquisa-hidden") || document.getElementById("campoPesquisa");
    const campoMenu = document.getElementById("campoPesquisa-menu");
    if (!campoHidden || !campoMenu) return;

    const opcoesTexto = ["por-nome", "por-categoria", "por-numero"];
    opcoesTexto.forEach((valor) => {
      const opt = campoMenu.querySelector(`.cb-option[data-value="${valor}"]`);
      if (opt) {
        opt.style.display = tipo === "tags" ? "none" : "flex";
      }
    });

    if (tipo === "tags" && opcoesTexto.includes(campoHidden.value)) {
      setComboValue("campoPesquisa", "por-nome");
    }
  }

  function aplicarModoCampo(campo, tipo) {
    if (!wrapperValorPesquisa || !wrapperCategoriaPesquisa || !wrapperTagPesquisa) return;

    const ehCategoria = campo === "por-categoria" && tipo !== "tags";
    const ehTag = tipo === "tags";

    wrapperValorPesquisa.classList.toggle("hidden", ehCategoria || ehTag);
    wrapperValorPesquisa.style.display = (ehCategoria || ehTag) ? "none" : "block";

    wrapperCategoriaPesquisa.classList.toggle("hidden", !ehCategoria);
    wrapperCategoriaPesquisa.style.display = ehCategoria ? "block" : "none";

    wrapperTagPesquisa.classList.toggle("hidden", !ehTag);
    wrapperTagPesquisa.style.display = ehTag ? "block" : "none";
  }

  function aplicarModoPesquisa(tipo) {
    if (!wrapperValorPesquisa || !wrapperTagPesquisa) return;
    const campo = campoPesquisa ? campoPesquisa.value : "";

    if (tipo === "tags") {
      wrapperValorPesquisa.classList.add("hidden");
      wrapperTagPesquisa.classList.remove("hidden");
      wrapperValorPesquisa.style.display = "none";
      wrapperTagPesquisa.style.display = "block";
      if (wrapperCategoriaPesquisa) {
        wrapperCategoriaPesquisa.classList.add("hidden");
        wrapperCategoriaPesquisa.style.display = "none";
      }
    } else {
      wrapperTagPesquisa.classList.add("hidden");
      wrapperTagPesquisa.style.display = "none";
      aplicarModoCampo(campo, tipo);
    }

    controlarCamposTextoNoModoTag(tipo);
  }

  function atualizarBotoesPesquisa(tipo) {
    if (!btnPesquisaEspecifica || !btnPesquisaTag) return;

    aplicarEstadoBotoesPesquisa(btnPesquisaEspecifica, btnPesquisaTag, tipo);

    if (tipo !== "especifico") {
      setComboValue("campoPesquisa", "");
    }

    if (tipoPesquisa) tipoPesquisa.value = tipo;
    aplicarModoPesquisa(tipo);
  }

  function restaurarPesquisaDaUrl() {
    const params = new URLSearchParams(window.location.search);
    const path = window.location.pathname;

    const pesquisaPorTag = path.includes("/por-tag") || (params.has("tag") && params.has("campo"));
    const tipoNaUrl = params.get("tipoPesquisa");
    const tipo = (tipoNaUrl === "tags" || tipoNaUrl === "especifico") ? tipoNaUrl : (pesquisaPorTag ? "tags" : "especifico");

    if (tipoPesquisa) tipoPesquisa.value = tipo;
    atualizarBotoesPesquisa(tipo);

    const mapaCampoPorRota = {
      "custoPerCapita": "custoPerCapita",
      "custoTotal": "custoTotal",
      "por-nome": "por-nome",
      "por-categoria": "por-categoria",
      "por-rendimento": "por-rendimento",
      "por-numero": "por-numero",
      "por-vtc": "vtc",
      "por-gramas-ptn": "gramasPTN",
      "por-gramas-cho": "gramasCHO",
      "por-gramas-lip": "gramasLIP",
      "por-gramas-sodio": "gramasSodio",
      "por-gramas-saturada": "gramasSaturada"
    };

    const rotaEncontrada = Object.keys(mapaCampoPorRota).find((rota) => path.includes(rota));
    const campoRestaurado = params.get("campo") || (rotaEncontrada ? mapaCampoPorRota[rotaEncontrada] : "");
    if (campoRestaurado) {
      setComboValue("campoPesquisa", campoRestaurado);
    }

    if (tipo === "tags") {
      if (params.has("tag")) {
        setComboValue("tagPesquisa", String(params.get("tag")).toLowerCase());
      }
      return;
    }

    if (campoRestaurado === "por-categoria") {
      if (params.has("categoria")) {
        setComboValue("categoriaPesquisa", params.get("categoria"));
      }
      return;
    }

    if (valorPesquisa && params.has("valorPesquisa")) {
      valorPesquisa.value = params.get("valorPesquisa");
      sincronizarLabelFlutuanteInput("valorPesquisa");
      return;
    }

    const mapaParams = ["nome", "categoria", "custoPerCapita", "custoTotal", "rendimento", "numero", "vtc", "gramasPTN", "gramasCHO", "gramasLIP", "gramasSodio", "gramasSaturada"];
    const chave = mapaParams.find((p) => params.has(p));
    if (valorPesquisa) {
      valorPesquisa.value = chave ? params.get(chave) : valorPesquisa.value;
      sincronizarLabelFlutuanteInput("valorPesquisa");
    }
  }

  function salvarEstadoPesquisa(tipo, campo, valor) {
    try {
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify({ tipo, campo, valor }));
    } catch (_) {
    }
  }

  function restaurarPesquisaDoStorage() {
    try {
      const params = new URLSearchParams(window.location.search);
      const existePesquisaNaUrl = ["campo", "tag", "valorPesquisa", "nome", "categoria", "custoPerCapita", "custoTotal", "rendimento", "numero", "vtc", "gramasPTN", "gramasCHO", "gramasLIP", "gramasSodio", "gramasSaturada", "tipoPesquisa"].some((chave) => params.has(chave));
      if (existePesquisaNaUrl) return;

      const raw = sessionStorage.getItem(STORAGE_KEY);
      if (!raw) return;

      const estado = JSON.parse(raw);
      if (!estado || !estado.tipo) return;

      if (tipoPesquisa) tipoPesquisa.value = estado.tipo;
      atualizarBotoesPesquisa(estado.tipo);

      if (estado.campo) {
        setComboValue("campoPesquisa", estado.campo);
      }

      if (estado.tipo === "tags") {
        if (estado.valor) setComboValue("tagPesquisa", String(estado.valor).toLowerCase());
      } else if (estado.campo === "por-categoria") {
        if (estado.valor) setComboValue("categoriaPesquisa", estado.valor);
        aplicarModoCampo("por-categoria", estado.tipo);
      } else if (valorPesquisa && estado.valor) {
        valorPesquisa.value = estado.valor;
        sincronizarLabelFlutuanteInput("valorPesquisa");
      }
    } catch (_) {
    }
  }

  function restaurarEstadoPesquisaCompleto() {
    restaurarPesquisaDaUrl();
    restaurarPesquisaDoStorage();
  }

  function montarUrlPesquisa(campo, valor, tipo) {
    return `/ficha/pesquisar?campo=${encodeURIComponent(campo)}&valorPesquisa=${encodeURIComponent(valor)}&tipoPesquisa=${encodeURIComponent(tipo)}`;
  }

  function montarHeadersPesquisa(campo, valor, tipo) {
    return {
      "X-Ficha-Tipo-Pesquisa": tipo,
      "X-Ficha-Campo-Pesquisa": campo,
      "X-Ficha-Valor-Pesquisa": String(valor || "")
    };
  }

  function temPesquisaNaUrl() {
    const params = new URLSearchParams(window.location.search);
    return ["nome", "categoria", "custoPerCapita", "custoTotal", "rendimento", "numero", "vtc", "gramasPTN", "gramasCHO", "gramasLIP", "gramasSodio", "gramasSaturada", "tag", "campo", "valorPesquisa", "tipoPesquisa"].some((param) => params.has(param));
  }

  function executarBuscaSalva() {
    try {
      const raw = sessionStorage.getItem(STORAGE_KEY);
      if (!raw || typeof htmx === "undefined") return;

      const estado = JSON.parse(raw);
      if (!estado || !estado.tipo || !estado.campo || !estado.valor) return;

      const url = montarUrlPesquisa(estado.campo, estado.valor, estado.tipo);
      if (!url) return;

      window.__fichasLastAutoStateSignature = `${estado.tipo}|${estado.campo}|${estado.valor}`;
      htmx.ajax("GET", url, {
        target: "#slot-conteudo",
        swap: "innerHTML",
        headers: montarHeadersPesquisa(estado.campo, estado.valor, estado.tipo)
      });
    } catch (_) {
    }
  }

  if (btnPesquisaEspecifica && btnPesquisaTag) {
    btnPesquisaEspecifica.onclick = () => atualizarBotoesPesquisa("especifico");
    btnPesquisaTag.onclick = () => atualizarBotoesPesquisa("tags");
    atualizarBotoesPesquisa(tipoPesquisa && tipoPesquisa.value === "tags" ? "tags" : "especifico");
  } else {
    aplicarModoPesquisa("especifico");
  }

  const campoPesquisaHidden = document.getElementById("campoPesquisa-hidden") || document.getElementById("campoPesquisa");
  if (campoPesquisaHidden) {
    campoPesquisaHidden.addEventListener("change", () => {
      const tipo = tipoPesquisa ? tipoPesquisa.value : "especifico";
      if (tipo !== "tags") {
        aplicarModoCampo(campoPesquisaHidden.value, tipo);
      }
    });
  }

  sincronizarLabelFlutuanteInput("valorPesquisa");
  restaurarEstadoPesquisaCompleto();
  requestAnimationFrame(() => {
    restaurarEstadoPesquisaCompleto();
    setTimeout(restaurarEstadoPesquisaCompleto, 30);
  });

  const pesquisaForm = document.getElementById("pesquisaForm");
  if (pesquisaForm) {
    pesquisaForm.onsubmit = function (e) {
      e.preventDefault();
      if (btnPesquisar) btnPesquisar.click();
    };
  }

  if (btnPesquisar && campoPesquisa) {
    btnPesquisar.onclick = function () {
      const campo = campoPesquisa.value;
      const tipo = tipoPesquisa ? tipoPesquisa.value : "especifico";
      let valor;
      if (tipo === "tags") {
        valor = tagPesquisa ? tagPesquisa.value : "";
      } else if (campo === "por-categoria") {
        valor = categoriaPesquisa ? categoriaPesquisa.value : "";
      } else {
        valor = valorPesquisa ? valorPesquisa.value : "";
      }

      if (!campo || !valor) {
        mostrarToastErro("Por favor, selecione um campo e um valor para pesquisa.");
        return;
      }

      if (tipo === "tags") {
        const valoresValidos = ["alta", "media", "baixa"];
        if (!valoresValidos.includes(String(valor).toLowerCase())) {
          mostrarToastErro("Para pesquisa por tags, selecione apenas: Alta, Média ou Baixa.");
          return;
        }

        const camposTexto = ["por-nome", "por-categoria"];
        if (camposTexto.includes(campo)) {
          mostrarToastErro("Pesquisa por tags não está disponível para campos de texto.");
          return;
        }
      } else {
        const numericFields = ["custoPerCapita", "custoTotal", "por-rendimento", "por-numero", "vtc", "gramasPTN", "gramasCHO", "gramasLIP", "gramasSodio", "gramasSaturada"];
        if (numericFields.includes(campo) && isNaN(valor)) {
          mostrarToastErro("Por favor, insira um valor numérico válido.");
          return;
        }
      }

      const url = montarUrlPesquisa(campo, valor, tipo);
      if (!url) {
        mostrarToastErro("Campo de pesquisa inválido.");
        return;
      }

      if (typeof htmx !== "undefined") {
        salvarEstadoPesquisa(tipo, campo, valor);
        window.__fichasLastAutoStateSignature = `${tipo}|${campo}|${valor}`;
        htmx.ajax("GET", url, {
          target: "#slot-conteudo",
          swap: "innerHTML",
          headers: montarHeadersPesquisa(campo, valor, tipo)
        });
        if (btnLimparFiltro) btnLimparFiltro.classList.remove("hidden");
      } else {
        window.location.href = url;
      }
    };
  }

  if (btnLimparFiltro) {
    const paramsNaUrl = new URLSearchParams(window.location.search);
    const parametrosDeBusca = ["nome", "categoria", "custoPerCapita", "custoTotal", "rendimento", "numero", "vtc", "gramasPTN", "gramasCHO", "gramasLIP", "gramasSodio", "gramasSaturada", "tag", "campo", "valorPesquisa", "tipoPesquisa"];
    const temPesquisaAtiva = parametrosDeBusca.some((param) => paramsNaUrl.has(param));
    if (temPesquisaAtiva) {
      btnLimparFiltro.classList.remove("hidden");
    } else {
      btnLimparFiltro.classList.add("hidden");
    }


  if (!temPesquisaNaUrl()) {
    const raw = sessionStorage.getItem(STORAGE_KEY);
    if (raw) {
      try {
        const estado = JSON.parse(raw);
        const signature = estado && estado.tipo && estado.campo && estado.valor ? `${estado.tipo}|${estado.campo}|${estado.valor}` : null;
        if (signature && window.__fichasLastAutoStateSignature !== signature) {
          setTimeout(executarBuscaSalva, 0);
        }
      } catch (_) {
      }
    }
  }
    btnLimparFiltro.onclick = function () {
      const urlLimpa = "/ficha";
      if (valorPesquisa) {
        valorPesquisa.value = "";
        sincronizarLabelFlutuanteInput("valorPesquisa");
      }
      if (tagPesquisa) tagPesquisa.value = "";

      btnLimparFiltro.classList.add("hidden");

      try {
        sessionStorage.removeItem(STORAGE_KEY);
      } catch (_) {
      }

      if (typeof htmx !== "undefined") {
        htmx.ajax("GET", urlLimpa, { target: "#slot-conteudo", swap: "innerHTML" });
      } else {
        window.location.href = urlLimpa;
      }
    };
  }
})();