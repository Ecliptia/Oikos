package com.ecliptia.oikos.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.ecliptia.oikos.ui.theme.BackgroundDark
import com.ecliptia.oikos.ui.theme.BackgroundLight
import com.ecliptia.oikos.ui.theme.BlueDarkColorScheme
import com.ecliptia.oikos.ui.theme.BlueLightColorScheme
import com.ecliptia.oikos.ui.theme.ErrorDark
import com.ecliptia.oikos.ui.theme.ErrorLight
import com.ecliptia.oikos.ui.theme.GoldDark
import com.ecliptia.oikos.ui.theme.GoldDarkContainer
import com.ecliptia.oikos.ui.theme.GoldLight
import com.ecliptia.oikos.ui.theme.GoldLightContainer
import com.ecliptia.oikos.ui.theme.OnErrorDark
import com.ecliptia.oikos.ui.theme.OnErrorLight
import com.ecliptia.oikos.ui.theme.OnGoldDark
import com.ecliptia.oikos.ui.theme.OnGoldLight
import com.ecliptia.oikos.ui.theme.OnOrangeDark
import com.ecliptia.oikos.ui.theme.OnOrangeLight
import com.ecliptia.oikos.ui.theme.OnSecondaryDark
import com.ecliptia.oikos.ui.theme.OnSecondaryLight
import com.ecliptia.oikos.ui.theme.OnSurfaceDark
import com.ecliptia.oikos.ui.theme.OnSurfaceLight
import com.ecliptia.oikos.ui.theme.OnSurfaceVariantDark
import com.ecliptia.oikos.ui.theme.OnSurfaceVariantLight
import com.ecliptia.oikos.ui.theme.OnTertiaryDark
import com.ecliptia.oikos.ui.theme.OnTertiaryLight
import com.ecliptia.oikos.ui.theme.OrangeDark
import com.ecliptia.oikos.ui.theme.OrangeDarkContainer
import com.ecliptia.oikos.ui.theme.OrangeLight
import com.ecliptia.oikos.ui.theme.OrangeLightContainer
import com.ecliptia.oikos.ui.theme.OutlineDark
import com.ecliptia.oikos.ui.theme.OutlineLight
import com.ecliptia.oikos.ui.theme.PrimaryDark
import com.ecliptia.oikos.ui.theme.PrimaryLight
import com.ecliptia.oikos.ui.theme.SecondaryDark
import com.ecliptia.oikos.ui.theme.SecondaryLight
import com.ecliptia.oikos.ui.theme.SurfaceDark
import com.ecliptia.oikos.ui.theme.SurfaceLight
import com.ecliptia.oikos.ui.theme.SurfaceVariantDark
import com.ecliptia.oikos.ui.theme.SurfaceVariantLight
import com.ecliptia.oikos.ui.theme.TertiaryDark
import com.ecliptia.oikos.ui.theme.TertiaryLight

// --- Red (Bad) Theme ---
// Source: #B71C1C
private val red_light_primary = Color(0xFFB52221)
private val red_light_onPrimary = Color(0xFFFFFFFF)
private val red_light_primaryContainer = Color(0xFFFFDAD7)
private val red_light_onPrimaryContainer = Color(0xFF410004)
private val red_light_secondary = Color(0xFF775653)
private val red_light_onSecondary = Color(0xFFFFFFFF)
private val red_light_secondaryContainer = Color(0xFFFFDAD7)
private val red_light_onSecondaryContainer = Color(0xFF2C1513)
private val red_light_tertiary = Color(0xFF715B2E)
private val red_light_onTertiary = Color(0xFFFFFFFF)
private val red_light_tertiaryContainer = Color(0xFFFDDFA6)
private val red_light_onTertiaryContainer = Color(0xFF261A00)
private val red_light_error = Color(0xFFBA1A1A)
private val red_light_onError = Color(0xFFFFFFFF)
private val red_light_errorContainer = Color(0xFFFFDAD6)
private val red_light_onErrorContainer = Color(0xFF410002)
private val red_light_background = Color(0xFFFCFCFC)
private val red_light_onBackground = Color(0xFF201A19)
private val red_light_surface = Color(0xFFFCFCFC)
private val red_light_onSurface = Color(0xFF201A19)
private val red_light_surfaceVariant = Color(0xFFF5DDDA)
private val red_light_onSurfaceVariant = Color(0xFF534341)
private val red_light_outline = Color(0xFF857371)

