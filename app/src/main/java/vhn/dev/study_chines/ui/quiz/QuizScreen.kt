package vhn.dev.study_chines.ui.quiz

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    viewModel: QuizViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ôn tập (${uiState.remainingVocabs} từ)") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else if (uiState.step == QuizStep.FINISHED) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Hoàn thành ôn tập!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onNavigateBack) {
                        Text("Trở về trang chủ")
                    }
                }
            } else {
                QuizContent(uiState = uiState, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun QuizContent(uiState: QuizState, viewModel: QuizViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val currentVocab = uiState.currentVocab ?: return

        // Flashcard (Hanzi)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = currentVocab.hanzi,
                    fontSize = 80.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Instruction
        Text(
            text = if (uiState.step == QuizStep.PINYIN_VALIDATION) "Chọn Pinyin đúng" else "Chọn Nghĩa đúng",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Options Grid (hoặc List)
        uiState.options.forEach { option ->
            val isSelected = uiState.isAnswerSelected
            val isCorrectAnswer = when (uiState.step) {
                QuizStep.PINYIN_VALIDATION -> option == currentVocab.pinyin
                QuizStep.MEANING_VALIDATION -> option == currentVocab.meaning
                else -> false
            }

            val buttonColor = if (isSelected) {
                if (isCorrectAnswer) Color.Green.copy(alpha = 0.5f)
                else Color.Red.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }

            Button(
                onClick = { viewModel.submitAnswer(option) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                enabled = !isSelected
            ) {
                Text(
                    text = option,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Next Button (hiện khi đã chọn đáp án)
        if (uiState.isAnswerSelected) {
            Button(
                onClick = { viewModel.nextStep() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tiếp tục")
            }
        }
    }
}
