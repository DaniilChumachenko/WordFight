
## Code Reuse Rule

Before creating any new composable, component, or utility — always check first:

1. **`core/ui/components/`** — is there already a suitable component?
2. **Search the codebase** — is there a similar implementation in another feature module?
3. If the same pattern exists in 2+ places — **extract to `core/ui`** as a reusable component.
4. If an existing component almost fits — **extend it with optional parameters** (without breaking existing call sites) rather than duplicating it.
5. If a component is feature-specific and truly unique — keep it in the feature module, but confirm it doesn't duplicate something elsewhere.

Examples of already extracted shared components:
- `CryptoAddressCard` / `CryptoAddressSummaryRow` — `core/ui/components/CryptoAddressRow.kt`
- `ProfileAvatar` — `core/ui/components/ProfileAvatar.kt`
- `BarvaButton` / `BarvaOutlinedButton` — `core/ui/btns/`
- `CommonLoading`, `ErrorContent` — `core/ui/screens/`

---

## Kotlin 2.0+ Development Guide

### Quick Reference

Kotlin 2.0+ Expert - K2 compiler, coroutines, Ktor.

Auto-Triggers: Kotlin files (`.kt`, `.kts`), Gradle Kotlin DSL (`build.gradle.kts`, `settings.gradle.kts`)

Core Capabilities:
- Kotlin 2.0: K2 compiler, coroutines, Flow, sealed classes, value classes
- Ktor 3.0: Async HTTP server/client, WebSocket, JWT authentication
- Exposed 0.55: Kotlin SQL framework with coroutines support
- Testing: JUnit 5, MockK, Kotest, Turbine for Flow testing

---

## Kotlin 2.0 Features

### Coroutines and Flow

```kotlin
// Structured concurrency with async/await
suspend fun fetchUserWithOrders(userId: Long): UserWithOrders = coroutineScope {
    val userDeferred = async { userRepository.findById(userId) }
    val ordersDeferred = async { orderRepository.findByUserId(userId) }
    UserWithOrders(userDeferred.await(), ordersDeferred.await())
}

// Flow for reactive streams
fun observeUsers(): Flow<User> = flow {
    while (true) {
        emit(userRepository.findLatest())
        delay(1000)
    }
}.flowOn(Dispatchers.IO)
```

### Sealed Classes and Value Classes

```kotlin
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val exception: Throwable) : Result<Nothing>
    data object Loading : Result<Nothing>
}

@JvmInline
value class UserId(val value: Long) {
    init { require(value > 0) { "UserId must be positive" } }
}

@JvmInline
value class Email(val value: String) {
    init { require(value.contains("@")) { "Invalid email format" } }
}
```

---

## Project Structure & Feature Modules

### Module Organization Principles

**1. Feature Isolation**
- Each feature should be in its own `feature/<name>` module
- Features should be independent and not depend on other features
- Common functionality goes in `core/*` modules

**2. Module Structure**
```
project/
├── app/                    # Main application module
├── core/
│   ├── ui/                # Shared UI components, themes, BaseViewModel
│   ├── domain/            # Domain models, repository interfaces, UseCases
│   ├── data/              # Repository implementations, data sources
│   ├── common/            # Utilities, extensions
│   └── security/          # Auth, crypto, security UseCases
└── feature/
    ├── dashboard/         # Dashboard feature
    ├── settings/          # Settings feature
    ├── wallet/            # Wallet feature
    └── contacts/          # Contacts feature
```

**3. Feature Module Structure**
```
feature/dashboard/
├── domain/                # Feature-specific UseCases (if any)
├── ui/
│   ├── dashboard/         # Feature screens
│   │   ├── DashboardScreen.kt
│   │   ├── components/    # Screen-specific components
│   │   └── viewmodel/     # ViewModels, Actions, States, Effects
│   └── components/        # Feature-wide shared components
├── navigation/            # Feature navigation
└── di/                    # Feature Koin/Hilt module
```

**4. Component Reusability Rules**

**When to keep components LOCAL (in feature):**
- Used only in ONE feature
- Tightly coupled to feature's business logic
- Example: `DashboardBalanceCard`, `SendAmountInput`

**When to move to CORE/UI:**
- Used in 2+ features
- Generic, reusable UI components
- No business logic dependency
- Example: `BarvaButton`, `LoadingContent`, `ErrorContent`

**Example:**
```kotlin
// ❌ BAD - Feature-specific component in core/ui
// core/ui/src/.../DashboardCard.kt
@Composable
fun DashboardBalanceCard(balance: Double) { ... }

// ✅ GOOD - Keep in feature
// feature/dashboard/ui/components/BalanceCard.kt
@Composable
fun BalanceCard(balance: Double) { ... }

// ✅ GOOD - Generic component in core/ui
// core/ui/src/.../Card.kt
@Composable
fun BarvaCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) { ... }
```

**5. Dependency Rules**

**Allowed dependencies:**
- `feature/*` → `core/*` ✅
- `app` → `feature/*` ✅
- `core/data` → `core/domain` ✅
- `core/security` → `core/domain` ✅

**Forbidden dependencies:**
- `feature/A` → `feature/B` ❌ (features should be independent)
- `core/*` → `feature/*` ❌ (core is shared, not feature-specific)
- `core/domain` → `core/data` ❌ (domain is pure, no implementation)

**6. Navigation Between Features**

Navigation is defined in the `app` module:
```kotlin
// app/navigation/NavGraph.kt
fun BarvapayNavGraph(...) {
    NavHost(...) {
        dashboardGraph(...)  // from feature/dashboard
        settingsGraph(...)   // from feature/settings

        // Navigation between features
        settingsScreen(
            onLogout = {
                navController.navigate(WELCOME_ROUTE) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
}
```

**7. Shared Business Logic**

Use UseCases in appropriate modules:
```kotlin
// ✅ GOOD - Auth logic in core/security
core/security/usecase/LogoutUseCase.kt

// ✅ GOOD - Feature-specific in feature module
feature/dashboard/domain/GetPortfolioUseCase.kt

// ❌ BAD - Shared logic in feature module
feature/dashboard/domain/LogoutUseCase.kt  // Should be in core!
```

**8. When to Create a New Feature Module**

Create new feature module when:
- Feature has 2+ screens
- Feature has significant business logic
- Feature can be developed/tested independently
- Feature might be removed/replaced in future

Keep in existing module when:
- Single simple screen
- Tightly coupled to parent feature
- Very small functionality

---

## Android Architecture

### MVVM Architecture with MVI-Reducer Pattern

Modern ViewModel-Compose communication using BaseViewModel with reducer pattern:

**Key Concepts:**
- **Action**: User interactions from UI
- **Entity**: Domain layer data model
- **State**: Presentation layer UI model
- **Reducer**: Pure function that processes actions and updates state

