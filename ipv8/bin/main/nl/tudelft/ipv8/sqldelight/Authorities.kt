package nl.tudelft.ipv8.sqldelight

import kotlin.ByteArray
import kotlin.String

public data class Authorities(
  public val public_key: ByteArray,
  public val hash: String,
)
