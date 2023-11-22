package com.example.codegeneratorfromjson

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

fun downloadJsonFile(urlString: String): String {
    val url = URL(urlString)
    val connection = url.openConnection()
    val reader = BufferedReader(InputStreamReader(connection.getInputStream()))
    val stringBuilder = StringBuilder()
    var line: String?
    while (reader.readLine().also { line = it } != null) {
        stringBuilder.append(line)
    }
    reader.close()
    return stringBuilder.toString()
}
