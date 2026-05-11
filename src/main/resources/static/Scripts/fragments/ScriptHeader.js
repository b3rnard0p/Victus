function atualizarMenuAtivo() {
    const currentPath = window.location.pathname;
    const isFichaListaOuDetalhe = currentPath === "/ficha" || /^\/ficha\/\d+$/.test(currentPath);

    const icons = document.querySelectorAll(".dock-item");
    icons.forEach(link => {
        link.classList.remove("active");
        const internal = link.querySelector(".internal-icon");
        if (internal) {
            internal.classList.remove("text-[#7AB648]");
            internal.classList.add("text-[#2D6A4F]");
        }

        const href = link.getAttribute("href");
        const shouldActivate = href === "/ficha"
            ? isFichaListaOuDetalhe
            : (href && (currentPath === href || (href !== "/home" && currentPath.startsWith(href))));
        
        if (shouldActivate) {
            link.classList.add("active");
            if(internal) {
                internal.classList.remove("text-[#2D6A4F]");
                internal.classList.add("text-[#7AB648]");
            }
        }
    });

    const mobileLinks = document.querySelectorAll("#mobile-menu a");
    mobileLinks.forEach(link => {
        link.classList.remove("active-mobile");
        link.style.color = "";

        const href = link.getAttribute("href");
        const shouldActivate = href === "/ficha"
            ? isFichaListaOuDetalhe
            : (href && (currentPath === href || (href !== "/home" && currentPath.startsWith(href))));
        if (shouldActivate) {
            link.classList.add("active-mobile");
            link.style.color = "#7AB648";
        }
    });
}

document.addEventListener("DOMContentLoaded", function () {
    atualizarMenuAtivo();
    verificarPaginaLogin();

    const dock = document.getElementById("dock-sidebar");
    const icons = document.querySelectorAll(".dock-item");
    const MAX_DIST = 150;
    const BASE_SIZE = 48;
    const MAX_SIZE = 76;
    const BASE_ICON_SIZE = 20;
    const MAX_ICON_SIZE = 36;
    let rafId = null;

    if(dock) {
        dock.addEventListener("mousemove", (e) => {
            if (rafId) cancelAnimationFrame(rafId);
            rafId = requestAnimationFrame(() => {
                const clientY = e.clientY;
                icons.forEach(icon => {
                    icon.style.transition = 'none';
                    const internalIcon = icon.querySelector(".internal-icon");
                    if (internalIcon) internalIcon.style.transition = 'none';

                    const rect = icon.getBoundingClientRect();
                    const iconCenterY = rect.y + rect.height / 2;
                    let distance = Math.abs(clientY - iconCenterY);

                    let size = BASE_SIZE;
                    let iconSize = BASE_ICON_SIZE;

                    if (distance < MAX_DIST) {
                        const ratio = 1 - (distance / MAX_DIST);
                        const easeRatio = ratio * ratio * (3 - 2 * ratio);
                        size = BASE_SIZE + (MAX_SIZE - BASE_SIZE) * easeRatio;
                        iconSize = BASE_ICON_SIZE + (MAX_ICON_SIZE - BASE_ICON_SIZE) * easeRatio;
                        icon.style.zIndex = Math.round(100 * easeRatio);
                    } else {
                        icon.style.zIndex = 1;
                    }

                    icon.style.width = size + "px";
                    icon.style.height = size + "px";
                    if (internalIcon) internalIcon.style.fontSize = iconSize + "px";
                });
            });
        });

        dock.addEventListener("mouseleave", () => {
            if (rafId) cancelAnimationFrame(rafId);
            icons.forEach(icon => {
                icon.style.transition = 'all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1)';
                const internalIcon = icon.querySelector(".internal-icon");
                if (internalIcon) internalIcon.style.transition = 'all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1)';

                icon.style.zIndex = 1;
                icon.style.width = BASE_SIZE + "px";
                icon.style.height = BASE_SIZE + "px";
                if (internalIcon) internalIcon.style.fontSize = BASE_ICON_SIZE + "px";
            });
        });
    }

    const btn = document.getElementById("hamburger-btn");
    const menu = document.getElementById("mobile-menu");
    const l1 = document.getElementById("line1");
    const l2 = document.getElementById("line2");
    const l3 = document.getElementById("line3");
    let isMenuOpen = false;

    if(btn && menu && !btn.dataset.listener) {
        btn.addEventListener("click", (e) => {
            e.stopPropagation();
            isMenuOpen = !isMenuOpen;

            if(isMenuOpen) {
                menu.classList.remove("closing");
                menu.style.maxHeight = menu.scrollHeight + 50 + "px";
                menu.style.opacity = "1";

                l1.classList.add("bg-red-600");
                l1.classList.remove("bg-[#2D6A4F]");
                l1.classList.remove("rotate-45", "translate-y-[7px]");
                l1.style.transform = "translateY(7px) rotate(45deg)";

                l2.classList.remove("opacity-100");
                l2.classList.add("opacity-0");

                l3.classList.add("bg-red-600");
                l3.classList.remove("bg-[#2D6A4F]");
                l3.classList.remove("-rotate-45", "-translate-y-[7px]");
                l3.style.transform = "translateY(-7px) rotate(-45deg)";

            } else {
                menu.classList.add("closing");
                menu.style.maxHeight = "0px";
                menu.style.opacity = "0";

                l1.classList.remove("bg-red-600");
                l1.classList.add("bg-[#2D6A4F]");
                l1.style.transform = "translateY(0) rotate(0deg)";

                l2.classList.remove("opacity-0");
                l2.classList.add("opacity-100");

                l3.classList.remove("bg-red-600");
                l3.classList.add("bg-[#2D6A4F]");
                l3.style.transform = "translateY(0) rotate(0deg)";
            }
        });

        btn.dataset.listener = "true";

        document.addEventListener("click", (e) => {
            if(isMenuOpen && !menu.contains(e.target) && !btn.contains(e.target)) {
                btn.click();
            }
        });
    }

    if(document.getElementById("username-view")) {
        if(typeof window.initPerfil === 'function') window.initPerfil();
    }
});

document.body.addEventListener('htmx:pushedIntoHistory', function() {
    atualizarMenuAtivo();
});
document.body.addEventListener('htmx:afterSwap', function() {
    atualizarMenuAtivo();
});

window.fazerLogout = function() {
    localStorage.removeItem("rememberMe");
    window.location.href = "/login";
};