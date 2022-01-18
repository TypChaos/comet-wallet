package org.ergoplatform.utils

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

private const val IPV4_PATTERN = "^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$"

fun isLocalOrIpAddress(url: String): Boolean {
    val hostname = getHostname(url)
    return hostname.equals("localhost", false) || hostname.matches(Regex(IPV4_PATTERN))
}

fun getHostname(url: String): String {
    return url.substringAfter("://").substringBefore('/').substringBefore(':')
}

fun fetchHttpGetStringSync(httpUrl: String): String {
    val request = Request.Builder().url(httpUrl).build()
    val jsonResponse = OkHttpClient().newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            throw IOException("Unexpected response code $response")
        }

        response.body()!!.string()

    }
    return jsonResponse
}