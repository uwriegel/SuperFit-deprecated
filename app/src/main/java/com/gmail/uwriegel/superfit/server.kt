package com.gmail.uwriegel.superfit

import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

/**
 * Created by uwe on 01.10.17.
 */

fun runServer(): ()->Unit {
    var server: ServerSocket? = null
    Thread {
        try {
            server = ServerSocket (9865, 0, InetAddress.getLoopbackAddress())
            while (true) {
                val client = server!!.accept()
                clientConnected(client)
            }
        }
        catch (err: Exception) {
            val affe = err.toString()
            val mist = affe + "l"
        }
        return@Thread
    }.start()
    return { -> server?.close() }
}

fun clientConnected(client: Socket) {
    Thread {
        try {
            val istream = client.getInputStream()
            while (true) {
                val buffer = ByteArray(2000)
                val read = istream.read(buffer)

                val responseBody = "Das kam davon, Affe"
                val contentLength = responseBody.length
                val response = "HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=UTF-8\r\nContent-Length: $contentLength\r\n\r\n$responseBody"
                val ostream = client.getOutputStream()
                ostream.write(response.toByteArray())
                ostream.flush()
            }
        }
        catch (err: Exception) {
            val affe = err.toString()
            val mist = affe + "l"
        }
        return@Thread
    }.start()
}