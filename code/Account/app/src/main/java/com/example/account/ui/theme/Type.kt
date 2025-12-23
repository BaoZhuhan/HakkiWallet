package com.example.account.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.account.R

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.material.MaterialTheme

val spartanFontFamily = FontFamily(
    Font(R.font.spartan_bold, weight = FontWeight.Bold),
    Font(R.font.spartan_extra_bold, weight = FontWeight.ExtraBold),
    Font(R.font.spartan_light, weight = FontWeight.Light),
    Font(R.font.spartan_extra_light, weight = FontWeight.ExtraLight),
    Font(R.font.spartan_medium, weight = FontWeight.Medium),
    Font(R.font.spartan_regular, weight = FontWeight.Normal),
    Font(R.font.spartan_semi_bold, weight = FontWeight.SemiBold),
    Font(R.font.spartan_thin, weight = FontWeight.Thin),
)

// Set of Material typography styles to start with
val Typography = Typography(
    body1 = TextStyle(
        fontFamily = spartanFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    ),
    button = TextStyle(
        fontFamily = spartanFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
    ),
    caption = TextStyle(
        fontFamily = spartanFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp
    ),
    h1 = TextStyle(
        fontFamily = spartanFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),
    h3 = TextStyle(
        fontFamily = spartanFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp
    ),
    h4 = TextStyle(
        fontFamily = spartanFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    ),
    subtitle1 = TextStyle(
        fontFamily = spartanFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp
    ),
    subtitle2 = TextStyle(
        fontFamily = spartanFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp
    )

)

/**
 * Return a scaled sp value based on screen width.
 * Uses 360dp as baseline. Screens smaller than 320dp will be clamped to 320 to avoid extremely small text.
 */
@Composable
fun scaledSp(base: Int): TextUnit {
    val screenWidth = LocalConfiguration.current.screenWidthDp.coerceAtLeast(320)
    val scale = screenWidth / 360f
    return (base * scale).sp
}

/**
 * Common app styles using the responsive scaledSp helper. These are small wrappers so screens can opt-in
 * to responsive sizing without touching the global MaterialTheme typography definitions.
 */
@Composable
fun appTitleStyle(): TextStyle = MaterialTheme.typography.h6.copy(
    fontSize = scaledSp(18),
    fontWeight = FontWeight.SemiBold
)

@Composable
fun appBodyStyle(): TextStyle = MaterialTheme.typography.body1.copy(
    fontSize = scaledSp(14)
)

@Composable
fun appSmallStyle(): TextStyle = MaterialTheme.typography.caption.copy(
    fontSize = scaledSp(12)
)

@Composable
fun appCaptionStyle(): TextStyle = MaterialTheme.typography.caption.copy(
    fontSize = scaledSp(11)
)

