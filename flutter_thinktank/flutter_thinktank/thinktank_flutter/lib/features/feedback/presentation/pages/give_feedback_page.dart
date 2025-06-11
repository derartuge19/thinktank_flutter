import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:dio/dio.dart';
import 'package:thinktank_flutter/features/auth/data/repositories/auth_repository.dart';

enum FeedbackStatus {
  reviewed,
  approved,
  rejected,
}

class GiveFeedbackPage extends StatefulWidget {
  final String ideaId;

  const GiveFeedbackPage({
    super.key,
    required this.ideaId,
  });

  @override
  State<GiveFeedbackPage> createState() => _GiveFeedbackPageState();
}

class _GiveFeedbackPageState extends State<GiveFeedbackPage> {
  final _formKey = GlobalKey<FormState>();
  final _commentController = TextEditingController();
  FeedbackStatus _selectedStatus = FeedbackStatus.reviewed;
  
  bool _isLoading = true;
  bool _isSaving = false;
  String? _error;
  Map<String, dynamic>? _idea;
  late AuthRepository _authRepo;

  static const String baseUrl = 'http://10.0.2.2:3444';

  @override
  void initState() {
    super.initState();
    _initGiveFeedbackPage();
  }

  Future<void> _initGiveFeedbackPage() async {
    _authRepo = await AuthRepository.create();
    _loadIdea();
  }

  @override
  void dispose() {
    _commentController.dispose();
    super.dispose();
  }

