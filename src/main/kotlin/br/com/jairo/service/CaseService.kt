package br.com.jairo.service

import br.com.jairo.constant.CaseVisibility
import br.com.jairo.entity.Case
import br.com.jairo.repository.CaseRepository
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.ZoneId
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

  private fun <T> getFromMap(map: Map<String, Any>, property: String)  : T? {
    val value = map[property] ?:
      return null
    return value as T
  }

  fun getById(id: Long) : Case {
    return repository.getById(id) ?:
      throw NotFoundException("Case not found for id [$id]")
  }

  fun update(payload: Map<String, Any>) : Case {

    val id = payload["id"] ?:
      throw BadRequestException("Id not informed")

    val case = getById(id as Long)

    val updated = case.copy(
      folder = getFromMap<String>(payload, Case::folder.name) ?: case.folder,
      title = getFromMap<String>(payload, Case::title.name) ?: case.title,
      clients = getFromMap<List<String>>(payload, Case::title.name) ?: case.clients,
      tags = getFromMap<List<String>>(payload, Case::tags.name) ?: case.tags,
      description = getFromMap<String>(payload, Case::description.name) ?: case.description,
      notes = getFromMap<String>(payload, Case::notes.name) ?: case.notes,
      responsible = getFromMap<String>(payload, Case::responsible.name) ?: case.responsible,
      visibility = getFromMap<CaseVisibility>(payload, Case::visibility.name) ?: case.visibility,
      created = getFromMap<Date>(payload, Case::created.name) ?: case.created
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

  fun getVisibilityEnum(idx: Int, list: List<String>) : CaseVisibility {
    if (list.size < idx + 1)
      return CaseVisibility.PUBLIC
    return CaseVisibility.valueOf(list[idx])
  }

  fun getDate(idx: Int, list: List<String>) : Date {
    if (list.size < idx + 1)
      return Date()
    val localDate = LocalDate.parse(list[idx].trim())
    return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
  }

  fun importLine(line: String) : Case? {
    val regex = """"\[[,\s\w']+\]"|[^,][\w"-]+"""".toRegex()
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
      visibility = getVisibilityEnum(7, values),
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