private val red_dark_primary = Color(0xFFFFB3B1)
private val red_dark_onPrimary = Color(0xFF680006)
private val red_dark_primaryContainer = Color(0xFF93000B)
private val red_dark_onPrimaryContainer = Color(0xFFFFDAD7)
private val red_dark_secondary = Color(0xFFE7BDB8)
private val red_dark_onSecondary = Color(0xFF442927)
private val red_dark_secondaryContainer = Color(0xFF5D3F3C)
private val red_dark_onSecondaryContainer = Color(0xFFFFDAD7)
private val red_dark_tertiary = Color(0xFFE0C38C)
private val red_dark_onTertiary = Color(0xFF3F2E04)
private val red_dark_tertiaryContainer = Color(0xFF584419)
private val red_dark_onTertiaryContainer = Color(0xFFFDDFA6)
private val red_dark_error = Color(0xFFFFB4AB)
private val red_dark_onError = Color(0xFF690005)
private val red_dark_errorContainer = Color(0xFF93000A)
private val red_dark_onErrorContainer = Color(0xFFFFB4AB)
private val red_dark_background = Color(0xFF201A19)
private val red_dark_onBackground = Color(0xFFEDE0DE)
private val red_dark_surface = Color(0xFF201A19)
private val red_dark_onSurface = Color(0xFFEDE0DE)
private val red_dark_surfaceVariant = Color(0xFF534341)
private val red_dark_onSurfaceVariant = Color(0xFFD8C2BF)
private val red_dark_outline = Color(0xFFA08C8A)

val RedLightColorScheme = lightColorScheme(
    primary = red_light_primary,
    onPrimary = red_light_onPrimary,
    primaryContainer = red_light_primaryContainer,
    onPrimaryContainer = red_light_onPrimaryContainer,
    secondary = red_light_secondary,
    onSecondary = red_light_onSecondary,
    secondaryContainer = red_light_secondaryContainer,
    onSecondaryContainer = red_light_onSecondaryContainer,
    tertiary = red_light_tertiary,
    onTertiary = red_light_onTertiary,
    tertiaryContainer = red_light_tertiaryContainer,
    onTertiaryContainer = red_light_onTertiaryContainer,
    error = red_light_error,
    onError = red_light_onError,
    errorContainer = red_light_errorContainer,
    onErrorContainer = red_light_onErrorContainer,
    background = red_light_background,
    onBackground = red_light_onBackground,
    surface = red_light_surface,
    onSurface = red_light_onSurface,
    surfaceVariant = red_light_surfaceVariant,
    onSurfaceVariant = red_light_onSurfaceVariant,
    outline = red_light_outline,
)

val RedDarkColorScheme = darkColorScheme(
    primary = red_dark_primary,
    onPrimary = red_dark_onPrimary,
    primaryContainer = red_dark_primaryContainer,
    onPrimaryContainer = red_dark_onPrimaryContainer,
    secondary = red_dark_secondary,
    onSecondary = red_dark_onSecondary,
    secondaryContainer = red_dark_secondaryContainer,
    onSecondaryContainer = red_dark_onSecondaryContainer,
    tertiary = red_dark_tertiary,
    onTertiary = red_dark_onTertiary,
    tertiaryContainer = red_dark_tertiaryContainer,
    onTertiaryContainer = red_dark_onTertiaryContainer,
    error = red_dark_error,
    onError = red_dark_onError,
    errorContainer = red_dark_errorContainer,
    onErrorContainer = red_dark_onErrorContainer,
    background = red_dark_background,
    onBackground = red_dark_onBackground,
    surface = red_dark_surface,
    onSurface = red_dark_onSurface,
    surfaceVariant = red_dark_surfaceVariant,
    onSurfaceVariant = red_dark_onSurfaceVariant,
    outline = red_dark_outline,
)


// --- Blue (Good) Theme ---
// Source: #1E88E5
private val blue_light_primary = Color(0xFF0062A1)
private val blue_light_onPrimary = Color(0xFFFFFFFF)
private val blue_light_primaryContainer = Color(0xFFD0E4FF)
private val blue_light_onPrimaryContainer = Color(0xFF001D35)
private val blue_light_secondary = Color(0xFF535F70)
private val blue_light_onSecondary = Color(0xFFFFFFFF)
private val blue_light_secondaryContainer = Color(0xFFD7E3F7)
private val blue_light_onSecondaryContainer = Color(0xFF101C2B)
private val blue_light_tertiary = Color(0xFF6B5778)
private val blue_light_onTertiary = Color(0xFFFFFFFF)
private val blue_light_tertiaryContainer = Color(0xFFF2DAFF)
private val blue_light_onTertiaryContainer = Color(0xFF251431)
private val blue_light_error = Color(0xFFBA1A1A)
private val blue_light_onError = Color(0xFFFFFFFF)
private val blue_light_errorContainer = Color(0xFFFFDAD6)
private val blue_light_onErrorContainer = Color(0xFF410002)
private val blue_light_background = Color(0xFFFDFCFF)
private val blue_light_onBackground = Color(0xFF1A1C1E)
private val blue_light_surface = Color(0xFFFDFCFF)
private val blue_light_onSurface = Color(0xFF1A1C1E)
private val blue_light_surfaceVariant = Color(0xFFDFE2EB)
private val blue_light_onSurfaceVariant = Color(0xFF43474E)
private val blue_light_outline = Color(0xFF73777F)

