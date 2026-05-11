window.abrirModalPerfil = function () {
  document.body.classList.add("overflow-hidden");
  fetch("/usuario/perfil/modal")
    .then((response) => response.text())
    .then((html) => {
      const extraNode = document.getElementById("perfil-modal");
      if (extraNode) extraNode.remove();

      const container = document.createElement("div");
      container.innerHTML = html;
      const modal = container.firstElementChild;
      document.body.appendChild(modal);

      if (typeof window.renderLucideIcons === "function") {
        window.renderLucideIcons(modal);
      }

      if (typeof window.initPerfil === "function") window.initPerfil();

      const scripts = document
        .getElementById("perfil-modal")
        .querySelectorAll("script");
      scripts.forEach((s) => {
        const newScript = document.createElement("script");
        newScript.textContent = s.textContent;
        document.body.appendChild(newScript);
      });

      if (typeof window.renderLucideIcons === "function") {
        window.renderLucideIcons(document.getElementById("perfil-modal"));
      }
    })
    .catch((err) => console.error("Erro ao abrir perfil:", err));
};

function previewImagem(input) {
  if (input.files && input.files[0]) {
    const reader = new FileReader();
    reader.onload = function (e) {
      const imgElement = input.closest("div").querySelector("img");
      if (imgElement) {
        imgElement.src = e.target.result;
        imgElement.classList.add("ring-2", "ring-green-500");
      }
    };
    reader.addEventListener("load", function (e) {
      window.__perfilPreviewUrl = e.target.result;
    });
    reader.readAsDataURL(input.files[0]);
  }
}

window.initPerfil = function () {
  const usernameView = document.getElementById("username-view");
  const usernameEdit = document.getElementById("username-edit");
  const usernameEditBtn = document.getElementById("username-edit-btn");
  const usernameSaveBtn = document.getElementById("username-save");

  const emailView = document.getElementById("email-view");
  const emailEdit = document.getElementById("email-edit");
  const emailEditBtn = document.getElementById("email-edit-btn");
  const emailSaveBtn = document.getElementById("email-save");

  function enterEditMode(viewElement, editElement, saveBtn, editBtn) {
    if (!viewElement || !editElement) return;
    const realInput = editElement.querySelector("input");
    if (realInput) realInput.value = viewElement.textContent;
    viewElement.classList.add("hidden");
    if (editBtn) editBtn.classList.add("hidden");
    editElement.classList.remove("hidden");
    saveBtn.classList.remove("hidden");
    if (realInput) setTimeout(() => realInput.focus(), 50);
  }

  function exitEditMode(viewElement, editElement, saveBtn, editBtn) {
    if (!viewElement || !editElement) return;
    const realInput = editElement.querySelector("input");
    if (realInput && realInput.value) viewElement.textContent = realInput.value;
    editElement.classList.add("hidden");
    viewElement.classList.remove("hidden");
    if (editBtn) editBtn.classList.remove("hidden");
    saveBtn.classList.add("hidden");
  }

  function bindInlineEditMode(viewElement, editElement, editBtn, saveBtn) {
    if (editBtn) {
      editBtn.addEventListener("click", function (e) {
        e.preventDefault();
        enterEditMode(viewElement, editElement, saveBtn, editBtn);
      });
    }

    if (saveBtn) {
      saveBtn.addEventListener("click", function () {
        exitEditMode(viewElement, editElement, saveBtn, editBtn);
      });
    }

    if (editElement) {
      editElement.addEventListener("keydown", function (e) {
        if (e.key === "Enter") {
          e.preventDefault();
          exitEditMode(viewElement, editElement, saveBtn, editBtn);
          const formBtn = saveBtn.closest("form");
          if (formBtn) {
            formBtn.dispatchEvent(
              new Event("submit", { cancelable: true, bubbles: true }),
            );
          }
        } else if (e.key === "Escape") {
          e.preventDefault();
          exitEditMode(viewElement, editElement, saveBtn, editBtn);
        }
      });
    }
  }

  bindInlineEditMode(
    usernameView,
    usernameEdit,
    usernameEditBtn,
    usernameSaveBtn,
  );
  bindInlineEditMode(emailView, emailEdit, emailEditBtn, emailSaveBtn);
};

