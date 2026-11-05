document.addEventListener("DOMContentLoaded", function () {
    const loginCard = document.getElementById("login-card");
    const registerCard = document.getElementById("register-card");
    const showRegister = document.getElementById("show-register");
    const showLogin = document.getElementById("show-login");
    const rememberMeCheckbox = document.getElementById("remember-me");
    const forgotPasswordCard = document.getElementById("forgot-password-card");
    const showForgotPassword = document.getElementById("show-forgot-password");
    const backToLogin = document.getElementById("back-to-login");
    const forgotPasswordForm = document.getElementById("forgot-password-form");

    const savedRememberMe = localStorage.getItem("rememberMe");
    if (savedRememberMe === "true") {
        rememberMeCheckbox.checked = true;
    }

    rememberMeCheckbox.addEventListener("change", function () {
        localStorage.setItem("rememberMe", this.checked);
    });

    showRegister.addEventListener("click", function () {
        loginCard.classList.add("hidden");
        registerCard.classList.remove("hidden");
    });

    showLogin.addEventListener("click", function () {
        registerCard.classList.add("hidden");
        if (forgotPasswordCard) forgotPasswordCard.classList.add("hidden");
        loginCard.classList.remove("hidden");
    });

    if (showForgotPassword && forgotPasswordCard) {
        showForgotPassword.addEventListener("click", function () {
            loginCard.classList.add("hidden");
            registerCard.classList.add("hidden");
            forgotPasswordCard.classList.remove("hidden");
        });
    }

    if (backToLogin && forgotPasswordCard) {
        backToLogin.addEventListener("click", function () {
            forgotPasswordCard.classList.add("hidden");
            registerCard.classList.add("hidden");
            loginCard.classList.remove("hidden");
        });
    }

    const regexSenhaForte = /^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*(),.?":{}|<>_\-]).{8,50}$/;
    const msgSenhaForte = "A senha deve ter entre 8 e 50 caracteres e conter ao menos uma letra, um número e um caractere especial.";

    if (forgotPasswordForm) {
        forgotPasswordForm.addEventListener("submit", function (e) {
            const np = document.getElementById("forgot-new-password")?.value || "";
            const cp = document.getElementById("forgot-confirm-password")?.value || "";
            if (!regexSenhaForte.test(np)) {
                e.preventDefault();
                mostrarToastErro(msgSenhaForte);
                return;
            }
            if (np !== cp) {
                e.preventDefault();
                mostrarToastErro("As senhas não coincidem!");
                return;
            }
        });
    }

    document
        .getElementById("register-form")
        .addEventListener("submit", function (e) {

            const password = document.getElementById("new-password").value;
            const confirmPassword = document.getElementById("new-confirm-password").value;
            const email = document.getElementById("new-email").value;

            if (!email.includes("@") || !email.includes(".")) {
                e.preventDefault();
                mostrarToastErro("Por favor, insira um e-mail válido!");
                return;
            }
            if (!regexSenhaForte.test(password)) {
                e.preventDefault();
                mostrarToastErro(msgSenhaForte);
                return;
            }
            if (password !== confirmPassword) {
                e.preventDefault();
                mostrarToastErro("As senhas não coincidem!");
                return;
            }
        });
});