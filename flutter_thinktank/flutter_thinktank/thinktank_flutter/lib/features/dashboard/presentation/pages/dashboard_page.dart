import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:dio/dio.dart';
import 'package:thinktank_flutter/features/auth/data/repositories/auth_repository.dart';

class DashboardPage extends ConsumerStatefulWidget {
  const DashboardPage({super.key});

  @override
  ConsumerState<DashboardPage> createState() => _DashboardPageState();
}

class _DashboardPageState extends ConsumerState<DashboardPage> {
  final GlobalKey<ScaffoldState> _scaffoldKey = GlobalKey<ScaffoldState>();
  bool _isLoading = true;
  String? _error;
  List<dynamic> _ideas = [];
  late AuthRepository _authRepository;
  static const String baseUrl = 'http://10.0.2.2:3444';

  @override
  void initState() {
    super.initState();
    _initDashboardPage();
  }

  Future<void> _initDashboardPage() async {
    _authRepository = await AuthRepository.create();
    _loadIdeas();
  }

  Future<void> _loadIdeas() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      final token = await _authRepository.getToken();
      if (token == null) {
        context.go('/login');
        return;
      }

      final dio = Dio();
      dio.options.headers['Authorization'] = 'Bearer $token';
      dio.options.validateStatus = (status) => status! < 500;
      dio.options.connectTimeout = const Duration(seconds: 10);
      dio.options.receiveTimeout = const Duration(seconds: 10);

      // Get approved feedback from the new public endpoint
      print('Fetching approved feedback from: ${baseUrl}/feedback/approved-ideas');
      final feedbackResponse = await dio.get('$baseUrl/feedback/approved-ideas');
      print('Feedback response status: ${feedbackResponse.statusCode}');
      print('Feedback response data: ${feedbackResponse.data}');

