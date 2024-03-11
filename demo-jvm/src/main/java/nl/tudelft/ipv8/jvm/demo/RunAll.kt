package nl.tudelft.ipv8.jvm.demo

import java.util.*

class RunAll {

    fun main() {
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                val instanceApp = Application()
                instanceApp.run()
                val runtime = Runtime.getRuntime()
                val memory = runtime.totalMemory() - runtime.freeMemory()
                println("Used memory: $memory bytes")
            }
        }, 0, 5000)
    }
}