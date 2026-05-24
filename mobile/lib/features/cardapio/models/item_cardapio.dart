class ItemCardapio {
  final String id;
  final String nome;
  final String? descricao;
  final double preco;
  final String? fotoUrl;
  final bool disponivel;

  const ItemCardapio({
    required this.id,
    required this.nome,
    this.descricao,
    required this.preco,
    this.fotoUrl,
    required this.disponivel,
  });

  factory ItemCardapio.fromJson(Map<String, dynamic> j) => ItemCardapio(
        id: j['id'] as String,
        nome: j['nome'] as String,
        descricao: j['descricao'] as String?,
        preco: (j['preco'] as num).toDouble(),
        fotoUrl: j['fotoUrl'] as String?,
        disponivel: j['disponivel'] as bool? ?? true,
      );
}