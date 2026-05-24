
# Quickstart: Florinda Eats Mobile

App mobile do Florinda Eats em Flutter, integrado aos microserviços Quarkus do backend.

---

## Arquitetura de Integração

```
┌──────────────────────────────────────────────────────────────┐
│                    Flutter Mobile App                        │
│                                                              │
│  RestaurantesScreen ──► CardapioScreen ──► CheckoutScreen   │
│         │                    │                   │           │
│  GET /v1/restaurantes  GET /v1/cardapios   POST /v1/pedidos  │
│  ms-catalogo:8082      /itens              ms-pedidos:8080   │
│                        ms-catalogo:8082         │            │
│                                          [Kafka: order.created]
│                                          ms-pagamentos:8081  │
│                                                              │
│  TrackingScreen ─── GET /v1/pedidos/{id} (polling 5s)       │
│  ms-pedidos:8080                                             │
│                                                              │
│  ChatScreen (IA) ──── POST /v1/ia/chat ── ms-ia-suporte:8083│
└──────────────────────────────────────────────────────────────┘
```

### Microserviços e Portas

| Serviço          | Porta | Endpoints usados pelo mobile              |
|------------------|-------|-------------------------------------------|
| ms-catalogo      | 8082  | `GET /v1/restaurantes`                    |
|                  |       | `GET /v1/restaurantes/{id}/cardapios`     |
|                  |       | `GET /v1/cardapios/{id}/itens`            |
| ms-pedidos       | 8080  | `POST /v1/pedidos`                        |
|                  |       | `GET /v1/pedidos/{id}`                    |
| ms-pagamentos    | 8081  | Processado via Kafka (automático)         |
| ms-ia-suporte    | 8083  | `POST /v1/ia/chat`, `GET /v1/ia/health`  |

Configurado em `lib/core/constants.dart`.

---

## Pré-requisitos

1. **Flutter SDK** `>= 3.12.0` — verifique com `flutter --version`
2. **Android Studio** com Android SDK e AVD Manager configurados
3. **Backend rodando** — os microserviços Quarkus devem estar ativos

---

## Comandos do Emulador

### Listar e iniciar emuladores

```bash
# Ver emuladores disponíveis instalados no AVD Manager
flutter emulators

# Iniciar um emulador pelo ID
flutter emulators --launch Pixel8_API35

# Criar novo emulador (abre AVD Manager)
flutter emulators --create --name MeuEmulador

# Listar dispositivos conectados (físicos + emuladores ativos)
flutter devices

# Aguardar emulador ficar online e listar
flutter devices --device-timeout 60
```

### Rodar o app no emulador

```bash
# Rodar no emulador padrão (único conectado)
flutter run

# Rodar especificando o emulador pelo ID
flutter run -d emulator-5554

# Rodar com hot reload habilitado explicitamente
flutter run -d emulator-5554 --hot

# Rodar em modo release (mais próximo da produção, sem debug)
flutter run -d emulator-5554 --release

# Rodar em modo profile (para análise de performance)
flutter run -d emulator-5554 --profile
```

### Comandos interativos durante `flutter run`

| Tecla | Ação |
|-------|------|
| `r` | Hot reload (recarrega Dart sem reiniciar o app) |
| `R` | Hot restart (reinicia o app, mantém o processo) |
| `d` | Detach (sai do terminal, app continua rodando) |
| `q` | Quit (encerra o app e o processo) |
| `h` | Listar todos os comandos disponíveis |
| `c` | Limpar o terminal |

### ADB — comandos úteis com o emulador

```bash
# Verificar dispositivos reconhecidos pelo ADB
adb devices

# Reiniciar servidor ADB (resolve "device not found")
adb kill-server && adb start-server

# Instalar APK manualmente no emulador
adb -s emulator-5554 install build/app/outputs/flutter-apk/app-debug.apk

# Ver logs do app em tempo real (filtrado pelo pacote)
adb -s emulator-5554 logcat | grep flutter

# Capturar screenshot do emulador
adb -s emulator-5554 exec-out screencap -p > screenshot.png

# Abrir shell dentro do emulador
adb -s emulator-5554 shell
```

### Build do APK

```bash
# Build APK debug (para testes locais)
flutter build apk --debug

# Build APK release (otimizado, requer keystore configurada)
flutter build apk --release

# Build separado por ABI (menor tamanho)
flutter build apk --split-per-abi

# APK gerado em:
# build/app/outputs/flutter-apk/app-debug.apk
# build/app/outputs/flutter-apk/app-release.apk
```

---

