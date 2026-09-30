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

// Muc & Giay palette — Light
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

// Muc & Giay palette — Dark
private val DPaper_ = Color(0xFF18181B)
private val DPaperDeep_ = Color(0xFF27272A)
private val DPaperCard_ = Color(0xFF1F1F23)
private val DInk_ = Color(0xFFF4F4F5)
private val DInkSoft_ = Color(0xFFA1A1AA)
private val DInkFaint_ = Color(0xFF71717A)
private val DHairline_ = Color(0xFF3F3F46)
private val DSealSon_ = Color(0xFFF17568)
private val DSealDeep_ = Color(0xFFE55A4D)
private val DJade_ = Color(0xFF34D399)
private val DJadeFill_ = Color(0xFF10B981)
private val DJadeTint_ = Color(0xFF0A2A1F)
private val DAmber_ = Color(0xFFFBBF24)
private val DAmberTint_ = Color(0xFF2D2408)
private val DSlateTint_ = Color(0xFF1E2025)
private val DRedBg_ = Color(0xFF2C1A18)

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

private val DarkColorScheme = darkColorScheme(
    primary = DSealSon_,
    onPrimary = Color.White,
    primaryContainer = DJadeTint_,
    onPrimaryContainer = DJade_,
    secondary = DJade_,
    onSecondary = Color(0xFF0A2A1F),
    secondaryContainer = DJadeTint_,
    onSecondaryContainer = DJade_,
    tertiary = DAmber_,
    onTertiary = Color(0xFF2D2408),
    tertiaryContainer = DAmberTint_,
    onTertiaryContainer = DAmber_,
    background = DPaper_,
    onBackground = DInk_,
    surface = DPaper_,
    onSurface = DInk_,
    surfaceVariant = DPaperCard_,
    onSurfaceVariant = DInkSoft_,
    outline = DHairline_,
    outlineVariant = DHairline_.copy(alpha = 0.5f),
    error = DSealSon_,
    onError = Color.White,
    errorContainer = DRedBg_,
    onErrorContainer = DSealDeep_,
    inverseSurface = DInk_,
    inverseOnSurface = DPaper_,
    inversePrimary = DPaper_,
    surfaceTint = DPaperDeep_,
)

@Composable
fun HanziQuizTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
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

// Helper colors for use in composables — always resolves to correct light/dark values via MaterialTheme
object MucGiayColors {
    // Light palette (still available for hardcoded uses)
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
    val Indigo = Color(0xFF4F46E5)
    val IndigoFill = Color(0xFF4338CA)
    val IndigoTint = Color(0xFFEEF2FF)
    val Purple = Color(0xFF7C3AED)
    val PurpleTint = Color(0xFFF3E8FF)

    // Dark palette equivalents
    val DarkPaper = DPaper_
    val DarkPaperDeep = DPaperDeep_
    val DarkInk = DInk_
    val DarkInkSoft = DInkSoft_
    val DarkHairline = DHairline_
    val DarkSealSon = DSealSon_
    val DarkJade = DJade_
    val DarkJadeTint = DJadeTint_
    val DarkAmber = DAmber_
    val DarkAmberTint = DAmberTint_
}
