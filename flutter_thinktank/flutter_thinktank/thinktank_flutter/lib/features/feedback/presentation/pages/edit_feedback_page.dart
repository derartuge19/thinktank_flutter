import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:dio/dio.dart';
import 'package:thinktank_flutter/features/auth/data/repositories/auth_repository.dart';

enum FeedbackStatus {
  reviewed,
  approved,
  rejected,
}

class EditFeedbackPage extends StatefulWidget {
  final String ideaId;
  final String feedbackId;

  const EditFeedbackPage({
    Key? key,
    required this.ideaId,
    required this.feedbackId,
  }) : super(key: key);

  @override
  State<EditFeedbackPage> createState() => _EditFeedbackPageState();
}

class _EditFeedbackPageState extends State<EditFeedbackPage> {
  final _formKey = GlobalKey<FormState>();
  final _commentController = TextEditingController();
  FeedbackStatus _status = FeedbackStatus.reviewed;
  bool _isLoading = false;
  String? _error;
  static const String baseUrl = 'http://10.0.2.2:3444';
  final _dio = Dio(BaseOptions(
    baseUrl: baseUrl,
    validateStatus: (status) => status != null && status < 500,
    connectTimeout: const Duration(seconds: 10),
    receiveTimeout: const Duration(seconds: 10),
  ));
  late AuthRepository _authRepository;

  @override
  void initState() {
    super.initState();
    _initEditFeedbackPage();
  }

  Future<void> _initEditFeedbackPage() async {
    _authRepository = await AuthRepository.create();
    _loadFeedback();
  }

  @override
  void dispose() {
    _commentController.dispose();
    super.dispose();
  }

