import 'dart:async';
import 'package:dio/dio.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:flutter/foundation.dart' show kIsWeb;
import 'dart:convert';
import 'dart:math' show min;
import 'package:jwt_decoder/jwt_decoder.dart';
import 'package:flutter/material.dart';

class AuthRepository {
  // Use different URLs based on platform
  static String get _baseUrl {
    if (kIsWeb) {
      return 'http://localhost:3444'; // For web (Chrome)
    } else {
      return 'http://10.0.2.2:3444'; // For Android emulator
    }
  }

  static const String _tokenKey = 'auth_token';
  late final SharedPreferences _prefs;
  late final Dio dio;

  // Private constructor
  AuthRepository._(this._prefs) {
    dio = Dio()
      ..options = BaseOptions(
        baseUrl: _baseUrl,
        validateStatus: (status) => status! < 500,
        connectTimeout: const Duration(seconds: 10),
        receiveTimeout: const Duration(seconds: 10),
      )
      ..interceptors.add(LogInterceptor(
        requestBody: true,
        responseBody: true,
        logPrint: (object) => print('AuthRepo Dio: $object'),
      ));
  }

  // Async factory constructor
  static Future<AuthRepository> create() async {
    final prefs = await SharedPreferences.getInstance();
    return AuthRepository._(prefs);
  }

  // Decode JWT token to get user data
  Map<String, dynamic>? _decodeToken(String token) {
    try {
      final parts = token.split('.');
      if (parts.length != 3) {
        print('Invalid token format: wrong number of parts');
        return null;
      }

      final payload = parts[1];
      final normalized = base64Url.normalize(payload);
      final decoded = utf8.decode(base64Url.decode(normalized));
      final data = json.decode(decoded);
      
      // Check token expiration
      final exp = data['exp'] as int?;
      if (exp != null) {
        final expirationTime = DateTime.fromMillisecondsSinceEpoch(exp * 1000);
        final now = DateTime.now();
        if (now.isAfter(expirationTime)) {
          print('Token has expired. Expired at: $expirationTime');
          return null;
        }
        print('Token is valid until: $expirationTime');
      }
      
      return data;
    } catch (e) {
      print('Error decoding token: $e');
      return null;
    }
  }

  // Test the connection to the backend
  Future<bool> testConnection() async {
    try {
      print("Testing connection to $_baseUrl");
      final response = await dio.get(
        '/auth/health', // or any endpoint that should always be available
        options: Options(
          validateStatus: (status) => status != null && status < 500,
        ),
      );
      print("Connection test response: ${response.statusCode}");
      return response.statusCode != null;
    } catch (e) {
      print("Connection test failed: $e");
      return false;
    }
  }

  // (Real login: POST /auth/login with { email, password } and store the returned access_token.)
  Future<String?> login(String email, String password) async {
    if (email.isEmpty || password.isEmpty) {
      throw DioException(
        requestOptions: RequestOptions(path: '/auth/login'),
        error: 'Email and password are required',
      );
    }

    try {
      print("Attempting login for email: $email");
      final response = await dio.post(
        "/auth/login",
        data: {"email": email, "password": password},
      );
      
      print("Login response status: ${response.statusCode}");
      print("Login response data: ${response.data}");
      
      // Accept both 200 and 201 status codes
      if ((response.statusCode == 200 || response.statusCode == 201) && response.data is Map) {
        final data = response.data as Map;
        final token = data["access_token"]?.toString();
        if (token != null && token.isNotEmpty) {
          await _saveToken(token);
          print("Login successful, token stored: ${token.substring(0, 20)}...");
          return token;
        } else {
          print("Login failed: Token is null or empty");
        }
      } else {
        print("Login failed: Invalid response format or status code");
      }
      return null;
    } on DioException catch (e) {
      print("Login error (DioException): ${e.message}");
      if (e.response != null) {
        print("Error response data: ${e.response?.data}");
        print("Error response status: ${e.response?.statusCode}");
      }
      if (e.type == DioExceptionType.connectionTimeout ||
          e.type == DioExceptionType.connectionError) {
        throw DioException(
          requestOptions: e.requestOptions,
          error: 'Cannot connect to the server. Please check if the server is running at $_baseUrl',
        );
      }
      rethrow;
    } catch (e) {
      print("Login error (Unexpected): $e");
      rethrow;
    }
  }

