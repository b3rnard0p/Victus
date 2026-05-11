(function initDetailScript() {
  const currentPath = window.location.pathname;

  const navItems = document.querySelectorAll('nav a');
  navItems.forEach(item => item.classList.remove('active'));

  const fichasLink = document.querySelector('a[href="/ficha"]');
  const isFichaListaOuDetalhe = currentPath === '/ficha' || /^\/ficha\/\d+$/.test(currentPath);

  if (fichasLink && isFichaListaOuDetalhe) {
    fichasLink.classList.add('active');
  }

  navItems.forEach(item => {
    const href = item.getAttribute('href');
    if (href && href === currentPath) {
      item.classList.add('active');
    }
  });

  const menuToggle = document.getElementById('menu-toggle');
  const menuClose = document.getElementById('menu-close');
  const mobileMenu = document.getElementById('mobile-menu');

  if (menuToggle && mobileMenu) {
    const newToggle = menuToggle.cloneNode(true);
    menuToggle.parentNode.replaceChild(newToggle, menuToggle);

    newToggle.addEventListener('click', () => mobileMenu.classList.toggle('hidden'));
  }

  if (menuClose && mobileMenu) {
    const newClose = menuClose.cloneNode(true);
    menuClose.parentNode.replaceChild(newClose, menuClose);

    newClose.addEventListener('click', () => mobileMenu.classList.add('hidden'));
  }
})();