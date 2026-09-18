@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package vhn.dev.study_chines.ui.classifier

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import vhn.dev.study_chines.R
import vhn.dev.study_chines.audio.ChineseSpeechManager
import vhn.dev.study_chines.data.local.UserPreferences
import vhn.dev.study_chines.data.model.ClassifierCollocation
import vhn.dev.study_chines.data.model.ClassifierPoint
import vhn.dev.study_chines.ui.quiz.SoundManager
import vhn.dev.study_chines.ui.theme.MucGiayColors

@Composable
fun ClassifierScreen(
    viewModel: ClassifierViewModel,
    preferences: UserPreferences,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val speechManager = remember { ChineseSpeechManager(context, preferences) }
    val soundManager = remember { SoundManager(context, preferences) }

    DisposableEffect(Unit) {
        onDispose {
            speechManager.release()
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showLessonDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MucGiayColors.Paper,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showLessonDialog = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    uiState.lessonTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MucGiayColors.Ink
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = "Chọn bài",
                                    tint = MucGiayColors.InkSoft
                                )
                            }
                            Text(
                                "HSK ${uiState.hskLevel} • Lượng từ chuẩn BLCUP",
                                fontSize = 11.sp,
                                color = MucGiayColors.Indigo,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = MucGiayColors.Ink
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MucGiayColors.Paper)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Selector: Thẻ học & Cụm từ vs Luyện tập
            TabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = MucGiayColors.PaperDeep,
                contentColor = MucGiayColors.Indigo
            ) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.switchTab(0) },
                    text = {
                        Text(
                            "🏷️ Thẻ Lượng từ (${uiState.classifiers.size})",
                            fontWeight = if (uiState.selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (uiState.selectedTab == 0) MucGiayColors.Indigo else MucGiayColors.InkSoft
                        )
                    }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.switchTab(1) },
                    text = {
                        Text(
                            "✍ Luyện tập (${uiState.exercises.size})",
                            fontWeight = if (uiState.selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (uiState.selectedTab == 1) MucGiayColors.Indigo else MucGiayColors.InkSoft
                        )
                    }
                )
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MucGiayColors.Indigo)
                }
            } else if (uiState.selectedTab == 0) {
                // Tab 0: Thẻ Lượng từ & Danh từ ghép đôi
                ClassifierCardsTab(
                    classifiers = uiState.classifiers,
                    onSpeak = { text -> speechManager.speak(text) }
                )
            } else {
                // Tab 1: Luyện tập trắc nghiệm
                ClassifierPracticeTab(
                    uiState = uiState,
                    onSelectOption = { option ->
                        viewModel.selectOption(option) { isCorrect, phrase ->
                            if (isCorrect) {
                                soundManager.play(R.raw.correct)
                                speechManager.speak(phrase)
                            } else {
                                soundManager.play(R.raw.wrong)
                            }
                        }
                    },
                    onNext = {
                        viewModel.nextExercise()
                        if (uiState.currentExerciseIndex + 1 >= uiState.exercises.size) {
                            soundManager.play(R.raw.finish)
                        }
                    },
                    onRestart = { viewModel.restartPractice() },
                    onSpeak = { text -> speechManager.speak(text) }
                )
            }
        }
    }

    // Dialog chọn bài học lượng từ
    if (showLessonDialog) {
        AlertDialog(
            onDismissRequest = { showLessonDialog = false },
            title = {
                Text(
                    "Chọn bài học (HSK ${uiState.hskLevel})",
                    fontWeight = FontWeight.Bold,
                    color = MucGiayColors.Ink
                )
            },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                    items(uiState.availableLessons) { lessonItem ->
                        val isCurrent = lessonItem.lessonNum == uiState.lessonNum
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    showLessonDialog = false
                                    viewModel.selectLesson(lessonItem.hskLevel, lessonItem.lessonNum)
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isCurrent) MucGiayColors.IndigoTint else MucGiayColors.PaperDeep,
                            border = if (isCurrent) BorderStroke(1.dp, MucGiayColors.Indigo) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Bài ${lessonItem.lessonNum}",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrent) MucGiayColors.Indigo else MucGiayColors.Ink,
                                    fontSize = 14.sp
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    lessonItem.title,
                                    color = MucGiayColors.InkSoft,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                if (lessonItem.classifiers.isNotEmpty()) {
                                    Text(
                                        lessonItem.classifiers.joinToString(" "),
                                        fontSize = 12.sp,
                                        color = MucGiayColors.Indigo,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLessonDialog = false }) {
                    Text("Đóng", color = MucGiayColors.Indigo)
                }
            },
            containerColor = MucGiayColors.Paper
        )
    }
}

