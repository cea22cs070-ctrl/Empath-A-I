@file:OptIn(ExperimentalMaterial3Api::class)

package com.empathai.app

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

/* ---------- FONT ---------- */

val Poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold)
)

/* ---------- AVATAR STATE ---------- */

enum class AvatarState {
    INTRO,
    TALKING,
    NORMAL
}