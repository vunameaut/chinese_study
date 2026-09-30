package vhn.dev.study_chines.ui.quiz

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlin.random.Random
import vhn.dev.study_chines.data.remote.VocabularyDto
import vhn.dev.study_chines.ui.theme.MucGiayColors

// ===== FLASHCARD COMPONENT =====
@Composable
fun FlashCard(
    hanzi: String,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 88.sp,
    audioButton: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MucGiayColors.PaperDeep),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.5.dp, MucGiayColors.Hairline)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Box(Modifier.matchParentSize()) {
                val hairlineColor = MucGiayColors.Hairline
                Canvas(modifier = Modifier.matchParentSize()) {
                    val gridColor = hairlineColor.copy(alpha = 0.4f)
                    drawLine(gridColor, Offset(size.width * 0.5f, 0f), Offset(size.width * 0.5f, size.height), strokeWidth = 1.dp.toPx())
                    drawLine(gridColor, Offset(0f, size.height * 0.5f), Offset(size.width, size.height * 0.5f), strokeWidth = 1.dp.toPx())
                }
            }
            Text(
                hanzi,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Black,
                fontSize = fontSize,
                color = MucGiayColors.Ink,
                lineHeight = 1.em
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                if (audioButton != null) {
                    audioButton()
                } else {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(MucGiayColors.SealSon),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "漢",
                            fontFamily = FontFamily.Serif,
                            fontSize = 11.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// ===== FINISH SCREEN =====
@Composable
fun FinishContent(
    correct: Int,
    wrong: Int,
    isRepractice: Boolean = false,
    wrongItems: List<VocabularyDto> = emptyList(),
    onSpeak: ((String) -> Unit)? = null,
    onBack: () -> Unit
) {
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }
    val iconScale by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "finishIcon"
    )

    Box(Modifier.fillMaxSize()) {
        ConfettiRain(Modifier.fillMaxSize())
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(56.dp).scale(iconScale),
                tint = MucGiayColors.Jade
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = if (isRepractice) "Hoàn thành ôn lại" else "Hoàn thành ôn tập!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MucGiayColors.Ink
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (isRepractice) "Lượt ôn lại không tính vào thống kê tiến độ" else "Hôm nay bạn đã thuộc $correct từ vựng",
                color = MucGiayColors.InkSoft,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 32.dp).fillMaxWidth()
            ) {
                StatColumn(value = "$correct", label = "Thuộc được", color = MucGiayColors.Jade)
                Spacer(Modifier.width(44.dp))
                StatColumn(value = "$wrong", label = "Lần chọn sai", color = MucGiayColors.SealSon)
            }

            // Danh sách các từ cần ôn lại nếu có
            if (wrongItems.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                WrongItemsSummaryBox(
                    wrongItems = wrongItems,
                    onSpeak = onSpeak
                )
            }

            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = MucGiayColors.SealSon),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) {
                Text("Về trang chủ", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun WrongItemsSummaryBox(
    wrongItems: List<VocabularyDto>,
    onSpeak: ((String) -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MucGiayColors.PaperDeep),
        border = BorderStroke(1.2.dp, MucGiayColors.Hairline)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "⚠️ Các từ cần ôn lại (${wrongItems.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = MucGiayColors.SealSon
                )
                Text(
                    "Nhấn ▶ để nghe",
                    fontSize = 11.5.sp,
                    color = MucGiayColors.InkFaint
                )
            }
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = MucGiayColors.Hairline.copy(alpha = 0.6f), thickness = 0.8.dp)
            Spacer(Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                wrongItems.forEach { vocab ->
                    WrongVocabRow(vocab = vocab, onSpeak = onSpeak)
                }
            }
        }
    }
}

@Composable
private fun WrongVocabRow(
    vocab: VocabularyDto,
    onSpeak: ((String) -> Unit)?
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MucGiayColors.Paper,
        border = BorderStroke(1.dp, MucGiayColors.Hairline.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = vocab.hanzi,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MucGiayColors.Ink
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = vocab.pinyin,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.5.sp,
                        color = MucGiayColors.Amber
                    )
                    Spacer(Modifier.height(1.dp))
                    Text(
                        text = if (!vocab.wordType.isNullOrBlank()) "${vocab.meaning} (${vocab.wordType})" else vocab.meaning,
                        fontSize = 12.sp,
                        color = MucGiayColors.InkSoft,
                        maxLines = 1
                    )
                }
            }

            IconButton(
                onClick = { onSpeak?.invoke(vocab.hanzi) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Phát âm",
                    tint = MucGiayColors.InkSoft,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun StatColumn(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.displayLarge.copy(fontSize = 36.sp), color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, letterSpacing = spToEm(0.02f))
    }
}

// ===== CONFETTI =====
data class Particle(val x: Float, val y: Float, val speed: Float, val size: Float, val color: Color)

@Composable
fun ConfettiRain(modifier: Modifier = Modifier) {
    val particles = remember {
        val colors = listOf(Color(0xFFC73E2E), Color(0xFF35705F), Color(0xFF8F6409), Color(0xFF4F46E5), Color(0xFFE5DCC9))
        List(60) {
            Particle(
                x = Random.nextFloat(),
                y = -Random.nextFloat() * 0.5f,
                speed = 0.4f + Random.nextFloat() * 0.6f,
                size = 6f + Random.nextFloat() * 8f,
                color = colors[Random.nextInt(colors.size)]
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "confetti")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confettiY"
    )

    val alpha = (1f - (progress - 0.5f).coerceAtLeast(0f) * 2f).coerceIn(0f, 1f)

    Canvas(modifier = modifier) {
        particles.forEach { p ->
            val y = (p.y + progress * p.speed) * size.height
            val sway = kotlin.math.sin((progress * 8f + p.x * 10f).toDouble()).toFloat() * 24f
            drawCircle(
                color = p.color.copy(alpha = p.color.alpha * alpha),
                radius = p.size,
                center = Offset(p.x * size.width + sway, y)
            )
        }
    }
}

// Helper
fun spToEm(v: Float): androidx.compose.ui.unit.TextUnit = v.em