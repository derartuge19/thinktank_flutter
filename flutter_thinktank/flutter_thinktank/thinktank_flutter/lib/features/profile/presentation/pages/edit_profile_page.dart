import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:dio/dio.dart';
import 'package:thinktank_flutter/features/auth/data/repositories/auth_repository.dart';

class EditProfilePage extends StatefulWidget {
  const EditProfilePage({super.key});

  @override
  State<EditProfilePage> createState() => _EditProfilePageState();
}

class _EditProfilePageState extends State<EditProfilePage> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _emailController = TextEditingController();
  bool _isLoading = true;
  bool _isSaving = false;
  String? _error;
  Map<String, dynamic>? _profile;
  late AuthRepository _authRepo;
  static const String baseUrl = 'http://10.0.2.2:3444';

  @override
  void initState() {
    super.initState();
    _initEditProfilePage();
  }

  Future<void> _initEditProfilePage() async {
    _authRepo = await AuthRepository.create();
    _loadProfile();
  }

  Future<void> _loadProfile() async {
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

      print('Loading profile with token: ${token.substring(0, 20)}...');

      // Get current user info
      final dio = Dio();
      // Ensure token is properly formatted with 'Bearer ' prefix
      final authToken = token.startsWith('Bearer ') ? token : 'Bearer $token';
      dio.options.headers['Authorization'] = authToken;
      print('Using Authorization header: $authToken');
      
      dio.options.validateStatus = (status) => status! < 500;
      dio.interceptors.add(LogInterceptor(
        requestBody: true,
        responseBody: true,
        logPrint: (object) => print('Dio: $object'),
      ));

      // Get user info from auth endpoint
      print('Fetching user data from: $baseUrl/auth/me');
      final userResponse = await dio.get('$baseUrl/auth/me');
      print('User response status: ${userResponse.statusCode}');
      print('User response data: ${userResponse.data}');

      if (userResponse.statusCode == 200) {
        final userData = userResponse.data;
        if (userData == null) {
          throw Exception('User data is null');
        }
        final userId = userData['id'];
        if (userId == null) {
          throw Exception('User ID is missing from response');
        }

        print('Fetching profile data for user: $userId');
        // Get profile info
        final profileResponse = await dio.get('$baseUrl/profiles/$userId');
        print('Profile response status: ${profileResponse.statusCode}');
        print('Profile response data: ${profileResponse.data}');

        if (profileResponse.statusCode == 200) {
          setState(() {
            _profile = {
              ...profileResponse.data,
              'email': userData['email'],
              'name': userData['name'],
            };
            _nameController.text = userData['name'] ?? '';
            _emailController.text = userData['email'] ?? '';
            _isLoading = false;
          });
        } else if (profileResponse.statusCode == 404) {
          // Profile doesn't exist yet, create a default one
          setState(() {
            _profile = {
              'email': userData['email'],
              'name': userData['name'],
              'role': 'User',
            };
            _nameController.text = userData['name'] ?? '';
            _emailController.text = userData['email'] ?? '';
            _isLoading = false;
          });
        } else {
          throw Exception('Failed to load profile: ${profileResponse.statusCode} - ${profileResponse.data}');
        }
      } else if (userResponse.statusCode == 401) {
        // Token might be expired or invalid
        print('Token validation failed. Response: ${userResponse.data}');
        // Try to refresh token or redirect to login
        context.go('/login');
        return;
      } else {
        throw Exception('Failed to load user data: ${userResponse.statusCode} - ${userResponse.data}');
      }
    } catch (e) {
      print('Error loading profile: $e');
      if (e is DioException) {
        print('DioError type: ${e.type}');
        print('DioError message: ${e.message}');
        print('DioError response: ${e.response?.data}');
        if (e.response?.statusCode == 401) {
          // Token expired or invalid
          context.go('/login');
          return;
        }
        setState(() {
          _error = 'Network error: ${e.message}';
          _isLoading = false;
        });
      } else {
        setState(() {
          _error = 'Failed to load profile: ${e.toString()}';
          _isLoading = false;
        });
      }
    }
  }

  Future<void> _saveProfile() async {
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

      final dio = Dio();
      dio.options.headers['Authorization'] = 'Bearer $token';
      dio.options.validateStatus = (status) => status! < 500;

      // Get user info to get the ID
      final userResponse = await dio.get('$baseUrl/auth/me');
      if (userResponse.statusCode == 200) {
        final userId = userResponse.data['id'];

        // Update profile
        final response = await dio.patch(
          '$baseUrl/profiles/$userId',
          data: {
            'name': _nameController.text,
            // Add other profile fields here
          },
        );

        if (response.statusCode == 200) {
          if (mounted) {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(
                content: Text('Profile updated successfully'),
                backgroundColor: Colors.green,
              ),
            );
            context.pop(); // Go back to profile page
          }
        } else {
          throw Exception(response.data?['message'] ?? 'Failed to update profile');
        }
      } else {
        throw Exception('Failed to get user data');
      }
    } catch (e) {
      print('Error updating profile: $e');
      setState(() {
        _error = e.toString();
      });
    } finally {
      if (mounted) {
        setState(() {
          _isSaving = false;
        });
      }
    }
  }

  @override
  void dispose() {
    _nameController.dispose();
    _emailController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF1A1A1A),
      appBar: AppBar(
        backgroundColor: const Color(0xFF1A1A1A),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back, color: Colors.white),
          onPressed: () => context.pop(),
        ),
        title: const Text(
          'Edit Profile',
          style: TextStyle(
            color: Color(0xFFFFA500),
            fontSize: 24,
            fontWeight: FontWeight.bold,
          ),
        ),
        actions: [
          if (!_isLoading)
            TextButton(
              onPressed: _isSaving ? null : _saveProfile,
              child: _isSaving
                  ? const SizedBox(
                      width: 20,
                      height: 20,
                      child: CircularProgressIndicator(
                        color: Color(0xFFFFA500),
                        strokeWidth: 2,
                      ),
                    )
                  : const Text(
                      'Save',
                      style: TextStyle(
                        color: Color(0xFFFFA500),
                        fontSize: 16,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
            ),
        ],
      ),
      body: _isLoading
          ? const Center(
              child: CircularProgressIndicator(
                color: Color(0xFFFFA500),
              ),
            )
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
                        onPressed: _loadProfile,
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
                      crossAxisAlignment: CrossAxisAlignment.center,
                      children: [
                        // Profile Picture
                        Stack(
                          children: [
                            CircleAvatar(
                              radius: 60,
                              backgroundColor: Colors.white,
                              child: _profile?['profilePicture'] != null
                                  ? ClipOval(
                                      child: Image.network(
                                        _profile!['profilePicture'],
                                        width: 120,
                                        height: 120,
                                        fit: BoxFit.cover,
                                      ),
                                    )
                                  : const Icon(
                                      Icons.person,
                                      size: 60,
                                      color: Color(0xFFFFA500),
                                    ),
                            ),
                            Positioned(
                              bottom: 0,
                              right: 0,
                              child: Container(
                                decoration: BoxDecoration(
                                  color: const Color(0xFF2A2A2A),
                                  borderRadius: BorderRadius.circular(20),
                                  border: Border.all(
                                    color: const Color(0xFFFFA500),
                                    width: 2,
                                  ),
                                ),
                                child: IconButton(
                                  icon: const Icon(
                                    Icons.camera_alt,
                                    color: Color(0xFFFFA500),
                                  ),
                                  onPressed: () {
                                    // TODO: Implement profile picture upload
                                  },
                                ),
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 32),

                        // Form Fields
                        TextFormField(
                          controller: _nameController,
                          decoration: const InputDecoration(
                            labelText: 'Name',
                            labelStyle: TextStyle(color: Colors.white),
                            enabledBorder: OutlineInputBorder(
                              borderSide: BorderSide(color: Colors.grey),
                              borderRadius: BorderRadius.all(Radius.circular(12)),
                            ),
                            focusedBorder: OutlineInputBorder(
                              borderSide: BorderSide(color: Color(0xFFFFA500)),
                              borderRadius: BorderRadius.all(Radius.circular(12)),
                            ),
                            filled: true,
                            fillColor: Color(0xFF2A2A2A),
                          ),
                          style: const TextStyle(color: Colors.white),
                          validator: (value) {
                            if (value == null || value.isEmpty) {
                              return 'Please enter your name';
                            }
                            return null;
                          },
                        ),
                        const SizedBox(height: 16),

                        // Email field (read-only)
                        TextFormField(
                          controller: _emailController,
                          enabled: false,
                          decoration: const InputDecoration(
                            labelText: 'Email',
                            labelStyle: TextStyle(color: Colors.grey),
                            enabledBorder: OutlineInputBorder(
                              borderSide: BorderSide(color: Colors.grey),
                              borderRadius: BorderRadius.all(Radius.circular(12)),
                            ),
                            filled: true,
                            fillColor: Color(0xFF2A2A2A),
                          ),
                          style: const TextStyle(color: Colors.grey),
                        ),
                        const SizedBox(height: 24),

                        // Role display (read-only)
                        Container(
                          padding: const EdgeInsets.all(16),
                          decoration: BoxDecoration(
                            color: const Color(0xFF2A2A2A),
                            borderRadius: BorderRadius.circular(12),
                            border: Border.all(color: Colors.grey),
                          ),
                          child: Row(
                            children: [
                              const Icon(
                                Icons.person_outline,
                                color: Color(0xFFFFA500),
                              ),
                              const SizedBox(width: 16),
                              Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  const Text(
                                    'Role',
                                    style: TextStyle(
                                      color: Colors.grey,
                                      fontSize: 14,
                                    ),
                                  ),
                                  Text(
                                    _profile?['role'] ?? 'User',
                                    style: const TextStyle(
                                      color: Colors.white,
                                      fontSize: 16,
                                      fontWeight: FontWeight.bold,
                                    ),
                                  ),
                                ],
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
    );
  }
} 