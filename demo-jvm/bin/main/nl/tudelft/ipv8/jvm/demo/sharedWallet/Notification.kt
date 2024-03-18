class Notification(val proposalId: String) : Serializable {
    override fun serialize(): ByteArray {
        return proposalId.toByteArray()
    }

    companion object Deserializer : Deserializable<Notification> {
        override fun deserialize(buffer: ByteArray, offset: Int): Pair<Notification, Int> {
            return Pair(Notification(buffer.toString(Charsets.UTF_8)), buffer.size)
        }
    }
}