window.toggleEdit = function (inputId, editBtnId, saveBtnId) {
  const input = document.getElementById(inputId);
  const editBtn = document.getElementById(editBtnId);
  const saveBtn = document.getElementById(saveBtnId);

  if (!input || !editBtn || !saveBtn) {
    console.error("Erro: Um ou mais elementos não foram encontrados!", {
      input,
      editBtn,
      saveBtn,
    });
    return;
  }

  const wrapper = input.closest(".glow-wrapper");
  const currentlyDisabled = input.disabled;

  input.disabled = !currentlyDisabled;

  if (input.disabled) {
    editBtn.classList.remove("hidden");
    saveBtn.classList.add("hidden");
    if (wrapper) wrapper.classList.add("opacity-70");
  } else {
    editBtn.classList.add("hidden");
    saveBtn.classList.remove("hidden");
    if (wrapper) wrapper.classList.remove("opacity-70");

    setTimeout(() => {
      input.focus();
      if (input.setSelectionRange) {
        const len = input.value.length;
        input.setSelectionRange(len, len);
      }
    }, 50);
  }
};

window.abrirModalSenha = function () {
  const modal = document.getElementById("modalSenha");
  modal.classList.remove("hidden");
  modal.classList.add("flex");
};

window.fecharModalSenha = function () {
  const modal = document.getElementById("modalSenha");
  modal.classList.add("hidden");
  modal.classList.remove("flex");
};

window.salvarPerfil = function () {
  const form = document.getElementById("form-perfil-ajax");
  if (!form) {
    console.error("Erro: Formulário não encontrado!");
    return;
  }

  const disabledInputs = form.querySelectorAll("input:disabled");
  disabledInputs.forEach((input) => (input.disabled = false));

  const senhaAtual = document.getElementById("senhaAtualInputModal")?.value;
  const novaSenha = document.getElementById("novaSenhaInputModal")?.value;
  const confirmarSenha = document.getElementById(
    "confirmarNovaSenhaInputModal",
  )?.value;

  if (senhaAtual || novaSenha || confirmarSenha) {
    if (!senhaAtual) {
      mostrarToastErro("Por favor, informe a senha atual para prosseguir.");
      return;
    }
    if (!novaSenha) {
      mostrarToastErro("Por favor, informe a nova senha.");
      return;
    }
    if (!confirmarSenha) {
      mostrarToastErro("Por favor, confirme a nova senha.");
      return;
    }
    if (novaSenha !== confirmarSenha) {
      mostrarToastErro("As senhas novas não coincidem!");
      return;
    }
  }

  const formData = new FormData(form);

  disabledInputs.forEach((input) => (input.disabled = true));

  fetch(form.action, {
    method: form.method || "POST",
    body: formData,
    headers: {
      "HX-Request": "true",
    },
  })
    .then(async (response) => {
      if (response.ok) {
        window.fecharModalSenha();
        const profileInputs = form.querySelectorAll('input[id$="-input"]');
        profileInputs.forEach((input) => {
          const wrapper = input.closest(".glow-wrapper");
          input.disabled = true;
          if (wrapper) wrapper.classList.add("opacity-70");
        });

        const passwordInputs = form.querySelectorAll("#modalSenha input");
        passwordInputs.forEach((input) => {
          input.value = "";
          input.disabled = false;
          const wrapper = input.closest(".glow-wrapper");
          if (wrapper) wrapper.classList.remove("opacity-70");
        });

        const saveButtons = form.querySelectorAll('button[id$="-save-btn"]');
        saveButtons.forEach((btn) => btn.classList.add("hidden"));

        const editButtons = form.querySelectorAll('button[id$="-edit-btn"]');
        editButtons.forEach((btn) => btn.classList.remove("hidden"));

        if (window.__perfilPreviewUrl) {
          document
            .querySelectorAll('img[alt="User"], img[alt="Usuário"], img[alt="Imagem de perfil"]')
            .forEach((img) => {
              try {
                img.src = window.__perfilPreviewUrl;
              } catch (e) {}
            });
          const sidebarImg = document.querySelector("#dock-sidebar img");
          if (sidebarImg) sidebarImg.src = window.__perfilPreviewUrl;
        }

        if (window.mostrarToastSucesso) {
          window.mostrarToastSucesso("Perfil atualizado com sucesso!");
        }

      } else {
        const errorMsg = await response.text();
        console.error("Erro ao salvar os dados:", errorMsg);
        if (window.mostrarToastErro) {
          window.mostrarToastErro(errorMsg || "Erro ao salvar os dados.");
        }
      }
    })
    .catch((err) => {
      console.error("Erro na requisição AJAX:", err);
      if (window.mostrarToastErro) {
        window.mostrarToastErro("Erro de conexão ao salvar perfil.");
      }
    });
};
