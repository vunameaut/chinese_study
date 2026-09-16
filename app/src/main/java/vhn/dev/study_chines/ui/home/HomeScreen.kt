package vhn.dev.study_chines.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import vhn.dev.study_chines.data.model.ClassifierLessonItem
import vhn.dev.study_chines.data.remote.SessionDto
import vhn.dev.study_chines.ui.theme.MucGiayColors
import vhn.dev.study_chines.update.AppUpdateManager
import vhn.dev.study_chines.update.AppUpdateState
import vhn.dev.study_chines.update.UpdateCheckResult
import vhn.dev.study_chines.update.UpdateDialog
import vhn.dev.study_chines.update.UpdateInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToQuiz: (sessionId: Long) -> Unit,
    onNavigateToWritePinyin: (sessionId: Long) -> Unit,
    onNavigateToGrammar: (sessionId: Long) -> Unit,
    onNavigateToClassifier: (hskLevel: Int, lessonNum: Int) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val updateManager = remember { AppUpdateManager(context) }
    var availableUpdate by remember { mutableStateOf<UpdateInfo?>(null) }
    var updateState by remember { mutableStateOf<AppUpdateState>(AppUpdateState.Idle) }
    var hasInstallPermission by remember { mutableStateOf(updateManager.canRequestPackageInstalls()) }

    // Tự động kiểm tra bản cập nhật mới khi mở HomeScreen
    LaunchedEffect(Unit) {
        val result = updateManager.checkForUpdate()
        if (result is UpdateCheckResult.UpdateAvailable) {
            availableUpdate = result.info
            updateState = AppUpdateState.Available(result.info)
        }
    }

    val pullToRefreshState = rememberPullToRefreshState()
    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.refresh()
        }
    }
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            pullToRefreshState.endRefresh()
        }
    }

    // Filter sessions by selected HSK level
    val filteredSessions = uiState.sessions.filter { it.hskLevel == uiState.selectedHsk }

    Scaffold(
        containerColor = MucGiayColors.Paper,
        contentColor = MucGiayColors.Ink,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = { },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Cài đặt", tint = MucGiayColors.InkSoft)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            Column(
                Modifier
                    .padding(padding)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize()
            ) {
                // Header
                Text("Ứng dụng", fontFamily = FontFamily.Serif, color = MucGiayColors.InkSoft, fontSize = 16.sp)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("Hanzi Quiz", style = MaterialTheme.typography.displayLarge, color = MucGiayColors.Ink)
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .height(2.5.dp)
                            .width(60.dp)
                            .background(MucGiayColors.SealSon)
                    )
                }

                // Date
                val days = listOf("Chủ Nhật","Thứ Hai","Thứ Ba","Thứ Tư","Thứ Năm","Thứ Sáu","Thứ Bảy")
                val now = java.util.Calendar.getInstance()
                Text(
                    "${days[now.get(java.util.Calendar.DAY_OF_WEEK)-1]}, ${now.get(java.util.Calendar.DAY_OF_MONTH)} tháng ${now.get(java.util.Calendar.MONTH)+1}",
                    color = MucGiayColors.InkFaint, fontSize = 13.sp
                )

                Spacer(Modifier.height(20.dp))

                // HSK Level Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    viewModel.hskLevels.forEach { level ->
                        val isSelected = uiState.selectedHsk == level
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) MucGiayColors.SealSon else MucGiayColors.PaperDeep,
                            contentColor = if (isSelected) Color.White else MucGiayColors.InkSoft,
                            modifier = Modifier.clickable { viewModel.selectHsk(level) }
                        ) {
                            Text(
                                "HSK$level",
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Thanh chuyển chế độ học (Study Mode Switcher)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MucGiayColors.PaperDeep)
                        .border(1.dp, MucGiayColors.Hairline, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isSessions = uiState.studyMode == 0
                    Surface(
                        onClick = { viewModel.switchStudyMode(0) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(9.dp),
                        color = if (isSessions) MucGiayColors.Paper else Color.Transparent,
                        border = if (isSessions) BorderStroke(1.dp, MucGiayColors.Hairline) else null,
                        shadowElevation = if (isSessions) 1.dp else 0.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📚", fontSize = 14.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Buổi học (${filteredSessions.size})",
                                fontSize = 13.sp,
                                fontWeight = if (isSessions) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSessions) MucGiayColors.Ink else MucGiayColors.InkSoft
                            )
                        }
                    }

                    val isClassifiers = uiState.studyMode == 1
                    Surface(
                        onClick = { viewModel.switchStudyMode(1) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(9.dp),
                        color = if (isClassifiers) MucGiayColors.IndigoTint else Color.Transparent,
                        border = if (isClassifiers) BorderStroke(1.2.dp, MucGiayColors.Indigo.copy(alpha = 0.6f)) else null,
                        shadowElevation = if (isClassifiers) 1.dp else 0.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🏷️", fontSize = 14.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Lượng từ (${uiState.classifierLessons.size})",
                                fontSize = 13.sp,
                                fontWeight = if (isClassifiers) FontWeight.Bold else FontWeight.Medium,
                                color = if (isClassifiers) MucGiayColors.Indigo else MucGiayColors.InkSoft
                            )
                        }
                    }
                }

                if (uiState.studyMode == 1) {
                    // Chế độ: Lượng từ theo HSK & Bài
                    if (uiState.isLoadingClassifiers && !pullToRefreshState.isRefreshing) {
                        Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MucGiayColors.Indigo)
                        }
                    } else if (uiState.classifierLessons.isEmpty()) {
                        EmptyClassifierState(uiState.selectedHsk)
                    } else {
                        ClassifierLessonList(
                            lessons = uiState.classifierLessons,
                            hskLevel = uiState.selectedHsk,
                            onNavigateToClassifier = onNavigateToClassifier
                        )
                    }
                } else if (uiState.isLoading && !pullToRefreshState.isRefreshing) {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MucGiayColors.JadeFill)
                    }
                } else {
                    // Last session shortcut
                    val lastSession = uiState.sessions.find { it.id.toLong() == uiState.lastSessionId }
                    if (lastSession != null) {
                        Column {
                            Text(
                                "TIẾP TỤC ÔN TẬP",
                                style = MaterialTheme.typography.labelSmall,
                                letterSpacing = spToEm(0.06f)
                            )
                            Spacer(Modifier.height(8.dp))
                            SessionCard(
                                session = lastSession,
                                ordinal = uiState.sessions.indexOf(lastSession) + 1,
                                onSelectSession = { onNavigateToQuiz(lastSession.id.toLong()) },
                                onWritePinyin = { onNavigateToWritePinyin(lastSession.id.toLong()) },
                                onGrammar = { onNavigateToGrammar(lastSession.id.toLong()) }
                            )
                            Spacer(Modifier.height(20.dp))
                        }
                    }

                    if (filteredSessions.isEmpty()) {
                        EmptyHomeState()
                    } else {
                        SessionList(
                            sessions = filteredSessions,
                            onSelectSession = onNavigateToQuiz,
                            onWritePinyin = onNavigateToWritePinyin,
                            onGrammar = onNavigateToGrammar
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            PullToRefreshContainer(
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = MucGiayColors.Paper,
                contentColor = MucGiayColors.SealSon
            )
        }
    }

    if (availableUpdate != null) {
        UpdateDialog(
            updateInfo = availableUpdate!!,
            currentVersion = updateManager.currentVersionName,
            state = updateState,
            onDismiss = {
                availableUpdate = null
                updateState = AppUpdateState.Idle
            },
            onStartDownload = {
                val info = availableUpdate ?: return@UpdateDialog
                coroutineScope.launch {
                    updateState = AppUpdateState.Downloading(0, 0, info.fileSize)
                    val downloadResult = updateManager.downloadApk(
                        downloadUrl = info.downloadUrl,
                        targetVersion = info.versionName,
                        onProgress = { percent, downloaded, total ->
                            updateState = AppUpdateState.Downloading(percent, downloaded, total)
                        }
                    )
                    downloadResult.onSuccess { file ->
                        updateState = AppUpdateState.ReadyToInstall(info, file)
                        hasInstallPermission = updateManager.canRequestPackageInstalls()
                        if (hasInstallPermission) {
                            updateManager.installApk(file)
                        }
                    }.onFailure { error ->
                        updateState = AppUpdateState.Error(error.localizedMessage ?: "Tải bản cập nhật thất bại")
                    }
                }
            },
            onInstall = { file ->
                hasInstallPermission = updateManager.canRequestPackageInstalls()
                if (hasInstallPermission) {
                    updateManager.installApk(file)
                } else {
                    updateManager.openInstallPermissionSettings()
                }
            },
            onRequestPermission = {
                updateManager.openInstallPermissionSettings()
            },
            hasInstallPermission = hasInstallPermission
        )
    }
}

