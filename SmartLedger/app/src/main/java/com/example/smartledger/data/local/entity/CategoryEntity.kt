package com.example.smartledger.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 分类实体
 */
@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["type"]),
        Index(value = ["parentId"])
    ]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val icon: String,  // emoji或图标名

    val color: String,  // 十六进制颜色

    val type: TransactionType,  // EXPENSE或INCOME

    val parentId: Long? = null,  // 父分类ID

    val sortOrder: Int = 0,

    val isSystem: Boolean = false,  // 系统预设分类

    val isActive: Boolean = true
)
