import 'package:dio/dio.dart';
import '../../../core/api/api_client.dart';
import '../models/item_cardapio.dart';

class CardapioRepository {
  final Dio _dio = ApiClient.catalogo;

  Future<List<ItemCardapio>> listarItens(String cardapioId) async {
    final res = await _dio.get('/v1/cardapios/$cardapioId/itens');
    return (res.data as List).map((j) => ItemCardapio.fromJson(j)).toList();
  }
}