
import 'package:flutter_test/flutter_test.dart';
import 'package:florinda_eats/main.dart';

void main() {
  testWidgets('App inicia sem erros', (WidgetTester tester) async {
    await tester.pumpWidget(const FlorindaEatsApp());
    expect(find.byType(FlorindaEatsApp), findsOneWidget);
    await tester.pumpAndSettle();
  });
}