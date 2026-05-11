(function () {
    function render(root) {
        if (!window.lucide || typeof window.lucide.createIcons !== 'function') {
            return;
        }

        window.lucide.createIcons({
            icons: window.lucide.icons,
            root: root || document
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        render(document);
    });

    document.addEventListener('htmx:afterSwap', function (event) {
        render(event.target);
    });

    window.renderLucideIcons = render;
})();
