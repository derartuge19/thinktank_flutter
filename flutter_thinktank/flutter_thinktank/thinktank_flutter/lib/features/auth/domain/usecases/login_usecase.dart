import 'package:dartz/dartz.dart';
import 'package:thinktank_flutter/core/error/failures.dart';
import 'package:thinktank_flutter/core/usecases/usecase.dart';
import 'package:thinktank_flutter/features/auth/domain/entities/user.dart';
import 'package:thinktank_flutter/features/auth/domain/repositories/auth_repository.dart';

class LoginParams {
  final String email;
  final String password;

  LoginParams({
    required this.email,
    required this.password,
  });
}

class LoginUseCase implements UseCase<AuthResponse, LoginParams> {
  final AuthRepository repository;

  LoginUseCase(this.repository);

  @override
  Future<Either<Failure, AuthResponse>> call(LoginParams params) async {
    return await repository.login(
      email: params.email,
      password: params.password,
    );
  }
} 