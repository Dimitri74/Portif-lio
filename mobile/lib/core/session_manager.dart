import 'package:uuid/uuid.dart';

class SessionManager {
  SessionManager._();
  static final SessionManager instance = SessionManager._();

  late final String sessaoId = 'sessao-${const Uuid().v4().substring(0, 13)}';
}