```kotlin
// Base interfaces
interface Action
interface Entity
interface State

// UiState wrapper for loading/error handling
data class UiState<T : State>(
    val isLoading: Boolean,
    val isRefreshing: Boolean,
    val content: T?,
    val hasError: Boolean,
) : State {
    val showLoadingScreen: Boolean get() = isLoading && content == null
    val showFullScreenError: Boolean get() = hasError && content == null
    val hasContent: Boolean get() = content != null

    fun loading(isRefreshing: Boolean = false, content: T? = this.content): UiState<T> =
        this.copy(isLoading = true, isRefreshing = isRefreshing, content = content, hasError = false)

    fun content(content: T): UiState<T> =
        this.copy(isLoading = false, isRefreshing = false, content = content, hasError = false)

    fun error(): UiState<T> =
        this.copy(isLoading = false, isRefreshing = false, hasError = true)

    companion object {
        fun <T : State> initialLoading(): UiState<T> =
            UiState(isLoading = true, isRefreshing = false, content = null, hasError = false)
    }
}

// BaseViewModel with reducer pattern
abstract class BaseViewModel<A : Action, E : Entity, S : State> : ViewModel() {

    private val _actionFlow by lazy {
        MutableSharedFlow<A>(
            replay = 1,
            extraBufferCapacity = 64,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
    }

    val uiState: StateFlow<UiState<S>> by lazy {
        var state = ViewModelState<E, S>()
        _actionFlow
            .flatMapConcat { action ->
                flow<ViewModelState<E, S>> {
                    this.safeReduce(action) { state }
                }.onEach { newState ->
                    state = newState
                }
            }
            .map { newState -> newState.uiState }
            .stateIn(viewModelScope, SharingStarted.Lazily, UiState.initialLoading())
    }

    fun setAction(action: A) {
        _actionFlow.tryEmit(action)
    }

    protected abstract suspend fun ReducerScope<E, S>.reduce(action: A)

    private suspend fun FlowCollector<ViewModelState<E, S>>.safeReduce(
        action: A,
        getCurrentState: () -> ViewModelState<E, S>,
    ) {
        try {
            val scope = object : ReducerScope<E, S> {
                override fun getEntity(): E? = getCurrentState().entity
                override fun getUiState(): UiState<S> = getCurrentState().uiState

                override suspend fun updateEntity(update: suspend E?.() -> E): E {
                    return getEntity().update()
                        .apply { emit(getCurrentState().copy(entity = this)) }
                }

                override suspend fun updateUiState(update: suspend UiState<S>.() -> UiState<S>): UiState<S> {
                    return getUiState().update()
                        .apply { emit(getCurrentState().copy(uiState = this)) }
                }
            }
            scope.reduce(action)
        } catch (error: Throwable) {
            Log.e("BaseViewModel", "Error during reduce operation", error)
            val errorUiState = getCurrentState().uiState.error()
            emit(getCurrentState().copy(uiState = errorUiState))
        }
    }

    private data class ViewModelState<E : Entity, S : State>(
        val entity: E? = null,
        val uiState: UiState<S> = UiState.initialLoading(),
    )

    interface ReducerScope<E : Entity, S : State> {
        fun getEntity(): E?
        fun getUiState(): UiState<S>
        suspend fun updateEntity(update: suspend E?.() -> E): E
        suspend fun updateUiState(update: suspend UiState<S>.() -> UiState<S>): UiState<S>
    }
}

// Example: Counter feature
// Domain layer
data class CounterEntity(val count: Int) : Entity

class CounterUseCases {
    suspend fun init(): CounterEntity {
        delay(1000) // Simulate loading
        return CounterEntity(0)
    }

    suspend fun increment(entity: CounterEntity): CounterEntity {
        delay(500)
        return entity.copy(count = entity.count + 1)
    }

    suspend fun decrement(entity: CounterEntity): CounterEntity {
        delay(500)
        return entity.copy(count = entity.count - 1)
    }
}

// Presentation layer
sealed class CounterAction : Action {
    data object Init : CounterAction()
    data object Increment : CounterAction()
    data object Decrement : CounterAction()
}

data class CounterState(val count: Int) : State {
    companion object {
        fun fromEntity(entity: CounterEntity): CounterState =
            CounterState(count = entity.count)
    }
}

// ViewModel implementation
class CounterViewModel(
    private val useCases: CounterUseCases = CounterUseCases()
) : BaseViewModel<CounterAction, CounterEntity, CounterState>() {

    init {
        setAction(CounterAction.Init)
    }

    override suspend fun ReducerScope<CounterEntity, CounterState>.reduce(action: CounterAction) {
        updateUiState { loading() }

        val entity = updateEntity {
            if (this == null) return@updateEntity useCases.init()

            when (action) {
                CounterAction.Init -> useCases.init()
                CounterAction.Increment -> useCases.increment(this)
                CounterAction.Decrement -> useCases.decrement(this)
            }
        }

        updateUiState { content(CounterState.fromEntity(entity)) }
    }
}

// Composable
@Composable
fun CounterScreen(viewModel: CounterViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Counter: ${uiState.content?.count ?: "Loading..."}")

        if (uiState.isLoading) {
            CircularProgressIndicator()
        }

        if (uiState.showFullScreenError) {
            Text("An error occurred!")
        }

        Button(onClick = { viewModel.setAction(CounterAction.Increment) }) {
            Text("Increment")
        }
        Button(onClick = { viewModel.setAction(CounterAction.Decrement) }) {
            Text("Decrement")
        }
    }
}
```

**Benefits:**
- **Separation of Concerns**: Entity (domain) and State (presentation) are decoupled
- **Backpressure Handling**: MutableSharedFlow with buffer handles rapid UI events
- **MVI Pattern**: Unidirectional data flow (Action → Reducer → State)
- **Multiple State Updates**: Single action can emit multiple state updates (e.g., loading → content)
- **Error Handling**: Automatic error state on exceptions

### Single-Fire Events (One-Time Events)

For events that should only be consumed once (like showing a Snackbar or navigation):

**Approach 1: Using Channel**
```kotlin
abstract class BaseViewModel<A : Action, E : Entity, S : State> : ViewModel() {
    // ... existing code ...

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events: Flow<UiEvent> = _events.receiveAsFlow()

    protected suspend fun sendEvent(event: UiEvent) {
        _events.send(event)
    }
}

// In ViewModel implementation
override suspend fun ReducerScope<UserEntity, UserState>.reduce(action: UserAction) {
    when (action) {
        is UserAction.DeleteUser -> {
            val entity = updateEntity { useCases.deleteUser(this!!, action.userId) }
            updateUiState { content(UserState.fromEntity(entity)) }
            sendEvent(UiEvent.ShowMessage("User deleted successfully"))
        }
    }
}

// In Composable
@Composable
fun UserScreen(viewModel: UserViewModel = hiltViewModel()) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
                is UiEvent.Navigate -> { /* handle navigation */ }
            }
        }
    }
}

sealed interface UiEvent {
    data class ShowMessage(val message: String) : UiEvent
    data class Navigate(val route: String) : UiEvent
}
```

**Approach 2: Event as Part of State**
```kotlin
data class UserListState(
    val users: List<UserUi>,
    val consumableEvent: ConsumableEvent? = null
) : State

sealed interface ConsumableEvent {
    data class ShowSnackbar(val message: String) : ConsumableEvent
    data class NavigateTo(val route: String) : ConsumableEvent
}

// In ViewModel
override suspend fun ReducerScope<UserEntity, UserState>.reduce(action: UserAction) {
    when (action) {
        is UserAction.DeleteUser -> {
            val entity = updateEntity { useCases.deleteUser(this!!, action.userId) }
            updateUiState {
                content(
                    UserState.fromEntity(entity).copy(
                        consumableEvent = ConsumableEvent.ShowSnackbar("User deleted")
                    )
                )
            }
        }

        UserAction.EventConsumed -> {
            updateUiState {
                content(getUiState().content!!.copy(consumableEvent = null))
            }
        }
    }
}

// In Composable
@Composable
fun UserScreen(viewModel: UserViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.content?.consumableEvent) {
        uiState.content?.consumableEvent?.let { event ->
            when (event) {
                is ConsumableEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is ConsumableEvent.NavigateTo -> {
                    // Handle navigation
                }
            }
            viewModel.setAction(UserAction.EventConsumed)
        }
    }
}
```

