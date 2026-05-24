import 'package:dio/dio.dart';
import '../../../core/api/api_client.dart';
import '../models/restaurante.dart';

class RestaurantesRepository {
  final Dio _dio = ApiClient.catalogo;

  Future<List<RestauranteResumo>> listar() async {
    final res = await _dio.get('/v1/restaurantes');
    return (res.data as List).map((j) => RestauranteResumo.fromJson(j)).toList();
  }

  Future<List<CardapioResumo>> cardapios(String restauranteId) async {
    final res = await _dio.get('/v1/restaurantes/$restauranteId/cardapios');
    return (res.data as List).map((j) => CardapioResumo.fromJson(j)).toList();
  }
}