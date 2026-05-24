class RestauranteResumo {
  final String id;
  final String nome;
  final String categoria;
  final String status;
  final String cidade;
  final String? fotoUrl;

  const RestauranteResumo({
    required this.id,
    required this.nome,
    required this.categoria,
    required this.status,
    required this.cidade,
    this.fotoUrl,
  });

  factory RestauranteResumo.fromJson(Map<String, dynamic> j) => RestauranteResumo(
        id: j['id'] as String,
        nome: j['nome'] as String,
        categoria: j['categoria'] as String,
        status: j['status'] as String,
        cidade: j['cidade'] as String? ?? '',
        fotoUrl: j['fotoUrl'] as String?,
      );

  bool get aberto => status == 'ABERTO';
}

class CardapioResumo {
  final String id;
  final String nome;

  const CardapioResumo({required this.id, required this.nome});

  factory CardapioResumo.fromJson(Map<String, dynamic> j) =>
      CardapioResumo(id: j['id'] as String, nome: j['nome'] as String);
}