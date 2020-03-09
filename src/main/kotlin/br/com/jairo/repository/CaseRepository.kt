package br.com.jairo.repository

import br.com.jairo.constant.CaseConstants
import br.com.jairo.entity.Case
import br.com.jairo.toCase
import com.google.cloud.Timestamp
import com.google.cloud.datastore.*
import java.util.*
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class CaseRepository: AbstractRepository() {

  val keyFactory: KeyFactory = store.newKeyFactory().setKind("cases")

  fun fillQueryParams(queryBuilder: EntityQuery.Builder, params: Map<String, Any?>) {

    var clauses = mutableListOf<StructuredQuery.Filter>()

    params.forEach { when (it.key) {
      CaseConstants.CREATED -> clauses.add(StructuredQuery.PropertyFilter.eq(it.key, Timestamp.of(it.value as Date)))
      CaseConstants.TAGS, CaseConstants.CLIENTS -> (it.value as List<String>)
        .forEach { value -> clauses.add(StructuredQuery.PropertyFilter.eq(it.key, value)) }
      else -> clauses.add(StructuredQuery.PropertyFilter.eq(it.key, it.value as String))
    }  }

    if (clauses.isNotEmpty()) {
      if (clauses.size == 1)
        queryBuilder.setFilter(clauses.first())
      else
        queryBuilder.setFilter(StructuredQuery.CompositeFilter.and(clauses.first(), *clauses.drop(1).toTypedArray()))
    }

  }

  private fun createQueryBuilder() = Query.newEntityQueryBuilder().setKind(Case.KIND)

  fun filter(
    params: Map<String, Any>,
    offset: Int,
    limit: Int,
    order: String? = null,
    dir: String? = null) : List<Case> {

    val queryBuilder = createQueryBuilder()

    if (order != null) {
      val orderBy = if (dir === CaseConstants.DESC)
        StructuredQuery.OrderBy.desc(order)
      else
        StructuredQuery.OrderBy.asc(order)
      queryBuilder.setOrderBy(orderBy)
    }

    fillQueryParams(queryBuilder, params)
    queryBuilder.setLimit(limit)
    queryBuilder.setOffset(offset)

    val resultList = mutableListOf<Case>()
    val query: Query<Entity> = queryBuilder.build()
    try {
      val queryResult = store.run(query)
      queryResult.iterator().forEachRemaining() { resultList.add(it.toCase()) }
    } catch (ex: Exception) {
      println(ex)
    }

    return resultList
  }

  fun save(case: Case): Case {
    val key = keyFactory.newKey()
    val entity = store.put(case.toEntity(key))
    return case.copy(id = entity.key.id)
  }

  fun getById(id: Long) : Case? {
    val queryBuilder = createQueryBuilder()
    queryBuilder.setFilter(StructuredQuery.PropertyFilter.eq("__key__", keyFactory.newKey(id)))
    try {
      val queryResult = store.run(queryBuilder.build())
      return queryResult.iterator().next().toCase()
    } catch (ex: Exception) {
      println(ex)
    }
    return null
  }

  fun update(case: Case) : Case {
    val key = keyFactory.newKey(case.id!!)
    val fullEntity = case.toEntity(key)
    val entity = Entity.newBuilder(key, fullEntity).build()
    store.update(entity)
    return case
  }


}