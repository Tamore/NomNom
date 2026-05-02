package com.nomnom.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Culinary Serenity — full Material3 light color scheme
private val CulinarySerenityColorScheme = lightColorScheme(
    primary                 = CSSage,
    onPrimary               = CSOnSage,
    primaryContainer        = CSSageContainer,
    onPrimaryContainer      = CSOnSageContainer,
    inversePrimary          = CSSageDim,

    secondary               = CSTerracotta,
    onSecondary             = CSOnTerracotta,
    secondaryContainer      = CSTerracottaContainer,
    onSecondaryContainer    = CSOnTerracottaContainer,

    tertiary                = CSGold,
    onTertiary              = CSOnGold,
    tertiaryContainer       = CSGoldContainer,
    onTertiaryContainer     = CSOnGoldContainer,

    background              = CSBackground,
    onBackground            = CSOnBackground,

    surface                 = CSSurface,
    onSurface               = CSOnSurface,
    surfaceBright           = CSSurface,
    surfaceDim              = CSSurfaceDim,
    surfaceVariant          = CSSurfaceVariant,
    onSurfaceVariant        = CSOnSurfaceVariant,
    surfaceContainer        = CSSurfaceContainer,
    surfaceContainerLow     = CSSurfaceLow,
    surfaceContainerHigh    = CSSurfaceHigh,
    surfaceContainerHighest = CSSurfaceHighest,
    surfaceContainerLowest  = CSSurfaceLowest,
    inverseSurface          = CSInverseSurface,
    inverseOnSurface        = CSInverseOnSurface,

    error                   = CSError,
    onError                 = CSOnError,
    errorContainer          = CSErrorContainer,
    onErrorContainer        = CSOnErrorContainer,

    outline                 = CSOutline,
    outlineVariant          = CSOutlineVariant,
    scrim                   = CSSage,
)

@Composable
fun NomNomTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CulinarySerenityColorScheme,
        typography  = NomNomTypography,
        content     = content
    )
}