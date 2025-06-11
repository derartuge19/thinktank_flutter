class Idea {
  final String id;
  final String title;
  final String description;
  final List<String> tags;
  final String status;
  final DateTime createdAt;
  final DateTime updatedAt;
  final String userId;

  Idea({
    required this.id,
    required this.title,
    required this.description,
    required this.tags,
    required this.status,
    required this.createdAt,
    required this.updatedAt,
    required this.userId,
  });

  factory Idea.fromJson(Map<String, dynamic> json) {
    return Idea(
      id: json['id'] as String,
      title: json['title'] as String,
      description: json['description'] as String,
      tags: List<String>.from(json['tags'] as List),
      status: json['status'] as String,
      createdAt: DateTime.parse(json['createdAt'] as String),
      updatedAt: DateTime.parse(json['updatedAt'] as String),
      userId: json['userId'] as String,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'title': title,
      'description': description,
      'tags': tags,
      'status': status,
      'createdAt': createdAt.toIso8601String(),
      'updatedAt': updatedAt.toIso8601String(),
      'userId': userId,
    };
  }
} 