@Composable
private fun ClassifierCardsTab(
    classifiers: List<ClassifierPoint>,
    onSpeak: (String) -> Unit
) {
    if (classifiers.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Không có dữ liệu lượng từ cho bài này.", color = MucGiayColors.InkFaint)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(classifiers) { cl ->
            ClassifierDetailCard(cl = cl, onSpeak = onSpeak)
        }
    }
}

@Composable
private fun ClassifierDetailCard(
    cl: ClassifierPoint,
    onSpeak: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.2.dp, MucGiayColors.Indigo.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MucGiayColors.PaperDeep)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header: Big character in Hanzi grid box + Pinyin + Audio + Meaning
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MucGiayColors.Paper)
                        .border(1.5.dp, MucGiayColors.Indigo.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        cl.classifier,
                        fontFamily = FontFamily.Serif,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MucGiayColors.Indigo
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            cl.pinyin,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MucGiayColors.Ink
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = { onSpeak(cl.classifier) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text("🔊", fontSize = 16.sp)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MucGiayColors.IndigoTint
                    ) {
                        Text(
                            cl.meaning,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MucGiayColors.Indigo,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Usage Note / Memory tip
            if (!cl.usageNote.isNullOrBlank()) {
                Spacer(Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MucGiayColors.Paper,
                    border = BorderStroke(1.dp, MucGiayColors.Hairline)
                ) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                        Text("💡", fontSize = 14.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            cl.usageNote,
                            fontSize = 12.5.sp,
                            color = MucGiayColors.InkSoft,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Collocations Grid / List
            if (cl.collocations.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "DANH TỪ KẾT HỢP TIÊU BIỂU (BẤM ĐỂ PHÁT ÂM 🔊)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MucGiayColors.Indigo,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    cl.collocations.forEach { col ->
                        CollocationRow(col = col, onSpeak = onSpeak)
                    }
                }
            }
        }
    }
}

@Composable
private fun CollocationRow(
    col: ClassifierCollocation,
    onSpeak: (String) -> Unit
) {
    val phraseText = col.phrase.ifEmpty { col.noun }
    Surface(
        onClick = { onSpeak(phraseText) },
        shape = RoundedCornerShape(8.dp),
        color = MucGiayColors.Paper,
        border = BorderStroke(0.8.dp, MucGiayColors.Hairline.copy(alpha = 0.8f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        phraseText,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MucGiayColors.Ink
                    )
                    if (col.pinyin.isNotBlank()) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            col.pinyin,
                            fontSize = 12.sp,
                            color = MucGiayColors.InkFaint
                        )
                    }
                }
                if (col.meaning.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        col.meaning,
                        fontSize = 12.sp,
                        color = MucGiayColors.InkSoft
                    )
                }
            }
            Text("🔊", fontSize = 16.sp)
        }
    }
}

