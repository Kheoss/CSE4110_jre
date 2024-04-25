package nl.tudelft.ipv8.jvm.demo.util
import nl.tudelft.ipv8.messaging.*
import nl.tudelft.ipv8.Community

class NotificationMessage(val message: String, val messageId: String, val ttl: UInt) : Serializable {
    override fun serialize(): ByteArray {
        return serializeVarLen(message.toByteArray(Charsets.UTF_8)) + serializeVarLen(messageId.toByteArray(Charsets.UTF_8)) + serializeUInt(ttl)
    }

    companion object Deserializer : Deserializable<NotificationMessage> {
        override fun deserialize(buffer: ByteArray, offset: Int): Pair<NotificationMessage, Int> {
            var localOffset = 0
            val (message, messageLen) = deserializeVarLen(buffer, offset + localOffset)
            localOffset += messageLen

            val (messageId, messageIdLen) = deserializeVarLen(buffer, offset + localOffset )
            localOffset += messageIdLen

            val ttl = deserializeUInt(buffer, offset + localOffset)
            localOffset += SERIALIZED_UINT_SIZE

            return Pair(NotificationMessage(String(message, Charsets.UTF_8), String(messageId, Charsets.UTF_8), ttl), localOffset)
        }
    }
}