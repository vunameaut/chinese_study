package vhn.dev.study_chines.ui.quiz

import androidx.compose.ui.platform.LocalContext
import vhn.dev.study_chines.R
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlin.random.Random
import vhn.dev.study_chines.ui.theme.MucGiayColors
import vhn.dev.study_chines.data.local.UserPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(viewModel: QuizViewModel, preferences: UserPreferences, onNavigateBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    // Âm thanh hiệu ứng
    val context = LocalContext.current
    val sound = remember { 
        SoundManager(context, preferences).apply {
            loadDefault(R.raw.correct, R.raw.wrong, R.raw.finish)
        }
    }
    DisposableEffect(Unit) { onDispose { sound.release() } }


    // Đếm số lần chọn đáp án để tránh phát âm thanh trùng khi step thay đổi
    var answerSoundKey by remember { mutableIntStateOf(0) }
    LaunchedEffect(uiState.isAnswerSelected, uiState.step) {
        if (uiState.isAnswerSelected && uiState.step != QuizStep.FINISHED) {
            answerSoundKey++
        }
    }
    LaunchedEffect(answerSoundKey) {
        if (answerSoundKey > 0 && uiState.isAnswerSelected && uiState.step != QuizStep.FINISHED) {
            if (uiState.isCorrect) {
                // Arpeggio vui tươi: 3 nốt tăng dần
                sound.play(R.raw.correct)
                delay(70)
                sound.play(R.raw.correct)
                delay(70)
                sound.play(R.raw.correct)
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            } else {
                sound.play(R.raw.wrong)
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
    }

    // Fanfare khi hoàn thành
    LaunchedEffect(uiState.step) {
        if (uiState.step == QuizStep.FINISHED && !uiState.isLoading) {
            sound.play(R.raw.finish)
            delay(140)
            sound.play(R.raw.finish)
            delay(140)
            sound.play(R.raw.finish)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

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
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }
    val iconScale by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "finishIcon"
    )

    Box(Modifier.fillMaxSize()) {
        ConfettiRain(Modifier.fillMaxSize())
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.weight(0.15f))
            Icon(Icons.Default.CheckCircle, contentDescription = null,
                modifier = Modifier.size(56.dp).scale(iconScale), tint = MucGiayColors.Jade)
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
}

@Composable
private fun StatColumn(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.displayLarge.copy(fontSize = 36.sp), color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, letterSpacing = spToEm(0.02f))
    }
}

// ===== CONFETTI =====
private data class Particle(val x: Float, val y: Float, val speed: Float, val size: Float, val color: Color)

@Composable
private fun ConfettiRain(modifier: Modifier = Modifier) {
    val colors = listOf(
        Color(0xFFE53935), Color(0xFFFFB300), Color(0xFF43A047),
        Color(0xFF1E88E5), Color(0xFFEC407A), Color(0xFF8E24AA),
        Color(0xFFFF7043), Color(0xFF00ACC1)
    )
    val particles = remember {
        List(60) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                speed = Random.nextFloat() * 0.6f + 0.3f,
                size = Random.nextFloat() * 7f + 4f,
                color = colors[Random.nextInt(colors.size)]
            )
        }
    }
    val transition = rememberInfiniteTransition(label = "confetti")
    val progress by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3200, easing = LinearEasing)),
        label = "confettiProgress"
    )
    Canvas(modifier = modifier) {
        particles.forEach { p ->
            val y = ((p.y + progress * p.speed) % 1.05f) * size.height
            val sway = kotlin.math.sin((progress * 6f + p.x * 10f).toFloat()) * 20f
            drawCircle(
                color = p.color,
                radius = p.size,
                center = Offset(p.x * size.width + sway, y)
            )
        }
    }
}

