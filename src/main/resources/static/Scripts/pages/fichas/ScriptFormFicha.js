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
  if (typeof formatarInputsDecimais === "function") {
    formatarInputsDecimais();
  }

  atualizarTotaisDaFicha();
  document.dispatchEvent(new Event("ingredienteAdicionado"));
})();
