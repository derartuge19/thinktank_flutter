// This is a basic Flutter widget test.
//
// To perform an interaction with a widget in your test, use the WidgetTester
// utility in the flutter_test package. For example, you can send tap and scroll
// gestures. You can also use WidgetTester to find child widgets in the widget
// tree, read text, and verify that the values of widget properties are correct.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:mockito/annotations.dart';
import 'package:thinktank_flutter/main.dart';
import 'package:thinktank_flutter/features/auth/domain/repositories/auth_repository.dart';
import 'package:thinktank_flutter/features/ideas/domain/repositories/idea_repository.dart';
import 'package:thinktank_flutter/features/feedback/domain/repositories/feedback_repository.dart';
import 'package:thinktank_flutter/features/auth/domain/entities/user.dart';
import 'package:thinktank_flutter/core/error/failures.dart';
import 'package:dartz/dartz.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:thinktank_flutter/features/auth/presentation/providers/auth_provider.dart';
import 'package:thinktank_flutter/features/ideas/presentation/providers/idea_provider.dart';
import 'package:thinktank_flutter/features/feedback/presentation/providers/feedback_provider.dart';

@GenerateMocks([
  AuthRepository,
  IdeaRepository,
  FeedbackRepository,
])
import 'widget_test.mocks.dart';

void main() {
  late MockAuthRepository mockAuthRepository;
  late MockIdeaRepository mockIdeaRepository;
  late MockFeedbackRepository mockFeedbackRepository;

  setUp(() {
    mockAuthRepository = MockAuthRepository();
    mockIdeaRepository = MockIdeaRepository();
    mockFeedbackRepository = MockFeedbackRepository();

    // Setup default mock responses
    when(mockAuthRepository.isAuthenticated())
        .thenAnswer((_) async => const Right(false));
  });

  Widget createTestWidget(Widget child) {
    return ProviderScope(
      overrides: [
        authRepositoryProvider.overrideWithValue(mockAuthRepository),
        ideaRepositoryProvider.overrideWithValue(mockIdeaRepository),
        feedbackRepositoryProvider.overrideWithValue(mockFeedbackRepository),
      ],
      child: MaterialApp(
        home: child,
      ),
    );
  }

  testWidgets('App shows landing page initially', (WidgetTester tester) async {
    await tester.pumpWidget(createTestWidget(const MyApp()));
    await tester.pumpAndSettle();

    expect(find.text('ThinkTank Inspire the world!'), findsOneWidget);
  });

  testWidgets('Login form shows correct fields', (WidgetTester tester) async {
    await tester.pumpWidget(createTestWidget(const MyApp()));
    await tester.pumpAndSettle();

    // Navigate to login page
    await tester.tap(find.text('Login'));
    await tester.pumpAndSettle();

    // Check for form fields
    expect(find.byType(TextFormField), findsNWidgets(2)); // Email and password
    expect(find.text('Email'), findsOneWidget);
    expect(find.text('Password'), findsOneWidget);
  });

  testWidgets('Register form shows correct fields', (WidgetTester tester) async {
    await tester.pumpWidget(createTestWidget(const MyApp()));
    await tester.pumpAndSettle();

    // Navigate to register page
    await tester.tap(find.text('Register'));
    await tester.pumpAndSettle();

    // Check for form fields
    expect(find.byType(TextFormField), findsNWidgets(3)); // Name, email, and password
    expect(find.text('Name'), findsOneWidget);
    expect(find.text('Email'), findsOneWidget);
    expect(find.text('Password'), findsOneWidget);
  });

  testWidgets('Successful login navigates to dashboard', (WidgetTester tester) async {
    // Setup mock response for successful login
    final authResponse = AuthResponse(
      token: 'test_token',
      user: User(
        id: 1,
        email: 'test@example.com',
        name: 'Test User',
        role: 'user',
      ),
    );
    when(mockAuthRepository.login(
      email: 'test@example.com',
      password: 'password123',
    )).thenAnswer((_) async => Right(authResponse));

    // Setup mock response for authenticated state after login
    when(mockAuthRepository.isAuthenticated())
        .thenAnswer((_) async => const Right(true));

    await tester.pumpWidget(createTestWidget(const MyApp()));
    await tester.pumpAndSettle();

    // Navigate to login page
    await tester.tap(find.text('Login'));
    await tester.pumpAndSettle();

    // Enter login credentials
    await tester.enterText(find.byType(TextFormField).first, 'test@example.com');
    await tester.enterText(find.byType(TextFormField).last, 'password123');
    await tester.tap(find.text('Login'));
    await tester.pumpAndSettle();

    // Verify navigation to dashboard
    expect(find.text('Dashboard'), findsOneWidget);
  });

  testWidgets('Profile page shows user information', (WidgetTester tester) async {
    // Setup mock response for authenticated state
    when(mockAuthRepository.isAuthenticated())
        .thenAnswer((_) async => const Right(true));

    // Setup mock response for current user
    final user = User(
      id: 1,
      email: 'test@example.com',
      name: 'Test User',
      role: 'user',
    );
    when(mockAuthRepository.getCurrentUser())
        .thenAnswer((_) async => Right(user));

    await tester.pumpWidget(createTestWidget(const MyApp()));
    await tester.pumpAndSettle();

    // Navigate to profile page
    await tester.tap(find.byIcon(Icons.person));
    await tester.pumpAndSettle();

    // Verify profile information is displayed
    expect(find.text('Profile'), findsOneWidget);
    expect(find.text('Test User'), findsOneWidget);
    expect(find.text('test@example.com'), findsOneWidget);
  });
}
