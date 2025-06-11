class ServerException implements Exception {
  final String? message;
  ServerException([this.message]);
}

class UnauthorizedException implements Exception {
  final String? message;
  UnauthorizedException([this.message]);
}

class ForbiddenException implements Exception {
  final String? message;
  ForbiddenException([this.message]);
}

class TimeoutException implements Exception {
  final String? message;
  TimeoutException([this.message]);
}

class CacheException implements Exception {
  final String? message;
  CacheException([this.message]);
}

class ValidationException implements Exception {
  final String? message;
  ValidationException([this.message]);
}

class NetworkException implements Exception {
  final String? message;
  NetworkException([this.message]);
} 