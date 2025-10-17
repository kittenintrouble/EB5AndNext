package com.eb5.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.eb5.app.data.model.ArticleStatus
import androidx.compose.ui.res.stringResource
import com.eb5.app.R
import com.eb5.app.ui.quizzes.QuizzesRoute
import com.eb5.app.ui.quizzes.QuizzesViewModel
import com.eb5.app.ui.navigation.AppDestination
import com.eb5.app.ui.navigation.BottomDestination
import com.eb5.app.ui.navigation.bottomDestinations
import com.eb5.app.EB5Application
import com.eb5.app.ui.screens.ArticleDetailScreen
import com.eb5.app.ui.screens.BaseScreen
import com.eb5.app.ui.screens.HomeScreen
import com.eb5.app.ui.screens.OnboardingScreen
import com.eb5.app.ui.screens.ProgressScreen
import com.eb5.app.ui.screens.ProjectDetailScreen
import com.eb5.app.ui.screens.ProjectsScreen
import com.eb5.app.ui.screens.NewsDetailScreen
import com.eb5.app.ui.screens.QuizResultScreen
import com.eb5.app.ui.screens.QuizRunnerScreen
import com.eb5.app.ui.screens.SettingsScreen
import com.eb5.app.ui.theme.EB5Theme
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eb5.app.ui.news.NewsDetailViewModel
import com.eb5.app.ui.projects.ProjectDetailViewModel
import android.util.Log
import kotlinx.coroutines.flow.collectLatest

