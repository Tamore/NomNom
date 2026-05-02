package com.nomnom.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.nomnom.app.R

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs
)

// ── Culinary Serenity typefaces ────────────────────────────────────────────

/** Noto Serif — editorial headings, recipe titles */
val NotoSerif = FontFamily(
    Font(GoogleFont("Noto Serif"), provider, weight = FontWeight.Normal),
    Font(GoogleFont("Noto Serif"), provider, weight = FontWeight.SemiBold),
    Font(GoogleFont("Noto Serif"), provider, weight = FontWeight.Bold),
    Font(GoogleFont("Noto Serif"), provider, weight = FontWeight.ExtraBold)
)

/** Plus Jakarta Sans — UI body, labels, navigation */
val PlusJakartaSans = FontFamily(
    Font(GoogleFont("Plus Jakarta Sans"), provider, weight = FontWeight.Normal),
    Font(GoogleFont("Plus Jakarta Sans"), provider, weight = FontWeight.Medium),
    Font(GoogleFont("Plus Jakarta Sans"), provider, weight = FontWeight.SemiBold),
    Font(GoogleFont("Plus Jakarta Sans"), provider, weight = FontWeight.Bold),
    Font(GoogleFont("Plus Jakarta Sans"), provider, weight = FontWeight.ExtraBold)
)

// ── Type scale ─────────────────────────────────────────────────────────────

val NomNomTypography = Typography(
    // display-lg — recipe hero titles
    displayLarge = TextStyle(
        fontFamily   = NotoSerif,
        fontWeight   = FontWeight.Bold,
        fontSize     = 40.sp,
        lineHeight   = 48.sp,
        letterSpacing = (-0.02).em
    ),
    displayMedium = TextStyle(
        fontFamily   = NotoSerif,
        fontWeight   = FontWeight.Bold,
        fontSize     = 32.sp,
        lineHeight   = 40.sp,
        letterSpacing = (-0.01).em
    ),
    displaySmall = TextStyle(
        fontFamily   = NotoSerif,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 28.sp,
        lineHeight   = 36.sp
    ),
    // headline-md — section headers, collection names
    headlineLarge = TextStyle(
        fontFamily = NotoSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 28.sp,
        lineHeight = 36.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = NotoSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 24.sp,
        lineHeight = 32.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = NotoSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 20.sp,
        lineHeight = 28.sp
    ),
    // title-sm — card titles, screen headers
    titleLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 20.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 16.sp,
        lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 14.sp,
        lineHeight = 20.sp
    ),
    // body-md — ingredients, steps, descriptions (generous 1.6 line height)
    bodyLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize   = 16.sp,
        lineHeight = 26.sp   // ≈ 1.6x
    ),
    bodyMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        lineHeight = 21.sp   // ≈ 1.5x
    ),
    bodySmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize   = 12.sp,
        lineHeight = 18.sp
    ),
    // label-caps — chips, tags, metadata
    labelLarge = TextStyle(
        fontFamily    = PlusJakartaSans,
        fontWeight    = FontWeight.Bold,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.02.em
    ),
    labelMedium = TextStyle(
        fontFamily    = PlusJakartaSans,
        fontWeight    = FontWeight.Bold,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.05.em
    ),
    labelSmall = TextStyle(
        fontFamily    = PlusJakartaSans,
        fontWeight    = FontWeight.Bold,
        fontSize      = 11.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.08.em
    )
)