private val blue_dark_primary = Color(0xFF9CCAFF)
private val blue_dark_onPrimary = Color(0xFF003256)
private val blue_dark_primaryContainer = Color(0xFF00497A)
private val blue_dark_onPrimaryContainer = Color(0xFFD0E4FF)
private val blue_dark_secondary = Color(0xFFBBC7DB)
private val blue_dark_onSecondary = Color(0xFF253141)
private val blue_dark_secondaryContainer = Color(0xFF3B4858)
private val blue_dark_onSecondaryContainer = Color(0xFFD7E3F7)
private val blue_dark_tertiary = Color(0xFFD6BEE4)
private val blue_dark_onTertiary = Color(0xFF3B2947)
private val blue_dark_tertiaryContainer = Color(0xFF523F5F)
private val blue_dark_onTertiaryContainer = Color(0xFFF2DAFF)
private val blue_dark_error = Color(0xFFFFB4AB)
private val blue_dark_onError = Color(0xFF690005)
private val blue_dark_errorContainer = Color(0xFF93000A)
private val blue_dark_onErrorContainer = Color(0xFFFFDAD6)
private val blue_dark_background = Color(0xFF1A1C1E)
private val blue_dark_onBackground = Color(0xFFE2E2E6)
private val blue_dark_surface = Color(0xFF1A1C1E)
private val blue_dark_onSurface = Color(0xFFE2E2E6)
private val blue_dark_surfaceVariant = Color(0xFF43474E)
private val blue_dark_onSurfaceVariant = Color(0xFFC3C7CF)
private val blue_dark_outline = Color(0xFF8D9199)

val BlueLightColorScheme = lightColorScheme(
    primary = blue_light_primary,
    onPrimary = blue_light_onPrimary,
    primaryContainer = blue_light_primaryContainer,
    onPrimaryContainer = blue_light_onPrimaryContainer,
    secondary = blue_light_secondary,
    onSecondary = blue_light_onSecondary,
    secondaryContainer = blue_light_secondaryContainer,
    onSecondaryContainer = blue_light_onSecondaryContainer,
    tertiary = blue_light_tertiary,
    onTertiary = blue_light_onTertiary,
    tertiaryContainer = blue_light_tertiaryContainer,
    onTertiaryContainer = blue_light_onTertiaryContainer,
    error = blue_light_error,
    onError = blue_light_onError,
    errorContainer = blue_light_errorContainer,
    onErrorContainer = blue_light_onErrorContainer,
    background = blue_light_background,
    onBackground = blue_light_onBackground,
    surface = blue_light_surface,
    onSurface = blue_light_onSurface,
    surfaceVariant = blue_light_surfaceVariant,
    onSurfaceVariant = blue_light_onSurfaceVariant,
    outline = blue_light_outline,
)

val BlueDarkColorScheme = darkColorScheme(
    primary = blue_dark_primary,
    onPrimary = blue_dark_onPrimary,
    primaryContainer = blue_dark_primaryContainer,
    onPrimaryContainer = blue_dark_onPrimaryContainer,
    secondary = blue_dark_secondary,
    onSecondary = blue_dark_onSecondary,
    secondaryContainer = blue_dark_secondaryContainer,
    onSecondaryContainer = blue_dark_onSecondaryContainer,
    tertiary = blue_dark_tertiary,
    onTertiary = blue_dark_onTertiary,
    tertiaryContainer = blue_dark_tertiaryContainer,
    onTertiaryContainer = blue_dark_onTertiaryContainer,
    error = blue_dark_error,
    onError = blue_dark_onError,
    errorContainer = blue_dark_errorContainer,
    onErrorContainer = blue_dark_onErrorContainer,
    background = blue_dark_background,
    onBackground = blue_dark_onBackground,
    surface = blue_dark_surface,
    onSurface = blue_dark_onSurface,
    surfaceVariant = blue_dark_surfaceVariant,
    onSurfaceVariant = blue_dark_onSurfaceVariant,
    outline = blue_dark_outline,
)

// --- Gold (Excellent) Theme ---
val GoldLightColorScheme = lightColorScheme(
    primary = GoldLight,
    onPrimary = OnGoldLight,
    primaryContainer = GoldLightContainer,
    onPrimaryContainer = OnGoldLightContainer,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
)

val GoldDarkColorScheme = darkColorScheme(
    primary = GoldDark,
    onPrimary = OnGoldDark,
    primaryContainer = GoldDarkContainer,
    onPrimaryContainer = OnGoldDarkContainer,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
)

// --- Orange (Warning) Theme ---
val OrangeLightColorScheme = lightColorScheme(
    primary = OrangeLight,
    onPrimary = OnOrangeLight,
    primaryContainer = OrangeLightContainer,
    onPrimaryContainer = OnOrangeLightContainer,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
)

val OrangeDarkColorScheme = darkColorScheme(
    primary = OrangeDark,
    onPrimary = OnOrangeDark,
    primaryContainer = OrangeDarkContainer,
    onPrimaryContainer = OnOrangeDarkContainer,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
)
