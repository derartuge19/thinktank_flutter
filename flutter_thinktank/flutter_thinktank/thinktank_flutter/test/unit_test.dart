import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:dio/dio.dart';
import 'package:mockito/mockito.dart';
import 'package:mockito/annotations.dart';
import 'package:thinktank_flutter/core/error/failures.dart';
import 'package:thinktank_flutter/core/usecases/usecase.dart';
import 'package:thinktank_flutter/features/auth/domain/repositories/auth_repository.dart';
import 'package:thinktank_flutter/features/auth/data/repositories/auth_repository_impl.dart';
import 'package:thinktank_flutter/features/auth/data/datasources/auth_remote_data_source.dart';
import 'package:thinktank_flutter/features/ideas/data/models/idea.dart';
import 'package:thinktank_flutter/features/feedback/data/models/feedback.dart';
import 'package:dartz/dartz.dart';
import 'package:thinktank_flutter/features/auth/domain/entities/user.dart';

// Generate mock classes
@GenerateMocks([AuthRemoteDataSource, SharedPreferences, AuthRepository])
import 'unit_test.mocks.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  // Mock SharedPreferences for testing
  SharedPreferences.setMockInitialValues({});

  late MockAuthRemoteDataSource mockRemoteDataSource;
  late MockSharedPreferences mockPrefs;
  late AuthRepository authRepository;
  late MockAuthRepository mockAuthRepository;

  setUp(() async {
    // Clear SharedPreferences before each test
    final prefs = await SharedPreferences.getInstance();
    await prefs.clear();
    
    // Create mock instances
    mockRemoteDataSource = MockAuthRemoteDataSource();
    mockPrefs = MockSharedPreferences();
    
    // Create test repository with mocks
    authRepository = AuthRepositoryImpl(mockRemoteDataSource);
    mockAuthRepository = MockAuthRepository();
  });

  tearDown(() async {
    // Clear SharedPreferences after each test
    final prefs = await SharedPreferences.getInstance();
    await prefs.clear();
  });

  group('Unit Tests', () {
    test('A simple unit test', () {
      // This is a placeholder for a unit test.
      // Replace with actual unit tests for your app's logic.
      expect(1 + 1, 2);
    });
  });

  group('Failure', () {
    test('ServerFailure should have correct message and code', () {
      const tMessage = 'Server Error';
      const tCode = '500';
      const serverFailure = ServerFailure(message: tMessage, code: tCode);

      expect(serverFailure.message, tMessage);
      expect(serverFailure.code, tCode);
      expect(serverFailure.props, [tMessage, tCode]);
    });

    test('NetworkFailure should have correct message and code', () {
      const tMessage = 'No Internet Connection';
      const networkFailure = NetworkFailure(message: tMessage);

      expect(networkFailure.message, tMessage);
      expect(networkFailure.code, isNull);
      expect(networkFailure.props, [tMessage, null]);
    });
  });

  group('NoParams', () {
    test('should return true when comparing two NoParams instances', () {
      const noParams1 = NoParams();
      const noParams2 = NoParams();

      expect(noParams1, equals(noParams2));
    });
  });

  group('AuthRepository', () {
    final testEmail = 'test@example.com';
    final testPassword = 'password123';
    final testName = 'Test User';

    test('login should return AuthResponse on success', () async {
      // Arrange
      final authResponse = AuthResponse(
        token: 'test_token',
        user: User(
          id: 1,
          email: testEmail,
          name: testName,
          role: 'user',
        ),
      );
      when(mockAuthRepository.login(
        email: testEmail,
        password: testPassword,
      )).thenAnswer((_) async => Right(authResponse));

      // Act
      final result = await mockAuthRepository.login(
        email: testEmail,
        password: testPassword,
      );

      // Assert
      expect(result, Right(authResponse));
      verify(mockAuthRepository.login(
        email: testEmail,
        password: testPassword,
      )).called(1);
    });

    test('register should return AuthResponse on success', () async {
      // Arrange
      final authResponse = AuthResponse(
        token: 'test_token',
        user: User(
          id: 1,
          email: testEmail,
          name: testName,
          role: 'user',
        ),
      );
      when(mockAuthRepository.register(
        email: testEmail,
        password: testPassword,
        name: testName,
      )).thenAnswer((_) async => Right(authResponse));

      // Act
      final result = await mockAuthRepository.register(
        email: testEmail,
        password: testPassword,
        name: testName,
      );

      // Assert
      expect(result, Right(authResponse));
      verify(mockAuthRepository.register(
        email: testEmail,
        password: testPassword,
        name: testName,
      )).called(1);
    });

    test('logout should return void on success', () async {
      // Arrange
      when(mockAuthRepository.logout())
          .thenAnswer((_) async => const Right(null));

      // Act
      final result = await mockAuthRepository.logout();

      // Assert
      expect(result, const Right(null));
      verify(mockAuthRepository.logout()).called(1);
    });

    test('getCurrentUser should return User on success', () async {
      // Arrange
      final user = User(
        id: 1,
        email: testEmail,
        name: testName,
        role: 'user',
      );
      when(mockAuthRepository.getCurrentUser())
          .thenAnswer((_) async => Right(user));

      // Act
      final result = await mockAuthRepository.getCurrentUser();

      // Assert
      expect(result, Right(user));
      verify(mockAuthRepository.getCurrentUser()).called(1);
    });

    test('isAuthenticated should return true when user is authenticated', () async {
      // Arrange
      when(mockAuthRepository.isAuthenticated())
          .thenAnswer((_) async => const Right(true));

      // Act
      final result = await mockAuthRepository.isAuthenticated();

      // Assert
      expect(result, const Right(true));
      verify(mockAuthRepository.isAuthenticated()).called(1);
    });
  });

  group('Idea Model Tests', () {
    test('Idea model should correctly parse from JSON', () {
      // Arrange
      final json = {
        'id': '1',
        'title': 'Test Idea',
        'description': 'Test Description',
        'tags': ['test', 'idea'],
        'status': 'pending',
        'createdAt': '2024-03-20T10:00:00Z',
        'updatedAt': '2024-03-20T10:00:00Z',
        'userId': 'user1'
      };

      // Act
      final idea = Idea.fromJson(json);

      // Assert
      expect(idea.id, equals('1'));
      expect(idea.title, equals('Test Idea'));
      expect(idea.description, equals('Test Description'));
      expect(idea.tags, equals(['test', 'idea']));
      expect(idea.status, equals('pending'));
      expect(idea.userId, equals('user1'));
    });

    test('Idea model should correctly convert to JSON', () {
      // Arrange
      final idea = Idea(
        id: '1',
        title: 'Test Idea',
        description: 'Test Description',
        tags: ['test', 'idea'],
        status: 'pending',
        createdAt: DateTime.parse('2024-03-20T10:00:00Z'),
        updatedAt: DateTime.parse('2024-03-20T10:00:00Z'),
        userId: 'user1'
      );

      // Act
      final json = idea.toJson();

      // Assert
      expect(json['id'], equals('1'));
      expect(json['title'], equals('Test Idea'));
      expect(json['description'], equals('Test Description'));
      expect(json['tags'], equals(['test', 'idea']));
      expect(json['status'], equals('pending'));
      expect(json['userId'], equals('user1'));
    });
  });

  group('Feedback Model Tests', () {
    test('Feedback model should correctly parse from JSON', () {
      // Arrange
      final json = {
        'id': '1',
        'ideaId': 'idea1',
        'content': 'Test Feedback',
        'rating': 4,
        'status': 'approved',
        'createdAt': '2024-03-20T10:00:00Z',
        'updatedAt': '2024-03-20T10:00:00Z',
        'userId': 'user1'
      };

      // Act
      final feedback = Feedback.fromJson(json);

      // Assert
      expect(feedback.id, equals('1'));
      expect(feedback.ideaId, equals('idea1'));
      expect(feedback.content, equals('Test Feedback'));
      expect(feedback.rating, equals(4));
      expect(feedback.status, equals('approved'));
      expect(feedback.userId, equals('user1'));
    });

    test('Feedback model should correctly convert to JSON', () {
      // Arrange
      final feedback = Feedback(
        id: '1',
        ideaId: 'idea1',
        content: 'Test Feedback',
        rating: 4,
        status: 'approved',
        createdAt: DateTime.parse('2024-03-20T10:00:00Z'),
        updatedAt: DateTime.parse('2024-03-20T10:00:00Z'),
        userId: 'user1'
      );

      // Act
      final json = feedback.toJson();

      // Assert
      expect(json['id'], equals('1'));
      expect(json['ideaId'], equals('idea1'));
      expect(json['content'], equals('Test Feedback'));
      expect(json['rating'], equals(4));
      expect(json['status'], equals('approved'));
      expect(json['userId'], equals('user1'));
    });
  });
} 