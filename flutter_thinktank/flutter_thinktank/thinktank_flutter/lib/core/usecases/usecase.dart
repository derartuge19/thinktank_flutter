import 'package:dartz/dartz.dart';
import 'package:thinktank_flutter/core/error/failures.dart';

abstract class UseCase<Type, Params> {
  Future<Either<Failure, Type>> call(Params params);
}

class NoParams {
  const NoParams();
}

// Example usage:
// class GetIdeasUseCase implements UseCase<List<Idea>, NoParams> {
//   @override
//   Future<Either<Failure, List<Idea>>> call(NoParams params) async {
//     // Implementation
//   }
// } 