package nl.tudelft.ipv8.jvm.demo.util
import nl.tudelft.ipv8.messaging.*

class NotificationMessage(val message: String) : Serializable {
    override fun serialize(): ByteArray {
        return message.toByteArray()
    }

    companion object Deserializer : Deserializable<NotificationMessage> {
        override fun deserialize(buffer: ByteArray, offset: Int): Pair<NotificationMessage, Int> {
            return Pair(NotificationMessage(buffer.toString(Charsets.UTF_8)), buffer.size)
        }
    }
}