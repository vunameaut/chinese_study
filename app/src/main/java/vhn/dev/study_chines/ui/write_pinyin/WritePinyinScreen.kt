package vhn.dev.study_chines.ui.write_pinyin

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import vhn.dev.study_chines.R
import vhn.dev.study_chines.data.local.UserPreferences
import vhn.dev.study_chines.ui.quiz.FinishContent
import vhn.dev.study_chines.ui.quiz.FlashCard
import vhn.dev.study_chines.ui.quiz.SoundManager
import vhn.dev.study_chines.ui.quiz.spToEm
import vhn.dev.study_chines.ui.theme.MucGiayColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WritePinyinScreen(
    viewModel: WritePinyinViewModel,
    preferences: UserPreferences,
    onNavigateBack: () -> Unit
) {
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

    // Phát âm thanh khi kiểm tra đáp án
    var answerSoundKey by remember { mutableIntStateOf(0) }
    LaunchedEffect(uiState.isChecked) {
        if (uiState.isChecked && !uiState.isFinished) {
            answerSoundKey++
        }
    }
    LaunchedEffect(answerSoundKey) {
        if (answerSoundKey > 0 && uiState.isChecked && !uiState.isFinished) {
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
        }
    }

    // Fanfare khi hoàn thành
    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished && !uiState.isLoading) {
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator(color = MucGiayColors.JadeFill)
                uiState.isFinished -> FinishContent(correct = uiState.correctCount, wrong = uiState.wrongCount, onBack = onNavigateBack)
                else -> WritePinyinContent(uiState = uiState, viewModel = viewModel)
            }
        }
    }
}