@Composable
private fun EmptyHomeState() {
    Column(
        Modifier.fillMaxWidth().padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Chưa có buổi ôn tập nào",
            style = MaterialTheme.typography.headlineMedium,
            color = MucGiayColors.InkSoft
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Hiện chưa có bài học nào trong cấp độ HSK này",
            color = MucGiayColors.InkFaint, fontSize = 14.sp
        )
    }
}

@Composable
private fun SessionList(
    sessions: List<SessionDto>,
    onSelectSession: (Long) -> Unit,
    onWritePinyin: (Long) -> Unit,
    onGrammar: (Long) -> Unit
) {
    Text(
        "CÁC BUỔI ÔN TẬP",
        style = MaterialTheme.typography.labelSmall,
        letterSpacing = spToEm(0.06f)
    )
    Spacer(Modifier.height(12.dp))

    sessions.forEachIndexed { index, session ->
        SessionCard(
            session = session,
            ordinal = index + 1,
            onSelectSession = { onSelectSession(session.id.toLong()) },
            onWritePinyin = { onWritePinyin(session.id.toLong()) },
            onGrammar = { onGrammar(session.id.toLong()) }
        )
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun SessionCard(
    session: SessionDto,
    ordinal: Int,
    onSelectSession: () -> Unit,
    onWritePinyin: () -> Unit,
    onGrammar: () -> Unit
) {
    val ordinals = listOf('壹','贰','叁','肆','伍','陆','柒','捌','玖','拾')
    val date = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(
        java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(session.createdAt.substringBefore('.'))
            ?: java.util.Date()
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MucGiayColors.Hairline, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MucGiayColors.PaperDeep),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header Info Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MucGiayColors.Paper)
                        .border(1.dp, MucGiayColors.Hairline.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        ordinals[ordinal.coerceAtMost(9)].toString(),
                        fontFamily = FontFamily.Serif,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MucGiayColors.InkSoft
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        session.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MucGiayColors.Ink
                    )
                    Spacer(Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(date, fontSize = 12.sp, color = MucGiayColors.InkFaint)
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MucGiayColors.JadeTint
                        ) {
                            Text(
                                "HSK ${session.hskLevel}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MucGiayColors.Jade,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = MucGiayColors.Hairline.copy(alpha = 0.7f), thickness = 0.8.dp)
            Spacer(Modifier.height(12.dp))

            // Action Buttons: 3 nút đồng bộ với bản Web (Ngữ pháp, Viết Pinyin, Ôn tập ngay)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nút Ngữ pháp (Màu Ngọc Bích Jade)
                Surface(
                    onClick = onGrammar,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MucGiayColors.JadeTint,
                    border = BorderStroke(1.2.dp, MucGiayColors.Jade.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "📖 Ngữ pháp",
                            fontWeight = FontWeight.SemiBold,
                            color = MucGiayColors.Jade,
                            fontSize = 12.5.sp
                        )
                    }
                }

                // Nút Viết Pinyin (Màu Hổ Phách Amber)
                Surface(
                    onClick = onWritePinyin,
                    modifier = Modifier
                        .weight(0.95f)
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MucGiayColors.AmberTint,
                    border = BorderStroke(1.2.dp, MucGiayColors.Amber.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "✍ Pinyin",
                            fontWeight = FontWeight.SemiBold,
                            color = MucGiayColors.Amber,
                            fontSize = 12.5.sp
                        )
                    }
                }

                // Nút Ôn tập ngay (Màu Đỏ Son SealSon)
                Surface(
                    onClick = onSelectSession,
                    modifier = Modifier
                        .weight(1.1f)
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MucGiayColors.SealSon
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Ôn tập ngay",
                            fontWeight = FontWeight.SemiBold,
                            color = androidx.compose.ui.graphics.Color.White,
                            fontSize = 12.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun spToEm(value: Float): androidx.compose.ui.unit.TextUnit = value.sp

@Composable
private fun EmptyClassifierState(hskLevel: Int) {
    Column(
        Modifier.fillMaxWidth().padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🏷️", fontSize = 36.sp)
        Spacer(Modifier.height(10.dp))
        Text(
            "Chưa có lượng từ cho HSK $hskLevel",
            style = MaterialTheme.typography.headlineMedium,
            color = MucGiayColors.InkSoft
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Hiện hệ thống đang cập nhật lượng từ cho cấp độ này",
            color = MucGiayColors.InkFaint, fontSize = 14.sp
        )
    }
}

@Composable
private fun ClassifierLessonList(
    lessons: List<ClassifierLessonItem>,
    hskLevel: Int,
    onNavigateToClassifier: (hskLevel: Int, lessonNum: Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "LƯỢNG TỪ THEO BÀI HỌC (HSK $hskLevel)",
            style = MaterialTheme.typography.labelSmall,
            color = MucGiayColors.Indigo,
            fontWeight = FontWeight.Bold,
            letterSpacing = spToEm(0.06f)
        )
        Text(
            "${lessons.size} bài học",
            fontSize = 12.sp,
            color = MucGiayColors.InkFaint
        )
    }
    Spacer(Modifier.height(12.dp))

    lessons.forEach { lesson ->
        ClassifierLessonCard(
            lesson = lesson,
            onStartStudy = { onNavigateToClassifier(lesson.hskLevel, lesson.lessonNum) }
        )
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun ClassifierLessonCard(
    lesson: ClassifierLessonItem,
    onStartStudy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.2.dp, MucGiayColors.Indigo.copy(alpha = 0.25f), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MucGiayColors.PaperDeep),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header: Lesson Num & Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MucGiayColors.IndigoTint,
                        border = BorderStroke(1.dp, MucGiayColors.Indigo.copy(alpha = 0.4f))
                    ) {
                        Text(
                            "Bài ${lesson.lessonNum}",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MucGiayColors.Indigo,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        lesson.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MucGiayColors.Ink
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Badges of Classifiers in this lesson
            if (lesson.classifiers.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    lesson.classifiers.forEach { cl ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MucGiayColors.Paper,
                            border = BorderStroke(0.8.dp, MucGiayColors.Hairline)
                        ) {
                            Text(
                                "[ $cl ]",
                                fontFamily = FontFamily.Serif,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MucGiayColors.Indigo,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            // Preview Collocations
            if (lesson.previewCollocations.isNotEmpty()) {
                Text(
                    "Cụm từ mẫu: ${lesson.previewCollocations.joinToString(" • ")}",
                    fontSize = 12.sp,
                    color = MucGiayColors.InkSoft,
                    maxLines = 1
                )
                Spacer(Modifier.height(12.dp))
            }

            // Direct Action Button
            Surface(
                onClick = onStartStudy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                shape = RoundedCornerShape(10.dp),
                color = MucGiayColors.Indigo
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Học lượng từ bài này ➔",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
