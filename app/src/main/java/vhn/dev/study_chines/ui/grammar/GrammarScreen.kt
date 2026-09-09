@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package vhn.dev.study_chines.ui.grammar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import vhn.dev.study_chines.audio.ChineseSpeechManager
import vhn.dev.study_chines.data.local.UserPreferences
import vhn.dev.study_chines.data.model.GrammarExample
import vhn.dev.study_chines.data.model.GrammarExercise
import vhn.dev.study_chines.data.model.GrammarPoint
import vhn.dev.study_chines.ui.theme.MucGiayColors


@Composable
fun GrammarScreen(
    viewModel: GrammarViewModel,
    preferences: UserPreferences,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val speechManager = remember { ChineseSpeechManager(context, preferences) }
    DisposableEffect(Unit) {
        onDispose { speechManager.release() }
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
                                "Chuẩn HSK ${uiState.hskLevel} • BLCUP",
                                fontSize = 11.sp,
                                color = MucGiayColors.Jade,
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
            // Tab Selector: Lý thuyết & Ví dụ vs Luyện tập
            TabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = MucGiayColors.PaperDeep,
                contentColor = MucGiayColors.Jade
            ) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.switchTab(0) },
                    text = {
                        Text(
                            "📖 Lý thuyết & Ví dụ",
                            fontWeight = if (uiState.selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.switchTab(1) },
                    text = {
                        Text(
                            "✍ Luyện tập (${uiState.exercises.size})",
                            fontWeight = if (uiState.selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MucGiayColors.JadeFill)
                }
            } else {
                if (uiState.selectedTab == 0) {
                    GrammarTheoryView(
                        points = uiState.grammarPoints,
                        onSpeak = { text -> speechManager.speak(text, forcePlay = true) }
                    )
                } else {
                    GrammarPracticeView(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    // Dialog chọn bài học nhanh
    if (showLessonDialog) {
        AlertDialog(
            onDismissRequest = { showLessonDialog = false },
            title = { Text("Chọn bài học HSK", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                    items(uiState.availableLessons) { item ->
                        val isSelected = item.hskLevel == uiState.hskLevel && item.lessonNum == uiState.lessonNum
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable {
                                    viewModel.selectLesson(item.hskLevel, item.lessonNum)
                                    showLessonDialog = false
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MucGiayColors.JadeTint else Color.Transparent,
                            border = if (isSelected) BorderStroke(1.dp, MucGiayColors.Jade) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    item.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MucGiayColors.Jade else MucGiayColors.Ink,
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MucGiayColors.Jade)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLessonDialog = false }) {
                    Text("Đóng")
                }
            }
        )
    }
}

// ==========================================
// VIEW 1: LÝ THUYẾT & VÍ DỤ
// ==========================================
@Composable
private fun GrammarTheoryView(
    points: List<GrammarPoint>,
    onSpeak: (String) -> Unit
) {
    if (points.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📖", fontSize = 48.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    "Chưa có điểm ngữ pháp cho bài học này",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MucGiayColors.InkSoft,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Nội dung đang được cập nhật từ giáo trình chuẩn BLCUP",
                    color = MucGiayColors.InkFaint,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(points) { point ->
            GrammarPointCard(point = point, onSpeak = onSpeak)
        }
    }
}

@Composable
private fun GrammarPointCard(
    point: GrammarPoint,
    onSpeak: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MucGiayColors.Hairline, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MucGiayColors.PaperDeep)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Tiêu đề điểm ngữ pháp
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MucGiayColors.JadeTint),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${point.orderIndex}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MucGiayColors.Jade
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    point.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MucGiayColors.Ink
                )
            }

            // Khung Công thức / Cấu trúc
            if (!point.structure.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MucGiayColors.JadeTint,
                    border = BorderStroke(1.dp, MucGiayColors.Jade.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "CẤU TRÚC TRỌNG TÂM",
                            style = MaterialTheme.typography.labelSmall,
                            color = MucGiayColors.Jade,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            point.structure,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.5.sp,
                            color = MucGiayColors.Ink
                        )
                    }
                }
            }

            // Giải thích chi tiết
            if (!point.explanation.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                val cleanExplanation = point.explanation
                    .replace("<br>", "\n")
                    .replace("<br/>", "\n")
                    .replace("<b>", "")
                    .replace("</b>", "")
                Text(
                    cleanExplanation,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = MucGiayColors.InkSoft
                )
            }

            // Danh sách các câu ví dụ
            if (point.examples.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "CÂU VÍ DỤ MINH HỌA",
                    style = MaterialTheme.typography.labelSmall,
                    color = MucGiayColors.Amber,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    point.examples.forEach { ex ->
                        ExampleItem(example = ex, onSpeak = onSpeak)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExampleItem(
    example: GrammarExample,
    onSpeak: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MucGiayColors.Paper,
        border = BorderStroke(0.8.dp, MucGiayColors.Hairline.copy(alpha = 0.8f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    example.hanzi,
                    fontFamily = FontFamily.Serif,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MucGiayColors.Ink
                )
                if (example.pinyin.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        example.pinyin,
                        fontStyle = FontStyle.Italic,
                        fontSize = 13.sp,
                        color = MucGiayColors.Amber
                    )
                }
                if (example.meaning.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        example.meaning,
                        fontSize = 13.sp,
                        color = MucGiayColors.InkSoft
                    )
                }
            }

            IconButton(onClick = { onSpeak(example.hanzi) }) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MucGiayColors.PaperDeep),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Phát âm",
                        tint = MucGiayColors.SealSon,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// VIEW 2: LUYỆN TẬP NGỮ PHÁP TƯƠNG TÁC
