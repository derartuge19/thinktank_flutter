import 'package:dartz/dartz.dart';
import 'package:thinktank_flutter/core/error/failures.dart';
import 'package:thinktank_flutter/features/feedback/domain/entities/feedback.dart';

abstract class FeedbackRepository {
  Future<Either<Failure, List<Feedback>>> getUserFeedback();
  Future<Either<Failure, void>> submitFeedback({
    required int ideaId,
    required String content,
    required int rating,
  });
  Future<Either<Failure, void>> updateFeedback({
    required int feedbackId,
    required String content,
    required int rating,
  });
  Future<Either<Failure, void>> deleteFeedback(int feedbackId);
} 