**Reference**: [ViewModel Events Antipatterns by Manuel Vivo](https://manuelvivo.dev/viewmodel-events-antipatterns)

### Clean Architecture Layers

```
app/
├── data/
│   ├── local/           # Room database, DataStore
│   │   ├── dao/
│   │   └── entities/
│   ├── remote/          # Retrofit, network
│   │   ├── api/
│   │   └── dto/
│   └── repository/      # Repository implementations
├── domain/
│   ├── model/           # Domain models
│   ├── repository/      # Repository interfaces
│   └── usecase/         # Business logic
└── presentation/
    ├── ui/              # Composables
    └── viewmodel/       # ViewModels
```

### Repository Pattern

```kotlin
// Domain layer - interface
interface UserRepository {
    fun getUser(id: String): Flow<User>
    suspend fun saveUser(user: User): Result<Unit>
    suspend fun deleteUser(id: String): Result<Unit>
}

// Data layer - implementation
class UserRepositoryImpl(
    private val userApi: UserApi,
    private val userDao: UserDao
) : UserRepository {

    override fun getUser(id: String): Flow<User> = flow {
        // Emit cached data first
        userDao.getUser(id)?.let { emit(it.toDomain()) }

        // Fetch fresh data
        try {
            val remoteUser = userApi.getUser(id)
            userDao.insertUser(remoteUser.toEntity())
            emit(remoteUser.toDomain())
        } catch (e: Exception) {
            // Network error, cached data already emitted
        }
    }

    override suspend fun saveUser(user: User): Result<Unit> = runCatching {
        userApi.updateUser(user.toDto())
        userDao.insertUser(user.toEntity())
    }
}
```

### Dependency Injection with Hilt

```kotlin
// Module definition
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi {
        return retrofit.create(UserApi::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}

// ViewModel injection
@HiltViewModel
class UserViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: String = savedStateHandle.get<String>("userId")
        ?: throw IllegalArgumentException("userId required")
}
```

### Use Cases for Business Logic

```kotlin
class GetUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val analyticsTracker: AnalyticsTracker
) {
    operator fun invoke(userId: String): Flow<Result<User>> = flow {
        emit(Result.Loading)

        userRepository.getUser(userId)
            .catch { e ->
                analyticsTracker.trackError("get_user_failed", e)
                emit(Result.Error(e))
            }
            .collect { user ->
                emit(Result.Success(user))
            }
    }
}

// Sealed class for results
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
```

### Data Mapping

```kotlin
// DTO (Data Transfer Object) - from API
data class UserDto(
    @Json(name = "id") val id: String,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "email_address") val email: String
)

// Entity - for Room
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String
)

// Domain model
data class User(
    val id: String,
    val name: String,
    val email: String
)

// Mappers
fun UserDto.toEntity() = UserEntity(id = id, name = fullName, email = email)
fun UserDto.toDomain() = User(id = id, name = fullName, email = email)
fun UserEntity.toDomain() = User(id = id, name = name, email = email)
fun User.toEntity() = UserEntity(id = id, name = name, email = email)
```

---

## Jetpack Compose

### State Management

Compose provides several ways to manage state:

- **remember**: Survives recomposition
- **rememberSaveable**: Survives configuration changes
- **mutableStateOf**: Creates observable state
- **derivedStateOf**: Computed state that updates when dependencies change

```kotlin
@Composable
fun Counter() {
    var count by remember { mutableStateOf(0) }

    Column {
        Text("Count: $count")
        Button(onClick = { count++ }) {
            Text("Increment")
        }
    }
}

// With saveable for configuration changes
@Composable
fun SearchField() {
    var query by rememberSaveable { mutableStateOf("") }

    TextField(
        value = query,
        onValueChange = { query = it },
        placeholder = { Text("Search...") }
    )
}
```

### State Hoisting

Lift state up to make composables stateless and reusable:

```kotlin
// Stateless composable
@Composable
fun NameInput(
    name: String,
    onNameChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text("Name") },
        modifier = modifier
    )
}

// Stateful parent
@Composable
fun UserForm() {
    var name by remember { mutableStateOf("") }

    NameInput(
        name = name,
        onNameChange = { name = it }
    )
}
```

### Composable Function Guidelines

```kotlin
// Use Modifier as first optional parameter
@Composable
fun CustomCard(
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(16.dp)
        )
    }
}

// Use slot APIs for flexible content
@Composable
fun CustomScaffold(
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = topBar,
        bottomBar = bottomBar,
        content = content
    )
}
```

### Efficient Recomposition

```kotlin
// Use keys for list items
@Composable
fun UserList(users: List<User>) {
    LazyColumn {
        items(
            items = users,
            key = { it.id }  // Stable key for efficient updates
        ) { user ->
            UserItem(user)
        }
    }
}

// Use derivedStateOf for expensive computations
@Composable
fun FilteredList(items: List<Item>, query: String) {
    val filteredItems by remember(items, query) {
        derivedStateOf {
            items.filter { it.name.contains(query, ignoreCase = true) }
        }
    }

    LazyColumn {
        items(filteredItems) { item ->
            ItemRow(item)
        }
    }
}
```

### Side Effects

```kotlin
// LaunchedEffect for coroutine-based side effects
@Composable
fun UserProfile(userId: String, viewModel: UserViewModel) {
    LaunchedEffect(userId) {
        viewModel.loadUser(userId)
    }
}

// DisposableEffect for cleanup
@Composable
fun LifecycleAwareComponent(lifecycle: Lifecycle) {
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            // Handle lifecycle events
        }
        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}

// SideEffect for non-suspend side effects
@Composable
fun AnalyticsScreen(screenName: String) {
    SideEffect {
        analytics.logScreenView(screenName)
    }
}
```

### Material 3 Theming

```kotlin
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> darkColorScheme(
            primary = Purple80,
            secondary = PurpleGrey80,
            tertiary = Pink80
        )
        else -> lightColorScheme(
            primary = Purple40,
            secondary = PurpleGrey40,
            tertiary = Pink40
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

---

## Kotlin Coroutines

### Coroutine Basics

```kotlin
// Launching coroutines
class UserViewModel : ViewModel() {

    fun loadUser(id: String) {
        // viewModelScope is automatically cancelled when ViewModel is cleared
        viewModelScope.launch {
            try {
                val user = userRepository.getUser(id)
                _uiState.value = UiState.Success(user)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message)
            }
        }
    }

    // For operations that return a value
    fun fetchUserAsync(id: String): Deferred<User> {
        return viewModelScope.async {
            userRepository.getUser(id)
        }
    }
}

// Suspend functions
suspend fun fetchUserFromNetwork(id: String): User {
    return withContext(Dispatchers.IO) {
        api.getUser(id)
    }
}
```

### Dispatchers

```kotlin
// Main - UI operations
withContext(Dispatchers.Main) {
    textView.text = "Updated"
}

// IO - Network, database, file operations
withContext(Dispatchers.IO) {
    val data = api.fetchData()
    database.save(data)
}

// Default - CPU-intensive work
withContext(Dispatchers.Default) {
    val result = expensiveComputation(data)
}

