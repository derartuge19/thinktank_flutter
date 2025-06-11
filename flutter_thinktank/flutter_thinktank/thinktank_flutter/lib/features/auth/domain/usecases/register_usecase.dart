import 'package:dartz/dartz.dart';
import 'package:thinktank_flutter/core/error/failures.dart';
import 'package:thinktank_flutter/core/usecases/usecase.dart';
import 'package:thinktank_flutter/features/auth/domain/entities/user.dart';
import 'package:thinktank_flutter/features/auth/domain/repositories/auth_repository.dart';

class RegisterParams {
  final String email;
  final String password;
  final String name;

  RegisterParams({
    required this.email,
    required this.password,
    required this.name,
  });
}

class RegisterUseCase implements UseCase<AuthResponse, RegisterParams> {
  final AuthRepository repository;

  RegisterUseCase(this.repository);

  @override
  Future<Either<Failure, AuthResponse>> call(RegisterParams params) async {
    return await repository.register(
      email: params.email,
      password: params.password,
      name: params.name,
    );
  }
} 