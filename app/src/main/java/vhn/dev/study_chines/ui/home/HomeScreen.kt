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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import vhn.dev.study_chines.data.remote.SessionDto
import vhn.dev.study_chines.ui.theme.MucGiayColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToEntry: (sessionId: Long) -> Unit,
    onNavigateToQuiz: (sessionId: Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showNewSession by remember { mutableStateOf(false) }
    var sessionTitle by remember { mutableStateOf("") }
    var newHskLevel by remember { mutableIntStateOf(1) }

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
                    IconButton(onClick = { 
                        newHskLevel = uiState.selectedHsk
                        showNewSession = true 
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Tạo buổi học", tint = MucGiayColors.SealSon)
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

                if (uiState.isLoading && !pullToRefreshState.isRefreshing) {
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
                                onDelete = { viewModel.deleteSession(lastSession.id) },
                                onClick = { onNavigateToQuiz(lastSession.id.toLong()) }
                            )
                            Spacer(Modifier.height(20.dp))
                        }
                    }

                    if (filteredSessions.isEmpty()) {
                        EmptyHomeState(onClick = { newHskLevel = uiState.selectedHsk; showNewSession = true })
                    } else {
                        SessionList(
                            sessions = filteredSessions,
                            onDelete = { viewModel.deleteSession(it) },
                            onSelectSession = onNavigateToQuiz,
                            onAddVocab = onNavigateToEntry
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

    // New Session Dialog
    if (showNewSession) {
        AlertDialog(
            onDismissRequest = { showNewSession = false },
            containerColor = MucGiayColors.Paper,
            shape = RoundedCornerShape(12.dp),
            title = { Text("Tạo buổi ôn tập mới", style = MaterialTheme.typography.headlineMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // HSK level selector in dialog
                    Text("Chọn trình độ HSK", style = MaterialTheme.typography.labelMedium, color = MucGiayColors.InkSoft)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        viewModel.hskLevels.forEach { level ->
                            val isSelected = newHskLevel == level
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) MucGiayColors.Jade else Color.Transparent,
                                contentColor = if (isSelected) Color.White else MucGiayColors.InkSoft,
                                border = if (!isSelected) BorderStroke(1.dp, MucGiayColors.Hairline) else null,
                                modifier = Modifier.clickable { newHskLevel = level }
                            ) {
                                Text(
                                    "$level",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = sessionTitle,
                        onValueChange = { sessionTitle = it },
                        label = { Text("Tên buổi học") },
                        placeholder = { Text("VD: Buổi 1 - Lớp 10A1") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MucGiayColors.Jade,
                            cursorColor = MucGiayColors.Ink
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val title = sessionTitle.ifBlank { "Buổi ${filteredSessions.size + 1}" }
                        viewModel.createSession(title, newHskLevel) { id ->
                            showNewSession = false
                            sessionTitle = ""
                            onNavigateToEntry(id)
                        }
                    },
                    enabled = true
                ) { Text("Tạo", color = MucGiayColors.SealSon, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showNewSession = false }) { Text("Hủy", color = MucGiayColors.InkSoft) }
            }
        )
    }
}

@Composable
private fun EmptyHomeState(onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Add, contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MucGiayColors.Jade.copy(alpha = 0.5f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Chưa có buổi ôn tập nào",
            style = MaterialTheme.typography.headlineMedium,
            color = MucGiayColors.InkSoft
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Tạo buổi mới để bắt đầu nhập từ vựng và ôn tập",
            color = MucGiayColors.InkFaint, fontSize = 14.sp
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = MucGiayColors.SealSon),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.padding(horizontal = 40.dp).height(52.dp)
        ) { Text("Tạo buổi học đầu tiên", fontWeight = FontWeight.SemiBold) }
    }
}

@Composable
private fun SessionList(
    sessions: List<SessionDto>,
    onDelete: (Int) -> Unit,
    onSelectSession: (Long) -> Unit,
    onAddVocab: (Long) -> Unit
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
            onDelete = { onDelete(session.id) }
        ) {
            onSelectSession(session.id.toLong())
        }
        Spacer(Modifier.height(4.dp))

        Row(Modifier.padding(start = 28.dp)) {
            TextButton(onClick = { onAddVocab(session.id.toLong()) }) {
                Text("+ Thêm từ", color = MucGiayColors.Jade, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
            Spacer(Modifier.weight(1f))
            FilledTonalButton(
                onClick = { onSelectSession(session.id.toLong()) },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MucGiayColors.SealSon,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
            ) { Text("Ôn tập ngay", fontWeight = FontWeight.SemiBold) }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SessionCard(
    session: SessionDto,
    ordinal: Int,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val ordinals = listOf('壹','贰','叁','肆','伍','陆','柒','捌','玖','拾')
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.5.dp, MucGiayColors.Hairline, RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MucGiayColors.PaperDeep),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                ordinals[ordinal.coerceAtMost(9)].toString(),
                fontFamily = FontFamily.Serif,
                fontSize = 15.sp,
                color = MucGiayColors.InkFaint
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(session.title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                val date = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(
                    java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(session.createdAt.substringBefore('.'))
                        ?: java.util.Date()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(date, fontSize = 12.sp, color = MucGiayColors.InkFaint)
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MucGiayColors.JadeTint
                    ) {
                        Text(
                            "HSK${session.hskLevel}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MucGiayColors.Jade,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            IconButton(onClick = onDelete, Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Delete, contentDescription = "Xóa",
                    tint = MucGiayColors.InkFaint,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun spToEm(value: Float): androidx.compose.ui.unit.TextUnit = value.sp