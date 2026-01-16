package com.example.smartledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.smartledger.presentation.navigation.SmartLedgerNavHost
import com.example.smartledger.presentation.ui.theme.SmartLedgerTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * 主Activity
 * 作为Compose应用的容器
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // 安装启动页
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // 启用边到边显示
        enableEdgeToEdge()

        setContent {
            SmartLedgerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SmartLedgerNavHost()
                }
            }
        }
    }
}
