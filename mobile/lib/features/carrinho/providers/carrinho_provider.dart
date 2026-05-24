import 'package:flutter/foundation.dart';
import '../models/carrinho.dart';

class CarrinhoProvider extends ChangeNotifier {
  Carrinho? _carrinho;

  Carrinho? get carrinho => _carrinho;
  bool get temItens => _carrinho != null && _carrinho!.itens.isNotEmpty;
  double get total => _carrinho?.total ?? 0;
  int get totalItens => _carrinho?.totalItens ?? 0;

  void adicionarItem(
    String restauranteId,
    String nomeRestaurante,
    ItemCarrinho item,
  ) {
    if (_carrinho != null && _carrinho!.restauranteId != restauranteId) {
      _carrinho = null; // limpa carrinho de outro restaurante
    }

    if (_carrinho == null) {
      _carrinho = Carrinho(
        restauranteId: restauranteId,
        nomeRestaurante: nomeRestaurante,
        itens: [],
      );
    }

    final existente = _carrinho!.itens
        .where((i) => i.itemId == item.itemId)
        .firstOrNull;

    if (existente != null) {
      existente.quantidade++;
    } else {
      _carrinho!.itens.add(item);
    }
    notifyListeners();
  }

  void atualizarQuantidade(String itemId, int quantidade) {
    if (_carrinho == null) return;
    if (quantidade <= 0) {
      removerItem(itemId);
      return;
    }
    final item = _carrinho!.itens.where((i) => i.itemId == itemId).firstOrNull;
    if (item != null) {
      item.quantidade = quantidade;
      notifyListeners();
    }
  }

  void removerItem(String itemId) {
    _carrinho?.itens.removeWhere((i) => i.itemId == itemId);
    if (_carrinho?.itens.isEmpty ?? false) _carrinho = null;
    notifyListeners();
  }

  void limpar() {
    _carrinho = null;
    notifyListeners();
  }
}