  Future<void> _loadFeedback() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });
    try {
      final token = await _authRepository.getToken();
      if (token == null) {
        if (mounted) context.go('/login');
        return;
      }

      print('Fetching feedback from: $baseUrl/feedback/admin/all');
      print('With token: Bearer ${token.substring(0, 20)}...');

      final response = await _dio.get(
        '/feedback/admin/all',
        options: Options(headers: {'Authorization': 'Bearer $token'}),
      );
      
      print('Response status: ${response.statusCode}');
      print('Response data: ${response.data}');

      if (response.statusCode == 200) {
        final List<dynamic> feedbackList = response.data;
        // Find the specific feedback by ID
        final feedback = feedbackList.firstWhere(
          (f) => f['id'].toString() == widget.feedbackId,
          orElse: () => null,
        );

        if (feedback == null) {
          setState(() {
            _error = 'Feedback not found';
            _isLoading = false;
          });
          return;
        }

        // Convert comment to string if it's not already
        final comment = feedback['comment']?.toString() ?? '';
        _commentController.text = comment;

        // Convert string status to enum
        switch (feedback['status']?.toString().toLowerCase()) {
          case 'approved':
            _status = FeedbackStatus.approved;
            break;
          case 'rejected':
            _status = FeedbackStatus.rejected;
            break;
          default:
            _status = FeedbackStatus.reviewed;
        }
        setState(() {});
      } else if (response.statusCode == 403) {
        setState(() {
          _error = 'Access denied. Only administrators can edit feedback.';
        });
      } else {
        String errorMessage;
        if (response.data is Map && response.data.containsKey('message')) {
          errorMessage = response.data['message'];
        } else if (response.data is List) {
          errorMessage = (response.data as List).map((e) => e.toString()).join(', ');
        } else {
          errorMessage = response.data?.toString() ?? 'Unknown error';
        }
        throw Exception('Failed to load feedback: $errorMessage');
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
          _error = 'Access denied. Only administrators can edit feedback.';
        } else {
          _error = e.response?.data?['message'] ?? 
                  'Failed to load feedback: ${e.message}';
        }
      });
    } catch (e) {
      print('Unexpected error: $e');
      setState(() {
        _error = 'An unexpected error occurred: $e';
      });
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }

  Future<void> _updateFeedback() async {
    if (!_formKey.currentState!.validate()) return;
    setState(() {
      _isLoading = true;
      _error = null;
    });
    try {
      final token = await _authRepository.getToken();
      if (token == null) {
        if (mounted) context.go('/login');
        return;
      }

      // Convert enum to proper status string
      String statusString;
      switch (_status) {
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
        'comment': _commentController.text.trim(),
        'status': statusString,
      };

      print('Updating feedback at: $baseUrl/feedback/admin/${widget.feedbackId}');
      print('With data: $feedbackData');

      final response = await _dio.patch(
        '/feedback/admin/${widget.feedbackId}',
        data: feedbackData,
        options: Options(headers: {'Authorization': 'Bearer $token'}),
      );
      
      print('Update response status: ${response.statusCode}');
      print('Update response data: ${response.data}');

      if (response.statusCode == 200) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Feedback updated successfully')),
          );
          context.go('/reviewed-ideas');
        }
      } else if (response.statusCode == 403) {
        setState(() {
          _error = 'Access denied. Only administrators can edit feedback.';
        });
      } else {
        throw Exception(response.data['message'] ?? 'Failed to update feedback');
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
          _error = 'Access denied. Only administrators can edit feedback.';
        } else {
          _error = e.response?.data?['message'] ?? 
                  'Failed to update feedback: ${e.message}';
        }
      });
    } catch (e) {
      print('Unexpected error: $e');
      setState(() {
        _error = 'An unexpected error occurred: $e';
      });
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Edit Feedback'),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back, color: Colors.white),
          onPressed: () {
            if (context.canPop()) {
              context.pop();
            } else {
              context.go('/reviewed-ideas'); // Navigate to a logical previous page if no route to pop
            }
          },
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _loadFeedback,
          ),
        ],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _error != null
              ? Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Text(
                        'Error: $_error',
                        style: const TextStyle(color: Colors.red),
                        textAlign: TextAlign.center,
                      ),
                      const SizedBox(height: 16),
                      ElevatedButton(
                        onPressed: _loadFeedback,
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
                        DropdownButtonFormField<FeedbackStatus>(
                          value: _status,
                          decoration: const InputDecoration(
                            labelText: 'Status',
                            border: OutlineInputBorder(),
                          ),
                          items: const [
                            DropdownMenuItem(
                              value: FeedbackStatus.reviewed,
                              child: Text('Reviewed'),
                            ),
                            DropdownMenuItem(
                              value: FeedbackStatus.approved,
                              child: Text('Approved'),
                            ),
                            DropdownMenuItem(
                              value: FeedbackStatus.rejected,
                              child: Text('Rejected'),
                            ),
                          ],
                          onChanged: (value) {
                            if (value != null) {
                              setState(() {
                                _status = value;
                              });
                            }
                          },
                          validator: (value) {
                            if (value == null) {
                              return 'Please select a status';
                            }
                            return null;
                          },
                        ),
                        const SizedBox(height: 16),
                        TextFormField(
                          controller: _commentController,
                          decoration: const InputDecoration(
                            labelText: 'Feedback Comment',
                            border: OutlineInputBorder(),
                          ),
                          maxLines: 5,
                          validator: (value) {
                            if (value == null || value.isEmpty) {
                              return 'Please enter your feedback';
                            }
                            return null;
                          },
                        ),
                        const SizedBox(height: 24),
                        ElevatedButton(
                          onPressed: _isLoading ? null : _updateFeedback,
                          style: ElevatedButton.styleFrom(
                            padding: const EdgeInsets.symmetric(vertical: 16),
                          ),
                          child: _isLoading
                              ? const SizedBox(
                                  height: 20,
                                  width: 20,
                                  child: CircularProgressIndicator(
                                    strokeWidth: 2,
                                  ),
                                )
                              : const Text('Update Feedback'),
                        ),
                      ],
                    ),
                  ),
                ),
    );
  }
} 