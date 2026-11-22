package com.example.data

import com.example.model.Course
import com.example.model.CourseModule
import com.example.model.Lesson
import com.example.model.LessonVisualType
import com.example.model.Quiz
import com.example.model.QuizQuestion
import com.example.model.TranscriptSnippet
import com.example.model.UserBadge
import com.example.model.VideoCheckpoint

object SampleCoursesData {

  val badges = listOf(
    UserBadge(
      id = "badge_first_lesson",
      title = "First Milestone",
      description = "Completed your very first interactive video lesson!",
      iconName = "School",
      isUnlocked = true
    ),
    UserBadge(
      id = "badge_quiz_master",
      title = "Quiz Master",
      description = "Scored 100% on a module comprehension quiz.",
      iconName = "EmojiEvents",
      isUnlocked = true
    ),
    UserBadge(
      id = "badge_streak_3",
      title = "Consistency Champion",
      description = "Maintained a 3-day continuous learning streak.",
      iconName = "LocalFireDepartment",
      isUnlocked = true
    ),
    UserBadge(
      id = "badge_checkpoint_ninja",
      title = "Checkpoint Ninja",
      description = "Answered 5 in-video checkpoint popups flawlessly.",
      iconName = "CheckCircle",
      isUnlocked = false
    ),
    UserBadge(
      id = "badge_graduate",
      title = "Course Graduate",
      description = "Earned an official certificate of completion.",
      iconName = "WorkspacePremium",
      isUnlocked = false
    ),
    UserBadge(
      id = "badge_deep_diver",
      title = "Deep Diver",
      description = "Accumulated over 60 minutes of real-time video study.",
      iconName = "Timer",
      isUnlocked = false
    )
  )

