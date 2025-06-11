import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:dio/dio.dart';
import 'package:thinktank_flutter/features/auth/data/repositories/auth_repository.dart';

class ReviewedIdeasPage extends StatefulWidget {
  const ReviewedIdeasPage({super.key});

  @override
  State<ReviewedIdeasPage> createState() => _ReviewedIdeasPageState();
}

class _ReviewedIdeasPageState extends State<ReviewedIdeasPage> {
  bool _isLoading = true;
  String? _error;
  List<dynamic> _feedback = [];
  late AuthRepository _authRepo;
  static const String baseUrl = 'http://10.0.2.2:3444';

  @override
  void initState() {
    super.initState();
    _initReviewedIdeasPage();
  }

  Future<void> _initReviewedIdeasPage() async {
    _authRepo = await AuthRepository.create();
    _loadFeedback();
  }

  Future<void> _loadFeedback() async {
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

      final dio = Dio();
      dio.options.headers['Authorization'] = 'Bearer $token';
      dio.options.validateStatus = (status) => status! < 500;
      dio.options.connectTimeout = const Duration(seconds: 10);
      dio.options.receiveTimeout = const Duration(seconds: 10);

      print('Fetching all feedback from: ${baseUrl}/feedback/admin/all');
      print('With token: Bearer $token');

      final response = await dio.get('$baseUrl/feedback/admin/all');
      
      print('Response status: ${response.statusCode}');
      print('Response data: ${response.data}');

      if (response.statusCode == 200) {
        final List<dynamic> feedbackData = response.data;
        setState(() {
          _feedback = feedbackData.map((item) => Map<String, dynamic>.from(item)).toList();
          _isLoading = false;
        });
      } else if (response.statusCode == 403) {
        setState(() {
          _error = 'Access denied. Only administrators can view feedback.';
          _isLoading = false;
        });
      } else {
        setState(() {
          _error = response.data?['message'] ?? 'Failed to load feedback. Status: ${response.statusCode}';
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
          _error = 'Access denied. Only administrators can view feedback.';
        } else {
          _error = e.response?.data?['message'] ?? 
                  'Failed to load feedback: ${e.message}';
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

  Future<void> _deleteFeedback(int id) async {
    try {
      final token = await _authRepo.getToken();
      if (token == null) {
        context.go('/login');
        return;
      }

      final dio = Dio();
      dio.options.headers['Authorization'] = 'Bearer $token';
      dio.options.validateStatus = (status) => status! < 500;

      print('Deleting feedback at: ${baseUrl}/feedback/admin/$id');
      final response = await dio.delete('$baseUrl/feedback/admin/$id');
      
      print('Delete response status: ${response.statusCode}');
      print('Delete response data: ${response.data}');

      if (response.statusCode == 200) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Feedback deleted successfully')),
          );
          _loadFeedback(); // Reload the list
        }
      } else if (response.statusCode == 403) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Access denied. Only administrators can delete feedback.')),
          );
        }
      } else {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text(response.data?['message'] ?? 'Failed to delete feedback')),
          );
        }
      }
    } catch (e) {
      print('Delete error: $e');
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Failed to delete feedback. Please try again.')),
        );
      }
    }
  }

  void _showDeleteConfirmation(int id) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        backgroundColor: const Color(0xFF2A2A2A),
        title: const Text(
          'Delete Feedback',
          style: TextStyle(color: Colors.white),
        ),
        content: const Text(
          'Are you sure you want to delete this feedback? This action cannot be undone.',
          style: TextStyle(color: Colors.white),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text(
              'Cancel',
              style: TextStyle(color: Colors.white),
            ),
          ),
          TextButton(
            onPressed: () {
              Navigator.pop(context);
              _deleteFeedback(id);
            },
            child: const Text(
              'Delete',
              style: TextStyle(color: Colors.red),
            ),
          ),
        ],
      ),
    );
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
              context.go('/feedback-pool'); // Navigate to feedback pool if no route to pop
            }
          },
        ),
        title: const Text(
          'Reviewed Ideas',
          style: TextStyle(
            color: Color(0xFFFFA500),
            fontSize: 24,
            fontWeight: FontWeight.bold,
          ),
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh, color: Color(0xFFFFA500)),
            onPressed: _loadFeedback,
          ),
          IconButton(
            icon: const Icon(Icons.rate_review, color: Color(0xFFFFA500)),
            onPressed: () => context.go('/feedback-pool'),
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
                          onPressed: _loadFeedback,
                          style: ElevatedButton.styleFrom(
                            backgroundColor: const Color(0xFFFFA500),
                          ),
                          child: const Text('Retry'),
                        ),
                      ],
                    ),
                  )
                : _feedback.isEmpty
                    ? const Center(
                        child: Text(
                          'No feedback given yet.',
                          style: TextStyle(color: Colors.white, fontSize: 16),
                          textAlign: TextAlign.center,
                        ),
                      )
                    : RefreshIndicator(
                        onRefresh: _loadFeedback,
                        color: const Color(0xFFFFA500),
                        child: ListView.builder(
                          padding: const EdgeInsets.all(16),
                          itemCount: _feedback.length,
                          itemBuilder: (context, index) {
                            final item = _feedback[index];
                            final idea = item['idea'] as Map<String, dynamic>?;
                            if (idea == null) return const SizedBox.shrink();

                            Color statusColor;
                            switch (item['status']?.toString().toLowerCase()) {
                              case 'approved':
                                statusColor = Colors.green;
                                break;
                              case 'rejected':
                                statusColor = Colors.red;
                                break;
                              default:
                                statusColor = Colors.orange;
                            }

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
                                        Row(
                                          children: [
                                            IconButton(
                                              icon: const Icon(Icons.edit, color: Color(0xFFFFA500)),
                                              onPressed: () => context.go('/edit-feedback/${item['id']}'),
                                            ),
                                            IconButton(
                                              icon: const Icon(Icons.delete, color: Colors.red),
                                              onPressed: () => _showDeleteConfirmation(item['id']),
                                            ),
                                          ],
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
                                    Container(
                                      padding: const EdgeInsets.all(12),
                                      decoration: BoxDecoration(
                                        color: statusColor.withOpacity(0.2),
                                        borderRadius: BorderRadius.circular(8),
                                        border: Border.all(color: statusColor),
                                      ),
                                      child: Column(
                                        crossAxisAlignment: CrossAxisAlignment.start,
                                        children: [
                                          Text(
                                            'Feedback Status: ${item['status']?.toString().toUpperCase() ?? 'PENDING'}',
                                            style: TextStyle(
                                              color: statusColor,
                                              fontWeight: FontWeight.bold,
                                            ),
                                          ),
                                          const SizedBox(height: 8),
                                          Text(
                                            item['comment'] ?? '',
                                            style: const TextStyle(color: Colors.white70),
                                          ),
                                          const SizedBox(height: 8),
                                          Text(
                                            'Given by: ${item['admin']?['email'] ?? 'Unknown'}',
                                            style: const TextStyle(
                                              color: Colors.grey,
                                              fontSize: 12,
                                            ),
                                          ),
                                          Text(
                                            'Given on: ${DateTime.parse(item['createdAt']).toLocal().toString().split('.')[0]}',
                                            style: const TextStyle(
                                              color: Colors.grey,
                                              fontSize: 12,
                                            ),
                                          ),
                                        ],
                                      ),
                                    ),
                                    const SizedBox(height: 8),
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