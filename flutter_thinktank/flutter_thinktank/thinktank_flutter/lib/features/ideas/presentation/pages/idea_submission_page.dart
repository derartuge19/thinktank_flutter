import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

class IdeaSubmissionPage extends StatefulWidget {
  const IdeaSubmissionPage({Key? key}) : super(key: key);

  @override
  State<IdeaSubmissionPage> createState() => _IdeaSubmissionPageState();
}

class _IdeaSubmissionPageState extends State<IdeaSubmissionPage> {
  final _titleController = TextEditingController();
  final _descriptionController = TextEditingController();
  final _tagsController = TextEditingController();
  String? _errorMessage;
  bool _isSubmitting = false;

  void _submitIdea() async {
    setState(() {
      _errorMessage = null;
    });
    final title = _titleController.text.trim();
    final description = _descriptionController.text.trim();
    if (title.length < 3) {
      setState(() {
        _errorMessage = 'Title must be at least 3 characters.';
      });
      return;
    }
    if (description.length < 10) {
      setState(() {
        _errorMessage = 'Description must be at least 10 characters.';
      });
      return;
    }
    setState(() {
      _isSubmitting = true;
    });
    // TODO: Implement actual submission logic
    await Future.delayed(const Duration(seconds: 1));
    setState(() {
      _isSubmitting = false;
    });
    if (mounted) {
      Navigator.pop(context); // Go back after submission
    }
  }

  @override
  Widget build(BuildContext context) {
    const buttonColor = Color(0xFFFFA500);
    const backgroundColor = Color(0xFF1A1A1A);
    const errorColor = Color(0xFFFF5252);
    return Scaffold(
      backgroundColor: backgroundColor,
      body: SafeArea(
        child: SingleChildScrollView(
          child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                Row(
                  children: [
                    IconButton(
                      icon: const Icon(Icons.arrow_back, color: Colors.white),
                      onPressed: () {
                        if (context.canPop()) {
                          Navigator.pop(context); // Use Navigator.pop for consistency with existing code
                        } else {
                          // If this page is reached directly, navigate to a logical previous page
                          context.go('/my-ideas'); 
                        }
                      },
                      tooltip: 'Back',
                    ),
                    const SizedBox(width: 16),
                    const Text(
                      'Submit Your Idea',
                      style: TextStyle(
                        color: Colors.white,
                        fontWeight: FontWeight.bold,
                        fontSize: 24,
                      ),
                    ),
                  ],
                ),
                Padding(
                  padding: const EdgeInsets.symmetric(vertical: 24.0),
                  child: Image.asset(
                    'assets/images/idea_submission_image.png',
                    height: 200,
                    errorBuilder: (context, error, stackTrace) => Container(
                      color: Colors.grey,
                      height: 200,
                      child: const Center(child: Text("Image not found (fallback)", style: TextStyle(color: Colors.white))),
                    ),
                  ),
                ),
                TextField(
                  controller: _titleController,
                  style: const TextStyle(color: Colors.white),
                  decoration: InputDecoration(
                    labelText: 'Title',
                    labelStyle: const TextStyle(color: Colors.white),
                    filled: true,
                    fillColor: const Color(0xFF2A2A2A),
                    enabledBorder: OutlineInputBorder(
                      borderSide: const BorderSide(color: Colors.grey),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    focusedBorder: OutlineInputBorder(
                      borderSide: const BorderSide(color: buttonColor),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    helperText: 'Minimum 3 characters',
                    helperStyle: TextStyle(
                      color: _titleController.text.length < 3 && _titleController.text.isNotEmpty ? errorColor : Colors.grey,
                      fontSize: 12,
                    ),
                  ),
                ),
                const SizedBox(height: 16),
                TextField(
                  controller: _descriptionController,
                  style: const TextStyle(color: Colors.white),
                  maxLines: 5,
                  decoration: InputDecoration(
                    labelText: 'Description',
                    labelStyle: const TextStyle(color: Colors.white),
                    filled: true,
                    fillColor: const Color(0xFF2A2A2A),
                    enabledBorder: OutlineInputBorder(
                      borderSide: const BorderSide(color: Colors.grey),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    focusedBorder: OutlineInputBorder(
                      borderSide: const BorderSide(color: buttonColor),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    helperText: 'Minimum 10 characters',
                    helperStyle: TextStyle(
                      color: _descriptionController.text.length < 10 && _descriptionController.text.isNotEmpty ? errorColor : Colors.grey,
                      fontSize: 12,
                    ),
                  ),
                ),
                const SizedBox(height: 16),
                TextField(
                  controller: _tagsController,
                  style: const TextStyle(color: Colors.white),
                  decoration: InputDecoration(
                    labelText: 'Tags (optional)',
                    labelStyle: const TextStyle(color: Colors.white),
                    filled: true,
                    fillColor: const Color(0xFF2A2A2A),
                    enabledBorder: OutlineInputBorder(
                      borderSide: const BorderSide(color: Colors.grey),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    focusedBorder: OutlineInputBorder(
                      borderSide: const BorderSide(color: buttonColor),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    helperText: 'Separate tags with commas',
                    helperStyle: const TextStyle(color: Colors.grey, fontSize: 12),
                  ),
                ),
                const SizedBox(height: 16),
                if (_errorMessage != null)
                  Padding(
                    padding: const EdgeInsets.symmetric(vertical: 8.0),
                    child: Text(
                      _errorMessage!,
                      style: const TextStyle(
                        fontSize: 16,
                        color: errorColor,
                      ),
                    ),
                  ),
                const SizedBox(height: 8),
                SizedBox(
                  width: double.infinity,
                  height: 50,
                  child: ElevatedButton(
                    onPressed: _isSubmitting ? null : _submitIdea,
                    style: ElevatedButton.styleFrom(
                      backgroundColor: buttonColor,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(12),
                      ),
                    ),
                    child: _isSubmitting
                        ? const SizedBox(
                            width: 24,
                            height: 24,
                            child: CircularProgressIndicator(
                              color: Colors.white,
                            ),
                          )
                        : const Text(
                            'Submit',
                            style: TextStyle(
                              fontSize: 18,
                              color: Colors.white,
                            ),
                          ),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
} 