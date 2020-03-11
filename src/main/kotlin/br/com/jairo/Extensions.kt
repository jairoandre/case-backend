package br.com.jairo

import br.com.jairo.constant.CaseAccess
import br.com.jairo.entity.Case
import com.google.cloud.datastore.Entity
import com.google.cloud.datastore.Value
import java.util.*

fun Entity.getListString(property: String) =
  getList<Value<String>>(property).map { it.get() }

fun Entity.getStringIfExists(property: String, default: String = "") : String =
  if (names.contains(property)) getString(property) else default

fun Entity.getTimeStampIfExists(property: String) : Date =
  if (names.contains(property)) getTimestamp(property).toDate() else Date()

fun Entity.toCase(): Case =
  Case(
    id = key.id,
    folder = getStringIfExists(Case::folder.name),
    title = getStringIfExists(Case::title.name),
    clients = getListString(Case::clients.name),
    tags = getListString(Case::tags.name),
    description = getStringIfExists(Case::description.name),
    notes = getStringIfExists(Case::notes.name),
    responsible = getStringIfExists(Case::responsible.name),
    created = getTimeStampIfExists(Case::created.name),
    access = CaseAccess.valueOf(getStringIfExists(Case::access.name, "PUBLIC"))
  )