      if (feedbackResponse.statusCode == 200) {
        final List<dynamic> allFeedback = feedbackResponse.data;
        print('Total feedback entries found: ${allFeedback.length}');

        // The backend now returns only approved feedback, so we just process the ideas
        final List<Map<String, dynamic>> approvedIdeas = [];
        final Set<int> processedIdeaIds = {};

        for (final feedback in allFeedback) {
          final idea = feedback['idea'];
          if (idea != null) {
            final ideaId = idea['id'];
            if (!processedIdeaIds.contains(ideaId)) {
              // Create a new idea object with the feedback
              final ideaWithFeedback = Map<String, dynamic>.from(idea);
              ideaWithFeedback['feedback'] = [feedback]; // Attach the feedback for display
              approvedIdeas.add(ideaWithFeedback);
              processedIdeaIds.add(ideaId);
              print('Added approved idea to list: $ideaId');
            }
          }
        }print('Total approved ideas found: ${approvedIdeas.length}');

        setState(() {
          _ideas = approvedIdeas;
          _isLoading = false;
        });
      } else {
        setState(() {
          _error = feedbackResponse.data?['message'] ?? 'Failed to load approved feedback. Status: ${feedbackResponse.statusCode}';
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
          _error = 'Access denied. You do not have permission to access this resource.';
        } else {
          _error = e.response?.data?['message'] ??
              'Failed to load approved feedback: ${e.message}';
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

  void _openDrawer() {
    _scaffoldKey.currentState?.openDrawer();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        key: _scaffoldKey,
        backgroundColor: const Color(0xFF1A1A1A),
    drawer: Drawer(
    backgroundColor: const Color(0xFF1A1A1A),
    child: Column(
    children: [
    const DrawerHeader(
    decoration: BoxDecoration(
    color: Color(0xFF1A1A1A),
    ),
    child: Center(
    child: Text(
    'Menu',
    style: TextStyle(
    color: Color(0xFFFFA500),
    fontSize: 24,
    fontWeight: FontWeight.bold,
    ),
    ),
    ),
    ),
    _buildDrawerItem(
    icon: Icons.home,
    title: 'Dashboard',
    onTap: () {
    Navigator.pop(context);
    context.go('/dashboard');
    },
    ),
    _buildDrawerItem(
    icon: Icons.person,
    title: 'User Profile',
    onTap: () {
    Navigator.pop(context);
    context.go('/profile');
    },
    ),
    _buildDrawerItem(
    icon: Icons.add,
    title: 'Idea Submission',
    onTap: () {
    Navigator.pop(context);
    context.go('/submit-idea');
    },
    ),
    _buildDrawerItem(
    icon: Icons.rate_review,
    title: 'Feedback Pool',
    onTap: () {
    Navigator.pop(context);
    context.go('/feedback-pool');
    },
    ),
    _buildDrawerItem(
    icon: Icons.logout,
    title: 'Logout',
    onTap: () {
    Navigator.pop(context);
    context.go('/');
    },
    ),
    _buildDrawerItem(
    icon: Icons.close,
    title: 'Exit',
    onTap: () {
    Navigator.pop(context);
    // TODO: Implement app exit
    },
    ),
    ],
    ),
    ),
    appBar: AppBar(
    backgroundColor: const Color(0xFF1A1A1A),
    leading: IconButton(
    icon: const Icon(Icons.arrow_back, color: Colors.white),
    onPressed: () {
    if (_scaffoldKey.currentState?.isDrawerOpen ?? false) {Navigator.pop(context); // Close drawer
    } else if (context.canPop()) {
      context.pop(); // Go back one step
    } else {
      context.go('/'); // Navigate to landing page if no route to pop
    }
    },
    ),
      title: const Center(
        child: Text(
          'Approved Ideas',
          style: TextStyle(
            color: Color(0xFFFFA500),
            fontSize: 24,
            fontWeight: FontWeight.bold,
          ),
        ),
      ),
      actions: [
        Padding(
          padding: const EdgeInsets.only(right: 8.0),
          child: IconButton(
            onPressed: _openDrawer,
            icon: Container(
              padding: const EdgeInsets.all(8),
              decoration: BoxDecoration(
                color: const Color(0xFFFFA500).withOpacity(0.2),
                borderRadius: BorderRadius.circular(8),
              ),
              child: const Icon(
                Icons.menu,
                color: Colors.white,
              ),
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
                onPressed: _loadIdeas,
                child: const Text('Retry'),
              ),
            ],
          ),
        )
            : _ideas.isEmpty
            ? const Center(
          child: Text(
            'No approved ideas yet',
            style: TextStyle(
              color: Colors.white,
              fontSize: 18,
            ),
          ),
        )
            : RefreshIndicator(
            onRefresh: _loadIdeas,
            color: const Color(0xFFFFA500),
            child: LayoutBuilder(
            builder: (context, constraints) {
        // Always use 2 columns for mobile and tablet
        // Use 3 columns only for desktop (> 900px)
        final crossAxisCount = constraints.maxWidth < 900 ? 2 : 3;

    // Calculate card width to ensure proper grid
    final cardWidth = (constraints.maxWidth - (crossAxisCount + 1) * 12) / crossAxisCount;
    // Calculate aspect ratio based on card width
    final aspectRatio = cardWidth / (cardWidth * 1.1); // Slightly shorter height for 2 columns

    return GridView.builder(
    padding: const EdgeInsets.all(12),
    gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
    crossAxisCount: crossAxisCount,
    childAspectRatio: aspectRatio,
    crossAxisSpacing: 12,
    mainAxisSpacing: 12,
    ),
    itemCount: _ideas.length,
    itemBuilder: (context, index) {
    final idea = _ideas[index];
    final feedback = (idea['feedback'] as List?)?.last;
    return Card(
    elevation: 1,shadowColor: const Color(0xFFFFA500).withOpacity(0.1),
    color: const Color(0xFF2A2A2A),
    shape: RoundedRectangleBorder(
    borderRadius: BorderRadius.circular(6),
    side: BorderSide(
    color: const Color(0xFFFFA500).withOpacity(0.2),
    width: 1,
    ),
    ),
    child: InkWell(
    onTap: () {
    // TODO: Navigate to idea details
    },
    borderRadius: BorderRadius.circular(6),
    child: Container(
    decoration: BoxDecoration(
    borderRadius: BorderRadius.circular(6),
    color: const Color(0xFF2A2A2A),
    ),
    child: Column(
    crossAxisAlignment: CrossAxisAlignment.start,
    children: [
    // Status and Like button row
    Padding(
    padding: const EdgeInsets.fromLTRB(8, 8, 8, 4),
    child: Row(
    children: [
    Container(
    padding: const EdgeInsets.symmetric(
    horizontal: 6,
    vertical: 2,
    ),
    decoration: BoxDecoration(
    color: const Color(0xFFFFA500).withOpacity(0.15),
    borderRadius: BorderRadius.circular(4),
    ),
    child: const Text(
    'Approved',
    style: TextStyle(
    color: Color(0xFFFFA500),
    fontSize: 10,
    fontWeight: FontWeight.w600,
    ),
    ),
    ),
    const Spacer(),
    IconButton(
    icon: const Icon(
    Icons.favorite_border,
    color: Color(0xFFFFA500),
    size: 16,
    ),
    onPressed: () {
    // TODO: Implement like functionality
    },
    padding: EdgeInsets.zero,
    constraints: const BoxConstraints(),
    ),
    ],
    ),
    ),
    // Title
    Padding(
    padding: const EdgeInsets.symmetric(horizontal: 8),
    child: Text(idea['title'] ?? 'Untitled',
    style: const TextStyle(
    color: Colors.white,
    fontSize: 13,
    fontWeight: FontWeight.bold,
    height: 1.2,
    ),
    maxLines: 2,
    overflow: TextOverflow.ellipsis,
    ),
    ),
    const SizedBox(height: 4),
    // Description
    Padding(
    padding: const EdgeInsets.symmetric(horizontal: 8),
    child: Text(
    idea['description'] ?? 'No description',
    style: TextStyle(
    color: Colors.grey[400],
    fontSize: 11,
    height: 1.2,
    ),
    maxLines: 2,
    overflow: TextOverflow.ellipsis,
    ),
    ),
    const Spacer(),
    // Feedback section
    if (feedback != null)
    Container(
    margin: const EdgeInsets.fromLTRB(8, 0, 8, 6),
    padding: const EdgeInsets.all(6),
    decoration: BoxDecoration(
    color: Colors.black.withOpacity(0.2),
    borderRadius: BorderRadius.circular(4),
    ),
    child: Column(
    crossAxisAlignment: CrossAxisAlignment.start,
    children: [
    const Text(
    'Latest Feedback',
    style: TextStyle(
    color: Color(0xFFFFA500),
    fontSize: 10,
    fontWeight: FontWeight.w600,
    ),
    ),
    const SizedBox(height: 2),
    Text(
    feedback['comment'] ?? '',
    style: TextStyle(
    color: Colors.grey[300],
    fontSize: 10,
    height: 1.1,
    ),
    maxLines: 1,
    overflow: TextOverflow.ellipsis,
    ),
    ],
    ),
    ),
    // User info and timestamp
    Padding(
    padding: const EdgeInsets.fromLTRB(8, 0, 8, 8),child: Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Expanded(
          child: Row(
            children: [
              Icon(
                Icons.person_outline,
                size: 10,
                color: Colors.grey[400],
              ),
              const SizedBox(width: 2),
              Expanded(
                child: Text(
                  idea['user']?['email'] ?? 'Unknown',
                  style: TextStyle(
                    color: Colors.grey[400],
                    fontSize: 10,
                  ),
                  overflow: TextOverflow.ellipsis,
                ),
              ),
            ],
          ),
        ),
        const SizedBox(width: 4),
        Row(
          children: [
            Icon(
              Icons.access_time,
              size: 10,
              color: Colors.grey[400],
            ),
            const SizedBox(width: 2),
            Text(
              DateTime.parse(idea['createdAt'])
                  .toLocal()
                  .toString()
                  .split('.')[0],
              style: TextStyle(
                color: Colors.grey[400],
                fontSize: 10,
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
    },
    );
            },
            ),
        ),
    );
  }Widget _buildDrawerItem({
    required IconData icon,
    required String title,
    required VoidCallback onTap,
  }) {
    return InkWell(
      onTap: onTap,
      child: Container(
        width: double.infinity,
        padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 16),
        decoration: BoxDecoration(
          color: const Color(0xFF1A1A1A),
          borderRadius: BorderRadius.circular(8),
        ),
        child: Row(
          children: [
            Icon(
              icon,
              color: const Color(0xFFFFA500),
              size: 24,
            ),
            const SizedBox(width: 16),
            Text(
              title,
              style: const TextStyle(
                color: Colors.white,
                fontSize: 18,
                fontWeight: FontWeight.w500,
              ),
            ),
          ],
        ),
      ),
    );
  }
}