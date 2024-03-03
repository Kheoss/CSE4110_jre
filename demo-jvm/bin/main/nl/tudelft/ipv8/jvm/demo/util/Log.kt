package nl.tudelft.ipv8.jvm.demo.util

class Log {
    companion object {
        fun i(tag: String, msg: String) {
            println("I/$tag: $msg")
        }
    }
}
