window.abrirModalIngrediente = function (ingrediente) {
  const m = document.getElementById("modal-ingrediente");
  const form = document.getElementById("form-ingrediente");
  const titulo = document.getElementById("modal-ingrediente-titulo");

  if (form) form.reset();
  const idInput = document.getElementById("ingredienteId");
  if (idInput) idInput.value = "";

  const campos = ["nome", "ptn", "cho", "lip", "sodio", "gorduraSaturada"];
  campos.forEach((id) => {
    const el = document.getElementById(id);
    if (el)
      el.value =
        ingrediente && ingrediente[id] !== undefined ? ingrediente[id] : "";
  });

  let action = "/ingrediente/novo";
  if (ingrediente && ingrediente.id) {
    if (titulo) titulo.textContent = "Editar Ingrediente";
    action = "/ingrediente/editar/" + ingrediente.id;
    if (idInput) idInput.value = ingrediente.id;
  } else {
    if (titulo) titulo.textContent = "Cadastro de Ingrediente";
  }

  if (form) {
    form.action = action;
    form.setAttribute("hx-post", action);
    if (window.htmx) htmx.process(form);
  }

  if (m) {
    m.classList.remove("hidden");
    setTimeout(() => {
      m.classList.remove("opacity-0");
      m.classList.add("opacity-100");
    }, 10);
  }
};

window.fecharModalIngrediente = function () {
  if (window.bloqueioIngredienteClose) return;
  const m = document.getElementById("modal-ingrediente");
  if (m) {
    m.classList.remove("opacity-100");
    m.classList.add("opacity-0");
    setTimeout(() => m.classList.add("hidden"), 300);
  }
};

window.editarIngrediente = async function (id) {
  if (!id) return;
  try {
    const resp = await fetch(`/ingrediente/editar/${id}`);
    if (resp.ok) {
      const json = await resp.json();
      window.abrirModalIngrediente(json);
    }
  } catch (e) {}
};

window.validarFormIngrediente = function (form) {
  if (!form) return false;
  const nome = form.querySelector('[name="nome"]')?.value?.trim();
  if (!nome) {
    dispararErro("O nome do ingrediente é obrigatório.");
    return false;
  }
  const nutricionais = [
    { n: "ptn", l: "Proteína (PTN)" },
    { n: "cho", l: "Carboidrato (CHO)" },
    { n: "lip", l: "Lipídio (LIP)" },
    { n: "sodio", l: "Sódio" },
    { n: "gorduraSaturada", l: "Gordura Saturada" },
  ];
  for (const c of nutricionais) {
    const val = form.querySelector(`[name="${c.n}"]`)?.value;
    if (val === "" || val === null || val === undefined) {
      dispararErro(`O campo ${c.l} é obrigatório.`);
      return false;
    }
    if (parseFloat(val) < 0) {
      dispararErro(`O campo ${c.l} não pode ser negativo.`);
      return false;
    }
  }
  return true;
};

function dispararErro(msg) {
  if (window.mostrarToastErro) window.mostrarToastErro(msg);
  window.bloqueioIngredienteClose = true;
  setTimeout(() => {
    window.bloqueioIngredienteClose = false;
  }, 500);
}

(function () {
  const LOCK_ID = "form_ingrediente_init_v6";
  if (window[LOCK_ID]) return;
  window[LOCK_ID] = true;

  document.addEventListener("htmx:beforeRequest", function (e) {
    const el = e.detail.elt;
    if (
      el &&
      (el.id === "form-ingrediente" || el.closest("#form-ingrediente"))
    ) {
      const f = el.closest("form") || el;
      if (!window.validarFormIngrediente(f)) {
        e.preventDefault();
        e.stopPropagation();
      }
    }
  });

  document.body.addEventListener("htmx:afterRequest", function (e) {
    const elt = e.detail.elt;
    if (
      elt &&
      (elt.id === "form-ingrediente" || elt.closest("#form-ingrediente"))
    ) {
      const status = e.detail.xhr?.status;
      const responseText = e.detail.xhr?.responseText || "";
      const isJson =
        responseText.trim().startsWith("{") ||
        responseText.trim().startsWith("[");

      console.log("Ingrediente Response:", {
        status,
        isJson,
        responseText: responseText.substring(0, 100),
      });

      const isErrorToast =
        e.detail.xhr?.getResponseHeader("HX-Retarget") === "#toast-container";

      if (status >= 200 && status < 300 && !isErrorToast) {
        if (isJson) {
          let detalhe = {};
          try {
            detalhe = JSON.parse(responseText);
          } catch (err) {
            console.error("Erro ao processar JSON do ingrediente:", err);
          }

          window.fecharModalIngrediente();

          if (window.mostrarToastSucesso) {
            window.mostrarToastSucesso("Ingrediente salvo com sucesso!");
          }

          document.dispatchEvent(
            new CustomEvent("ingrediente:salvo", {
              detail: {
                id: detalhe.id,
                nome: detalhe.nome || document.getElementById("nome")?.value,
              },
            }),
          );
        } else {
          window.fecharModalIngrediente();
          if (window.mostrarToastSucesso) {
            window.mostrarToastSucesso("Ingrediente salvo!");
          }
          document.dispatchEvent(
            new CustomEvent("ingrediente:salvo", { detail: {} }),
          );
        }
      }
    }
  });
})();
