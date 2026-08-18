package vhn.dev.study_chines.ui.entry

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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

    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(isSaved) {
        if (isSaved) {
            snackbarHostState.showSnackbar("Đã lưu từ vựng!")
            viewModel.resetSaveState()
            focusManager.clearFocus()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Nhập từ vựng mới") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = hanzi,
                onValueChange = { viewModel.updateHanzi(it) },
                label = { Text("Chữ Hán (*)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = pinyin,
                onValueChange = { viewModel.updatePinyin(it) },
                label = { Text("Pinyin (*)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = wordType,
                onValueChange = { viewModel.updateWordType(it) },
                label = { Text("Từ loại (không bắt buộc)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = meaning,
                onValueChange = { viewModel.updateMeaning(it) },
                label = { Text("Nghĩa tiếng Việt (*)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.saveVocabulary()
                    }
                ),
                singleLine = true
            )

            Button(
                onClick = { viewModel.saveVocabulary() },
                modifier = Modifier.fillMaxWidth(),
                enabled = hanzi.isNotBlank() && pinyin.isNotBlank() && meaning.isNotBlank()
            ) {
                Text("Lưu từ vựng")
            }
            
            OutlinedButton(
                 onClick = onNavigateBack,
                 modifier = Modifier.fillMaxWidth()
            ) {
                Text("Trở về")
            }
        }
    }
}
