import 'package:dartz/dartz.dart';
import 'package:thinktank_flutter/core/error/failures.dart';
import 'package:thinktank_flutter/core/usecases/usecase.dart';
import 'package:thinktank_flutter/features/auth/domain/repositories/auth_repository.dart';

class LogoutUseCase implements UseCase<void, NoParams> {
  final AuthRepository repository;

  LogoutUseCase(this.repository);

  @override
  Future<Either<Failure, void>> call(NoParams params) async {
    return await repository.logout();
  }
} 