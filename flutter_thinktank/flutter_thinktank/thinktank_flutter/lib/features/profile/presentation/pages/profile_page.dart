import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:dio/dio.dart';
import 'package:thinktank_flutter/features/auth/data/repositories/auth_repository.dart';

class ProfilePage extends StatefulWidget {
  const ProfilePage({super.key});

  @override
  State<ProfilePage> createState() => _ProfilePageState();
}

class _ProfilePageState extends State<ProfilePage> {
  bool _isLoading = true;
  String? _error;
  Map<String, dynamic>? _profile;
  late AuthRepository _authRepo;
  static const String baseUrl = 'http://10.0.2.2:3444';

  @override
  void initState() {
    super.initState();
    _initProfilePage();
  }

  Future<void> _initProfilePage() async {
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
        if (mounted) {
          context.go('/login');
        }
        return;
      }

      // Decode token to get user ID
      final decodedToken = await _authRepo.decodeToken(token);
      if (decodedToken == null || decodedToken['sub'] == null) {
        throw Exception('Invalid token: missing user ID');
      }
      final userId = decodedToken['sub'];

      print('Loading profile for user ID: $userId');

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

      // Get user info from users endpoint
      print('Fetching user data from: $baseUrl/users/$userId');
      final userResponse = await dio.get('$baseUrl/users/$userId');
      print('User response status: ${userResponse.statusCode}');
      print('User response data: ${userResponse.data}');

