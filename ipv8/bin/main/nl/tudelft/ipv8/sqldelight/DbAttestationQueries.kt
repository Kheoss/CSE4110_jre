package nl.tudelft.ipv8.sqldelight

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.ByteArray
import kotlin.String

public class DbAttestationQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> getAllAttestations(mapper: (
    hash: ByteArray,
    blob: ByteArray,
    key: ByteArray,
    id_format: String,
    meta_data: String?,
    signature: ByteArray?,
    attestor_key: ByteArray?,
  ) -> T): Query<T> = Query(-1_031_372_774, arrayOf("attestations"), driver, "DbAttestation.sq",
      "getAllAttestations", "SELECT * FROM attestations") { cursor ->
    mapper(
      cursor.getBytes(0)!!,
      cursor.getBytes(1)!!,
      cursor.getBytes(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4),
      cursor.getBytes(5),
      cursor.getBytes(6)
    )
  }

  public fun getAllAttestations(): Query<Attestations> = getAllAttestations { hash, blob, key,
      id_format, meta_data, signature, attestor_key ->
    Attestations(
      hash,
      blob,
      key,
      id_format,
      meta_data,
      signature,
      attestor_key
    )
  }

  public fun getAttestationByHash(hash: ByteArray): Query<ByteArray> =
      GetAttestationByHashQuery(hash) { cursor ->
    cursor.getBytes(0)!!
  }

  public fun <T : Any> getAllAuthorities(mapper: (public_key: ByteArray, hash: String) -> T):
      Query<T> = Query(-540_900_406, arrayOf("authorities"), driver, "DbAttestation.sq",
      "getAllAuthorities", "SELECT * FROM authorities") { cursor ->
    mapper(
      cursor.getBytes(0)!!,
      cursor.getString(1)!!
    )
  }

  public fun getAllAuthorities(): Query<Authorities> = getAllAuthorities { public_key, hash ->
    Authorities(
      public_key,
      hash
    )
  }

  public fun <T : Any> getAuthorityByPublicKey(public_key: ByteArray,
      mapper: (public_key: ByteArray, hash: String) -> T): Query<T> =
      GetAuthorityByPublicKeyQuery(public_key) { cursor ->
    mapper(
      cursor.getBytes(0)!!,
      cursor.getString(1)!!
    )
  }

  public fun getAuthorityByPublicKey(public_key: ByteArray): Query<Authorities> =
      getAuthorityByPublicKey(public_key) { public_key_, hash ->
    Authorities(
      public_key_,
      hash
    )
  }

  public fun <T : Any> getAuthorityByHash(hash: String, mapper: (public_key: ByteArray,
      hash: String) -> T): Query<T> = GetAuthorityByHashQuery(hash) { cursor ->
    mapper(
      cursor.getBytes(0)!!,
      cursor.getString(1)!!
    )
  }

  public fun getAuthorityByHash(hash: String): Query<Authorities> = getAuthorityByHash(hash) {
      public_key, hash_ ->
    Authorities(
      public_key,
      hash_
    )
  }

  public fun insertAttestation(
    hash: ByteArray,
    blob: ByteArray,
    key: ByteArray,
    id_format: String,
    meta_data: String?,
    signature: ByteArray?,
    attestor_key: ByteArray?,
  ) {
    driver.execute(1_473_264_235,
        """INSERT INTO attestations (hash, blob, key, id_format, meta_data, signature, attestor_key) VALUES(?, ?, ?, ?, ?, ?, ?)""",
        7) {
          bindBytes(0, hash)
          bindBytes(1, blob)
          bindBytes(2, key)
          bindString(3, id_format)
          bindString(4, meta_data)
          bindBytes(5, signature)
          bindBytes(6, attestor_key)
        }
    notifyQueries(1_473_264_235) { emit ->
      emit("attestations")
    }
  }

  public fun deleteAttestationByHash(hash: ByteArray) {
    driver.execute(-1_982_797_922, """DELETE FROM attestations WHERE hash = ?""", 1) {
          bindBytes(0, hash)
        }
    notifyQueries(-1_982_797_922) { emit ->
      emit("attestations")
    }
  }

  public fun insertAuthority(public_key: ByteArray, hash: String) {
    driver.execute(1_334_865_310, """INSERT INTO authorities (public_key, hash) VALUES (?, ?)""", 2)
        {
          bindBytes(0, public_key)
          bindString(1, hash)
        }
    notifyQueries(1_334_865_310) { emit ->
      emit("authorities")
    }
  }

  public fun deleteAuthorityByHash(hash: String) {
    driver.execute(-289_397_743, """DELETE FROM authorities WHERE hash = ?""", 1) {
          bindString(0, hash)
        }
    notifyQueries(-289_397_743) { emit ->
      emit("authorities")
    }
  }

  private inner class GetAttestationByHashQuery<out T : Any>(
    public val hash: ByteArray,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("attestations", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("attestations", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_537_071_019, """SELECT blob FROM attestations WHERE hash = ?""",
        mapper, 1) {
      bindBytes(0, hash)
    }

    override fun toString(): String = "DbAttestation.sq:getAttestationByHash"
  }

  private inner class GetAuthorityByPublicKeyQuery<out T : Any>(
    public val public_key: ByteArray,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("authorities", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("authorities", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-708_448_538,
        """SELECT public_key, hash FROM authorities WHERE public_key = ?""", mapper, 1) {
      bindBytes(0, public_key)
    }

    override fun toString(): String = "DbAttestation.sq:getAuthorityByPublicKey"
  }

  private inner class GetAuthorityByHashQuery<out T : Any>(
    public val hash: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("authorities", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("authorities", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(255_046_494,
        """SELECT public_key, hash FROM authorities WHERE hash = ?""", mapper, 1) {
      bindString(0, hash)
    }

    override fun toString(): String = "DbAttestation.sq:getAuthorityByHash"
  }
}
