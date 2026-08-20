package vhn.dev.study_chines.ui.quiz

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import vhn.dev.study_chines.ui.theme.MucGiayColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(viewModel: QuizViewModel, onNavigateBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MucGiayColors.Paper,
        contentColor = MucGiayColors.Ink,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Trở về", tint = MucGiayColors.InkSoft)
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
            when {
                uiState.isLoading -> CircularProgressIndicator(color = MucGiayColors.JadeFill)
                uiState.step == QuizStep.FINISHED -> FinishContent(correct = uiState.correctCount, wrong = uiState.wrongCount, onBack = onNavigateBack)
                else -> QuizContent(uiState = uiState, viewModel = viewModel, onBack = onNavigateBack)
            }
        }
    }
}

// ===== FINISH SCREEN =====
@Composable
private fun FinishContent(correct: Int, wrong: Int, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.weight(0.15f))
        Icon(Icons.Default.CheckCircle, contentDescription = null,
            modifier = Modifier.size(56.dp), tint = MucGiayColors.Jade)
        Spacer(Modifier.height(12.dp))
        Text("Hoàn thành", style = MaterialTheme.typography.headlineMedium, color = MucGiayColors.Ink)
        Text("Hôm nay bạn đã thuộc $correct từ", color = MucGiayColors.InkSoft, fontSize = 14.sp)

        Spacer(Modifier.height(28.dp))
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(horizontal = 32.dp).fillMaxWidth()) {
            StatColumn(value = "$correct", label = "Thuộc được", color = MucGiayColors.Jade)
            Spacer(Modifier.width(40.dp))
            StatColumn(value = "$wrong", label = "Lần sai", color = MucGiayColors.SealSon)
        }

        Spacer(Modifier.weight(0.2f))
        Button(
            onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = MucGiayColors.SealSon),
            shape = RoundedCornerShape(10.dp), modifier = Modifier.height(52.dp).fillMaxWidth().padding(horizontal = 32.dp)
        ) { Text("Về trang chủ", fontWeight = FontWeight.SemiBold) }
        Spacer(Modifier.weight(0.1f))
    }
}

@Composable
private fun StatColumn(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.displayLarge.copy(fontSize = 36.sp), color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, letterSpacing = spToEm(0.02f))
    }
}

// ===== QUIZ CONTENT =====
@Composable
private fun QuizContent(uiState: QuizState, viewModel: QuizViewModel, onBack: () -> Unit) {
    val vocab = uiState.currentVocab ?: return
    var showContinue by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isAnswerSelected) { if (uiState.isAnswerSelected) showContinue = true }

    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress bar + counter
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Spacer(Modifier.width(8.dp))
            Box(Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(9999.dp)).background(MucGiayColors.Hairline)) {
                val progress = (uiState.remainingVocabs - (if (uiState.step == QuizStep.FINISHED) 0 else 1)).toFloat() / maxOf(1, uiState.remainingVocabs).toFloat()
                Box(Modifier.fillMaxWidth(progress.coerceIn(0f..1f)).background(MucGiayColors.JadeFill))
            }
            Spacer(Modifier.width(8.dp))
            Text("${uiState.remainingVocabs - if(uiState.isAnswerSelected && !uiState.isCorrect) 1 else 0}/${uiState.remainingVocabs}",
                style = MaterialTheme.typography.labelMedium, color = MucGiayColors.InkSoft)
        }

        Spacer(Modifier.height(16.dp))

        // Step chips
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            StepChip(text = "1 Phiên Âm", active = uiState.step == QuizStep.PINYIN_VALIDATION)
            Text(",", color = MucGiayColors.InkFaint, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 4.dp))
            StepChip(text = "2 Nghĩa", active = uiState.step == QuizStep.MEANING_VALIDATION)
        }

        Spacer(Modifier.height(16.dp))

        // Flashcard - thẻ giấy
        FlashCard(hanzi = vocab.hanzi)

        Spacer(Modifier.height(16.dp))

        // Options instruction
        Text(if (uiState.step == QuizStep.PINYIN_VALIDATION) "Chọn phiên âm đúng:" else "Chọn nghĩa đúng:",
            style = MaterialTheme.typography.labelSmall, letterSpacing = spToEm(0.06f))

        Spacer(Modifier.height(8.dp))

        // Options list
        val ordinals = listOf('甲','乙','丙','丁')
        uiState.options.forEachIndexed { idx, option ->
            val isSelected = uiState.isAnswerSelected
            val isCorrectAnswer = when (uiState.step) {
                QuizStep.PINYIN_VALIDATION -> option == vocab.pinyin
                QuizStep.MEANING_VALIDATION -> option == vocab.meaning
                QuizStep.FINISHED -> false
            }

            OptionRow(
                ordinal = ordinals[idx],
                text = option,
                enabled = !isSelected,
                state = when {
                    !isSelected -> OptionState.Normal
                    isCorrectAnswer -> OptionState.Correct
                    else -> OptionState.Wrong
                },
                onClick = { viewModel.submitAnswer(option) }
            )
        }

        // Feedback + Continue button
        if (showContinue) {
            Spacer(Modifier.height(16.dp))
            Text(
                if (uiState.isCorrect) "Chính xác" else "Chưa đúng",
                style = MaterialTheme.typography.labelMedium,
                color = if (uiState.isCorrect) MucGiayColors.Jade else MucGiayColors.SealDeep,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { viewModel.nextStep(); showContinue = false },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.5.dp, MucGiayColors.Hairline),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text("Tiếp tục", fontWeight = FontWeight.SemiBold, color = MucGiayColors.InkSoft, fontSize = 16.sp) }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ===== FLASHCARD COMPONENT =====