// Custom dispatcher for limited parallelism
val limitedDispatcher = Dispatchers.IO.limitedParallelism(4)
```

### StateFlow and SharedFlow

```kotlin
class SearchViewModel : ViewModel() {
    // StateFlow - always has a current value
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // SharedFlow - for events without initial value
    private val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    // Derived state from flow
    val searchResults: StateFlow<List<Item>> = _searchQuery
        .debounce(300)
        .filter { it.length >= 2 }
        .flatMapLatest { query ->
            searchRepository.search(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateQuery(query: String) {
        _searchQuery.value = query
    }

    fun sendEvent(event: UiEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }
}
```

### Structured Concurrency

```kotlin
// Using coroutineScope for parallel operations
suspend fun loadDashboard(): Dashboard = coroutineScope {
    val userDeferred = async { userRepository.getUser() }
    val ordersDeferred = async { orderRepository.getOrders() }
    val notificationsDeferred = async { notificationRepository.getNotifications() }

    // All complete or all fail together
    Dashboard(
        user = userDeferred.await(),
        orders = ordersDeferred.await(),
        notifications = notificationsDeferred.await()
    )
}

// With timeout
suspend fun loadWithTimeout(): Data {
    return withTimeout(5000) {
        api.fetchData()
    }
}

// Or with nullable result on timeout
suspend fun loadWithTimeoutOrNull(): Data? {
    return withTimeoutOrNull(5000) {
        api.fetchData()
    }
}
```

### Exception Handling

```kotlin
// Using runCatching
suspend fun safeApiCall(): Result<User> = runCatching {
    api.getUser()
}

// Handling in ViewModel
fun loadUser() {
    viewModelScope.launch {
        safeApiCall()
            .onSuccess { user ->
                _uiState.value = UiState.Success(user)
            }
            .onFailure { error ->
                _uiState.value = UiState.Error(error.message)
            }
    }
}

// SupervisorJob for independent child failures
class MyViewModel : ViewModel() {
    private val supervisorJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + supervisorJob)

    fun loadMultiple() {
        scope.launch {
            // This failure won't cancel other children
            userRepository.getUser()
        }
        scope.launch {
            // This continues even if above fails
            orderRepository.getOrders()
        }
    }
}
```

### Flow Operators

```kotlin
// Transformation operators
userRepository.getUsers()
    .map { users -> users.filter { it.isActive } }
    .distinctUntilChanged()
    .collect { activeUsers -> updateUI(activeUsers) }

// Combining flows
val combined: Flow<Pair<User, Settings>> = combine(
    userRepository.getUser(),
    settingsRepository.getSettings()
) { user, settings ->
    Pair(user, settings)
}

// FlatMapLatest for search
searchQuery
    .debounce(300)
    .flatMapLatest { query ->
        if (query.isEmpty()) flowOf(emptyList())
        else searchRepository.search(query)
    }
    .collect { results -> updateResults(results) }

// Retry with exponential backoff
api.fetchData()
    .retry(3) { cause ->
        if (cause is IOException) {
            delay(1000 * (2.0.pow(retryCount)).toLong())
            true
        } else false
    }
```

### Lifecycle-Aware Collection

```kotlin
// In Compose - collectAsStateWithLifecycle
@Composable
fun UserScreen(viewModel: UserViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    UserContent(uiState)
}

// In Activity/Fragment - repeatOnLifecycle
class UserFragment : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUI(state)
                }
            }
        }
    }
}

// Multiple flows
viewLifecycleOwner.lifecycleScope.launch {
    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        launch {
            viewModel.users.collect { updateUserList(it) }
        }
        launch {
            viewModel.events.collect { handleEvent(it) }
        }
    }
}
```

---

## Anti-Patterns to Avoid

### Architecture Anti-Patterns

**God Activity/Fragment** - All logic in one Activity.
Use MVVM with clear separation of concerns.

**Network Calls in ViewModel** - Direct network dependency.
Inject repository through constructor, use UseCases for business logic.

**Exposing Mutable State**:
```kotlin
// Bad
val uiState = MutableStateFlow(UiState())  // Mutable exposed!

// Good
private val _uiState = MutableStateFlow(UiState())
val uiState: StateFlow<UiState> = _uiState.asStateFlow()
```

**Mixing Domain and Presentation Models**:
```kotlin
// Bad - Using the same model for both layers
data class User(
    val id: String,
    val name: String,
    val email: String,
    val isEmailVisible: Boolean  // UI concern in domain model!
) : Entity, State

// Good - Separate models for each layer
data class UserEntity(
    val id: String,
    val name: String,
    val email: String,
    val isActive: Boolean  // Domain concern
) : Entity

data class UserState(
    val displayName: String,
    val isEmailVisible: Boolean  // UI concern
) : State {
    companion object {
        fun fromEntity(entity: UserEntity): UserState =
            UserState(
                displayName = entity.name,
                isEmailVisible = entity.isActive
            )
    }
}
```

**Direct Action Flow Emission**:
```kotlin
// Bad - Exposing action flow
val actionFlow: MutableSharedFlow<Action> = _actionFlow  // Direct access!

// Good - Using setAction method
fun setAction(action: A) {
    _actionFlow.tryEmit(action)
}
```

**Multiple Reducers for Same Action**:
```kotlin
// Bad - Complex branching in reducer
override suspend fun ReducerScope<E, S>.reduce(action: Action) {
    when (action) {
        is LoadData -> {
            if (condition1) {
                // Complex logic 1
            } else if (condition2) {
                // Complex logic 2
            } else {
                // Complex logic 3
            }
        }
    }
}

// Good - Separate actions for different cases
sealed class DataAction : Action {
    data object LoadInitial : DataAction()
    data object LoadCached : DataAction()
    data object LoadRemote : DataAction()
}

override suspend fun ReducerScope<E, S>.reduce(action: DataAction) {
    when (action) {
        DataAction.LoadInitial -> handleInitialLoad()
        DataAction.LoadCached -> handleCachedLoad()
        DataAction.LoadRemote -> handleRemoteLoad()
    }
}
```

**Forgetting to Update UiState**:
```kotlin
// Bad - Only updating entity
override suspend fun ReducerScope<E, S>.reduce(action: Action) {
    val entity = updateEntity { useCases.loadData() }
    // Forgot to update UI state! User won't see changes
}

// Good - Always update UiState after entity changes
override suspend fun ReducerScope<E, S>.reduce(action: Action) {
    updateUiState { loading() }
    val entity = updateEntity { useCases.loadData() }
    updateUiState { content(State.fromEntity(entity)) }
}
```

### Module Anti-Patterns

**Feature-to-Feature Dependencies**:
```kotlin
// ❌ BAD - feature/dashboard depends on feature/settings
// feature/dashboard/build.gradle.kts
dependencies {
    implementation(project(":feature:settings"))  // Wrong!
}

// ✅ GOOD - Both features independent, use shared core
// feature/dashboard/build.gradle.kts
dependencies {
    implementation(project(":core:security"))  // Shared LogoutUseCase
}

// feature/settings/build.gradle.kts
dependencies {
    implementation(project(":core:security"))  // Same LogoutUseCase
}
```

**Feature-Specific Code in Core**:
```kotlin
// ❌ BAD - Dashboard-specific component in core/ui
// core/ui/components/DashboardCard.kt
@Composable
fun DashboardBalanceCard(totalBalance: Double) { ... }

// ✅ GOOD - Generic component in core/ui
// core/ui/components/Card.kt
@Composable
fun BarvaCard(content: @Composable () -> Unit) { ... }

