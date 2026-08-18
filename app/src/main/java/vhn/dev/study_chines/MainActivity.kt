package vhn.dev.study_chines

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import vhn.dev.study_chines.data.local.AppDatabase
import vhn.dev.study_chines.data.repository.VocabularyRepository
import vhn.dev.study_chines.ui.entry.EntryScreen
import vhn.dev.study_chines.ui.entry.EntryViewModel
import vhn.dev.study_chines.ui.home.HomeScreen
import vhn.dev.study_chines.ui.quiz.QuizScreen
import vhn.dev.study_chines.ui.quiz.QuizViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Khởi tạo Database và Repository (trong thực tế nên dùng Hilt/Dagger)
        val database = AppDatabase.getDatabase(this)
        val repository = VocabularyRepository(database.vocabularyDao())
        
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StudyChineseApp(repository)
                }
            }
        }
    }
}

@Composable
fun StudyChineseApp(repository: VocabularyRepository) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onNavigateToEntry = { navController.navigate("entry") },
                onNavigateToQuiz = { navController.navigate("quiz") }
            )
        }
        
        composable("entry") {
            val viewModel: EntryViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return EntryViewModel(repository) as T
                    }
                }
            )
            EntryScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable("quiz") {
            val viewModel: QuizViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return QuizViewModel(repository) as T
                    }
                }
            )
            QuizScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
