import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import '../models/restaurante.dart';
import '../repositories/restaurantes_repository.dart';
import '../../../shared/widgets/chat_fab.dart';
import '../../../shared/theme/app_theme.dart';

class RestaurantesScreen extends StatefulWidget {
  const RestaurantesScreen({super.key});

  @override
  State<RestaurantesScreen> createState() => _RestaurantesScreenState();
}

class _RestaurantesScreenState extends State<RestaurantesScreen> {
  final _repo = RestaurantesRepository();
  List<RestauranteResumo> _restaurantes = [];
  bool _loading = true;
  String _search = '';
  String? _erro;

  @override
  void initState() {
    super.initState();
    _carregar();
  }

  Future<void> _carregar() async {
    try {
      final lista = await _repo.listar();
      setState(() { _restaurantes = lista; _loading = false; });
    } catch (_) {
      setState(() { _erro = 'Não foi possível carregar os restaurantes.'; _loading = false; });
    }
  }

  List<RestauranteResumo> get _filtrados => _restaurantes.where((r) {
        final q = _search.toLowerCase();
        return r.nome.toLowerCase().contains(q) ||
            r.categoria.toLowerCase().contains(q);
      }).toList();

  Future<void> _selecionarRestaurante(RestauranteResumo r) async {
    try {
      final cardapios = await _repo.cardapios(r.id);
      final cardapioId = cardapios.isNotEmpty ? cardapios.first.id : '';
      if (!mounted) return;
      context.push(
        '/cardapio/${r.id}?nome=${Uri.encodeComponent(r.nome)}&cardapioId=$cardapioId',
      );
    } catch (_) {
      if (!mounted) return;
      context.push('/cardapio/${r.id}?nome=${Uri.encodeComponent(r.nome)}&cardapioId=');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Florinda Eats', style: TextStyle(fontWeight: FontWeight.bold)),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () { setState(() { _loading = true; _erro = null; }); _carregar(); },
          ),
        ],
      ),
      floatingActionButton: const ChatFab(),
      body: Column(
        children: [
          // Hero banner
          Container(
            width: double.infinity,
            decoration: const BoxDecoration(
              gradient: LinearGradient(
                colors: [AppTheme.primary, AppTheme.secondary],
              ),
            ),
            padding: const EdgeInsets.fromLTRB(16, 16, 16, 24),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text('O que você quer comer hoje? 🍽️',
                    style: TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.bold)),
                const SizedBox(height: 4),
                const Text('Escolha um restaurante e monte seu pedido',
                    style: TextStyle(color: Colors.white70, fontSize: 13)),
                const SizedBox(height: 12),
                TextField(
                  onChanged: (v) => setState(() => _search = v),
                  decoration: InputDecoration(
                    hintText: 'Buscar restaurante ou categoria...',
                    prefixIcon: const Icon(Icons.search),
                    filled: true,
                    fillColor: Colors.white,
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(12),
                      borderSide: BorderSide.none,
                    ),
                    contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                  ),
                ),
              ],
            ),
          ),
          // Lista
          Expanded(
            child: _loading
                ? const Center(child: CircularProgressIndicator())
                : _erro != null
                    ? Center(child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          const Icon(Icons.error_outline, size: 48, color: Colors.grey),
                          const SizedBox(height: 8),
                          Text(_erro!, style: const TextStyle(color: Colors.grey)),
                          const SizedBox(height: 12),
                          ElevatedButton(onPressed: _carregar, child: const Text('Tentar novamente')),
                        ],
                      ))
                    : _filtrados.isEmpty
                        ? const Center(child: Text('Nenhum restaurante encontrado'))
                        : RefreshIndicator(
                            onRefresh: _carregar,
                            child: GridView.builder(
                              padding: const EdgeInsets.all(16),
                              gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                                crossAxisCount: 2,
                                childAspectRatio: 0.85,
                                crossAxisSpacing: 12,
                                mainAxisSpacing: 12,
                              ),
                              itemCount: _filtrados.length,
                              itemBuilder: (_, i) => _RestauranteCard(
                                restaurante: _filtrados[i],
                                onTap: () => _selecionarRestaurante(_filtrados[i]),
                              ),
                            ),
                          ),
          ),
        ],
      ),
    );
  }
}

class _RestauranteCard extends StatelessWidget {
  final RestauranteResumo restaurante;
  final VoidCallback onTap;

  const _RestauranteCard({required this.restaurante, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: restaurante.aberto ? onTap : null,
      child: Card(
        elevation: 2,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            ClipRRect(
              borderRadius: const BorderRadius.vertical(top: Radius.circular(16)),
              child: Container(
                height: 90,
                width: double.infinity,
                color: AppTheme.primary.withOpacity(0.1),
                child: const Icon(Icons.restaurant, size: 40, color: AppTheme.primary),
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(10),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(restaurante.nome,
                      style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis),
                  const SizedBox(height: 2),
                  Text(restaurante.categoria,
                      style: const TextStyle(fontSize: 11, color: Colors.grey)),
                  const SizedBox(height: 6),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                    decoration: BoxDecoration(
                      color: restaurante.aberto
                          ? Colors.green.shade50
                          : Colors.red.shade50,
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Text(
                      restaurante.aberto ? 'Aberto' : 'Fechado',
                      style: TextStyle(
                        fontSize: 10,
                        fontWeight: FontWeight.bold,
                        color: restaurante.aberto ? Colors.green : Colors.red,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
