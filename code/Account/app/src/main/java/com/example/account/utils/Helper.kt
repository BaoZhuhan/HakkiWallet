package com.example.account.utils

import com.example.account.model.InvoiceItem
import com.example.account.model.enums.InvoiceStatus
import java.math.RoundingMode
import java.text.NumberFormat.*
import java.text.SimpleDateFormat
import java.util.*

fun getNewInvoiceId(): String {
    val alphabets: CharRange = ('A'..'Z')
    val numbers: CharRange = ('0'..'9')
    val prefix: String = List(2) { alphabets.random() }.joinToString("")
    val suffix: String = List(4) { numbers.random() }.joinToString("")
    return prefix + suffix
}

fun getStatus(status: String): InvoiceStatus {
    when (status) {
        Constants.paid -> return InvoiceStatus.Paid
        Constants.pending -> return InvoiceStatus.Pending
        Constants.draft -> return InvoiceStatus.Draft
    }
    return InvoiceStatus.Draft
}

fun getInvoiceDateForDbFormat(invoiceDate: String): String {
    return try {
        val originalFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = originalFormat.parse(invoiceDate)
        val newFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return newFormat.format(calendar.time)
    } catch (e: Exception) {
        ""
    }
}

fun getInvoiceDate(invoiceDate: String): String {
    return try {
        val originalFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = originalFormat.parse(invoiceDate)
        val newFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA)
        return newFormat.format(calendar.time)
    } catch (e: Exception) {
        ""
    }
}

fun getDueDate(invoiceDate: String, paymentTerms: Int): String {
    return try {
        val originalFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = originalFormat.parse(invoiceDate)
        calendar.add(Calendar.DATE, paymentTerms)
        val newFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA)
        newFormat.format(calendar.time)
    } catch (e: Exception) {
        ""
    }
}

fun getTotal(items: List<InvoiceItem>): String {
    var total = 0.0
    for (item in items) {
        total += item.price * item.quantity
    }
    total = total.toBigDecimal().setScale(2, RoundingMode.HALF_DOWN).toDouble()
    return getCurrencyInstance(Locale.CHINA).format(total).replace(
        Currency.getInstance(Locale.CHINA).symbol,
        "${Currency.getInstance(Locale.CHINA).symbol} "
    )
}

fun getItemTotal(item: InvoiceItem): String {
    var total = item.price * item.quantity
    total = total.toBigDecimal().setScale(2, RoundingMode.HALF_DOWN).toFloat()
    return getCurrencyInstance(Locale.CHINA).format(total).replace(
        Currency.getInstance(Locale.CHINA).symbol,
        "${Currency.getInstance(Locale.CHINA).symbol} "
    )
}

fun getItemTotal(price: Float, quantity: Int): String {
    val total = price * quantity
    return getCurrencyInstance(Locale.CHINA)
        .format(total.toBigDecimal().setScale(2, RoundingMode.HALF_DOWN).toDouble()).replace(
            Currency.getInstance(Locale.CHINA).symbol,
            "${Currency.getInstance(Locale.CHINA).symbol} "
        )
}

fun getPrice(price: Float): String {
    return getCurrencyInstance(Locale.CHINA)
        .format(price.toBigDecimal().setScale(2, RoundingMode.HALF_DOWN).toDouble()).replace(
            Currency.getInstance(Locale.CHINA).symbol,
            "${Currency.getInstance(Locale.CHINA).symbol} "
        )
}