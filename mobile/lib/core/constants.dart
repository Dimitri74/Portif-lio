class ApiConstants {
  // Android Emulator: 10.0.2.2 aponta para o localhost do host
  // iOS Simulator: localhost
  // Dispositivo físico: IP da máquina na rede local (ex: 192.168.1.100)
  static const String _host = '10.0.2.2';

  // Portas reais dos microserviços (quarkus.http.port em cada application.properties)
  static const String baseUrlCatalogo   = 'http://$_host:8082'; // ms-catalogo
  static const String baseUrlPedidos    = 'http://$_host:8080'; // ms-pedidos
  static const String baseUrlPagamentos = 'http://$_host:8081'; // ms-pagamentos
  static const String baseUrlIa         = 'http://$_host:8083'; // ms-ia-suporte

  // UUID fixo para o cliente demo (backend exige UUID válido)
  static const String clienteDemoId = '00000000-0000-0000-0000-000000000001';

  // Timeouts
  static const Duration timeoutPadrao = Duration(seconds: 15);
  static const Duration timeoutIa     = Duration(seconds: 160); // LLM pode demorar
}