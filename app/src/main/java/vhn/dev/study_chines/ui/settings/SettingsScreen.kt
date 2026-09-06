package vhn.dev.study_chines.ui.settings

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import vhn.dev.study_chines.R
import vhn.dev.study_chines.audio.ChineseSpeechManager
import vhn.dev.study_chines.audio.VoiceOption
import vhn.dev.study_chines.data.local.UserPreferences
import vhn.dev.study_chines.ui.quiz.SoundManager
import vhn.dev.study_chines.ui.theme.MucGiayColors
import java.io.File
import java.io.FileOutputStream

class SettingsViewModel(val preferences: UserPreferences) : ViewModel() {
    var masterEnabled by mutableStateOf(preferences.masterEnabled)
        private set
    var masterVolume by mutableIntStateOf(preferences.masterVolume)
        private set

    var speechEnabled by mutableStateOf(preferences.speechEnabled)
        private set
    var speechVolume by mutableIntStateOf(preferences.speechVolume)
        private set
    var speechAutoPlay by mutableStateOf(preferences.speechAutoPlay)
        private set
    var speechRate by mutableFloatStateOf(preferences.speechRate)
        private set
    var speechVoice by mutableStateOf(preferences.speechVoice)
        private set

    var sfxEnabled by mutableStateOf(preferences.sfxEnabled)
        private set
    var sfxVolume by mutableIntStateOf(preferences.sfxVolume)
        private set

    var finishEnabled by mutableStateOf(preferences.finishEnabled)
        private set
    var finishVolume by mutableIntStateOf(preferences.finishVolume)
        private set

    var customCorrectSoundPath by mutableStateOf(preferences.customCorrectSoundPath)
        private set
    var customWrongSoundPath by mutableStateOf(preferences.customWrongSoundPath)
        private set
    var customFinishSoundPath by mutableStateOf(preferences.customFinishSoundPath)
        private set

    fun setMaster(enabled: Boolean? = null, volume: Int? = null) {
        enabled?.let { preferences.masterEnabled = it; masterEnabled = it }
        volume?.let { preferences.masterVolume = it; masterVolume = it }
    }

    fun setSpeech(
        enabled: Boolean? = null,
        volume: Int? = null,
        autoPlay: Boolean? = null,
        rate: Float? = null,
        voice: String? = null
    ) {
        enabled?.let { preferences.speechEnabled = it; speechEnabled = it }
        volume?.let { preferences.speechVolume = it; speechVolume = it }
        autoPlay?.let { preferences.speechAutoPlay = it; speechAutoPlay = it }
        rate?.let { preferences.speechRate = it; speechRate = it }
        voice?.let { preferences.speechVoice = it; speechVoice = it }
    }

    fun setSfx(enabled: Boolean? = null, volume: Int? = null) {
        enabled?.let { preferences.sfxEnabled = it; sfxEnabled = it }
        volume?.let { preferences.sfxVolume = it; sfxVolume = it }
    }

    fun setFinish(enabled: Boolean? = null, volume: Int? = null) {
        enabled?.let { preferences.finishEnabled = it; finishEnabled = it }
        volume?.let { preferences.finishVolume = it; finishVolume = it }
    }

    fun resetDefaults() {
        preferences.resetAudioDefaults()
        masterEnabled = preferences.masterEnabled
        masterVolume = preferences.masterVolume
        speechEnabled = preferences.speechEnabled
        speechVolume = preferences.speechVolume
        speechAutoPlay = preferences.speechAutoPlay
        speechRate = preferences.speechRate
        speechVoice = preferences.speechVoice
        sfxEnabled = preferences.sfxEnabled
        sfxVolume = preferences.sfxVolume
        finishEnabled = preferences.finishEnabled
        finishVolume = preferences.finishVolume
    }

