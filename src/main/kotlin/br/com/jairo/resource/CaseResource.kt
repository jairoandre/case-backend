package br.com.jairo.resource

import br.com.jairo.constant.CaseAccess
import br.com.jairo.constant.CaseConstants
import br.com.jairo.entity.Case
import br.com.jairo.resource.common.MultipartBody
import br.com.jairo.service.CaseService
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm
import javax.enterprise.inject.Default
import javax.inject.Inject
import javax.validation.Valid
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Path("/api/case")
class CaseResource {

  @Inject
  @field: Default
  lateinit var service: CaseService

  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  fun get(@PathParam("id") id: Long) : Case {
    return service.getById(id)
  }

  @DELETE
  @Path("/{id}")
  @Produces(MediaType.TEXT_PLAIN)
  fun delete(@PathParam("id") id: Long) : String {
    return service.delete(id).toString()
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  fun post(@Valid case: Case): Case {
    return service.save(case)
  }

  @POST
  @Path("/batch")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  fun batch(@MultipartForm data: MultipartBody) : List<Case> {
    if (data.file == null)
      return emptyList()
    return service.batchCaseCreation(data.file)
  }


  /**
   * Update an exist case
   */
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  fun put(payload: Map<String, Any>): Case {
    return service.update(payload)
  }

  fun includeParam(queryParams: MutableMap<String, Any>, key: String, value: Any?) {
    if (value == null)
      return
    queryParams[key] = value
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  fun filter(
    @QueryParam(CaseConstants.FOLDER) folder: String? = null,
    @QueryParam(CaseConstants.TITLE) title: String? = null,
    @QueryParam(CaseConstants.DESCRIPTION) description: String? = null,
    @QueryParam(CaseConstants.CREATED) created: String? = null,
    @QueryParam(CaseConstants.VISIBILITY) access: CaseAccess? = null,
    @QueryParam(CaseConstants.TAGS) tags: List<String> = emptyList(),
    @QueryParam(CaseConstants.CLIENTS) clients: List<String> = emptyList(),
    @QueryParam("offset") offset: Int = 0,
    @QueryParam("limit") limit: Int = 100,
    @QueryParam("order") order: String? = null,
    @QueryParam("dir") dir: String? = null
  ): List<Case> {
    val queryParams = mutableMapOf<String, Any>();
    includeParam(queryParams, CaseConstants.FOLDER, folder)
    includeParam(queryParams, CaseConstants.TITLE, title)
    includeParam(queryParams, CaseConstants.DESCRIPTION, description)
    includeParam(queryParams, CaseConstants.CREATED, created)
    includeParam(queryParams, CaseConstants.VISIBILITY, access)
    includeParam(queryParams, CaseConstants.TAGS, if (tags.isEmpty()) null else tags)
    includeParam(queryParams, CaseConstants.CLIENTS, if (clients.isEmpty()) null else clients)
    return service.filter(queryParams, offset, if (limit == 0) 100 else limit, order, dir)
  }

}