import 'package:dio/dio.dart';
import '../../../core/api/api_client.dart';
import '../models/pedido.dart';

class PedidoRepository {
  final Dio _dio = ApiClient.pedidos;

  Future<PedidoResponse> criar(CriarPedidoRequest request) async {
    final res = await _dio.post('/v1/pedidos', data: request.toJson());
    return PedidoResponse.fromJson(res.data);
  }

  Future<PedidoResponse> buscar(String id) async {
    final res = await _dio.get('/v1/pedidos/$id');
    return PedidoResponse.fromJson(res.data);
  }
}