@Composable
private fun ClassifierPracticeTab(
    uiState: ClassifierUiState,
    onSelectOption: (String) -> Unit,
    onNext: () -> Unit,
    onRestart: () -> Unit,
    onSpeak: (String) -> Unit
) {
    if (uiState.exercises.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text("Chưa có câu hỏi luyện tập cho bài này.", color = MucGiayColors.InkFaint)
        }
        return
    }

    if (uiState.isPracticeFinished) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🎉", fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                "Hoàn thành xuất sắc!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MucGiayColors.Ink
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Bạn đã hoàn thành các câu hỏi lượng từ của ${uiState.lessonTitle}",
                color = MucGiayColors.InkSoft,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MucGiayColors.PaperDeep,
                border = BorderStroke(1.dp, MucGiayColors.Hairline)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(30.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Chính xác", fontSize = 12.sp, color = MucGiayColors.Jade)
                        Text("${uiState.correctCount}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MucGiayColors.Jade)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Chưa đúng", fontSize = 12.sp, color = MucGiayColors.SealSon)
                        Text("${uiState.wrongCount}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MucGiayColors.SealSon)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(containerColor = MucGiayColors.Indigo),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(0.7f).height(46.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Luyện tập lại", fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    val currentEx = uiState.exercises[uiState.currentExerciseIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Progress Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "CÂU ${uiState.currentExerciseIndex + 1} / ${uiState.exercises.size}",
                style = MaterialTheme.typography.labelSmall,
                color = MucGiayColors.Indigo,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("✓ ${uiState.correctCount}", color = MucGiayColors.Jade, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("✗ ${uiState.wrongCount}", color = MucGiayColors.SealSon, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { (uiState.currentExerciseIndex + 1).toFloat() / uiState.exercises.size },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = MucGiayColors.Indigo,
            trackColor = MucGiayColors.PaperDeep
        )

        Spacer(Modifier.height(20.dp))

        // Question Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MucGiayColors.Hairline, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MucGiayColors.PaperDeep)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        currentEx.question,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MucGiayColors.Ink,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        val textToSpeak = currentEx.question.replace(Regex("""\(?\s*___\s*\)?"""), currentEx.answer).substringAfter(":")
                        onSpeak(if (textToSpeak.isNotBlank()) textToSpeak else currentEx.answer)
                    }) {
                        Text("🔊", fontSize = 18.sp)
                    }
                }
                if (currentEx.pinyin.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(currentEx.pinyin, fontSize = 13.sp, color = MucGiayColors.InkFaint)
                }
                if (currentEx.meaning.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(currentEx.meaning, fontSize = 13.sp, color = MucGiayColors.InkSoft)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Options List (A, B, C, D)
        val optionLabels = listOf("A", "B", "C", "D")
        currentEx.options.forEachIndexed { index, option ->
            val isSelected = uiState.selectedOption == option
            val isCorrectOption = option.trim() == currentEx.answer.trim()

            val bgColor = when {
                !uiState.isAnswered -> if (isSelected) MucGiayColors.IndigoTint else MucGiayColors.PaperDeep
                isCorrectOption -> MucGiayColors.JadeTint
                isSelected && !isCorrectOption -> MucGiayColors.RedBg
                else -> MucGiayColors.PaperDeep
            }

            val borderColor = when {
                !uiState.isAnswered -> if (isSelected) MucGiayColors.Indigo else MucGiayColors.Hairline
                isCorrectOption -> MucGiayColors.Jade
                isSelected && !isCorrectOption -> MucGiayColors.SealSon
                else -> MucGiayColors.Hairline
            }

            Surface(
                onClick = { if (!uiState.isAnswered) onSelectOption(option) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(10.dp),
                color = bgColor,
                border = BorderStroke(1.2.dp, borderColor)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (isSelected || (uiState.isAnswered && isCorrectOption)) borderColor else MucGiayColors.Paper),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            optionLabels.getOrElse(index) { "${index + 1}" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isSelected || (uiState.isAnswered && isCorrectOption)) Color.White else MucGiayColors.InkSoft
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Text(
                        option,
                        fontFamily = FontFamily.Serif,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MucGiayColors.Ink,
                        modifier = Modifier.weight(1f)
                    )

                    if (uiState.isAnswered) {
                        if (isCorrectOption) {
                            Icon(Icons.Default.Check, contentDescription = "Đúng", tint = MucGiayColors.Jade)
                        } else if (isSelected) {
                            Icon(Icons.Default.Clear, contentDescription = "Sai", tint = MucGiayColors.SealSon)
                        }
                    }
                }
            }
        }

        // Explanation & Next Button
        if (uiState.isAnswered) {
            Spacer(Modifier.height(14.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (uiState.isCorrect) MucGiayColors.JadeTint else MucGiayColors.RedBg,
                border = BorderStroke(1.dp, if (uiState.isCorrect) MucGiayColors.Jade else MucGiayColors.SealSon)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text(
                        if (uiState.isCorrect) "✓ Chính xác!" else "✗ Chưa chính xác!",
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.isCorrect) MucGiayColors.Jade else MucGiayColors.SealSon,
                        fontSize = 14.sp
                    )
                    if (currentEx.explanation.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            currentEx.explanation,
                            fontSize = 13.sp,
                            color = MucGiayColors.Ink,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MucGiayColors.Indigo),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    if (uiState.currentExerciseIndex + 1 >= uiState.exercises.size) "Xem kết quả ➔" else "Câu tiếp theo ➔",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