    fun setCustomSound(context: Context, type: String, uri: Uri) {
        val extension = context.contentResolver.getType(uri)?.split("/")?.lastOrNull() ?: "mp3"
        val fileName = "custom_${type}_${System.currentTimeMillis()}.${extension}"
        val file = File(context.filesDir, fileName)

        try {
            context.filesDir.listFiles { _, name -> name.startsWith("custom_$type") }?.forEach { it.delete() }
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output -> input.copyTo(output) }
            }
            val path = file.absolutePath
            when (type) {
                "correct" -> { preferences.customCorrectSoundPath = path; customCorrectSoundPath = path }
                "wrong" -> { preferences.customWrongSoundPath = path; customWrongSoundPath = path }
                "finish" -> { preferences.customFinishSoundPath = path; customFinishSoundPath = path }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resetSound(type: String) {
        when (type) {
            "correct" -> { preferences.customCorrectSoundPath = null; customCorrectSoundPath = null }
            "wrong" -> { preferences.customWrongSoundPath = null; customWrongSoundPath = null }
            "finish" -> { preferences.customFinishSoundPath = null; customFinishSoundPath = null }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val soundManager = remember {
        SoundManager(context, viewModel.preferences).apply {
            loadDefault(R.raw.correct, R.raw.wrong, R.raw.finish)
        }
    }
    val speechManager = remember { ChineseSpeechManager(context, viewModel.preferences) }

    DisposableEffect(Unit) {
        onDispose {
            soundManager.release()
            speechManager.release()
        }
    }

    Scaffold(
        containerColor = MucGiayColors.Paper,
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt âm thanh", color = MucGiayColors.Ink, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Trở về", tint = MucGiayColors.Ink)
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.resetDefaults() }) {
                        Text("Mặc định", color = MucGiayColors.SealSon, fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MucGiayColors.Paper)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ===== 1. TỔNG ÂM LƯỢNG (MASTER) =====
            ChannelCard(
                title = "Tổng âm lượng",
                subtitle = "Điều khiển âm thanh toàn bộ ứng dụng",
                icon = "🔊",
                enabled = viewModel.masterEnabled,
                onToggle = { viewModel.setMaster(enabled = it) },
                volume = viewModel.masterVolume,
                onVolumeChange = { viewModel.setMaster(volume = it) }
            )

            Spacer(Modifier.height(16.dp))

            // ===== 2. GIỌNG ĐỌC TIẾNG TRUNG (SPEECH) =====
            ChannelCard(
                title = "Giọng đọc từ vựng (TTS)",
                subtitle = "Phát âm tiếng Trung chuẩn xác",
                icon = "🗣️",
                enabled = viewModel.masterEnabled && viewModel.speechEnabled,
                onToggle = { viewModel.setSpeech(enabled = it) },
                volume = viewModel.speechVolume,
                onVolumeChange = { viewModel.setSpeech(volume = it) },
                testButton = {
                    TextButton(onClick = { speechManager.testVoice(viewModel.speechVoice) }) {
                        Text("▶ Nghe thử", color = MucGiayColors.SealSon, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            ) {
                // Tự động phát âm
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tự động đọc sau khi chọn Pinyin", fontSize = 12.sp, color = MucGiayColors.InkSoft)
                    Switch(
                        checked = viewModel.speechAutoPlay,
                        onCheckedChange = { viewModel.setSpeech(autoPlay = it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = MucGiayColors.Jade, checkedTrackColor = MucGiayColors.JadeTint),
                        modifier = Modifier.scale(0.8f)
                    )
                }

                // Tốc độ đọc (Speed Chips)
                Text("Tốc độ giọng đọc:", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = MucGiayColors.InkFaint, modifier = Modifier.padding(top = 6.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val speeds = listOf(0.7f to "0.7x (Chậm)", 0.85f to "0.85x (Khuyên dùng)", 1.0f to "1.0x (Tự nhiên)")
                    speeds.forEach { (rate, label) ->
                        val isSelected = kotlin.math.abs(viewModel.speechRate - rate) < 0.05f
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MucGiayColors.SealSon else MucGiayColors.Paper,
                            border = BorderStroke(1.dp, if (isSelected) MucGiayColors.SealSon else MucGiayColors.Hairline),
                            modifier = Modifier.clickable { viewModel.setSpeech(rate = rate) }
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MucGiayColors.InkSoft,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Chọn giọng đọc (Voice Dropdown)
                Spacer(Modifier.height(10.dp))
                VoiceDropdownSelector(
                    speechManager = speechManager,
                    currentVoiceId = viewModel.speechVoice,
                    onVoiceSelected = {
                        viewModel.setSpeech(voice = it)
                        speechManager.testVoice(it)
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            // ===== 3. HIỆU ỨNG KIỂM TRA (SFX) =====
            ChannelCard(
                title = "Hiệu ứng kiểm tra",
                subtitle = "Âm thanh phản hồi khi trả lời Đúng / Sai",
                icon = "🔔",
                enabled = viewModel.masterEnabled && viewModel.sfxEnabled,
                onToggle = { viewModel.setSfx(enabled = it) },
                volume = viewModel.sfxVolume,
                onVolumeChange = { viewModel.setSfx(volume = it) },
                testButton = {
                    Row {
                        TextButton(onClick = { soundManager.play(R.raw.correct, forcePlay = true) }) {
                            Text("✓ Đúng", color = MucGiayColors.Jade, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = { soundManager.play(R.raw.wrong, forcePlay = true) }) {
                            Text("✗ Sai", color = MucGiayColors.SealSon, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )

            Spacer(Modifier.height(16.dp))

            // ===== 4. ÂM THANH KẾT THÚC (FINISH) =====
            ChannelCard(
                title = "Âm thanh hoàn thành",
                subtitle = "Nhạc chúc mừng khi hoàn thành bài học",
                icon = "🎉",
                enabled = viewModel.masterEnabled && viewModel.finishEnabled,
                onToggle = { viewModel.setFinish(enabled = it) },
                volume = viewModel.finishVolume,
                onVolumeChange = { viewModel.setFinish(volume = it) },
                testButton = {
                    TextButton(onClick = { soundManager.play(R.raw.finish, forcePlay = true) }) {
                        Text("▶ Thử", color = MucGiayColors.Amber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )

            Spacer(Modifier.height(24.dp))

            // ===== 5. TÙY CHỌN FILE ÂM THANH RIÊNG (CUSTOM FILE) =====
            Text("Tùy chỉnh file âm thanh riêng", style = MaterialTheme.typography.titleMedium, color = MucGiayColors.SealSon)
            Text("Chọn file mp3 riêng từ máy nếu không muốn dùng âm thanh mặc định", fontSize = 12.sp, color = MucGiayColors.InkSoft)
            Spacer(Modifier.height(8.dp))

            SoundPickerItem(
                label = "Khi trả lời đúng",
                path = viewModel.customCorrectSoundPath,
                onPick = { uri -> viewModel.setCustomSound(context, "correct", uri) },
                onReset = { viewModel.resetSound("correct") },
                onTest = { soundManager.play(R.raw.correct, forcePlay = true) }
            )

            Spacer(Modifier.height(10.dp))

            SoundPickerItem(
                label = "Khi trả lời sai",
                path = viewModel.customWrongSoundPath,
                onPick = { uri -> viewModel.setCustomSound(context, "wrong", uri) },
                onReset = { viewModel.resetSound("wrong") },
                onTest = { soundManager.play(R.raw.wrong, forcePlay = true) }
            )

            Spacer(Modifier.height(10.dp))

            SoundPickerItem(
                label = "Khi kết thúc bài học",
                path = viewModel.customFinishSoundPath,
                onPick = { uri -> viewModel.setCustomSound(context, "finish", uri) },
                onReset = { viewModel.resetSound("finish") },
                onTest = { soundManager.play(R.raw.finish, forcePlay = true) }
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ===== THẺ ĐIỀU KHIỂN KÊNH ÂM THANH =====
@Composable
fun ChannelCard(
    title: String,
    subtitle: String,
    icon: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    volume: Int,
    onVolumeChange: (Int) -> Unit,
    testButton: (@Composable () -> Unit)? = null,
    extraContent: (@Composable () -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MucGiayColors.PaperDeep,
        border = BorderStroke(1.dp, MucGiayColors.Hairline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(icon, fontSize = 20.sp, modifier = Modifier.padding(end = 10.dp))
                    Column {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = MucGiayColors.Ink)
                        Text(subtitle, fontSize = 11.5.sp, color = MucGiayColors.InkSoft)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    testButton?.invoke()
                    Switch(
                        checked = enabled,
                        onCheckedChange = onToggle,
                        colors = SwitchDefaults.colors(checkedThumbColor = MucGiayColors.Jade, checkedTrackColor = MucGiayColors.JadeTint)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Thanh Slider Discord 0 - 200%
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    Slider(
                        value = volume.toFloat(),
                        onValueChange = { onVolumeChange(it.toInt()) },
                        valueRange = 0f..200f,
                        enabled = enabled,
                        colors = SliderDefaults.colors(
                            thumbColor = if (volume > 100) MucGiayColors.SealSon else MucGiayColors.Ink,
                            activeTrackColor = if (volume > 100) MucGiayColors.SealSon else MucGiayColors.Ink,
                            inactiveTrackColor = MucGiayColors.Hairline
                        )
                    )
                    // Vạch đánh dấu 100% ở chính giữa
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .width(2.dp)
                            .height(14.dp)
                            .background(MucGiayColors.InkFaint.copy(alpha = 0.5f))
                    )
                }

                Spacer(Modifier.width(8.dp))

                Text(
                    text = if (volume > 100) "$volume% ⚡" else "$volume%",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (volume > 100) MucGiayColors.SealSon else MucGiayColors.InkSoft,
                    modifier = Modifier.width(54.dp)
                )
            }

            extraContent?.invoke()
        }
    }
}

// ===== BADGE NHÃN PHÂN LOẠI GIỌNG ĐỌC =====
@Composable
fun VoiceBadge(badge: String) {
    if (badge.isBlank()) return

    val (bgColor, textColor) = when {
        badge.contains("Online") -> Color(0xFFE0F2FE) to Color(0xFF0369A1) // Xanh dương nhạt
        badge.contains("Nam") -> Color(0xFFE0E7FF) to Color(0xFF3730A3) // Chàm Indigo
        badge.contains("Nữ") -> Color(0xFFFCE7F3) to Color(0xFF9D174D) // Hồng nhạt
        badge.contains("Học Tập") -> Color(0xFFDCFCE7) to Color(0xFF15803D) // Xanh ngọc
        else -> Color(0xFFF3F4F6) to Color(0xFF374151) // Xám
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = bgColor
    ) {
        Text(
            text = badge,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// ===== DROPDOWN CHỌN GIỌNG ĐỌC TIẾNG TRUNG =====
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceDropdownSelector(
    speechManager: ChineseSpeechManager,
    currentVoiceId: String,
    onVoiceSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val voices by speechManager.availableVoices.collectAsState()
    val currentVoice = voices.find { it.id == currentVoiceId } ?: voices.firstOrNull()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Giọng đọc tiếng Trung:",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = MucGiayColors.InkFaint
            )
            Text(
                text = "${voices.size} giọng khả dụng",
                fontSize = 10.5.sp,
                color = MucGiayColors.Jade,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(4.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MucGiayColors.Paper,
                border = BorderStroke(1.dp, MucGiayColors.Hairline),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currentVoice?.let { voice ->
                            VoiceBadge(voice.badge)
                            Column {
                                Text(
                                    text = voice.displayName,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MucGiayColors.Ink
                                )
                                if (voice.description.isNotBlank()) {
                                    Text(
                                        text = voice.description,
                                        fontSize = 10.5.sp,
                                        color = MucGiayColors.InkSoft,
                                        maxLines = 1
                                    )
                                }
                            }
                        } ?: Text(
                            text = "Chọn giọng đọc",
                            fontSize = 12.5.sp,
                            color = MucGiayColors.InkSoft
                        )
                    }

                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Mở danh sách",
                        tint = MucGiayColors.InkSoft
                    )
                }
            }

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(MucGiayColors.Paper)
                    .heightIn(max = 380.dp)
            ) {
                voices.forEach { voice ->
                    val isSelected = voice.id == currentVoiceId
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        VoiceBadge(voice.badge)
                                        Text(
                                            text = voice.displayName,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MucGiayColors.SealSon else MucGiayColors.Ink
                                        )
                                    }
                                    if (voice.description.isNotBlank()) {
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = voice.description,
                                            fontSize = 10.5.sp,
                                            color = MucGiayColors.InkSoft,
                                            lineHeight = 14.sp
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Đang chọn",
                                        tint = MucGiayColors.SealSon,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        onClick = {
                            onVoiceSelected(voice.id)
                            expanded = false
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = MucGiayColors.Ink
                        )
                    )
                    HorizontalDivider(color = MucGiayColors.Hairline.copy(alpha = 0.4f), thickness = 0.5.dp)
                }
            }
        }
    }
}

// ===== ITEM CHỌN FILE ÂM THANH TÙY CHỌN =====
@Composable
fun SoundPickerItem(
    label: String,
    path: String?,
    onPick: (Uri) -> Unit,
    onReset: () -> Unit,
    onTest: () -> Unit
) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onPick(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MucGiayColors.PaperDeep)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MucGiayColors.Ink)
                Text(
                    text = if (path != null) File(path).name.substringAfterLast("custom_").substringAfter("_") else "Mặc định",
                    fontSize = 11.sp,
                    color = if (path != null) MucGiayColors.Jade else MucGiayColors.InkSoft
                )
            }

            Row {
                IconButton(onClick = onTest) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Nghe thử", tint = MucGiayColors.InkSoft)
                }

                if (path != null) {
                    IconButton(onClick = onReset) {
                        Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = MucGiayColors.SealSon)
                    }
                }

                IconButton(onClick = { launcher.launch("audio/*") }) {
                    Icon(Icons.Default.Add, contentDescription = "Chọn file", tint = MucGiayColors.InkSoft)
                }
            }
        }
    }
}
