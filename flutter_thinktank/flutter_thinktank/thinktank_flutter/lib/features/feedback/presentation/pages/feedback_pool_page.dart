import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:dio/dio.dart';
import 'package:thinktank_flutter/features/auth/data/repositories/auth_repository.dart';

class FeedbackPoolPage extends StatefulWidget {
  const FeedbackPoolPage({super.key});

  @override
  State<FeedbackPoolPage> createState() => _FeedbackPoolPageState();
}

class _FeedbackPoolPageState extends State<FeedbackPoolPage> {
  bool _isLoading = true;
  String? _error;
  List<dynamic> _ideas = [];
  late AuthRepository _authRepo;

  static const String baseUrl = 'http://10.0.2.2:3444';

  @override
  void initState() {
    super.initState();
    _initFeedbackPoolPage();
  }

  Future<void> _initFeedbackPoolPage() async {
    _authRepo = await AuthRepository.create();
    _loadIdeas();
  }

  Future<void> _loadIdeas() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      final token = await _authRepo.getToken();
      if (token == null) {
        context.go('/login');
        return;
      }

      // Verify admin status
      final isAdmin = await _authRepo.isAdmin();
      if (!isAdmin) {
        setState(() {
          _error = 'Access denied. Only administrators can access the feedback pool.';
          _isLoading = false;
        });
        return;
      }

      final dio = Dio();
      dio.options.headers['Authorization'] = 'Bearer $token';
      dio.options.validateStatus = (status) => status! < 500;
      dio.options.connectTimeout = const Duration(seconds: 10);
      dio.options.receiveTimeout = const Duration(seconds: 10);

      print('Fetching ideas pending feedback from: ${baseUrl}/ideas/admin/all');
      print('With token: Bearer ${token.substring(0, 20)}...');

      // Get all ideas for admin
      final response = await dio.get('$baseUrl/ideas/admin/all');
      
      print('Response status: ${response.statusCode}');
      print('Response data: ${response.data}');

