import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import '../models/item_cardapio.dart';
import '../repositories/cardapio_repository.dart';
import '../../carrinho/models/carrinho.dart';
import '../../carrinho/providers/carrinho_provider.dart';
import '../../../shared/widgets/chat_fab.dart';
import '../../../shared/theme/app_theme.dart';

class CardapioScreen extends StatefulWidget {
  final String restauranteId;
  final String nomeRestaurante;
  final String cardapioId;

  const CardapioScreen({
    super.key,
    required this.restauranteId,
    required this.nomeRestaurante,
    required this.cardapioId,
  });

  @override
  State<CardapioScreen> createState() => _CardapioScreenState();
}

class _CardapioScreenState extends State<CardapioScreen> {
  final _repo = CardapioRepository();
  List<ItemCardapio> _itens = [];
  bool _loading = true;
  String? _erro;

  @override
  void initState() {
    super.initState();
    if (widget.cardapioId.isNotEmpty) {
      _carregar();
    } else {
      setState(() { _loading = false; });
    }
  }

  Future<void> _carregar() async {
    try {
      final itens = await _repo.listarItens(widget.cardapioId);
      setState(() { _itens = itens; _loading = false; });
    } catch (_) {
      setState(() { _erro = 'Não foi possível carregar o cardápio.'; _loading = false; });
    }
  }

  void _adicionarAoCarrinho(ItemCardapio item) {
    context.read<CarrinhoProvider>().adicionarItem(
      widget.restauranteId,
      widget.nomeRestaurante,
      ItemCarrinho(
        itemId: item.id,
        nomeItem: item.nome,
        precoUnitario: item.preco,
        fotoUrl: item.fotoUrl,
      ),
    );
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('${item.nome} adicionado ao carrinho!'),
        duration: const Duration(seconds: 1),
        backgroundColor: AppTheme.primary,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final carrinho = context.watch<CarrinhoProvider>();
    final fmt = NumberFormat.currency(locale: 'pt_BR', symbol: 'R\$');

    return Scaffold(
      appBar: AppBar(
        title: Text(widget.nomeRestaurante,
            style: const TextStyle(fontWeight: FontWeight.bold)),
        actions: [
          if (carrinho.temItens)
            Stack(
              alignment: Alignment.topRight,
              children: [
                IconButton(
                  icon: const Icon(Icons.shopping_cart),
                  onPressed: () => context.push('/checkout'),
                ),
                Positioned(
                  right: 8,
                  top: 8,
                  child: CircleAvatar(
                    radius: 8,
                    backgroundColor: AppTheme.secondary,
                    child: Text('${carrinho.totalItens}',
                        style: const TextStyle(fontSize: 10, color: Colors.white)),
                  ),
                ),
              ],
            ),
        ],
      ),
      floatingActionButton: const ChatFab(),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _erro != null
              ? Center(child: Text(_erro!, style: const TextStyle(color: Colors.grey)))
              : widget.cardapioId.isEmpty || _itens.isEmpty
                  ? const Center(
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Icon(Icons.restaurant_menu, size: 64, color: Colors.grey),
                          SizedBox(height: 8),
                          Text('Cardápio não disponível',
                              style: TextStyle(color: Colors.grey, fontWeight: FontWeight.bold)),
                        ],
                      ),
                    )
                  : ListView.separated(
                      padding: const EdgeInsets.all(16),
                      itemCount: _itens.length,
                      separatorBuilder: (_, __) => const SizedBox(height: 12),
                      itemBuilder: (_, i) {
                        final item = _itens[i];
                        return Card(
                          elevation: 1,
                          shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(16)),
                          child: Padding(
                            padding: const EdgeInsets.all(12),
                            child: Row(
                              children: [
                                ClipRRect(
                                  borderRadius: BorderRadius.circular(12),
                                  child: Container(
                                    width: 72,
                                    height: 72,
                                    color: AppTheme.primary.withOpacity(0.1),
                                    child: const Icon(Icons.fastfood,
                                        color: AppTheme.primary, size: 32),
                                  ),
                                ),
                                const SizedBox(width: 12),
                                Expanded(
                                  child: Column(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      Text(item.nome,
                                          style: const TextStyle(
                                              fontWeight: FontWeight.bold, fontSize: 15)),
                                      if (item.descricao != null) ...[
                                        const SizedBox(height: 2),
                                        Text(item.descricao!,
                                            style: const TextStyle(
                                                fontSize: 12, color: Colors.grey),
                                            maxLines: 2,
                                            overflow: TextOverflow.ellipsis),
                                      ],
                                      const SizedBox(height: 6),
                                      Text(fmt.format(item.preco),
                                          style: const TextStyle(
                                              color: AppTheme.primary,
                                              fontWeight: FontWeight.bold,
                                              fontSize: 14)),
                                    ],
                                  ),
                                ),
                                IconButton(
                                  onPressed: item.disponivel
                                      ? () => _adicionarAoCarrinho(item)
                                      : null,
                                  icon: const Icon(Icons.add_circle),
                                  color: AppTheme.primary,
                                  iconSize: 32,
                                ),
                              ],
                            ),
                          ),
                        );
                      },
                    ),
      bottomNavigationBar: carrinho.temItens
          ? SafeArea(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: ElevatedButton.icon(
                  onPressed: () => context.push('/checkout'),
                  icon: const Icon(Icons.shopping_cart_checkout),
                  label: Text(
                      'Ver carrinho · ${carrinho.totalItens} item(s) · ${fmt.format(carrinho.total)}'),
                ),
              ),
            )
          : null,
    );
  }
}
