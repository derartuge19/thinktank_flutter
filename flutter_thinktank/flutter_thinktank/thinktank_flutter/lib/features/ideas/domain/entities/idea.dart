import 'package:freezed_annotation/freezed_annotation.dart';

part 'idea.freezed.dart';
part 'idea.g.dart';

@freezed
class Idea with _$Idea {
  const factory Idea({
    required int id,
    required String title,
    required String description,
    required String status,
    required int userId,
    required String userName,
    String? feedback,
    int? rating,
    DateTime? createdAt,
    DateTime? updatedAt,
  }) = _Idea;

  factory Idea.fromJson(Map<String, dynamic> json) => _$IdeaFromJson(json);
} 