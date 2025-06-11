import 'package:dio/dio.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';
import 'package:pretty_dio_logger/pretty_dio_logger.dart';
import 'package:thinktank_flutter/core/error/exceptions.dart';
import 'package:thinktank_flutter/features/auth/data/repositories/auth_repository.dart';
import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';
import 'dart:io'; // Import for SocketException

class ApiClient {
  final Dio dio;
  late AuthRepository _authRepo;

  ApiClient() : dio = Dio() {
    _initApiClient();
  }

  Future<void> _initApiClient() async {
    _authRepo = await AuthRepository.create();

    dio.options.baseUrl = 'http://localhost:3444';
    dio.options.validateStatus = (status) => status! < 500;
    dio.options.connectTimeout = const Duration(seconds: 10);
    dio.options.receiveTimeout = const Duration(seconds: 10);

    dio.interceptors.add(InterceptorsWrapper(
      onRequest: (options, handler) async {
        final token = await _authRepo.getToken();
        if (token != null) {
          options.headers['Authorization'] = 'Bearer $token';
        }
        print('API Request: ${options.method} ${options.uri}');
        print('Headers: ${options.headers}');
        if (options.data != null) {
          print('Data: ${options.data}');
        }
        handler.next(options);
      },
      onResponse: (response, handler) {
        print('API Response: ${response.statusCode} ${response.requestOptions.uri}');
        print('Response data: ${response.data}');
        handler.next(response);
      },
      onError: (DioException e, handler) async {
        print('API Error: ${e.response?.statusCode} ${e.requestOptions.uri}');
        print('Error message: ${e.message}');
        if (e.response?.data != null) {
          print('Error data: ${e.response?.data}');
        }

        if (e.response?.statusCode == 401) {
          // Unauthorized - token might be expired or invalid
          print('401 Unauthorized - Attempting to refresh token or redirect to login.');
          await _authRepo.logout(); // Clear invalid token
          // You might want to navigate to login page here
          // Note: Cannot use context here. The navigation should be handled at the UI layer.
        } else if (e.response?.statusCode == 403) {
          print('403 Forbidden - User does not have access.');
        } else if (e.type == DioExceptionType.connectionTimeout) {
          print('Connection Timeout Error.');
        } else if (e.type == DioExceptionType.receiveTimeout) {
          print('Receive Timeout Error.');
        } else if (e.type == DioExceptionType.sendTimeout) {
          print('Send Timeout Error.');
        } else if (e.type == DioExceptionType.badResponse) {
          print('Bad Response Error: ${e.response?.statusCode}');
        } else if (e.type == DioExceptionType.cancel) {
          print('Request Cancelled.');
        } else if (e.type == DioExceptionType.unknown) {
          if (e.error is SocketException) {
            print('Network Error: No internet connection or server is down.');
          }
          print('Unknown Error.');
        }
        handler.next(e);
      },
    ));
  }
} 