import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:thinktank_flutter/core/navigation/app_router.dart';

void main() {
  runApp(const ProviderScope(child: MyApp()));
}

class MyApp extends ConsumerWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final appRouter = AppRouter();
    return MaterialApp.router(
      title: 'ThinkTank',
      theme: ThemeData(
        primaryColor: const Color(0xFFFFA500),
        scaffoldBackgroundColor: const Color(0xFF1A1A1A),
        textTheme: const TextTheme(bodyLarge: TextStyle(color: Colors.white), bodyMedium: TextStyle(color: Colors.white)),
        appBarTheme: const AppBarTheme(backgroundColor: Color(0xFF1A1A1A), titleTextStyle: TextStyle(color: Color(0xFFFFA500), fontSize: 24, fontWeight: FontWeight.bold)),
        elevatedButtonTheme: ElevatedButtonThemeData(style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFFFFA500), foregroundColor: Colors.black, shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)))),
      ),
      routerConfig: appRouter.router,
    );
  }
}
