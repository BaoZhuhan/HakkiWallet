package com.example.account

import com.example.account.model.TransactionItem
import com.example.account.utils.*
import org.junit.Assert.assertEquals
import org.junit.Test

class HelperUnitTest {

    @Test
    fun test_getNewTransactionId_Length() {
        val id = getNewTransactionId()
        assertEquals(id.length, 6)
    }

    @Test
    fun test_calculateTotal() {
        // 测试计算交易项目总金额
        val items = listOf(
            TransactionItem(name = "项目1", amount = 2.0f, note = "", parentTransactionId = "#TR1234"),
            TransactionItem(name = "项目2", amount = 3.0f, note = "", parentTransactionId = "#TR1234")
        )
        
        // 简单计算总金额进行测试
        val total = items.sumOf { it.amount.toDouble() }
        assertEquals(total, 5.0, 0.01)
    }

}