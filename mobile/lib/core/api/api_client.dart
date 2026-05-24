import 'package:dio/dio.dart';
import '../constants.dart';

class ApiClient {
  static Dio _build(String baseUrl, {Duration? timeout}) {
    final t = timeout ?? ApiConstants.timeoutPadrao;
    return Dio(
      BaseOptions(
        baseUrl: baseUrl,
        connectTimeout: t,
        receiveTimeout: t,
        headers: {'Content-Type': 'application/json'},
      ),
    );
  }

  static final Dio catalogo   = _build(ApiConstants.baseUrlCatalogo);
  static final Dio pedidos    = _build(ApiConstants.baseUrlPedidos);
  static final Dio pagamentos = _build(ApiConstants.baseUrlPagamentos);
  static final Dio ia         = _build(ApiConstants.baseUrlIa, timeout: ApiConstants.timeoutIa);
}