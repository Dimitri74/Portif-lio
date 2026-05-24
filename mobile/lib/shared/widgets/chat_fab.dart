import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import '../../shared/theme/app_theme.dart';

class ChatFab extends StatelessWidget {
  const ChatFab({super.key});

  @override
  Widget build(BuildContext context) {
    return FloatingActionButton(
      onPressed: () => context.push('/chat'),
      backgroundColor: AppTheme.primary,
      tooltip: 'Falar com Florinda IA',
      child: const Icon(Icons.chat_bubble_outline, color: Colors.white),
    );
  }
}