  // (Real register: POST /auth/register with { firstName, lastName, email, password } (role defaults to "user").)
  Future<String?> register(String firstName, String lastName, String email, String password) async {
    try {
      print('AuthRepository: Starting registration process');
      print('AuthRepository: Validating input...');
      
      if (firstName.isEmpty || lastName.isEmpty || email.isEmpty || password.isEmpty) {
        print('AuthRepository: Registration failed - Empty fields');
        throw Exception('All fields are required');
      }

      if (password.length < 8) {
        print('AuthRepository: Registration failed - Password too short');
        throw Exception('Password must be at least 8 characters long');
      }

      final emailRegex = RegExp(r'^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$');
      if (!emailRegex.hasMatch(email)) {
        print('AuthRepository: Registration failed - Invalid email format');
        throw Exception('Invalid email format');
      }

      print('AuthRepository: Making registration request to server...');
      final response = await dio.post(
        '/auth/register',
        data: {
          'firstName': firstName,
          'lastName': lastName,
          'email': email,
          'password': password,
          'role': 'user',
        },
      );

      print('AuthRepository: Server response status: ${response.statusCode}');
      print('AuthRepository: Server response data: ${response.data}');

      if (response.statusCode == 201 || response.statusCode == 200) {
        // After successful registration, automatically login to get the token
        print('AuthRepository: Registration successful, attempting auto-login...');
        return await login(email, password);
      } else {
        print('AuthRepository: Unexpected response status: ${response.statusCode}');
        throw Exception('Registration failed: ${response.statusMessage}');
      }
    } on DioException catch (e) {
      print('AuthRepository: DioException during registration');
      print('AuthRepository: Error type: ${e.type}');
      print('AuthRepository: Error message: ${e.message}');
      print('AuthRepository: Error response: ${e.response?.data}');
      
      if (e.type == DioExceptionType.connectionTimeout) {
        throw Exception('Connection timed out. Please check your internet connection.');
      } else if (e.type == DioExceptionType.connectionError) {
        throw Exception('Could not connect to the server. Please make sure the server is running.');
      } else if (e.response?.statusCode == 400) {
        final data = e.response?.data;
        if (data is Map && data.containsKey('message')) {
          throw Exception(data['message']);
        } else if (data is List) {
          throw Exception(data.join('\n'));
        } else {
          throw Exception('Invalid registration data. Please check your input.');
        }
      } else if (e.response?.statusCode == 409) {
        throw Exception('An account with this email already exists.');
      } else {
        throw Exception(e.response?.data?['message'] ?? 'Registration failed. Please try again.');
      }
    } catch (e) {
      print('AuthRepository: Unexpected error during registration: $e');
      rethrow;
    }
  }

  // (Retrieve token (from shared_preferences) for auth checks.)
  Future<String?> getToken() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final token = prefs.getString(_tokenKey);
      print("Retrieved token: ${token != null ? "exists" : "null"}");
      
      if (token != null) {
        print('Token preview: ${token.substring(0, min(20, token.length))}...');
        
        // Validate token
        final decodedToken = _decodeToken(token);
        if (decodedToken == null) {
          print('Token validation failed, clearing token');
          await _clearToken();
          return null;
        }
        
        // Log token details
        print('Token details:');
        print('- User ID: ${decodedToken['sub']}');
        print('- Email: ${decodedToken['email']}');
        print('- Role: ${decodedToken['role']}');
        print('- Issued at: ${DateTime.fromMillisecondsSinceEpoch(decodedToken['iat'] * 1000)}');
        print('- Expires at: ${DateTime.fromMillisecondsSinceEpoch(decodedToken['exp'] * 1000)}');
        
        // Check if token is about to expire (within 5 minutes)
        final exp = decodedToken['exp'] as int;
        final expirationTime = DateTime.fromMillisecondsSinceEpoch(exp * 1000);
        final now = DateTime.now();
        final timeUntilExpiration = expirationTime.difference(now);
        if (timeUntilExpiration.inMinutes < 5) {
          print('Warning: Token expires in ${timeUntilExpiration.inMinutes} minutes');
        }
      }
      
      return token;
    } catch (e) {
      print("Error getting token: $e");
      return null;
    }
  }

  // (Logout: remove token (i.e. clear shared_preferences).)
  Future<void> logout() async {
    try {
      await clearToken();
    } catch (e) {
      print("Error during logout: $e");
      rethrow;
    }
  }

  // Public method to clear the token
  Future<void> clearToken() async {
    try {
      await _prefs.remove(_tokenKey);
      print("Token cleared from shared preferences");
    } catch (e) {
      print("Error clearing token: $e");
      rethrow;
    }
  }

  // Private method for internal use
  Future<void> _clearToken() async {
    await clearToken();
  }

  // Check if user is admin by decoding JWT token
  Future<bool> isAdmin() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final token = prefs.getString(_tokenKey);
      if (token == null) {
        print('No token found');
        return false;
      }

      print('Checking admin role for token: ${token.substring(0, 20)}...');
      
      // Decode the token
      final decodedToken = _decodeToken(token);
      if (decodedToken == null) {
        print('Failed to decode token');
        return false;
      }

      print('Decoded token data: $decodedToken');

      // Check for role in the token
      final role = decodedToken['role']?.toString().toLowerCase();
      print('User role from token: $role');

      // For testing, also check if email contains 'admin'
      final email = decodedToken['email']?.toString().toLowerCase() ?? '';
      final isAdminByEmail = email.contains('admin');

      final isAdmin = role == 'admin' || isAdminByEmail;
      print('Is admin: $isAdmin (by role: ${role == 'admin'}, by email: $isAdminByEmail)');

      return isAdmin;
    } catch (e) {
      print('Error checking admin role: $e');
      return false;
    }
  }

  Future<void> _saveToken(String token) async {
    try {
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString(_tokenKey, token);
      print('Token saved successfully');
      
      // Verify the token was saved
      final savedToken = await getToken();
      print('Verified saved token: ${savedToken != null ? "exists" : "null"}');
      
      // Check admin status after saving
      final isAdminUser = await isAdmin();
      print('User admin status after login: $isAdminUser');
    } catch (e) {
      print('Error saving token: $e');
    }
  }

  Future<Map<String, dynamic>?> decodeToken(String token) async {
    try {
      // Remove 'Bearer ' prefix if present
      final cleanToken = token.startsWith('Bearer ') ? token.substring(7) : token;
      
      // Check if token is expired
      if (JwtDecoder.isExpired(cleanToken)) {
        print('Token is expired');
        await logout(); // Clear expired token
        return null;
      }

      // Decode the token
      final decodedToken = JwtDecoder.decode(cleanToken);
      print('Decoded token: $decodedToken');

      // Validate required claims
      if (!decodedToken.containsKey('sub')) {
        print('Token missing required claims');
        return null;
      }

      return decodedToken;
    } catch (e) {
      print('Error decoding token: $e');
      return null;
    }
  }
} 