  val courses = listOf(
    // 1. Android Jetpack Compose
    Course(
      id = "course_android_compose",
      title = "Modern Android & Jetpack Compose Masterclass",
      tagline = "Build declarative, reactive UI and state-driven mobile applications",
      description = "Learn declarative UI architectures, state hoisting, LaunchedEffect lifecycles, Canvas rendering, and modern Clean Architecture with Room and Flow in Kotlin.",
      category = "Mobile Dev",
      level = "Intermediate",
      instructor = "Alex Rivera",
      instructorRole = "Staff Android Engineer",
      rating = 4.9f,
      totalLessonsCount = 4,
      totalDurationMinutes = 35,
      accentColorHex = 0xFF6366F1, // Indigo
      tags = listOf("Kotlin", "Jetpack Compose", "M3", "State", "Room"),
      modules = listOf(
        CourseModule(
          id = "mod_compose_state",
          courseId = "course_android_compose",
          title = "Module 1: Declarative State & Recomposition",
          description = "Mastering mutableStateOf, derivedStateOf, and SideEffects",
          order = 1,
          lessons = listOf(
            Lesson(
              id = "lesson_state_hoisting",
              courseId = "course_android_compose",
              moduleId = "mod_compose_state",
              title = "State Hoisting & Unidirectional Data Flow",
              description = "Learn how to hoist state to caller composables, prevent unnecessary recompositions, and decouple business logic.",
              durationSeconds = 120,
              visualType = LessonVisualType.CODE_EDITOR,
              codePreview = """@Composable
fun CounterScreen(
    count: Int,
    onIncrement: () -> Unit
) {
    Button(onClick = onIncrement) {
        Text("Count: ${'$'}count")
    }
}""",
              checkpoints = listOf(
                VideoCheckpoint(
                  timestampSeconds = 35,
                  title = "Checkpoint: State Hoisting Core",
                  question = "What is the primary benefit of State Hoisting in Jetpack Compose?",
                  options = listOf(
                    "It makes composables stateless, reusable, and easy to test",
                    "It automatically saves state across configuration changes without Room",
                    "It speeds up network HTTP requests",
                    "It converts imperative Views to XML layout files"
                  ),
                  correctOptionIndex = 0,
                  explanation = "State hoisting decouples UI presentation from state management, making composables stateless and straightforward to test and preview."
                ),
                VideoCheckpoint(
                  timestampSeconds = 85,
                  title = "Checkpoint: rememberSaveable",
                  question = "When should you use rememberSaveable instead of remember?",
                  options = listOf(
                    "Only when drawing custom graphics with Canvas",
                    "When state needs to survive Activity recreation (e.g. screen rotation)",
                    "When calling suspend functions inside ViewModel",
                    "When defining TopAppBar navigation icons"
                  ),
                  correctOptionIndex = 1,
                  explanation = "rememberSaveable stores values in the Android SavedInstanceState Bundle to survive configuration changes and process death."
                )
              ),
              transcripts = listOf(
                TranscriptSnippet(
                  startSecond = 0,
                  endSecond = 30,
                  speaker = "Alex Rivera",
                  text = "Welcome to Lesson 1. In declarative UI frameworks like Jetpack Compose, UI is a direct mathematical function of state. When state changes, Compose intelligently recalculates and redraws only the modified nodes.",
                  codeSnippet = "// UI = f(state)"
                ),
                TranscriptSnippet(
                  startSecond = 31,
                  endSecond = 65,
                  speaker = "Alex Rivera",
                  text = "State hoisting is the design pattern where you move state up to a caller to make components stateless. Look at our CounterScreen example: it receives an immutable count and an onIncrement lambda.",
                  codeSnippet = "fun Counter(count: Int, onIncrement: () -> Unit)"
                ),
                TranscriptSnippet(
                  startSecond = 66,
                  endSecond = 100,
                  speaker = "Alex Rivera",
                  text = "For local UI state that must survive rotation, we use rememberSaveable. Let's see how passing a Saver or primitive type prevents data loss when the user switches orientations.",
                  codeSnippet = "var query by rememberSaveable { mutableStateOf(\"\") }"
                ),
                TranscriptSnippet(
                  startSecond = 101,
                  endSecond = 120,
                  speaker = "Alex Rivera",
                  text = "In the next lesson, we will explore SideEffects and LaunchedEffect for coroutine lifecycles. Take a moment to review the code and complete the interactive checkpoint!",
                  codeSnippet = null
                )
              ),
              takeaways = listOf(
                "UI in Compose is a direct representation of state: UI = f(State).",
                "State hoisting makes composables idempotent, testable, and previewable.",
                "Use rememberSaveable for transient UI states that must survive screen rotations."
              )
            ),
            Lesson(
              id = "lesson_side_effects",
              courseId = "course_android_compose",
              moduleId = "mod_compose_state",
              title = "SideEffects & Coroutine Lifecycles",
              description = "Harnessing LaunchedEffect, rememberCoroutineScope, and DisposableEffect safely.",
              durationSeconds = 150,
              visualType = LessonVisualType.ARCHITECTURE_CANVAS,
              codePreview = """LaunchedEffect(userId) {
    viewModel.loadProfile(userId)
}
DisposableEffect(lifecycle) {
    val observer = LifecycleEventObserver { ... }
    lifecycle.addObserver(observer)
    onDispose { lifecycle.removeObserver(observer) }
}""",
              checkpoints = listOf(
                VideoCheckpoint(
                  timestampSeconds = 50,
                  title = "Checkpoint: LaunchedEffect Keys",
                  question = "What happens when a key passed to LaunchedEffect changes?",
                  options = listOf(
                    "The current running coroutine is cancelled and a new one is launched",
                    "The app crashes with an IllegalStateException",
                    "The key is ignored until the activity restarts",
                    "It writes state directly into the SQLite database"
                  ),
                  correctOptionIndex = 0,
                  explanation = "LaunchedEffect cancels its active coroutine and relaunches it whenever any of its key parameters change value."
                )
              ),
              transcripts = listOf(
                TranscriptSnippet(
                  startSecond = 0,
                  endSecond = 45,
                  speaker = "Alex Rivera",
                  text = "Welcome back. In Compose, composables can recompose frequently. Never perform non-pure operations like database queries or network requests directly in the Composable body!",
                  codeSnippet = "// ❌ BAD: repository.fetchUser() in body"
                ),
                TranscriptSnippet(
                  startSecond = 46,
                  endSecond = 95,
                  speaker = "Alex Rivera",
                  text = "Instead, we use LaunchedEffect. It creates a coroutine scope tied to the composable's composition lifecycle. If the composable leaves the tree, the coroutine automatically cancels.",
                  codeSnippet = "LaunchedEffect(key1) { ... }"
                ),
                TranscriptSnippet(
                  startSecond = 96,
                  endSecond = 150,
                  speaker = "Alex Rivera",
                  text = "When registering callbacks or listeners, always use DisposableEffect with an onDispose block to prevent memory leaks.",
                  codeSnippet = "DisposableEffect(Unit) { onDispose { ... } }"
                )
              ),
              takeaways = listOf(
                "Never run asynchronous or non-idempotent operations directly in composable body.",
                "LaunchedEffect restarts whenever provided keys change.",
                "DisposableEffect guarantees cleanup logic in onDispose when leaving the composition."
              )
            )
          )
        ),
        CourseModule(
          id = "mod_compose_advanced",
          courseId = "course_android_compose",
          title = "Module 2: Custom Graphics & Canvas Animation",
          description = "Drawing custom UI, bezier curves, and physics-based springs",
          order = 2,
          lessons = listOf(
            Lesson(
              id = "lesson_canvas_drawing",
              courseId = "course_android_compose",
              moduleId = "mod_compose_advanced",
              title = "Custom Canvas & Vector Paths",
              description = "Explore DrawScope coordinates, path drawing, and gradient shader fills.",
              durationSeconds = 140,
              visualType = LessonVisualType.DESIGN_SYSTEM_PREVIEW,
              codePreview = """Canvas(modifier = Modifier.fillMaxSize()) {
    val radius = size.minDimension / 2
    drawCircle(
        brush = Brush.radialGradient(listOf(Color.Cyan, Color.Blue)),
        radius = radius,
        center = center
    )
}""",
              checkpoints = listOf(
                VideoCheckpoint(
                  timestampSeconds = 60,
                  title = "Checkpoint: DrawScope Coordinate Space",
                  question = "In DrawScope, what does size.width and size.height represent?",
                  options = listOf(
                    "The current drawing area bounds of this specific Canvas component",
                    "The full hardware screen resolution in physical pixels",
                    "The DP dimension of the root Activity window",
                    "The size of the navigation bar in pixels"
                  ),
                  correctOptionIndex = 0,
                  explanation = "Inside DrawScope, size represents the exact measured bounding box of the Canvas composable."
                )
              ),
              transcripts = listOf(
                TranscriptSnippet(
                  startSecond = 0,
                  endSecond = 40,
                  speaker = "Alex Rivera",
                  text = "Canvas in Jetpack Compose grants hardware-accelerated drawing capabilities. Everything inside DrawScope uses local coordinates.",
                  codeSnippet = "Canvas(modifier = Modifier.size(200.dp)) { ... }"
                ),
                TranscriptSnippet(
                  startSecond = 41,
                  endSecond = 90,
                  speaker = "Alex Rivera",
                  text = "We can draw smooth gradients, animated rings, and custom chart bars. Notice how we reference size.minDimension for proportional scaling.",
                  codeSnippet = "drawArc(brush, startAngle, sweepAngle, false)"
                ),
                TranscriptSnippet(
                  startSecond = 91,
                  endSecond = 140,
                  speaker = "Alex Rivera",
                  text = "Combined with Animatable float transitions, you can create rich fluid interactive visual indicators.",
                  codeSnippet = "val progress by animateFloatAsState(targetValue)"
                )
              ),
              takeaways = listOf(
                "DrawScope handles hardware-accelerated rendering directly on the GPU.",
                "Always use DrawScope.size rather than external screen dimensions.",
                "Pair Canvas with animateFloatAsState for smooth 60fps animations."
              )
            ),
            Lesson(
              id = "lesson_clean_architecture",
              courseId = "course_android_compose",
              moduleId = "mod_compose_advanced",
              title = "Reactive MVVM with Room and StateFlow",
              description = "Connecting reactive local SQLite databases to Jetpack Compose UI state.",
              durationSeconds = 160,
              visualType = LessonVisualType.TERMINAL_EXECUTION,
              codePreview = """val uiState: StateFlow<UiState> = repository.getItems()
    .map { UiState.Success(it) }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Loading
    )""",
              checkpoints = listOf(
                VideoCheckpoint(
                  timestampSeconds = 70,
                  title = "Checkpoint: WhileSubscribed 5000ms",
                  question = "Why is SharingStarted.WhileSubscribed(5000) recommended for StateFlow in ViewModels?",
                  options = listOf(
                    "It keeps upstream flow active for 5s during brief configuration changes (like rotation) before stopping",
                    "It forces Room queries to delay by 5 seconds",
                    "It limits API calls to 5 per minute",
                    "It caches results on the device disk forever"
                  ),
                  correctOptionIndex = 0,
                  explanation = "WhileSubscribed(5000) prevents restarting data collection during screen rotations while avoiding wasteful memory usage when the app is in background."
                )
              ),
              transcripts = listOf(
                TranscriptSnippet(
                  startSecond = 0,
                  endSecond = 50,
                  speaker = "Alex Rivera",
                  text = "In this lesson, we connect Room Database to ViewModel using Kotlin Flow and expose a single immutable UI state.",
                  codeSnippet = "interface ItemDao { @Query(...) fun get(): Flow<List<T>> }"
                ),
                TranscriptSnippet(
                  startSecond = 51,
                  endSecond = 110,
                  speaker = "Alex Rivera",
                  text = "By leveraging stateIn with WhileSubscribed, your UI smoothly survives screen rotations and only consumes battery when actively visible.",
                  codeSnippet = "flow.stateIn(viewModelScope, WhileSubscribed(5000), Initial)"
                ),
                TranscriptSnippet(
                  startSecond = 111,
                  endSecond = 160,
                  speaker = "Alex Rivera",
                  text = "In your Composable, collect with collectAsStateWithLifecycle() to ensure optimal lifecycle-aware flow collection.",
                  codeSnippet = "val state by viewModel.uiState.collectAsStateWithLifecycle()"
                )
              ),
              takeaways = listOf(
                "Room DAO queries returning Flow emit fresh data automatically whenever the database updates.",
                "WhileSubscribed(5000) is the Android gold standard for StateFlow in ViewModels.",
                "Always collect in UI using collectAsStateWithLifecycle()."
              )
            )
          )
        )
      ),
      finalQuiz = Quiz(
        id = "quiz_compose_mastery",
        courseId = "course_android_compose",
        title = "Compose & Kotlin Architecture Certification Quiz",
        description = "Test your mastery of unidirectional data flow, side effects, canvas rendering, and reactive state.",
        passingPercentage = 75,
        xpReward = 200,
        timeLimitMinutes = 6,
        questions = listOf(
          QuizQuestion(
            id = "q_comp_1",
            prompt = "Which composable effect should you use to execute a coroutine whenever a specific ID changes?",
            codeSnippet = "// Example:\nLaunchedEffect(userId) {\n    fetchUserData(userId)\n}",
            options = listOf(
              "LaunchedEffect(userId)",
              "rememberCoroutineScope()",
              "SideEffect { }",
              "DisposableEffect(Unit)"
            ),
            correctAnswerIndex = 0,
            explanation = "LaunchedEffect accepts key arguments; when a key changes, it cancels and restarts the coroutine block."
          ),
          QuizQuestion(
            id = "q_comp_2",
            prompt = "What is the purpose of rememberSaveable over standard remember in Compose?",
            codeSnippet = null,
            options = listOf(
              "It automatically encrypts state variables",
              "It persists state across Android configuration changes and process recreation",
              "It converts the variable into a Room database entity",
              "It shares the value globally with other Android apps"
            ),
            correctAnswerIndex = 1,
            explanation = "rememberSaveable leverages the SavedStateRegistry to survive configuration changes like screen rotations."
          ),
          QuizQuestion(
            id = "q_comp_3",
            prompt = "Which lifecycle parameter stops the upstream cold Flow 5 seconds after the last UI subscriber disconnects?",
            codeSnippet = "repository.data.stateIn(\n    scope = viewModelScope,\n    started = /* ? */,\n    initialValue = emptyList()\n)",
            options = listOf(
              "SharingStarted.WhileSubscribed(5000)",
              "SharingStarted.Eagerly",
              "SharingStarted.Lazily",
              "SharingStarted.Never"
            ),
            correctAnswerIndex = 0,
            explanation = "SharingStarted.WhileSubscribed(5000) provides a 5-second buffer to bridge screen rotation transitions without dropping the cache."
          ),
          QuizQuestion(
            id = "q_comp_4",
            prompt = "What happens if you modify a normal non-State Kotlin var inside a Composable function body?",
            codeSnippet = "var counter = 0 // Standard Kotlin var\nButton(onClick = { counter++ }) { Text(\"\$counter\") }",
            options = listOf(
              "The UI will not reliably recompose or reflect the new count",
              "The app will trigger an immediate compiler error",
              "Compose automatically wraps it in a State object",
              "The Activity will restart"
            ),
            correctAnswerIndex = 0,
            explanation = "Compose can only track changes and trigger recompositions on observable State objects (such as mutableStateOf)."
          )
        )
      )
    ),

    // 2. Full-Stack Python & AI Systems
    Course(
      id = "course_python_ai",
      title = "Full-Stack Python & AI Agent Architecture",
      tagline = "Design LLM workflows, FastAPI microservices, and autonomous tool-calling agents",
      description = "Build production-grade AI systems with LangChain, Gemini API, FastAPI async backends, semantic search embeddings, and vector indexing.",
      category = "AI & Data",
      level = "Advanced",
      instructor = "Dr. Elena Rostova",
      instructorRole = "AI Research Lead",
      rating = 4.85f,
      totalLessonsCount = 3,
      totalDurationMinutes = 28,
      accentColorHex = 0xFF06B6D4, // Cyan
      tags = listOf("Python", "FastAPI", "Gemini", "Embeddings", "Agents"),
      modules = listOf(
        CourseModule(
          id = "mod_python_agents",
          courseId = "course_python_ai",
          title = "Module 1: Agentic Workflows & Tool Calling",
          description = "Designing structured outputs, tool declarations, and reasoning loops",
          order = 1,
          lessons = listOf(
            Lesson(
              id = "lesson_tool_calling",
              courseId = "course_python_ai",
              moduleId = "mod_python_agents",
              title = "Autonomous Tool Calling & Structured Schemas",
              description = "Declare function tools with JSON schemas and orchestrate multi-step execution loops.",
              durationSeconds = 130,
              visualType = LessonVisualType.TERMINAL_EXECUTION,
              codePreview = """def get_weather(location: str) -> dict:
    return {"temp": 72, "unit": "fahrenheit"}

tools = [get_weather]
model = genai.GenerativeModel('gemini-2.0-flash', tools=tools)
response = model.generate_content("What is the weather in Tokyo?")""",
              checkpoints = listOf(
                VideoCheckpoint(
                  timestampSeconds = 45,
                  title = "Checkpoint: Tool Declarations",
                  question = "How does an LLM know which arguments to pass when invoking a declared function tool?",
                  options = listOf(
                    "Via the parameter names, types, and docstring descriptions defined in the tool schema",
                    "By guessing random integers until successful",
                    "By executing the Python interpreter directly on the client",
                    "Through an encrypted SSH socket"
                  ),
                  correctOptionIndex = 0,
                  explanation = "The model reads the schema definition, including parameter type descriptions and docstrings, to generate valid function call arguments."
                )
              ),
              transcripts = listOf(
                TranscriptSnippet(
                  startSecond = 0,
                  endSecond = 40,
                  speaker = "Dr. Elena Rostova",
                  text = "Welcome to AI Agent architectures. Autonomous agents extend language models by allowing them to interact with the external world through declared tools.",
                  codeSnippet = "tools = [search_database, send_email]"
                ),
                TranscriptSnippet(
                  startSecond = 41,
                  endSecond = 85,
                  speaker = "Dr. Elena Rostova",
                  text = "When the model decides a tool is required, it returns a structured FunctionCall object rather than natural language text.",
                  codeSnippet = "FunctionCall(name='search_database', args={'query': 'reports'})"
                ),
                TranscriptSnippet(
                  startSecond = 86,
                  endSecond = 130,
                  speaker = "Dr. Elena Rostova",
                  text = "Your application executes the tool locally and passes the FunctionResponse back to the model to synthesize the final answer.",
                  codeSnippet = "response = chat.send_message(tool_response)"
                )
              ),
              takeaways = listOf(
                "Tool calling enables LLMs to access live data and trigger deterministic software actions.",
                "The model produces structured JSON arguments matching your typed function signature.",
                "Execution occurs securely on your application runtime, not inside the neural network itself."
              )
            ),
            Lesson(
              id = "lesson_vector_embeddings",
              courseId = "course_python_ai",
              moduleId = "mod_python_agents",
              title = "Vector Embeddings & Semantic Search",
              description = "Transform unstructured documents into dense high-dimensional vectors for cosine similarity search.",
              durationSeconds = 120,
              visualType = LessonVisualType.DATA_FLOW_VIZ,
              codePreview = """embedding = genai.embed_content(
    model="models/text-embedding-004",
    content="Semantic document retrieval"
)
similarity = cosine_similarity(query_vec, doc_vec)""",
              checkpoints = listOf(
                VideoCheckpoint(
                  timestampSeconds = 40,
                  title = "Checkpoint: Vector Distance",
                  question = "Which metric is most commonly used to compute semantic proximity between normalized text embeddings?",
                  options = listOf(
                    "Cosine Similarity (or Cosine Distance)",
                    "Hamming Binary XOR",
                    "Standard Levenshtein String Edit Distance",
                    "ASCII sum difference"
                  ),
                  correctOptionIndex = 0,
                  explanation = "Cosine similarity measures the angle between high-dimensional vector embeddings, effectively capturing semantic meaning regardless of text length."
                )
              ),
              transcripts = listOf(
                TranscriptSnippet(
                  startSecond = 0,
                  endSecond = 40,
                  speaker = "Dr. Elena Rostova",
                  text = "Vector embeddings map text passages into high-dimensional geometric spaces where semantically related concepts cluster together.",
                  codeSnippet = "vec = embed(text) # e.g. 768 dimensions"
                ),
                TranscriptSnippet(
                  startSecond = 41,
                  endSecond = 80,
                  speaker = "Dr. Elena Rostova",
                  text = "Unlike keyword search which fails on synonyms, semantic search retrieves documents based on true conceptual intent.",
                  codeSnippet = "similarity = dot(v1, v2) / (|v1| * |v2|)"
                ),
                TranscriptSnippet(
                  startSecond = 81,
                  endSecond = 120,
                  speaker = "Dr. Elena Rostova",
                  text = "In the next module, we will explore RAG (Retrieval-Augmented Generation) pipelines and dynamic context injection.",
                  codeSnippet = null
                )
              ),
              takeaways = listOf(
                "Embeddings capture semantic nuance, resolving synonym and paraphrase ambiguities.",
                "Cosine similarity evaluates the angular alignment between dense vector representations.",
                "Vector indexing powers high-speed semantic retrieval across millions of knowledge chunks."
              )
            ),
            Lesson(
              id = "lesson_fastapi_microservice",
              courseId = "course_python_ai",
              moduleId = "mod_python_agents",
              title = "High-Performance Asynchronous FastAPI",
              description = "Build non-blocking streaming SSE endpoints for real-time AI token delivery.",
              durationSeconds = 135,
              visualType = LessonVisualType.ARCHITECTURE_CANVAS,
              codePreview = """@app.post("/stream")
async def stream_ai(prompt: PromptRequest):
    async def event_generator():
        async for chunk in model.generate_stream(prompt.text):
            yield f"data: {chunk.text}\n\n"
    return StreamingResponse(event_generator(), media_type="text/event-stream")""",
              checkpoints = listOf(
                VideoCheckpoint(
                  timestampSeconds = 55,
                  title = "Checkpoint: Server-Sent Events (SSE)",
                  question = "What is the advantage of using StreamingResponse (SSE) for AI generation endpoints?",
                  options = listOf(
                    "Users see words streamed in real-time with ultra-low Time-To-First-Token (TTFT)",
                    "It converts text into MP4 video format automatically",
                    "It bypasses all network firewalls completely",
                    "It eliminates the need for any backend server"
                  ),
                  correctOptionIndex = 0,
                  explanation = "Streaming tokens via SSE dramatically improves perceived latency because users begin reading immediately rather than waiting for full generation completion."
                )
              ),
              transcripts = listOf(
                TranscriptSnippet(
                  startSecond = 0,
                  endSecond = 50,
                  speaker = "Dr. Elena Rostova",
                  text = "FastAPI is built on Starlette and Pydantic, offering native asynchronous request handling via Python's asyncio event loop.",
                  codeSnippet = "async def endpoint() -> Response:"
                ),
                TranscriptSnippet(
                  startSecond = 51,
                  endSecond = 90,
                  speaker = "Dr. Elena Rostova",
                  text = "By yielding chunks from an async generator, we stream tokens through HTTP Server-Sent Events directly to our mobile or web clients.",
                  codeSnippet = "StreamingResponse(generator, media_type='text/event-stream')"
                ),
                TranscriptSnippet(
                  startSecond = 91,
                  endSecond = 135,
                  speaker = "Dr. Elena Rostova",
                  text = "This pattern minimizes perceived latency and delivers responsive, delightful real-time user experiences.",
                  codeSnippet = null
                )
              ),
              takeaways = listOf(
                "FastAPI leverages asyncio for high concurrency with minimal RAM overhead.",
                "Server-Sent Events (SSE) enable low-latency token streaming for generative AI.",
                "Pydantic models enforce strict compile-time and runtime payload validation."
              )
            )
          )
        )
      ),
      finalQuiz = Quiz(
        id = "quiz_python_ai_cert",
        courseId = "course_python_ai",
        title = "AI Systems & Microservices Assessment",
        description = "Evaluate your knowledge of tool calling, vector search, embeddings, and async streaming.",
        passingPercentage = 75,
        xpReward = 220,
        timeLimitMinutes = 5,
        questions = listOf(
          QuizQuestion(
            id = "q_py_1",
            prompt = "What format does an LLM use when returning a function call invocation request?",
            codeSnippet = null,
            options = listOf(
              "Structured JSON containing the function name and dictionary of arguments",
              "Raw unparsed binary bytecode",
              "Compiled C++ shared library pointers",
              "SQL INSERT statements"
            ),
            correctAnswerIndex = 0,
            explanation = "The model returns structured JSON with the function name and argument dictionary matching the declared tool schema."
          ),
          QuizQuestion(
            id = "q_py_2",
            prompt = "Why is cosine similarity preferred over Euclidean distance for high-dimensional text embeddings?",
            codeSnippet = null,
            options = listOf(
              "It measures orientation/angle rather than magnitude, preventing bias toward document word length",
              "It only works on 2D coordinates",
              "It requires no floating-point math",
              "It is built into HTTP headers"
            ),
            correctAnswerIndex = 0,
            explanation = "Cosine similarity focuses on directional alignment, so long and short texts discussing the same subject maintain high similarity."
          ),
          QuizQuestion(
            id = "q_py_3",
            prompt = "Which media type header is standard for streaming real-time tokens over HTTP Server-Sent Events?",
            codeSnippet = "StreamingResponse(generator, media_type=\"/* ? */\")",
            options = listOf(
              "text/event-stream",
              "application/json-stream",
              "video/mp4",
              "multipart/form-data"
            ),
            correctAnswerIndex = 0,
            explanation = "text/event-stream is the official MIME standard for Server-Sent Events (SSE)."
          )
        )
      )
    ),

    // 3. UI/UX Design Systems & Motion
    Course(
      id = "course_design_systems",
      title = "Material Design 3 & Motion Systems",
      tagline = "Craft accessible, expressive, and dynamic cross-platform design systems",
      description = "Master color token hierarchies, typography pairing, spatial 8dp grids, micro-interactions, and accessibility standards.",
      category = "Design",
      level = "Beginner",
      instructor = "Sophia Chen",
      instructorRole = "Principal Design Architect",
      rating = 4.95f,
      totalLessonsCount = 2,
      totalDurationMinutes = 20,
      accentColorHex = 0xFFF59E0B, // Amber
      tags = listOf("Design Systems", "M3", "Tokens", "Accessibility", "Motion"),
      modules = listOf(
        CourseModule(
          id = "mod_m3_tokens",
          courseId = "course_design_systems",
          title = "Module 1: Color Roles & Spatial Tokens",
          description = "Dynamic theming, container roles, and contrast ratios",
          order = 1,
          lessons = listOf(
            Lesson(
              id = "lesson_color_roles",
              courseId = "course_design_systems",
              moduleId = "mod_m3_tokens",
              title = "Material 3 Semantic Color Roles",
              description = "Understand the purpose of Primary, Secondary, Tertiary, Surface, and Container color tokens.",
              durationSeconds = 110,
              visualType = LessonVisualType.DESIGN_SYSTEM_PREVIEW,
              codePreview = """// M3 Semantic Tokens
Primary -> Key brand color (FABs, primary actions)
PrimaryContainer -> Lower-emphasis filled backgrounds
Surface -> Page background and card planes
OnSurface -> High-contrast foreground text/icons""",
              checkpoints = listOf(
                VideoCheckpoint(
                  timestampSeconds = 40,
                  title = "Checkpoint: Accessibility Contrast",
                  question = "What is the minimum WCAG AA contrast ratio required for normal body text against its background?",
                  options = listOf(
                    "4.5:1",
                    "2.0:1",
                    "10:1",
                    "1.5:1"
                  ),
                  correctOptionIndex = 0,
                  explanation = "WCAG 2.1 AA mandates a minimum contrast ratio of 4.5:1 for standard body text and 3:1 for large text (18pt+)."
                )
              ),
              transcripts = listOf(
                TranscriptSnippet(
                  startSecond = 0,
                  endSecond = 35,
                  speaker = "Sophia Chen",
                  text = "Welcome to Material 3 Design Systems. Instead of hardcoding hex colors, modern design systems utilize semantic color roles.",
                  codeSnippet = "MaterialTheme.colorScheme.primaryContainer"
                ),
                TranscriptSnippet(
                  startSecond = 36,
                  endSecond = 75,
                  speaker = "Sophia Chen",
                  text = "Semantic tokens adapt seamlessly between light and dark modes, ensuring consistent contrast and visual hierarchy.",
                  codeSnippet = "Text(color = MaterialTheme.colorScheme.onSurface)"
                ),
                TranscriptSnippet(
                  startSecond = 76,
                  endSecond = 110,
                  speaker = "Sophia Chen",
                  text = "Always pair container colors with their corresponding 'on-' colors to maintain accessible readability.",
                  codeSnippet = "Surface(color = primaryContainer, contentColor = onPrimaryContainer)"
                )
              ),
              takeaways = listOf(
                "Semantic tokens separate visual intent from raw hex values.",
                "Always pair 'On' tokens (e.g. onPrimary) with their base containers.",
                "Ensure at least 4.5:1 contrast ratio for all legible text content."
              )
            ),
            Lesson(
              id = "lesson_motion_principles",
              courseId = "course_design_systems",
              moduleId = "mod_m3_tokens",
              title = "Choreographed Motion & Spatial Feedback",
              description = "Create expressive transitions using physics-based spring curves and staggered entrances.",
              durationSeconds = 115,
              visualType = LessonVisualType.ARCHITECTURE_CANVAS,
              codePreview = """val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.95f else 1f,
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
)""",
              checkpoints = listOf(
                VideoCheckpoint(
                  timestampSeconds = 45,
                  title = "Checkpoint: Spring vs Linear",
                  question = "Why are physics-based spring curves preferred over linear or hardcoded bezier curves for touch feedback?",
                  options = listOf(
                    "Springs feel natural and preserve continuous velocity when interrupted by new user gestures",
                    "Springs require zero CPU calculation",
                    "Linear curves cause screen tearing",
                    "Springs only work in dark theme"
                  ),
                  correctOptionIndex = 0,
                  explanation = "Physics springs naturally absorb velocity changes and seamlessly transition if the user changes direction mid-animation."
                )
              ),
              transcripts = listOf(
                TranscriptSnippet(
                  startSecond = 0,
                  endSecond = 40,
                  speaker = "Sophia Chen",
                  text = "Motion in UI is not mere decoration; it informs the user where elements come from and where they lead.",
                  codeSnippet = "spring(dampingRatio = 0.7f)"
                ),
                TranscriptSnippet(
                  startSecond = 41,
                  endSecond = 80,
                  speaker = "Sophia Chen",
                  text = "Keep standard feedback animations under 250ms to maintain a snappy, responsive feel.",
                  codeSnippet = "tween(durationMillis = 200, easing = FastOutSlowInEasing)"
                ),
                TranscriptSnippet(
                  startSecond = 81,
                  endSecond = 115,
                  speaker = "Sophia Chen",
                  text = "Combine scale and elevation changes to convey tactile depth during touch presses.",
                  codeSnippet = "Modifier.scale(scale).shadow(elevation)"
                )
              ),
              takeaways = listOf(
                "UI animations should provide functional spatial orientation.",
                "Keep touch transitions between 150ms and 300ms for optimal responsiveness.",
                "Spring animations feel physical and adapt smoothly to gesture interruptions."
              )
            )
          )
        )
      ),
      finalQuiz = Quiz(
        id = "quiz_design_cert",
        courseId = "course_design_systems",
        title = "Design Systems & Accessibility Quiz",
        description = "Test your knowledge on color tokens, WCAG ratios, 8dp grids, and spring physics.",
        passingPercentage = 75,
        xpReward = 180,
        timeLimitMinutes = 4,
        questions = listOf(
          QuizQuestion(
            id = "q_des_1",
            prompt = "What is the recommended minimum interactive touch target size according to Android Accessibility standards?",
            codeSnippet = null,
            options = listOf(
              "48dp x 48dp",
              "24dp x 24dp",
              "16dp x 16dp",
              "100dp x 100dp"
            ),
            correctAnswerIndex = 0,
            explanation = "Material 3 and Android accessibility guidelines require interactive touch targets to be at least 48dp x 48dp."
          ),
          QuizQuestion(
            id = "q_des_2",
            prompt = "Which color token is designed for low-emphasis background fills beneath primary content?",
            codeSnippet = null,
            options = listOf(
              "primaryContainer",
              "errorContainer",
              "outlineVariant",
              "inverseSurface"
            ),
            correctAnswerIndex = 0,
            explanation = "primaryContainer provides a soft, tinted background with lower visual weight than solid primary fill."
          )
        )
      )
    ),

    // 4. Cloud Native & Kubernetes Architecture
    Course(
      id = "course_cloud_k8s",
      title = "Cloud Native DevOps & Kubernetes",
      tagline = "Deploy resilient microservices, configure ingress controllers, and automate CI/CD",
      description = "Master Docker multi-stage builds, Kubernetes Pod scheduling, Helm charts, health probes, horizontal pod autoscaling, and production observability.",
      category = "DevOps & Cloud",
      level = "Intermediate",
      instructor = "Marcus Vance",
      instructorRole = "Principal Cloud Architect",
      rating = 4.82f,
      totalLessonsCount = 2,
      totalDurationMinutes = 22,
      accentColorHex = 0xFF10B981, // Emerald
      tags = listOf("Kubernetes", "Docker", "Cloud", "DevOps", "Helm"),
      modules = listOf(
        CourseModule(
          id = "mod_k8s_core",
          courseId = "course_cloud_k8s",
          title = "Module 1: Pods, Deployments & Services",
          description = "Declarative YAML, Rolling Updates, and Service Discovery",
          order = 1,
          lessons = listOf(
            Lesson(
              id = "lesson_k8s_pods",
              courseId = "course_cloud_k8s",
              moduleId = "mod_k8s_core",
              title = "Kubernetes Pod Lifecycle & Probes",
              description = "Configure liveness, readiness, and startup probes to ensure zero-downtime rolling deployments.",
              durationSeconds = 125,
              visualType = LessonVisualType.TERMINAL_EXECUTION,
              codePreview = """livenessProbe:
  httpGet:
    path: /healthz
    port: 8080
  initialDelaySeconds: 5
  periodSeconds: 10
readinessProbe:
  httpGet:
    path: /ready
    port: 8080""",
              checkpoints = listOf(
                VideoCheckpoint(
                  timestampSeconds = 50,
                  title = "Checkpoint: Liveness vs Readiness",
                  question = "What action does Kubernetes take when a Pod's readinessProbe fails?",
                  options = listOf(
                    "It stops routing network traffic to the pod while keeping the container running",
                    "It immediately kills and restarts the container",
                    "It deletes the entire Kubernetes cluster",
                    "It scales up the cluster node count"
                  ),
                  correctOptionIndex = 0,
                  explanation = "Readiness probes determine if the pod can receive client traffic. If it fails, traffic is temporarily stopped without terminating the process."
                )
              ),
              transcripts = listOf(
                TranscriptSnippet(
                  startSecond = 0,
                  endSecond = 45,
                  speaker = "Marcus Vance",
                  text = "In Kubernetes, a Pod is the smallest deployable compute unit. To ensure high availability, we must configure health probes.",
                  codeSnippet = "kubectl get pods -o wide"
                ),
                TranscriptSnippet(
                  startSecond = 46,
                  endSecond = 90,
                  speaker = "Marcus Vance",
                  text = "Liveness probes restart hung containers, while readiness probes protect clients from receiving errors during warmup.",
                  codeSnippet = "livenessProbe vs readinessProbe"
                ),
                TranscriptSnippet(
                  startSecond = 91,
                  endSecond = 125,
                  speaker = "Marcus Vance",
                  text = "Combined with RollingUpdate deployment strategies, you achieve true zero-downtime releases.",
                  codeSnippet = "spec.strategy.type: RollingUpdate"
                )
              ),
              takeaways = listOf(
                "Liveness probes restart deadlocked containers.",
                "Readiness probes gate incoming load balancer traffic until initialization completes.",
                "Never share persistent node disk state between stateless replica pods."
              )
            ),
            Lesson(
              id = "lesson_docker_multistage",
              courseId = "course_cloud_k8s",
              moduleId = "mod_k8s_core",
              title = "Optimized Docker Multi-Stage Builds",
              description = "Reduce container image size from 1GB to 40MB using alpine base images and builder stages.",
              durationSeconds = 115,
              visualType = LessonVisualType.CODE_EDITOR,
              codePreview = """# Build stage
FROM gradle:8-jdk17 AS builder
COPY . /app
RUN ./gradlew assembleRelease

# Runtime stage
FROM openjdk:17-slim
COPY --from=builder /app/build/libs/app.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]""",
              checkpoints = listOf(
                VideoCheckpoint(
                  timestampSeconds = 40,
                  title = "Checkpoint: Multi-Stage Benefits",
                  question = "What is the key security and performance advantage of Docker multi-stage builds?",
                  options = listOf(
                    "Build compilers, source files, and heavy toolchains are omitted from the final production runtime image",
                    "It makes containers run faster than bare metal",
                    "It encrypts the Linux kernel",
                    "It bypasses container registry authentication"
                  ),
                  correctOptionIndex = 0,
                  explanation = "Multi-stage builds leave behind compilers, SDKs, and build cache, resulting in tiny, secure container images with minimal attack surface."
                )
              ),
              transcripts = listOf(
                TranscriptSnippet(
                  startSecond = 0,
                  endSecond = 35,
                  speaker = "Marcus Vance",
                  text = "Bloated container images slow down deployments and expose vulnerabilities. Multi-stage builds are the remedy.",
                  codeSnippet = "FROM golang:1.22 AS build"
                ),
                TranscriptSnippet(
                  startSecond = 36,
                  endSecond = 75,
                  speaker = "Marcus Vance",
                  text = "By separating the compile environment from the scratch or alpine runtime, image sizes drop by over 90 percent.",
                  codeSnippet = "COPY --from=build /bin/app /bin/app"
                ),
                TranscriptSnippet(
                  startSecond = 76,
                  endSecond = 115,
                  speaker = "Marcus Vance",
                  text = "Fast pull times allow your autoscalers to spin up new pods in seconds during traffic spikes.",
                  codeSnippet = null
                )
              ),
              takeaways = listOf(
                "Multi-stage builds drastically reduce container footprint.",
                "Excluding compilers reduces Common Vulnerabilities and Exposures (CVE) attack surface.",
                "Lightweight images accelerate cold start times during horizontal scaling."
              )
            )
          )
        )
      ),
      finalQuiz = Quiz(
        id = "quiz_cloud_cert",
        courseId = "course_cloud_k8s",
        title = "Cloud Native Architecture Quiz",
        description = "Validate your understanding of Kubernetes controllers, probes, and container optimization.",
        passingPercentage = 75,
        xpReward = 190,
        timeLimitMinutes = 4,
        questions = listOf(
          QuizQuestion(
            id = "q_k8s_1",
            prompt = "Which Kubernetes resource manages a set of identical Pods and handles rolling version updates?",
            codeSnippet = null,
            options = listOf(
              "Deployment",
              "ConfigMap",
              "Ingress",
              "Secret"
            ),
            correctAnswerIndex = 0,
            explanation = "A Deployment provides declarative updates for Pods and ReplicaSets."
          ),
          QuizQuestion(
            id = "q_k8s_2",
            prompt = "What happens if a container fails its livenessProbe threshold count?",
            codeSnippet = null,
            options = listOf(
              "The kubelet kills the container and restarts it according to the restartPolicy",
              "The cluster reboots",
              "The pod is converted into a CronJob",
              "The network port is blocked"
            ),
            correctAnswerIndex = 0,
            explanation = "The kubelet terminates the unresponsive container and restarts it."
          )
        )
      )
    )
  )
}
