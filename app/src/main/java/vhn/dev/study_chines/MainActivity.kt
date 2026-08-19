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
import vhn.dev.study_chines.data.local.AppDatabase
import vhn.dev.study_chines.data.repository.StudyRepository
import vhn.dev.study_chines.ui.entry.EntryScreen
import vhn.dev.study_chines.ui.entry.EntryViewModel
import vhn.dev.study_chines.ui.home.HomeScreen
import vhn.dev.study_chines.ui.home.HomeViewModel
import vhn.dev.study_chines.ui.quiz.QuizScreen
import vhn.dev.study_chines.ui.quiz.QuizViewModel
import vhn.dev.study_chines.ui.theme.HanziQuizTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getDatabase(this)
        val repo = StudyRepository(db.vocabularyDao(), db.sessionDao())

        setContent {
            HanziQuizTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = vhn.dev.study_chines.ui.theme.MucGiayColors.Paper) {
                    StudyChineseApp(repo)
                }
            }
        }
    }
}

@Composable
fun StudyChineseApp(repository: StudyRepository) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            val vm: HomeViewModel = viewModel(factory = vmFactory { HomeViewModel(repository) })
            HomeScreen(
                viewModel = vm,
                onNavigateToEntry = { id -> navController.navigate("entry/$id") },
                onNavigateToQuiz = { id -> navController.navigate("quiz/$id") }
            )
        }
        composable(
            route = "entry/{sessionId}",
            arguments = listOf(navArgument("sessionId") { type = NavType.IntType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getInt("sessionId") ?: 0
            val vm: EntryViewModel = viewModel(factory = vmFactory { EntryViewModel(repository, sessionId.toLong()) })
            EntryScreen(viewModel = vm, onNavigateBack = { navController.popBackStack() })
        }
        composable(
            route = "quiz/{sessionId}",
            arguments = listOf(navArgument("sessionId") { type = NavType.IntType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getInt("sessionId") ?: 0
            val vm: QuizViewModel = viewModel(factory = vmFactory { QuizViewModel(repository, sessionId) })
            QuizScreen(viewModel = vm, onNavigateBack = { navController.popBackStack() })
        }
    }
}

private fun vmFactory(create: () -> ViewModel) = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = create() as T
}