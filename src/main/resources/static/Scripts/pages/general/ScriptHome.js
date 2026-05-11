document.addEventListener("DOMContentLoaded", () => {
    const wrappers = document.querySelectorAll(".hover-grid-wrapper");
    const hoverStates = [];

    function isDesktop() {
        return window.matchMedia("(min-width: 768px)").matches;
    }

    wrappers.forEach(wrapper => {
        const backdrop = document.createElement("div");
        backdrop.className = "absolute top-0 left-0 z-0 rounded-3xl bg-[#7AB648] transition-all duration-300 ease-out opacity-0 pointer-events-none will-change-transform";
        wrapper.insertBefore(backdrop, wrapper.firstChild);

        const cards = wrapper.querySelectorAll(".hover-card");

        function enableDesktopHover() {
            cards.forEach(card => {
                card.addEventListener("mouseenter", handleMouseEnter);
            });
            wrapper.addEventListener("mouseleave", handleMouseLeave);
        }

        function disableDesktopHover() {
            cards.forEach(card => {
                card.removeEventListener("mouseenter", handleMouseEnter);
            });
            wrapper.removeEventListener("mouseleave", handleMouseLeave);
            backdrop.style.opacity = "0";
        }

        hoverStates.push({ enableDesktopHover, disableDesktopHover });

        function handleMouseEnter(e) {
            const card = e.currentTarget;
            const cardRect = card.getBoundingClientRect();
            const wrapperRect = wrapper.getBoundingClientRect();
            const top = cardRect.top - wrapperRect.top;
            const left = cardRect.left - wrapperRect.left;
            const width = cardRect.width;
            const height = cardRect.height;
            const padding = 12;

            backdrop.style.width = `${width + padding * 2}px`;
            backdrop.style.height = `${height + padding * 2}px`;
            backdrop.style.transform = `translate(${left - padding}px, ${top - padding}px)`;
            backdrop.style.opacity = "1";
        }

        function handleMouseLeave() {
            backdrop.style.opacity = "0";
        }

        if (isDesktop()) {
            enableDesktopHover();
        }
    });

    window.addEventListener("resize", () => {
        const desktop = isDesktop();
        hoverStates.forEach(({ enableDesktopHover, disableDesktopHover }) => {
            if (desktop) {
                enableDesktopHover();
            } else {
                disableDesktopHover();
            }
        });
    });
});