// ===== QUIZ CONTENT =====
@Composable
private fun QuizContent(uiState: QuizState, viewModel: QuizViewModel, onBack: () -> Unit) {
    val vocab = uiState.currentVocab ?: return
    var showContinue by remember { mutableStateOf(false) }
    // Chống double-tap: nút "Tiếp tục" chỉ bấm được sau 400ms kể từ khi xuất hiện
    var continueClickable by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isAnswerSelected) {
        if (uiState.isAnswerSelected) {
            showContinue = true
            continueClickable = false
            delay(400) // Đợi animation xong mới cho bấm
            continueClickable = true
        } else {
            showContinue = false
            continueClickable = false
        }
    }

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

        // Flashcard với animation trượt khi chuyển từ
        AnimatedContent(
            targetState = vocab.hanzi,
            transitionSpec = {
                (slideInHorizontally(animationSpec = tween(340)) { it } +
                    fadeIn(tween(300)) +
                    scaleIn(animationSpec = tween(340), initialScale = 0.8f)) togetherWith
                    (slideOutHorizontally(animationSpec = tween(240)) { -it } + fadeOut(tween(180)))
            },
            label = "flashcard"
        ) { h -> FlashCard(hanzi = h) }

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
                    option == uiState.selectedAnswer -> OptionState.Wrong
                    else -> OptionState.Normal
                },
                onClick = { viewModel.submitAnswer(option) }
            )
        }

        // Feedback + Continue button
        AnimatedVisibility(
            visible = showContinue,
            enter = fadeIn(tween(200)) + expandVertically(tween(250)),
            exit = fadeOut(tween(120))
        ) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
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
                    onClick = { if (continueClickable) { continueClickable = false; viewModel.nextStep(); showContinue = false } },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.5.dp, MucGiayColors.Hairline),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) { Text("Tiếp tục", fontWeight = FontWeight.SemiBold, color = MucGiayColors.InkSoft, fontSize = 16.sp) }
                Spacer(Modifier.height(16.dp))
            }
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
            Box(Modifier.matchParentSize()) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val gridColor = MucGiayColors.Hairline.copy(alpha = 0.4f)
                    drawLine(gridColor, Offset(size.width * 0.5f, 0f), Offset(size.width * 0.5f, size.height), strokeWidth = 1.dp.toPx())
                    drawLine(gridColor, Offset(0f, size.height * 0.5f), Offset(size.width, size.height * 0.5f), strokeWidth = 1.dp.toPx())
                }
            }
            Text(hanzi,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Black,
                fontSize = 64.sp,
                color = MucGiayColors.Ink,
                lineHeight = 1.em
            )
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

// ===== OPTION ROW (có animation: đúng thì nảy, sai thì rung) =====
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
    val borderColor = when (state) {
        OptionState.Correct -> MucGiayColors.Jade
        OptionState.Wrong -> MucGiayColors.SealDeep
        else -> Color.Transparent
    }

    // Scale nảy khi đúng (bouncy hơn)
    val scale by animateFloatAsState(
        targetValue = if (state == OptionState.Correct) 1.06f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "optionScale"
    )
    // Rung khi sai
    val shake = remember { Animatable(0f) }
    LaunchedEffect(state) {
        if (state == OptionState.Wrong) {
            val amp = 12f
            for (i in 1..3) {
                shake.animateTo(amp, tween(45))
                shake.animateTo(-amp, tween(45))
            }
            shake.animateTo(0f, tween(45))
        }
    }

    Surface(
        onClick = { if (enabled) onClick() },
        shape = RectangleShape,
        color = bgColor,
        border = if (state == OptionState.Normal) null else BorderStroke(2.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .graphicsLayer { translationX = shake.value }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(ordinal.toString(), fontFamily = FontFamily.Serif, fontSize = 15.sp, color = ordinalColor)
            Spacer(Modifier.width(12.dp))
            Text(text, fontSize = 14.sp, color = textColor, fontWeight = if (state == OptionState.Correct) FontWeight.SemiBold else FontWeight.Normal, modifier = Modifier.weight(1f))
            if (state == OptionState.Correct) {
                Icon(Icons.Default.Check, contentDescription = null, tint = MucGiayColors.Jade, modifier = Modifier.size(20.dp))
            } else if (state == OptionState.Wrong) {
                Icon(Icons.Default.Close, contentDescription = null, tint = MucGiayColors.SealDeep, modifier = Modifier.size(20.dp))
            }
        }
    }
    HorizontalDivider(color = MucGiayColors.Hairline, thickness = 0.5.dp)
}

private fun spToEm(v: Float): androidx.compose.ui.unit.TextUnit = v.em
