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

  // Modal Functions
  window.abrirModalEquipamentos = function() {
    const modal = document.getElementById('modal-equipamentos');
    const content = document.getElementById('modal-equipamentos-content');
    modal.classList.remove('hidden');
    setTimeout(() => {
      content.classList.remove('scale-95', 'opacity-0');
      content.classList.add('scale-100', 'opacity-100');
      if (window.lucide) window.lucide.createIcons();
    }, 10);
  };

  window.fecharModalEquipamentos = function() {
    const modal = document.getElementById('modal-equipamentos');
    const content = document.getElementById('modal-equipamentos-content');
    content.classList.remove('scale-100', 'opacity-100');
    content.classList.add('scale-95', 'opacity-0');
    setTimeout(() => {
      modal.classList.add('hidden');
    }, 300);
  };

  window.abrirModalPreparo = function() {
    const modal = document.getElementById('modal-preparo');
    const content = document.getElementById('modal-preparo-content');
    modal.classList.remove('hidden');
    setTimeout(() => {
      content.classList.remove('scale-95', 'opacity-0');
      content.classList.add('scale-100', 'opacity-100');
      if (window.lucide) window.lucide.createIcons();
    }, 10);
  };

  window.fecharModalPreparo = function() {
    const modal = document.getElementById('modal-preparo');
    const content = document.getElementById('modal-preparo-content');
    content.classList.remove('scale-100', 'opacity-100');
    content.classList.add('scale-95', 'opacity-0');
    setTimeout(() => {
      modal.classList.add('hidden');
    }, 300);
  };

  // Attach listeners to buttons
  const btnEquip = document.getElementById('btn-modal-equipamentos');
  if (btnEquip) {
    btnEquip.addEventListener('click', window.abrirModalEquipamentos);
  }

  const btnPreparo = document.getElementById('btn-modal-preparo');
  if (btnPreparo) {
    btnPreparo.addEventListener('click', window.abrirModalPreparo);
  }

  // Close on backdrop click
  window.onclick = function(event) {
    const modalEquip = document.getElementById('modal-equipamentos');
    const modalPreparo = document.getElementById('modal-preparo');
    if (event.target == modalEquip) fecharModalEquipamentos();
    if (event.target == modalPreparo) fecharModalPreparo();
  }
})();