package vhn.dev.study_chines.ui.entry

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import vhn.dev.study_chines.ui.theme.MucGiayColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryScreen(
    viewModel: EntryViewModel,
    onNavigateBack: () -> Unit
) {
    val hanzi by viewModel.hanzi.collectAsStateWithLifecycle()
    val pinyin by viewModel.pinyin.collectAsStateWithLifecycle()
    val wordType by viewModel.wordType.collectAsStateWithLifecycle()
    val meaning by viewModel.meaning.collectAsStateWithLifecycle()
    val isSaved by viewModel.isSaved.collectAsStateWithLifecycle()
    val savedCount by viewModel.savedCount.collectAsStateWithLifecycle()

    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(isSaved) {
        if (isSaved) {
            snackbarHostState.showSnackbar("Đã lưu $hanzi vào sổ")
            viewModel.resetSaveState()
            focusManager.clearFocus()
        }
    }

    Scaffold(
        containerColor = MucGiayColors.Paper,
        contentColor = MucGiayColors.Ink,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Thêm từ vựng mới", style = MaterialTheme.typography.headlineMedium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Trở về", tint = MucGiayColors.InkSoft)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Saved count badge
            if (savedCount > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Đã thêm $savedCount từ", style = MaterialTheme.typography.labelMedium, color = MucGiayColors.Jade)
                }
            }

            InputField(label = "Chữ Hán *", value = hanzi, placeholder = "Nhập chữ Hán",
                isHanzi = true, imeAction = ImeAction.Next,
                onNext = { focusManager.moveFocus(FocusDirection.Down) }) { viewModel.updateHanzi(it) }
            InputField(label = "Phiên âm Pinyin *", value = pinyin, placeholder = "VD: xuéxiào",
                isHanzi = false, imeAction = ImeAction.Next,
                onNext = { focusManager.moveFocus(FocusDirection.Down) }) { viewModel.updatePinyin(it) }
            InputField(label = "Loại từ", value = wordType, placeholder = "VD: danh từ, động từ",
                isHanzi = false, imeAction = ImeAction.Next,
                onNext = { focusManager.moveFocus(FocusDirection.Down) }) { viewModel.updateWordType(it) }
            InputField(label = "Nghĩa tiếng Việt *", value = meaning, placeholder = "Nhập nghĩa tiếng Việt",
                isHanzi = false, imeAction = ImeAction.Done,
                onNext = { focusManager.clearFocus(); viewModel.saveVocabulary() }) { viewModel.updateMeaning(it) }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { viewModel.saveVocabulary() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = hanzi.isNotBlank() && pinyin.isNotBlank() && meaning.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MucGiayColors.SealSon),
                shape = RoundedCornerShape(10.dp)
            ) { Text("Lưu vào sổ", fontWeight = FontWeight.SemiBold) }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun InputField(
    label: String, value: String, placeholder: String,
    isHanzi: Boolean, imeAction: ImeAction,
    onNext: () -> Unit, onChange: (String) -> Unit
) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value, onValueChange = { onChange(it) }, label = null,
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = if (isHanzi) androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Serif, fontSize = 20.sp)
                       else androidx.compose.ui.text.TextStyle(fontSize = 15.sp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = imeAction),
            keyboardActions = KeyboardActions(onNext = { onNext() }, onDone = { onNext() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MucGiayColors.Jade,
                cursorColor = MucGiayColors.Ink,
                unfocusedBorderColor = MucGiayColors.Hairline
            ),
            shape = RoundedCornerShape(10.dp)
        )
    }
}