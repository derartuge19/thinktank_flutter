class Feedback {
  final String id;
  final String ideaId;
  final String content;
  final int rating;
  final String status;
  final DateTime createdAt;
  final DateTime updatedAt;
  final String userId;

  Feedback({
    required this.id,
    required this.ideaId,
    required this.content,
    required this.rating,
    required this.status,
    required this.createdAt,
    required this.updatedAt,
    required this.userId,
  });

  factory Feedback.fromJson(Map<String, dynamic> json) {
    return Feedback(
      id: json['id'] as String,
      ideaId: json['ideaId'] as String,
      content: json['content'] as String,
      rating: json['rating'] as int,
      status: json['status'] as String,
      createdAt: DateTime.parse(json['createdAt'] as String),
      updatedAt: DateTime.parse(json['updatedAt'] as String),
      userId: json['userId'] as String,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'ideaId': ideaId,
      'content': content,
      'rating': rating,
      'status': status,
      'createdAt': createdAt.toIso8601String(),
      'updatedAt': updatedAt.toIso8601String(),
      'userId': userId,
    };
  }
} 