      if (userResponse.statusCode == 200) {
        final userData = userResponse.data;
        if (userData == null) {
          throw Exception('User data is null');
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
              'role': userData['role'] ?? 'User',
            };
            _isLoading = false;
          });
        } else if (profileResponse.statusCode == 404) {
          // Profile doesn't exist yet, create a default one
          setState(() {
            _profile = {
              'email': userData['email'],
              'name': userData['name'],
              'role': userData['role'] ?? 'User',
              'status': 'Active',
            };
            _isLoading = false;
          });
        } else {
          throw Exception('Failed to load profile: ${profileResponse.statusCode} - ${profileResponse.data}');
        }
      } else if (userResponse.statusCode == 401) {
        // Token expired or invalid
        print('Token validation failed. Response: ${userResponse.data}');
        if (mounted) {
          context.go('/login');
        }
        return;
      } else {
        throw Exception('Failed to load user data: ${userResponse.statusCode} - ${userResponse.data}');
      }
    } on DioException catch (e) {
      print('DioException: ${e.message}');
      print('Error type: ${e.type}');
      print('Error response: ${e.response?.data}');
      
      if (e.type == DioExceptionType.connectionTimeout) {
        setState(() {
          _error = 'Connection timed out. Please check your internet connection and try again.';
          _isLoading = false;
        });
      } else if (e.type == DioExceptionType.connectionError) {
        setState(() {
          _error = 'Could not connect to the server. Please make sure:\n'
                  '1. The backend server is running on port 3444\n'
                  '2. You are using the correct server URL\n'
                  '3. Your browser can access the server';
          _isLoading = false;
        });
      } else if (e.response?.statusCode == 401) {
        // Token expired or invalid
        if (mounted) {
          context.go('/login');
        }
      } else {
        setState(() {
          _error = e.response?.data?['message'] ?? 
                  'Failed to load profile: ${e.message}';
          _isLoading = false;
        });
      }
    } catch (e) {
      print('Unexpected error: $e');
      setState(() {
        _error = 'An unexpected error occurred: $e';
        _isLoading = false;
      });
    }
  }

  Future<void> _deleteAccount() async {
    try {
      final token = await _authRepo.getToken();
      if (token == null) {
        if (mounted) {
          context.go('/login');
        }
        return;
      }

      final decodedToken = await _authRepo.decodeToken(token);
      if (decodedToken == null || decodedToken['sub'] == null) {
        throw Exception('Invalid token: missing user ID');
      }
      final userId = decodedToken['sub'];

      final dio = Dio();
      final authToken = token.startsWith('Bearer ') ? token : 'Bearer $token';
      dio.options.headers['Authorization'] = authToken;
      dio.options.validateStatus = (status) => status! < 500;

      final response = await dio.delete('$baseUrl/users/$userId');
      
      if (response.statusCode == 200) {
        // Clear token and navigate to login
        await _authRepo.clearToken();
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Account deleted successfully')),
          );
          context.go('/login');
        }
      } else {
        throw Exception(response.data?['message'] ?? 'Failed to delete account');
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Failed to delete account: ${e.toString()}')),
        );
      }
    }
  }

  Future<void> _logout() async {
    try {
      await _authRepo.clearToken();
      if (mounted) {
        context.go('/login');
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Failed to logout: ${e.toString()}')),
        );
      }
    }
  }

  void _showDeleteConfirmation() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        backgroundColor: const Color(0xFF2A2A2A),
        title: const Text(
          'Delete Account',
          style: TextStyle(color: Colors.white),
        ),
        content: const Text(
          'Are you sure you want to delete your account? This action cannot be undone and all your data will be permanently deleted.',
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
              _deleteAccount();
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

  void _showLogoutConfirmation() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        backgroundColor: const Color(0xFF2A2A2A),
        title: const Text(
          'Logout',
          style: TextStyle(color: Colors.white),
        ),
        content: const Text(
          'Are you sure you want to logout?',
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
              _logout();
            },
            child: const Text(
              'Logout',
              style: TextStyle(color: Color(0xFFFFA500)),
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
              : CustomScrollView(
                  slivers: [
                    // Custom App Bar
                    SliverAppBar(
                      expandedHeight: 200,
                      pinned: true,
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
                      flexibleSpace: FlexibleSpaceBar(
                        background: Container(
                          decoration: const BoxDecoration(
                            gradient: LinearGradient(
                              begin: Alignment.topCenter,
                              end: Alignment.bottomCenter,
                              colors: [
                                Color(0xFFFFA500),
                                Color(0xFF1A1A1A),
                              ],
                            ),
                          ),
                          child: Center(
                            child: Column(
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                const SizedBox(height: 40),
                                // Profile Picture
                                CircleAvatar(
                                  radius: 50,
                                  backgroundColor: Colors.white,
                                  child: _profile?['profilePicture'] != null
                                      ? ClipOval(
                                          child: Image.network(
                                            _profile!['profilePicture'],
                                            width: 100,
                                            height: 100,
                                            fit: BoxFit.cover,
                                          ),
                                        )
                                      : const Icon(
                                          Icons.person,
                                          size: 50,
                                          color: Color(0xFFFFA500),
                                        ),
                                ),
                              ],
                            ),
                          ),
                        ),
                      ),
                      actions: [
                        IconButton(
                          icon: const Icon(Icons.edit, color: Colors.white),
                          onPressed: () => context.go('/edit-profile'),
                        ),
                      ],
                    ),

                    // Profile Content
                    SliverToBoxAdapter(
                      child: Padding(
                        padding: const EdgeInsets.all(16.0),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.stretch,
                          children: [
                            // Name
                            Text(
                              _profile?['name'] ?? 'User',
                              style: const TextStyle(
                                color: Colors.white,
                                fontSize: 24,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                            const SizedBox(height: 8),
                            // Email
                            Text(
                              _profile?['email'] ?? '',
                              style: const TextStyle(
                                color: Colors.grey,
                                fontSize: 16,
                              ),
                            ),
                            const SizedBox(height: 32),

                            // Stats Section
                            Container(
                              padding: const EdgeInsets.all(16),
                              decoration: BoxDecoration(
                                color: const Color(0xFF2A2A2A),
                                borderRadius: BorderRadius.circular(16),
                              ),
                              child: Row(
                                mainAxisAlignment: MainAxisAlignment.spaceAround,
                                children: [
                                  _buildStatItem(
                                    'Role',
                                    _profile?['role'] ?? 'User',
                                    Icons.person_outline,
                                  ),
                                  _buildStatItem(
                                    'Status',
                                    _profile?['status'] ?? 'Active',
                                    Icons.check_circle_outline,
                                  ),
                                ],
                              ),
                            ),
                            const SizedBox(height: 32),

                            // Settings Section
                            _buildSettingsSection(),

                            // Add Logout and Delete Account buttons
                            ElevatedButton.icon(
                              onPressed: _showLogoutConfirmation,
                              style: ElevatedButton.styleFrom(
                                backgroundColor: const Color(0xFFFFA500),
                                padding: const EdgeInsets.symmetric(vertical: 12),
                              ),
                              icon: const Icon(Icons.logout, color: Colors.white),
                              label: const Text(
                                'Logout',
                                style: TextStyle(
                                  color: Colors.white,
                                  fontSize: 16,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                            ),
                            const SizedBox(height: 16),
                            ElevatedButton.icon(
                              onPressed: _showDeleteConfirmation,
                              style: ElevatedButton.styleFrom(
                                backgroundColor: Colors.red,
                                padding: const EdgeInsets.symmetric(vertical: 12),
                              ),
                              icon: const Icon(Icons.delete_forever, color: Colors.white),
                              label: const Text(
                                'Delete Account',
                                style: TextStyle(
                                  color: Colors.white,
                                  fontSize: 16,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                            ),
                            const SizedBox(height: 32),
                          ],
                        ),
                      ),
                    ),
                  ],
                ),
    );
  }

  Widget _buildStatItem(String label, String value, IconData icon) {
    return Column(
      children: [
        Icon(icon, color: const Color(0xFFFFA500), size: 24),
        const SizedBox(height: 8),
        Text(
          value,
          style: const TextStyle(
            color: Colors.white,
            fontSize: 16,
            fontWeight: FontWeight.bold,
          ),
        ),
        const SizedBox(height: 4),
        Text(
          label,
          style: const TextStyle(
            color: Colors.grey,
            fontSize: 14,
          ),
        ),
      ],
    );
  }

  Widget _buildSettingsSection() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          'Settings',
          style: TextStyle(
            color: Colors.white,
            fontSize: 20,
            fontWeight: FontWeight.bold,
          ),
        ),
        const SizedBox(height: 16),
        _buildSettingsItem(
          'Change Password',
          Icons.lock_outline,
          () {
            // TODO: Navigate to change password page
          },
        ),
        _buildSettingsItem(
          'Notifications',
          Icons.notifications_outlined,
          () {
            // TODO: Navigate to notifications settings
          },
        ),
        _buildSettingsItem(
          'Privacy',
          Icons.privacy_tip_outlined,
          () {
            // TODO: Navigate to privacy settings
          },
        ),
        _buildSettingsItem(
          'Help & Support',
          Icons.help_outline,
          () {
            // TODO: Navigate to help & support
          },
        ),
      ],
    );
  }

  Widget _buildSettingsItem(
    String title,
    IconData icon,
    VoidCallback onTap, {
    bool isDestructive = false,
  }) {
    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      color: const Color(0xFF2A2A2A),
      child: ListTile(
        leading: Icon(
          icon,
          color: isDestructive ? Colors.red : const Color(0xFFFFA500),
        ),
        title: Text(
          title,
          style: TextStyle(
            color: isDestructive ? Colors.red : Colors.white,
          ),
        ),
        trailing: const Icon(
          Icons.chevron_right,
          color: Colors.grey,
        ),
        onTap: onTap,
      ),
    );
  }
} 