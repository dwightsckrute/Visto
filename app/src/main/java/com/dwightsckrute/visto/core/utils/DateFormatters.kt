package com.dwightsckrute.visto.core.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

val DEFAULT_DATE_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("es-ES"))

val SYSTEM_ZONE: ZoneId = ZoneId.systemDefault()

fun Instant.formatDate(): String =
    atZone(SYSTEM_ZONE).format(DEFAULT_DATE_FORMATTER)
