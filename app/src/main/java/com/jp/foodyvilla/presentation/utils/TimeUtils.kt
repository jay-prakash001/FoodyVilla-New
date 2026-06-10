package com.jp.foodyvilla.presentation.utils

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

fun isOutletOpen(opensAt: String?, closesAt: String?): Boolean {
    if (opensAt.isNullOrBlank() || closesAt.isNullOrBlank()) return true
    
    return try {
        val now = LocalTime.now()
        // Supabase time format is usually HH:mm:ss
        val formatter = DateTimeFormatter.ISO_LOCAL_TIME
        val open = LocalTime.parse(opensAt, formatter)
        val close = LocalTime.parse(closesAt, formatter)
        
        if (close.isBefore(open)) {
            // Handles overnight hours (e.g., 10 PM to 4 AM)
            now.isAfter(open) || now.isBefore(close)
        } else {
            now.isAfter(open) && now.isBefore(close)
        }
    } catch (e: DateTimeParseException) {
        true // Fallback to true if we can't parse
    } catch (e: Exception) {
        true
    }
}