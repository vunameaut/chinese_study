package vhn.dev.study_chines.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// Muc & Giay palette - Light
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

// Muc & Giay palette - Dark
private val DPaper_ = Color(0xFF18181B)
private val DPaperDeep_ = Color(0xFF27272A)
private val DPaperCard_ = Color(0xFF222226)
private val DInk_ = Color(0xFFF4F4F5)
private val DInkSoft_ = Color(0xFFA1A1AA)
private val DInkFaint_ = Color(0xFF71717A)
private val DHairline_ = Color(0xFF3F3F46)
private val DSealSon_ = Color(0xFFE05A4D)
private val DSealDeep_ = Color(0xFFC73E2E)
private val DJade_ = Color(0xFF34D399)
private val DJadeFill_ = Color(0xFF10B981)
private val DJadeTint_ = Color(0xFF0D281E)
private val DAmber_ = Color(0xFFFBBF24)
private val DAmberTint_ = Color(0xFF2D2305)
private val DSlateTint_ = Color(0xFF1E2025)
private val DRedBg_ = Color(0xFF3B1815)

class AppPalette(
    val Paper: Color,
    val PaperDeep: Color,
    val Ink: Color,
    val InkSoft: Color,
    val InkFaint: Color,
    val Hairline: Color,
    val SealSon: Color,
    val SealDeep: Color,
    val Jade: Color,
    val JadeFill: Color,
    val JadeTint: Color,
    val Amber: Color,
    val AmberTint: Color,
    val Slate: Color,
    val SlateTint: Color,
    val RedBg: Color,
    val Indigo: Color,
    val IndigoFill: Color,
    val IndigoTint: Color,
    val Purple: Color,
    val PurpleTint: Color
)

val LightPalette = AppPalette(
    Paper = Paper_,
    PaperDeep = PaperDeep_,
    Ink = Ink_,
    InkSoft = InkSoft_,
    InkFaint = InkFaint_,
    Hairline = Hairline_,
    SealSon = SealSon_,
    SealDeep = SealDeep_,
    Jade = Jade_,
    JadeFill = JadeFill_,
    JadeTint = JadeTint_,
    Amber = Amber_,
    AmberTint = AmberTint_,
    Slate = Slate_,
    SlateTint = SlateTint_,
    RedBg = RedBg_,
    Indigo = Color(0xFF4F46E5),
    IndigoFill = Color(0xFF4338CA),
    IndigoTint = Color(0xFFEEF2FF),
    Purple = Color(0xFF7C3AED),
    PurpleTint = Color(0xFFF3E8FF)
)

val DarkPalette = AppPalette(
    Paper = DPaperCard_,
    PaperDeep = DPaperDeep_,
    Ink = DInk_,
    InkSoft = DInkSoft_,
    InkFaint = DInkFaint_,
    Hairline = DHairline_,
    SealSon = DSealSon_,
    SealDeep = DSealDeep_,
    Jade = DJade_,
    JadeFill = DJadeFill_,
    JadeTint = DJadeTint_,
    Amber = DAmber_,
    AmberTint = DAmberTint_,
    Slate = Color(0xFF94A3B8),
    SlateTint = DSlateTint_,
    RedBg = DRedBg_,
    Indigo = Color(0xFF818CF8),
    IndigoFill = Color(0xFF6366F1),
    IndigoTint = Color(0xFF1E1B4B),
    Purple = Color(0xFFA78BFA),
    PurpleTint = Color(0xFF2E1065)
)

val LocalAppPalette = compositionLocalOf { LightPalette }

object MucGiayColors {
    val Paper: Color @Composable @ReadOnlyComposable get() = LocalAppPalette.current.Paper
    val PaperDeep: Color @Composable @ReadOnlyComposable get() = LocalAppPalette.current.PaperDeep
    val Ink: Color @Composable @ReadOnlyComposable get() = LocalAppPalette.current.Ink
    val InkSoft: Color @Composable @ReadOnlyComposable get() = LocalAppPalette.current.InkSoft
    val InkFaint: Color @Composable @ReadOnlyComposable get() = LocalAppPalette.current.InkFaint
    val Hairline: Color @Composable @ReadOnlyComposable get() = LocalAppPalette.current.Hairline
    val SealSon: Color @Composable @ReadOnlyComposable get() = LocalAppPalette.current.SealSon
    val SealDeep: Color @Composable @ReadOnlyComposable get() = LocalAppPalette.current.SealDeep
    val Jade: Color @Composable @ReadOnlyComposable get() = LocalAppPalette.current.Jade
    val JadeFill: Color @Composable @ReadOnlyComposable get() = LocalAppPalette.current.JadeFill
    val JadeTint: Color @Composable @ReadOnlyComposable get() = LocalAppPalette.current.JadeTint
    val Amber: Color @Composable @ReadOnlyComposable get() = LocalAppPalette.current.Amber
    val AmberTint: Color @Composable @ReadOnlyComposable get() = LocalAppPalette.current.AmberTint
    val Slate: Color @Composable @ReadOnlyComposable get() = LocalAppPalette.current.Slate
    val SlateTint: Color @Composable @ReadOnlyComposable get() = LocalAppPalette.current.SlateTint
    val RedBg: Color @Composable @ReadOnlyComposable get() = LocalAppPalette.current.RedBg
    val Indigo: Color @Composable @ReadOnlyComposable get() = LocalAppPalette.current.Indigo
    val IndigoFill: Color @Composable @ReadOnlyComposable get() = LocalAppPalette.current.IndigoFill
    val IndigoTint: Color @Composable @ReadOnlyComposable get() = LocalAppPalette.current.IndigoTint
    val Purple: Color @Composable @ReadOnlyComposable get() = LocalAppPalette.current.Purple
    val PurpleTint: Color @Composable @ReadOnlyComposable get() = LocalAppPalette.current.PurpleTint

    // Constant colors if ever needed
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
    surfaceVariant = PaperDeep_,
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
    surface = DPaperCard_,
    onSurface = DInk_,
    surfaceVariant = DPaperDeep_,
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
    val appPalette = if (darkTheme) DarkPalette else LightPalette
    CompositionLocalProvider(LocalAppPalette provides appPalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
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

object AppColors {
    val background: Color @Composable get() = MaterialTheme.colorScheme.background
    val surface: Color @Composable get() = MaterialTheme.colorScheme.surface
    val surfaceVariant: Color @Composable get() = MaterialTheme.colorScheme.surfaceVariant
    val onBackground: Color @Composable get() = MaterialTheme.colorScheme.onBackground
    val onSurface: Color @Composable get() = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val outline: Color @Composable get() = MaterialTheme.colorScheme.outline
    val primary: Color @Composable get() = MaterialTheme.colorScheme.primary
    val onPrimary: Color @Composable get() = MaterialTheme.colorScheme.onPrimary
    val secondary: Color @Composable get() = MaterialTheme.colorScheme.secondary
    val tertiary: Color @Composable get() = MaterialTheme.colorScheme.tertiary
    val isDark: Boolean @Composable get() = MaterialTheme.colorScheme.background.luminance() < 0.5f
}

val ColorScheme.backgroundDeep: Color
    @Composable
    get() = if (background.luminance() < 0.5f) Color(0xFF222226) else Color(0xFFEDE8DF)