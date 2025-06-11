import 'package:dartz/dartz.dart';
import 'package:thinktank_flutter/core/error/failures.dart';
import 'package:thinktank_flutter/core/usecases/usecase.dart';
import 'package:thinktank_flutter/features/auth/domain/entities/user.dart';
import 'package:thinktank_flutter/features/auth/domain/repositories/auth_repository.dart';

class GetCurrentUserUseCase implements UseCase<User, NoParams> {
  final AuthRepository repository;

  GetCurrentUserUseCase(this.repository);

  @override
  Future<Either<Failure, User>> call(NoParams params) async {
    return await repository.getCurrentUser();
  }
} 