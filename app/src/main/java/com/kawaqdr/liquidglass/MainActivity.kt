package com.kawaqdr.liquidglass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.kawaqdr.liquidglass.ui.LiquidGlassApp
import com.kawaqdr.liquidglass.ui.theme.LiquidGlassTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            LiquidGlassTheme {
                LiquidGlassApp()
            }
        }
    }
}