@Composable
private fun FlashCard(hanzi: String) {
    Card(
        modifier = Modifier
            .widthIn(max = 220.dp)
            .aspectRatio(1f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MucGiayColors.PaperDeep),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.5.dp, MucGiayColors.Hairline)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            // Grid lines background
            Box(Modifier.matchParentSize()) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val gridColor = MucGiayColors.Hairline.copy(alpha = 0.4f)
                    drawLine(gridColor, Offset(size.width * 0.5f, 0f), Offset(size.width * 0.5f, size.height), strokeWidth = 1.dp.toPx())
                    drawLine(gridColor, Offset(0f, size.height * 0.5f), Offset(size.width, size.height * 0.5f), strokeWidth = 1.dp.toPx())
                }
            }
            // Hanzi character
            Text(hanzi,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Black,
                fontSize = 64.sp,
                color = MucGiayColors.Ink,
                lineHeight = 1.em
            )
            // Seal stamp
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .size(26.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(MucGiayColors.SealSon),
                contentAlignment = Alignment.Center
            ) {
                Text("学", fontFamily = FontFamily.Serif, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ===== STEP CHIP =====
@Composable
private fun StepChip(text: String, active: Boolean) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (active) MucGiayColors.SealSon else MucGiayColors.PaperDeep,
        contentColor = if (active) Color.White else MucGiayColors.InkFaint
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp))
    }
}

// ===== OPTION ROW =====
enum class OptionState { Normal, Correct, Wrong }

@Composable
private fun OptionRow(ordinal: Char, text: String, enabled: Boolean, state: OptionState, onClick: () -> Unit) {
    val bgColor = when (state) {
        OptionState.Correct -> MucGiayColors.JadeTint
        OptionState.Wrong -> MucGiayColors.RedBg
        else -> Color.Transparent
    }
    val textColor = when (state) {
        OptionState.Correct -> MucGiayColors.Jade
        OptionState.Wrong -> MucGiayColors.SealDeep
        else -> MucGiayColors.Ink
    }
    val ordinalColor = when (state) {
        OptionState.Correct -> MucGiayColors.Jade
        OptionState.Wrong -> MucGiayColors.SealDeep
        else -> MucGiayColors.InkFaint
    }

    Surface(
        onClick = { if (enabled) onClick() },
        shape = RectangleShape,
        color = bgColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(ordinal.toString(), fontFamily = FontFamily.Serif, fontSize = 15.sp, color = ordinalColor)
            Spacer(Modifier.width(12.dp))
            Text(text, fontSize = 14.sp, color = textColor, fontWeight = if (state == OptionState.Correct) FontWeight.SemiBold else FontWeight.Normal)
        }
    }
    HorizontalDivider(color = MucGiayColors.Hairline, thickness = 0.5.dp)
}

private fun spToEm(v: Float): androidx.compose.ui.unit.TextUnit = v.em
