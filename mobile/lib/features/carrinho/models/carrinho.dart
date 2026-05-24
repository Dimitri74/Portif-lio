class ItemCarrinho {
  final String itemId;
  final String nomeItem;
  final double precoUnitario;
  final String? fotoUrl;
  int quantidade;

  ItemCarrinho({
    required this.itemId,
    required this.nomeItem,
    required this.precoUnitario,
    this.fotoUrl,
    this.quantidade = 1,
  });

  double get subtotal => precoUnitario * quantidade;
}

class Carrinho {
  final String restauranteId;
  final String nomeRestaurante;
  final List<ItemCarrinho> itens;

  Carrinho({
    required this.restauranteId,
    required this.nomeRestaurante,
    required this.itens,
  });

  double get total => itens.fold(0, (sum, i) => sum + i.subtotal);
  int get totalItens => itens.fold(0, (sum, i) => sum + i.quantidade);
}