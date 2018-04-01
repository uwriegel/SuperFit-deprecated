package com.gmail.uwriegel.superfit.http

import android.util.Base64
import java.security.MessageDigest
import java.util.*

fun encodeKey(key: String): String {
    val crypt = MessageDigest.getInstance("SHA-1");
    crypt.reset();
    crypt.update(key.toByteArray())
    return Base64.encodeToString(crypt.digest(), Base64.NO_WRAP)
}

private fun byteToHex(hash: ByteArray): String {
    val formatter = Formatter()
    for (b in hash)
        formatter.format("%02x", b)

    val result = formatter.toString()
    formatter.close()
    return result
}
