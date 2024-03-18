package nl.tudelft.ipv8.sqldelight

import kotlin.ByteArray
import kotlin.Long
import kotlin.String

public data class Blocks(
  public val type: String,
  public val tx: ByteArray,
  public val public_key: ByteArray,
  public val sequence_number: Long,
  public val link_public_key: ByteArray,
  public val link_sequence_number: Long,
  public val previous_hash: ByteArray,
  public val signature: ByteArray,
  public val block_timestamp: Long,
  public val insert_time: Long,
  public val block_hash: ByteArray,
)
