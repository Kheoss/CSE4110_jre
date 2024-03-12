package nl.tudelft.ipv8.sqldelight

import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import kotlin.Unit
import nl.tudelft.ipv8.sqldelight.ipv8.newInstance
import nl.tudelft.ipv8.sqldelight.ipv8.schema

public interface Database : Transacter {
  public val dbAttestationQueries: DbAttestationQueries

  public val dbBlockQueries: DbBlockQueries

  public companion object {
    public val Schema: SqlSchema<QueryResult.Value<Unit>>
      get() = Database::class.schema

    public operator fun invoke(driver: SqlDriver): Database = Database::class.newInstance(driver)
  }
}
