package vhn.dev.study_chines.ui.quiz

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import vhn.dev.study_chines.R
import vhn.dev.study_chines.audio.ChineseSpeechManager
import vhn.dev.study_chines.data.local.UserPreferences
import vhn.dev.study_chines.ui.theme.MucGiayColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(viewModel: QuizViewModel, preferences: UserPreferences, onNavigateBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    // Âm thanh hiệu ứng & giọng đọc tiếng Trung
    val sound = remember {
        SoundManager(context, preferences).apply {
            loadDefault(R.raw.correct, R.raw.wrong, R.raw.finish)
        }
    }
    val speech = remember { ChineseSpeechManager(context, preferences) }

    DisposableEffect(Unit) {
        onDispose {
            sound.release()
            speech.release()
        }
    }

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

            // CHỈ PHÁT ÂM KHI HOÀN THÀNH BƯỚC CHỌN PINYIN, KHÔNG PHÁT ÂM KHI CHỌN NGHĨA
            if (uiState.step == QuizStep.PINYIN_VALIDATION) {
                uiState.currentVocab?.let { v ->
                    delay(200)
                    speech.speak(v.hanzi.ifBlank { v.pinyin })
                }
            }
        }
    }

    // Fanfare khi hoàn thành
    LaunchedEffect(uiState.step) {
        if (uiState.step == QuizStep.FINISHED && !uiState.isLoading) {
            speech.stop()
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
                    IconButton(onClick = {
                        speech.stop()
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Trở về", tint = MucGiayColors.InkSoft)
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
            when {
                uiState.isLoading -> CircularProgressIndicator(color = MucGiayColors.JadeFill)
                uiState.step == QuizStep.FINISHED -> FinishContent(
                    correct = uiState.correctCount,
                    wrong = uiState.wrongCount,
                    isRepractice = uiState.isRepractice,
                    onBack = onNavigateBack
                )
                else -> QuizContent(uiState = uiState, viewModel = viewModel, speechManager = speech)
            }
        }
    }
}

// ===== QUIZ CONTENT =====
@Composable
private fun QuizContent(
    uiState: QuizState,
    viewModel: QuizViewModel,
    speechManager: ChineseSpeechManager
) {
    val vocab = uiState.currentVocab ?: return
    var showContinue by remember { mutableStateOf(false) }
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
            Text(
                "${uiState.remainingVocabs - if(uiState.isAnswerSelected && !uiState.isCorrect) 1 else 0}/${uiState.remainingVocabs}",
                style = MaterialTheme.typography.labelMedium,
                color = MucGiayColors.InkSoft
            )
        }

        Spacer(Modifier.height(16.dp))

        // Step chips
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            StepChip(text = "1 Phiên Âm", active = uiState.step == QuizStep.PINYIN_VALIDATION)
            Text(" • ", color = MucGiayColors.InkFaint, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 4.dp))
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
        Text(
            if (uiState.step == QuizStep.PINYIN_VALIDATION) "Chọn phiên âm đúng:" else "Chọn nghĩa đúng:",
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = spToEm(0.06f)
        )

        Spacer(Modifier.height(8.dp))

        // Options list
        val ordinals = listOf('A', 'B', 'C', 'D')
        uiState.options.forEachIndexed { idx, option ->
            val isSelected = uiState.isAnswerSelected
            val isCorrectAnswer = when (uiState.step) {
                QuizStep.PINYIN_VALIDATION -> option == vocab.pinyin
                QuizStep.MEANING_VALIDATION -> option == vocab.meaning
                QuizStep.FINISHED -> false
            }

            OptionRow(
                ordinal = ordinals.getOrElse(idx) { '-' },
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

        // Feedback + Loa nghe lại + Nút tiếp tục
        AnimatedVisibility(
            visible = showContinue,
            enter = fadeIn(tween(200)) + expandVertically(tween(250)),
            exit = fadeOut(tween(120))
        ) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(14.dp))

                // Trạng thái đúng / sai
                Text(
                    if (uiState.isCorrect) "✓ Chính xác!" else "✗ Chưa đúng!",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (uiState.isCorrect) MucGiayColors.Jade else MucGiayColors.SealDeep,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                // Khung hiển thị đáp án chuẩn & nút LOA nghe lại
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (uiState.isCorrect) MucGiayColors.JadeTint else MucGiayColors.RedBg,
                    border = BorderStroke(1.dp, if (uiState.isCorrect) MucGiayColors.Jade.copy(alpha = 0.3f) else MucGiayColors.SealSon.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Đáp án chuẩn: ",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MucGiayColors.InkSoft
                                )
                                Text(
                                    vocab.pinyin,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.isCorrect) MucGiayColors.Jade else MucGiayColors.SealSon,
                                    fontFamily = FontFamily.Serif
                                )
                            }
                            if (vocab.meaning.isNotBlank()) {
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    "Nghĩa: ${vocab.meaning}" + (if (!vocab.wordType.isNullOrBlank()) " (${vocab.wordType})" else ""),
                                    fontSize = 12.5.sp,
                                    color = MucGiayColors.InkSoft
                                )
                            }
                        }

                        // Nút loa phát lại (Replay Speaker)
                        IconButton(
                            onClick = {
                                speechManager.speak(vocab.hanzi.ifBlank { vocab.pinyin }, forcePlay = true)
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (uiState.isCorrect) MucGiayColors.Jade else MucGiayColors.SealSon)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Nghe lại phát âm",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        if (continueClickable) {
                            continueClickable = false
                            speechManager.stop()
                            viewModel.nextStep()
                            showContinue = false
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.5.dp, MucGiayColors.Hairline),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MucGiayColors.SealSon,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Tiếp tục", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                }

                Spacer(Modifier.height(16.dp))
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
    val borderColor = when (state) {
        OptionState.Correct -> MucGiayColors.Jade
        OptionState.Wrong -> MucGiayColors.SealDeep
        else -> Color.Transparent
    }

    val scale by animateFloatAsState(
        targetValue = if (state == OptionState.Correct) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "optionScale"
    )
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
