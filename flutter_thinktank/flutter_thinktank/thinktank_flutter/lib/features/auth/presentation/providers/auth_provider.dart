import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:thinktank_flutter/core/network/api_client.dart';
import 'package:thinktank_flutter/core/usecases/usecase.dart';
import 'package:thinktank_flutter/features/auth/data/datasources/auth_remote_data_source.dart';
import 'package:thinktank_flutter/features/auth/data/repositories/auth_repository_impl.dart';
import 'package:thinktank_flutter/features/auth/domain/entities/user.dart';
import 'package:thinktank_flutter/features/auth/domain/repositories/auth_repository.dart';
import 'package:thinktank_flutter/features/auth/domain/usecases/get_current_user_usecase.dart';
import 'package:thinktank_flutter/features/auth/domain/usecases/login_usecase.dart';
import 'package:thinktank_flutter/features/auth/domain/usecases/logout_usecase.dart';
import 'package:thinktank_flutter/features/auth/domain/usecases/register_usecase.dart';

part 'auth_provider.freezed.dart';

@freezed
class AuthState with _$AuthState {
  const factory AuthState.initial() = _Initial;
  const factory AuthState.loading() = _Loading;
  const factory AuthState.authenticated(User user) = _Authenticated;
  const factory AuthState.unauthenticated() = _Unauthenticated;
  const factory AuthState.error(String message) = _Error;
}

class AuthNotifier extends StateNotifier<AuthState> {
  final LoginUseCase _loginUseCase;
  final RegisterUseCase _registerUseCase;
  final LogoutUseCase _logoutUseCase;
  final GetCurrentUserUseCase _getCurrentUserUseCase;

  AuthNotifier({
    required LoginUseCase loginUseCase,
    required RegisterUseCase registerUseCase,
    required LogoutUseCase logoutUseCase,
    required GetCurrentUserUseCase getCurrentUserUseCase,
  })  : _loginUseCase = loginUseCase,
        _registerUseCase = registerUseCase,
        _logoutUseCase = logoutUseCase,
        _getCurrentUserUseCase = getCurrentUserUseCase,
        super(const AuthState.initial());

  Future<void> login(String email, String password) async {
    state = const AuthState.loading();
    final result = await _loginUseCase(LoginParams(
      email: email,
      password: password,
    ));
    result.fold(
      (failure) => state = AuthState.error(failure.message),
      (authResponse) => state = AuthState.authenticated(authResponse.user),
    );
  }

  Future<void> register(String email, String password, String name) async {
    state = const AuthState.loading();
    final result = await _registerUseCase(RegisterParams(
      email: email,
      password: password,
      name: name,
    ));
    result.fold(
      (failure) => state = AuthState.error(failure.message),
      (authResponse) => state = AuthState.authenticated(authResponse.user),
    );
  }

  Future<void> logout() async {
    state = const AuthState.loading();
    final result = await _logoutUseCase(const NoParams());
    result.fold(
      (failure) => state = AuthState.error(failure.message),
      (_) => state = const AuthState.unauthenticated(),
    );
  }

  Future<void> getCurrentUser() async {
    state = const AuthState.loading();
    final result = await _getCurrentUserUseCase(const NoParams());
    result.fold(
      (failure) => state = AuthState.error(failure.message),
      (user) => state = AuthState.authenticated(user),
    );
  }
}

final apiClientProvider = Provider<ApiClient>((ref) {
  return ApiClient();
});

final authRemoteDataSourceProvider = Provider<AuthRemoteDataSource>((ref) {
  final apiClient = ref.watch(apiClientProvider);
  return AuthRemoteDataSourceImpl(apiClient.dio);
});

final authRepositoryProvider = Provider<AuthRepository>((ref) {
  throw UnimplementedError('This provider should be overridden in tests');
});

final authProvider = StateNotifierProvider<AuthNotifier, AuthState>((ref) {
  final repository = ref.watch(authRepositoryProvider);
  return AuthNotifier(
    loginUseCase: LoginUseCase(repository),
    registerUseCase: RegisterUseCase(repository),
    logoutUseCase: LogoutUseCase(repository),
    getCurrentUserUseCase: GetCurrentUserUseCase(repository),
  );
}); 