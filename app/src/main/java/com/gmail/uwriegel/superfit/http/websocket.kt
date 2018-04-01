package com.gmail.uwriegel.superfit.http

import java.net.Socket

fun upgrade(client: Socket, header: String) {

    fun getKey(): String {
        val pos = header.indexOf("Sec-WebSocket-Key")
        val posKey = header.indexOf(": ", pos) + 2
        val posEnd = header.indexOf("\r\n", posKey)
        return header.substring(posKey, posEnd)
    }

    val hashKey = encodeKey("${getKey()}$webSocketKeyConcat")
    val response = "HTTP/1.1 101 Switching Protocols\r\nConnection: Upgrade\r\nUpgrade: websocket\r\nSec-WebSocket-Accept: $hashKey\r\n\r\n"
    val ostream = client.getOutputStream()
    ostream.write(response.toByteArray())
    ostream.flush()
}

private val webSocketKeyConcat = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"
