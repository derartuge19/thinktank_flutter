import 'package:freezed_annotation/freezed_annotation.dart';

part 'feedback.freezed.dart';
part 'feedback.g.dart';

@freezed
class Feedback with _$Feedback {
  const factory Feedback({
    required int id,
    required int ideaId,
    required String content,
    required int rating,
    required int userId,
    required String userName,
    DateTime? createdAt,
    DateTime? updatedAt,
  }) = _Feedback;

  factory Feedback.fromJson(Map<String, dynamic> json) => _$FeedbackFromJson(json);
} 