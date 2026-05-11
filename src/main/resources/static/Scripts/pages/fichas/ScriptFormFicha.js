(function initFormFichaScript() {
  initCalculoFCFooter();
  initAutoResizeTextareas();
  initStatusCriacao();
  initCalculoPerCapita();
  initPesoPorcao();
  initAguaCheckbox();
  initIngredienteAutocomplete();
  initCalculoCustoUsadoFooter();
  initCalculoFCC();
  initEventDelegationTabela();

  atualizarTotaisDaFicha();
  document.dispatchEvent(new Event("ingredienteAdicionado"));
})();
