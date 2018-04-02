package com.gmail.uwriegel.superfit.http

import com.gmail.uwriegel.superfit.events.EventData
import com.gmail.uwriegel.superfit.sensor.data
import com.gmail.uwriegel.superfit.sensor.gpsActive
import com.gmail.uwriegel.superfit.tracking.LocationData
import com.gmail.uwriegel.superfit.tracking.currentLocation
import com.google.gson.Gson
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.Socket
import java.util.*
import kotlin.concurrent.timerTask

fun upgrade(client: Socket, header: String) {
    sockets.add(WebSocket(client, header))

    if (timer == null) {
        timer = Timer()
        timer?.schedule(timerTask {
            doAsync { uiThread {
                val socketArray = sockets.toTypedArray()
                socketArray.forEach {
                    it.sendData()
                }
            } }
        }, 0L, 500L)
    }
}

class WebSocket(private val client: Socket, header: String) {

    fun sendData() {
        val gson = Gson()
        var gps: Boolean? = null
        if (!gpsActiveSent && gpsActive) {
            gps = true
            gpsActiveSent = true
        }

        val location =
                if (currentLocation?.equals(recentLocation) != true)
                    recentLocation
                else
                    null

        recentLocation = currentLocation

        val event = EventData( data, gps, location)
        val json = gson.toJson(event)
        val text = json.toString()
        val bytes = text.toByteArray()
        send(bytes)
    }

    private fun send(bytes: ByteArray) {
        val length = bytes.size
        val FRRROPCODE = 129.toByte() //'"10000001" FIN is set, and OPCODE is 1 or Text

        val headerLength =
            if (length <= 125)
                2
            else if (length <= 65535)
                4
            else 10

        //val buffer: ByteArray = ByteArray(Math.min(20000, headerLength + length))
        val buffer: ByteArray = ByteArray(headerLength)

        var position =
            if (length <= 125) {
                buffer[0] = FRRROPCODE
                buffer[1] = length.toByte()
                2
            }
            else if (length <= 65535) {
                buffer[0] = FRRROPCODE
                buffer[1] = 126
                val byteArray = getByteArrayFromShort(length.toShort())
                buffer[2] = byteArray[0]
                buffer[3] = byteArray[1]
                4
            }
            else {
                buffer[0] = FRRROPCODE
                buffer[1] < 127
                val byteArray = getByteArrayFromLong(length.toLong())
                buffer[2] = byteArray[0]
                buffer[3] = byteArray[1]
                buffer[4] = byteArray[2]
                buffer[5] = byteArray[3]
                buffer[6] = byteArray[4]
                buffer[7] = byteArray[5]
                buffer[8] = byteArray[6]
                buffer[9] = byteArray[7]
                10
            }

        try {
            val ostream = client.getOutputStream()
            ostream.write(buffer)
            ostream.write(bytes)
            ostream.flush()
        }
        catch (e: Exception) {
            sockets.remove(this)
            if (sockets.size == 0) {
                timer?.cancel()
                timer = null
            }
        }

        val ende = 3
    }

    fun getByteArrayFromShort(n: Short): ByteArray {
        return byteArrayOf((n.toInt() ushr 8).toByte(), n.toByte())
    }

    fun getByteArrayFromLong(n: Long): ByteArray {
        return byteArrayOf((n ushr 56).toByte(), (n ushr 48).toByte(), (n ushr 40).toByte(), (n ushr 32).toByte(), (n ushr 24).toByte(), (n ushr 16).toByte(), (n ushr 8).toByte(), n.toByte())
    }

    init {
        fun getKey(): String {
            val pos = header.indexOf("Sec-WebSocket-Key")
            val posKey = header.indexOf(": ", pos) + 2
            val posEnd = header.indexOf("\r\n", posKey)
            return header.substring(posKey, posEnd)
        }

        val webSocketKeyConcat = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"
        val hashKey = encodeKey("${getKey()}$webSocketKeyConcat")
        val response = "HTTP/1.1 101 Switching Protocols\r\nConnection: Upgrade\r\nUpgrade: websocket\r\nSec-WebSocket-Accept: $hashKey\r\n\r\n"
        val ostream = client.getOutputStream()
        ostream.write(response.toByteArray())
        ostream.flush()
    }

    var recentLocation: LocationData? = null
    var gpsActiveSent = false
}

private val sockets: MutableList<WebSocket> = mutableListOf()
private var timer: Timer? = null