  Future<void> _loadIdea() async {
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

      print('Fetching idea from: ${baseUrl}/ideas/${widget.ideaId}');
      print('With token: Bearer ${token.substring(0, 20)}...');

      final response = await dio.get('$baseUrl/ideas/${widget.ideaId}');
      
      print('Response status: ${response.statusCode}');
      print('Response data: ${response.data}');

      if (response.statusCode == 200) {
        setState(() {
          _idea = Map<String, dynamic>.from(response.data);
          _isLoading = false;
        });
      } else if (response.statusCode == 403) {
        setState(() {
          _error = 'Access denied. Only administrators can give feedback.';
          _isLoading = false;
        });
      } else {
        setState(() {
          _error = response.data?['message'] ?? 'Failed to load idea. Status: ${response.statusCode}';
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
          _error = 'Access denied. Only administrators can give feedback.';
        } else {
          _error = e.response?.data?['message'] ?? 
                  'Failed to load idea: ${e.message}';
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

  Future<void> _submitFeedback() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() {
      _isSaving = true;
      _error = null;
    });

    try {
      final token = await _authRepo.getToken();
      if (token == null) {
        context.go('/login');
        return;
      }

      // Convert enum to proper status string
      String statusString;
      switch (_selectedStatus) {
        case FeedbackStatus.reviewed:
          statusString = 'Reviewed';
          break;
        case FeedbackStatus.approved:
          statusString = 'Approved';
          break;
        case FeedbackStatus.rejected:
          statusString = 'Rejected';
          break;
      }

      final feedbackData = {
        'ideaId': int.parse(widget.ideaId),
        'comment': _commentController.text.trim(),
        'status': statusString,
      };

      final dio = Dio();
      dio.options.headers['Authorization'] = 'Bearer $token';
      dio.options.validateStatus = (status) => status! < 500;

      // First, check if there's existing feedback for this idea
      print('Checking for existing feedback for idea: ${widget.ideaId}');
      final checkResponse = await dio.get(
        '$baseUrl/feedback/admin/all',
        options: Options(headers: {'Authorization': 'Bearer $token'}),
      );

      if (checkResponse.statusCode == 200) {
        final List<dynamic> feedbackList = checkResponse.data;
        // Find feedback for this idea
        final existingFeedback = feedbackList.firstWhere(
          (f) => f['idea']?['id'].toString() == widget.ideaId,
          orElse: () => null,
        );

        Response response;
        if (existingFeedback != null) {
          // Create a payload for updating feedback (excluding ideaId)
          final updatePayload = {
            'comment': _commentController.text.trim(),
            'status': statusString,
          };

          // Update existing feedback
          print('Updating existing feedback at: ${baseUrl}/feedback/admin/${existingFeedback['id']}');
          print('With data: $updatePayload');
          
          response = await dio.patch(
            '$baseUrl/feedback/admin/${existingFeedback['id']}',
            data: updatePayload,
          );
        } else {
          // Create new feedback
          print('Creating new feedback at: ${baseUrl}/feedback/admin');
          print('With data: $feedbackData');
          
          response = await dio.post(
            '$baseUrl/feedback/admin',
            data: feedbackData,
          );
        }
        
        print('Response status: ${response.statusCode}');
        print('Response data: ${response.data}');

        if (response.statusCode == 200 || response.statusCode == 201) {
          if (mounted) {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(
                content: Text(existingFeedback != null 
                  ? 'Feedback updated successfully'
                  : 'Feedback submitted successfully'
                ),
              ),
            );
            context.go('/reviewed-ideas');
          }
        } else if (response.statusCode == 403) {
          setState(() {
            _error = 'Access denied. Only administrators can give feedback.';
            _isSaving = false;
          });
        } else {
          final errorMessage = response.data is Map 
              ? response.data['message'] 
              : 'Failed to submit feedback. Status: ${response.statusCode}';
          setState(() {
            _error = errorMessage;
            _isSaving = false;
          });
        }
      } else {
        setState(() {
          _error = 'Failed to check for existing feedback. Status: ${checkResponse.statusCode}';
          _isSaving = false;
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
          _error = 'Access denied. Only administrators can give feedback.';
        } else {
          final errorMessage = e.response?.data is Map 
              ? e.response?.data['message'] 
              : 'Failed to submit feedback: ${e.message}';
          _error = errorMessage;
        }
        _isSaving = false;
      });
    } catch (e) {
      print('Unexpected error: $e');
      setState(() {
        _error = 'An unexpected error occurred: $e';
        _isSaving = false;
      });
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
              context.go('/feedback-pool'); // Navigate to a logical previous page if no route to pop
            }
          },
        ),
        title: const Text(
          'Give Feedback',
          style: TextStyle(
            color: Color(0xFFFFA500),
            fontSize: 24,
            fontWeight: FontWeight.bold,
          ),
        ),
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
                          onPressed: _loadIdea,
                          style: ElevatedButton.styleFrom(
                            backgroundColor: const Color(0xFFFFA500),
                          ),
                          child: const Text('Retry'),
                        ),
                      ],
                    ),
                  )
                : SingleChildScrollView(
                    padding: const EdgeInsets.all(16),
                    child: Form(
                      key: _formKey,
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.stretch,
                        children: [
                          if (_idea != null) ...[
                            Card(
                              color: const Color(0xFF2A2A2A),
                              child: Padding(
                                padding: const EdgeInsets.all(16),
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Text(
                                      _idea!['title'] ?? '',
                                      style: const TextStyle(
                                        color: Colors.white,
                                        fontSize: 20,
                                        fontWeight: FontWeight.bold,
                                      ),
                                    ),
                                    const SizedBox(height: 8),
                                    Text(
                                      _idea!['description'] ?? '',
                                      style: const TextStyle(color: Colors.white70),
                                    ),
                                    if (_idea!['tags'] != null && _idea!['tags'].toString().isNotEmpty) ...[
                                      const SizedBox(height: 8),
                                      Wrap(
                                        spacing: 8,
                                        children: _idea!['tags'].toString().split(',').map<Widget>((tag) {
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
                                    Row(
                                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                      children: [
                                        Text(
                                          'Submitted by: ${_idea!['user']?['email'] ?? 'Unknown'}',
                                          style: const TextStyle(
                                            color: Colors.grey,
                                            fontSize: 12,
                                          ),
                                        ),
                                        Text(
                                          'Created: ${DateTime.parse(_idea!['createdAt']).toLocal().toString().split('.')[0]}',
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
                            ),
                            const SizedBox(height: 24),
                          ],
                          Card(
                            color: const Color(0xFF2A2A2A),
                            child: Padding(
                              padding: const EdgeInsets.all(16),
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  const Text(
                                    'Feedback Status',
                                    style: TextStyle(
                                      color: Colors.white,
                                      fontSize: 18,
                                      fontWeight: FontWeight.bold,
                                    ),
                                  ),
                                  const SizedBox(height: 16),
                                  Wrap(
                                    spacing: 8,
                                    children: FeedbackStatus.values.map((status) {
                                      final isSelected = _selectedStatus == status;
                                      return ChoiceChip(
                                        label: Text(
                                          status.name.toUpperCase(),
                                          style: TextStyle(
                                            color: isSelected ? Colors.white : Colors.white70,
                                          ),
                                        ),
                                        selected: isSelected,
                                        onSelected: (selected) {
                                          if (selected) {
                                            setState(() {
                                              _selectedStatus = status;
                                            });
                                          }
                                        },
                                        backgroundColor: const Color(0xFF3A3A3A),
                                        selectedColor: const Color(0xFFFFA500),
                                      );
                                    }).toList(),
                                  ),
                                  const SizedBox(height: 24),
                                  TextFormField(
                                    controller: _commentController,
                                    decoration: const InputDecoration(
                                      labelText: 'Feedback Comment',
                                      labelStyle: TextStyle(color: Colors.white70),
                                      border: OutlineInputBorder(),
                                      enabledBorder: OutlineInputBorder(
                                        borderSide: BorderSide(color: Colors.white24),
                                      ),
                                      focusedBorder: OutlineInputBorder(
                                        borderSide: BorderSide(color: Color(0xFFFFA500)),
                                      ),
                                    ),
                                    style: const TextStyle(color: Colors.white),
                                    maxLines: 5,
                                    validator: (value) {
                                      if (value == null || value.trim().isEmpty) {
                                        return 'Please enter your feedback';
                                      }
                                      return null;
                                    },
                                  ),
                                  const SizedBox(height: 24),
                                  ElevatedButton(
                                    onPressed: _isSaving ? null : _submitFeedback,
                                    style: ElevatedButton.styleFrom(
                                      backgroundColor: const Color(0xFFFFA500),
                                      foregroundColor: Colors.white,
                                      padding: const EdgeInsets.symmetric(vertical: 16),
                                    ),
                                    child: _isSaving
                                        ? const SizedBox(
                                            height: 20,
                                            width: 20,
                                            child: CircularProgressIndicator(
                                              color: Colors.white,
                                              strokeWidth: 2,
                                            ),
                                          )
                                        : const Text(
                                            'Submit Feedback',
                                            style: TextStyle(fontSize: 16),
                                          ),
                                  ),
                                ],
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
      ),
    );
  }
} 