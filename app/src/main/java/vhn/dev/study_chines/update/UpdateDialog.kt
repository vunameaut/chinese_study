package vhn.dev.study_chines.update

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import vhn.dev.study_chines.ui.theme.MucGiayColors
import java.io.File
import java.util.Locale

@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo,
    currentVersion: String,
    state: AppUpdateState,
    onDismiss: () -> Unit,
    onStartDownload: () -> Unit,
    onInstall: (File) -> Unit,
    onRequestPermission: () -> Unit,
    hasInstallPermission: Boolean
) {
    Dialog(
        onDismissRequest = {
            // Không cho dismiss khi đang tải dở
            if (state !is AppUpdateState.Downloading) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = state !is AppUpdateState.Downloading,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MucGiayColors.Paper,
            border = BorderStroke(1.dp, MucGiayColors.Hairline),
            shadowElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MucGiayColors.JadeTint)
                        .border(1.dp, MucGiayColors.Jade.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = MucGiayColors.Jade,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title
                Text(
                    text = "Bản Cập Nhật Mới",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = MucGiayColors.Ink
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Version Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Hiện tại: v$currentVersion",
                        fontSize = 12.sp,
                        color = MucGiayColors.InkSoft
                    )
                    Text(
                        text = "➔",
                        fontSize = 12.sp,
                        color = MucGiayColors.InkFaint
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MucGiayColors.SealSon.copy(alpha = 0.12f),
                        border = BorderStroke(0.5.dp, MucGiayColors.SealSon.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = updateInfo.versionName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MucGiayColors.SealSon,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                if (updateInfo.fileSize > 0) {
                    val sizeMb = updateInfo.fileSize / (1024.0 * 1024.0)
                    Text(
                        text = String.format(Locale.getDefault(), "Dung lượng: %.1f MB", sizeMb),
                        fontSize = 11.sp,
                        color = MucGiayColors.InkFaint,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Release Notes Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MucGiayColors.PaperDeep,
                    border = BorderStroke(1.dp, MucGiayColors.Hairline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 60.dp, max = 150.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MucGiayColors.Amber,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Nội dung cập nhật:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MucGiayColors.Ink
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = updateInfo.releaseNotes,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = MucGiayColors.InkSoft
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Trạng thái tải & Cài đặt
                when (state) {
                    is AppUpdateState.Downloading -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (state.progressPercent >= 0) {
                                LinearProgressIndicator(
                                    progress = { state.progressPercent / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = MucGiayColors.Jade,
                                    trackColor = MucGiayColors.JadeTint,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                val downloadedMb = state.downloadedBytes / (1024.0 * 1024.0)
                                val totalMb = state.totalBytes / (1024.0 * 1024.0)
                                Text(
                                    text = String.format(Locale.getDefault(), "Đang tải: %d%% (%.1f / %.1f MB)", state.progressPercent, downloadedMb, totalMb),
                                    fontSize = 12.sp,
                                    color = MucGiayColors.InkSoft
                                )
                            } else {
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = MucGiayColors.Jade,
                                    trackColor = MucGiayColors.JadeTint,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Đang tải bản cập nhật...",
                                    fontSize = 12.sp,
                                    color = MucGiayColors.InkSoft
                                )
                            }
                        }
                    }

                    is AppUpdateState.ReadyToInstall -> {
                        if (!hasInstallPermission) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MucGiayColors.AmberTint,
                                border = BorderStroke(1.dp, MucGiayColors.Amber.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = MucGiayColors.Amber,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Cần cho phép app cài đặt ứng dụng từ nguồn này để tiếp tục.",
                                        fontSize = 11.sp,
                                        color = MucGiayColors.Ink
                                    )
                                }
                            }

                            Button(
                                onClick = onRequestPermission,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MucGiayColors.Amber
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Cấp Quyền Cài Đặt", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = { onInstall(state.apkFile) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MucGiayColors.Jade
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cài Đặt Ngay", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    is AppUpdateState.Error -> {
                        Text(
                            text = "Lỗi: ${state.message}",
                            fontSize = 12.sp,
                            color = MucGiayColors.SealSon,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Button(
                            onClick = onStartDownload,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MucGiayColors.SealSon
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Thử Tải Lại", fontWeight = FontWeight.Bold)
                        }
                    }

                    else -> {
                        // Trạng thái Available
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, MucGiayColors.Hairline),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Để sau", color = MucGiayColors.InkSoft)
                            }

                            Button(
                                onClick = onStartDownload,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MucGiayColors.SealSon
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Cập nhật", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
