import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import '../../carrinho/providers/carrinho_provider.dart';
import '../../pedido/models/pedido.dart';
import '../../pedido/repositories/pedido_repository.dart';
import '../../../core/constants.dart';
import '../../../shared/theme/app_theme.dart';

class CheckoutScreen extends StatefulWidget {
  const CheckoutScreen({super.key});

  @override
  State<CheckoutScreen> createState() => _CheckoutScreenState();
}

class _CheckoutScreenState extends State<CheckoutScreen> {
  final _formKey = GlobalKey<FormState>();
  final _enderecoCtrl = TextEditingController();
  final _clienteCtrl = TextEditingController(text: ApiConstants.clienteDemoId);
  String _metodoPagamento = 'PIX';
  bool _loading = false;

  final _pedidoRepo = PedidoRepository();

  final _metodos = ['PIX', 'CARTAO_CREDITO', 'CARTAO_DEBITO', 'VALE_REFEICAO'];

  @override
  void dispose() {
    _enderecoCtrl.dispose();
    _clienteCtrl.dispose();
    super.dispose();
  }

  Future<void> _confirmar() async {
    if (!_formKey.currentState!.validate()) return;
    final carrinho = context.read<CarrinhoProvider>().carrinho;
    if (carrinho == null) return;

    setState(() => _loading = true);
    try {
      final pedido = await _pedidoRepo.criar(CriarPedidoRequest(
        clienteId: _clienteCtrl.text.trim(),
        restauranteId: carrinho.restauranteId,
        enderecoEntrega: _enderecoCtrl.text.trim(),
        itens: carrinho.itens
            .map((i) => ItemPedidoRequest(
                  itemId: i.itemId,
                  nomeItem: i.nomeItem,
                  precoUnitario: i.precoUnitario,
                  quantidade: i.quantidade,
                ))
            .toList(),
      ));

      // Pagamento é processado automaticamente pelo ms-pagamentos
      // via evento Kafka (order.created) — não chamar REST diretamente.

      if (!mounted) return;
      context.read<CarrinhoProvider>().limpar();
      context.go('/tracking/${pedido.id}');
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Erro: $e'), backgroundColor: Colors.red),
      );
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final carrinho = context.watch<CarrinhoProvider>().carrinho;
    final fmt = NumberFormat.currency(locale: 'pt_BR', symbol: 'R\$');

    if (carrinho == null) {
      return Scaffold(
        appBar: AppBar(title: const Text('Finalizar Pedido')),
        body: const Center(child: Text('Carrinho vazio')),
      );
    }

    return Scaffold(
      appBar: AppBar(title: const Text('Finalizar Pedido',
          style: TextStyle(fontWeight: FontWeight.bold))),
      body: Form(
        key: _formKey,
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            // Resumo do pedido
            Card(
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(carrinho.nomeRestaurante,
                        style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                    const Divider(height: 20),
                    ...carrinho.itens.map((i) => Padding(
                          padding: const EdgeInsets.symmetric(vertical: 4),
                          child: Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Text('${i.quantidade}x ${i.nomeItem}'),
                              Text(fmt.format(i.subtotal),
                                  style: const TextStyle(fontWeight: FontWeight.bold)),
                            ],
                          ),
                        )),
                    const Divider(height: 20),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Text('Total', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                        Text(fmt.format(carrinho.total),
                            style: const TextStyle(
                                fontWeight: FontWeight.bold,
                                fontSize: 16,
                                color: AppTheme.primary)),
                      ],
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 16),
            // Endereço
            TextFormField(
              controller: _enderecoCtrl,
              decoration: InputDecoration(
                labelText: 'Endereço de entrega',
                prefixIcon: const Icon(Icons.location_on),
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
              ),
              validator: (v) =>
                  v == null || v.trim().isEmpty ? 'Informe o endereço' : null,
            ),
            const SizedBox(height: 12),
            // Método de pagamento
            DropdownButtonFormField<String>(
              value: _metodoPagamento,
              decoration: InputDecoration(
                labelText: 'Forma de pagamento',
                prefixIcon: const Icon(Icons.payment),
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
              ),
              items: _metodos
                  .map((m) => DropdownMenuItem(value: m, child: Text(m.replaceAll('_', ' '))))
                  .toList(),
              onChanged: (v) => setState(() => _metodoPagamento = v!),
            ),
            const SizedBox(height: 24),
            ElevatedButton.icon(
              onPressed: _loading ? null : _confirmar,
              icon: _loading
                  ? const SizedBox(
                      width: 18, height: 18,
                      child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                  : const Icon(Icons.check_circle),
              label: Text(_loading ? 'Processando...' : 'Confirmar Pedido'),
              style: ElevatedButton.styleFrom(
                  minimumSize: const Size.fromHeight(52)),
            ),
          ],
        ),
      ),
    );
  }
}
