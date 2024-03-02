package nl.tudelft.ipv8.sqldelight.ipv8

import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import kotlin.Long
import kotlin.Unit
import kotlin.reflect.KClass
import nl.tudelft.ipv8.sqldelight.Database
import nl.tudelft.ipv8.sqldelight.DbAttestationQueries
import nl.tudelft.ipv8.sqldelight.DbBlockQueries

internal val KClass<Database>.schema: SqlSchema<QueryResult.Value<Unit>>
  get() = DatabaseImpl.Schema

internal fun KClass<Database>.newInstance(driver: SqlDriver): Database = DatabaseImpl(driver)

private class DatabaseImpl(
  driver: SqlDriver,
) : TransacterImpl(driver), Database {
  override val dbAttestationQueries: DbAttestationQueries = DbAttestationQueries(driver)

  override val dbBlockQueries: DbBlockQueries = DbBlockQueries(driver)

  public object Schema : SqlSchema<QueryResult.Value<Unit>> {
    override val version: Long
      get() = 1

    override fun create(driver: SqlDriver): QueryResult.Value<Unit> {
      driver.execute(null, """
          |CREATE TABLE attestations (
          |    hash BLOB NOT NULL,
          |    blob BLOB NOT NULL,
          |    key  BLOB NOT NULL,
          |    id_format TEXT NOT NULL,
          |    meta_data TEXT,
          |    signature BLOB,
          |    attestor_key BLOB
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE authorities (
          |    public_key BLOB NOT NULL,
          |    hash TEXT NOT NULL
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE blocks (
          |    type TEXT NOT NULL,
          |    tx BLOB NOT NULL,
          |    public_key BLOB NOT NULL,
          |    sequence_number INTEGER NOT NULL,
          |    link_public_key BLOB NOT NULL,
          |    link_sequence_number INTEGER NOT NULL,
          |    previous_hash BLOB NOT NULL,
          |    signature BLOB NOT NULL,
          |    block_timestamp INTEGER NOT NULL,
          |    insert_time INTEGER NOT NULL,
          |    block_hash BLOB NOT NULL,
          |    PRIMARY KEY (public_key, sequence_number)
          |)
          """.trimMargin(), 0)
      return QueryResult.Unit
    }

    override fun migrate(
      driver: SqlDriver,
      oldVersion: Long,
      newVersion: Long,
      vararg callbacks: AfterVersion,
    ): QueryResult.Value<Unit> = QueryResult.Unit
  }
}
