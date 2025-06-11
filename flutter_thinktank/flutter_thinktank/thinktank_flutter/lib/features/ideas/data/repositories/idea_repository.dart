abstract class IdeaRepository {
  Future<List<dynamic>> getApprovedIdeas();
  Future<List<dynamic>> getUserIdeas();
  Future<List<dynamic>> getFeedbackPool();
} 