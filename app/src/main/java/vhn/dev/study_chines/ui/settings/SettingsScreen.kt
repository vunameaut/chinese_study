package vhn.dev.study_chines.ui.settings

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import vhn.dev.study_chines.R
import vhn.dev.study_chines.data.local.UserPreferences
import vhn.dev.study_chines.ui.quiz.SoundManager
import vhn.dev.study_chines.ui.theme.MucGiayColors
import java.io.File
import java.io.FileOutputStream

class SettingsViewModel(val preferences: UserPreferences) : ViewModel() {
    var isSoundEnabled by mutableStateOf(preferences.isSoundEnabled)
        private set

    var customCorrectSoundPath by mutableStateOf(preferences.customCorrectSoundPath)
        private set
    var customWrongSoundPath by mutableStateOf(preferences.customWrongSoundPath)
        private set
    var customFinishSoundPath by mutableStateOf(preferences.customFinishSoundPath)
        private set

    fun toggleSound(enabled: Boolean) {
        preferences.isSoundEnabled = enabled
        isSoundEnabled = enabled
    }

    fun setCustomSound(context: Context, type: String, uri: Uri) {
        val extension = context.contentResolver.getType(uri)?.split("/")?.lastOrNull() ?: "mp3"
        val fileName = "custom_${type}_${System.currentTimeMillis()}.${extension}"
        val file = File(context.filesDir, fileName)
        
        try {
            // Delete old files of the same type to save space
            context.filesDir.listFiles { _, name -> name.startsWith("custom_$type") }?.forEach { it.delete() }

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
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
    DisposableEffect(Unit) { onDispose { soundManager.release() } }

    Scaffold(
        containerColor = MucGiayColors.Paper,
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt", color = MucGiayColors.Ink, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Trở về", tint = MucGiayColors.Ink)
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
            Text("Âm thanh", style = MaterialTheme.typography.titleMedium, color = MucGiayColors.SealSon)
            Spacer(Modifier.height(8.dp))

            // Sound Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MucGiayColors.PaperDeep)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Hiệu ứng âm thanh", fontWeight = FontWeight.SemiBold, color = MucGiayColors.Ink)
                    Text("Bật hoặc tắt âm thanh khi làm quiz", fontSize = 12.sp, color = MucGiayColors.InkSoft)
                }
                Switch(
                    checked = viewModel.isSoundEnabled,
                    onCheckedChange = { viewModel.toggleSound(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = MucGiayColors.Jade, checkedTrackColor = MucGiayColors.JadeTint)
                )
            }

            Spacer(Modifier.height(24.dp))
            Text("Tùy chỉnh âm thanh", style = MaterialTheme.typography.titleMedium, color = MucGiayColors.SealSon)
            Text("Chọn file âm thanh từ máy của bạn", fontSize = 12.sp, color = MucGiayColors.InkSoft)
            Spacer(Modifier.height(8.dp))

            SoundPickerItem(
                label = "Khi trả lời đúng",
                path = viewModel.customCorrectSoundPath,
                onPick = { uri -> viewModel.setCustomSound(context, "correct", uri) },
                onReset = { viewModel.resetSound("correct") },
                onTest = { soundManager.play(R.raw.correct) }
            )

            Spacer(Modifier.height(12.dp))

            SoundPickerItem(
                label = "Khi trả lời sai",
                path = viewModel.customWrongSoundPath,
                onPick = { uri -> viewModel.setCustomSound(context, "wrong", uri) },
                onReset = { viewModel.resetSound("wrong") },
                onTest = { soundManager.play(R.raw.wrong) }
            )

            Spacer(Modifier.height(12.dp))

            SoundPickerItem(
                label = "Khi hoàn thành",
                path = viewModel.customFinishSoundPath,
                onPick = { uri -> viewModel.setCustomSound(context, "finish", uri) },
                onReset = { viewModel.resetSound("finish") },
                onTest = { soundManager.play(R.raw.finish) }
            )
        }
    }
}

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
                Text(label, fontWeight = FontWeight.SemiBold, color = MucGiayColors.Ink)
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
