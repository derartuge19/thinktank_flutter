import 'package:dio/dio.dart';
import 'package:thinktank_flutter/core/error/exceptions.dart';
import 'package:thinktank_flutter/features/auth/domain/entities/user.dart';

abstract class AuthRemoteDataSource {
  Future<AuthResponse> login({
    required String email,
    required String password,
  });

  Future<AuthResponse> register({
    required String email,
    required String password,
    required String name,
  });

  Future<void> logout();

  Future<User> getCurrentUser();

  Future<void> updateUserProfile({
    required String name,
    String? profileImage,
  });

  Future<void> changePassword({
    required String currentPassword,
    required String newPassword,
  });
}

class AuthRemoteDataSourceImpl implements AuthRemoteDataSource {
  final Dio _dio;

  AuthRemoteDataSourceImpl(this._dio);

  @override
  Future<AuthResponse> login({
    required String email,
    required String password,
  }) async {
    try {
      final response = await _dio.post(
        '/auth/login',
        data: {
          'email': email,
          'password': password,
        },
      );
      return AuthResponse.fromJson(response.data);
    } catch (e) {
      throw ServerException('Failed to login');
    }
  }

  @override
  Future<AuthResponse> register({
    required String email,
    required String password,
    required String name,
  }) async {
    try {
      final response = await _dio.post(
        '/auth/register',
        data: {
          'email': email,
          'password': password,
          'name': name,
        },
      );
      return AuthResponse.fromJson(response.data);
    } catch (e) {
      throw ServerException('Failed to register');
    }
  }

  @override
  Future<void> logout() async {
    try {
      await _dio.post('/auth/logout');
    } catch (e) {
      throw ServerException('Failed to logout');
    }
  }

  @override
  Future<User> getCurrentUser() async {
    try {
      print('Getting current user...');
      final token = _dio.options.headers['Authorization'];
      print('Using token: ${token?.substring(0, 20)}...');
      
      final response = await _dio.get(
        '/auth/me',
        options: Options(
          validateStatus: (status) => status! < 500,
          headers: {
            'Authorization': token,
          },
        ),
      );
      
      print('Get current user response status: ${response.statusCode}');
      print('Get current user response data: ${response.data}');
      
      if (response.statusCode == 200) {
        return User.fromJson(response.data);
      } else if (response.statusCode == 401) {
        throw UnauthorizedException('Token is invalid or expired');
      } else {
        throw ServerException('Failed to get current user: ${response.statusCode} - ${response.data}');
      }
    } on DioException catch (e) {
      print('DioError in getCurrentUser: ${e.type}');
      print('DioError message: ${e.message}');
      print('DioError response: ${e.response?.data}');
      
      if (e.response?.statusCode == 401) {
        throw UnauthorizedException('Token is invalid or expired');
      }
      throw ServerException('Failed to get current user: ${e.message}');
    } catch (e) {
      print('Error in getCurrentUser: $e');
      throw ServerException('Failed to get current user: $e');
    }
  }

  @override
  Future<void> updateUserProfile({
    required String name,
    String? profileImage,
  }) async {
    try {
      await _dio.patch(
        '/auth/profile',
        data: {
          'name': name,
          if (profileImage != null) 'profileImage': profileImage,
        },
      );
    } catch (e) {
      throw ServerException('Failed to update profile');
    }
  }

  @override
  Future<void> changePassword({
    required String currentPassword,
    required String newPassword,
  }) async {
    try {
      await _dio.patch(
        '/auth/change-password',
        data: {
          'currentPassword': currentPassword,
          'newPassword': newPassword,
        },
      );
    } catch (e) {
      throw ServerException('Failed to change password');
    }
  }
} 