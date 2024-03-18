package nl.tudelft.ipv8.sqldelight

import kotlin.ByteArray
import kotlin.Long

public data class GetUsers(
  public val public_key: ByteArray,
  public val MAX: Long?,
)
