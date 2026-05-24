enum MessageRole { user, assistant }

class Message {
  final String id;
  final MessageRole role;
  final String content;
  final DateTime timestamp;
  final bool isLoading;

  const Message({
    required this.id,
    required this.role,
    required this.content,
    required this.timestamp,
    this.isLoading = false,
  });

  static Message welcomeMessage() => Message(
        id: 'welcome',
        role: MessageRole.assistant,
        content:
            'Olá! Sou a Florinda, sua assistente virtual! 🍽️ Como posso ajudar você hoje? Posso consultar o status de pedidos, tirar dúvidas sobre o cardápio e muito mais!',
        timestamp: DateTime.now(),
      );
}

class ChatResponse {
  final String resposta;
  final String? sessaoId;
  final List<dynamic> fontes;
  final bool guardrailAtivado;
  final DateTime timestamp;

  const ChatResponse({
    required this.resposta,
    this.sessaoId,
    required this.fontes,
    required this.guardrailAtivado,
    required this.timestamp,
  });

  factory ChatResponse.fromJson(Map<String, dynamic> j) => ChatResponse(
        resposta: j['resposta'] as String,
        sessaoId: j['sessaoId'] as String?,
        fontes: j['fontes'] as List<dynamic>? ?? [],
        guardrailAtivado: j['guardrailAtivado'] as bool? ?? false,
        timestamp: j['timestamp'] != null
            ? DateTime.parse(j['timestamp'] as String)
            : DateTime.now(),
      );
}