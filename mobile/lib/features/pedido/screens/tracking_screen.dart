import 'dart:async';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import '../models/pedido.dart';
import '../repositories/pedido_repository.dart';
import '../../../shared/theme/app_theme.dart';

class TrackingScreen extends StatefulWidget {
  final String pedidoId;
  const TrackingScreen({super.key, required this.pedidoId});

  @override
  State<TrackingScreen> createState() => _TrackingScreenState();
}

class _TrackingScreenState extends State<TrackingScreen> {
  final _repo = PedidoRepository();
  PedidoResponse? _pedido;
  Timer? _timer;

  static const _statusOrdem = [
    'PENDENTE', 'CONFIRMADO', 'PREPARANDO', 'SAIU_PARA_ENTREGA', 'ENTREGUE',
  ];

  static const _statusLabel = {
    'PENDENTE': 'Pedido recebido',
    'CONFIRMADO': 'Confirmado',
    'PREPARANDO': 'Em preparo',
    'SAIU_PARA_ENTREGA': 'Saiu para entrega',
    'ENTREGUE': 'Entregue! 🎉',
    'CANCELADO': 'Cancelado',
  };

  static const _statusIcon = {
    'PENDENTE': Icons.hourglass_empty,
    'CONFIRMADO': Icons.check_circle_outline,
    'PREPARANDO': Icons.soup_kitchen,
    'SAIU_PARA_ENTREGA': Icons.delivery_dining,
    'ENTREGUE': Icons.celebration,
    'CANCELADO': Icons.cancel,
  };

  @override
  void initState() {
    super.initState();
    _buscar();
    _timer = Timer.periodic(const Duration(seconds: 5), (_) => _buscar());
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }

  Future<void> _buscar() async {
    try {
      final p = await _repo.buscar(widget.pedidoId);
      if (mounted) setState(() => _pedido = p);
      if (p.status == 'ENTREGUE' || p.status == 'CANCELADO') {
        _timer?.cancel();
      }
    } catch (_) {}
  }

  int get _statusIdx {
    if (_pedido == null) return 0;
    return _statusOrdem.indexOf(_pedido!.status).clamp(0, _statusOrdem.length - 1);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Acompanhar Pedido',
            style: TextStyle(fontWeight: FontWeight.bold)),
        leading: IconButton(
          icon: const Icon(Icons.home),
          onPressed: () => context.go('/'),
        ),
      ),
      body: _pedido == null
          ? const Center(child: CircularProgressIndicator())
          : Padding(
              padding: const EdgeInsets.all(24),
              child: Column(
                children: [
                  // Status atual
                  Container(
                    width: double.infinity,
                    padding: const EdgeInsets.all(20),
                    decoration: BoxDecoration(
                      gradient: const LinearGradient(
                          colors: [AppTheme.primary, AppTheme.secondary]),
                      borderRadius: BorderRadius.circular(20),
                    ),
                    child: Column(
                      children: [
                        Icon(
                          _statusIcon[_pedido!.status] ?? Icons.info,
                          color: Colors.white, size: 48,
                        ),
                        const SizedBox(height: 8),
                        Text(
                          _statusLabel[_pedido!.status] ?? _pedido!.status,
                          style: const TextStyle(
                              color: Colors.white,
                              fontSize: 20,
                              fontWeight: FontWeight.bold),
                        ),
                        const SizedBox(height: 4),
                        Text('Pedido #${widget.pedidoId.substring(0, 8)}...',
                            style: const TextStyle(color: Colors.white70, fontSize: 12)),
                      ],
                    ),
                  ),
                  const SizedBox(height: 32),
                  // Stepper de status
                  if (_pedido!.status != 'CANCELADO')
                    Column(
                      children: List.generate(_statusOrdem.length, (i) {
                        final concluido = i <= _statusIdx;
                        final atual = i == _statusIdx;
                        return Row(
                          children: [
                            Column(
                              children: [
                                AnimatedContainer(
                                  duration: const Duration(milliseconds: 300),
                                  width: 32, height: 32,
                                  decoration: BoxDecoration(
                                    shape: BoxShape.circle,
                                    color: concluido
                                        ? AppTheme.primary
                                        : Colors.grey.shade200,
                                  ),
                                  child: Icon(
                                    concluido ? Icons.check : Icons.circle_outlined,
                                    size: 16,
                                    color: concluido ? Colors.white : Colors.grey,
                                  ),
                                ),
                                if (i < _statusOrdem.length - 1)
                                  Container(
                                    width: 2, height: 28,
                                    color: i < _statusIdx
                                        ? AppTheme.primary
                                        : Colors.grey.shade200,
                                  ),
                              ],
                            ),
                            const SizedBox(width: 16),
                            Text(
                              _statusLabel[_statusOrdem[i]] ?? _statusOrdem[i],
                              style: TextStyle(
                                fontWeight: atual ? FontWeight.bold : FontWeight.normal,
                                color: concluido ? Colors.black87 : Colors.grey,
                              ),
                            ),
                          ],
                        );
                      }),
                    ),
                  const Spacer(),
                  if (_pedido!.status == 'ENTREGUE' || _pedido!.status == 'CANCELADO')
                    ElevatedButton.icon(
                      onPressed: () => context.go('/'),
                      icon: const Icon(Icons.home),
                      label: const Text('Voltar ao início'),
                      style: ElevatedButton.styleFrom(
                          minimumSize: const Size.fromHeight(52)),
                    )
                  else
                    const Text('Atualizando automaticamente...',
                        style: TextStyle(color: Colors.grey, fontSize: 12)),
                ],
              ),
            ),
    );
  }
}
