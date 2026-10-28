package com.librompaz.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Fecha de hoy en formato AAAA-MM-DD (para dejarla como valor inicial en los formularios). */
fun hoy(): String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
