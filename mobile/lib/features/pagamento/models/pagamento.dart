class ProcessarPagamentoRequest {
  final String pedidoId;
  final String clienteId;
  final String metodo;
  final double valor;

  const ProcessarPagamentoRequest({
    required this.pedidoId,
    required this.clienteId,
    required this.metodo,
    required this.valor,
  });

  Map<String, dynamic> toJson() => {
        'pedidoId': pedidoId,
        'clienteId': clienteId,
        'metodo': metodo,
        'valor': valor,
      };
}

class PagamentoResponse {
  final String id;
  final String status;
  final String metodo;
  final double valor;

  const PagamentoResponse({
    required this.id,
    required this.status,
    required this.metodo,
    required this.valor,
  });

  factory PagamentoResponse.fromJson(Map<String, dynamic> j) => PagamentoResponse(
        id: j['id'] as String,
        status: j['status'] as String,
        metodo: j['metodo'] as String,
        valor: (j['valor'] as num).toDouble(),
      );
}