## Subindo o Backend (necessário para funcionar)

Cada microserviço é um projeto Quarkus. No diretório `backend/`, execute em Dev Mode:

```bash
# Infraestrutura Docker (PostgreSQL, MySQL, Kafka, Redis)
.\dev-up.ps1         # Windows PowerShell
# ou
docker compose up -d

# Microserviços — cada um em um terminal
cd backend
mvn -pl ms-catalogo quarkus:dev       # :8082
mvn -pl ms-pedidos quarkus:dev        # :8080
mvn -pl ms-pagamentos quarkus:dev     # :8081 (opcional se Kafka ativo)
mvn -pl ms-ia-suporte quarkus:dev     # :8083 (opcional, requer Ollama)
```

---

## Subindo o App Mobile

### 1. Instalar dependências

```bash
cd mobile/
flutter pub get
```

### 2. Iniciar o emulador

```bash
# Listar emuladores disponíveis
flutter emulators

# Iniciar (substitua pelo ID do seu emulador)
flutter emulators --launch Pixel8_API35

# Aguardar aparecer como online
flutter devices
```

### 3. Executar

```bash
# Android Emulator (host 10.0.2.2 já configurado)
flutter run -d emulator-5554

# Web — antes altere _host para 'localhost' em constants.dart
flutter run -d chrome

# Windows Desktop
flutter run -d windows
```

### 4. Rodar testes

```bash
flutter test
```

---

## Configuração de Host por Plataforma

Edite `lib/core/constants.dart`:

```dart
// Android Emulator → 10.0.2.2 aponta para o localhost da máquina host
static const String _host = '10.0.2.2';

// iOS Simulator ou Web → use localhost
static const String _host = 'localhost';

// Dispositivo físico na mesma rede Wi-Fi → IP da sua máquina
static const String _host = '192.168.x.x';
```

---

## Fluxo de Uso

1. **Tela inicial** — lista restaurantes (`GET /v1/restaurantes`)
2. **Cardápio** — busca cardápios e itens (`GET /v1/restaurantes/{id}/cardapios`)
3. **Carrinho** — adiciona itens, gerenciado localmente via `CarrinhoProvider`
4. **Checkout** — cria pedido (`POST /v1/pedidos`); pagamento é processado automaticamente via evento Kafka pelo `ms-pagamentos`
5. **Tracking** — polling a cada 5s no status do pedido (`GET /v1/pedidos/{id}`)
6. **Chat IA** — assistente Florinda via `POST /v1/ia/chat` (requer Ollama + PgVector)

---

## Problemas Comuns

| Problema | Causa | Solução |
|---|---|---|
| `adb.exe: device not found` | Emulador fechou durante build | Reiniciar emulador e rodar `flutter run` novamente |
| "Não foi possível carregar os restaurantes" | `ms-catalogo:8082` parado ou host errado | Verificar se o backend está rodando |
| Erro 400 ao criar pedido | `clienteId` inválido ou campos faltando | Conferir logs do `ms-pedidos` |
| Erro 422 no pagamento | Pagamento duplicado (Kafka já processou) | Esperado — o app não chama mais o endpoint diretamente |
| Chat IA não responde | Ollama não está rodando | `ollama serve` + `ollama pull llama3.2` |
| CORS error no browser | Backend sem CORS configurado | `quarkus.http.cors=true` no `application.properties` |
| Gradle demorando 300s+ | Primeira build sem cache | Normal na primeira vez; builds seguintes usam cache (~30s) |

---

## Estrutura do Projeto

```
mobile/
├── lib/
│   ├── core/
│   │   ├── constants.dart          # URLs e portas dos microserviços
│   │   ├── api/api_client.dart     # Instâncias Dio por serviço
│   │   └── session_manager.dart   # Sessão UUID para o chat IA
│   ├── features/
│   │   ├── restaurantes/           # Listagem e busca de restaurantes
│   │   ├── cardapio/               # Itens do cardápio
│   │   ├── carrinho/               # Estado local do carrinho (Provider)
│   │   ├── pedido/                 # Checkout + Tracking de pedido
│   │   ├── pagamento/              # Modelos de pagamento
│   │   └── ia_chat/                # Chat com agente IA Florinda
│   └── shared/
│       ├── theme/app_theme.dart    # Tema Material Design
│       └── widgets/chat_fab.dart  # Botão flutuante do chat
├── android/                        # Configurações Android nativas
├── web/                            # Suporte web (Chrome/Edge)
├── pubspec.yaml                    # Dependências Flutter/Dart
└── quickstart.md                   # Este arquivo
```
