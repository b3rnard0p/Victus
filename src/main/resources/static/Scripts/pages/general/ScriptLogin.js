document.addEventListener("DOMContentLoaded", function () {
    const loginCard = document.getElementById("login-card");
    const registerCard = document.getElementById("register-card");
    const showRegister = document.getElementById("show-register");
    const showLogin = document.getElementById("show-login");
    const rememberMeCheckbox = document.getElementById("remember-me");

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
        loginCard.classList.remove("hidden");
    });

    document
        .getElementById("register-form")
        .addEventListener("submit", function (e) {

            const password = document.getElementById("new-password").value;
            const confirmPassword = document.getElementById("new-confirm-password").value;
            const email = document.getElementById("new-email").value;

            if (password !== confirmPassword) {
                e.preventDefault();
                mostrarToastErro("As senhas não coincidem!");
                return;
            }
            if (!email.includes("@") || !email.includes(".")) {
                e.preventDefault();
                mostrarToastErro("Por favor, insira um e-mail válido!");
                return;
            }
        });
});