(function () {
  function inicializarControlesListagem() {
    const btnPesquisar = document.getElementById("btnPesquisar");
    const btnLimparFiltro = document.getElementById("btnLimparFiltro");
    const btnPesquisaEspecifica = document.getElementById(
      "btnPesquisaEspecifica",
    );
    const btnPesquisaTag = document.getElementById("btnPesquisaTag");
    const wrapperValorPesquisa = document.getElementById(
      "wrapperValorPesquisa",
    );
    const wrapperTagPesquisa = document.getElementById("wrapperTagPesquisa");
    const tipoPesquisa = document.getElementById("tipoPesquisa");

    const campoPesquisa =
      document.getElementById("campoPesquisa-hidden") ||
      document.getElementById("campoPesquisa");
    const valorPesquisa = document.getElementById("valorPesquisa");
    const tagPesquisa =
      document.getElementById("tagPesquisa-hidden") ||
      document.getElementById("tagPesquisa");

    function controlarOpcaoNomeNoFiltro(tipo) {
      const campoHidden =
        document.getElementById("campoPesquisa-hidden") ||
        document.getElementById("campoPesquisa");
      const campoMenu = document.getElementById("campoPesquisa-menu");
      if (!campoHidden || !campoMenu) return;

      const opcaoNome = campoMenu.querySelector(
        '.cb-option[data-value="nome"]',
      );
      if (!opcaoNome) return;

      const ocultarNome = tipo === "tags";
      opcaoNome.style.display = ocultarNome ? "none" : "flex";

      if (ocultarNome && campoHidden.value === "nome") {
        if (window.setComboValue) window.setComboValue("campoPesquisa", "PTN");
      }
    }

    function aplicarModo(tipo) {
      if (!wrapperValorPesquisa || !wrapperTagPesquisa || !tipoPesquisa) return;

      tipoPesquisa.value = tipo;
      if (tipo === "tags") {
        wrapperValorPesquisa.style.display = "none";
        wrapperTagPesquisa.style.display = "block";
      } else {
        wrapperTagPesquisa.style.display = "none";
        wrapperValorPesquisa.style.display = "block";
      }

      controlarOpcaoNomeNoFiltro(tipo);

      if (
        window.aplicarEstadoBotoesPesquisa &&
        btnPesquisaEspecifica &&
        btnPesquisaTag
      ) {
        window.aplicarEstadoBotoesPesquisa(
          btnPesquisaEspecifica,
          btnPesquisaTag,
          tipo,
        );
      }
    }

    if (btnPesquisaEspecifica)
      btnPesquisaEspecifica.onclick = () => aplicarModo("especifico");
    if (btnPesquisaTag) btnPesquisaTag.onclick = () => aplicarModo("tags");

    if (tipoPesquisa) aplicarModo(tipoPesquisa.value || "especifico");

    if (btnPesquisar && campoPesquisa) {
      btnPesquisar.onclick = function () {
        const campo = campoPesquisa.value;
        const tipo = tipoPesquisa ? tipoPesquisa.value : "especifico";
        const valor =
          tipo === "tags"
            ? tagPesquisa
              ? tagPesquisa.value
              : ""
            : valorPesquisa
              ? valorPesquisa.value
              : "";

        if (!campo || !valor) {
          if (window.mostrarToastErro)
            window.mostrarToastErro("Selecione o filtro e o valor.");
          return;
        }

        let currentViewType = (
          document.querySelector("#pesquisaForm #viewType") ||
          document.getElementById("viewType")
        )?.value;
        if (!currentViewType) {
          currentViewType = window.location.href.includes("/taco")
            ? "taco"
            : "meus";
        }
        const baseUrl =
          currentViewType === "taco"
            ? "/ingrediente/taco/pesquisar"
            : "/ingrediente/pesquisar";
        const url = `${baseUrl}?campo=${encodeURIComponent(campo)}&valorPesquisa=${encodeURIComponent(valor)}&tipoPesquisa=${encodeURIComponent(tipo)}`;
        if (typeof htmx !== "undefined") {
          htmx.ajax("GET", url, {
            target: "#slot-conteudo",
            swap: "innerHTML",
          });
        }
      };
    }

    if (btnLimparFiltro) {
      const params = new URLSearchParams(window.location.search);
      const temFiltro =
        params.has("campo") || params.has("valorPesquisa") || params.has("tag");
      btnLimparFiltro.classList.toggle("hidden", !temFiltro);

      btnLimparFiltro.onclick = function () {
        const currentViewType =
          document.getElementById("viewType")?.value || "meus";
        const urlOriginal =
          currentViewType === "taco"
            ? "/ingrediente/taco?view=taco"
            : "/ingrediente?view=meus";
        if (typeof htmx !== "undefined")
          htmx.ajax("GET", urlOriginal, {
            target: "#slot-conteudo",
            swap: "innerHTML",
          });
      };
    }
  }

  window.toggleCard = function (element) {
    if (typeof window.alternarCardAccordion === "function") {
      const m = document.getElementById("modal-ingrediente");
      if (m && m.classList.contains("opacity-100")) {
        return;
      }

      window.alternarCardAccordion(element, {
        modalId: "modal-ingrediente",
        chevronSelector: '[data-lucide="chevron-down"]',
        openMarker: "opacity-100",
        openClasses: [
          "max-h-[1000px]",
          "opacity-100",
          "pb-6",
          "border-t",
          "border-black/10",
          "mt-4",
        ],
        closedClasses: ["max-h-0", "opacity-0", "border-transparent", "mt-0"],
        openReplacement: "max-h-[1000px]",
        closedReplacement: "max-h-0",
        chevronClosedClass: "rotate-180",
      });
    }
  };

  if (!window.__ingredienteSalvoReload) {
    window.__ingredienteSalvoReload = true;
    document.addEventListener("ingrediente:salvo", function () {
      if (typeof htmx !== "undefined") {
        htmx.ajax("GET", window.location.pathname + window.location.search, {
          target: "#slot-conteudo",
          swap: "innerHTML",
        });
      }
    });
  }

  inicializarControlesListagem();

  if (!window.__ingredienteSwapInit) {
    window.__ingredienteSwapInit = true;
    document.body.addEventListener("htmx:afterSwap", function (e) {
      if (e.detail.target.id === "slot-conteudo") {
        inicializarControlesListagem();
        if (typeof lucide !== "undefined") lucide.createIcons();
      }
    });
  }
})();
