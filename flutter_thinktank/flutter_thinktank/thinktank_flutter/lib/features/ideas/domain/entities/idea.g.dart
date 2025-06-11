// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'idea.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$IdeaImpl _$$IdeaImplFromJson(Map<String, dynamic> json) => _$IdeaImpl(
      id: (json['id'] as num).toInt(),
      title: json['title'] as String,
      description: json['description'] as String,
      status: json['status'] as String,
      userId: (json['userId'] as num).toInt(),
      userName: json['userName'] as String,
      feedback: json['feedback'] as String?,
      rating: (json['rating'] as num?)?.toInt(),
      createdAt: json['createdAt'] == null
          ? null
          : DateTime.parse(json['createdAt'] as String),
      updatedAt: json['updatedAt'] == null
          ? null
          : DateTime.parse(json['updatedAt'] as String),
    );

Map<String, dynamic> _$$IdeaImplToJson(_$IdeaImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      'title': instance.title,
      'description': instance.description,
      'status': instance.status,
      'userId': instance.userId,
      'userName': instance.userName,
      'feedback': instance.feedback,
      'rating': instance.rating,
      'createdAt': instance.createdAt?.toIso8601String(),
      'updatedAt': instance.updatedAt?.toIso8601String(),
    };
