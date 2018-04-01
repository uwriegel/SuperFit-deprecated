package com.gmail.uwriegel.superfit.http

import com.gmail.uwriegel.superfit.R
import com.gmail.uwriegel.superfit.activities.DisplayFragment
import com.gmail.uwriegel.superfit.sensor.data
import com.gmail.uwriegel.superfit.sensor.gpsActive
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
                sockets.forEach {
                    it.sendData()
                }
//                    if (!gpsActiveSent && gpsActive) {
//                        display!!.onGpsActive()
//                        gpsActiveSent = true
//                    }
            } }
        }, 0L, 500L)
    }
}

class WebSocket(private val client: Socket, header: String) {

    fun sendData() {
        val gson = Gson()
        val json = gson.toJson(com.gmail.uwriegel.superfit.sensor.data)
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
                val byteArray = getByteArrayFromInt(65535, 2)
                val eins = byteArray[0]
                buffer[2] = byteArray[1]
                buffer[3] = eins
                4
            }
            else {
                buffer[0] = FRRROPCODE
                buffer[1] < 127
                val byteArray = getByteArrayFromInt(0xFFFFFFFF, 8)
                val eins = byteArray[0]
                val zwei = byteArray[1]
                val drei = byteArray[2]
                val vier = byteArray[3]
                val fünf = byteArray[4]
                val sechs = byteArray[5]
                val sieben = byteArray[6]
                buffer[2] = byteArray[7]
                buffer[3] = sieben
                buffer[4] = sechs
                buffer[5] = fünf
                buffer[6] = vier
                buffer[7] = drei
                buffer[8] = zwei
                buffer[9] = eins
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

    fun getByteArrayFromInt(n: Long, size: Int): ByteArray {
        val result = ByteArray(size, {0})
        for (i: Int in 0 until size)
            result[result.size - i - 1] = n.and(0xFF).toByte()
        return result
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
}

private val sockets: MutableList<WebSocket> = mutableListOf()
private var timer: Timer? = null