// ✅ GOOD - Feature-specific in feature
// feature/dashboard/ui/components/BalanceCard.kt
@Composable
fun BalanceCard(totalBalance: Double) {
    BarvaCard { /* dashboard-specific content */ }
}
```

**UseCase in Wrong Module**:
```kotlin
// ❌ BAD - Auth logic in feature module
// feature/dashboard/domain/LogoutUseCase.kt
class LogoutUseCase(...)  // Used by multiple features!

// ✅ GOOD - Shared auth logic in core/security
// core/security/usecase/LogoutUseCase.kt
class LogoutUseCase(...)

// ✅ GOOD - Feature-specific logic in feature
// feature/dashboard/domain/GetPortfolioUseCase.kt
class GetPortfolioUseCase(...)  // Only for dashboard
```

**Wrong Screen Location**:
```kotlin
// ❌ BAD - Settings screens in dashboard module
feature/dashboard/ui/settings/SettingsScreen.kt
feature/dashboard/ui/settings/ThemeScreen.kt

// ✅ GOOD - Settings screens in settings module
feature/settings/ui/SettingsScreen.kt
feature/settings/ui/theme/ThemeScreen.kt
```

**Circular Dependencies**:
```kotlin
// ❌ BAD - Circular dependency
// feature/dashboard depends on feature/settings
// feature/settings depends on feature/dashboard

// ✅ GOOD - Extract shared code to core
// Both features depend on core/security for LogoutUseCase
// Navigation handled in app module
```

### Compose Anti-Patterns

**Side Effects in Composition**:
```kotlin
// Bad
@Composable
fun BadExample(viewModel: ViewModel) {
    viewModel.loadData()  // Called on every recomposition!
}

// Good
@Composable
fun GoodExample(viewModel: ViewModel) {
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }
}
```

**Heavy Computation During Composition**:
```kotlin
// Bad
@Composable
fun BadList(items: List<Item>) {
    val sorted = items.sortedBy { it.name }  // Runs on every recomposition
}

// Good
@Composable
fun GoodList(items: List<Item>) {
    val sorted by remember(items) {
        derivedStateOf { items.sortedBy { it.name } }
    }
}
```

### Coroutine Anti-Patterns

**GlobalScope Usage**:
```kotlin
// Bad
GlobalScope.launch { fetchData() }  // Never cancelled, leaks memory

// Good
viewModelScope.launch { fetchData() }  // Properly scoped
```

**Blocking Calls on Main Thread**:
```kotlin
// Bad
fun loadData() {
    runBlocking { api.fetchData() }  // Blocks main thread!
}

// Good
fun loadData() {
    viewModelScope.launch {
        withContext(Dispatchers.IO) { api.fetchData() }
    }
}
```

**Flow Collection Without Lifecycle**:
```kotlin
// Bad
lifecycleScope.launch {
    viewModel.uiState.collect { updateUI(it) }  // Collects even when in background
}

// Good
viewLifecycleOwner.lifecycleScope.launch {
    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiState.collect { updateUI(it) }
    }
}
```

---

## Troubleshooting

**K2 Compiler**: Add `kotlin.experimental.tryK2=true` to gradle.properties; clear `.gradle` for rebuild

**Coroutines**: Avoid `runBlocking` in suspend contexts; use `Dispatchers.IO` for blocking operations

**Compose**: Ensure state hoisting for reusable components; use `key` in LazyColumn for stable updates

**Flow**: Use `stateIn` with `WhileSubscribed(5000)` for proper lifecycle handling

---

## Testing Patterns

### MockK with Coroutines

```kotlin
class UserServiceTest {
    private val repository = mockk<UserRepository>()
    private val service = UserService(repository)

    @Test
    fun `should create user`() = runTest {
        // Arrange
        val request = CreateUserRequest("John", "john@example.com", "password")
        val expectedUser = User(1L, "John", "john@example.com")

        coEvery { repository.existsByEmail(any()) } returns false
        coEvery { repository.save(any()) } returns expectedUser

        // Act
        val result = service.create(request)

        // Assert
        assertThat(result).isEqualTo(expectedUser)
        coVerify(exactly = 1) { repository.save(any()) }
    }

    @Test
    fun `should throw on duplicate email`() = runTest {
        coEvery { repository.existsByEmail("existing@example.com") } returns true

        assertThrows<DuplicateEmailException> {
            service.create(CreateUserRequest("John", "existing@example.com", "pass"))
        }

        coVerify(exactly = 0) { repository.save(any()) }
    }
}
```

### Flow Testing with Turbine

```kotlin
@Test
fun `should emit user updates`() = runTest {
    val users = listOf(
        User(1L, "John", "john@example.com"),
        User(2L, "Jane", "jane@example.com")
    )

    service.observeUsers().test {
        assertThat(awaitItem()).isEqualTo(users[0])
        assertThat(awaitItem()).isEqualTo(users[1])
        awaitComplete()
    }
}

@Test
fun `should handle errors in flow`() = runTest {
    coEvery { repository.findAllAsFlow() } throws RuntimeException("DB error")

    service.streamUsers().test {
        val error = awaitError()
        assertThat(error).isInstanceOf(RuntimeException::class.java)
        assertThat(error.message).contains("DB error")
    }
}
```

### Kotest Specification Style

```kotlin
class UserServiceSpec : FunSpec({
    val repository = mockk<UserRepository>()
    val service = UserService(repository)

    beforeTest {
        clearAllMocks()
    }

    context("create user") {
        test("should create user successfully") {
            coEvery { repository.existsByEmail(any()) } returns false
            coEvery { repository.save(any()) } returns User(1L, "John", "john@example.com")

            val result = service.create(CreateUserRequest("John", "john@example.com", "pass"))

            result.name shouldBe "John"
            result.email shouldBe "john@example.com"
        }

        test("should reject duplicate email") {
            coEvery { repository.existsByEmail("taken@example.com") } returns true

            shouldThrow<DuplicateEmailException> {
                service.create(CreateUserRequest("John", "taken@example.com", "pass"))
            }
        }
    }
})
```

---

## Complete Ktor REST API Example

### Application Setup

```kotlin
fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureKoin()
        configureSecurity()
        configureRouting()
        configureContentNegotiation()
        configureStatusPages()
        configureMonitoring()
    }.start(wait = true)
}

fun Application.configureKoin() {
    install(Koin) {
        modules(appModule)
    }
}

val appModule = module {
    single<Database> { DatabaseFactory.create() }
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<UserService> { UserServiceImpl(get()) }
    single<JwtService> { JwtServiceImpl() }
}

fun Application.configureSecurity() {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = "User API"
            verifier(JwtConfig.verifier)
            validate { credential ->
                if (credential.payload.audience.contains("api"))
                    JWTPrincipal(credential.payload)
                else null
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Token invalid or expired"))
            }
        }
    }
}

