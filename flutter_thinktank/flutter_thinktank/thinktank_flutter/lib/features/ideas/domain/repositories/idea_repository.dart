import 'package:dartz/dartz.dart';
import 'package:thinktank_flutter/core/error/failures.dart';
import 'package:thinktank_flutter/features/ideas/domain/entities/idea.dart';

abstract class IdeaRepository {
  Future<Either<Failure, List<Idea>>> getApprovedIdeas();
  Future<Either<Failure, List<Idea>>> getUserIdeas();
  Future<Either<Failure, List<Idea>>> getFeedbackPool();
  Future<Either<Failure, Idea>> submitIdea({
    required String title,
    required String description,
  });
  Future<Either<Failure, void>> submitFeedback({
    required int ideaId,
    required String feedback,
  });
} 