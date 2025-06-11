import 'package:dartz/dartz.dart';
import 'package:thinktank_flutter/core/error/exceptions.dart';
import 'package:thinktank_flutter/core/error/failures.dart';
import 'package:thinktank_flutter/features/auth/data/datasources/auth_remote_data_source.dart';
import 'package:thinktank_flutter/features/auth/domain/entities/user.dart';
import 'package:thinktank_flutter/features/auth/domain/repositories/auth_repository.dart';

class AuthRepositoryImpl implements AuthRepository {
  final AuthRemoteDataSource remoteDataSource;

  AuthRepositoryImpl(this.remoteDataSource);

  @override
  Future<Either<Failure, AuthResponse>> login({
    required String email,
    required String password,
  }) async {
    try {
      final response = await remoteDataSource.login(
        email: email,
        password: password,
      );
      return Right(response);
    } on ServerException catch (e) {
      return Left(ServerFailure(message: e.message ?? 'Server error occurred'));
    } on UnauthorizedException catch (e) {
      return Left(AuthFailure(message: e.message ?? 'Invalid credentials'));
    } catch (e) {
      return Left(ServerFailure(message: 'An unexpected error occurred'));
    }
  }

  @override
  Future<Either<Failure, AuthResponse>> register({
    required String email,
    required String password,
    required String name,
  }) async {
    try {
      final response = await remoteDataSource.register(
        email: email,
        password: password,
        name: name,
      );
      return Right(response);
    } on ServerException catch (e) {
      return Left(ServerFailure(message: e.message ?? 'Server error occurred'));
    } on ValidationException catch (e) {
      return Left(ValidationFailure(message: e.message ?? 'Invalid input'));
    } catch (e) {
      return Left(ServerFailure(message: 'An unexpected error occurred'));
    }
  }

  @override
  Future<Either<Failure, void>> logout() async {
    try {
      await remoteDataSource.logout();
      return const Right(null);
    } on ServerException catch (e) {
      return Left(ServerFailure(message: e.message ?? 'Server error occurred'));
    } catch (e) {
      return Left(ServerFailure(message: 'An unexpected error occurred'));
    }
  }

  @override
  Future<Either<Failure, User>> getCurrentUser() async {
    try {
      final user = await remoteDataSource.getCurrentUser();
      return Right(user);
    } on ServerException catch (e) {
      return Left(ServerFailure(message: e.message ?? 'Server error occurred'));
    } on UnauthorizedException catch (e) {
      return Left(AuthFailure(message: e.message ?? 'Not authenticated'));
    } catch (e) {
      return Left(ServerFailure(message: 'An unexpected error occurred'));
    }
  }

  @override
  Future<Either<Failure, bool>> isAuthenticated() async {
    try {
      await remoteDataSource.getCurrentUser();
      return const Right(true);
    } on UnauthorizedException {
      return const Right(false);
    } catch (e) {
      return Left(ServerFailure(message: 'An unexpected error occurred'));
    }
  }

  @override
  Future<Either<Failure, void>> updateUserProfile({
    required String name,
    String? profileImage,
  }) async {
    try {
      await remoteDataSource.updateUserProfile(
        name: name,
        profileImage: profileImage,
      );
      return const Right(null);
    } on ServerException catch (e) {
      return Left(ServerFailure(message: e.message ?? 'Server error occurred'));
    } on ValidationException catch (e) {
      return Left(ValidationFailure(message: e.message ?? 'Invalid input'));
    } catch (e) {
      return Left(ServerFailure(message: 'An unexpected error occurred'));
    }
  }

  @override
  Future<Either<Failure, void>> changePassword({
    required String currentPassword,
    required String newPassword,
  }) async {
    try {
      await remoteDataSource.changePassword(
        currentPassword: currentPassword,
        newPassword: newPassword,
      );
      return const Right(null);
    } on ServerException catch (e) {
      return Left(ServerFailure(message: e.message ?? 'Server error occurred'));
    } on ValidationException catch (e) {
      return Left(ValidationFailure(message: e.message ?? 'Invalid input'));
    } catch (e) {
      return Left(ServerFailure(message: 'An unexpected error occurred'));
    }
  }
} 