fun Application.configureContentNegotiation() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        })
    }
}

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<ValidationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(cause.message ?: "Validation failed"))
        }
        exception<NotFoundException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, ErrorResponse(cause.message ?: "Resource not found"))
        }
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Internal server error"))
        }
    }
}
```

### Complete User Routes

```kotlin
fun Application.configureRouting() {
    val userService by inject<UserService>()
    val jwtService by inject<JwtService>()

    routing {
        route("/api/v1") {
            get("/health") {
                call.respond(mapOf("status" to "healthy", "timestamp" to Instant.now().toString()))
            }

            route("/auth") {
                post("/register") {
                    val request = call.receive<CreateUserRequest>()
                    request.validate()
                    val user = userService.create(request)
                    call.respond(HttpStatusCode.Created, user.toDto())
                }

                post("/login") {
                    val request = call.receive<LoginRequest>()
                    val user = userService.authenticate(request.email, request.password)
                    val token = jwtService.generateToken(user)
                    call.respond(TokenResponse(token, user.toDto()))
                }
            }

            authenticate("auth-jwt") {
                route("/users") {
                    get {
                        val page = call.parameters["page"]?.toIntOrNull() ?: 0
                        val size = call.parameters["size"]?.toIntOrNull()?.coerceIn(1, 100) ?: 20
                        val users = userService.findAll(page, size)
                        call.respond(users.map { it.toDto() })
                    }

                    get("/{id}") {
                        val id = call.parameters["id"]?.toLongOrNull()
                            ?: throw ValidationException("Invalid user ID")
                        val user = userService.findById(id)
                            ?: throw NotFoundException("User not found")
                        call.respond(user.toDto())
                    }

                    get("/me") {
                        val userId = call.principal<JWTPrincipal>()!!
                            .payload.getClaim("userId").asLong()
                        val user = userService.findById(userId)
                            ?: throw NotFoundException("User not found")
                        call.respond(user.toDto())
                    }
                }
            }
        }
    }
}
```

### Repository with Exposed

```kotlin
object Users : LongIdTable("users") {
    val name = varchar("name", 100)
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val status = enumerationByName<UserStatus>("status", 20)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())
}

class UserEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<UserEntity>(Users)
    var name by Users.name
    var email by Users.email
    var passwordHash by Users.passwordHash
    var status by Users.status
    var createdAt by Users.createdAt

    fun toModel() = User(id.value, name, email, passwordHash, status, createdAt)
}

class UserRepositoryImpl(private val database: Database) : UserRepository {
    override suspend fun findById(id: Long): User? = dbQuery {
        UserEntity.findById(id)?.toModel()
    }

    override suspend fun findByEmail(email: String): User? = dbQuery {
        UserEntity.find { Users.email eq email }.singleOrNull()?.toModel()
    }

    override suspend fun save(user: User): User = dbQuery {
        UserEntity.new {
            name = user.name
            email = user.email
            passwordHash = user.passwordHash
            status = user.status
        }.toModel()
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO, database) { block() }
}
```

---

## Android Complete Screen Example with BaseViewModel

### Real-World User List Screen

```kotlin
// Domain Layer - Entity
data class UserListEntity(
    val users: List<UserDomain>,
    val lastUpdated: Instant = Instant.now()
) : Entity

data class UserDomain(
    val id: Long,
    val name: String,
    val email: String,
    val isActive: Boolean
)

// Use Cases
class UserListUseCases @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend fun loadUsers(): UserListEntity {
        val users = userRepository.getUsers()
        return UserListEntity(users = users)
    }

    suspend fun deleteUser(entity: UserListEntity, userId: Long): UserListEntity {
        userRepository.deleteUser(userId)
        return entity.copy(
            users = entity.users.filter { it.id != userId },
            lastUpdated = Instant.now()
        )
    }

    suspend fun refreshUsers(): UserListEntity {
        val users = userRepository.refreshUsers()
        return UserListEntity(users = users)
    }
}

// Presentation Layer - Actions
sealed class UserListAction : Action {
    data object Init : UserListAction()
    data object Refresh : UserListAction()
    data class DeleteUser(val userId: Long) : UserListAction()
}

// Presentation Layer - State
data class UserListState(
    val users: List<UserUi>,
    val isEmpty: Boolean = users.isEmpty()
) : State {
    companion object {
        fun fromEntity(entity: UserListEntity): UserListState {
            return UserListState(
                users = entity.users.map { user ->
                    UserUi(
                        id = user.id,
                        displayName = user.name,
                        emailVisible = user.isActive,
                        email = user.email
                    )
                }
            )
        }
    }
}

data class UserUi(
    val id: Long,
    val displayName: String,
    val emailVisible: Boolean,
    val email: String
)

// ViewModel Implementation
@HiltViewModel
class UserListViewModel @Inject constructor(
    private val useCases: UserListUseCases
) : BaseViewModel<UserListAction, UserListEntity, UserListState>() {

    init {
        setAction(UserListAction.Init)
    }

    override suspend fun ReducerScope<UserListEntity, UserListState>.reduce(
        action: UserListAction
    ) {
        when (action) {
            UserListAction.Init -> {
                // Show loading
                updateUiState { loading() }

                // Load users
                val entity = updateEntity { useCases.loadUsers() }

                // Show content
                updateUiState { content(UserListState.fromEntity(entity)) }
            }

            UserListAction.Refresh -> {
                // Show refreshing indicator with current content
                updateUiState { loading(isRefreshing = true) }

                // Refresh users
                val entity = updateEntity {
                    this?.let { useCases.refreshUsers() } ?: useCases.loadUsers()
                }

                // Update content
                updateUiState { content(UserListState.fromEntity(entity)) }
            }

            is UserListAction.DeleteUser -> {
                val currentEntity = getEntity()
                if (currentEntity == null) {
                    updateUiState { error() }
                    return
                }

                // Delete user and update state
                val updatedEntity = updateEntity {
                    useCases.deleteUser(currentEntity, action.userId)
                }

                updateUiState { content(UserListState.fromEntity(updatedEntity)) }
            }
        }
    }
}

