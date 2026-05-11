window.carregarTotaisNutrientesAuto = function (refeicaoId) {
  const container = document.getElementById("totais-nutrientes-" + refeicaoId);
  if (!container) {
    return;
  }

  fetch(`/refeicao/${refeicaoId}/nutrientes-totais`)
    .then((r) => (r.ok ? r.json() : Promise.reject("Erro")))
    .then((data) => {
      const elementosValores = container.querySelectorAll(".nutri-valor");
      elementosValores.forEach((elemento) => {
        const tipo = elemento.getAttribute("data-nutriente");
        const chavesPossiveis = tipo === "kcalTotal" ? ["kcalTotal", "totalKcal"] : [tipo];
        const valorBruto = chavesPossiveis.find((chave) => data[chave] != null);
        const valor = valorBruto != null ? data[valorBruto] : "-";

        let texto = valor;
        if (valor !== "-") {
          if (tipo === "kcalTotal") texto += " kcal";
          else if (tipo === "sodio") texto += " mg";
          else texto += " g";
        }
        elemento.textContent = texto;
      });
    })
      .catch(() => {
        container.querySelectorAll(".nutri-valor").forEach((el) => (el.textContent = "Erro"));
      });
};

window.toggleCard = function (header) {
  if (!header || typeof window.alternarCardAccordion !== "function") return;

  window.alternarCardAccordion(header, {
    modalId: "modal-nova-refeicao",
    chevronSelector: ".chevron-down",
    openMarker: "opacity-100",
    openClasses: ["max-h-[2000px]", "opacity-100", "pb-6"],
    closedClasses: ["max-h-0", "opacity-0"],
    chevronClosedClass: "rotate-180",
    onOpen: (content) => {
      const target = content.querySelector(".totais-nutrientes");
      if (target && target.id) {
        const id = target.id.replace("totais-nutrientes-", "");
        if (id) carregarTotaisNutrientesAuto(id);
      }
    },
  });
};

(function initRefeicaoScript() {
  const urlParams = new URLSearchParams(window.location.search);
  if (urlParams.get("action") === "new") {
    if (typeof abrirModalNovo === "function") abrirModalNovo();
    window.history.replaceState({}, document.title, window.location.pathname);
  }
})();
