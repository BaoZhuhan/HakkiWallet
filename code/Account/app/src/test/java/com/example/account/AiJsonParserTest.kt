package com.example.account

import com.example.account.utils.AiJsonParser
import org.junit.Assert.assertEquals
import org.junit.Test

class AiJsonParserTest {

    @Test
    fun parseSingleTransaction() {
        val json = """
        {
          "id": "TX1234",
          "createdAt": "2025-12-16",
          "description": "测试交易",
          "category": "餐饮",
          "transactionType": "expense",
          "items": [
            { "name": "午餐", "amount": 45.5 }
          ]
        }
        """.trimIndent()

        val parsed = AiJsonParser.parse(json)
        assertEquals(1, parsed.size)
        val tx = parsed[0]
        assertEquals("TX1234", tx.id)
        assertEquals("测试交易", tx.description)
        assertEquals(1, tx.items.size)
        assertEquals("午餐", tx.items[0].name)
    }

    @Test
    fun parseArrayOfTransactions() {
        val json = """
        [
          {
            "id": "A1",
            "createdAt": "2025-12-01",
            "description": "t1",
            "items": [{ "name": "it1", "amount": 10 }]
          },
          {
            "id": "A2",
            "createdAt": "2025-12-02",
            "description": "t2",
            "items": [{ "name": "it2", "amount": 20 }]
          }
        ]
        """.trimIndent()

        val parsed = AiJsonParser.parse(json)
        assertEquals(2, parsed.size)
        assertEquals("A2", parsed[1].id)
    }
}