// Composable Screen
@Composable
fun UserListScreen(
    viewModel: UserListViewModel = hiltViewModel(),
    onUserClick: (Long) -> Unit,
    onAddUserClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Users") },
                actions = {
                    IconButton(
                        onClick = { viewModel.setAction(UserListAction.Refresh) },
                        enabled = !uiState.isRefreshing
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddUserClick) {
                Icon(Icons.Default.Add, contentDescription = "Add User")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.showLoadingScreen -> CommonLoading()

                uiState.showFullScreenError -> {
                    ErrorState(
                        message = "Failed to load users",
                        onRetryClick = { viewModel.setAction(UserListAction.Init) }
                    )
                }

                uiState.hasContent -> {
                    val state = uiState.content!!

                    if (state.isEmpty) {
                        EmptyState(
                            message = "No users found",
                            onAddClick = onAddUserClick
                        )
                    } else {
                        UserList(
                            users = state.users,
                            isRefreshing = uiState.isRefreshing,
                            onUserClick = onUserClick,
                            onDeleteClick = { userId ->
                                viewModel.setAction(UserListAction.DeleteUser(userId))
                            },
                            onRefresh = {
                                viewModel.setAction(UserListAction.Refresh)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserList(
    users: List<UserUi>,
    isRefreshing: Boolean,
    onUserClick: (Long) -> Unit,
    onDeleteClick: (Long) -> Unit,
    onRefresh: () -> Unit
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = onRefresh
    )

    Box(Modifier.pullRefresh(pullRefreshState)) {
        LazyColumn {
            items(users, key = { it.id }) { user ->
                UserItem(
                    user = user,
                    onClick = { onUserClick(user.id) },
                    onDeleteClick = { onDeleteClick(user.id) }
                )
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun UserItem(
    user: UserUi,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(user.displayName) },
        supportingContent = {
            if (user.emailVisible) {
                Text(user.email)
            }
        },
        trailingContent = {
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}
```

**Key Improvements:**
- **Clean Separation**: Entity (domain) vs State (presentation)
- **Single Source of Truth**: All logic in reducer
- **Multiple State Updates**: Init action emits loading → content
- **Pull-to-Refresh**: Handled with isRefreshing flag
- **Error Recovery**: Retry action reloads data

### Repository with Room and Retrofit

```kotlin
interface UserRepository {
    fun getUsers(): Flow<List<User>>
    suspend fun getUser(id: Long): Result<User>
    suspend fun deleteUser(id: Long): Result<Unit>
    suspend fun createUser(request: CreateUserRequest): Result<User>
}

class UserRepositoryImpl @Inject constructor(
    private val api: UserApi,
    private val dao: UserDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UserRepository {

    override fun getUsers(): Flow<List<User>> =
        dao.getAllUsers()
            .onStart { syncUsersFromNetwork() }
            .flowOn(ioDispatcher)

    private suspend fun syncUsersFromNetwork() {
        try {
            val users = api.getUsers()
            dao.insertAll(users.map { it.toEntity() })
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync users from network")
        }
    }

    override suspend fun getUser(id: Long): Result<User> = withContext(ioDispatcher) {
        runCatching {
            dao.getUser(id)
                ?: api.getUser(id).also { dao.insert(it.toEntity()) }.toDomain()
        }
    }

    override suspend fun deleteUser(id: Long): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            api.deleteUser(id)
            dao.delete(id)
        }
    }

    override suspend fun createUser(request: CreateUserRequest): Result<User> =
        withContext(ioDispatcher) {
            runCatching {
                val user = api.createUser(request)
                dao.insert(user.toEntity())
                user.toDomain()
            }
        }
}
```

---

## Form Screen Pattern (Sealed Class + Typed FormState)

Pattern for screens with input fields (add/edit forms). Based on AddContactScreen / EditContactScreen implementation.

**Key principles:**
- **Sealed class** for field definitions (not enum) — supports hierarchy and typed subclasses
- **Typed FormState** as separate `MutableStateFlow` outside reducer — prevents text input blocking during long operations (save/load)
- **Reusable composable** for field rendering configured from sealed class properties
- **`imePadding()`** on Scaffold + `contentWindowInsets = WindowInsets(0,0,0,0)` for keyboard-aware bottomBar
- **`adjustResize`** in AndroidManifest for edge-to-edge apps using `enableEdgeToEdge()`

### 1. Field Definition — Sealed Class

```kotlin
sealed class ContactField(
    @param:StringRes val labelRes: Int,
    val placeholderText: String? = null,
    @param:StringRes val placeholderRes: Int? = null,
    val isMonospace: Boolean = false,
    val isAsciiInput: Boolean = false,
    val singleLine: Boolean = true,
    val minLines: Int = 1,
    val maxLines: Int = 1,
) {
    data object FirstName : ContactField(labelRes = R.string.name_required)
    data object LastName : ContactField(labelRes = R.string.last_name)

    sealed class Address(
        @StringRes labelRes: Int,
        placeholderText: String? = null,
        @StringRes placeholderRes: Int? = null,
        val coinKey: String,
    ) : ContactField(labelRes, placeholderText, placeholderRes, isMonospace = true, isAsciiInput = true) {
        data object Btc : Address(R.string.bitcoin_address, placeholderText = "1A1zP1...", coinKey = Constants.BTC)
        data object Eth : Address(R.string.ethereum_address, placeholderText = "0x742d...", coinKey = Constants.ETH)
    }

    data object Note : ContactField(R.string.note_optional, singleLine = false, minLines = 2, maxLines = 4)

    companion object {
        val nameFields: List<ContactField> = listOf(FirstName, LastName)
        val addressFields: List<Address> = listOf(Address.Btc, Address.Eth)
        val allFields: List<ContactField> = nameFields + addressFields + Note
    }
}
```

### 2. Typed FormState

Separate data class with named fields, computed `isValid`, polymorphic `getValue()`/`withUpdatedField()`:

```kotlin
data class ContactFormState(
    val firstName: String = "",
    val lastName: String = "",
    val addresses: Map<String, String> = emptyMap(),
    val note: String = "",
) {
    val isValid: Boolean
        get() = firstName.isNotBlank() && addresses.values.any { it.isNotBlank() }

    fun getValue(field: ContactField): String = when (field) {
        ContactField.FirstName -> firstName
        ContactField.LastName -> lastName
        is ContactField.Address -> addresses[field.coinKey].orEmpty()
        ContactField.Note -> note
    }

    fun withUpdatedField(field: ContactField, value: String): ContactFormState = when (field) {
        ContactField.FirstName -> copy(firstName = value)
        ContactField.LastName -> copy(lastName = value)
        is ContactField.Address -> copy(addresses = addresses.toMutableMap().apply { put(field.coinKey, value) })
        ContactField.Note -> copy(note = value)
    }

    companion object {
        fun fromContact(contact: Contact) = ContactFormState(
            firstName = contact.firstName, lastName = contact.lastName,
            addresses = contact.addresses, note = contact.note.orEmpty(),
        )
    }
}
```

### 3. Reusable Field Composable

```kotlin
@Composable
fun ContactFormField(
    field: ContactField,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val placeholder: (@Composable () -> Unit)? = when {
        field.placeholderText != null -> ({ Text(field.placeholderText) })
        field.placeholderRes != null -> ({ Text(stringResource(field.placeholderRes)) })
        else -> null
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(field.labelRes)) },
        placeholder = placeholder,
        modifier = modifier.fillMaxWidth(),
        textStyle = if (field.isMonospace) {
            MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
        } else {
            MaterialTheme.typography.bodySmall
        },
        keyboardOptions = if (field.isAsciiInput) {
            KeyboardOptions(keyboardType = KeyboardType.Ascii)
        } else {
            KeyboardOptions.Default
        },
        singleLine = field.singleLine,
        minLines = field.minLines,
        maxLines = field.maxLines,
        enabled = enabled,
    )
}
```

### 4. ViewModel — FormState outside Reducer

```kotlin
class AddContactViewModel(
    private val contactRepository: ContactRepository
) : BaseViewModelWithEffects<AddContactAction, AddContactEntity, AddContactState, AddContactEffect>() {

    // FormState OUTSIDE reducer — not blocked by long operations
    private val _formState = MutableStateFlow(ContactFormState())
    val formState: StateFlow<ContactFormState> = _formState.asStateFlow()

    init { setAction(AddContactAction.Init) }

    fun updateField(field: ContactField, value: String) {
        _formState.value = _formState.value.withUpdatedField(field, value)
    }

    override suspend fun ReducerScope<AddContactEntity, AddContactState>.reduce(action: AddContactAction) {
        when (action) {
            AddContactAction.Init -> { /* ... */ }
            AddContactAction.SaveContact -> {
                updateUiState { loading(content = content) }
                val form = _formState.value  // read form state here
                // ... save logic, sendEffect(...)
            }
        }
    }
}
```

### 5. Screen — Scaffold + LazyColumn + IME

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddContactViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.imePadding(),  // keyboard-aware
        contentWindowInsets = WindowInsets(0, 0, 0, 0),  // prevent double insets
        topBar = { /* TopAppBar */ },
        bottomBar = {
            Column(
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (!uiState.isLoading) {
                    BarvaButton(
                        onClick = { viewModel.setAction(AddContactAction.SaveContact) },
                        enabled = formState.isValid,
                    ) { Text(stringResource(R.string.save_contact)) }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            uiState.isLoading -> CommonLoading()
            uiState.hasContent -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = padding.calculateTopPadding() + 16.dp,
                        bottom = padding.calculateBottomPadding() + 16.dp,
                        start = 16.dp, end = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(ContactField.nameFields, key = { it::class.simpleName!! }) { field ->
                        ContactFormField(
                            field = field,
                            value = formState.getValue(field),
                            onValueChange = { viewModel.updateField(field, it) },
                            enabled = true,
                        )
                    }
                    item(key = "section_header") {
                        Text("Section header", style = MaterialTheme.typography.labelMedium)
                    }
                    items(ContactField.addressFields, key = { it::class.simpleName!! }) { field ->
                        ContactFormField(
                            field = field,
                            value = formState.getValue(field),
                            onValueChange = { viewModel.updateField(field, it) },
                            enabled = true,
                        )
                    }
                    item(key = "note") {
                        ContactFormField(
                            field = ContactField.Note,
                            value = formState.getValue(ContactField.Note),
                            onValueChange = { viewModel.updateField(ContactField.Note, it) },
                            enabled = true,
                        )
                        Spacer(Modifier.height(40.dp))
                    }
                }
            }
            uiState.showFullScreenError -> { /* Error UI */ }
        }
    }
}
```

**Important notes:**
- `imePadding()` on `Scaffold`, NOT on bottomBar — prevents double padding
- `contentWindowInsets = WindowInsets(0, 0, 0, 0)` — zeroes system insets to avoid double accounting with `imePadding()`
- AndroidManifest: `android:windowSoftInputMode="adjustResize"` (NOT `adjustPan`) for edge-to-edge apps
- `formState` lives as separate `StateFlow` in ViewModel — text input never blocks
- `key = { it::class.simpleName!! }` for sealed class objects in `items()` — stable keys for recomposition
- `BarvaButton` in `bottomBar` — automatically appears above keyboard thanks to `imePadding()`

---

## Gradle Build Configuration

```kotlin
// build.gradle.kts
plugins {
    kotlin("jvm") version "2.0.20"
    kotlin("plugin.serialization") version "2.0.20"
    id("io.ktor.plugin") version "3.0.0"
    id("com.google.devtools.ksp") version "2.0.20-1.0.24"
}

group = "com.example"
version = "1.0.0"

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("com.example.ApplicationKt")
}

dependencies {
    // Ktor Server
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-server-auth-jvm")
    implementation("io.ktor:ktor-server-auth-jwt-jvm")
    implementation("io.ktor:ktor-server-status-pages-jvm")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

    // Koin DI
    implementation("io.insert-koin:koin-ktor:3.5.6")

    // Exposed
    implementation("org.jetbrains.exposed:exposed-core:0.55.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.55.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.55.0")

    // Database
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("com.zaxxer:HikariCP:5.1.0")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.6")

    // Testing
    testImplementation("io.ktor:ktor-server-test-host-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("app.cash.turbine:turbine:1.1.0")
}

tasks.test {
    useJUnitPlatform()
}
```

---

## Docker Configuration

### Multi-Stage Build

```dockerfile
FROM gradle:8.5-jdk21 AS builder
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon

COPY src ./src
RUN gradle shadowJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -g 1000 app && adduser -u 1000 -G app -s /bin/sh -D app
WORKDIR /app
COPY --from=builder /app/build/libs/*-all.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### GraalVM Native Image

```dockerfile
FROM ghcr.io/graalvm/native-image-community:21 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew nativeCompile

FROM gcr.io/distroless/base-debian12
COPY --from=builder /app/build/native/nativeCompile/app /app
ENTRYPOINT ["/app"]
```

### JVM Tuning for Containers

```yaml
containers:
  - name: kotlin-app
    image: myapp:latest
    resources:
      requests:
        memory: "256Mi"
        cpu: "250m"
      limits:
        memory: "512Mi"
        cpu: "500m"
    env:
      - name: JAVA_OPTS
        value: >-
          -XX:+UseContainerSupport
          -XX:MaxRAMPercentage=75.0
          -XX:+UseG1GC
          -XX:+UseStringDeduplication
```

---

## Room Database Setup

```kotlin
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    @ColumnInfo(name = "created_at") val createdAt: Long
)

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUser(id: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserEntity>)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM users")
    suspend fun deleteAll()
}

@Database(entities = [UserEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}

// Hilt module
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }
}
```

---

## Navigation with Type-Safe Args

```kotlin
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Detail : Screen("detail/{itemId}") {
        fun createRoute(itemId: String) = "detail/$itemId"
    }
    object Settings : Screen("settings")
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onItemClick = { itemId ->
                    navController.navigate(Screen.Detail.createRoute(itemId))
                }
            )
        }
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            DetailScreen(
                itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
```

---

## Models and DTOs

```kotlin
@Serializable
data class User(
    val id: Long = 0,
    val name: String,
    val email: String,
    val passwordHash: String,
    val status: UserStatus,
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant = Instant.now(),
    @Serializable(with = InstantSerializer::class)
    val updatedAt: Instant? = null
) {
    fun toDto() = UserDto(id, name, email, status, createdAt)
}

@Serializable
data class UserDto(
    val id: Long,
    val name: String,
    val email: String,
    val status: UserStatus,
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant
)

@Serializable
data class CreateUserRequest(
    val name: String,
    val email: String,
    val password: String
) {
    fun validate() {
        require(name.isNotBlank() && name.length in 2..100) { "Name must be 2-100 characters" }
        require(email.matches(Regex("^[\\w-.]+@[\\w-]+\\.[a-z]{2,}$"))) { "Invalid email format" }
        require(password.length >= 8) { "Password must be at least 8 characters" }
    }
}

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class TokenResponse(
    val token: String,
    val user: UserDto?
)

@Serializable
data class ErrorResponse(
    val message: String,
    val code: String? = null
)

@Serializable
enum class UserStatus {
    PENDING, ACTIVE, SUSPENDED
}

// Exceptions
class ValidationException(message: String) : RuntimeException(message)
class NotFoundException(message: String) : RuntimeException(message)
class DuplicateException(message: String) : RuntimeException(message)
class AuthenticationException(message: String) : RuntimeException(message)
```

---

## Performance Characteristics

### Compilation Times (Kotlin 2.0 with K2)
- Clean build: 30-60% faster than Kotlin 1.9
- Incremental build: 10-20% faster
- KSP processing: 2-3x faster than kapt

### Gradle Configuration
```properties
# gradle.properties
kotlin.experimental.tryK2=true
kotlin.incremental=true
kotlin.daemon.jvmargs=-Xmx4g
org.gradle.parallel=true
org.gradle.caching=true
```

### Runtime Performance
- Inline functions: Zero overhead
- Value classes: Zero runtime allocation
- Coroutines: ~100 bytes per suspended coroutine
- Flow: Minimal allocation per emission

### Coroutine Memory
- Continuation: ~100-200 bytes
- Job: ~300 bytes
- Channel: ~500 bytes per buffer slot

---

## References

**ViewModel-Compose Communication Pattern:**
- Based on [Swissquote Engineering: Bridging the Gap](https://medium.com/swissquote-engineering/bridging-the-gap-an-effective-viewmodel-compose-communication-pattern-853045519839)
- Sample implementation: [Counter App Example](https://github.com/swissquote/example-CounterApp)

**Additional Resources:**
- [Official Android Architecture Guide](https://developer.android.com/topic/architecture)
- [MVI Pattern](https://hannesdorfmann.com/android/model-view-intent/)
- [ViewModel Events Antipatterns](https://manuelvivo.dev/viewmodel-events-antipatterns)

---

Last Updated: 2026-01-30
