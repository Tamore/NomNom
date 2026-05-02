package com.nomnom.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── Culinary Serenity — Primary palette ────────────────────────────────────
val CSSage              = Color(0xFF4A6549)   // primary — sage green
val CSSageContainer     = Color(0xFF8BA888)   // primary_container
val CSSageLight         = Color(0xFFCCEBC7)   // primary_fixed
val CSSageDim           = Color(0xFFB0CFAD)   // primary_fixed_dim / inverse_primary
val CSOnSage            = Color(0xFFFFFFFF)   // on_primary
val CSOnSageContainer   = Color(0xFF243D24)   // on_primary_container

val CSTerracotta        = Color(0xFF8E4E14)   // secondary
val CSTerracottaContainer = Color(0xFFFFAB69) // secondary_container
val CSOnTerracotta      = Color(0xFFFFFFFF)
val CSOnTerracottaContainer = Color(0xFF783D01)

val CSGold              = Color(0xFF765A05)   // tertiary
val CSGoldContainer     = Color(0xFFBE9C47)
val CSOnGold            = Color(0xFFFFFFFF)
val CSOnGoldContainer   = Color(0xFF463400)

// ── Surfaces & backgrounds ─────────────────────────────────────────────────
val CSBackground        = Color(0xFFFAF9F6)   // warm linen — main bg
val CSLinen             = CSBackground        // semantic alias
val CSSurface           = Color(0xFFFAF9F6)   // surface
val CSSurfaceLowest     = Color(0xFFFFFFFF)   // cards pop against linen
val CSSurfaceLow        = Color(0xFFF4F3F1)
val CSSurfaceContainer  = Color(0xFFEFEEEB)
val CSSurfaceHigh       = Color(0xFFE9E8E5)
val CSSurfaceHighest    = Color(0xFFE3E2E0)
val CSSurfaceDim        = Color(0xFFDBDAD7)
val CSSurfaceVariant    = Color(0xFFE3E2E0)   // tab indicators, dividers
val CSInverseSurface    = Color(0xFF2F312F)   // dark chip/snackbar bg
val CSInverseOnSurface  = Color(0xFFF2F1EE)

// ── Text & icons ───────────────────────────────────────────────────────────
val CSOnBackground      = Color(0xFF1A1C1A)   // primary text — charcoal
val CSOnSurface         = Color(0xFF1A1C1A)
val CSOnSurfaceVariant  = Color(0xFF434841)   // secondary text
val CSOutline           = Color(0xFF737970)   // borders, dividers
val CSOutlineVariant    = Color(0xFFC3C8BF)   // subtle dividers

// ── Semantic ──────────────────────────────────────────────────────────────
val CSError             = Color(0xFFBA1A1A)
val CSErrorContainer    = Color(0xFFFFDAD6)
val CSOnError           = Color(0xFFFFFFFF)
val CSOnErrorContainer  = Color(0xFF93000A)

// ── Gradient helpers ───────────────────────────────────────────────────────
val CSHeroGradient = listOf(
    Color(0x00FAF9F6),   // transparent linen top
    Color(0xFFFAF9F6)    // full linen bottom
)
val CSShimmer1 = Color(0xFFEFEEEB)
val CSShimmer2 = Color(0xFFE3E2E0)

// ── Legacy aliases (kept so existing screens don't break immediately) ───────
val NomNomRed          = CSTerracotta
val NomNomGold         = CSGold
val NomNomSuccess      = CSSage
val NomNomBg           = CSBackground
val NomNomDark         = CSBackground
val NomNomNavy         = CSBackground
val NomNomNavyLighter  = CSSurfaceContainer
val NomNomSurface      = CSSurfaceLowest      // cards
val NomNomSurface2     = CSSurfaceContainer   // elevated
val NomNomNavyDeep     = CSSurfaceHigh
val NomNomWhite        = CSOnBackground
val NomNomTextSub      = CSOnSurfaceVariant
val NomNomTextDim      = CSOutline
val NomNomDivider      = CSOutlineVariant
val NomNomError        = CSError
val NomNomErrorSurface = CSErrorContainer
val NomNomSuccessSurface = CSSageLight
val NomNomGoldSurface  = Color(0xFFFFF8E1)
val NomNomGradientBg   = listOf(CSBackground, CSSurfaceLow, CSSurfaceContainer)
val NomNomHeroGradient = CSHeroGradient
val NomNomShimmer1     = CSShimmer1
val NomNomShimmer2     = CSShimmer2