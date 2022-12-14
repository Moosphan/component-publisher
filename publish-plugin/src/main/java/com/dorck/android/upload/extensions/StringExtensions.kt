package com.dorck.android.upload.extensions


fun String.formatCapitalize(): String {
    return replaceFirstCharacter { if (it.isLowerCase()) it.toTitleCase().toString() else it.toString() }
}

inline fun String.replaceFirstCharacter(transform: (Char) -> CharSequence): String {
    return if (isNotEmpty()) transform(this[0]).toString() + substring(1) else this
}

fun String.takeIfBlank(elseValue: () -> String?): String {
    if (isNotBlank()) {
        return this
    }
    return elseValue() ?: ""
}

fun String.checkIfLocalVersion(): Boolean = endsWith("-LOCAL")