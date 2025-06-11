import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:thinktank_flutter/features/auth/presentation/pages/login_page.dart';
import 'package:thinktank_flutter/features/auth/presentation/pages/register_page.dart';
import 'package:thinktank_flutter/features/auth/presentation/providers/auth_provider.dart';
import 'package:thinktank_flutter/features/dashboard/presentation/pages/dashboard_page.dart';
import 'package:thinktank_flutter/features/landing/presentation/pages/landing_page.dart';
import 'package:thinktank_flutter/features/profile/presentation/pages/profile_page.dart';
import 'package:thinktank_flutter/features/ideas/presentation/pages/my_ideas_page.dart';
import 'package:thinktank_flutter/features/ideas/presentation/pages/submit_idea_page.dart';
import 'package:thinktank_flutter/features/ideas/presentation/pages/edit_idea_page.dart';
import 'package:thinktank_flutter/features/feedback/presentation/pages/feedback_page.dart';
import 'package:thinktank_flutter/features/profile/presentation/pages/edit_profile_page.dart';
import 'package:thinktank_flutter/features/feedback/presentation/pages/feedback_pool_page.dart';
import 'package:thinktank_flutter/features/auth/data/repositories/auth_repository.dart';
import 'package:thinktank_flutter/features/feedback/presentation/pages/give_feedback_page.dart';
import 'package:thinktank_flutter/features/feedback/presentation/pages/reviewed_ideas_page.dart';
import 'package:thinktank_flutter/features/feedback/presentation/pages/edit_feedback_page.dart';

final navigatorKey = GlobalKey<NavigatorState>();

final publicRoutes = ['/', '/login', '/register'];
final protectedRoutes = [
  '/dashboard',
  '/profile',
  '/feedback-pool',
  '/edit-profile',
  '/feedback/:id',
  '/submit-idea',
  '/my-ideas',
  '/edit-idea/:id',
  '/edit-feedback/:id',
  '/give-feedback/:id',
  '/reviewed-ideas'
];

class AppRouter {
  late AuthRepository _authRepository;

  AppRouter() {
    _initAppRouter();
  }

  Future<void> _initAppRouter() async {
    _authRepository = await AuthRepository.create();
  }

  GoRouter get router => _router;

  late final GoRouter _router = GoRouter(
    navigatorKey: navigatorKey,
    initialLocation: '/',
    redirectLimit: 1,
    redirect: (context, state) async {
      final loc = state.matchedLocation;
      
      // Allow access to public routes
      if (publicRoutes.contains(loc)) {
        return null;
      }
      
      // Check authentication for protected routes
      if (protectedRoutes.any((route) => loc.startsWith(route.split(":")[0]))) {
        final token = await _authRepository.getToken();
        if (token == null) {
          return '/login';
        }
      }
      
      return null;
    },
    routes: [
      GoRoute(path: '/', builder: (context, state) => const LandingPage()),
      GoRoute(path: '/login', builder: (context, state) => const LoginPage()),
      GoRoute(path: '/register', builder: (context, state) => const RegisterPage()),
      GoRoute(path: '/dashboard', builder: (context, state) => const DashboardPage()),
      GoRoute(path: '/profile', builder: (context, state) => const ProfilePage()),
      GoRoute(path: '/feedback-pool', builder: (context, state) => const FeedbackPoolPage(), redirect: _adminGuard),
      GoRoute(path: '/edit-profile', builder: (context, state) => const EditProfilePage()),
      GoRoute(path: '/feedback/:id', builder: (context, state) => FeedbackPage(ideaId: state.pathParameters['id'] ?? '')),
      GoRoute(path: '/submit-idea', builder: (context, state) => const SubmitIdeaPage()),
      GoRoute(path: '/my-ideas', builder: (context, state) => const MyIdeasPage()),
      GoRoute(
        path: '/edit-idea/:id',
        builder: (context, state) => EditIdeaPage(ideaId: state.pathParameters['id'] ?? ''),
      ),
      GoRoute(
        path: '/give-feedback/:id',
        builder: (context, state) => GiveFeedbackPage(
          ideaId: state.pathParameters['id']!,
        ),
        redirect: _adminGuard,
      ),
      GoRoute(
        path: '/reviewed-ideas',
        builder: (context, state) => const ReviewedIdeasPage(),
        redirect: _adminGuard,
      ),
      GoRoute(
        path: '/edit-feedback/:id',
        builder: (context, state) => EditFeedbackPage(
          ideaId: state.pathParameters['id'] ?? '',
          feedbackId: state.pathParameters['id'] ?? '',
        ),
        redirect: _adminGuard,
      ),
    ],
  );

  // Admin role check middleware
  Future<String?> _adminGuard(BuildContext context, GoRouterState state) async {
    final token = await _authRepository.getToken();
    if (token == null) {
      return '/login';
    }

    // TODO: Add proper role check from JWT token
    // For now, we'll use a mock check
    final isAdmin = await _authRepository.isAdmin();
    if (!isAdmin) {
      return '/dashboard';
    }

    return null;
  }
} 