// ===== WRITE PINYIN CONTENT =====
@Composable
private fun WritePinyinContent(uiState: WritePinyinState, viewModel: WritePinyinViewModel) {
    val vocab = uiState.currentVocab ?: return
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    // Chống double-tap: nút chỉ bấm được sau 400ms
    var continueClickable by remember { mutableStateOf(false) }
    var showContinue by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isChecked) {
        if (uiState.isChecked) {
            showContinue = true
            continueClickable = false
            delay(400)
            continueClickable = true
        } else {
            showContinue = false
            continueClickable = false
        }
    }

    // Auto-focus TextField và tự động cuộn xuống khi chuyển từ mới
    LaunchedEffect(vocab.id) {
        delay(300)
        try { focusRequester.requestFocus() } catch (_: Exception) {}
        delay(150)
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    // Tự động cuộn khi kết quả hiển thị
    LaunchedEffect(showContinue) {
        if (showContinue) {
            delay(100)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress bar + counter
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier.weight(1f).height(6.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(MucGiayColors.Hairline)
            ) {
                val total = uiState.remainingVocabs
                val done = uiState.correctCount
                val progress = if (total + done > 0) done.toFloat() / (total + done).toFloat() else 0f
                Box(Modifier.fillMaxWidth(progress.coerceIn(0f..1f)).fillMaxHeight().background(MucGiayColors.JadeFill))
            }
            Spacer(Modifier.width(8.dp))
            Text(
                "${uiState.remainingVocabs}",
                style = MaterialTheme.typography.labelMedium,
                color = MucGiayColors.InkSoft
            )
        }

        Spacer(Modifier.height(10.dp))

        // Mode label
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MucGiayColors.AmberTint
        ) {
            Text(
                "✏ Viết Pinyin",
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = MucGiayColors.Amber,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }

        Spacer(Modifier.height(10.dp))

        // Flashcard với animation trượt khi chuyển từ (tối ưu kích thước 160dp khi mở bàn phím)
        AnimatedContent(
            targetState = vocab.hanzi,
            transitionSpec = {
                (slideInHorizontally(animationSpec = tween(340)) { it } +
                    fadeIn(tween(300)) +
                    scaleIn(animationSpec = tween(340), initialScale = 0.8f)) togetherWith
                    (slideOutHorizontally(animationSpec = tween(240)) { -it } + fadeOut(tween(180)))
            },
            label = "flashcard"
        ) { h ->
            FlashCard(
                hanzi = h,
                modifier = Modifier
                    .widthIn(max = 160.dp)
                    .aspectRatio(1f),
                fontSize = 54.sp
            )
        }

        Spacer(Modifier.height(14.dp))

        // Instruction
        Text(
            "Nhập pinyin cho chữ trên:",
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = spToEm(0.06f)
        )
        Text(
            "Chấp nhận cả có dấu (xué, xue2) và không dấu (xue). Dùng v thay ü.",
            fontSize = 11.sp,
            color = MucGiayColors.InkFaint,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, start = 16.dp, end = 16.dp)
        )

        Spacer(Modifier.height(12.dp))

        // Input field
        OutlinedTextField(
            value = uiState.userInput,
            onValueChange = { viewModel.onInputChange(it) },
            enabled = !uiState.isChecked,
            singleLine = true,
            placeholder = { Text("Nhập pinyin...", color = MucGiayColors.InkFaint) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                keyboardType = KeyboardType.Ascii,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (!uiState.isChecked && uiState.userInput.isNotBlank()) {
                        keyboardController?.hide()
                        viewModel.checkAnswer()
                    }
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MucGiayColors.Jade,
                unfocusedBorderColor = MucGiayColors.Hairline,
                disabledBorderColor = if (uiState.isChecked && uiState.isCorrect) MucGiayColors.Jade
                    else if (uiState.isChecked) MucGiayColors.SealSon
                    else MucGiayColors.Hairline,
                disabledTextColor = if (uiState.isChecked && uiState.isCorrect) MucGiayColors.Jade
                    else if (uiState.isChecked) MucGiayColors.SealDeep
                    else MucGiayColors.Ink,
                cursorColor = MucGiayColors.Ink
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            trailingIcon = {
                if (uiState.isChecked) {
                    if (uiState.isCorrect) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MucGiayColors.Jade)
                    } else {
                        Icon(Icons.Default.Close, contentDescription = null, tint = MucGiayColors.SealDeep)
                    }
                }
            }
        )

        Spacer(Modifier.height(12.dp))

        // Check button (trước khi kiểm tra)
        AnimatedVisibility(
            visible = !uiState.isChecked,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(100))
        ) {
            Button(
                onClick = {
                    keyboardController?.hide()
                    viewModel.checkAnswer()
                },
                enabled = uiState.userInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MucGiayColors.Jade,
                    disabledContainerColor = MucGiayColors.Hairline
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Kiểm tra", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }

        // Feedback + Continue (sau khi kiểm tra)
        AnimatedVisibility(
            visible = showContinue,
            enter = fadeIn(tween(200)) + expandVertically(tween(250)),
            exit = fadeOut(tween(120))
        ) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(8.dp))

                // Kết quả
                Text(
                    if (uiState.isCorrect) "Chính xác!" else "Chưa đúng",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (uiState.isCorrect) MucGiayColors.Jade else MucGiayColors.SealDeep,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // Hiện đáp án đúng khi sai
                if (!uiState.isCorrect && vocab.pinyin.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MucGiayColors.JadeTint,
                        border = BorderStroke(1.dp, MucGiayColors.Jade.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Đáp án đúng:", fontSize = 11.sp, color = MucGiayColors.InkSoft)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                vocab.pinyin,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MucGiayColors.Jade,
                                fontFamily = FontFamily.Serif
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Nút tiếp tục
                OutlinedButton(
                    onClick = {
                        if (continueClickable) {
                            continueClickable = false
                            viewModel.nextWord()
                            showContinue = false
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.5.dp, MucGiayColors.Hairline),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text(
                        "Tiếp tục",
                        fontWeight = FontWeight.SemiBold,
                        color = MucGiayColors.InkSoft,
                        fontSize = 16.sp
                    )
                }

                Spacer(Modifier.height(16.dp))
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}
