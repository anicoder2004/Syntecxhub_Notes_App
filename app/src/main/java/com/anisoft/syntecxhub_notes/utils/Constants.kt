package com.anisoft.syntecxhub_notes.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** App-wide constants and helper extensions. */
object Constants {
    const val EXTRA_NOTE_ID = "extra_note_id"
    const val NO_ID = 0L
}

/** Formats an epoch-millis timestamp into a short, human readable label. */
fun Long.toReadableDate(): String =
    SimpleDateFormat("MMM dd, yyyy  HH:mm", Locale.getDefault()).format(Date(this))

/** Returns the first [maxLength] characters of the content as a list preview. */
fun String.asPreview(maxLength: Int = 120): String =
    if (length <= maxLength) this else take(maxLength).trimEnd() + "..."
