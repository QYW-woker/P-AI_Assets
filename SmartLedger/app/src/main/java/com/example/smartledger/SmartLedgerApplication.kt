package com.example.smartledger

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * 智能记账应用程序入口
 * 使用Hilt进行依赖注入
 */
@HiltAndroidApp
class SmartLedgerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // 初始化逻辑可在此添加
    }
}
