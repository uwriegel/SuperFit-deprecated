package com.gmail.uwriegel.superfit.http

import com.gmail.uwriegel.superfit.events.TrackData
import com.gmail.uwriegel.superfit.sensor.ServiceCallback
import com.gmail.uwriegel.superfit.tracking.DataSource
import com.gmail.uwriegel.superfit.tracking.LocationData
import com.gmail.uwriegel.superfit.tracking.Track
import com.google.gson.Gson
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.Charset

val UTF8_CHARSET = Charset.forName("UTF-8")

fun startServer(serviceCallback: ServiceCallback) {
    service = serviceCallback
    Thread {
        try {
            server = ServerSocket (9865, 0, InetAddress.getLoopbackAddress())
            while (true) {
                val client = server!!.accept()
                clientConnected(client)
            }
        }
        catch (err: Exception) {}
        return@Thread
    }.start()
}

fun stopServer() {
    server?.close()
    server = null
}

private fun clientConnected(client: Socket) {
    Thread {
        try {
            val istream = client.getInputStream()
            val buffer = ByteArray(2000)
            istream.read(buffer)
            val header = String(buffer, UTF8_CHARSET)
            val method = checkMethod(header)
            var keepOpen = false
            when (method) {
                "check" -> sendResponse(client, """{"connected": true}""")
                "stop" -> {
                    if (service != null) {
                        service!!.stopService()
                        service = null
                    }
                    sendResponse(client, """{"connected": false}""")
                }
                "getTrack" -> {
                    val posLineEnd = header.indexOf("\r\n")
                    val firstLine = header.substring(0, posLineEnd)
                    val posEnd = firstLine.lastIndexOf(" ")
                    val posStart = firstLine.indexOf("/", 5) + 1
                    val trackNumber = firstLine.substring(posStart, posEnd)
                    sendTrack(client, trackNumber.toLong())
                    if (service?.stopAfterServing() == true)
                        stopServer()
                }
                else -> {
                    if (checkWebSocket((header))) {
                        upgrade(client, header)
                        keepOpen = true
                    }
                    else
                        sendResponse(client, """{"error": "method not implemented"}""")
                }
            }
            if (!keepOpen)
                client.close()
        }
        catch (err: Exception) {}
        return@Thread
    }.start()
}

private fun checkMethod(header: String): String {
    val posEnd1 = header.indexOf("/", 5)
    val posEnd2 = header.indexOf(" ", 5)
    val posEnd = Math.min(posEnd1, posEnd2)
    return header.substring(5, posEnd)
}

private fun checkWebSocket(header: String): Boolean {
    return header.indexOf("Upgrade: websocket", 0, true) != -1
}

private fun sendTrack(client: Socket, trackNumber: Long) {
    val dataSource = DataSource(service!!.getContext())
    val trackPoints = dataSource.getTrackPoints(trackNumber)
    val locationDatas = trackPoints.map { LocationData(it.longitude, it.latitude) }

    val gson = Gson()
    val json = gson.toJson(locationDatas.toList().toTypedArray())
    val text = json.toString()
    sendResponse(client, text)
}

private fun sendResponse(client: Socket, payload: String) {
    val contentLength = payload.length
    val response = "HTTP/1.1 200 OK\r\nContent-Type: text/json; charset=UTF-8\r\nContent-Length: $contentLength\r\n\r\n$payload"
    val ostream = client.getOutputStream()
    ostream.write(response.toByteArray())
    ostream.flush()
}

private var server: ServerSocket? = null
private var service: ServiceCallback? = null