package vhn.dev.study_chines.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import vhn.dev.study_chines.data.local.SessionEntity
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

    Scaffold(
        containerColor = MucGiayColors.Paper,
        contentColor = MucGiayColors.Ink,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = { },
                actions = {
                    IconButton(onClick = { showNewSession = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Tạo buổi học", tint = MucGiayColors.SealSon)
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(horizontal = 20.dp)) {
            // Header
            Text("Ứng dụng", fontFamily = FontFamily.Serif, color = MucGiayColors.InkSoft, fontSize = 16.sp)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("Hanzi Quiz", style = MaterialTheme.typography.displayLarge, color = MucGiayColors.Ink)
                Spacer(Modifier.width(8.dp))
                Box(modifier = Modifier
                    .height(2.5.dp)
                    .width(60.dp)
                    .background(MucGiayColors.SealSon)
                )
            }

            // Date
            val days = listOf("Chủ Nhật","Thứ Hai","Thứ Ba","Thứ Tư","Thứ Năm","Thứ Sáu","Thứ Bảy")
            val now = java.util.Calendar.getInstance()
            Text("${days[now.get(java.util.Calendar.DAY_OF_WEEK)-1]}, ${now.get(java.util.Calendar.DAY_OF_MONTH)} tháng ${now.get(java.util.Calendar.MONTH)+1}",
                color = MucGiayColors.InkFaint, fontSize = 13.sp)

            Spacer(Modifier.height(24.dp))

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MucGiayColors.JadeFill) }
            } else if (uiState.sessions.isEmpty()) {
                EmptyHomeState(onClick = { showNewSession = true })
            } else {
                SessionList(sessions = uiState.sessions, onDelete = { viewModel.deleteSession(it) }, onSelectSession = onNavigateToQuiz, onAddVocab = onNavigateToEntry)
            }
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
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val title = sessionTitle.ifBlank { "Buổi ${uiState.sessions.size + 1}" }
                        viewModel.createSession(title) { id -> showNewSession = false; onNavigateToEntry(id) }
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
    Column(Modifier.fillMaxSize().padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(48.dp), tint = MucGiayColors.Jade.copy(alpha = 0.5f))
        Spacer(Modifier.height(16.dp))
        Text("Chưa có buổi ôn tập nào", style = MaterialTheme.typography.headlineMedium, color = MucGiayColors.InkSoft)
        Spacer(Modifier.height(8.dp))
        Text("Tạo buổi mới để bắt đầu nhập từ vựng và ôn tập", color = MucGiayColors.InkFaint, fontSize = 14.sp)
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
    sessions: List<SessionEntity>,
    onDelete: (Int) -> Unit,
    onSelectSession: (Long) -> Unit,
    onAddVocab: (Long) -> Unit
) {
    Text("CÁC BUỔI ÔN TẬP", style = MaterialTheme.typography.labelSmall, letterSpacing = spToEm(0.06f))
    Spacer(Modifier.height(12.dp))

    sessions.forEachIndexed { index, session ->
        SessionCard(session = session, ordinal = index+1, onDelete = { onDelete(session.id) }) {
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
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = MucGiayColors.SealSon, contentColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
            ) { Text("Ôn tập ngay", fontWeight = FontWeight.SemiBold) }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SessionCard(session: SessionEntity, ordinal: Int, onDelete: () -> Unit, onClick: () -> Unit) {
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
            Text(ordinals[ordinal.coerceAtMost(9)].toString(), fontFamily = FontFamily.Serif, fontSize = 15.sp, color = MucGiayColors.InkFaint)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(session.title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                val date = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(java.util.Date(session.createdAt))
                Text(date, fontSize = 12.sp, color = MucGiayColors.InkFaint)
            }
            IconButton(onClick = onDelete, Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = MucGiayColors.InkFaint, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// Helper to convert sp to em for letter-spacing
private fun spToEm(value: Float): androidx.compose.ui.unit.TextUnit = value.em