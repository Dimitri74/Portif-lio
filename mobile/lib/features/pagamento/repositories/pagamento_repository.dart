import 'package:dio/dio.dart';
import '../../../core/api/api_client.dart';
import '../models/pagamento.dart';

class PagamentoRepository {
  final Dio _dio = ApiClient.pagamentos;

  Future<PagamentoResponse> processar(ProcessarPagamentoRequest request) async {
    final res = await _dio.post('/v1/pagamentos', data: request.toJson());
    return PagamentoResponse.fromJson(res.data);
  }
}