      if (response.statusCode == 200) {
        final List<dynamic> ideasData = response.data;
        // Filter ideas that are pending feedback (no feedback given yet)
        final pendingIdeas = ideasData.where((idea) {
          // Check if the idea has any feedback
          final hasFeedback = idea['feedback'] != null && 
                            (idea['feedback'] as List).isNotEmpty;
          return !hasFeedback;
        }).toList();
        
        setState(() {
          _ideas = pendingIdeas;
          _isLoading = false;
        });
      } else if (response.statusCode == 403) {
        setState(() {
          _error = 'Access denied. Only administrators can access the feedback pool.';
          _isLoading = false;
        });
      } else {
        setState(() {
          _error = response.data?['message'] ?? 'Failed to load ideas. Status: ${response.statusCode}';
          _isLoading = false;
        });
      }
    } on DioException catch (e) {
      print('DioException: ${e.message}');
      print('Error type: ${e.type}');
      print('Error response: ${e.response?.data}');
      
      setState(() {
        if (e.type == DioExceptionType.connectionTimeout) {
          _error = 'Connection timed out. Please check your internet connection and try again.';
        } else if (e.type == DioExceptionType.connectionError) {
          _error = 'Could not connect to the server. Please make sure:\n'
                  '1. The backend server is running on port 3444\n'
                  '2. You are using the correct server URL\n'
                  '3. Your browser can access the server';
        } else if (e.response?.statusCode == 403) {
          _error = 'Access denied. Only administrators can access the feedback pool.';
        } else {
          _error = e.response?.data?['message'] ?? 
                  'Failed to load ideas: ${e.message}';
        }
        _isLoading = false;
      });
    } catch (e) {
      print('Unexpected error: $e');
      setState(() {
        _error = 'An unexpected error occurred: $e';
        _isLoading = false;
      });
    }
  }

  Color _getStatusColor(String status) {
    switch (status.toLowerCase()) {
      case 'approved':
        return Colors.green;
      case 'rejected':
        return Colors.red;
      case 'needs_improvement':
        return Colors.orange;
      default:
        return Colors.grey;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF1A1A1A),
      appBar: AppBar(
        backgroundColor: const Color(0xFF1A1A1A),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back, color: Colors.white),
          onPressed: () {
            if (context.canPop()) {
              context.pop();
            } else {
              context.go('/dashboard'); // Navigate to dashboard if no route to pop
            }
          },
        ),
        title: const Text(
          'Feedback Pool',
          style: TextStyle(
            color: Color(0xFFFFA500),
            fontSize: 24,
            fontWeight: FontWeight.bold,
          ),
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh, color: Color(0xFFFFA500)),
            onPressed: _loadIdeas,
          ),
          IconButton(
            icon: const Icon(Icons.rate_review, color: Color(0xFFFFA500)),
            onPressed: () => context.go('/reviewed-ideas'),
          ),
        ],
      ),
      body: SafeArea(
        child: _isLoading
            ? const Center(child: CircularProgressIndicator(color: Color(0xFFFFA500)))
            : _error != null
                ? Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Text(
                          _error!,
                          style: const TextStyle(color: Colors.red),
                          textAlign: TextAlign.center,
                        ),
                        const SizedBox(height: 16),
                        ElevatedButton(
                          onPressed: _loadIdeas,
                          style: ElevatedButton.styleFrom(
                            backgroundColor: const Color(0xFFFFA500),
                          ),
                          child: const Text('Retry'),
                        ),
                      ],
                    ),
                  )
                : _ideas.isEmpty
                    ? const Center(
                        child: Text(
                          'No ideas pending feedback.',
                          style: TextStyle(color: Colors.white, fontSize: 16),
                          textAlign: TextAlign.center,
                        ),
                      )
                    : RefreshIndicator(
                        onRefresh: _loadIdeas,
                        color: const Color(0xFFFFA500),
                        child: ListView.builder(
                          padding: const EdgeInsets.all(16),
                          itemCount: _ideas.length,
                          itemBuilder: (context, index) {
                            final idea = _ideas[index];
                            final status = idea['status']?.toString().toLowerCase() ?? 'pending';
                            final statusColor = _getStatusColor(status);
                            
                            return Card(
                              color: const Color(0xFF2A2A2A),
                              margin: const EdgeInsets.only(bottom: 16),
                              child: Padding(
                                padding: const EdgeInsets.all(16),
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Row(
                                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                      children: [
                                        Expanded(
                                          child: Text(
                                            idea['title'] ?? '',
                                            style: const TextStyle(
                                              color: Colors.white,
                                              fontSize: 20,
                                              fontWeight: FontWeight.bold,
                                            ),
                                          ),
                                        ),
                                        Container(
                                          padding: const EdgeInsets.symmetric(
                                            horizontal: 12,
                                            vertical: 6,
                                          ),
                                          decoration: BoxDecoration(
                                            color: statusColor.withOpacity(0.2),
                                            borderRadius: BorderRadius.circular(4),
                                            border: Border.all(color: statusColor),
                                          ),
                                          child: Text(
                                            status.toUpperCase(),
                                            style: TextStyle(
                                              color: statusColor,
                                              fontWeight: FontWeight.bold,
                                            ),
                                          ),
                                        ),
                                      ],
                                    ),
                                    const SizedBox(height: 8),
                                    Text(
                                      idea['description'] ?? '',
                                      style: const TextStyle(color: Colors.white70),
                                      maxLines: 3,
                                      overflow: TextOverflow.ellipsis,
                                    ),
                                    if (idea['tags'] != null && idea['tags'].toString().isNotEmpty) ...[
                                      const SizedBox(height: 8),
                                      Wrap(
                                        spacing: 8,
                                        children: idea['tags'].toString().split(',').map<Widget>((tag) {
                                          final trimmedTag = tag.trim();
                                          if (trimmedTag.isEmpty) return const SizedBox.shrink();
                                          return Chip(
                                            label: Text(
                                              trimmedTag,
                                              style: const TextStyle(color: Colors.white),
                                            ),
                                            backgroundColor: const Color(0xFFFFA500).withOpacity(0.2),
                                          );
                                        }).toList(),
                                      ),
                                    ],
                                    const SizedBox(height: 16),
                                    Row(
                                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                      children: [
                                        Text(
                                          'Submitted by: ${idea['user']?['email'] ?? 'Unknown'}',
                                          style: const TextStyle(
                                            color: Colors.grey,
                                            fontSize: 12,
                                          ),
                                        ),
                                        Text(
                                          'Created: ${DateTime.parse(idea['createdAt']).toLocal().toString().split('.')[0]}',
                                          style: const TextStyle(
                                            color: Colors.grey,
                                            fontSize: 12,
                                          ),
                                        ),
                                      ],
                                    ),
                                    const SizedBox(height: 16),
                                    Row(
                                      mainAxisAlignment: MainAxisAlignment.end,
                                      children: [
                                        ElevatedButton.icon(
                                          onPressed: () => context.go('/give-feedback/${idea['id']}'),
                                          style: ElevatedButton.styleFrom(
                                            backgroundColor: const Color(0xFFFFA500),
                                            foregroundColor: Colors.white,
                                          ),
                                          icon: const Icon(Icons.rate_review),
                                          label: const Text('Give Feedback'),
                                        ),
                                      ],
                                    ),
                                  ],
                                ),
                              ),
                            );
                          },
                        ),
                      ),
      ),
    );
  }
} 