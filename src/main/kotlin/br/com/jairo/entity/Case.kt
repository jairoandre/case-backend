package br.com.jairo.entity

import br.com.jairo.constant.CaseAccess
import com.google.cloud.datastore.BaseEntity
import com.google.cloud.datastore.Entity
import com.google.cloud.datastore.FullEntity
import com.google.cloud.datastore.IncompleteKey
import java.util.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class Case(
  val id: Long? = null,
  @field:Size(max = 40, message = "The maximum size for the folder name is 40 characters")
  val folder: String = "",
  @field:NotEmpty(message = "At least one client should be informed")
  val clients: List<String>? = null,
  @field:NotEmpty(message = "The title is required")
  val title: String = "",
  val tags: List<String>? = null,
  val description: String = "",
  val notes: String = "",
  @field:NotEmpty(message = "The responsible is required")
  val responsible: String = "",
  val access: CaseAccess = CaseAccess.PUBLIC,
  @field:NotNull(message = "Creation date required")
  val created: Date = Date()
) : DatastoreEntity() {

  companion object {
    const val KIND = "cases"
  }

  private fun setProperties(builder: Any) : BaseEntity<out IncompleteKey> {
    val b = if (builder is Entity.Builder)
      builder
    else
      builder as FullEntity.Builder<*>
    return b
      .set(::folder.name, toStringValue(folder))
      .set(::title.name, toStringValue(title))
      .set(::clients.name, clients?.map { toStringValue(it) } ?: emptyList() )
      .set(::tags.name, tags?.map { toStringValue(it, false) } ?: emptyList())
      .set(::description.name, toStringValue(description) )
      .set(::notes.name, toStringValue(notes))
      .set(::responsible.name, toStringValue(responsible))
      .set(::created.name, toTimestamp(created))
      .set(::access.name, access.name)
      .build()
  }

  override fun <K: IncompleteKey> toEntity(key: K) : FullEntity<K> {
    return setProperties(Entity.newBuilder(key)) as FullEntity<K>
  }

}