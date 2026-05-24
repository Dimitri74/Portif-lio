import 'package:flutter/foundation.dart';
import 'package:dio/dio.dart';
import '../models/message.dart';
import '../repositories/ia_repository.dart';
import '../../../core/session_manager.dart';

class ChatProvider extends ChangeNotifier {
  final IaRepository _repo = IaRepository();

  final List<Message> messages = [Message.welcomeMessage()];
  bool isTyping = false;
  bool? iaOnline;

  String get sessaoId => SessionManager.instance.sessaoId;

  Future<void> checkHealth() async {
    iaOnline = null;
    notifyListeners();
    iaOnline = await _repo.isOnline();
    notifyListeners();
  }

  Future<void> sendMessage(String texto) async {
    final texto_ = texto.trim();
    if (texto_.isEmpty || isTyping) return;

    messages.add(Message(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      role: MessageRole.user,
      content: texto_,
      timestamp: DateTime.now(),
    ));
    isTyping = true;
    notifyListeners();

    try {
      final response = await _repo.chat(pergunta: texto_, sessaoId: sessaoId);
      messages.add(Message(
        id: (DateTime.now().millisecondsSinceEpoch + 1).toString(),
        role: MessageRole.assistant,
        content: response.resposta,
        timestamp: response.timestamp,
      ));
    } on DioException catch (e) {
      final isTimeout = e.type == DioExceptionType.receiveTimeout ||
          e.type == DioExceptionType.connectionTimeout;
      messages.add(Message(
        id: (DateTime.now().millisecondsSinceEpoch + 1).toString(),
        role: MessageRole.assistant,
        content: isTimeout
            ? 'O modelo está carregando. Isso pode levar alguns segundos na primeira mensagem. Tente novamente!'
            : 'Não foi possível conectar ao serviço de IA. Verifique se o ms-ia-suporte está rodando na porta 8083.',
        timestamp: DateTime.now(),
      ));
    } finally {
      isTyping = false;
      notifyListeners();
    }
  }
}
