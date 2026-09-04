package vhn.dev.study_chines.ui.quiz

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
import vhn.dev.study_chines.ui.theme.MucGiayColors

// ===== FLASHCARD COMPONENT =====
@Composable
fun FlashCard(
    hanzi: String,
    modifier: Modifier = Modifier
        .widthIn(max = 220.dp)
        .aspectRatio(1f),
    fontSize: androidx.compose.ui.unit.TextUnit = 64.sp
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
                Canvas(modifier = Modifier.matchParentSize()) {
                    val gridColor = MucGiayColors.Hairline.copy(alpha = 0.4f)
                    drawLine(gridColor, Offset(size.width * 0.5f, 0f), Offset(size.width * 0.5f, size.height), strokeWidth = 1.dp.toPx())
                    drawLine(gridColor, Offset(0f, size.height * 0.5f), Offset(size.width, size.height * 0.5f), strokeWidth = 1.dp.toPx())
                }
            }
            Text(hanzi,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Black,
                fontSize = fontSize,
                color = MucGiayColors.Ink,
                lineHeight = 1.em
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .size(26.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(MucGiayColors.SealSon),
                contentAlignment = Alignment.Center
            ) {
                Text("学", fontFamily = FontFamily.Serif, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ===== FINISH SCREEN =====
@Composable
fun FinishContent(correct: Int, wrong: Int, onBack: () -> Unit) {
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }
    val iconScale by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "finishIcon"
    )

    Box(Modifier.fillMaxSize()) {
        ConfettiRain(Modifier.fillMaxSize())
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.weight(0.15f))
            Icon(Icons.Default.CheckCircle, contentDescription = null,
                modifier = Modifier.size(56.dp).scale(iconScale), tint = MucGiayColors.Jade)
            Spacer(Modifier.height(12.dp))
            Text("Hoàn thành", style = MaterialTheme.typography.headlineMedium, color = MucGiayColors.Ink)
            Text("Hôm nay bạn đã thuộc $correct từ", color = MucGiayColors.InkSoft, fontSize = 14.sp)

            Spacer(Modifier.height(28.dp))
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(horizontal = 32.dp).fillMaxWidth()) {
                StatColumn(value = "$correct", label = "Thuộc được", color = MucGiayColors.Jade)
                Spacer(Modifier.width(40.dp))
                StatColumn(value = "$wrong", label = "Lần sai", color = MucGiayColors.SealSon)
            }

            Spacer(Modifier.weight(0.2f))
            Button(
                onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = MucGiayColors.SealSon),
                shape = RoundedCornerShape(10.dp), modifier = Modifier.height(52.dp).fillMaxWidth().padding(horizontal = 32.dp)
            ) { Text("Về trang chủ", fontWeight = FontWeight.SemiBold) }
            Spacer(Modifier.weight(0.1f))
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
    val colors = listOf(
        Color(0xFFE53935), Color(0xFFFFB300), Color(0xFF43A047),
        Color(0xFF1E88E5), Color(0xFFEC407A), Color(0xFF8E24AA),
        Color(0xFFFF7043), Color(0xFF00ACC1)
    )
    val particles = remember {
        List(60) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                speed = Random.nextFloat() * 0.6f + 0.3f,
                size = Random.nextFloat() * 7f + 4f,
                color = colors[Random.nextInt(colors.size)]
            )
        }
    }
    val transition = rememberInfiniteTransition(label = "confetti")
    val progress by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3200, easing = LinearEasing)),
        label = "confettiProgress"
    )
    Canvas(modifier = modifier) {
        particles.forEach { p ->
            val y = ((p.y + progress * p.speed) % 1.05f) * size.height
            val sway = kotlin.math.sin((progress * 6f + p.x * 10f).toFloat()) * 20f
            drawCircle(
                color = p.color,
                radius = p.size,
                center = Offset(p.x * size.width + sway, y)
            )
        }
    }
}

// Helper
fun spToEm(v: Float): androidx.compose.ui.unit.TextUnit = v.em
