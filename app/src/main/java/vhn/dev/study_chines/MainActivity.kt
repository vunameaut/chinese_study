package vhn.dev.study_chines

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import vhn.dev.study_chines.data.local.UserPreferences
import vhn.dev.study_chines.data.remote.SupabaseDataSource
import vhn.dev.study_chines.data.repository.StudyRepository
import vhn.dev.study_chines.ui.home.HomeScreen
import vhn.dev.study_chines.ui.home.HomeViewModel
import vhn.dev.study_chines.ui.quiz.QuizScreen
import vhn.dev.study_chines.ui.quiz.QuizViewModel
import vhn.dev.study_chines.ui.settings.SettingsScreen
import vhn.dev.study_chines.ui.settings.SettingsViewModel
import vhn.dev.study_chines.ui.write_pinyin.WritePinyinScreen
import vhn.dev.study_chines.ui.write_pinyin.WritePinyinViewModel
import vhn.dev.study_chines.ui.theme.HanziQuizTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferences = UserPreferences(this)
        val dataSource = SupabaseDataSource()
        val repository = StudyRepository(dataSource)

        setContent {
            HanziQuizTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = vhn.dev.study_chines.ui.theme.MucGiayColors.Paper) {
                    StudyChineseApp(repository = repository, preferences = preferences)
                }
            }
        }
    }
}

@Composable
fun StudyChineseApp(repository: StudyRepository, preferences: UserPreferences) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            val vm: HomeViewModel = viewModel(factory = vmFactory { HomeViewModel(repository, preferences) })
            HomeScreen(
                viewModel = vm,
                onNavigateToQuiz = { id ->
                    vm.saveLastSession(id)
                    navController.navigate("quiz/$id")
                },
                onNavigateToWritePinyin = { id ->
                    vm.saveLastSession(id)
                    navController.navigate("write_pinyin/$id")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                }
            )
        }
        composable("settings") {
            val vm: SettingsViewModel = viewModel(factory = vmFactory { SettingsViewModel(preferences) })
            SettingsScreen(viewModel = vm, onNavigateBack = { navController.popBackStack() })
        }
        composable(
            route = "quiz/{sessionId}",
            arguments = listOf(navArgument("sessionId") { type = NavType.IntType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getInt("sessionId") ?: 0
            val vm: QuizViewModel = viewModel(factory = vmFactory { QuizViewModel(repository, sessionId) })
            QuizScreen(viewModel = vm, preferences = preferences, onNavigateBack = { navController.popBackStack() })
        }
        composable(
            route = "write_pinyin/{sessionId}",
            arguments = listOf(navArgument("sessionId") { type = NavType.IntType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getInt("sessionId") ?: 0
            val vm: WritePinyinViewModel = viewModel(factory = vmFactory { WritePinyinViewModel(repository, sessionId) })
            WritePinyinScreen(viewModel = vm, preferences = preferences, onNavigateBack = { navController.popBackStack() })
        }
    }
}

private fun vmFactory(create: () -> ViewModel) = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = create() as T
}