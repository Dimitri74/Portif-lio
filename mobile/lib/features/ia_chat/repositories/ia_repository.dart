import 'package:dio/dio.dart';
import '../../../core/api/api_client.dart';
import '../models/message.dart';

class IaRepository {
  final Dio _dio = ApiClient.ia;

  Future<ChatResponse> chat({
    required String pergunta,
    required String sessaoId,
  }) async {
    final res = await _dio.post('/v1/ia/chat', data: {
      'pergunta': pergunta.substring(0, pergunta.length.clamp(0, 2000)),
      'sessaoId': sessaoId,
    });
    return ChatResponse.fromJson(res.data);
  }

  Future<bool> isOnline() async {
    try {
      await _dio.get('/v1/ia/health');
      return true;
    } catch (_) {
      return false;
    }
  }
}