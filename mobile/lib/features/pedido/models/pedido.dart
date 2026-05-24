class ItemPedidoRequest {
  final String itemId;
  final String nomeItem;
  final double precoUnitario;
  final int quantidade;

  const ItemPedidoRequest({
    required this.itemId,
    required this.nomeItem,
    required this.precoUnitario,
    required this.quantidade,
  });

  Map<String, dynamic> toJson() => {
        'itemId': itemId,
        'nomeItem': nomeItem,
        'precoUnitario': precoUnitario,
        'quantidade': quantidade,
      };
}

class CriarPedidoRequest {
  final String clienteId;
  final String restauranteId;
  final String enderecoEntrega;
  final String? observacao;
  final List<ItemPedidoRequest> itens;

  const CriarPedidoRequest({
    required this.clienteId,
    required this.restauranteId,
    required this.enderecoEntrega,
    this.observacao,
    required this.itens,
  });

  Map<String, dynamic> toJson() => {
        'clienteId': clienteId,
        'restauranteId': restauranteId,
        'enderecoEntrega': enderecoEntrega,
        if (observacao != null) 'observacao': observacao,
        'itens': itens.map((i) => i.toJson()).toList(),
      };
}

class PedidoResponse {
  final String id;
  final String status;
  final double valorTotal;
  final String? observacao;
  final String enderecoEntrega;
  final DateTime criadoEm;

  const PedidoResponse({
    required this.id,
    required this.status,
    required this.valorTotal,
    this.observacao,
    required this.enderecoEntrega,
    required this.criadoEm,
  });

  factory PedidoResponse.fromJson(Map<String, dynamic> j) => PedidoResponse(
        id: j['id'] as String,
        status: j['status'] as String,
        valorTotal: (j['valorTotal'] as num).toDouble(),
        observacao: j['observacao'] as String?,
        enderecoEntrega: j['enderecoEntrega'] as String,
        criadoEm: DateTime.parse(j['criadoEm'] as String),
      );
}