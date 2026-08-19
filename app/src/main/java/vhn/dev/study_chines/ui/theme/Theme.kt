package vhn.dev.study_chines.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// Mực & Giấy palette
private val Paper_ = Color(0xFFFAF6EF)
private val PaperDeep_ = Color(0xFFF3EDE1)
private val Ink_ = Color(0xFF26221C)
private val InkSoft_ = Color(0xFF6B6357)
private val InkFaint_ = Color(0xFFA89F90)
private val Hairline_ = Color(0xFFE5DCC9)
private val SealSon_ = Color(0xFFC73E2E)
private val SealDeep_ = Color(0xFFA33225)
private val Jade_ = Color(0xFF35705F)
private val JadeFill_ = Color(0xFF3E8A74)
private val JadeTint_ = Color(0xFFE9F2EE)
private val Amber_ = Color(0xFF8F6409)
private val AmberTint_ = Color(0xFFF5ECD9)
private val Slate_ = Color(0xFF5B6770)
private val SlateTint_ = Color(0xFFEEF0F2)
private val RedBg_ = Color(0xFFF7E5E1)

private val LightColorScheme = lightColorScheme(
    primary = SealSon_,
    onPrimary = Color.White,
    primaryContainer = JadeTint_,
    onPrimaryContainer = Jade_,
    secondary = Jade_,
    onSecondary = Color.White,
    secondaryContainer = JadeTint_,
    onSecondaryContainer = Jade_,
    tertiary = Amber_,
    onTertiary = Color.White,
    tertiaryContainer = AmberTint_,
    onTertiaryContainer = Amber_,
    background = Paper_,
    onBackground = Ink_,
    surface = Paper_,
    onSurface = Ink_,
    surfaceVariant = Hairline_,
    onSurfaceVariant = InkSoft_,
    outline = Hairline_,
    outlineVariant = Hairline_.copy(alpha = 0.5f),
    error = SealSon_,
    onError = Color.White,
    errorContainer = RedBg_,
    onErrorContainer = SealDeep_,
    inverseSurface = Ink_,
    inverseOnSurface = Paper_,
    inversePrimary = Paper_,
    surfaceTint = PaperDeep_,
)

@Composable
fun HanziQuizTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = TextStyle.Default.lineHeight,
        letterSpacing = (-0.02).em
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        letterSpacing = (-0.01).em
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        letterSpacing = 0.02.em
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 0.06.em
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        letterSpacing = 0.02.em
    )
)

// Helper colors for use in composables
object MucGiayColors {
    val Paper = Paper_
    val PaperDeep = PaperDeep_
    val Ink = Ink_
    val InkSoft = InkSoft_
    val InkFaint = InkFaint_
    val Hairline = Hairline_
    val SealSon = SealSon_
    val SealDeep = SealDeep_
    val Jade = Jade_
    val JadeFill = JadeFill_
    val JadeTint = JadeTint_
    val Amber = Amber_
    val AmberTint = AmberTint_
    val Slate = Slate_
    val SlateTint = SlateTint_
    val RedBg = RedBg_
}