package nl.tudelft.ipv8.jvm.demo.util

import nl.tudelft.ipv8.jvm.demo.Application
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class CommandListener(serverUri: URI, private val application: Application) : WebSocketClient(serverUri) {

    override fun onOpen(handshakedata: ServerHandshake?) {
        println("New connection opened")
//        send("Hello, it's me. I was wondering if after all these years you'd like to meet.")
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        println("Closed with exit code $code additional info: $reason")
    }

    override fun onMessage(message: String?) {
        println("Received message: $message")
        if (message != null) {
            application.interpretCommand(message)
        }
    }

    override fun onError(ex: Exception?) {
        println("An error occurred: ${ex?.message}")
    }

}