// ==========================================
@Composable
private fun GrammarPracticeView(
    uiState: GrammarUiState,
    viewModel: GrammarViewModel
) {
    if (uiState.exercises.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("✍", fontSize = 48.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    "Chưa có bài tập cho bài học này",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MucGiayColors.InkSoft,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    if (uiState.isPracticeFinished) {
        // Màn hình kết thúc
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MucGiayColors.Jade,
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Hoàn thành luyện tập ngữ pháp!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MucGiayColors.Ink,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Bạn đã hoàn thành các câu hỏi bài tập của bài học này.",
                color = MucGiayColors.InkSoft,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))

            // Thống kê kết quả
            Row(
                modifier = Modifier.fillMaxWidth(0.8f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${uiState.correctCount}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MucGiayColors.Jade
                    )
                    Text("Chính xác", fontSize = 12.sp, color = MucGiayColors.InkSoft)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${uiState.wrongCount}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MucGiayColors.SealSon
                    )
                    Text("Làm lại", fontSize = 12.sp, color = MucGiayColors.InkSoft)
                }
            }

            Spacer(Modifier.height(32.dp))
            Button(
                onClick = { viewModel.restartPractice() },
                colors = ButtonDefaults.buttonColors(containerColor = MucGiayColors.Jade),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Luyện tập lại", fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    val currentEx = uiState.exercises[uiState.currentExerciseIndex]
    val total = uiState.exercises.size
    val progress = (uiState.currentExerciseIndex + 1).toFloat() / total

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Progress Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Câu ${uiState.currentExerciseIndex + 1} / $total",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MucGiayColors.InkSoft
            )
            Text(
                if (currentEx.type == "order") "Sắp xếp câu" else "Trắc nghiệm",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MucGiayColors.Amber
            )
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            color = MucGiayColors.Jade,
            trackColor = MucGiayColors.Hairline
        )

        Spacer(Modifier.height(20.dp))

        if (currentEx.type == "order") {
            // DẠNG SẮP XẾP TỪ THÀNH CÂU
            Text(
                "Chạm vào các từ để ghép thành câu hoàn chỉnh:",
                fontSize = 13.5.sp,
                color = MucGiayColors.InkSoft
            )
            Spacer(Modifier.height(12.dp))

            // Khu vực câu đang ghép
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 90.dp),
                shape = RoundedCornerShape(10.dp),
                color = MucGiayColors.PaperDeep,
                border = BorderStroke(1.2.dp, if (uiState.isAnswered) (if (uiState.isCorrect) MucGiayColors.Jade else MucGiayColors.SealSon) else MucGiayColors.Hairline)
            ) {
                if (uiState.selectedWords.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "Chọn từ bên dưới để điền vào đây...",
                            color = MucGiayColors.InkFaint,
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic
                        )
                    }
                } else {
                    FlowRow(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        uiState.selectedWords.forEachIndexed { index, word ->
                            Surface(
                                modifier = Modifier.clickable {
                                    if (!uiState.isAnswered) viewModel.unpickWord(index)
                                },
                                shape = RoundedCornerShape(6.dp),
                                color = MucGiayColors.JadeTint,
                                border = BorderStroke(1.dp, MucGiayColors.Jade)
                            ) {
                                Text(
                                    word,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MucGiayColors.Jade,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Ngân hàng các từ có sẵn để chọn
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.availableWords.forEach { (wordId, word) ->
                    Surface(
                        modifier = Modifier.clickable {
                            if (!uiState.isAnswered) viewModel.pickWord(wordId, word)
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = MucGiayColors.Paper,
                        border = BorderStroke(1.dp, MucGiayColors.Hairline)
                    ) {
                        Text(
                            word,
                            fontFamily = FontFamily.Serif,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MucGiayColors.Ink,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            if (!uiState.isAnswered) {
                Button(
                    onClick = { viewModel.checkOrderAnswer() },
                    enabled = uiState.selectedWords.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MucGiayColors.SealSon)
                ) {
                    Text("Kiểm tra câu trả lời", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

        } else {
            // DẠNG TRẮC NGHIỆM 4 ĐÁP ÁN
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MucGiayColors.PaperDeep)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        currentEx.question,
                        fontFamily = FontFamily.Serif,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MucGiayColors.Ink
                    )
                    if (currentEx.pinyin.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            currentEx.pinyin,
                            fontStyle = FontStyle.Italic,
                            fontSize = 13.5.sp,
                            color = MucGiayColors.Amber
                        )
                    }
                    if (currentEx.meaning.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            currentEx.meaning,
                            fontSize = 13.5.sp,
                            color = MucGiayColors.InkSoft
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // 4 Lựa chọn
            currentEx.options.forEach { option ->
                val isSelected = uiState.selectedChoiceOption == option
                val isCorrectAnswer = option.trim() == currentEx.answer.trim()

                val (bgColor, borderColor, textColor) = when {
                    !uiState.isAnswered -> if (isSelected) Triple(MucGiayColors.PaperDeep, MucGiayColors.Ink, MucGiayColors.Ink) else Triple(MucGiayColors.Paper, MucGiayColors.Hairline, MucGiayColors.Ink)
                    isCorrectAnswer -> Triple(MucGiayColors.JadeTint, MucGiayColors.Jade, MucGiayColors.Jade)
                    isSelected && !uiState.isCorrect -> Triple(MucGiayColors.RedBg, MucGiayColors.SealSon, MucGiayColors.SealSon)
                    else -> Triple(MucGiayColors.Paper, MucGiayColors.Hairline, MucGiayColors.InkFaint)
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clickable(enabled = !uiState.isAnswered) {
                            viewModel.selectChoice(option)
                        },
                    shape = RoundedCornerShape(10.dp),
                    color = bgColor,
                    border = BorderStroke(1.2.dp, borderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            option,
                            fontFamily = FontFamily.Serif,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            modifier = Modifier.weight(1f)
                        )
                        if (uiState.isAnswered && isCorrectAnswer) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MucGiayColors.Jade)
                        } else if (uiState.isAnswered && isSelected && !uiState.isCorrect) {
                            Icon(Icons.Default.Clear, contentDescription = null, tint = MucGiayColors.SealSon)
                        }
                    }
                }
            }
        }

        // BẢNG GIẢI THÍCH KHI ĐÃ TRẢ LỜI
        AnimatedVisibility(visible = uiState.isAnswered) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = if (uiState.isCorrect) MucGiayColors.JadeTint else MucGiayColors.RedBg,
                    border = BorderStroke(1.dp, if (uiState.isCorrect) MucGiayColors.Jade else MucGiayColors.SealSon)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            if (uiState.isCorrect) "✓ Chính xác! Bạn làm rất tốt." else "✕ Chưa chính xác!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (uiState.isCorrect) MucGiayColors.Jade else MucGiayColors.SealSon
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Đáp án đúng: ${currentEx.answer}",
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MucGiayColors.Ink
                        )
                        if (currentEx.explanation.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                currentEx.explanation,
                                fontSize = 13.sp,
                                color = MucGiayColors.InkSoft
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.nextExercise() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MucGiayColors.Jade)
                ) {
                    Text(
                        if (uiState.currentExerciseIndex + 1 >= total) "Xem kết quả" else "Câu tiếp theo ➔",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
