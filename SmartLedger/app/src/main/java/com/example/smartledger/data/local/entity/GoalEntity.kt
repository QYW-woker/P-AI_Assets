package com.example.smartledger.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 储蓄目标实体
 */
@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val icon: String,

    val targetAmount: Double,

    val currentAmount: Double = 0.0,

    val deadline: Long? = null,

    val note: String = "",

    val createdAt: Long = System.currentTimeMillis(),

    val isCompleted: Boolean = false
)
