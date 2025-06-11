import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:dio/dio.dart';
import 'package:thinktank_flutter/features/auth/data/repositories/auth_repository.dart';

class MyIdeasPage extends StatefulWidget {
  const MyIdeasPage({super.key});

  @override
  State<MyIdeasPage> createState() => _MyIdeasPageState();
}

class _MyIdeasPageState extends State<MyIdeasPage> {
  bool _isLoading = true;
  String? _error;
  List<dynamic> _ideas = [];
  late AuthRepository _authRepo;
  static const String baseUrl = 'http://10.0.2.2:3444';

  @override
  void initState() {
    super.initState();
    _initMyIdeasPage();
  }

  Future<void> _initMyIdeasPage() async {
    _authRepo = await AuthRepository.create();
    _loadMyIdeas();
  }

  Future<void> _loadMyIdeas() async {
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

      print('Fetching ideas from: ${baseUrl}/ideas/user');
      print('With token: Bearer $token');

      final response = await dio.get('$baseUrl/ideas/user');
      
      print('Response status: ${response.statusCode}');
      print('Response data: ${response.data}');

      if (response.statusCode == 200) {
        final List<dynamic> ideasData = response.data;
        setState(() {
          _ideas = ideasData;
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

  Future<void> _deleteIdea(int ideaId) async {
    try {
      final token = await _authRepo.getToken();
      if (token == null) {
        context.go('/login');
        return;
      }

      final dio = Dio();
      dio.options.headers['Authorization'] = 'Bearer $token';
      dio.options.validateStatus = (status) => status! < 500;

      print('Deleting idea at: ${baseUrl}/ideas/$ideaId');
      final response = await dio.delete('$baseUrl/ideas/$ideaId');
      
      print('Delete response status: ${response.statusCode}');
      print('Delete response data: ${response.data}');

      if (response.statusCode == 200) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Idea deleted successfully')),
          );
          _loadMyIdeas(); // Reload the list
        }
      } else {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text(response.data?['message'] ?? 'Failed to delete idea')),
          );
        }
      }
    } catch (e) {
      print('Delete error: $e');
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Failed to delete idea. Please try again.')),
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
          'Delete Idea',
          style: TextStyle(color: Colors.white),
        ),
        content: const Text(
          'Are you sure you want to delete this idea? This action cannot be undone.',
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
              _deleteIdea(id);
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
              context.go('/dashboard'); // Navigate to dashboard if no route to pop
            }
          },
        ),
        title: const Text(
          'My Ideas',
          style: TextStyle(
            color: Color(0xFFFFA500),
            fontSize: 24,
            fontWeight: FontWeight.bold,
          ),
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh, color: Color(0xFFFFA500)),
            onPressed: _loadMyIdeas,
          ),
          IconButton(
            icon: const Icon(Icons.add, color: Color(0xFFFFA500)),
            onPressed: () => context.go('/submit-idea'),
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
                          onPressed: _loadMyIdeas,
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
                          'No ideas yet. Click the + button to submit your first idea!',
                          style: TextStyle(color: Colors.white, fontSize: 16),
                          textAlign: TextAlign.center,
                        ),
                      )
                    : RefreshIndicator(
                        onRefresh: _loadMyIdeas,
                        color: const Color(0xFFFFA500),
                        child: ListView.builder(
                          padding: const EdgeInsets.all(16),
                          itemCount: _ideas.length,
                          itemBuilder: (context, index) {
                            final idea = _ideas[index];
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
                                              onPressed: () => context.go('/edit-idea/${idea['id']}'),
                                            ),
                                            IconButton(
                                              icon: const Icon(Icons.delete, color: Colors.red),
                                              onPressed: () => _showDeleteConfirmation(idea['id']),
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
                                    const SizedBox(height: 8),
                                    Text(
                                      'Status: ${idea['status'] ?? 'Pending'}',
                                      style: TextStyle(
                                        color: idea['status'] == 'Approved'
                                            ? Colors.green
                                            : idea['status'] == 'Rejected'
                                                ? Colors.red
                                                : Colors.orange,
                                        fontWeight: FontWeight.bold,
                                      ),
                                    ),
                                    const SizedBox(height: 4),
                                    Text(
                                      'Created: ${DateTime.parse(idea['createdAt']).toLocal().toString().split('.')[0]}',
                                      style: const TextStyle(
                                        color: Colors.grey,
                                        fontSize: 12,
                                      ),
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