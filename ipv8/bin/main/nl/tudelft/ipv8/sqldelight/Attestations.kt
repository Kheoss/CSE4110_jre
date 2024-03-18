package nl.tudelft.ipv8.sqldelight

import kotlin.ByteArray
import kotlin.String

public data class Attestations(
  public val hash: ByteArray,
  public val blob: ByteArray,
  public val key: ByteArray,
  public val id_format: String,
  public val meta_data: String?,
  public val signature: ByteArray?,
  public val attestor_key: ByteArray?,
)
