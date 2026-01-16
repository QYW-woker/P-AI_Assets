package com.example.smartledger.data.local.database

import androidx.room.TypeConverter
import com.example.smartledger.data.local.entity.AccountType
import com.example.smartledger.data.local.entity.BudgetPeriod
import com.example.smartledger.data.local.entity.TransactionType

/**
 * Room数据库类型转换器
 */
class Converters {

    // TransactionType转换
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String {
        return type.name
    }

    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return TransactionType.valueOf(value)
    }

    // AccountType转换
    @TypeConverter
    fun fromAccountType(type: AccountType): String {
        return type.name
    }

    @TypeConverter
    fun toAccountType(value: String): AccountType {
        return AccountType.valueOf(value)
    }

    // BudgetPeriod转换
    @TypeConverter
    fun fromBudgetPeriod(period: BudgetPeriod): String {
        return period.name
    }

    @TypeConverter
    fun toBudgetPeriod(value: String): BudgetPeriod {
        return BudgetPeriod.valueOf(value)
    }

    // List<String>转换（用于tags等字段）
    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        return list?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }
}
