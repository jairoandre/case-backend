package br.com.jairo.entity

import com.google.cloud.Timestamp
import com.google.cloud.datastore.FullEntity
import com.google.cloud.datastore.IncompleteKey
import com.google.cloud.datastore.StringValue
import java.util.*

abstract class DatastoreEntity {

  abstract fun <K: IncompleteKey> toEntity(key: K) : FullEntity<K>

  fun toStringValue(string: String, excludeFromIndexes: Boolean = false): StringValue =
    StringValue.newBuilder(string).build()

  /**
   * Convert a java.util.Date object to google data store time object.
   */
  fun toTimestamp(date: Date): Timestamp = Timestamp.of(date)
}