private const val BaseReturnSavedStateKey = "base_return_snapshot"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EB5App(viewModel: AppViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    EB5Theme(language = state.language) {
        when {
            state.isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            !state.onboardingCompleted -> OnboardingScreen(
                language = state.language,
                availableLanguages = state.availableLanguages,
                onLanguageChanged = viewModel::setLanguage,
                onContinue = { viewModel.completeOnboarding(it) },
                articles = state.articles.takeIf { it.isNotEmpty() }
            )

            else -> {
                if (state.error != null) {
                    LaunchedEffect(state.error) {
                        scope.launch {
                            snackbarHostState.showSnackbar(message = state.error ?: "")
                        }
                    }
                }
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                LaunchedEffect(currentRoute) {
                    viewModel.updateCurrentDestination(currentRoute)
                }
                LaunchedEffect(state.pendingRestoreRoute) {
                    val target = state.pendingRestoreRoute
                    when {
                        target == null -> Unit
                        target == navController.currentDestination?.route -> {
                            viewModel.clearPendingRestoreRoute()
                        }
                        else -> {
                            navController.navigate(target) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                            viewModel.clearPendingRestoreRoute()
                        }
                    }
                }
                LaunchedEffect(state.pendingNewsArticleId, state.pendingNewsLanguage) {
                    val articleId = state.pendingNewsArticleId ?: return@LaunchedEffect
                    val language = state.pendingNewsLanguage
                    val route = AppDestination.NewsDetail.routeWithId(articleId, language)
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = false
                    }
                    viewModel.clearPendingNewsArticle()
                }
                LaunchedEffect(state.pendingProjectId, state.pendingProjectLanguage) {
                    val projectId = state.pendingProjectId ?: return@LaunchedEffect
                    val language = state.pendingProjectLanguage
                    val route = AppDestination.ProjectDetail.routeWithId(projectId, language)
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = false
                    }
                    viewModel.clearPendingProject()
                }
                Scaffold(
                    topBar = {
                        val title = when {
                            currentRoute?.startsWith(AppDestination.Home.route) == true -> stringResource(R.string.title_home)
                            currentRoute?.startsWith(AppDestination.Base.route) == true -> stringResource(R.string.title_base)
                            currentRoute?.startsWith(AppDestination.Quizzes.route) == true -> stringResource(R.string.title_quizzes)
                            currentRoute?.startsWith(AppDestination.Progress.route) == true -> stringResource(R.string.title_progress)
                            currentRoute?.startsWith(AppDestination.Projects.route) == true -> stringResource(R.string.title_projects)
                            currentRoute?.startsWith(AppDestination.Settings.route) == true -> stringResource(R.string.title_settings)
                            currentRoute?.startsWith(AppDestination.ArticleDetail.route.substringBefore("/{")) == true -> stringResource(R.string.title_article_detail)
                            currentRoute?.startsWith(AppDestination.QuizRunner.route.substringBefore("/{")) == true -> stringResource(R.string.title_quiz_runner)
                            currentRoute?.startsWith(AppDestination.QuizResult.route.substringBefore("/{")) == true -> stringResource(R.string.title_quiz_result)
                            currentRoute?.startsWith(AppDestination.ProjectDetail.route.substringBefore("/{")) == true -> stringResource(R.string.title_project_detail)
                            else -> stringResource(R.string.app_name)
                        }
                        TopAppBar(
                            title = { Text(title) },
                            actions = {
                                if (currentRoute?.startsWith(AppDestination.Settings.route) != true &&
                                    currentRoute?.startsWith(AppDestination.Onboarding.route) != true
                                ) {
                                    IconButton(onClick = { navController.navigate(AppDestination.Settings.route) }) {
                                        Icon(imageVector = Icons.Default.Settings, contentDescription = stringResource(R.string.cd_open_settings))
                                    }
                                }
                            }
                        )
                    },
                    bottomBar = {
                        if (bottomDestinations.any { currentRoute?.startsWith(it.route) == true }) {
                            BottomNavigationBar(
                                navController = navController,
                                destinations = bottomDestinations,
                                currentRoute = currentRoute,
                                onBaseSelected = viewModel::resetBaseScreen
                            )
                        }
                    },
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = AppDestination.Home.route,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        composable(AppDestination.Home.route) {
                            HomeScreen(
                                modifier = Modifier.fillMaxSize(),
                                articles = state.articles,
                                articleStatuses = state.articleStatuses,
                                favorites = state.favorites,
                                news = state.news,
                                newsFavorites = state.newsFavorites,
                                projects = state.projects,
                                projectFavorites = state.projectFavorites,
                                onExploreArticles = {
                                    navController.navigate(AppDestination.Base.route)
                                },
                                onOpenArticle = { articleId ->
                                    viewModel.setArticleReturnRoute(AppDestination.Home.route)
                                    navController.navigate(AppDestination.ArticleDetail.routeWithId(articleId))
                                },
                                onToggleFavorite = viewModel::toggleFavorite,
                                onOpenNews = { article ->
                                    navController.navigate(
                                        AppDestination.NewsDetail.routeWithId(
                                            article.id,
                                            state.language.tag
                                        )
                                    )
                                },
                                onToggleNewsFavorite = viewModel::toggleNewsFavorite,
                                onOpenProject = { project ->
                                    navController.navigate(AppDestination.ProjectDetail.routeWithId(project.id))
                                },
                                onToggleProjectFavorite = viewModel::toggleProjectFavorite
                            )
                        }
                        composable(AppDestination.Base.route) { baseBackStackEntry ->
                            val savedStateHandle = baseBackStackEntry.savedStateHandle
                            LaunchedEffect(savedStateHandle) {
                                savedStateHandle.getStateFlow<AppViewModel.BaseReturnSnapshot?>(BaseReturnSavedStateKey, null)
                                    .collectLatest { snapshot ->
                                        snapshot ?: return@collectLatest
                                        viewModel.restoreBaseFromSnapshot(snapshot)
                                        savedStateHandle[BaseReturnSavedStateKey] = null
                                    }
                            }
                            BaseScreen(
                                articles = state.articles,
                                articleStatuses = state.articleStatuses,
                                favorites = state.favorites,
                                onToggleFavorite = viewModel::toggleFavorite,
                                onToggleStatus = viewModel::toggleArticleStatus,
                                onArticleSelected = { articleId ->
                                    viewModel.setArticleReturnRoute(AppDestination.Base.route)
                                    navController.navigate(AppDestination.ArticleDetail.routeWithId(articleId))
                                },
                                scrollToArticleId = state.pendingScrollArticleId,
                                onScrollHandled = viewModel::clearPendingArticleFocus,
                                resetToken = state.baseResetToken,
                                baseReturn = state.baseReturn,
                                selectedCategory = state.baseSelectedCategory,
                                selectedSubcategory = state.baseSelectedSubcategory,
                                onSelectCategory = viewModel::selectBaseCategory,
                                onSelectSubcategory = viewModel::selectBaseSubcategory,
                                onRecordBaseReturn = viewModel::recordBaseReturn,
                                onConsumeBaseReturn = viewModel::clearBaseReturn
                            )
                        }
                        composable(AppDestination.Quizzes.route) {
                            val quizzesViewModel: QuizzesViewModel = viewModel()
                            QuizzesRoute(
                                viewModel = quizzesViewModel,
                                onOpenQuiz = { quizId ->
                                    navController.navigate(AppDestination.QuizRunner.routeWithId(quizId))
                                },
                                onOpenTrack = { trackId ->
                                    navController.navigate(AppDestination.QuizTrackDetail.routeWithId(trackId))
                                }
                            )
                        }
                        composable(AppDestination.Progress.route) {
                            ProgressScreen(
                                news = state.news,
                                newsFavorites = state.newsFavorites,
                                isLoading = state.isLoading,
                                onToggleNewsFavorite = viewModel::toggleNewsFavorite,
                                onOpenNews = { article ->
                                    navController.navigate(
                                        AppDestination.NewsDetail.routeWithId(
                                            article.id,
                                            state.language.tag
                                        )
                                    )
                                }
                            )
                        }
                        composable(AppDestination.Projects.route) {
                            ProjectsScreen(
                                projects = state.projects,
                                projectFavorites = state.projectFavorites,
                                onToggleProjectFavorite = viewModel::toggleProjectFavorite,
                                onProjectSelected = { project ->
                                    navController.navigate(AppDestination.ProjectDetail.routeWithId(project.id))
                                }
                            )
                        }
                        composable(AppDestination.QuizTrackDetail.route) { backStackEntry ->
                            val trackId = backStackEntry.arguments?.getString("trackId")
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = trackId ?: "", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                        composable(AppDestination.Settings.route) {
                            SettingsScreen(
                                language = state.language,
                                availableLanguages = state.availableLanguages,
                                onLanguageChanged = viewModel::setLanguage,
                                onClose = { _ ->
                                    navController.navigateUp()
                                },
                                appVersion = state.versionName
                            )
                        }
                        composable(AppDestination.ArticleDetail.route) { backStackEntry ->
                            val articleId = backStackEntry.arguments?.getString("articleId")?.toIntOrNull()
                            val article = state.articles.firstOrNull { it.id == articleId }
                            if (article != null) {
                                ArticleDetailScreen(
                                    article = article,
                                    status = state.articleStatuses[article.id] ?: ArticleStatus.IN_PROGRESS,
                                    isFavorite = article.id in state.favorites,
                                    onToggleFavorite = { viewModel.toggleFavorite(article.id) },
                                    onToggleStatus = { viewModel.toggleArticleStatus(article.id) },
                                    onBack = {
                                        val currentState = viewModel.uiState.value
                                        val baseSnapshot = currentState.baseReturn
                                        val recordedRoute = currentState.pendingArticleReturnRoute
                                        val previousRoute = navController.previousBackStackEntry?.destination?.route
                                        val targetRoute = recordedRoute ?: previousRoute

                                        if (baseSnapshot != null) {
                                            navController.previousBackStackEntry?.savedStateHandle?.set(BaseReturnSavedStateKey, baseSnapshot)
                                            Log.d("ArticleDetailBack", "Restoring base snapshot category=${baseSnapshot.category} subcategory=${baseSnapshot.subcategory}")
                                            viewModel.activateBaseReturn()
                                        } else when {
                                            targetRoute?.startsWith(AppDestination.Home.route) == true -> viewModel.focusArticleOnReturn(article.id)
                                            previousRoute?.startsWith(AppDestination.Home.route) == true -> viewModel.focusArticleOnReturn(article.id)
                                        }

                                        val popped = navController.popBackStack()
                                        if (!popped) {
                                            val fallback = when {
                                                baseSnapshot != null -> AppDestination.Base.route
                                                targetRoute != null -> targetRoute
                                                else -> AppDestination.Home.route
                                            }
                                            val fallbackPrefix = fallback.substringBefore("/{")
                                            val alreadyOnFallback = navController.currentDestination?.route?.startsWith(fallbackPrefix) == true
                                            if (!alreadyOnFallback) {
                                                navController.navigate(fallback) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        }

                                        viewModel.clearArticleReturnRoute()
                                    }
                                )
                            }
                        }
                        composable(
                            route = AppDestination.NewsDetail.route,
                            arguments = listOf(
                                navArgument("articleId") { type = NavType.StringType },
                                navArgument("lang") {
                                    type = NavType.StringType
                                    defaultValue = ""
                                }
                            ),
                            deepLinks = listOf(
                                navDeepLink { uriPattern = "eb5news://article?id={articleId}&lang={lang}" }
                            )
                        ) { backStackEntry ->
                            val articleId = backStackEntry.arguments?.getString("articleId") ?: return@composable
                            val langArg = backStackEntry.arguments?.getString("lang").orEmpty()
                            val app = LocalContext.current.applicationContext as EB5Application
                            val newsDetailViewModel: NewsDetailViewModel = viewModel(
                                factory = viewModelFactory {
                                    initializer {
                                        NewsDetailViewModel(
                                            newsRepository = app.container.newsRepository,
                                            articleId = articleId,
                                            languageCode = langArg
                                        )
                                    }
                                }
                            )
                            val newsState by newsDetailViewModel.uiState.collectAsStateWithLifecycle()
                            NewsDetailScreen(
                                state = newsState,
                                isFavorite = state.newsFavorites.contains(articleId),
                                onToggleFavorite = { viewModel.toggleNewsFavorite(articleId) },
                                onBack = {
                                    val fallbackRoute = state.pendingNewsReturnRoute
                                    val popped = navController.popBackStack()
                                    if (fallbackRoute != null) {
                                        val currentRoute = navController.currentDestination?.route
                                        val fallbackPrefix = fallbackRoute.substringBefore("/{")
                                        val alreadyOnFallback = currentRoute?.startsWith(fallbackPrefix) == true
                                        if (!alreadyOnFallback) {
                                            navController.navigate(fallbackRoute) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                        viewModel.clearPendingNewsReturnRoute()
                                    } else if (!popped) {
                                        navController.navigate(AppDestination.Home.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                        viewModel.clearPendingNewsReturnRoute()
                                    } else {
                                        viewModel.clearPendingNewsReturnRoute()
                                    }
                                },
                                onRetry = { newsDetailViewModel.reload() }
                            )
                        }
                        composable(AppDestination.QuizRunner.route) { backStackEntry ->
                            val quizId = backStackEntry.arguments?.getString("quizId") ?: return@composable
                            val topic = state.quizzes.firstOrNull { it.id == quizId }
                            if (topic != null) {
                                QuizRunnerScreen(
                                    topic = topic,
                                    onCompleted = { score ->
                                        viewModel.recordQuizResult(topic.id, score)
                                        navController.navigate(AppDestination.QuizResult.routeWithScore(topic.id, score))
                                    },
                                    onExit = { navController.navigateUp() }
                                )
                            }
                        }
                        composable(AppDestination.QuizResult.route) { backStackEntry ->
                            val quizId = backStackEntry.arguments?.getString("quizId") ?: return@composable
                            val score = backStackEntry.arguments?.getString("score")?.toIntOrNull() ?: 0
                            val topic = state.quizzes.firstOrNull { it.id == quizId }
                            if (topic != null) {
                                QuizResultScreen(
                                    topic = topic,
                                    score = score,
                                    onRetake = {
                                        navController.navigate(AppDestination.QuizRunner.routeWithId(topic.id))
                                    },
                                    onBack = { navController.popBackStack(AppDestination.Quizzes.route, false) }
                                )
                            }
                        }
                        composable(
                            route = AppDestination.ProjectDetail.route,
                            arguments = listOf(
                                navArgument("projectId") { type = NavType.StringType },
                                navArgument("lang") {
                                    type = NavType.StringType
                                    defaultValue = ""
                                }
                            ),
                            deepLinks = listOf(
                                navDeepLink { uriPattern = "eb5projects://project?id={projectId}&lang={lang}" },
                                navDeepLink { uriPattern = "eb5projects://project?id={projectId}" }
                            )
                        ) { backStackEntry ->
                            val projectId = backStackEntry.arguments?.getString("projectId") ?: return@composable
                            val langArg = backStackEntry.arguments?.getString("lang").orEmpty()
                            val app = LocalContext.current.applicationContext as EB5Application
                            val projectDetailViewModel: ProjectDetailViewModel = viewModel(
                                factory = viewModelFactory {
                                    initializer {
                                        ProjectDetailViewModel(
                                            projectRepository = app.container.projectRepository,
                                            projectId = projectId,
                                            languageCode = langArg.ifBlank { state.language.tag }
                                        )
                                    }
                                }
                            )
                            val projectState by projectDetailViewModel.uiState.collectAsStateWithLifecycle()
                            val project = projectState.project
                            if (project != null) {
                                ProjectDetailScreen(
                                    state = projectState,
                                    isFavorite = state.projectFavorites.contains(projectId),
                                    onToggleFavorite = { viewModel.toggleProjectFavorite(projectId) },
                                    onBack = {
                                        val fallback = state.pendingProjectReturnRoute
                                        val popped = navController.popBackStack()
                                        if (fallback != null) {
                                            val currentRoute = navController.currentDestination?.route
                                            val fallbackPrefix = fallback.substringBefore("/{")
                                            val alreadyOnFallback = currentRoute?.startsWith(fallbackPrefix) == true
                                            if (!alreadyOnFallback) {
                                                navController.navigate(fallback) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                            viewModel.clearPendingProjectReturnRoute()
                                        } else if (!popped) {
                                            navController.navigate(AppDestination.Home.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                            viewModel.clearPendingProjectReturnRoute()
                                        } else {
                                            viewModel.clearPendingProjectReturnRoute()
                                        }
                                    },
                                    onRetry = { projectDetailViewModel.reload() }
                                )
                            } else if (!projectState.isLoading) {
                                LaunchedEffect(projectState) {
                                    viewModel.refreshProjects(force = false)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(
    navController: NavHostController,
    destinations: List<BottomDestination>,
    currentRoute: String?,
    onBaseSelected: () -> Unit = {}
) {
    NavigationBar {
        destinations.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute?.startsWith(destination.route) == true,
                onClick = {
                    when (destination.destination) {
                        AppDestination.Home -> {
                            val popped = navController.popBackStack(AppDestination.Home.route, false)
                            if (!popped) {
                                navController.navigate(AppDestination.Home.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                        AppDestination.Base -> {
                            onBaseSelected()
                            navController.popBackStack(AppDestination.Home.route, false)
                            navController.navigate(AppDestination.Base.route) {
                                launchSingleTop = true
                                restoreState = false
                            }
                        }
                        else -> {
                            val startRoute = navController.graph.startDestinationRoute
                            navController.navigate(destination.route) {
                                startRoute?.let {
                                    popUpTo(it) {
                                        saveState = true
                                    }
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                },
                icon = {
                    Icon(imageVector = destination.icon, contentDescription = null)
                },
                label = { Text(text = stringResource(destination.labelRes)) }
            )
        }
    }
}
