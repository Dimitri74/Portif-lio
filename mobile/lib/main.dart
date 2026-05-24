import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:go_router/go_router.dart';

import 'features/carrinho/providers/carrinho_provider.dart';
import 'features/ia_chat/providers/chat_provider.dart';
import 'features/restaurantes/screens/restaurantes_screen.dart';
import 'features/cardapio/screens/cardapio_screen.dart';
import 'features/pedido/screens/checkout_screen.dart';
import 'features/pedido/screens/tracking_screen.dart';
import 'features/ia_chat/screens/chat_screen.dart';
import 'shared/theme/app_theme.dart';

final _router = GoRouter(
  initialLocation: '/',
  routes: [
    GoRoute(
      path: '/',
      builder: (_, __) => const RestaurantesScreen(),
    ),
    GoRoute(
      path: '/cardapio/:restauranteId',
      builder: (_, state) => CardapioScreen(
        restauranteId: state.pathParameters['restauranteId']!,
        nomeRestaurante: state.uri.queryParameters['nome'] ?? '',
        cardapioId: state.uri.queryParameters['cardapioId'] ?? '',
      ),
    ),
    GoRoute(
      path: '/checkout',
      builder: (_, __) => const CheckoutScreen(),
    ),
    GoRoute(
      path: '/tracking/:pedidoId',
      builder: (_, state) =>
          TrackingScreen(pedidoId: state.pathParameters['pedidoId']!),
    ),
    GoRoute(
      path: '/chat',
      builder: (_, __) => const ChatScreen(),
    ),
  ],
);

void main() {
  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => CarrinhoProvider()),
        ChangeNotifierProvider(create: (_) => ChatProvider()),
      ],
      child: const FlorindaEatsApp(),
    ),
  );
}

class FlorindaEatsApp extends StatelessWidget {
  const FlorindaEatsApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp.router(
      title: 'Florinda Eats',
      theme: AppTheme.light,
      routerConfig: _router,
      debugShowCheckedModeBanner: false,
    );
  }
}
