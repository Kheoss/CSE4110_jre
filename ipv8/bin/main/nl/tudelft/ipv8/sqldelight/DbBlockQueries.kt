package nl.tudelft.ipv8.sqldelight

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.ByteArray
import kotlin.Long
import kotlin.String

public class DbBlockQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> `get`(
    public_key: ByteArray,
    sequence_number: Long,
    mapper: (
      type: String,
      tx: ByteArray,
      public_key: ByteArray,
      sequence_number: Long,
      link_public_key: ByteArray,
      link_sequence_number: Long,
      previous_hash: ByteArray,
      signature: ByteArray,
      block_timestamp: Long,
      insert_time: Long,
      block_hash: ByteArray,
    ) -> T,
  ): Query<T> = GetQuery(public_key, sequence_number) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getBytes(1)!!,
      cursor.getBytes(2)!!,
      cursor.getLong(3)!!,
      cursor.getBytes(4)!!,
      cursor.getLong(5)!!,
      cursor.getBytes(6)!!,
      cursor.getBytes(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getBytes(10)!!
    )
  }

  public fun `get`(public_key: ByteArray, sequence_number: Long): Query<Blocks> = get(public_key,
      sequence_number) { type, tx, public_key_, sequence_number_, link_public_key,
      link_sequence_number, previous_hash, signature, block_timestamp, insert_time, block_hash ->
    Blocks(
      type,
      tx,
      public_key_,
      sequence_number_,
      link_public_key,
      link_sequence_number,
      previous_hash,
      signature,
      block_timestamp,
      insert_time,
      block_hash
    )
  }

  public fun <T : Any> getAllBlocks(mapper: (
    type: String,
    tx: ByteArray,
    public_key: ByteArray,
    sequence_number: Long,
    link_public_key: ByteArray,
    link_sequence_number: Long,
    previous_hash: ByteArray,
    signature: ByteArray,
    block_timestamp: Long,
    insert_time: Long,
    block_hash: ByteArray,
  ) -> T): Query<T> = Query(-1_049_506_438, arrayOf("blocks"), driver, "DbBlock.sq", "getAllBlocks",
      "SELECT * FROM blocks") { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getBytes(1)!!,
      cursor.getBytes(2)!!,
      cursor.getLong(3)!!,
      cursor.getBytes(4)!!,
      cursor.getLong(5)!!,
      cursor.getBytes(6)!!,
      cursor.getBytes(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getBytes(10)!!
    )
  }

  public fun getAllBlocks(): Query<Blocks> = getAllBlocks { type, tx, public_key, sequence_number,
      link_public_key, link_sequence_number, previous_hash, signature, block_timestamp, insert_time,
      block_hash ->
    Blocks(
      type,
      tx,
      public_key,
      sequence_number,
      link_public_key,
      link_sequence_number,
      previous_hash,
      signature,
      block_timestamp,
      insert_time,
      block_hash
    )
  }

  public fun <T : Any> getBlockWithHash(block_hash: ByteArray, mapper: (
    type: String,
    tx: ByteArray,
    public_key: ByteArray,
    sequence_number: Long,
    link_public_key: ByteArray,
    link_sequence_number: Long,
    previous_hash: ByteArray,
    signature: ByteArray,
    block_timestamp: Long,
    insert_time: Long,
    block_hash: ByteArray,
  ) -> T): Query<T> = GetBlockWithHashQuery(block_hash) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getBytes(1)!!,
      cursor.getBytes(2)!!,
      cursor.getLong(3)!!,
      cursor.getBytes(4)!!,
      cursor.getLong(5)!!,
      cursor.getBytes(6)!!,
      cursor.getBytes(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getBytes(10)!!
    )
  }

  public fun getBlockWithHash(block_hash: ByteArray): Query<Blocks> = getBlockWithHash(block_hash) {
      type, tx, public_key, sequence_number, link_public_key, link_sequence_number, previous_hash,
      signature, block_timestamp, insert_time, block_hash_ ->
    Blocks(
      type,
      tx,
      public_key,
      sequence_number,
      link_public_key,
      link_sequence_number,
      previous_hash,
      signature,
      block_timestamp,
      insert_time,
      block_hash_
    )
  }

  public fun <T : Any> getBlocksWithType(type: String, mapper: (
    type: String,
    tx: ByteArray,
    public_key: ByteArray,
    sequence_number: Long,
    link_public_key: ByteArray,
    link_sequence_number: Long,
    previous_hash: ByteArray,
    signature: ByteArray,
    block_timestamp: Long,
    insert_time: Long,
    block_hash: ByteArray,
  ) -> T): Query<T> = GetBlocksWithTypeQuery(type) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getBytes(1)!!,
      cursor.getBytes(2)!!,
      cursor.getLong(3)!!,
      cursor.getBytes(4)!!,
      cursor.getLong(5)!!,
      cursor.getBytes(6)!!,
      cursor.getBytes(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getBytes(10)!!
    )
  }

  public fun getBlocksWithType(type: String): Query<Blocks> = getBlocksWithType(type) { type_, tx,
      public_key, sequence_number, link_public_key, link_sequence_number, previous_hash, signature,
      block_timestamp, insert_time, block_hash ->
    Blocks(
      type_,
      tx,
      public_key,
      sequence_number,
      link_public_key,
      link_sequence_number,
      previous_hash,
      signature,
      block_timestamp,
      insert_time,
      block_hash
    )
  }

  public fun <T : Any> getLatestWithType(
    public_key: ByteArray,
    type: String,
    public_key_: ByteArray,
    type_: String,
    mapper: (
      type: String,
      tx: ByteArray,
      public_key: ByteArray,
      sequence_number: Long,
      link_public_key: ByteArray,
      link_sequence_number: Long,
      previous_hash: ByteArray,
      signature: ByteArray,
      block_timestamp: Long,
      insert_time: Long,
      block_hash: ByteArray,
    ) -> T,
  ): Query<T> = GetLatestWithTypeQuery(public_key, type, public_key_, type_) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getBytes(1)!!,
      cursor.getBytes(2)!!,
      cursor.getLong(3)!!,
      cursor.getBytes(4)!!,
      cursor.getLong(5)!!,
      cursor.getBytes(6)!!,
      cursor.getBytes(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getBytes(10)!!
    )
  }

  public fun getLatestWithType(
    public_key: ByteArray,
    type: String,
    public_key_: ByteArray,
    type_: String,
  ): Query<Blocks> = getLatestWithType(public_key, type, public_key_, type_) { type__, tx,
      public_key__, sequence_number, link_public_key, link_sequence_number, previous_hash,
      signature, block_timestamp, insert_time, block_hash ->
    Blocks(
      type__,
      tx,
      public_key__,
      sequence_number,
      link_public_key,
      link_sequence_number,
      previous_hash,
      signature,
      block_timestamp,
      insert_time,
      block_hash
    )
  }

  public fun <T : Any> getLatest(
    public_key: ByteArray,
    public_key_: ByteArray,
    mapper: (
      type: String,
      tx: ByteArray,
      public_key: ByteArray,
      sequence_number: Long,
      link_public_key: ByteArray,
      link_sequence_number: Long,
      previous_hash: ByteArray,
      signature: ByteArray,
      block_timestamp: Long,
      insert_time: Long,
      block_hash: ByteArray,
    ) -> T,
  ): Query<T> = GetLatestQuery(public_key, public_key_) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getBytes(1)!!,
      cursor.getBytes(2)!!,
      cursor.getLong(3)!!,
      cursor.getBytes(4)!!,
      cursor.getLong(5)!!,
      cursor.getBytes(6)!!,
      cursor.getBytes(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getBytes(10)!!
    )
  }

  public fun getLatest(public_key: ByteArray, public_key_: ByteArray): Query<Blocks> =
      getLatest(public_key, public_key_) { type, tx, public_key__, sequence_number, link_public_key,
      link_sequence_number, previous_hash, signature, block_timestamp, insert_time, block_hash ->
    Blocks(
      type,
      tx,
      public_key__,
      sequence_number,
      link_public_key,
      link_sequence_number,
      previous_hash,
      signature,
      block_timestamp,
      insert_time,
      block_hash
    )
  }

  public fun <T : Any> getLatestBlocksWithTypes(
    public_key: ByteArray,
    type: String,
    `value`: Long,
    mapper: (
      type: String,
      tx: ByteArray,
      public_key: ByteArray,
      sequence_number: Long,
      link_public_key: ByteArray,
      link_sequence_number: Long,
      previous_hash: ByteArray,
      signature: ByteArray,
      block_timestamp: Long,
      insert_time: Long,
      block_hash: ByteArray,
    ) -> T,
  ): Query<T> = GetLatestBlocksWithTypesQuery(public_key, type, value) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getBytes(1)!!,
      cursor.getBytes(2)!!,
      cursor.getLong(3)!!,
      cursor.getBytes(4)!!,
      cursor.getLong(5)!!,
      cursor.getBytes(6)!!,
      cursor.getBytes(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getBytes(10)!!
    )
  }

  public fun getLatestBlocksWithTypes(
    public_key: ByteArray,
    type: String,
    value_: Long,
  ): Query<Blocks> = getLatestBlocksWithTypes(public_key, type, value_) { type_, tx, public_key_,
      sequence_number, link_public_key, link_sequence_number, previous_hash, signature,
      block_timestamp, insert_time, block_hash ->
    Blocks(
      type_,
      tx,
      public_key_,
      sequence_number,
      link_public_key,
      link_sequence_number,
      previous_hash,
      signature,
      block_timestamp,
      insert_time,
      block_hash
    )
  }

  public fun <T : Any> getLatestBlocks(
    public_key: ByteArray,
    `value`: Long,
    mapper: (
      type: String,
      tx: ByteArray,
      public_key: ByteArray,
      sequence_number: Long,
      link_public_key: ByteArray,
      link_sequence_number: Long,
      previous_hash: ByteArray,
      signature: ByteArray,
      block_timestamp: Long,
      insert_time: Long,
      block_hash: ByteArray,
    ) -> T,
  ): Query<T> = GetLatestBlocksQuery(public_key, value) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getBytes(1)!!,
      cursor.getBytes(2)!!,
      cursor.getLong(3)!!,
      cursor.getBytes(4)!!,
      cursor.getLong(5)!!,
      cursor.getBytes(6)!!,
      cursor.getBytes(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getBytes(10)!!
    )
  }

  public fun getLatestBlocks(public_key: ByteArray, value_: Long): Query<Blocks> =
      getLatestBlocks(public_key, value_) { type, tx, public_key_, sequence_number, link_public_key,
      link_sequence_number, previous_hash, signature, block_timestamp, insert_time, block_hash ->
    Blocks(
      type,
      tx,
      public_key_,
      sequence_number,
      link_public_key,
      link_sequence_number,
      previous_hash,
      signature,
      block_timestamp,
      insert_time,
      block_hash
    )
  }

  public fun <T : Any> getBlockAfterWithType(
    sequence_number: Long,
    public_key: ByteArray,
    type: String,
    mapper: (
      type: String,
      tx: ByteArray,
      public_key: ByteArray,
      sequence_number: Long,
      link_public_key: ByteArray,
      link_sequence_number: Long,
      previous_hash: ByteArray,
      signature: ByteArray,
      block_timestamp: Long,
      insert_time: Long,
      block_hash: ByteArray,
    ) -> T,
  ): Query<T> = GetBlockAfterWithTypeQuery(sequence_number, public_key, type) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getBytes(1)!!,
      cursor.getBytes(2)!!,
      cursor.getLong(3)!!,
      cursor.getBytes(4)!!,
      cursor.getLong(5)!!,
      cursor.getBytes(6)!!,
      cursor.getBytes(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getBytes(10)!!
    )
  }

  public fun getBlockAfterWithType(
    sequence_number: Long,
    public_key: ByteArray,
    type: String,
  ): Query<Blocks> = getBlockAfterWithType(sequence_number, public_key, type) { type_, tx,
      public_key_, sequence_number_, link_public_key, link_sequence_number, previous_hash,
      signature, block_timestamp, insert_time, block_hash ->
    Blocks(
      type_,
      tx,
      public_key_,
      sequence_number_,
      link_public_key,
      link_sequence_number,
      previous_hash,
      signature,
      block_timestamp,
      insert_time,
      block_hash
    )
  }

  public fun <T : Any> getBlockAfter(
    sequence_number: Long,
    public_key: ByteArray,
    mapper: (
      type: String,
      tx: ByteArray,
      public_key: ByteArray,
      sequence_number: Long,
      link_public_key: ByteArray,
      link_sequence_number: Long,
      previous_hash: ByteArray,
      signature: ByteArray,
      block_timestamp: Long,
      insert_time: Long,
      block_hash: ByteArray,
    ) -> T,
  ): Query<T> = GetBlockAfterQuery(sequence_number, public_key) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getBytes(1)!!,
      cursor.getBytes(2)!!,
      cursor.getLong(3)!!,
      cursor.getBytes(4)!!,
      cursor.getLong(5)!!,
      cursor.getBytes(6)!!,
      cursor.getBytes(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getBytes(10)!!
    )
  }

  public fun getBlockAfter(sequence_number: Long, public_key: ByteArray): Query<Blocks> =
      getBlockAfter(sequence_number, public_key) { type, tx, public_key_, sequence_number_,
      link_public_key, link_sequence_number, previous_hash, signature, block_timestamp, insert_time,
      block_hash ->
    Blocks(
      type,
      tx,
      public_key_,
      sequence_number_,
      link_public_key,
      link_sequence_number,
      previous_hash,
      signature,
      block_timestamp,
      insert_time,
      block_hash
    )
  }

  public fun <T : Any> getBlockBeforeWithType(
    sequence_number: Long,
    public_key: ByteArray,
    type: String,
    mapper: (
      type: String,
      tx: ByteArray,
      public_key: ByteArray,
      sequence_number: Long,
      link_public_key: ByteArray,
      link_sequence_number: Long,
      previous_hash: ByteArray,
      signature: ByteArray,
      block_timestamp: Long,
      insert_time: Long,
      block_hash: ByteArray,
    ) -> T,
  ): Query<T> = GetBlockBeforeWithTypeQuery(sequence_number, public_key, type) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getBytes(1)!!,
      cursor.getBytes(2)!!,
      cursor.getLong(3)!!,
      cursor.getBytes(4)!!,
      cursor.getLong(5)!!,
      cursor.getBytes(6)!!,
      cursor.getBytes(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getBytes(10)!!
    )
  }

  public fun getBlockBeforeWithType(
    sequence_number: Long,
    public_key: ByteArray,
    type: String,
  ): Query<Blocks> = getBlockBeforeWithType(sequence_number, public_key, type) { type_, tx,
      public_key_, sequence_number_, link_public_key, link_sequence_number, previous_hash,
      signature, block_timestamp, insert_time, block_hash ->
    Blocks(
      type_,
      tx,
      public_key_,
      sequence_number_,
      link_public_key,
      link_sequence_number,
      previous_hash,
      signature,
      block_timestamp,
      insert_time,
      block_hash
    )
  }

  public fun <T : Any> getBlockBefore(
    sequence_number: Long,
    public_key: ByteArray,
    mapper: (
      type: String,
      tx: ByteArray,
      public_key: ByteArray,
      sequence_number: Long,
      link_public_key: ByteArray,
      link_sequence_number: Long,
      previous_hash: ByteArray,
      signature: ByteArray,
      block_timestamp: Long,
      insert_time: Long,
      block_hash: ByteArray,
    ) -> T,
  ): Query<T> = GetBlockBeforeQuery(sequence_number, public_key) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getBytes(1)!!,
      cursor.getBytes(2)!!,
      cursor.getLong(3)!!,
      cursor.getBytes(4)!!,
      cursor.getLong(5)!!,
      cursor.getBytes(6)!!,
      cursor.getBytes(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getBytes(10)!!
    )
  }

  public fun getBlockBefore(sequence_number: Long, public_key: ByteArray): Query<Blocks> =
      getBlockBefore(sequence_number, public_key) { type, tx, public_key_, sequence_number_,
      link_public_key, link_sequence_number, previous_hash, signature, block_timestamp, insert_time,
      block_hash ->
    Blocks(
      type,
      tx,
      public_key_,
      sequence_number_,
      link_public_key,
      link_sequence_number,
      previous_hash,
      signature,
      block_timestamp,
      insert_time,
      block_hash
    )
  }

  public fun getLatestSequenceNumber(public_key: ByteArray, public_key_: ByteArray): Query<Long> =
      GetLatestSequenceNumberQuery(public_key, public_key_) { cursor ->
    cursor.getLong(0)!!
  }

  public fun getLowestSequenceNumberAfter(public_key: ByteArray, sequence_number: Long): Query<Long>
      = GetLowestSequenceNumberAfterQuery(public_key, sequence_number) { cursor ->
    cursor.getLong(0)!!
  }

  public fun <T : Any> getLinked(
    public_key: ByteArray,
    sequence_number: Long,
    link_public_key: ByteArray,
    link_sequence_number: Long,
    mapper: (
      type: String,
      tx: ByteArray,
      public_key: ByteArray,
      sequence_number: Long,
      link_public_key: ByteArray,
      link_sequence_number: Long,
      previous_hash: ByteArray,
      signature: ByteArray,
      block_timestamp: Long,
      insert_time: Long,
      block_hash: ByteArray,
    ) -> T,
  ): Query<T> = GetLinkedQuery(public_key, sequence_number, link_public_key, link_sequence_number) {
      cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getBytes(1)!!,
      cursor.getBytes(2)!!,
      cursor.getLong(3)!!,
      cursor.getBytes(4)!!,
      cursor.getLong(5)!!,
      cursor.getBytes(6)!!,
      cursor.getBytes(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getBytes(10)!!
    )
  }

  public fun getLinked(
    public_key: ByteArray,
    sequence_number: Long,
    link_public_key: ByteArray,
    link_sequence_number: Long,
  ): Query<Blocks> = getLinked(public_key, sequence_number, link_public_key, link_sequence_number) {
      type, tx, public_key_, sequence_number_, link_public_key_, link_sequence_number_,
      previous_hash, signature, block_timestamp, insert_time, block_hash ->
    Blocks(
      type,
      tx,
      public_key_,
      sequence_number_,
      link_public_key_,
      link_sequence_number_,
      previous_hash,
      signature,
      block_timestamp,
      insert_time,
      block_hash
    )
  }

  public fun <T : Any> getAllLinked(
    public_key: ByteArray,
    sequence_number: Long,
    link_public_key: ByteArray,
    link_sequence_number: Long,
    mapper: (
      type: String,
      tx: ByteArray,
      public_key: ByteArray,
      sequence_number: Long,
      link_public_key: ByteArray,
      link_sequence_number: Long,
      previous_hash: ByteArray,
      signature: ByteArray,
      block_timestamp: Long,
      insert_time: Long,
      block_hash: ByteArray,
    ) -> T,
  ): Query<T> = GetAllLinkedQuery(public_key, sequence_number, link_public_key,
      link_sequence_number) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getBytes(1)!!,
      cursor.getBytes(2)!!,
      cursor.getLong(3)!!,
      cursor.getBytes(4)!!,
      cursor.getLong(5)!!,
      cursor.getBytes(6)!!,
      cursor.getBytes(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getBytes(10)!!
    )
  }

  public fun getAllLinked(
    public_key: ByteArray,
    sequence_number: Long,
    link_public_key: ByteArray,
    link_sequence_number: Long,
  ): Query<Blocks> = getAllLinked(public_key, sequence_number, link_public_key,
      link_sequence_number) { type, tx, public_key_, sequence_number_, link_public_key_,
      link_sequence_number_, previous_hash, signature, block_timestamp, insert_time, block_hash ->
    Blocks(
      type,
      tx,
      public_key_,
      sequence_number_,
      link_public_key_,
      link_sequence_number_,
      previous_hash,
      signature,
      block_timestamp,
      insert_time,
      block_hash
    )
  }

  public fun <T : Any> crawl(
    sequence_number: Long,
    sequence_number_: Long,
    public_key: ByteArray,
    `value`: Long,
    link_sequence_number: Long,
    link_sequence_number_: Long,
    link_public_key: ByteArray,
    value_: Long,
    mapper: (
      type: String,
      tx: ByteArray,
      public_key: ByteArray,
      sequence_number: Long,
      link_public_key: ByteArray,
      link_sequence_number: Long,
      previous_hash: ByteArray,
      signature: ByteArray,
      block_timestamp: Long,
      insert_time: Long,
      block_hash: ByteArray,
    ) -> T,
  ): Query<T> = CrawlQuery(sequence_number, sequence_number_, public_key, value,
      link_sequence_number, link_sequence_number_, link_public_key, value_) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getBytes(1)!!,
      cursor.getBytes(2)!!,
      cursor.getLong(3)!!,
      cursor.getBytes(4)!!,
      cursor.getLong(5)!!,
      cursor.getBytes(6)!!,
      cursor.getBytes(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getBytes(10)!!
    )
  }

  public fun crawl(
    sequence_number: Long,
    sequence_number_: Long,
    public_key: ByteArray,
    value_: Long,
    link_sequence_number: Long,
    link_sequence_number_: Long,
    link_public_key: ByteArray,
    value__: Long,
  ): Query<Blocks> = crawl(sequence_number, sequence_number_, public_key, value_,
      link_sequence_number, link_sequence_number_, link_public_key, value__) { type, tx,
      public_key_, sequence_number__, link_public_key_, link_sequence_number__, previous_hash,
      signature, block_timestamp, insert_time, block_hash ->
    Blocks(
      type,
      tx,
      public_key_,
      sequence_number__,
      link_public_key_,
      link_sequence_number__,
      previous_hash,
      signature,
      block_timestamp,
      insert_time,
      block_hash
    )
  }

  public fun <T : Any> getRecentBlocks(
    `value`: Long,
    value_: Long,
    mapper: (
      type: String,
      tx: ByteArray,
      public_key: ByteArray,
      sequence_number: Long,
      link_public_key: ByteArray,
      link_sequence_number: Long,
      previous_hash: ByteArray,
      signature: ByteArray,
      block_timestamp: Long,
      insert_time: Long,
      block_hash: ByteArray,
    ) -> T,
  ): Query<T> = GetRecentBlocksQuery(value, value_) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getBytes(1)!!,
      cursor.getBytes(2)!!,
      cursor.getLong(3)!!,
      cursor.getBytes(4)!!,
      cursor.getLong(5)!!,
      cursor.getBytes(6)!!,
      cursor.getBytes(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getBytes(10)!!
    )
  }

  public fun getRecentBlocks(value_: Long, value__: Long): Query<Blocks> = getRecentBlocks(value_,
      value__) { type, tx, public_key, sequence_number, link_public_key, link_sequence_number,
      previous_hash, signature, block_timestamp, insert_time, block_hash ->
    Blocks(
      type,
      tx,
      public_key,
      sequence_number,
      link_public_key,
      link_sequence_number,
      previous_hash,
      signature,
      block_timestamp,
      insert_time,
      block_hash
    )
  }

  public fun <T : Any> getUsers(`value`: Long, mapper: (public_key: ByteArray, MAX: Long?) -> T):
      Query<T> = GetUsersQuery(value) { cursor ->
    mapper(
      cursor.getBytes(0)!!,
      cursor.getLong(1)
    )
  }

  public fun getUsers(value_: Long): Query<GetUsers> = getUsers(value_) { public_key, MAX ->
    GetUsers(
      public_key,
      MAX
    )
  }

  public fun <T : Any> getMutualBlocks(
    public_key: ByteArray,
    link_public_key: ByteArray,
    `value`: Long,
    mapper: (
      type: String,
      tx: ByteArray,
      public_key: ByteArray,
      sequence_number: Long,
      link_public_key: ByteArray,
      link_sequence_number: Long,
      previous_hash: ByteArray,
      signature: ByteArray,
      block_timestamp: Long,
      insert_time: Long,
      block_hash: ByteArray,
    ) -> T,
  ): Query<T> = GetMutualBlocksQuery(public_key, link_public_key, value) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getBytes(1)!!,
      cursor.getBytes(2)!!,
      cursor.getLong(3)!!,
      cursor.getBytes(4)!!,
      cursor.getLong(5)!!,
      cursor.getBytes(6)!!,
      cursor.getBytes(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getBytes(10)!!
    )
  }

  public fun getMutualBlocks(
    public_key: ByteArray,
    link_public_key: ByteArray,
    value_: Long,
  ): Query<Blocks> = getMutualBlocks(public_key, link_public_key, value_) { type, tx, public_key_,
      sequence_number, link_public_key_, link_sequence_number, previous_hash, signature,
      block_timestamp, insert_time, block_hash ->
    Blocks(
      type,
      tx,
      public_key_,
      sequence_number,
      link_public_key_,
      link_sequence_number,
      previous_hash,
      signature,
      block_timestamp,
      insert_time,
      block_hash
    )
  }

  public fun getBlockCountWithPublicKey(public_key: ByteArray): Query<Long> =
      GetBlockCountWithPublicKeyQuery(public_key) { cursor ->
    cursor.getLong(0)!!
  }

  public fun getBlockCount(): Query<Long> = Query(1_715_942_383, arrayOf("blocks"), driver,
      "DbBlock.sq", "getBlockCount", "SELECT COUNT(*) FROM blocks") { cursor ->
    cursor.getLong(0)!!
  }

  public fun addBlock(
    type: String,
    tx: ByteArray,
    public_key: ByteArray,
    sequence_number: Long,
    link_public_key: ByteArray,
    link_sequence_number: Long,
    previous_hash: ByteArray,
    signature: ByteArray,
    block_timestamp: Long,
    insert_time: Long,
    block_hash: ByteArray,
  ) {
    driver.execute(-1_435_862_379, """
        |INSERT INTO blocks (type, tx, public_key, sequence_number, link_public_key, link_sequence_number,
        |    previous_hash, signature, block_timestamp, insert_time, block_hash)
        |VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimMargin(), 11) {
          bindString(0, type)
          bindBytes(1, tx)
          bindBytes(2, public_key)
          bindLong(3, sequence_number)
          bindBytes(4, link_public_key)
          bindLong(5, link_sequence_number)
          bindBytes(6, previous_hash)
          bindBytes(7, signature)
          bindLong(8, block_timestamp)
          bindLong(9, insert_time)
          bindBytes(10, block_hash)
        }
    notifyQueries(-1_435_862_379) { emit ->
      emit("blocks")
    }
  }

  private inner class GetQuery<out T : Any>(
    public val public_key: ByteArray,
    public val sequence_number: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("blocks", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("blocks", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-30_066_803,
        """SELECT * FROM blocks WHERE public_key = ? AND sequence_number = ? LIMIT 1""", mapper, 2)
        {
      bindBytes(0, public_key)
      bindLong(1, sequence_number)
    }

    override fun toString(): String = "DbBlock.sq:get"
  }

  private inner class GetBlockWithHashQuery<out T : Any>(
    public val block_hash: ByteArray,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("blocks", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("blocks", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(369_804_308, """SELECT * FROM blocks WHERE block_hash = ? LIMIT 1""",
        mapper, 1) {
      bindBytes(0, block_hash)
    }

    override fun toString(): String = "DbBlock.sq:getBlockWithHash"
  }

  private inner class GetBlocksWithTypeQuery<out T : Any>(
    public val type: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("blocks", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("blocks", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_699_182_003, """SELECT * FROM blocks WHERE type = ?""", mapper, 1) {
      bindString(0, type)
    }

    override fun toString(): String = "DbBlock.sq:getBlocksWithType"
  }

  private inner class GetLatestWithTypeQuery<out T : Any>(
    public val public_key: ByteArray,
    public val type: String,
    public val public_key_: ByteArray,
    public val type_: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("blocks", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("blocks", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(2_132_911_796, """
    |SELECT * FROM blocks WHERE public_key = ? AND type = ? AND sequence_number =
    |(SELECT MAX(sequence_number) FROM blocks WHERE public_key = ? AND type = ?) LIMIT 1
    """.trimMargin(), mapper, 4) {
      bindBytes(0, public_key)
      bindString(1, type)
      bindBytes(2, public_key_)
      bindString(3, type_)
    }

    override fun toString(): String = "DbBlock.sq:getLatestWithType"
  }

  private inner class GetLatestQuery<out T : Any>(
    public val public_key: ByteArray,
    public val public_key_: ByteArray,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("blocks", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("blocks", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-482_601_612, """
    |SELECT * FROM blocks WHERE public_key = ? AND sequence_number =
    |(SELECT MAX(sequence_number) FROM blocks WHERE public_key = ?) LIMIT 1
    """.trimMargin(), mapper, 2) {
      bindBytes(0, public_key)
      bindBytes(1, public_key_)
    }

    override fun toString(): String = "DbBlock.sq:getLatest"
  }

  private inner class GetLatestBlocksWithTypesQuery<out T : Any>(
    public val public_key: ByteArray,
    public val type: String,
    public val `value`: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("blocks", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("blocks", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_762_261_081,
        """SELECT * FROM blocks WHERE public_key = ? AND type IN (?) ORDER BY sequence_number DESC LIMIT ?""",
        mapper, 3) {
      bindBytes(0, public_key)
      bindString(1, type)
      bindLong(2, value)
    }

    override fun toString(): String = "DbBlock.sq:getLatestBlocksWithTypes"
  }

  private inner class GetLatestBlocksQuery<out T : Any>(
    public val public_key: ByteArray,
    public val `value`: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("blocks", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("blocks", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-667_883_494,
        """SELECT * FROM blocks WHERE public_key = ? ORDER BY sequence_number DESC LIMIT ?""",
        mapper, 2) {
      bindBytes(0, public_key)
      bindLong(1, value)
    }

    override fun toString(): String = "DbBlock.sq:getLatestBlocks"
  }

  private inner class GetBlockAfterWithTypeQuery<out T : Any>(
    public val sequence_number: Long,
    public val public_key: ByteArray,
    public val type: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("blocks", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("blocks", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_832_340_996, """
    |SELECT * FROM blocks WHERE sequence_number > ? AND public_key = ? AND type = ?
    |ORDER BY sequence_number ASC LIMIT 1
    """.trimMargin(), mapper, 3) {
      bindLong(0, sequence_number)
      bindBytes(1, public_key)
      bindString(2, type)
    }

    override fun toString(): String = "DbBlock.sq:getBlockAfterWithType"
  }

  private inner class GetBlockAfterQuery<out T : Any>(
    public val sequence_number: Long,
    public val public_key: ByteArray,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("blocks", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("blocks", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_713_825_980, """
    |SELECT * FROM blocks WHERE sequence_number > ? AND public_key = ? ORDER BY sequence_number ASC
    |LIMIT 1
    """.trimMargin(), mapper, 2) {
      bindLong(0, sequence_number)
      bindBytes(1, public_key)
    }

    override fun toString(): String = "DbBlock.sq:getBlockAfter"
  }

  private inner class GetBlockBeforeWithTypeQuery<out T : Any>(
    public val sequence_number: Long,
    public val public_key: ByteArray,
    public val type: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("blocks", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("blocks", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_134_039_329, """
    |SELECT * FROM blocks WHERE sequence_number < ? AND public_key = ? AND type = ?
    |ORDER BY sequence_number DESC LIMIT 1
    """.trimMargin(), mapper, 3) {
      bindLong(0, sequence_number)
      bindBytes(1, public_key)
      bindString(2, type)
    }

    override fun toString(): String = "DbBlock.sq:getBlockBeforeWithType"
  }

  private inner class GetBlockBeforeQuery<out T : Any>(
    public val sequence_number: Long,
    public val public_key: ByteArray,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("blocks", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("blocks", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_616_296_095, """
    |SELECT * FROM blocks WHERE sequence_number < ? AND public_key = ? ORDER BY sequence_number DESC
    |LIMIT 1
    """.trimMargin(), mapper, 2) {
      bindLong(0, sequence_number)
      bindBytes(1, public_key)
    }

    override fun toString(): String = "DbBlock.sq:getBlockBefore"
  }

  private inner class GetLatestSequenceNumberQuery<out T : Any>(
    public val public_key: ByteArray,
    public val public_key_: ByteArray,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("blocks", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("blocks", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_259_212_546, """
    |SELECT b1.sequence_number FROM blocks b1 WHERE b1.public_key = ? AND NOT EXISTS
    |(SELECT b2.sequence_number FROM blocks b2 WHERE b2.sequence_number = b1.sequence_number + 1
    |AND b2.public_key = ?) ORDER BY b1.sequence_number LIMIT 1
    """.trimMargin(), mapper, 2) {
      bindBytes(0, public_key)
      bindBytes(1, public_key_)
    }

    override fun toString(): String = "DbBlock.sq:getLatestSequenceNumber"
  }

  private inner class GetLowestSequenceNumberAfterQuery<out T : Any>(
    public val public_key: ByteArray,
    public val sequence_number: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("blocks", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("blocks", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_600_016_525, """
    |SELECT sequence_number FROM blocks WHERE public_key = ? AND sequence_number > ?
    |ORDER BY sequence_number LIMIT 1
    """.trimMargin(), mapper, 2) {
      bindBytes(0, public_key)
      bindLong(1, sequence_number)
    }

    override fun toString(): String = "DbBlock.sq:getLowestSequenceNumberAfter"
  }

  private inner class GetLinkedQuery<out T : Any>(
    public val public_key: ByteArray,
    public val sequence_number: Long,
    public val link_public_key: ByteArray,
    public val link_sequence_number: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("blocks", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("blocks", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-475_386_874, """
    |SELECT * FROM blocks WHERE public_key = ? AND sequence_number = ? OR link_public_key = ? AND
    |link_sequence_number = ? ORDER BY block_timestamp ASC LIMIT 1
    """.trimMargin(), mapper, 4) {
      bindBytes(0, public_key)
      bindLong(1, sequence_number)
      bindBytes(2, link_public_key)
      bindLong(3, link_sequence_number)
    }

    override fun toString(): String = "DbBlock.sq:getLinked"
  }

  private inner class GetAllLinkedQuery<out T : Any>(
    public val public_key: ByteArray,
    public val sequence_number: Long,
    public val link_public_key: ByteArray,
    public val link_sequence_number: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("blocks", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("blocks", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-766_007_795, """
    |SELECT * FROM blocks WHERE public_key = ? AND sequence_number = ? OR link_public_key = ? AND
    |link_sequence_number = ?
    """.trimMargin(), mapper, 4) {
      bindBytes(0, public_key)
      bindLong(1, sequence_number)
      bindBytes(2, link_public_key)
      bindLong(3, link_sequence_number)
    }

    override fun toString(): String = "DbBlock.sq:getAllLinked"
  }

  private inner class CrawlQuery<out T : Any>(
    public val sequence_number: Long,
    public val sequence_number_: Long,
    public val public_key: ByteArray,
    public val `value`: Long,
    public val link_sequence_number: Long,
    public val link_sequence_number_: Long,
    public val link_public_key: ByteArray,
    public val value_: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("blocks", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("blocks", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_167_252_126, """
    |SELECT * FROM
    |    (SELECT * FROM blocks WHERE
    |        sequence_number >= ? AND
    |        sequence_number <= ? AND
    |        public_key = ? LIMIT ?)
    |UNION
    |SELECT * FROM
    |    (SELECT * FROM blocks WHERE
    |        link_sequence_number >= ? AND
    |        link_sequence_number <= ? AND
    |        link_sequence_number != 0 AND
    |        link_public_key = ? LIMIT ?)
    """.trimMargin(), mapper, 8) {
      bindLong(0, sequence_number)
      bindLong(1, sequence_number_)
      bindBytes(2, public_key)
      bindLong(3, value)
      bindLong(4, link_sequence_number)
      bindLong(5, link_sequence_number_)
      bindBytes(6, link_public_key)
      bindLong(7, value_)
    }

    override fun toString(): String = "DbBlock.sq:crawl"
  }

  private inner class GetRecentBlocksQuery<out T : Any>(
    public val `value`: Long,
    public val value_: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("blocks", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("blocks", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(430_387_182,
        """SELECT * FROM blocks ORDER BY block_timestamp DESC LIMIT ? OFFSET ?""", mapper, 2) {
      bindLong(0, value)
      bindLong(1, value_)
    }

    override fun toString(): String = "DbBlock.sq:getRecentBlocks"
  }

  private inner class GetUsersQuery<out T : Any>(
    public val `value`: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("blocks", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("blocks", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_794_381_435, """
    |SELECT DISTINCT public_key, MAX(sequence_number) FROM blocks GROUP BY public_key
    |ORDER BY MAX(sequence_number) DESC LIMIT ?
    """.trimMargin(), mapper, 1) {
      bindLong(0, value)
    }

    override fun toString(): String = "DbBlock.sq:getUsers"
  }

  private inner class GetMutualBlocksQuery<out T : Any>(
    public val public_key: ByteArray,
    public val link_public_key: ByteArray,
    public val `value`: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("blocks", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("blocks", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-788_499_737, """
    |SELECT * FROM blocks WHERE public_key = ? OR link_public_key = ?
    |ORDER BY block_timestamp DESC LIMIT ?
    """.trimMargin(), mapper, 3) {
      bindBytes(0, public_key)
      bindBytes(1, link_public_key)
      bindLong(2, value)
    }

    override fun toString(): String = "DbBlock.sq:getMutualBlocks"
  }

  private inner class GetBlockCountWithPublicKeyQuery<out T : Any>(
    public val public_key: ByteArray,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("blocks", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("blocks", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(199_095_649, """SELECT COUNT(*) FROM blocks WHERE public_key = ?""",
        mapper, 1) {
      bindBytes(0, public_key)
    }

    override fun toString(): String = "DbBlock.sq:getBlockCountWithPublicKey"
  }
}
