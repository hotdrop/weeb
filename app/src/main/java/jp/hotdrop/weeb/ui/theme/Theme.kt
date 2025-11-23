package jp.hotdrop.weeb.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun WeebTheme(content: @Composable () -> Unit) {
    val colorScheme = darkColorScheme(
        primary = Color(0xFF9D7EF3),
    )

    val typography = androidx.compose.material3.Typography(
        bodyLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        )
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
