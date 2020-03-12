package br.com.jairo.service

import br.com.jairo.constant.CaseAccess
import br.com.jairo.entity.Case
import br.com.jairo.repository.CaseRepository
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.function.Consumer
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.Default
import javax.inject.Inject
import javax.ws.rs.BadRequestException
import javax.ws.rs.NotFoundException

@ApplicationScoped
class CaseService {

  @Inject
  @field: Default
  lateinit var repository: CaseRepository

  fun filter(
    params: Map<String, Any>,
    offset: Int,
    limit: Int,
    order: String? = null,
    dir: String? = null) =
    repository.filter(params, offset, limit, order, dir)

  fun save(case: Case) = repository.save(case)

  private fun <T> convertFromMap(map: Map<String, Any>, property: String)  : T? {
    val value = map[property] ?:
      return null
    return value as T
  }

  private fun convertMapValueToAccessEnum(map: Map<String, Any>, property: String) : CaseAccess? {
    val value = map[property] ?:
      return null
    return CaseAccess.valueOf(value as String)
  }

  private fun convertMapValueToDate(map: Map<String, Any>, property: String) : Date? {
    val value = map[property] ?:
      return null
    // Because this come from the web
    return isoStringToDate(value as String)
  }

  fun getById(id: Long) : Case {
    return repository.getById(id) ?:
      throw NotFoundException("Case not found for id [$id]")
  }

  fun delete(id: Long) : Boolean {
    return repository.delete(id)
  }

  fun update(payload: Map<String, Any>) : Case {

    val id = payload["id"] ?:
      throw BadRequestException("Id not informed")

    val case = getById(id as Long)

    val updated = case.copy(
      folder = convertFromMap<String>(payload, Case::folder.name) ?: case.folder,
      title = convertFromMap<String>(payload, Case::title.name) ?: case.title,
      clients = convertFromMap<List<String>>(payload, Case::clients.name) ?: case.clients,
      tags = convertFromMap<List<String>>(payload, Case::tags.name) ?: case.tags,
      description = convertFromMap<String>(payload, Case::description.name) ?: case.description,
      notes = convertFromMap<String>(payload, Case::notes.name) ?: case.notes,
      responsible = convertFromMap<String>(payload, Case::responsible.name) ?: case.responsible,
      access = convertMapValueToAccessEnum(payload, Case::access.name) ?: case.access,
      created = convertMapValueToDate(payload, Case::created.name) ?: case.created
    )
    return repository.update(updated)
  }

  fun getList(idx: Int, list: List<String>) : List<String> {
    if (list.size < idx + 1)
      return emptyList()
    return list[idx]
      .replace("'", "")
      .replace("[", "")
      .replace("]", "")
      .split(",")
      .map { it.trim() }
  }

  fun getString(idx: Int, list: List<String>) : String {
    if (list.size < idx + 1)
      return ""
    return list[idx].trim()
  }

  fun getAccessEnum(idx: Int, list: List<String>) : CaseAccess {
    if (list.size < idx + 1)
      return CaseAccess.PUBLIC
    return CaseAccess.valueOf(list[idx])
  }

  private fun stringToDate(string: String) : Date {
    val localDate = LocalDate.parse(string.trim())
    return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
  }

  private fun isoStringToDate(string: String) : Date {
    val localDate = LocalDate.parse(string.trim(), DateTimeFormatter.ISO_DATE_TIME)
    return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
  }

  fun getDate(idx: Int, list: List<String>) : Date {
    if (list.size < idx + 1)
      return Date()
    return stringToDate(list[idx].trim())
  }

  fun importLine(line: String) : Case? {
    val regex = """"[^;]+"""".toRegex()
    val matchResult = regex.findAll(line)
    val values = mutableListOf<String>()
    matchResult.iterator().forEachRemaining(Consumer { values.add(it.value.replace("\"","").trim()) })
    if (values.isEmpty())
      return null
    return Case(
      folder = getString(0, values),
      clients = getList(1, values),
      title = getString(2, values),
      tags = getList(3, values),
      description = getString(4, values),
      notes = getString(5, values),
      responsible = getString(6, values),
      access = getAccessEnum(7, values),
      created = getDate(8, values)
    )
  }

  fun batchCaseCreation(inputStream: InputStream) : Map<Int, Case> {
    val mutableMap = mutableMapOf<Int, Case>()
    try {
      val reader = BufferedReader(InputStreamReader(inputStream))
      var lineCount = 0
      while (reader.ready()) {
        val line = reader.readLine()
        val case = importLine(line)
        if (case == null)
          lineCount++
        else {
          mutableMap[lineCount++] = repository.save(case)
        }
      }
      reader.close()
      return mutableMap
    } catch (ex: Exception) {
      println("Error reading input stream: ${ex.message}")
    }
    return emptyMap();
  }
}